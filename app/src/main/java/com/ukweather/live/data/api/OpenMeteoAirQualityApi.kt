package com.ukweather.live.data.api

import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.Call

interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality")
    fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "european_aqi,us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide,ozone,sulphur_dioxide,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen",
        @Query("hourly") hourly: String = "european_aqi,us_aqi",
        @Query("timezone") timezone: String = "auto"
    ): Call<OpenMeteoAirQualityResponse>
}



