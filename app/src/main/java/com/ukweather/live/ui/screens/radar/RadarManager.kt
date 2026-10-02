package com.ukweather.live.ui.screens.radar

import com.ukweather.live.data.api.RadarItem
import com.ukweather.live.data.repository.WeatherRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadarManager(
    private val scope: CoroutineScope,
    private val repository: WeatherRepository
) {
    private val _radarFrames = MutableStateFlow<List<RadarItem>>(emptyList())
    val radarFrames = _radarFrames.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex = _currentFrameIndex.asStateFlow()

    private val _isAnimating = MutableStateFlow(false)
    val isAnimating = _isAnimating.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRadarEnabled = MutableStateFlow(true)
    val isRadarEnabled = _isRadarEnabled.asStateFlow()

    private var animationJob: Job? = null

    /**
     * Fetches current timestamps from RainViewer API
     */
    fun fetchRadarData() {
        scope.launch {
            _isLoading.value = true
            try {
                val response = repository.getRadarData()
                // Fetch both past and nowcast (forecast) frames
                val pastFrames = response?.radar?.past?.takeLast(10) ?: emptyList()
                val forecastFrames = response?.radar?.nowcast?.take(3) ?: emptyList()
                
                val combinedFrames = pastFrames + forecastFrames
                _radarFrames.value = combinedFrames
                
                if (combinedFrames.isNotEmpty()) {
                    // Start at the last available past frame
                    _currentFrameIndex.value = (pastFrames.size - 1).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _radarFrames.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleRadar(enabled: Boolean) {
        _isRadarEnabled.value = enabled
        if (!enabled) stopAnimation()
    }

    fun toggleAnimation() {
        if (!_isRadarEnabled.value) return
        _isAnimating.value = !_isAnimating.value
        if (_isAnimating.value) {
            startAnimationLoop()
        } else {
            stopAnimation()
        }
    }

    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = scope.launch {
            while (isActive) {
                if (_radarFrames.value.isNotEmpty()) {
                    val nextIndex = (_currentFrameIndex.value + 1) % _radarFrames.value.size
                    _currentFrameIndex.value = nextIndex
                }
                delay(600) // Professional transition speed
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
        _isAnimating.value = false
    }
    
    fun onCleared() {
        stopAnimation()
    }
}



