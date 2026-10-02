package com.ukweather.live

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SkyCastApp : Application(), Configuration.Provider {
    
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // Initialize App Open Ad Manager
        com.ukweather.live.util.AppOpenAdManager(this)

        // Schedule weather alerts worker with polite interval
        val workRequest = PeriodicWorkRequestBuilder<com.ukweather.live.worker.WeatherNotificationWorker>(
            4, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_alerts",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // Schedule periodic widget refresh (Every 30 mins for maximum reliability)
        val widgetWorkRequest = PeriodicWorkRequestBuilder<com.ukweather.live.worker.WeatherWidgetWorker>(
            30, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            widgetWorkRequest
        )
    }
}



