package com.ukweather.live.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: Array<GeocodingResult>? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "country_code") val country_code: String? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current") val current: CurrentWeather? = null,
    @Json(name = "hourly") val hourly: HourlyWeather,
    @Json(name = "daily") val daily: DailyWeather,
    @Json(name = "utc_offset_seconds") val utcOffsetSeconds: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "time") val time: String = "",
    @Json(name = "temperature_2m") val temperature: Double = 0.0,
    @Json(name = "relative_humidity_2m") val humidity: Int = 0,
    @Json(name = "apparent_temperature") val feelsLike: Double = 0.0,
    @Json(name = "is_day") val isDay: Int = 1,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0,
    @Json(name = "wind_direction_10m") val windDirection: Int = 0,
    @Json(name = "wind_gusts_10m") val windGusts: Double = 0.0,
    @Json(name = "surface_pressure") val surfacePressure: Double = 1013.2,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "cloud_cover") val cloudCover: Int = 0,
    @Json(name = "visibility") val visibility: Double = 10000.0
)

@Keep
@JsonClass(generateAdapter = true)
data class HourlyWeather(
    @Json(name = "time") val time: Array<String>,
    @Json(name = "temperature_2m") val temperatures: DoubleArray,
    @Json(name = "precipitation_probability") val precipitationProbability: IntArray,
    @Json(name = "weather_code") val weatherCodes: IntArray,
    @Json(name = "is_day") val isDay: IntArray? = null,
    @Json(name = "visibility") val visibility: DoubleArray? = null,
    @Json(name = "surface_pressure") val surfacePressure: DoubleArray? = null,
    @Json(name = "relative_humidity_2m") val humidity: IntArray? = null,
    @Json(name = "precipitation") val precipitation: DoubleArray? = null,
    @Json(name = "wind_speed_10m") val windSpeeds: DoubleArray? = null,
    @Json(name = "wind_direction_10m") val windDirections: IntArray? = null,
    @Json(name = "wind_gusts_10m") val windGusts: DoubleArray? = null,
    @Json(name = "cloud_cover") val cloudCover: IntArray? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: Array<String>,
    @Json(name = "weather_code") val weatherCodes: IntArray,
    @Json(name = "temperature_2m_max") val maxTemp: DoubleArray,
    @Json(name = "temperature_2m_min") val minTemp: DoubleArray,
    @Json(name = "sunrise") val sunrise: Array<String?>? = null,
    @Json(name = "sunset") val sunset: Array<String?>? = null,
    @Json(name = "moonrise") val moonrise: Array<String?>? = null,
    @Json(name = "moonset") val moonset: Array<String?>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: DoubleArray? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: IntArray? = null,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: DoubleArray? = null,
    @Json(name = "apparent_temperature_max") val apparentTempMax: DoubleArray? = null,
    @Json(name = "precipitation_sum") val precipitationSum: DoubleArray? = null,
    @Json(name = "moon_phase") val moonPhase: DoubleArray? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    @Json(name = "current") val current: CurrentAirQuality,
    @Json(name = "hourly") val hourly: HourlyAirQuality? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class CurrentAirQuality(
    @Json(name = "time") val time: String,
    @Json(name = "european_aqi") val aqi: Int = -1,
    @Json(name = "us_aqi") val usAqi: Int = -1,
    @Json(name = "pm2_5") val pm2_5: Double = 0.0,
    @Json(name = "pm10") val pm10: Double = 0.0,
    @Json(name = "carbon_monoxide") val co: Double = 0.0,
    @Json(name = "nitrogen_dioxide") val no2: Double = 0.0,
    @Json(name = "ozone") val o3: Double = 0.0,
    @Json(name = "sulphur_dioxide") val so2: Double = 0.0,
    @Json(name = "alder_pollen") val alder: Double? = 0.0,
    @Json(name = "birch_pollen") val birch: Double? = 0.0,
    @Json(name = "grass_pollen") val grass: Double? = 0.0,
    @Json(name = "mugwort_pollen") val mugwort: Double? = 0.0,
    @Json(name = "olive_pollen") val olive: Double? = 0.0,
    @Json(name = "ragweed_pollen") val ragweed: Double? = 0.0
)

@Keep
@JsonClass(generateAdapter = true)
data class HourlyAirQuality(
    @Json(name = "time") val time: Array<String>,
    @Json(name = "european_aqi") val aqi: IntArray? = null
)



