import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_header = [
    '                                    // --- Move Unified Activity Logic Up ---\n',
    '                                    val (score, scoreMsg) = WeatherUtils.calculateOutdoorScore(\n',
    '                                        weather.current?.temperature ?: 0.0,\n',
    '                                        weather.current?.windSpeed ?: 0.0,\n',
    '                                        currentRainChance\n',
    '                                    )\n',
    '                                    val (sportScore, sportMsg) = WeatherUtils.calculateSportScore(\n',
    '                                        weather.current?.temperature ?: 0.0,\n',
    '                                        weather.current?.windSpeed ?: 0.0,\n',
    '                                        currentRainChance,\n',
    '                                        state.airQuality?.current?.aqi ?: 30\n',
    '                                    )\n',
    '                                    val currentHourIndex = weather.hourly.time.indexOfFirst { \n',
    '                                        it.startsWith(weather.current?.time?.substring(0, 13) ?: "") \n',
    '                                    }.coerceAtLeast(0)\n',
    '\n',
    '                                    // --- 3-ZONE HEADER SECTION ---\n',
    '                                    Row(\n',
    '                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),\n',
    '                                        verticalAlignment = Alignment.CenterVertically\n',
    '                                    ) {\n',
    '                                        // Zone 1: Sport (Left)\n',
    '                                        Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {\n',
    '                                            MiniScore(sportScore, sportMsg, "🏃")\n',
    '                                        }\n',
    '\n',
    '                                        // Zone 2: Main Weather (Center Focus)\n',
    '                                        Column(\n',
    '                                            modifier = Modifier.weight(0.5f),\n',
    '                                            horizontalAlignment = Alignment.CenterHorizontally\n',
    '                                        ) {\n',
    '                                            Row(verticalAlignment = Alignment.CenterVertically) {\n',
    '                                                Text(\n',
    '                                                    state.cityName, \n',
    '                                                    style = MaterialTheme.typography.titleLarge, \n',
    '                                                    color = Color.White, \n',
    '                                                    fontWeight = FontWeight.Bold,\n',
    '                                                    maxLines = 1,\n',
    '                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis\n',
    '                                                )\n',
    '                                                IconButton(onClick = { \n',
    '                                                    viewModel.toggleFavorite(state.cityName, state.latitude, state.longitude)\n',
    '                                                }, modifier = Modifier.size(32.dp)) {\n',
    '                                                    Icon(\n',
    '                                                        imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,\n',
    '                                                        contentDescription = "Favorite",\n',
    '                                                        tint = if (state.isFavorite) Color.Red else Color.White.copy(alpha = 0.8f),\n',
    '                                                        modifier = Modifier.size(18.dp)\n',
    '                                                    )\n',
    '                                                }\n',
    '                                            }\n',
    '                                            Text(\n',
    '                                                currentIconData.description, \n',
    '                                                style = MaterialTheme.typography.bodyMedium, \n',
    '                                                color = Color.White.copy(alpha = 0.9f)\n',
    '                                            )\n',
    '                                            Text(\n',
    '                                                "${weather.current?.temperature?.toInt() ?: 0}°", \n',
    '                                                style = MaterialTheme.typography.displayMedium.copy(\n',
    '                                                    fontWeight = FontWeight.Light,\n',
    '                                                    shadow = androidx.compose.ui.graphics.Shadow(\n',
    '                                                        color = Color.Black.copy(alpha = 0.2f),\n',
    '                                                        offset = Offset(0f, 4f),\n',
    '                                                        blurRadius = 8f\n',
    '                                                    )\n',
    '                                                ), \n',
    '                                                color = Color.White\n',
    '                                            )\n',
    '                                        }\n',
    '\n',
    '                                        // Zone 3: Outdoor (Right)\n',
    '                                        Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {\n',
    '                                            MiniScore(score, scoreMsg, "🌿")\n',
    '                                        }\n',
    '                                    }\n'
]

# Replacement 1: Header (180 to 213)
lines[179:213] = new_header
# Shift = 76 - 34 = 42

# Replacement 2: Redundant Text (234 to 266)
lines[233+42:266+42] = ['                                    // --- Description/Temp moved to Zone 2 ---\n']
# Shift = 1 - 33 = -32. Total shift = 42 - 32 = 10

# Replacement 3: Redundant Logic (287 to 314)
lines[286+10:314+10] = ['                                    // --- Logic moved to top header block ---\n']

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
