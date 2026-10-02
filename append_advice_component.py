import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_component = """
@Composable
fun RunningAdviceCard(
    advice: WeatherUtils.WeatherAdvice,
    hourly: com.ukweatherlive.data.api.HourlyWeather,
    currentHourIdx: Int
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Running", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                
                // Smart Advice Badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(advice.smartAdvice, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Large Runner Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏃", fontSize = 32.sp)
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        advice.sportStatus, 
                        style = MaterialTheme.typography.headlineLarge, 
                        color = Color.White, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        advice.sportAdvice, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(20.dp))
            
            // Hourly indicators (3-hour lookahead)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                for (i in 1..3) {
                    val idx = currentHourIdx + i
                    if (idx < hourly.time.size) {
                        val fullTime = hourly.time[idx]
                        val hourStr = fullTime.substring(11, 13).toInt()
                        val amPm = if (hourStr >= 12) "PM" else "AM"
                        val displayTime = if (hourStr > 12) hourStr - 12 else if (hourStr == 0) 12 else hourStr
                        
                        val temp = hourly.temperatures[idx] ?: 15.0
                        val rainProb = hourly.precipitationProbability.getOrNull(idx) ?: 0
                        val wind = 10.0
                        val humidity = 50
                        val code = hourly.weatherCodes[idx] ?: 0
                        
                        val hourAdvice = WeatherUtils.getWeatherAdvice(temp, rainProb, wind, humidity, code)
                        
                        val emoji = when (hourAdvice.sportStatus) {
                            "Excellent" -> "😊"
                            "Good" -> "🙂"
                            "Decent" -> "😐"
                            else -> "🙁"
                        }
                        
                        val statusColor = when (hourAdvice.sportStatus) {
                            "Excellent" -> Color(0xFF4CAF50)
                            "Good" -> Color(0xFF8BC34A)
                            "Decent" -> Color(0xFFFFEB3B)
                            else -> Color(0xFFF44336)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$displayTime $amPm", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Box(
                                modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(hourAdvice.sportStatus, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
"""

with open(file_path, 'a', encoding='utf-8') as f:
    f.write(new_component)
