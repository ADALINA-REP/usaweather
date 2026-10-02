import sys

file_path = r"d:\\AKS PROJECTs\\UK Weather Live\\app\\src\\main\\java\\com\\ukweatherlive\\ui\\screens\\home\\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 1. Update imports
# Replace around line 40
import_idx = -1
for i, line in enumerate(lines):
    if "import androidx.compose.foundation.shape.CircleShape" in line:
        import_idx = i
        break

if import_idx != -1:
    lines[import_idx] = """import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.*
import androidx.compose.animation.*
"""

# 2. Find and replace RunningAdviceCard call
# Current call: 
# RunningAdviceCard(
#     advice = currentAdvice,
#     hourly = weather.hourly,
#     currentHourIdx = currentHourIndex
# )
call_start = -1
call_end = -1
for i, line in enumerate(lines):
    if "RunningAdviceCard(" in line:
        call_start = i
        # Find closing )
        for j in range(i, len(lines)):
            if ")" in lines[j]:
                call_end = j + 1
                break
        break

if call_start != -1:
    new_call = """                                     SportAdvicePager(
                                         temp = weather.current?.temperature ?: 15.0,
                                         rainProb = currentRainChance,
                                         wind = weather.current?.windSpeed ?: 10.0,
                                         humidity = weather.current?.humidity ?: 50,
                                         weatherCode = weather.current?.weatherCode ?: 0,
                                         hourly = weather.hourly,
                                         currentHourIdx = currentHourIndex
                                     )
"""
    lines[call_start:call_end] = [new_call]

# 3. Add components at bottom, replacing RunningAdviceCard definition
runner_idx = -1
for i, line in enumerate(lines):
    if "@Composable" in line and "fun RunningAdviceCard" in line:
        runner_idx = i
        break

if runner_idx != -1:
    del lines[runner_idx:]

new_components = """
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SportAdvicePager(
    temp: Double,
    rainProb: Int,
    wind: Double,
    humidity: Int,
    weatherCode: Int,
    hourly: com.ukweatherlive.data.api.HourlyWeather,
    currentHourIdx: Int
) {
    val sports = listOf(
        WeatherUtils.SportType.RUNNING,
        WeatherUtils.SportType.CYCLING,
        WeatherUtils.SportType.HIKING
    )
    val pagerState = rememberPagerState(pageCount = { sports.size })
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            SportAdviceCard(
                temp = temp,
                rainProb = rainProb,
                wind = wind,
                humidity = humidity,
                weatherCode = weatherCode,
                hourly = hourly,
                currentHourIdx = currentHourIdx,
                sportType = sports[page]
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Dots indicator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(sports.size) { i ->
                val color = if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.3f)
                val width = if (pagerState.currentPage == i) 16.dp else 6.dp
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(6.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

@Composable
fun SportAdviceCard(
    temp: Double,
    rainProb: Int,
    wind: Double,
    humidity: Int,
    weatherCode: Int,
    hourly: com.ukweatherlive.data.api.HourlyWeather,
    currentHourIdx: Int,
    sportType: WeatherUtils.SportType
) {
    val advice = WeatherUtils.getWeatherAdvice(temp, rainProb, wind, humidity, weatherCode, sportType)
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row: Sport Name & Smart Advice
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(advice.sportName, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                
                // Smart Advice Badge (Hydration, clothing, etc.)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(advice.smartAdvice, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Middle Main Row: Icon & Dynamic Hourly Indicators side-by-side
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Large Rounded Sport Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(advice.sportIcon, fontSize = 28.sp)
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Dynamic Hourly Indicators (Next 3 hours)
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (i in 1..3) {
                        val idx = currentHourIdx + i
                        if (idx < hourly.time.size) {
                            val fullTime = hourly.time[idx]
                            val hourNum = fullTime.substring(11, 13).toInt()
                            val displayTime = if (hourNum > 12) hourNum - 12 else if (hourNum == 0) 12 else hourNum
                            val pmAm = if (hourNum >= 12) "PM" else "AM"
                            
                            val hTemp = hourly.temperatures[idx] ?: 15.0
                            val hRain = hourly.precipitationProbability.getOrNull(idx) ?: 0
                            val hAdvice = WeatherUtils.getWeatherAdvice(hTemp, hRain, 10.0, 50, hourly.weatherCodes[idx] ?: 0, sportType)
                            
                            val emoji = when (hAdvice.sportStatus) {
                                "Excellent" -> "😊"
                                "Good" -> "🙂"
                                "Decent" -> "😐"
                                else -> "🙁"
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$displayTime $pmAm", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(hAdvice.sportStatus, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bottom Status Text Section
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
}
"""
lines.append(new_components)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
