package com.ukweather.live.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.ukweather.live.ui.theme.*

@Composable
fun AirQualityCard(
    airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse?,
    onClick: () -> Unit
) {
    val aqi = airQuality?.current?.aqi ?: 0
    
    val (status, color, advice) = when {
        aqi <= 20 -> Triple("Good", StatusSuccess, "Safe for outdoor activities")
        aqi <= 40 -> Triple("Fair", WeatherRainLight, "Safe for most people")
        aqi <= 60 -> Triple("Moderate", WeatherSunnyLight, "Limit prolonged outdoor exertion")
        aqi <= 80 -> Triple("Poor", StatusWarning, "Limit outdoor activities")
        aqi <= 100 -> Triple("Very Poor", StatusDangerLight, "Avoid outdoor activities")
        else -> Triple("Extremely Poor", WeatherThunderstormLight, "Stay indoors if possible")
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Air Quality Index",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        status,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        aqi.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ℹ️",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    advice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



