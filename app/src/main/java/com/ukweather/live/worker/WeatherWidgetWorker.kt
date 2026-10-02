package com.ukweather.live.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ukweather.live.R
import com.ukweather.live.data.repository.WeatherRepository
import com.ukweather.live.data.repository.LocationRepository
import com.ukweather.live.util.WeatherUtils
import com.ukweather.live.ui.widget.WeatherAppWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class WeatherWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository,
    private val locationRepository: LocationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lastLoc = locationRepository.getLastLocation() ?: return Result.success()
        val weatherResult = repository.getWeatherData(lastLoc.latitude, lastLoc.longitude)
        
        val weatherDomain = when (weatherResult) {
            is com.ukweather.live.data.repository.WeatherResult.Success -> weatherResult.data
            else -> return Result.failure()
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WeatherAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)
        val isCelsius = prefs.getBoolean("use_celsius", true)

        val current = weatherDomain.current
        val today = weatherDomain.daily.firstOrNull()
        
        val tempStr = if (isCelsius) "${current.temperature.toInt()}°" else "${(current.temperature * 9 / 5 + 32).toInt()}°"
        val highLowStr = if (today != null) {
            val h = if (isCelsius) today.maxTemp.toInt() else (today.maxTemp * 9 / 5 + 32).toInt()
            val l = if (isCelsius) today.minTemp.toInt() else (today.minTemp * 9 / 5 + 32).toInt()
            "H:${h}° L:${l}°"
        } else "--"

        val feelsLikeStr = if (current.feelsLike != null) {
            val f = if (isCelsius) current.feelsLike.toInt() else (current.feelsLike * 9 / 5 + 32).toInt()
            "${f}°"
        } else tempStr

        val humidityStr = "${current.humidity ?: 0}%"
        val windStr = "${current.windSpeed.toInt()} km/h"
        val rainProb = weatherDomain.hourly.firstOrNull()?.precipitationProbability ?: 0
        val rainStr = "${rainProb}%"

        val info = WeatherUtils.getWeatherInfo(current.weatherCode, current.isDay)
        val condition = context.getString(info.descriptionResId)
        val emoji = info.iconEmoji

        // Get hourly forecast for next 5 slots starting from now
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val now = Date()
        val hourlyItems = weatherDomain.hourly.filter { item ->
            try {
                val date = isoFormatter.parse(item.time)
                date != null && date.after(now)
            } catch (e: Exception) {
                true 
            }
        }.take(5)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)
            
            views.setTextViewText(R.id.widget_city, lastLoc.name)
            views.setTextViewText(R.id.widget_condition, condition)
            views.setTextViewText(R.id.widget_temp, tempStr)
            views.setTextViewText(R.id.widget_high_low, highLowStr)
            views.setTextViewText(R.id.widget_icon, emoji)

            // Populate Details
            views.setTextViewText(R.id.widget_humidity, humidityStr)
            views.setTextViewText(R.id.widget_wind, windStr)
            views.setTextViewText(R.id.widget_rain, rainStr)
            views.setTextViewText(R.id.widget_feels_like, feelsLikeStr)

            // Populate forecast slots
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            val forecastData = listOf(
                Triple(R.id.f1_time, R.id.f1_temp, R.id.f1_icon),
                Triple(R.id.f2_time, R.id.f2_temp, R.id.f2_icon),
                Triple(R.id.f3_time, R.id.f3_temp, R.id.f3_icon),
                Triple(R.id.f4_time, R.id.f4_temp, R.id.f4_icon),
                Triple(R.id.f5_time, R.id.f5_temp, R.id.f5_icon)
            )

            hourlyItems.forEachIndexed { index, item ->
                if (index < forecastData.size) {
                    val (timeId, tempId, iconId) = forecastData[index]
                    
                    val timeStr = try {
                        val date = isoFormatter.parse(item.time)
                        timeFormatter.format(date ?: Date())
                    } catch (e: Exception) {
                        item.time.takeLast(5)
                    }
                    val hTemp = if (isCelsius) "${item.temperature.toInt()}°" else "${(item.temperature * 9 / 5 + 32).toInt()}°"
                    val hEmoji = WeatherUtils.getWeatherInfo(item.weatherCode, item.isDay).iconEmoji
                    
                    views.setTextViewText(timeId, timeStr)
                    views.setTextViewText(tempId, hTemp)
                    views.setTextViewText(iconId, hEmoji)
                }
            }

            // Intent to open app
            val intent = android.content.Intent(context, com.ukweather.live.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.header, pendingIntent)
            views.setOnClickPendingIntent(R.id.temp_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        return Result.success()
    }
}
