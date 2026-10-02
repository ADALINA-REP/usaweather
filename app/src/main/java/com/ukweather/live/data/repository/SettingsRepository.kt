package com.ukweather.live.data.repository

import android.content.Context
import com.ukweather.live.data.model.WeatherWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)

    private val _isCelsius = MutableStateFlow(prefs.getBoolean("use_celsius", true))
    val isCelsius = _isCelsius.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _useOpenWeather = MutableStateFlow(prefs.getBoolean("use_openweather", false))
    val useOpenWeather = _useOpenWeather.asStateFlow()

    private val _isMb = MutableStateFlow(prefs.getBoolean("use_mb", true))
    val isMb = _isMb.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "English") ?: "English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _visibleWidgets = MutableStateFlow(
        prefs.getStringSet("visible_widgets", WeatherWidget.values().map { it.id }.toSet())
            ?.mapNotNull { id -> WeatherWidget.values().find { it.id == id } }
            ?.toSet() ?: WeatherWidget.values().toSet()
    )
    val visibleWidgets = _visibleWidgets.asStateFlow()

    private val languageMap = mapOf(
        "English" to "en", "Spanish" to "es", "French" to "fr", "German" to "de",
        "Italian" to "it", "Portuguese" to "pt", "Russian" to "ru", "Chinese" to "zh",
        "Japanese" to "ja", "Korean" to "ko", "Arabic" to "ar", "Hindi" to "hi",
        "Bengali" to "bn", "Turkish" to "tr", "Dutch" to "nl", "Polish" to "pl",
        "Swedish" to "sv", "Greek" to "el", "Thai" to "th", "Vietnamese" to "vi"
    )

    fun getPopularLanguages() = languageMap.keys.toList()

    fun getLanguageCode(name: String) = languageMap[name] ?: "en"

    fun getSelectedLanguageName() = prefs.getString("selected_language", "English") ?: "English"

    fun saveLanguage(name: String) {
        _selectedLanguage.value = name
        prefs.edit().putString("selected_language", name).apply()
    }

    fun toggleCelsius(enabled: Boolean) {
        _isCelsius.value = enabled
        prefs.edit().putBoolean("use_celsius", enabled).apply()
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun toggleOpenWeather(enabled: Boolean) {
        _useOpenWeather.value = enabled
        prefs.edit().putBoolean("use_openweather", enabled).apply()
    }

    fun toggleMb(enabled: Boolean) {
        _isMb.value = enabled
        prefs.edit().putBoolean("use_mb", enabled).apply()
    }

    fun removeWidget(widget: WeatherWidget) {
        val current = _visibleWidgets.value.toMutableSet()
        current.remove(widget)
        _visibleWidgets.value = current
        prefs.edit().putStringSet("visible_widgets", current.map { it.id }.toSet()).apply()
    }

    fun restoreWidgets() {
        val all = WeatherWidget.values().toSet()
        _visibleWidgets.value = all
        prefs.edit().putStringSet("visible_widgets", all.map { it.id }.toSet()).apply()
    }
}



