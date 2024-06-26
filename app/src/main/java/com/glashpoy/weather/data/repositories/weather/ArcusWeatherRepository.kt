package com.glashpoy.weather.data.repositories.weather

import com.glashpoy.weather.data.getBodyOrThrowException
import com.glashpoy.weather.data.local.weather.ArcusDatabaseDao
import com.glashpoy.weather.data.local.weather.SavedWeatherLocationEntity
import com.glashpoy.weather.data.remote.weather.WeatherClient
import com.glashpoy.weather.domain.models.location.SavedLocation
import com.glashpoy.weather.domain.models.location.toSavedLocation
import com.glashpoy.weather.domain.models.weather.BriefWeatherDetails
import com.glashpoy.weather.domain.models.weather.CurrentWeatherDetails
import com.glashpoy.weather.domain.models.weather.HourlyForecast
import com.glashpoy.weather.domain.models.weather.PrecipitationProbability
import com.glashpoy.weather.domain.models.weather.SingleWeatherDetail
import com.glashpoy.weather.domain.models.weather.toCurrentWeatherDetails
import com.glashpoy.weather.domain.models.weather.toHourlyForecasts
import com.glashpoy.weather.domain.models.weather.toPrecipitationProbabilities
import com.glashpoy.weather.domain.models.weather.toSavedWeatherLocationEntity
import com.glashpoy.weather.domain.models.weather.toSingleWeatherDetailList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * The default concrete implementation of [WeatherRepository].
 */
class ArcusWeatherRepository @Inject constructor(
    private val weatherClient: WeatherClient,
    private val arcusDatabaseDao: ArcusDatabaseDao
) : WeatherRepository {

    override suspend fun fetchWeatherForLocation(
        nameOfLocation: String,
        latitude: String,
        longitude: String
    ): Result<CurrentWeatherDetails> = try {
        val response = weatherClient.getWeatherForCoordinates(
            latitude = latitude,
            longitude = longitude
        )
        Result.success(response.getBodyOrThrowException().toCurrentWeatherDetails(nameOfLocation))
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        Result.failure(exception)
    }

    override fun getSavedLocationsListStream(): Flow<List<SavedLocation>> = arcusDatabaseDao
        .getAllWeatherEntitiesMarkedAsNotDeleted()
        .map { savedLocationEntitiesList -> savedLocationEntitiesList.map { it.toSavedLocation() } }

    override suspend fun saveWeatherLocation(
        nameOfLocation: String,
        latitude: String,
        longitude: String
    ) {
        val savedWeatherEntity = SavedWeatherLocationEntity(
            nameOfLocation = nameOfLocation,
            latitude = latitude,
            longitude = longitude
        )
        arcusDatabaseDao.addSavedWeatherEntity(savedWeatherEntity)
    }

    override suspend fun deleteWeatherLocationFromSavedItems(briefWeatherLocation: BriefWeatherDetails) {
        val savedLocationEntity = briefWeatherLocation.toSavedWeatherLocationEntity()
        arcusDatabaseDao.markWeatherEntityAsDeleted(savedLocationEntity.nameOfLocation)
    }

    override suspend fun permanentlyDeleteWeatherLocationFromSavedItems(briefWeatherLocation: BriefWeatherDetails) {
        briefWeatherLocation.toSavedWeatherLocationEntity().run {
            arcusDatabaseDao.deleteSavedWeatherEntity(this)
        }
    }

    override suspend fun fetchHourlyPrecipitationProbabilities(
        latitude: String,
        longitude: String,
        dateRange: ClosedRange<LocalDate>
    ): Result<List<PrecipitationProbability>> = try {
        val precipitationProbabilities = weatherClient.getHourlyForecast(
            latitude = latitude,
            longitude = longitude,
            startDate = dateRange.start,
            endDate = dateRange.endInclusive
        ).getBodyOrThrowException().toPrecipitationProbabilities()
        Result.success(precipitationProbabilities)
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        Result.failure(exception)
    }

    override suspend fun fetchHourlyForecasts(
        latitude: String,
        longitude: String,
        dateRange: ClosedRange<LocalDate>
    ): Result<List<HourlyForecast>> = try {
        val hourlyForecasts = weatherClient.getHourlyForecast(
            latitude = latitude,
            longitude = longitude,
            startDate = dateRange.start,
            endDate = dateRange.endInclusive
        ).getBodyOrThrowException().toHourlyForecasts()
        Result.success(hourlyForecasts)
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        Result.failure(exception)
    }

    override suspend fun fetchAdditionalWeatherInfoItemsListForCurrentDay(
        latitude: String,
        longitude: String,
    ): Result<List<SingleWeatherDetail>> = try {
        val additionalWeatherInfoItemsList = weatherClient.getAdditionalDailyForecastVariables(
            latitude = latitude,
            longitude = longitude,
            startDate = LocalDate.now(),
            endDate = LocalDate.now()
        ).getBodyOrThrowException().toSingleWeatherDetailList()
        Result.success(additionalWeatherInfoItemsList)
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        Result.failure(exception)
    }

    override suspend fun tryRestoringDeletedWeatherLocation(nameOfLocation: String) {
        arcusDatabaseDao.markWeatherEntityAsUnDeleted(nameOfLocation)
    }
}