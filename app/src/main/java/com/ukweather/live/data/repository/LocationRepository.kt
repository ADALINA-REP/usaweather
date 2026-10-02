package com.ukweather.live.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

data class LocationData(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    fun saveLocation(name: String, lat: Double, lon: Double) {
        prefs.edit().apply {
            putString("last_city_name", name)
            putFloat("last_lat", lat.toFloat())
            putFloat("last_lon", lon.toFloat())
            apply()
        }
    }

    fun getLastLocation(): LocationData? {
        val name = prefs.getString("last_city_name", null) ?: return null
        val lat = prefs.getFloat("last_lat", 0f).toDouble()
        val lon = prefs.getFloat("last_lon", 0f).toDouble()
        return LocationData(name, lat, lon)
    }

    fun clearStoredLocation() {
        prefs.edit().clear().apply()
    }

    fun setFollowGps(enabled: Boolean) {
        prefs.edit().putBoolean("follow_gps", enabled).apply()
    }

    fun shouldFollowGps(): Boolean {
        return prefs.getBoolean("follow_gps", true)
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunch(isFirst: Boolean) {
        prefs.edit().putBoolean("is_first_launch", isFirst).apply()
    }

    // --- Favorites Management ---
    
    @Synchronized
    fun getFavorites(): List<LocationData> {
        val jsonStr = prefs.getString("favorite_locations_json", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<LocationData>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LocationData(
                            name = obj.getString("name"),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude")
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Backward compatibility migration from legacy key-based prefs if present
        val legacyFavoriteSet = prefs.getStringSet("favorite_cities_keys", null)
        if (!legacyFavoriteSet.isNullOrEmpty()) {
            val migrated = legacyFavoriteSet.mapNotNull { key ->
                val name = prefs.getString("fav_name_$key", null) ?: return@mapNotNull null
                val lat = prefs.getFloat("fav_lat_$key", 0f).toDouble()
                val lon = prefs.getFloat("fav_lon_$key", 0f).toDouble()
                LocationData(name, lat, lon)
            }
            if (migrated.isNotEmpty()) {
                saveFavorites(migrated)
                return migrated
            }
        }
        return emptyList()
    }

    @Synchronized
    private fun saveFavorites(list: List<LocationData>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("name", item.name)
                put("latitude", item.latitude)
                put("longitude", item.longitude)
            }
            array.put(obj)
        }
        prefs.edit().putString("favorite_locations_json", array.toString()).apply()
    }

    @Synchronized
    fun toggleFavorite(name: String, lat: Double, lon: Double) {
        val currentList = getFavorites().toMutableList()
        val index = currentList.indexOfFirst { isMatchingLocation(it, name, lat, lon) }
        
        if (index >= 0) {
            currentList.removeAt(index)
        } else {
            currentList.add(0, LocationData(name.trim(), lat, lon))
        }
        saveFavorites(currentList)
    }

    @Synchronized
    fun isFavorite(name: String, lat: Double? = null, lon: Double? = null): Boolean {
        val currentList = getFavorites()
        return currentList.any { isMatchingLocation(it, name, lat, lon) }
    }

    private fun isMatchingLocation(item: LocationData, name: String, lat: Double?, lon: Double?): Boolean {
        // Match by coordinate proximity if coords available (~12km tolerance)
        if (lat != null && lon != null && lat != 0.0 && lon != 0.0) {
            if (abs(item.latitude - lat) < 0.12 && abs(item.longitude - lon) < 0.12) {
                return true
            }
        }
        // Match by name
        val norm1 = normalizeName(item.name)
        val norm2 = normalizeName(name)
        return norm1 == norm2 || norm1.contains(norm2) || norm2.contains(norm1)
    }

    private fun normalizeName(name: String): String {
        return name.lowercase()
            .replace(",", " ")
            .replace("-", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
}
