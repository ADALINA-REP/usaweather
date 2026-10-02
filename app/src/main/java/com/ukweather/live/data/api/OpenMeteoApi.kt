package com.ukweather.live.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): Call<GeocodingResponse>

    @GET("https://api.open-meteo.com/v1/forecast")
    fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,surface_pressure,cloud_cover,visibility",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,weather_code,precipitation_probability,surface_pressure,is_day,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max,wind_speed_10m_max,apparent_temperature_max,precipitation_sum",
        @Query("timezone") timezone: String = "auto"
    ): Call<WeatherResponse>
}
