import sys

file_path = r"d:\AKS PROJECTs\UK Weather Live\app\src\main\java\com\ukweatherlive\ui\screens\home\HomeScreen.kt"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find the start of the activity/metric block
start_idx = -1
for i, line in enumerate(lines):
    if "// --- ACTIVITY & PRIMARY METRICS ---" in line:
        start_idx = i
        break

if start_idx == -1:
    print("Could not find start marker")
    sys.exit(1)

# Find the end of that GlassCard block
# It ends after 2 nested Row/Column blocks
end_idx = -1
bracket_count = 0
for i in range(start_idx, len(lines)):
    bracket_count += lines[i].count('{')
    bracket_count -= lines[i].count('}')
    if i > start_idx and bracket_count == 0:
        end_idx = i + 1
        break

if end_idx == -1:
    print("Could not find end of GlassCard")
    sys.exit(1)

# Also update the precalc logic to get the advice
advice_calc_line = -1
for i in range(180, start_idx):
    if "val currentHourIndex" in lines[i]:
        advice_calc_line = i
        break

new_calc = """                                     val currentAdvice = WeatherUtils.getWeatherAdvice(
                                         weather.current?.temperature ?: 15.0,
                                         currentRainChance,
                                         weather.current?.windSpeed ?: 10.0,
                                         weather.current?.humidity ?: 50,
                                         weather.current?.weatherCode ?: 0
                                     )
"""

# Find where to insert the advice card
# It should replace the old GlassCard block

new_advice_block = """                                     // --- PRIMARY WEATHER METRICS ---
                                     GlassCard(modifier = Modifier.fillMaxWidth()) {
                                         Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                             WeatherMetric("Feels", "${weather.current?.feelsLike?.toInt() ?: 0}°", "🌡️")
                                             WeatherMetric("Wind", "${weather.current?.windSpeed?.toInt() ?: 0} km/h", "💨")
                                             WeatherMetric("Humidity", "${weather.current?.humidity ?: 0}%", "💧")
                                             WeatherMetric("Rain", "${currentRainChance}%", "🌧️")
                                         }
                                     }
                                     
                                     Spacer(modifier = Modifier.height(24.dp))

                                     // --- RUNNING ADVICE DASHBOARD ---
                                     RunningAdviceCard(
                                         advice = currentAdvice,
                                         hourly = weather.hourly,
                                         currentHourIdx = currentHourIndex
                                     )
"""

# Apply changes
# 1. Update calc
lines.insert(advice_calc_line, new_calc)

# Re-search for markers since indices shifted
for i, line in enumerate(lines):
    if "// --- ACTIVITY & PRIMARY METRICS ---" in line:
        start_idx = i
        break

end_idx = -1
bracket_count = 0
for i in range(start_idx, len(lines)):
    bracket_count += lines[i].count('{')
    bracket_count -= lines[i].count('}')
    if i > start_idx and bracket_count == 0:
        end_idx = i + 1
        break

lines[start_idx:end_idx] = [new_advice_block]

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
