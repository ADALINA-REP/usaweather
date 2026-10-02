import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find the start and end of the Success content block
# We know it starts around 180 and ends around 358
start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "// --- Move Unified Activity Logic Up ---" in line:
        start_idx = i
        break

if start_idx == -1:
    print("Could not find start marker")
    sys.exit(1)

# The block ends after the last Spacer(40.dp) before the brace closing the Scrollable Column
# Let's find the closing brace that follows the Spacer(40.dp)
for i in range(start_idx + 1, len(lines)):
    if "Spacer(modifier = Modifier.height(40.dp))" in lines[i]:
        # The next line should be the closing brace for the success Column
        end_idx = i + 1
        break

if end_idx == -1:
    print("Could not find end marker")
    sys.exit(1)

new_content = """                                     // --- PRECALC LOGIC ---
                                     val (score, scoreMsg) = WeatherUtils.calculateOutdoorScore(
                                         weather.current?.temperature ?: 0.0,
                                         weather.current?.windSpeed ?: 0.0,
                                         currentRainChance
                                     )
                                     val (sportScore, sportMsg) = WeatherUtils.calculateSportScore(
                                         weather.current?.temperature ?: 0.0,
                                         weather.current?.windSpeed ?: 0.0,
                                         currentRainChance,
                                         state.airQuality?.current?.aqi ?: 30
                                     )
                                     val currentHourIndex = weather.hourly.time.indexOfFirst { 
                                         it.startsWith(weather.current?.time?.substring(0, 13) ?: "") 
                                     }.coerceAtLeast(0)
                                     
                                     val sportAqiMsg = WeatherUtils.getSportAqiMessage(state.airQuality?.current?.aqi ?: 30)
                                     val gearList = WeatherUtils.getEquipmentRecommendation(
                                         weather.current?.temperature ?: 0.0,
                                         weather.current?.windSpeed ?: 0.0,
                                         weather.current?.weatherCode ?: 0
                                     )
                                     val hoursRemainingToday = 24 - (currentHourIndex % 24)
                                     val bestTime = WeatherUtils.findBestExerciseTime(weather.hourly, currentHourIndex)

                                     // --- PROFESSIONAL HEADER & ILLUSTRATION ---
                                     Row(
                                         modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Column(modifier = Modifier.weight(1f)) {
                                             Row(verticalAlignment = Alignment.CenterVertically) {
                                                 Text(
                                                     state.cityName, 
                                                     style = MaterialTheme.typography.displaySmall, 
                                                     color = Color.White, 
                                                     fontWeight = FontWeight.Bold,
                                                     maxLines = 1
                                                 )
                                                 IconButton(onClick = { 
                                                     viewModel.toggleFavorite(state.cityName, state.latitude, state.longitude)
                                                 }, modifier = Modifier.size(40.dp)) {
                                                     Icon(
                                                         imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                         contentDescription = "Favorite",
                                                         tint = if (state.isFavorite) Color.Red else Color.White.copy(alpha = 0.8f),
                                                         modifier = Modifier.size(24.dp)
                                                     )
                                                 }
                                             }
                                             Text(
                                                 currentIconData.description, 
                                                 style = MaterialTheme.typography.titleMedium, 
                                                 color = Color.White.copy(alpha = 0.9f)
                                             )
                                             Text(
                                                 "${weather.current?.temperature?.toInt() ?: 0}°", 
                                                 style = MaterialTheme.typography.displayLarge.copy(
                                                     fontSize = 110.sp,
                                                     fontWeight = FontWeight.Light,
                                                     shadow = androidx.compose.ui.graphics.Shadow(
                                                         color = Color.Black.copy(alpha = 0.1f),
                                                         offset = Offset(0f, 4f),
                                                         blurRadius = 8f
                                                     )
                                                 ), 
                                                 color = Color.White
                                             )
                                         }
                                         
                                         // Premium Illustration from Generated Asset
                                         AsyncImage(
                                             model = "file:///C:/Users/lenovo/.gemini/antigravity/brain/7c102fa8-8897-4783-9604-4530bba1b3f3/weather_illustration_1774890684761.png",
                                             contentDescription = null,
                                             modifier = Modifier.size(160.dp).alpha(0.9f),
                                             contentScale = ContentScale.Fit
                                         )
                                     }

                                     if (state.favorites.isNotEmpty()) {
                                         LazyRow(
                                             modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                             horizontalArrangement = Arrangement.Start
                                         ) {
                                             items(state.favorites.size) { index ->
                                                 val fav = state.favorites[index]
                                                 AssistChip(
                                                     onClick = { viewModel.selectCity(fav.name, fav.latitude, fav.longitude) },
                                                     label = { Text(fav.name, color = Color.White) },
                                                     leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                                                     colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                                     border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                                     modifier = Modifier.padding(end = 8.dp)
                                                 )
                                             }
                                         }
                                     }
                                     
                                     Spacer(modifier = Modifier.height(24.dp))
                                     
                                     // --- ACTIVITY & PRIMARY METRICS ---
                                     GlassCard(modifier = Modifier.fillMaxWidth()) {
                                         Column(modifier = Modifier.padding(20.dp)) {
                                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                 WeatherMetric("Feels", "${weather.current?.feelsLike?.toInt() ?: 0}°", "🌡️")
                                                 WeatherMetric("Wind", "${weather.current?.windSpeed?.toInt() ?: 0} km/h", "💨")
                                                 WeatherMetric("Humidity", "${weather.current?.humidity ?: 0}%", "💧")
                                                 WeatherMetric("Rain", "${currentRainChance}%", "🌧️")
                                             }
                                             
                                             Spacer(modifier = Modifier.height(20.dp))
                                             Divider(color = Color.White.copy(alpha = 0.1f))
                                             Spacer(modifier = Modifier.height(20.dp))
                                             
                                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                 Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                     Text("🏃 RUNNING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                                     Spacer(modifier = Modifier.height(8.dp))
                                                     Text(sportMsg, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                                 }
                                                 Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                                     Text("🌿 OUTDOOR", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                                     Spacer(modifier = Modifier.height(8.dp))
                                                     Text(scoreMsg, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                                 }
                                             }
                                         }
                                     }
                                     
                                     Spacer(modifier = Modifier.height(24.dp))

                                     // --- HOURLY TREND WITH LINE/BAR CHART ---
                                     SectionTitle("Hourly Trend")
                                     GlassCard(modifier = Modifier.fillMaxWidth()) {
                                         HourlyTrendChart(
                                             hourly = weather.hourly, 
                                             startIndex = currentHourIndex, 
                                             maxHours = hoursRemainingToday
                                         )
                                     }

                                     Spacer(modifier = Modifier.height(24.dp))

                                     // --- GRID OF SPECIAL METRICS ---
                                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                         Box(modifier = Modifier.weight(1f)) {
                                            UVIndexCard(weather.daily.uvIndexMax?.get(0) ?: 0.0)
                                         }
                                         Box(modifier = Modifier.weight(1f)) {
                                             SunsetCard(weather.daily.sunrise?.get(0), weather.daily.sunset?.get(0))
                                         }
                                     }
                                     
                                     Spacer(modifier = Modifier.height(16.dp))
                                     
                                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                         Box(modifier = Modifier.weight(1f)) {
                                             MetricSmallCard("Pressure", "${weather.current?.surfacePressure?.toInt() ?: 1020} mb", "Currently stable", "📊")
                                         }
                                         Box(modifier = Modifier.weight(1f)) {
                                             MetricSmallCard("Visibility", "${(weather.hourly.visibility?.get(currentHourIndex) ?: 10.0).toInt()} km", "Good visibility", "👁️")
                                         }
                                     }

                                     Spacer(modifier = Modifier.height(32.dp))

                                     // --- VERTICAL 7-DAY FORECAST ---
                                     SectionTitle("7-Day Forecast")
                                     GlassCard(modifier = Modifier.fillMaxWidth()) {
                                         Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                             for (index in 0 until 7) {
                                                 DailyForecastRow(
                                                     index = index, 
                                                     daily = weather.daily, 
                                                     onClick = { selectedDayIndex = index }
                                                 )
                                                 if (index < 6) {
                                                     Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
                                                 }
                                             }
                                         }
                                     }

                                     Spacer(modifier = Modifier.height(40.dp))
"""

# Replace the block
lines[start_idx:end_idx] = [new_content]

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
