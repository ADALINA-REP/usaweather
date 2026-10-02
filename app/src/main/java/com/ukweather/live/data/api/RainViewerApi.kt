package com.ukweather.live.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep
import retrofit2.http.GET
import retrofit2.Call

@Keep
@JsonClass(generateAdapter = true)
data class RainViewerResponse(
    @Json(name = "version") val version: String,
    @Json(name = "generated") val generated: Long,
    @Json(name = "host") val host: String,
    @Json(name = "radar") val radar: RadarData
)

@Keep
@JsonClass(generateAdapter = true)
data class RadarData(
    @Json(name = "past") val past: Array<RadarItem>,
    @Json(name = "nowcast") val nowcast: Array<RadarItem>
)

@Keep
@JsonClass(generateAdapter = true)
data class RadarItem(
    @Json(name = "time") val time: Long,
    @Json(name = "path") val path: String
)

interface RainViewerApi {
    @GET("https://api.rainviewer.com/public/weather-maps.json")
    fun getRadarMaps(): Call<RainViewerResponse>
}
