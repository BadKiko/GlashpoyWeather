package com.glashpoy.weather.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glashpoy.weather.data.remote.location.ReverseGeocoder
import com.glashpoy.weather.data.repositories.location.LocationServicesRepository
import com.glashpoy.weather.data.repositories.weather.WeatherRepository
import com.glashpoy.weather.data.repositories.weather.fetchHourlyForecastsForNext24Hours
import com.glashpoy.weather.domain.location.CurrentLocationProvider
import com.glashpoy.weather.domain.models.weather.BriefWeatherDetails
import com.glashpoy.weather.domain.models.weather.CurrentWeatherDetails
import com.glashpoy.weather.domain.models.location.SavedLocation
import com.glashpoy.weather.domain.models.weather.toBriefWeatherDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentLocationProvider: CurrentLocationProvider,
    private val reverseGeocoder: ReverseGeocoder,
    private val locationServicesRepository: LocationServicesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val currentSearchQuery = MutableStateFlow("")
    private val isCurrentlyRetryingToFetchSavedLocation = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState as StateFlow<HomeScreenUiState>

    // a cache that stores the CurrentWeatherDetails of a specific SavedLocation
    private var currentWeatherDetailsCache = mutableMapOf<SavedLocation, CurrentWeatherDetails>()
    private var recentlyDeletedItem: BriefWeatherDetails? = null

    init {
        // saved locations stream
        combine(
            weatherRepository.getSavedLocationsListStream(),
            isCurrentlyRetryingToFetchSavedLocation
        ) { savedLocations, _ ->
            savedLocations
        }.onEach {
            _uiState.update {
                it.copy(
                    isLoadingSavedLocations = true,
                    errorFetchingWeatherForSavedLocations = false
                )
            }
        }.map { savedLocations ->
            fetchCurrentWeatherDetailsWithCache(savedLocations)
        }.onEach { weatherDetailsOfSavedLocationsResult ->
            val weatherDetailsOfSavedLocations =
                weatherDetailsOfSavedLocationsResult.getOrNull()
            _uiState.update {
                it.copy(
                    isLoadingSavedLocations = false,
                    weatherDetailsOfSavedLocations = weatherDetailsOfSavedLocations ?: emptyList(),
                    errorFetchingWeatherForSavedLocations = weatherDetailsOfSavedLocations == null
                )
            }
            isCurrentlyRetryingToFetchSavedLocation.update { false }
        }.launchIn(viewModelScope)

        // suggestions for current search query stream
        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        currentSearchQuery.debounce(250)
            .distinctUntilChanged()
            .mapLatest { query ->
                if (query.isBlank()) return@mapLatest Result.success(emptyList())
                _uiState.update {
                    it.copy(
                        isLoadingAutofillSuggestions = true,
                        errorFetchingAutofillSuggestions = false
                    )
                }
                locationServicesRepository.fetchSuggestedPlacesForQuery(query)
            }
            .onEach { autofillSuggestionsResult ->
                val autofillSuggestions = autofillSuggestionsResult.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoadingAutofillSuggestions = false,
                        autofillSuggestions = autofillSuggestions ?: emptyList(),
                        errorFetchingAutofillSuggestions = autofillSuggestions == null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun retryFetchingSavedLocations() {
        val isCurrentlyRetrying = isCurrentlyRetryingToFetchSavedLocation.value
        if (isCurrentlyRetrying) return
        isCurrentlyRetryingToFetchSavedLocation.update { true }
    }

    /**
     * Used to set the [searchQuery] for which the suggestions should be generated.
     */
    fun setSearchQueryForSuggestionsGeneration(searchQuery: String) {
        currentSearchQuery.value = searchQuery
    }

    fun deleteSavedWeatherLocation(briefWeatherDetails: BriefWeatherDetails) {
        recentlyDeletedItem = briefWeatherDetails
        viewModelScope.launch {
            weatherRepository.deleteWeatherLocationFromSavedItems(briefWeatherDetails)
        }
    }

    fun restoreRecentlyDeletedItem() {
        recentlyDeletedItem?.let {
            viewModelScope.launch { weatherRepository.tryRestoringDeletedWeatherLocation(it.nameOfLocation) }
        }
    }

    /**
     * Used to fetch a list of [BriefWeatherDetails] for all the [savedLocations] efficiently
     * using the [currentWeatherDetailsCache]
     */
    private suspend fun fetchCurrentWeatherDetailsWithCache(savedLocations: List<SavedLocation>): Result<List<BriefWeatherDetails>?> {
        val savedLocationsSet = savedLocations.toSet()
        // remove locations in the cache that have been deleted by the user
        val removedLocations = currentWeatherDetailsCache.keys subtract savedLocationsSet
        for (removedLocation in removedLocations) {
            currentWeatherDetailsCache.remove(removedLocation)
        }
        // only fetch weather details of the items that are not in cache.
        val locationsNotInCache = savedLocationsSet subtract currentWeatherDetailsCache.keys
        for (savedLocationNotInCache in locationsNotInCache) {
            try {
                weatherRepository.fetchWeatherForLocation(
                    nameOfLocation = savedLocationNotInCache.nameOfLocation,
                    latitude = savedLocationNotInCache.coordinates.latitude,
                    longitude = savedLocationNotInCache.coordinates.longitude
                ).getOrThrow().also { currentWeatherDetailsCache[savedLocationNotInCache] = it }
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                return Result.failure(exception)
            }
        }
        return Result.success(
            currentWeatherDetailsCache.values.toList().map { it.toBriefWeatherDetails() })
    }

    fun fetchWeatherForCurrentUserLocation() {
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = false,
                    errorFetchingWeatherForCurrentLocation = true
                )
            }
        }
        viewModelScope.launch(exceptionHandler) {

            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = true,
                    errorFetchingWeatherForCurrentLocation = false
                )
            }

            val coordinates = currentLocationProvider.getCurrentLocation().getOrThrow()
            val nameOfLocation = reverseGeocoder.getLocationNameForCoordinates(
                coordinates.latitude.toDouble(),
                coordinates.longitude.toDouble()
            ).getOrThrow()

            val weatherDetailsForCurrentLocation = async {
                weatherRepository.fetchWeatherForLocation(
                    nameOfLocation = nameOfLocation,
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude
                ).getOrThrow().toBriefWeatherDetails()
            }

            val hourlyForecastsForCurrentLocation = async {
                weatherRepository.fetchHourlyForecastsForNext24Hours(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude
                ).getOrThrow()
            }
            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = false,
                    errorFetchingWeatherForCurrentLocation = false,
                    weatherDetailsOfCurrentLocation = weatherDetailsForCurrentLocation.await(),
                    hourlyForecastsForCurrentLocation = hourlyForecastsForCurrentLocation.await(),
                )
            }
        }
    }
}