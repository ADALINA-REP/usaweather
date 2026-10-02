import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find the SectionTitle("Hourly Trend")
start_idx = -1
for i, line in enumerate(lines):
    if 'SectionTitle("Hourly Trend")' in line:
        start_idx = i
        break

if start_idx != -1:
    # Find the SectionTitle("Air Quality Index") or the next grid
    grid_idx = -1
    for i in range(start_idx, len(lines)):
        if '// --- GRID OF SPECIAL METRICS ---' in lines[i]:
            grid_idx = i
            break
    
    if grid_idx != -1:
        new_block = """                                      SectionTitle("Hourly Trend")
                                      GlassCard(modifier = Modifier.fillMaxWidth()) {
                                          HourlyTrendChart(
                                              hourly = weather.hourly, 
                                              startIndex = currentHourIndex, 
                                              maxHours = hoursRemainingToday
                                          )
                                      }

                                      Spacer(modifier = Modifier.height(24.dp))

                                      // --- AQI DASHBOARD ---
                                      SectionTitle("Air Quality Index")
                                      AQICard(aqiValue = state.airQuality?.current?.usAqi ?: state.airQuality?.current?.aqi ?: 49)
                                      
                                      Spacer(modifier = Modifier.height(24.dp))
"""
        lines[start_idx:grid_idx] = [new_block]

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
