package com.ukweather.live.data.repository

import com.ukweather.live.data.api.OpenMeteoApi
import com.ukweather.live.data.api.OpenWeatherApi
import com.ukweather.live.data.api.WeatherResponse
import com.ukweather.live.data.api.OpenMeteoAirQualityApi
import com.ukweather.live.data.api.OpenMeteoAirQualityResponse
import com.ukweather.live.data.api.GeocodingApi
import com.ukweather.live.data.api.RainViewerApi
import com.ukweather.live.data.api.RainViewerResponse
import com.ukweather.live.data.api.SunriseSunsetApi
import com.ukweather.live.data.api.SunriseSunsetResponse
import com.ukweather.live.data.model.WeatherDomain
import com.ukweather.live.data.model.CurrentWeatherDomain
import com.ukweather.live.data.model.HourlyWeatherDomain
import com.ukweather.live.data.model.DailyWeatherDomain
import javax.inject.Inject
import javax.inject.Singleton

@androidx.annotation.Keep
sealed class WeatherResult<out T> {
    @androidx.annotation.Keep
    data class Success<out T>(val data: T) : WeatherResult<T>()
    @androidx.annotation.Keep
    data class Error(val message: String) : WeatherResult<Nothing>()
}

@Singleton
class WeatherRepository @Inject constructor(
    private val openMeteoApi: OpenMeteoApi,
    private val openWeatherApi: OpenWeatherApi,
    private val rainViewerApi: RainViewerApi,
    private val geocodingApi: GeocodingApi,
    private val airQualityApi: OpenMeteoAirQualityApi,
    private val sunriseSunsetApi: SunriseSunsetApi,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    @javax.inject.Named("openWeatherApiKey") private val apiKey: String
) {

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherResult<WeatherDomain> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val weatherResponse = try {
            openMeteoApi.getWeather(lat, lon, 
                current = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,surface_pressure,cloud_cover,visibility",
                hourly = "temperature_2m,relative_humidity_2m,weather_code,precipitation_probability,surface_pressure,is_day,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
                daily = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max,wind_speed_10m_max,apparent_temperature_max,precipitation_sum",
                timezone = "auto"
            ).execute().body()!!
        } catch (e: Exception) {
            return@withContext WeatherResult.Error("Weather API: ${e.message}")
        }
        
        val astroResponse = try {
            sunriseSunsetApi.getSunriseSunset(lat, lon).execute().body()
        } catch (e: Exception) {
            null
        }

        try {
            WeatherResult.Success(normalizeOpenMeteo(weatherResponse, astroResponse))
        } catch (e: Exception) {
             try {
                WeatherResult.Success(normalizeOpenMeteo(weatherResponse, null))
             } catch (e2: Exception) {
                WeatherResult.Error("Parse Err: ${e.message}")
             }
        }
    }

    suspend fun getRadarData(): RainViewerResponse? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            rainViewerApi.getRadarMaps().execute().body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCoordinatesForCity(cityName: String, langCode: String = "en"): Triple<Double, Double, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val response = geocodingApi.searchCities(cityName, language = langCode).execute().body()
            val result = response?.results?.firstOrNull()
            if (result != null) {
                Triple(result.latitude, result.longitude, result.name)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCityNameFromCoordinates(lat: Double, lon: Double, langCode: String = "en"): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        kotlinx.coroutines.withTimeoutOrNull(3000) {
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale(langCode))
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, lon, 1) { addresses ->
                            val addr = addresses.firstOrNull()
                            val name = addr?.locality ?: addr?.subLocality ?: addr?.featureName ?: addr?.subAdminArea ?: addr?.adminArea
                            continuation.resumeWith(Result.success(name))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val addr = addresses?.firstOrNull()
                    addr?.locality ?: addr?.subLocality ?: addr?.featureName ?: addr?.subAdminArea ?: addr?.adminArea
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getAirQuality(lat: Double, lon: Double): OpenMeteoAirQualityResponse? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            airQualityApi.getAirQuality(lat, lon).execute().body()
        } catch (e: Exception) {
            null
        }
    }
    private fun normalizeOpenMeteo(
        response: WeatherResponse,
        astro: SunriseSunsetResponse? = null
    ): WeatherDomain {
        val current = response.current?.let { c ->
            CurrentWeatherDomain(
                temperature = c.temperature,
                feelsLike = c.feelsLike,
                humidity = c.humidity,
                windSpeed = c.windSpeed,
                windDirection = c.windDirection ?: 0,
                windGusts = c.windGusts,
                pressure = c.surfacePressure ?: 1013.2,
                weatherCode = c.weatherCode,
                isDay = c.isDay,
                precipitation = c.precipitation ?: 0.0,
                cloudCover = c.cloudCover,
                visibility = c.visibility,
                time = c.time
            )
        } ?: CurrentWeatherDomain(
            temperature = 0.0,
            windSpeed = 0.0,
            weatherCode = 0,
            isDay = 1,
            time = java.time.LocalDateTime.now().toString()
        )

        val hourly = response.hourly.time.indices.map { i ->
            HourlyWeatherDomain(
                time = response.hourly.time[i],
                temperature = response.hourly.temperatures.getOrNull(i) ?: 0.0,
                weatherCode = response.hourly.weatherCodes.getOrNull(i) ?: 0,
                precipitationProbability = response.hourly.precipitationProbability.getOrNull(i) ?: 0,
                humidity = response.hourly.humidity?.getOrNull(i),
                windSpeed = response.hourly.windSpeeds?.getOrNull(i) ?: 0.0,
                windDirection = response.hourly.windDirections?.getOrNull(i) ?: 0,
                windGusts = response.hourly.windGusts?.getOrNull(i),
                pressure = response.hourly.surfacePressure?.getOrNull(i) ?: 1013.2,
                visibility = response.hourly.visibility?.getOrNull(i) ?: 10000.0,
                isDay = response.hourly.isDay?.getOrNull(i) ?: 1,
                cloudCover = response.hourly.cloudCover?.getOrNull(i)
            )
        }

        val daily = response.daily.time.indices.map { i ->
            DailyWeatherDomain(
                time = response.daily.time[i],
                maxTemp = response.daily.maxTemp.getOrNull(i) ?: 0.0,
                minTemp = response.daily.minTemp.getOrNull(i) ?: 0.0,
                weatherCode = response.daily.weatherCodes.getOrNull(i) ?: 0,
                sunrise = response.daily.sunrise?.getOrNull(i),
                sunset = response.daily.sunset?.getOrNull(i),
                moonrise = response.daily.moonrise?.getOrNull(i) ?: if (i == 0) astro?.results?.moonrise else null,
                moonset = response.daily.moonset?.getOrNull(i) ?: if (i == 0) astro?.results?.moonset else null,
                uvIndex = response.daily.uvIndexMax?.getOrNull(i) ?: 0.0,
                precipitationProbability = response.daily.precipitationProbabilityMax?.getOrNull(i) ?: 0,
                precipitationSum = response.daily.precipitationSum?.getOrNull(i) ?: 0.0,
                windSpeedMax = response.daily.windSpeedMax?.getOrNull(i) ?: 10.0,
                apparentTempMax = response.daily.apparentTempMax?.getOrNull(i) ?: 20.0,
                moonPhase = com.ukweather.live.util.WeatherUtils.calculateMoonPhase(response.daily.time[i])
            )
        }

        return WeatherDomain(current, hourly, daily, "Open-Meteo", response.utcOffsetSeconds)
    }
}



