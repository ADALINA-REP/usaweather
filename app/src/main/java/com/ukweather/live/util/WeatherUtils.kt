package com.ukweather.live.util

import androidx.compose.ui.graphics.Color
import com.ukweather.live.ui.theme.*
import com.ukweather.live.data.model.HourlyWeatherDomain
import com.ukweather.live.R

enum class WeatherState { SUNNY, RAIN, CLOUDY, NIGHT, SNOW, STORM, FOG, DRIZZLE }

object WeatherUtils {

    fun formatTemperature(temp: Double, isCelsius: Boolean): String {
        return if (isCelsius) {
            "${temp.toInt()}°C"
        } else {
            val fahrenheit = (temp * 9 / 5) + 32
            "${fahrenheit.toInt()}°F"
        }
    }

    fun mapWeatherCode(code: Int, isDay: Int): WeatherState {
        if (isDay == 0) return WeatherState.NIGHT
        return when (code) {
            0, 1 -> WeatherState.SUNNY
            2, 3 -> WeatherState.CLOUDY
            45, 48 -> WeatherState.FOG
            51, 53, 55 -> WeatherState.DRIZZLE
            56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherState.RAIN
            95, 96, 99 -> WeatherState.STORM
            71, 73, 75, 77, 85, 86 -> WeatherState.SNOW
            else -> WeatherState.SUNNY
        }
    }

    fun getGradientForState(state: WeatherState): List<Color> = when (state) {
        WeatherState.SUNNY -> listOf(WeatherSunnyStart, WeatherSunnyEnd)
        WeatherState.NIGHT -> listOf(WeatherNightStart, WeatherNightEnd)
        WeatherState.RAIN -> listOf(WeatherRainyStart, WeatherRainyEnd)
        WeatherState.CLOUDY -> listOf(WeatherCloudyStart, WeatherCloudyEnd)
        WeatherState.SNOW -> listOf(WeatherSnowStart, WeatherSnowEnd)
        WeatherState.STORM -> listOf(WeatherStormyStart, WeatherStormyEnd)
        WeatherState.FOG -> listOf(WeatherFogStart, WeatherFogEnd)
        WeatherState.DRIZZLE -> listOf(WeatherRainyStart, WeatherRainyEnd)
    }

    data class WeatherInfo(
        val descriptionResId: Int,
        val iconEmoji: String,
        val animation: WeatherState,
        val backgroundResName: String
    )

