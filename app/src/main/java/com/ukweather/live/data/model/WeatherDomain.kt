package com.ukweather.live.data.model

import androidx.annotation.Keep

@Keep
data class WeatherDomain(
    val current: CurrentWeatherDomain,
    val hourly: List<HourlyWeatherDomain>,
    val daily: List<DailyWeatherDomain>,
    val source: String,
    val utcOffsetSeconds: Int = 0
)

@Keep
data class CurrentWeatherDomain(
    val temperature: Double,
    val feelsLike: Double? = null,
    val humidity: Int? = null,
    val windSpeed: Double,
    val windDirection: Int = 0,
    val windGusts: Double? = null,
    val pressure: Double = 1013.2,
    val weatherCode: Int,
    val isDay: Int,
    val precipitation: Double? = null,
    val cloudCover: Int? = null,
    val visibility: Double? = null,
    val time: String
)

@Keep
data class HourlyWeatherDomain(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val humidity: Int? = null,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val windGusts: Double? = null,
    val pressure: Double = 1013.2,
    val visibility: Double = 10000.0,
    val isDay: Int = 1,
    val cloudCover: Int? = null
)

@Keep
data class DailyWeatherDomain(
    val time: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val uvIndex: Double = 0.0,
    val precipitationProbability: Int = 0,
    val precipitationSum: Double = 0.0,
    val windSpeedMax: Double = 0.0,
    val apparentTempMax: Double = 0.0,
    val moonPhase: Double = 0.0
)



