package com.ukweather.live.ui.screens.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ukweather.live.data.model.WeatherDomain
import com.ukweather.live.data.model.HourlyWeatherDomain
import com.ukweather.live.data.model.DailyWeatherDomain
import com.ukweather.live.ui.components.GlassCard
import com.ukweather.live.ui.screens.home.WeatherMetric
import com.ukweather.live.util.WeatherUtils
import com.ukweather.live.ui.theme.*

@Composable
fun AdvancedDetailsScreen(
    cityName: String,
    weather: WeatherDomain,
    airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Using current weather metrics
    val uvIndex = weather.daily.getOrNull(0)?.uvIndex ?: 0.0
    val pressure = weather.current.pressure.toInt().toString()
    val currentHourIdx = weather.hourly.indexOfFirst { 
        it.time.startsWith(weather.current.time.substring(0, 13)) 
    }.coerceAtLeast(0)
    
    val visibilityKm = (weather.hourly.getOrNull(currentHourIdx)?.visibility ?: 10000.0) / 1000.0
    val windDir = WeatherUtils.getWindDirectionString(weather.current.windDirection)
    val humidity = weather.current.humidity ?: 0
    val windSpeed = weather.current.windSpeed
    val currentPProb = weather.hourly.getOrNull(currentHourIdx)?.precipitationProbability ?: 0
    val currentIconData = WeatherUtils.getWeatherIcon(weather.current.weatherCode, isDay = weather.current.isDay, humidity = currentPProb)
    
    val activeState = currentIconData.animation
    val gradientColors = WeatherUtils.getGradientForState(activeState)
    // AQI Data
    val currentAir = airQuality?.current
    val aqi = currentAir?.aqi ?: 0

    val (aqiStatus, aqiColor) = when {
        aqi <= 20 -> "Good" to StatusSuccess
        aqi <= 40 -> "Fair" to WeatherRainLight
        aqi <= 60 -> "Moderate" to WeatherSunnyLight
        aqi <= 80 -> "Poor" to StatusWarning
        aqi <= 100 -> "Very Poor" to StatusDangerLight
        else -> "Extremely Poor" to WeatherThunderstormLight
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Advanced Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        cityName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Weather Metrics Card
            Text(
                "Current Metrics",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            val vKm = visibilityKm
                            val vColor = WeatherUtils.getVisibilityColor(vKm)
                            val vIcon = WeatherUtils.getVisibilityIcon(vKm)
                            WeatherMetric("Visibility", "${vKm.toInt()} km", vIcon, valueColor = vColor)
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            WeatherMetric("Humidity", "${humidity}%", "💧")
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            WeatherMetric("UV Index", "${uvIndex}", "☀️")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            WeatherMetric("Wind Speed", "${windSpeed.toInt()} km/h", "💨")
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            WeatherMetric("Wind Dir", windDir, "🧭")
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            WeatherMetric("Pressure", "${pressure} hPa", "⏲️")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Air Quality Section
            Text(
                "Air Quality Index (AQI)",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(aqiColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (aqi == 0) "--" else aqi.toString(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "AQI",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        aqiStatus,
                        style = MaterialTheme.typography.headlineSmall,
                        color = aqiColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (currentAir != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Main Pollutants",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )

                val pollutants = listOf(
                    PollutantItemData("PM2.5", currentAir.pm2_5 ?: 0.0, "μg/m³"),
                    PollutantItemData("PM10", currentAir.pm10 ?: 0.0, "μg/m³"),
                    PollutantItemData("CO", currentAir.co ?: 0.0, "μg/m³"),
                    PollutantItemData("NO₂", currentAir.no2 ?: 0.0, "μg/m³"),
                    PollutantItemData("O₃", currentAir.o3 ?: 0.0, "μg/m³"),
                    PollutantItemData("SO₂", currentAir.so2 ?: 0.0, "μg/m³")
                )

                pollutants.chunked(3).forEach { rowPollutants ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowPollutants.forEach { pollutant ->
                            Box(modifier = Modifier.weight(1f)) {
                                PollutantSmallCard(pollutant)
                            }
                        }
                        // Fill empty spaces in the row if any
                        repeat(3 - rowPollutants.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(32.dp))

                // Hourly Forecast for the next 24 hours
                com.ukweather.live.ui.screens.home.SectionTitle("24-Hour Forecast")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    com.ukweather.live.ui.screens.home.HourlyTrendChart(
                        hourly = weather.hourly,
                        daily = weather.daily.getOrNull(0),
                        isCelsius = true, // We should ideally get this from settings
                        startIndex = currentHourIdx,
                        maxHours = 24
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PollutantSmallCard(data: PollutantItemData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                data.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                "${data.value.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class PollutantItemData(
    val name: String,
    val value: Double,
    val unit: String
)