    fun getWeatherInfo(code: Int, isDay: Int = 1): WeatherInfo {
        val animation = mapWeatherCode(code, isDay)
        val isNight = isDay == 0

        return when (code) {
            0 -> WeatherInfo(
                descriptionResId = if (isNight) R.string.clear_sky else R.string.sunny,
                iconEmoji = if (isNight) "🌙" else "☀️",
                animation = animation,
                backgroundResName = if (isNight) "bg_clear_night" else "bg_clear_day"
            )
            1 -> WeatherInfo(
                descriptionResId = if (isNight) R.string.mainly_clear else R.string.mostly_sunny,
                iconEmoji = if (isNight) "🌙" else "☀️",
                animation = animation,
                backgroundResName = if (isNight) "bg_clear_night" else "bg_mainly_clear_day"
            )
            2 -> WeatherInfo(
                descriptionResId = R.string.partly_cloudy,
                iconEmoji = if (isNight) "☁️" else "⛅",
                animation = animation,
                backgroundResName = if (isNight) "bg_partly_cloudy_night" else "bg_partly_cloudy_day"
            )
            3 -> WeatherInfo(
                descriptionResId = R.string.overcast,
                iconEmoji = "☁️",
                animation = animation,
                backgroundResName = "bg_overcast"
            )
            45, 48 -> WeatherInfo(
                descriptionResId = R.string.foggy,
                iconEmoji = "🌫️",
                animation = WeatherState.FOG,
                backgroundResName = "bg_fog"
            )
            51 -> WeatherInfo(
                descriptionResId = R.string.light_drizzle,
                iconEmoji = "🌦️",
                animation = WeatherState.DRIZZLE,
                backgroundResName = "bg_drizzle"
            )
            53 -> WeatherInfo(
                descriptionResId = R.string.moderate_drizzle,
                iconEmoji = "🌦️",
                animation = WeatherState.DRIZZLE,
                backgroundResName = "bg_drizzle"
            )
            55 -> WeatherInfo(
                descriptionResId = R.string.dense_drizzle,
                iconEmoji = "🌦️",
                animation = WeatherState.DRIZZLE,
                backgroundResName = "bg_drizzle"
            )
            56 -> WeatherInfo(
                descriptionResId = R.string.light_freezing_drizzle,
                iconEmoji = "🌨️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_freezing_rain"
            )
            57 -> WeatherInfo(
                descriptionResId = R.string.dense_freezing_drizzle,
                iconEmoji = "🌨️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_freezing_rain"
            )
            61 -> WeatherInfo(
                descriptionResId = R.string.light_rain,
                iconEmoji = "🌦️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain"
            )
            63 -> WeatherInfo(
                descriptionResId = R.string.moderate_rain,
                iconEmoji = "🌧️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain"
            )
            65 -> WeatherInfo(
                descriptionResId = R.string.heavy_rain,
                iconEmoji = "🌧️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain"
            )
            66 -> WeatherInfo(
                descriptionResId = R.string.light_freezing_rain,
                iconEmoji = "🌨️🌧️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_freezing_rain"
            )
            67 -> WeatherInfo(
                descriptionResId = R.string.heavy_freezing_rain,
                iconEmoji = "🌨️🌧️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_freezing_rain"
            )
            71 -> WeatherInfo(
                descriptionResId = R.string.light_snow,
                iconEmoji = "❄️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow"
            )
            73 -> WeatherInfo(
                descriptionResId = R.string.moderate_snow,
                iconEmoji = "❄️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow"
            )
            75 -> WeatherInfo(
                descriptionResId = R.string.heavy_snow,
                iconEmoji = "❄️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow"
            )
            77 -> WeatherInfo(
                descriptionResId = R.string.snow_grains,
                iconEmoji = "🌨️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow_grains"
            )
            80 -> WeatherInfo(
                descriptionResId = R.string.light_rain_showers,
                iconEmoji = "🌦️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain_showers"
            )
            81 -> WeatherInfo(
                descriptionResId = R.string.moderate_rain_showers,
                iconEmoji = "🌧️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain_showers"
            )
            82 -> WeatherInfo(
                descriptionResId = R.string.violent_rain_showers,
                iconEmoji = "🌧️",
                animation = WeatherState.RAIN,
                backgroundResName = "bg_rain_showers"
            )
            85 -> WeatherInfo(
                descriptionResId = R.string.light_snow_showers,
                iconEmoji = "🌨️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow_showers"
            )
            86 -> WeatherInfo(
                descriptionResId = R.string.heavy_snow_showers,
                iconEmoji = "❄️",
                animation = WeatherState.SNOW,
                backgroundResName = "bg_snow_showers"
            )
            95 -> WeatherInfo(
                descriptionResId = R.string.thunderstorm,
                iconEmoji = "⛈️",
                animation = WeatherState.STORM,
                backgroundResName = "bg_thunderstorm"
            )
            96 -> WeatherInfo(
                descriptionResId = R.string.thunderstorm_hail_light,
                iconEmoji = "⛈️🌨️",
                animation = WeatherState.STORM,
                backgroundResName = "bg_thunderstorm_hail"
            )
            99 -> WeatherInfo(
                descriptionResId = R.string.thunderstorm_hail_heavy,
                iconEmoji = "⛈️🌨️",
                animation = WeatherState.STORM,
                backgroundResName = "bg_thunderstorm_hail"
            )
            else -> WeatherInfo(
                descriptionResId = R.string.unknown,
                iconEmoji = "❓",
                animation = WeatherState.SUNNY,
                backgroundResName = "bg_clear_day"
            )
        }
    }

    fun getBackgroundResourceId(context: android.content.Context, resName: String): Int {
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        // Fallback to a placeholder or stay transparent if not found
        return if (resId != 0) resId else 0 
    }

    data class WeatherIconData(
        val iconEmoji: String,
        val animation: WeatherState,
        val descriptionResId: Int,
        val dropCount: Int? = null
    )

