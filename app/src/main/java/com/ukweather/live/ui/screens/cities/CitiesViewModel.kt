package com.ukweather.live.ui.screens.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukweather.live.data.api.GeocodingApi
import com.ukweather.live.data.api.GeocodingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CitiesUiState {
    object Idle : CitiesUiState()
    object Loading : CitiesUiState()
    data class Success(val cities: List<GeocodingResult>) : CitiesUiState()
    data class Error(val message: String) : CitiesUiState()
}

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val geocodingApi: GeocodingApi
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<CitiesUiState>(CitiesUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val famousCities = listOf(
        GeocodingResult(id = 1, name = "London", latitude = 51.5074, longitude = -0.1278, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 2, name = "Birmingham", latitude = 52.4862, longitude = -1.8904, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 3, name = "Manchester", latitude = 53.4808, longitude = -2.2426, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 4, name = "Glasgow", latitude = 55.8642, longitude = -4.2518, admin1 = "Scotland", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 5, name = "Edinburgh", latitude = 55.9533, longitude = -3.1883, admin1 = "Scotland", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 6, name = "Liverpool", latitude = 53.4084, longitude = -2.9916, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 7, name = "Leeds", latitude = 53.8008, longitude = -1.5491, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 8, name = "Bristol", latitude = 51.4545, longitude = -2.5879, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 9, name = "Sheffield", latitude = 53.3811, longitude = -1.4701, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 10, name = "Newcastle upon Tyne", latitude = 54.9783, longitude = -1.6178, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 11, name = "Belfast", latitude = 54.5973, longitude = -5.9301, admin1 = "Northern Ireland", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 12, name = "Cardiff", latitude = 51.4816, longitude = -3.1791, admin1 = "Wales", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 13, name = "Nottingham", latitude = 52.9548, longitude = -1.1581, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 14, name = "Southampton", latitude = 50.9097, longitude = -1.4044, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 15, name = "Leicester", latitude = 52.6369, longitude = -1.1398, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 16, name = "Coventry", latitude = 52.4068, longitude = -1.5197, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 17, name = "Aberdeen", latitude = 57.1497, longitude = -2.0943, admin1 = "Scotland", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 18, name = "Plymouth", latitude = 50.3755, longitude = -4.1427, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 19, name = "Brighton", latitude = 50.8225, longitude = -0.1372, admin1 = "England", country = "United Kingdom", country_code = "GB"),
        GeocodingResult(id = 20, name = "Oxford", latitude = 51.7520, longitude = -1.2577, admin1 = "England", country = "United Kingdom", country_code = "GB")
    )

    init {
        setupSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.value = CitiesUiState.Loading
                    
                    // Local matches from famous cities for instant feedback
                    val localMatches = famousCities.filter { 
                        it.name.contains(query, ignoreCase = true) 
                    }
                    
                    try {
                        val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            geocodingApi.searchCities(query, count = 50).execute().body()!!
                        }
                        val allResults = response.results?.toList() ?: emptyList<GeocodingResult>()
                        
                        // Combine local famous matches with API results, removing duplicates
                        // Deduplicate by Name + Region and rounded coordinates
                        val combined = (localMatches + allResults).distinctBy { 
                            val roundedLat = (it.latitude * 10).toInt()
                            val roundedLon = (it.longitude * 10).toInt()
                            "${it.name.lowercase()}_${it.admin1?.lowercase()}_${roundedLat}_${roundedLon}"
                        }
                        
                        if (combined.isEmpty()) {
                            _uiState.value = CitiesUiState.Error("City not found")
                        } else {
                            _uiState.value = CitiesUiState.Success(combined)
                        }
                    } catch (e: Exception) {
                        // If API fails but we have local matches, still show them
                        if (localMatches.isNotEmpty()) {
                            _uiState.value = CitiesUiState.Success(localMatches)
                        } else {
                            _uiState.value = CitiesUiState.Error("Failed to fetch cities")
                        }
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length < 2) {
            _uiState.value = CitiesUiState.Idle
        } else {
            // Instant local filtering for famous cities while waiting for debounce/API
            val matches = famousCities.filter { it.name.contains(query, ignoreCase = true) }
            if (matches.isNotEmpty() && _uiState.value !is CitiesUiState.Success) {
                _uiState.value = CitiesUiState.Success(matches)
            }
        }
    }
}



