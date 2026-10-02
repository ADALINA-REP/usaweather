const fs = require('fs');
const file = 'd:\\AKS PROJECTs\\Skycast\\app\\src\\main\\java\\com\\skycast\\Localweather\\liveForecast\\radar\\ui\\screens\\home\\HomeScreen.kt';
let content = fs.readFileSync(file, 'utf8');

// The previous script handled most issues. We just need to fix the specific sports faces.
// They are exactly localized around hAdvice.sportStatusResId
let block = `
                            val emoji = when (hAdvice.sportStatusResId) {
                                R.string.status_excellent -> "🤩"
                                R.string.status_good -> "😊"
                                R.string.status_decent -> "😐"
                                else -> "😕"
                            }
`;

// regex target matching the specific when block for sports
content = content.replace(/val emoji = when \(hAdvice\.sportStatusResId\) \{[\s\S]*?else -> ".*"[\s\S]*?\}/, block.trim());

fs.writeFileSync(file, content, 'utf8');
console.log('Fixed last remaining encoding issues carefully');
