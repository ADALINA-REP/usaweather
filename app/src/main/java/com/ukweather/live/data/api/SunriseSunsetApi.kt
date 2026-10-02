package com.ukweather.live.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep
import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.Call

interface SunriseSunsetApi {
    @GET("https://api.sunrisesunset.io/json")
    fun getSunriseSunset(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("date") date: String = "today"
    ): Call<SunriseSunsetResponse>
}

@Keep
@JsonClass(generateAdapter = true)
data class SunriseSunsetResponse(
    @Json(name = "results") val results: SunriseSunsetResults,
    @Json(name = "status") val status: String
)

@Keep
@JsonClass(generateAdapter = true)
data class SunriseSunsetResults(
    @Json(name = "sunrise") val sunrise: String,
    @Json(name = "sunset") val sunset: String,
    @Json(name = "moonrise") val moonrise: String? = null,
    @Json(name = "moonset") val moonset: String? = null,
    @Json(name = "moon_phase") val moonPhaseName: String? = null,
    @Json(name = "moon_phase_value") val moonPhase: Double? = null,
    @Json(name = "timezone") val timezone: String
)
