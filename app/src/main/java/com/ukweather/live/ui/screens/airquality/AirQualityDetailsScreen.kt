package com.ukweather.live.ui.screens.airquality

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.ukweather.live.ui.components.GlassCard
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
import com.ukweather.live.util.WeatherUtils
import com.ukweather.live.R
import androidx.compose.ui.res.stringResource

import com.ukweather.live.ui.theme.*

@Composable
fun AirQualityDetailsScreen(
    cityName: String,
    weather: com.ukweather.live.data.model.WeatherDomain?,
    airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse?,
    onBack: () -> Unit
) {
    val current = airQuality?.current ?: return
    val aqi = current.aqi ?: 0
    val scrollState = rememberScrollState()

    val (statusRes, color, adviceRes) = when {
        aqi <= 20 -> Triple(R.string.aqi_good, StatusSuccess, R.string.aqi_advice_good)
        aqi <= 40 -> Triple(R.string.aqi_fair, WeatherRainLight, R.string.aqi_advice_fair)
        aqi <= 60 -> Triple(R.string.aqi_moderate, WeatherSunnyLight, R.string.aqi_advice_moderate)
        aqi <= 80 -> Triple(R.string.aqi_poor, StatusWarning, R.string.aqi_advice_poor)
        aqi <= 100 -> Triple(R.string.aqi_very_poor, StatusDangerLight, R.string.aqi_advice_very_poor)
        else -> Triple(R.string.aqi_extremely_poor, WeatherThunderstormLight, R.string.aqi_advice_extremely_poor)
    }

    val activeState = if (weather != null) {
        val currentHourIdx = weather.hourly.indexOfFirst { 
            it.time.startsWith(weather.current.time.substring(0, 13)) 
        }.coerceAtLeast(0)
        val currentPProb = weather.hourly.getOrNull(currentHourIdx)?.precipitationProbability ?: 0
        WeatherUtils.getWeatherIcon(weather.current.weatherCode, isDay = weather.current.isDay, humidity = currentPProb).animation
    } else {
        com.ukweather.live.util.WeatherState.CLOUDY
    }
    val gradientColors = WeatherUtils.getGradientForState(activeState)

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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.aqi_title),
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

            Spacer(modifier = Modifier.height(40.dp))

            // Main AQI Display
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                aqi.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                stringResource(R.string.aqi_format, stringResource(statusRes)),
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        stringResource(statusRes),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        stringResource(adviceRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                stringResource(R.string.pollutants_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            val pollutants = listOf(
                PollutantData("PM2.5", current.pm2_5 ?: 0.0, "μg/m³"),
                PollutantData("PM10", current.pm10 ?: 0.0, "μg/m³"),
                PollutantData("CO", current.co ?: 0.0, "μg/m³"),
                PollutantData("NO₂", current.no2 ?: 0.0, "μg/m³"),
                PollutantData("O₃", current.o3 ?: 0.0, "μg/m³"),
                PollutantData("SO₂", current.so2 ?: 0.0, "μg/m³")
            )

            pollutants.chunked(2).forEach { rowPollutants ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    rowPollutants.forEachIndexed { index, pollutant ->
                        Column(modifier = Modifier.weight(1f)) {
                            PollutantItem(pollutant)
                        }
                        if (index == 0 && rowPollutants.size > 1) {
                            Spacer(modifier = Modifier.width(16.dp))
                        } else if (rowPollutants.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Health Advice Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.health_advice_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val recommendationResId = when {
                        aqi <= 20 -> R.string.aqi_health_good
                        aqi <= 40 -> R.string.aqi_health_fair
                        aqi <= 60 -> R.string.aqi_health_moderate
                        aqi <= 80 -> R.string.aqi_health_poor
                        aqi <= 100 -> R.string.aqi_health_very_poor
                        else -> R.string.aqi_health_extremely_poor
                    }
                    
                    Text(
                        stringResource(recommendationResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                stringResource(R.string.aqi_forecast_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val hourlyAqi = airQuality?.hourly
                if (hourlyAqi != null && weather != null) {
                    val currentHourIdx = weather.hourly.indexOfFirst { 
                        it.time.startsWith(weather.current.time.substring(0, 13)) 
                    }.coerceAtLeast(0)
                    
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(24) { index ->
                            val idx = currentHourIdx + index
                            if (idx < (hourlyAqi.aqi?.size ?: 0)) {
                                val aqiVal = hourlyAqi.aqi?.getOrNull(idx) ?: 0
                                val time = hourlyAqi.time.getOrNull(idx)?.substringAfterLast("T")?.substring(0, 5) ?: "--:--"
                                val statusResId = when {
                                    aqiVal <= 20 -> R.string.aqi_good
                                    aqiVal <= 40 -> R.string.aqi_fair
                                    aqiVal <= 60 -> R.string.aqi_moderate
                                    aqiVal <= 80 -> R.string.aqi_poor
                                    aqiVal <= 100 -> R.string.aqi_very_poor
                                    else -> R.string.aqi_extremely_poor
                                }
                                val statusColor = when {
                                    aqiVal <= 20 -> StatusSuccess
                                    aqiVal <= 40 -> WeatherRainLight
                                    aqiVal <= 60 -> WeatherSunnyLight
                                    aqiVal <= 80 -> StatusWarning
                                    aqiVal <= 100 -> StatusDangerLight
                                    else -> WeatherThunderstormLight
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(time, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(modifier = Modifier.size(12.dp).background(statusColor, CircleShape))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(aqiVal.toString(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PollutantItem(data: PollutantData) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                data.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${data.value.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                data.unit,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

data class PollutantData(
    val name: String,
    val value: Double,
    val unit: String
)



