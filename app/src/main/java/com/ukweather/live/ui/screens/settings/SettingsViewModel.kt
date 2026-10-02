package com.ukweather.live.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: com.ukweather.live.data.repository.SettingsRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)

    init {
        // Apply stored locale on startup
        val storedLang = prefs.getString("selected_language", "English") ?: "English"
        applyLocale(storedLang)
    }

    val isCelsius = settingsRepository.isCelsius
    val isDarkMode = settingsRepository.isDarkMode
    val useOpenWeather = settingsRepository.useOpenWeather
    val isMb = settingsRepository.isMb

    private val _isAlertsEnabled = MutableStateFlow(prefs.getBoolean("weather_alerts", true))
    val isAlertsEnabled: StateFlow<Boolean> = _isAlertsEnabled.asStateFlow()

    private val _isRainAlertEnabled = MutableStateFlow(prefs.getBoolean("rain_alerts", true))
    val isRainAlertEnabled: StateFlow<Boolean> = _isRainAlertEnabled.asStateFlow()

    private val _isTempAlertEnabled = MutableStateFlow(prefs.getBoolean("temp_alerts", true))
    val isTempAlertEnabled: StateFlow<Boolean> = _isTempAlertEnabled.asStateFlow()

    // Hardcoded to false for initial free PlayStore release
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(settingsRepository.getSelectedLanguageName())
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    val popularLanguages = settingsRepository.getPopularLanguages()

    fun toggleLanguage(name: String) {
        _selectedLanguage.value = name
        settingsRepository.saveLanguage(name)
        
        applyLocale(name)
    }

    private fun applyLocale(name: String) {
        val code = settingsRepository.getLanguageCode(name)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun toggleCelsius(enabled: Boolean) {
        settingsRepository.toggleCelsius(enabled)
    }

    fun toggleDarkMode(enabled: Boolean) {
        settingsRepository.toggleDarkMode(enabled)
    }

    fun toggleOpenWeather(enabled: Boolean) {
        settingsRepository.toggleOpenWeather(enabled)
    }

    fun toggleMb(enabled: Boolean) {
        settingsRepository.toggleMb(enabled)
    }
    
    fun toggleAlerts(enabled: Boolean) {
        _isAlertsEnabled.value = enabled
        prefs.edit().putBoolean("weather_alerts", enabled).apply()
    }

    fun toggleRainAlerts(enabled: Boolean) {
        _isRainAlertEnabled.value = enabled
        prefs.edit().putBoolean("rain_alerts", enabled).apply()
    }

    fun toggleTempAlerts(enabled: Boolean) {
        _isTempAlertEnabled.value = enabled
        prefs.edit().putBoolean("temp_alerts", enabled).apply()
    }

    fun togglePremium(enabled: Boolean) {
        _isPremium.value = enabled
        prefs.edit().putBoolean("is_premium", enabled).apply()
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}



