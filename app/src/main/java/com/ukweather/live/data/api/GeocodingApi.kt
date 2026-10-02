package com.ukweather.live.data.api

import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.Call

interface GeocodingApi {
    @GET("v1/search")
    fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 50,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): Call<GeocodingResponse>
    @GET("v1/reverse")
    fun reverseGeocode(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): Call<GeocodingResponse>
}



