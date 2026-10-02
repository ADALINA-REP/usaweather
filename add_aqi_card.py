import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 1. Update getAqiDescription and getAqiColor
description_found = False
color_found = False

for i, line in enumerate(lines):
    if "fun getAqiDescription" in line:
        description_found = True
        bracket_count = 0
        end_idx = -1
        for j in range(i, len(lines)):
            bracket_count += lines[j].count('{')
            bracket_count -= lines[j].count('}')
            if j > i and bracket_count == 0:
                end_idx = j + 1
                break
        if end_idx != -1:
            new_desc = """fun getAqiDescription(aqi: Int): String {
    return when {
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Unhealthy for Sensitive Groups"
        aqi <= 200 -> "Unhealthy"
        aqi <= 300 -> "Very Unhealthy"
        else -> "Hazardous"
    }
}
"""
            lines[i:end_idx] = [new_desc]

for i, line in enumerate(lines):
    if "fun getAqiColor" in line:
        color_found = True
        bracket_count = 0
        end_idx = -1
        for j in range(i, len(lines)):
            bracket_count += lines[j].count('{')
            bracket_count -= lines[j].count('}')
            if j > i and bracket_count == 0:
                end_idx = j + 1
                break
        if end_idx != -1:
            new_color = """fun getAqiColor(aqiValue: Int): Color {
    return when {
        aqiValue <= 50 -> Color(0xFF4CAF50)
        aqiValue <= 100 -> Color(0xFFFFEB3B)
        aqiValue <= 150 -> Color(0xFFFF9800)
        aqiValue <= 200 -> Color(0xFFF44336)
        aqiValue <= 300 -> Color(0xFF9C27B0)
        else -> Color(0xFF7E0023)
    }
}
"""
            lines[i:end_idx] = [new_color]

# 2. Add AQICard component at the bottom
aqi_card_component = """
@Composable
fun AQICard(aqiValue: Int) {
    val description = getAqiDescription(aqiValue)
    val aqiColor = getAqiColor(aqiValue)
    val progress = aqiValue / 500f // Common US AQI Max for visualization

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AQI", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$description ($aqiValue)", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(18.dp))
            
            // Premium Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(aqiColor.copy(alpha = 0.8f), aqiColor)),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }
    }
}
"""
lines.append(aqi_card_component)

# 3. Insert AQICard below Hourly Trend
insert_idx = -1
for i, line in enumerate(lines):
    if "HourlyTrendChart(" in line:
        # Go to the end of the GlassCard
        bracket_count = 0
        for j in range(i, len(lines)):
            bracket_count += lines[j].count('{')
            bracket_count -= lines[j].count('}')
            if j > i and bracket_count == 0:
                insert_idx = j + 2 # Skip the Spacer(height(24.dp)) if it exists
                break
        break

if insert_idx != -1:
    aqi_insertion = """
                                     // --- AQI DASHBOARD ---
                                     SectionTitle("Air Quality Index")
                                     AQICard(aqiValue = state.airQuality?.current?.usAqi ?: state.airQuality?.current?.aqi ?: 30)
                                     
                                     Spacer(modifier = Modifier.height(24.dp))
"""
    lines.insert(insert_idx, aqi_insertion)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
