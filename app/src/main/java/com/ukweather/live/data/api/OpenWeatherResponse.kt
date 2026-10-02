package com.ukweather.live.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class OpenWeatherResponse(
    @Json(name = "current") val current: OpenCurrentWeather? = null,
    @Json(name = "hourly") val hourly: Array<OpenHourlyWeather>? = null,
    @Json(name = "daily") val daily: Array<OpenDailyWeather>? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenCurrentWeather(
    @Json(name = "temp") val temp: Double,
    @Json(name = "weather") val weather: Array<OpenWeatherDescription>,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "wind_deg") val windDirection: Int = 0,
    @Json(name = "pressure") val pressure: Int = 1013,
    @Json(name = "humidity") val humidity: Int = 50,
    @Json(name = "sunrise") val sunrise: Long = 0,
    @Json(name = "sunset") val sunset: Long = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenHourlyWeather(
    @Json(name = "dt") val dt: Long,
    @Json(name = "temp") val temp: Double,
    @Json(name = "weather") val weather: Array<OpenWeatherDescription>,
    @Json(name = "pop") val precipitationProbability: Double,
    @Json(name = "pressure") val pressure: Int = 1013,
    @Json(name = "humidity") val humidity: Int = 50,
    @Json(name = "visibility") val visibility: Int = 10000
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenDailyWeather(
    @Json(name = "dt") val dt: Long,
    @Json(name = "temp") val temp: OpenTempRange,
    @Json(name = "weather") val weather: Array<OpenWeatherDescription>,
    @Json(name = "wind_speed") val windSpeed: Double = 0.0,
    @Json(name = "rain") val rain: Double? = null,
    @Json(name = "uvi") val uvi: Double = 0.0,
    @Json(name = "sunrise") val sunrise: Long = 0,
    @Json(name = "sunset") val sunset: Long = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenTempRange(
    @Json(name = "day") val day: Double,
    @Json(name = "min") val min: Double,
    @Json(name = "max") val max: Double
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenWeatherDescription(
    @Json(name = "id") val id: Int, 
    @Json(name = "main") val main: String,
    @Json(name = "description") val description: String
)



