const fs = require('fs');
const file = 'd:\\AKS PROJECTs\\Skycast\\app\\src\\main\\java\\com\\skycast\\Localweather\\liveForecast\\radar\\ui\\screens\\home\\HomeScreen.kt';
let content = fs.readFileSync(file, 'utf8');

const replacements = {
  'ðŸŒ§ï¸ ': '🌧️',
  'ðŸŒ§ï¸': '🌧️',
  'Â°': '°',
  'â˜€ï¸': '☀️',
  'â˜…': '★',
  'â€¢': '•',
  'ðŸŒž': '🌞',
  'â˜': '☔️',
  'ðŸŒ±': '🌱',
  'ðŸŒ™': '🌙',
  'ðŸŒ¤ï¸': '🌤️',
  'ðŸŒ¦ï¸': '🌦️'
};

for (const [bad, good] of Object.entries(replacements)) {
  content = content.split(bad).join(good);
}

fs.writeFileSync(file, content, 'utf8');
console.log('Fixed encoding issues');
