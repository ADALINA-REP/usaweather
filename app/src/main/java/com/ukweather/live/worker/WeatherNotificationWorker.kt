package com.ukweather.live.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ukweather.live.R
import com.ukweather.live.data.repository.WeatherRepository
import com.ukweather.live.data.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class WeatherNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WeatherRepository,
    private val locationRepository: LocationRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val WEATHER_NOTIFICATION_ID = 1001
        private const val SEVERE_NOTIFICATION_ID = 1002
        private const val MIN_INTERVAL_BETWEEN_ALERTS_MS = 4 * 60 * 60 * 1000L // 4 hours cooldown minimum
    }

    override suspend fun doWork(): Result {
        val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)
        val alertsEnabled = prefs.getBoolean("weather_alerts", true)
        val rainAlertsEnabled = prefs.getBoolean("rain_alerts", true)
        
        if (!alertsEnabled && !rainAlertsEnabled) return Result.success()

        // Respect Quiet Hours (10:00 PM to 07:30 AM)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isQuietHours = currentHour in 22..23 || currentHour < 7

        val lastLoc = locationRepository.getLastLocation() ?: return Result.success()
        val weatherResult = repository.getWeatherData(lastLoc.latitude, lastLoc.longitude)
        val weatherDomain = when (weatherResult) {
            is com.ukweather.live.data.repository.WeatherResult.Success -> weatherResult.data
            else -> return Result.success()
        }

        val lastAlertTime = prefs.getLong("last_alert_timestamp", 0L)
        val now = System.currentTimeMillis()
        val isThrottled = (now - lastAlertTime) < MIN_INTERVAL_BETWEEN_ALERTS_MS

        // 1. Severe Weather Warnings (Only for severe thunderstorms / dangerous weather)
        val code = weatherDomain.current.weatherCode
        if (code in 95..99) {
            if (!isThrottled || (now - lastAlertTime > 2 * 60 * 60 * 1000L)) {
                showNotification(
                    title = "Severe Weather Warning ⛈️",
                    message = "Thunderstorms detected in ${lastLoc.name}. Stay safe!",
                    channelId = "severe_channel",
                    notificationId = SEVERE_NOTIFICATION_ID,
                    priority = NotificationCompat.PRIORITY_HIGH
                )
                prefs.edit().putLong("last_alert_timestamp", now).apply()
            }
            return Result.success()
        }

        // 2. Gentle Rain Alerts (Strictly throttled, only during daytime, threshold >= 70%)
        if (rainAlertsEnabled && !isQuietHours && !isThrottled) {
            val rainProb = weatherDomain.hourly.firstOrNull()?.precipitationProbability ?: 0
            if (rainProb >= 70) {
                showNotification(
                    title = "Rain Expected 🌧️",
                    message = "High chance of rain ($rainProb%) in ${lastLoc.name}. Remember your umbrella.",
                    channelId = "rain_channel",
                    notificationId = WEATHER_NOTIFICATION_ID,
                    priority = NotificationCompat.PRIORITY_DEFAULT
                )
                prefs.edit().putLong("last_alert_timestamp", now).apply()
            }
        }

        return Result.success()
    }

    private fun showNotification(
        title: String,
        message: String,
        channelId: String,
        notificationId: Int,
        priority: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        createNotificationChannel(channelId)

        val intent = android.content.Intent(context, com.ukweather.live.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.skycast_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, importance) = when(channelId) {
                "severe_channel" -> "Severe Weather" to NotificationManager.IMPORTANCE_HIGH
                else -> "Daily Weather & Rain" to NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Weather notifications for Weather UK"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
