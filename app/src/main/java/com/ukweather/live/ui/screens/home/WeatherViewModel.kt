package com.ukweather.live.ui.screens.home

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val cityName: String,
        val weather: com.ukweather.live.data.model.WeatherDomain,
        val latitude: Double,
        val longitude: Double,
        val airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse? = null,
        val isFavorite: Boolean = false,
        val favorites: List<com.ukweather.live.data.repository.LocationData> = emptyList()
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: com.ukweather.live.data.repository.WeatherRepository,
    private val locationRepository: com.ukweather.live.data.repository.LocationRepository,
    private val settingsRepository: com.ukweather.live.data.repository.SettingsRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<com.ukweather.live.data.repository.LocationData>>(locationRepository.getFavorites())
    val favorites: StateFlow<List<com.ukweather.live.data.repository.LocationData>> = _favorites.asStateFlow()

    val visibleWidgets = settingsRepository.visibleWidgets

    suspend fun getRadarData() = repository.getRadarData()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale = _showPermissionRationale.asStateFlow()

    private val _showGpsRationale = MutableStateFlow(false)
    val showGpsRationale = _showGpsRationale.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun shouldFollowGps() = locationRepository.shouldFollowGps()

    init {
        initializeWeather()
    }

    fun onFirstLaunchChoice(enableLocation: Boolean) {
        if (enableLocation) {
            locationRepository.setFollowGps(true)
            requestLocationAndLoadWeather()
        } else {
            locationRepository.setFollowGps(false)
            initializeWeather()
        }
        locationRepository.setFirstLaunch(false)
    }

    private fun initializeWeather() {
        // Priority 1: If follow GPS is enabled (and permission potentially granted)
        if (locationRepository.shouldFollowGps()) {
            requestLocationAndLoadWeather()
        } else {
            // Priority 2: Last Viewed / Selected City
            val lastLocation = locationRepository.getLastLocation()
            if (lastLocation != null) {
                loadWeatherFromCoordinates(lastLocation.latitude, lastLocation.longitude, lastLocation.name)
            } else {
                // Priority 3: Default City
                loadWeatherForCity(context.getString(com.ukweather.live.R.string.default_city))
            }
        }
    }

    fun dismissRationale() {
        _showPermissionRationale.value = false
        _showGpsRationale.value = false
    }
    
    fun refreshWeather() {
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    val result = repository.getWeatherData(currentState.latitude, currentState.longitude)
                    when (result) {
                        is com.ukweather.live.data.repository.WeatherResult.Success -> {
                            _uiState.value = currentState.copy(weather = result.data)
                        }
                        is com.ukweather.live.data.repository.WeatherResult.Error -> {
                            android.util.Log.e("WeatherViewModel", "Refresh failed: ${result.message}")
                        }
                    }
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun removeFavorite(name: String, lat: Double? = null, lon: Double? = null) {
        val favs = locationRepository.getFavorites()
        val target = if (lat != null && lon != null) {
            favs.find { kotlin.math.abs(it.latitude - lat) < 0.12 && kotlin.math.abs(it.longitude - lon) < 0.12 }
        } else {
            favs.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
        } ?: favs.find { it.name.contains(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true) }

        if (target != null) {
            toggleFavorite(target.name, target.latitude, target.longitude)
        }
    }

    fun removeWidget(widget: com.ukweather.live.data.model.WeatherWidget) {
        settingsRepository.removeWidget(widget)
    }

    fun restoreWidgets() {
        settingsRepository.restoreWidgets()
    }

    fun loadWeatherForCity(cityName: String) {
        val trimmedCity = cityName.trim()
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val langName = settingsRepository.getSelectedLanguageName()
                val langCode = settingsRepository.getLanguageCode(langName)
                
                val result = repository.getCoordinatesForCity(trimmedCity, langCode)
                if (result != null) {
                    val (lat, lon, translatedName) = result
                    val weatherResult = repository.getWeatherData(lat, lon)
                    val airQuality = repository.getAirQuality(lat, lon)
                    
                    when (weatherResult) {
                        is com.ukweather.live.data.repository.WeatherResult.Success -> {
                            locationRepository.setFollowGps(false)
                            locationRepository.saveLocation(translatedName, lat, lon)
                            updateSuccessState(translatedName, weatherResult.data, lat, lon, airQuality)
                        }
                        is com.ukweather.live.data.repository.WeatherResult.Error -> {
                            _uiState.value = WeatherUiState.Error(weatherResult.message)
                        }
                    }
                } else {
                    _uiState.value = WeatherUiState.Error("City not found: $trimmedCity")
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Failed to reach weather API")
            }
        }
    }

    fun selectCity(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            locationRepository.setFollowGps(false)
            locationRepository.saveLocation(name, lat, lon)
            loadWeatherFromCoordinates(lat, lon, name)
        }
    }

    fun toggleFavorite(name: String, lat: Double, lon: Double) {
        locationRepository.toggleFavorite(name, lat, lon)
        val newFavorites = locationRepository.getFavorites()
        _favorites.value = newFavorites
        
        val isNowFavorite = locationRepository.isFavorite(name, lat, lon)
        
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            _uiState.value = currentState.copy(
                isFavorite = isNowFavorite,
                favorites = newFavorites
            )
        }
    }

    fun followCurrentLocation() {
        locationRepository.setFollowGps(true)
        requestLocationAndLoadWeather()
    }

    @SuppressLint("MissingPermission")
    fun requestLocationAndLoadWeather() {
        _uiState.value = WeatherUiState.Loading
        
        // 1. Permission Check
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            if (locationRepository.shouldFollowGps()) {
                _showPermissionRationale.value = true
            }
            loadFallbackWeather()
            return
        }

        // 2. GPS Hardware Check
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) || 
                          locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            _showGpsRationale.value = true
            loadFallbackWeather()
            return
        }

        // Use getCurrentLocation for higher precision on fresh request (as requested)
        val locationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        
        // Safety timeout: If GPS takes too long, fallback to avoid blocking the user
        viewModelScope.launch {
            kotlinx.coroutines.delay(10000) 
            if (_uiState.value is WeatherUiState.Loading) {
                // Priority 1b: Fallback to last known location if current location times out
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null && _uiState.value is WeatherUiState.Loading) {
                        loadWeatherFromCoordinates(lastLoc.latitude, lastLoc.longitude)
                    } else if (_uiState.value is WeatherUiState.Loading) {
                        // Priority 2/3: Go deeper into the fallback chain
                        loadFallbackWeather()
                    }
                }
            }
        }

        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                // Priority 1: High accuracy GPS success
                loadWeatherFromCoordinates(location.latitude, location.longitude)
            } else {
                // Priority 1c: Try last known location if high accuracy failed
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        loadWeatherFromCoordinates(lastLoc.latitude, lastLoc.longitude)
                    } else {
                        // Priority 2/3: Proceed to last viewed/default
                        loadFallbackWeather()
                    }
                }.addOnFailureListener {
                    loadFallbackWeather()
                }
            }
        }.addOnFailureListener {
            // High accuracy failed or permission issues
            loadFallbackWeather()
        }
    }

    /**
     * Fallback Chain Strategy:
     * 1. Last Selected/Viewed City
     * 2. System Default City (London)
     */
    private fun loadFallbackWeather() {
        val lastLoc = locationRepository.getLastLocation()
        if (lastLoc != null) {
            loadWeatherFromCoordinates(lastLoc.latitude, lastLoc.longitude, lastLoc.name)
        } else {
            loadWeatherForCity(context.getString(com.ukweather.live.R.string.default_city))
        }
    }

    private fun loadWeatherFromCoordinates(lat: Double, lon: Double, storedName: String? = null) {
        viewModelScope.launch {
            val weatherResult = repository.getWeatherData(lat, lon)
            val airQuality = repository.getAirQuality(lat, lon)
            
            when (weatherResult) {
                is com.ukweather.live.data.repository.WeatherResult.Success -> {
                    val langName = settingsRepository.getSelectedLanguageName()
                    val langCode = settingsRepository.getLanguageCode(langName)
                    val cityName = storedName ?: repository.getCityNameFromCoordinates(lat, lon, langCode) ?: "Current Location"
                    updateSuccessState(cityName, weatherResult.data, lat, lon, airQuality)
                }
                is com.ukweather.live.data.repository.WeatherResult.Error -> {
                    _uiState.value = WeatherUiState.Error(weatherResult.message)
                }
            }
        }
    }

    private fun updateSuccessState(cityName: String, weather: com.ukweather.live.data.model.WeatherDomain, lat: Double, lon: Double, airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse?) {
        val newFavorites = locationRepository.getFavorites()
        _favorites.value = newFavorites
        
        _uiState.value = WeatherUiState.Success(
            cityName = cityName,
            weather = weather,
            latitude = lat,
            longitude = lon,
            airQuality = airQuality,
            isFavorite = locationRepository.isFavorite(cityName, lat, lon),
            favorites = newFavorites
        )

        // Trigger widget update
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.ukweather.live.worker.WeatherWidgetWorker>()
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
    }
}