    fun getWeatherIcon(code: Int, isDay: Int = 1, humidity: Int = 50): WeatherIconData {
        val info = getWeatherInfo(code, isDay)
        return WeatherIconData(
            iconEmoji = info.iconEmoji,
            animation = info.animation,
            descriptionResId = info.descriptionResId
        )
    }

    fun getWindDirectionString(degree: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = kotlin.math.round((degree % 360) / 22.5).toInt()
        return directions[index % 16]
    }

    fun getWeatherDescriptionResId(code: Int): Int {
        return getWeatherInfo(code).descriptionResId
    }

    fun getWeatherEmoji(code: Int, isDay: Int = 1): String {
        return getWeatherInfo(code, isDay).iconEmoji
    }

    fun calculateOutdoorScore(temp: Double, windSpeed: Double, rainProb: Int): Pair<Int, Int> {
        var score = 10
        
        // Temperature score (Optimal 15-25)
        val tempEffect = when {
            temp in 15.0..25.0 -> 0
            temp in 10.0..15.0 || temp in 25.0..28.0 -> 1
            temp in 5.0..10.0 || temp in 28.0..32.0 -> 2
            temp in 0.0..5.0 -> 3
            else -> 5
        }
        score -= tempEffect
        
        // Wind score (Optimal < 20)
        val windEffect = when {
            windSpeed < 20.0 -> 0
            windSpeed < 30.0 -> 1
            windSpeed < 45.0 -> 2
            else -> 5
        }
        score -= windEffect
        
        // Rain score (Optimal < 10)
        val rainEffect = when {
            rainProb < 10 -> 0
            rainProb < 30 -> 2
            rainProb < 60 -> 4
            else -> 6
        }
        score -= rainEffect
        
        score = score.coerceIn(1, 10)
        
        val messageResId = when {
            score >= 8 -> R.string.outdoor_perfect
            score >= 5 -> R.string.outdoor_moderate
            else -> R.string.outdoor_poor
        }
        
        return Pair(score, messageResId)
    }
        
    enum class SportType { RUNNING, CYCLING, HIKING }

    data class WeatherAdvice(
        val sportNameResId: Int,
        val sportIcon: String,
        val sportAdviceResId: Int,
        val sportStatusResId: Int, 
        val smartAdviceResId: Int
    )

    fun getWeatherAdvice(
        temp: Double, 
        rainProb: Int, 
        wind: Double, 
        humidity: Int, 
        weatherCode: Int,
        sportType: SportType = SportType.RUNNING
    ): WeatherAdvice {
        // 1. Sport Advice Logic
        val sportStatus: String
        val sportAdvice: String
        val sportName = sportType.name.lowercase().replaceFirstChar { it.uppercase() }
        val sportIcon = when(sportType) {
            SportType.RUNNING -> "🏃"
            SportType.CYCLING -> "🚴"
            SportType.HIKING -> "🥾"
        }
        
        // Thresholds vary slightly by sport
        val windThreshold = if (sportType == SportType.CYCLING) 20 else 25
        val tempMin = if (sportType == SportType.HIKING) 5.0 else 10.0
        
        val statusResId: Int
        val adviceResId: Int
        val sportNameResId = when(sportType) {
            SportType.RUNNING -> R.string.sport_running
            SportType.CYCLING -> R.string.sport_cycling
            SportType.HIKING -> R.string.sport_hiking
        }

        when {
            rainProb > 60 || wind > windThreshold || weatherCode in 95..99 -> {
                statusResId = R.string.status_avoid
                adviceResId = if (weatherCode in 95..99) R.string.advice_storm else R.string.advice_not_ideal
            }
            rainProb in 20..50 -> {
                statusResId = R.string.status_decent
                adviceResId = R.string.advice_decent_template
            }
            rainProb < 20 && wind < 15 && temp in tempMin..25.0 -> {
                statusResId = R.string.status_excellent
                adviceResId = R.string.advice_excellent_template
            }
            else -> {
                statusResId = R.string.status_good
                adviceResId = R.string.advice_good_template
            }
        }

        // 2. Smart Advice Logic (Safety & Comfort)
        val smartAdviceResId = when {
            temp > 28.0 -> R.string.smart_hydration
            temp < 10.0 -> R.string.smart_dress_warm
            wind > 30.0 -> R.string.smart_strong_wind
            humidity > 80 -> R.string.smart_high_humidity
            weatherCode in 0..1 && temp in 18.0..25.0 -> R.string.smart_great_day
            weatherCode in 51..67 || weatherCode in 80..82 -> R.string.smart_okay_outings
            weatherCode in 95..99 -> R.string.smart_stay_indoors
            else -> R.string.smart_enjoy_day
        }

        return WeatherAdvice(sportNameResId, sportIcon, adviceResId, statusResId, smartAdviceResId)
    }

