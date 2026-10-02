package com.ukweather.live.data.api

import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.Call

interface OpenWeatherApi {
    @GET("data/3.0/onecall")
    fun getOneCallWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely"
    ): Call<OpenWeatherResponse>
}



