package com.ukweather.live.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ukweather.live.worker.WeatherWidgetWorker

class WeatherAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Trigger background update via WorkManager
        val workRequest = OneTimeWorkRequestBuilder<WeatherWidgetWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