    fun findOptimalExerciseHour(hourly: List<HourlyWeatherDomain>, startIndex: Int): Pair<Int, String?> {
        var maxScore = -1
        var bestHourIdx = -1
        
        // Look ahead 12 hours
        val endIdx = (startIndex + 12).coerceAtMost(hourly.size)
        for (i in startIndex until endIdx) {
            val hourData = hourly[i]
            val temp = hourData.temperature
            val rainProb = hourData.precipitationProbability
            
            // Temporary simple score for finding best time
            val score = if (rainProb < 20 && temp in 10.0..25.0) 10 else 5
            if (score > maxScore) {
                maxScore = score
                bestHourIdx = i
            }
        }
        
        return if (bestHourIdx != -1) {
            val time = hourly[bestHourIdx].time.substringAfter("T").substring(0, 5)
            Pair(R.string.best_time_at, time)
        } else {
            Pair(R.string.condition_stable, null)
        }
    }

    fun getVisibilityLevel(km: Double): Int = when {
        km > 10 -> com.ukweather.live.R.string.excellent
        km > 5 -> com.ukweather.live.R.string.good
        km > 3 -> com.ukweather.live.R.string.moderate
        km > 1 -> com.ukweather.live.R.string.poor
        else -> com.ukweather.live.R.string.very_poor
    }

    fun getVisibilityColor(km: Double): Color = when {
        km > 10 -> Color(0xFF4CAF50) // Green
        km > 5 -> Color(0xFF8BC34A)  // Light Green
        km > 3 -> Color(0xFFFFEB3B)  // Yellow
        km > 1 -> Color(0xFFFF9800)  // Orange
        else -> Color(0xFFF44336)     // Red
    }

    fun getVisibilityMessage(km: Double): Int = when {
        km > 10 -> com.ukweather.live.R.string.clear_visibility
        km > 5 -> com.ukweather.live.R.string.slight_haze
        km > 3 -> com.ukweather.live.R.string.reduced_visibility
        km <= 0 -> com.ukweather.live.R.string.extreme_fog
        else -> com.ukweather.live.R.string.low_visibility
    }

    fun calculateMoonPhase(dateString: String): Double {
        return try {
            val date = java.time.LocalDate.parse(dateString)
            // Days since January 1, 2000 (New Moon reference)
            val epoch = java.time.LocalDate.of(2000, 1, 6) 
            val days = java.time.temporal.ChronoUnit.DAYS.between(epoch, date)
            val phase = (days % 29.530588853) / 29.530588853
            if (phase < 0) phase + 1 else phase
        } catch (e: Exception) { 0.5 }
    }

    fun getMoonPhaseResId(phase: Double): Int {
        return when {
            phase >= 0.0 && phase < 0.1 -> R.string.moon_new
            phase >= 0.1 && phase < 0.2 -> R.string.moon_waxing_crescent
            phase >= 0.2 && phase < 0.3 -> R.string.moon_first_quarter
            phase >= 0.3 && phase < 0.45 -> R.string.moon_waxing_gibbous
            phase >= 0.45 && phase < 0.55 -> R.string.moon_full
            phase >= 0.55 && phase < 0.7 -> R.string.moon_waning_gibbous
            phase >= 0.7 && phase < 0.8 -> R.string.moon_last_quarter
            phase >= 0.8 && phase < 0.9 -> R.string.moon_waning_crescent
            else -> R.string.moon_new
        }
    }


    fun getCloudStatusResId(cloudPercentage: Int): Int = when {
        cloudPercentage < 10 -> R.string.cloud_clear
        cloudPercentage < 40 -> R.string.cloud_partly
        cloudPercentage < 70 -> R.string.cloud_mostly
        else -> R.string.cloud_overcast
    }

    fun getVisibilityIcon(km: Double): String = "👁️"
}



