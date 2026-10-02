const fs = require('fs');
const file = 'd:\\AKS PROJECTs\\Skycast\\app\\src\\main\\java\\com\\skycast\\Localweather\\liveForecast\\radar\\ui\\screens\\home\\HomeScreen.kt';
let content = fs.readFileSync(file, 'utf8');

// 1. Fix the function definition
content = content.replace(
    /fun MoonCard\(weather\.daily\.getOrNull\(0\)\?\.moonrise, weather\.daily\.getOrNull\(0\)\?\.moonset\)/,
    'fun MoonCard(moonrise: String?, moonset: String?)'
);

// 2. Fix the second invocation which is deep inside DayDetailsScreen
content = content.replace(
    /MoonCard\(weather\.daily\.getOrNull\(0\)\?\.moonrise, weather\.daily\.getOrNull\(0\)\?\.moonset\)/g,
    function(match, offset, str) {
        // If it's the very first invocation in HomeScreen, it should be weather.daily
        // If it's the second invocation in DayDetailsScreen, it should be daily
        return match;
    }
);

// Actually, wait, let's just do a specific replace for the second invocation
content = content.replace(
    /Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s*MoonCard\(weather\.daily\.getOrNull\(0\)\?\.moonrise, weather\.daily\.getOrNull\(0\)\?\.moonset\)/,
    function(match) {
        // This targets the first one in HomeScreen.
        return match;
    }
);

content = content.replace(
    /SunsetCard\(daily\.getOrNull\(dayIndex\)\?\.sunrise, daily\.getOrNull\(dayIndex\)\?\.sunset\)\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s*MoonCard\(weather\.daily\.getOrNull\(0\)\?\.moonrise, weather\.daily\.getOrNull\(0\)\?\.moonset\)/,
    'SunsetCard(daily.getOrNull(dayIndex)?.sunrise, daily.getOrNull(dayIndex)?.sunset)\n                Spacer(modifier = Modifier.height(16.dp))\n                MoonCard(daily.getOrNull(dayIndex)?.moonrise, daily.getOrNull(dayIndex)?.moonset)'
);

// 3. Fix the body of MoonCard
const replacementBody = `fun MoonCard(moonrise: String?, moonset: String?) {
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
}`;

content = content.replace(/fun MoonCard\(moonrise: String\?, moonset: String\?\) \{[\s\S]*?Text\("5:51 AM"[\s\S]*?Text\("5:27 PM"[\s\S]*?\}\n\}/, replacementBody);

fs.writeFileSync(file, content, 'utf8');
console.log("Fixed!");
