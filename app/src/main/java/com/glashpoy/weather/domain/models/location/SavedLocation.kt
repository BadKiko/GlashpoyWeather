package com.glashpoy.weather.domain.models.location

import com.glashpoy.weather.data.local.weather.SavedWeatherLocationEntity

/**
 * A domain object that represents a location saved by the user.
 */
data class SavedLocation(
    val nameOfLocation: String,
    val coordinates: Coordinates
)

/**
 * Used to map an instance of [SavedWeatherLocationEntity] to an instance of [SavedLocation].
 */
fun SavedWeatherLocationEntity.toSavedLocation() = SavedLocation(
    nameOfLocation = nameOfLocation,
    coordinates = Coordinates(latitude = latitude, longitude = longitude)
)