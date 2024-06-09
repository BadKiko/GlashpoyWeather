package com.glashpoy.weather.domain.models.weather

import androidx.annotation.DrawableRes
import com.glashpoy.weather.data.remote.weather.models.CurrentWeatherResponse
import com.glashpoy.weather.data.remote.weather.models.getWeatherIconResForCode
import com.glashpoy.weather.data.remote.weather.models.getWeatherImageForCode
import com.glashpoy.weather.domain.models.location.Coordinates
import kotlin.math.roundToInt

/**
 * A data class that holds the current weather details for a specific location.
 */
data class CurrentWeatherDetails(
    val nameOfLocation: String,
    val temperatureRoundedToInt: Int,
    val weatherCondition: String,
    val isDay: Int,
    @DrawableRes val iconResId: Int,
    @DrawableRes val imageResId: Int,
    val coordinates: Coordinates
)

/**
 * Used to convert an instance of [CurrentWeatherDetails] to an instance of [BriefWeatherDetails].
 */
fun CurrentWeatherDetails.toBriefWeatherDetails(): BriefWeatherDetails = BriefWeatherDetails(
    nameOfLocation = nameOfLocation,
    currentTemperatureRoundedToInt = temperatureRoundedToInt,
    shortDescription = weatherCondition,
    shortDescriptionIcon = iconResId,
    coordinates = coordinates
)


/**
 * Used to map an instance of [CurrentWeatherResponse] to an instance of [CurrentWeatherDetails]
 */
fun CurrentWeatherResponse.toCurrentWeatherDetails(nameOfLocation: String): CurrentWeatherDetails =
    CurrentWeatherDetails(
        temperatureRoundedToInt = currentWeather.temperature.roundToInt(),
        nameOfLocation = nameOfLocation,
        weatherCondition = weatherCodeToDescriptionMap.getValue(currentWeather.weatherCode),
        isDay = currentWeather.isDay,
        iconResId = getWeatherIconResForCode(
            weatherCode = currentWeather.weatherCode,
            isDay = currentWeather.isDay == 1
        ),
        imageResId = getWeatherImageForCode(
            weatherCode = currentWeather.weatherCode,
            isDay = currentWeather.isDay == 1
        ),
        coordinates = Coordinates(
            latitude = latitude,
            longitude = longitude,
        )
    )

private val weatherCodeToDescriptionMap = mapOf(
    0 to "Ясное небо",
    1 to "В основном ясно",
    2 to "Частичная облачность",
    3 to "Пасмурно",
    45 to "Туман",
    48 to "Морозный туман",
    51 to "Морось",
    53 to "Морось",
    55 to "Морось",
    56 to "Замерзающая морось",
    57 to "Замерзающая морось",
    61 to "Небольшой дождь",
    63 to "Умеренный дождь",
    65 to "Сильный дождь",
    66 to "Легкий ледяной дождь",
    67 to "Сильный ледяной дождь",
    71 to "Небольшой снегопад",
    73 to "Умеренный снегопад",
    75 to "Сильный снегопад",
    77 to "Снежные зерна",
    80 to "Небольшие дождевые ливни",
    81 to "Умеренные дождевые ливни",
    82 to "Сильные дождевые ливни",
    85 to "Небольшие снежные ливни",
    86 to "Сильные снежные ливни",
    95 to "Грозы",
    96 to "Грозы с небольшим градом",
    99 to "Грозы с сильным градом",
)

