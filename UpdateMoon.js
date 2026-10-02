const fs = require('fs');
const file = 'd:\\AKS PROJECTs\\Skycast\\app\\src\\main\\java\\com\\skycast\\Localweather\\liveForecast\\radar\\ui\\screens\\home\\HomeScreen.kt';
let content = fs.readFileSync(file, 'utf8');

// Update MoonCard invocations
content = content.replace(
    /MoonCard\(\)/g,
    'MoonCard(weather.daily.getOrNull(0)?.moonrise, weather.daily.getOrNull(0)?.moonset)'
);

// We need to fix the second invocation, which should use `daily.getOrNull(dayIndex)` instead of `weather`.
content = content.replace(
    /MoonCard\(weather\.daily\.getOrNull\(0\)\?\.moonrise, weather\.daily\.getOrNull\(0\)\?\.moonset\)([\s\S]*?fun MoonCard)/,
    'MoonCard(daily.getOrNull(dayIndex)?.moonrise, daily.getOrNull(dayIndex)?.moonset)$1'
);

// We need to replace the last invocation back to fun MoonCard(moonrise...
content = content.replace(
    /fun MoonCard\(daily\.getOrNull\(dayIndex\)\?\.moonrise, daily\.getOrNull\(dayIndex\)\?\.moonset\)/,
    'fun MoonCard(moonrise: String?, moonset: String?)'
);

// We need to update the body of MoonCard
content = content.replace(
    /fun MoonCard\(moonrise: String\?, moonset: String\?\) \{[\s\S]*?Text\("5:51 AM"[\s\S]*?Text\("5:27 PM"[\s\S]*?\}\n\}/,
    `fun MoonCard(moonrise: String?, moonset: String?) {
    val moonriseTime = try { java.time.LocalTime.parse(moonrise?.substringAfterLast("T")?.substring(0, 5))?.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)) } catch (e: Exception) { "---" }
    val moonsetTime = try { java.time.LocalTime.parse(moonset?.substringAfterLast("T")?.substring(0, 5))?.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)) } catch (e: Exception) { "---" }

    GlassCard(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Left Half: Moon Image and Phase Name
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🌔", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Moon phase", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
            
            // Right Half: Moonset / Moonrise Times
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Moonset", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(moonsetTime ?: "---", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Moonrise", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(moonriseTime ?: "---", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}`
);

fs.writeFileSync(file, content, 'utf8');
console.log('Fixed MoonCard updates');
