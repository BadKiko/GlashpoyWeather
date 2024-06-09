package com.glashpoy.weather.data.repositories.location

import com.glashpoy.weather.domain.models.location.LocationAutofillSuggestion

/**
 * A repository that is responsible for managing all location related operations.
 */
interface LocationServicesRepository {
    /**
     * Fetches suggested places for a given [query] and returns an instance of [Result] containing
     * a list of [LocationAutofillSuggestion]'s if the fetch was successful.
     */
    suspend fun fetchSuggestedPlacesForQuery(query: String): Result<List<LocationAutofillSuggestion>>
}