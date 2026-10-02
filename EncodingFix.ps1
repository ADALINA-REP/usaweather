$file = "d:\AKS PROJECTs\Skycast\app\src\main\java\com\skycast\Localweather\liveForecast\radar\ui\screens\home\HomeScreen.kt"
$content = Get-Content $file -Raw -Encoding UTF8
$content = $content.Replace('Â°', '°')
$content = $content.Replace('ðŸŒ¡ï¸', '🌡️')
$content = $content.Replace('ðŸ’¨', '💨')
$content = $content.Replace('ðŸ’§', '💧')
$content = $content.Replace('â˜‚ï¸', '☔️')
$content = $content.Replace('â˜€ï¸', '☀️')
$content = $content.Replace('ðŸŒ±', '🌱')
$content = $content.Replace('ðŸŒ™', '🌙')
$content = $content.Replace('ðŸŒ¤ï¸', '🌤️')
$content = $content.Replace('ðŸŒ¦ï¸', '🌦️')
$content = $content.Replace('â˜…', '★')
$content = $content.Replace('â€¢', '•')
$content = $content.Replace('ðŸŒž', '🌞')
$content = $content.Replace('â˜', '☂') # In case there's a dangling umbrella

[System.IO.File]::WriteAllText($file, $content, [System.Text.Encoding]::UTF8)
Write-Output "Done"
