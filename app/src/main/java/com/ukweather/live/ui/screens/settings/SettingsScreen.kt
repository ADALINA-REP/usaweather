package com.ukweather.live.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.ukweather.live.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val isCelsius by viewModel.isCelsius.collectAsState()
    val isAlertsEnabled by viewModel.isAlertsEnabled.collectAsState()
    val isRainAlertEnabled by viewModel.isRainAlertEnabled.collectAsState()
    val isTempAlertEnabled by viewModel.isTempAlertEnabled.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isMb by viewModel.isMb.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.settings), 
            style = MaterialTheme.typography.displaySmall, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        SettingToggle(
            title = if (isCelsius) stringResource(R.string.unit_celsius) else stringResource(R.string.unit_fahrenheit),
            checked = isCelsius
        ) { viewModel.toggleCelsius(it) }

        SettingToggle(
            title = if (isMb) stringResource(R.string.pressure_unit_mb) else stringResource(R.string.pressure_unit_hpa),
            checked = isMb
        ) { viewModel.toggleMb(it) }
        
        SettingToggle(stringResource(R.string.general_alerts), isAlertsEnabled) { viewModel.toggleAlerts(it) }
        
        if (isAlertsEnabled) {
            Column(modifier = Modifier.padding(start = 24.dp)) {
                SettingToggle(stringResource(R.string.rain_alerts), isRainAlertEnabled) { viewModel.toggleRainAlerts(it) }
                SettingToggle(stringResource(R.string.temp_alerts), isTempAlertEnabled) { viewModel.toggleTempAlerts(it) }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        LanguageSelector(
            currentLanguage = selectedLanguage,
            languages = viewModel.popularLanguages,
            onLanguageSelected = { viewModel.toggleLanguage(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        SettingToggle(stringResource(R.string.label_dark_mode), isDarkMode) { viewModel.toggleDarkMode(it) }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.label_legal), 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        
        val context = androidx.compose.ui.platform.LocalContext.current
        LegalItem(stringResource(R.string.label_privacy_policy)) {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://skycast-weather.github.io/privacy-policy"))
            context.startActivity(intent)
        }
        
        var showDisclaimer by remember { mutableStateOf(false) }
        LegalItem(stringResource(R.string.label_data_disclaimer)) {
            showDisclaimer = true
        }

        if (showDisclaimer) {
            AlertDialog(
                onDismissRequest = { showDisclaimer = false },
                title = { Text(stringResource(R.string.label_data_disclaimer)) },
                text = { Text(stringResource(R.string.data_disclaimer_content)) },
                confirmButton = {
                    TextButton(onClick = { showDisclaimer = false }) {
                        Text(stringResource(R.string.got_it))
                    }
                }
            )
        }
    }
}

@Composable
fun LegalItem(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(currentLanguage: String, languages: List<String>, onLanguageSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.label_language), 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${getFlagEmoji(currentLanguage)}  $currentLanguage",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getFlagEmoji(language), modifier = Modifier.padding(end = 12.dp))
                                Text(language, color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        onClick = {
                            onLanguageSelected(language)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

fun getFlagEmoji(language: String): String {
    return when (language) {
        "English" -> "\uD83C\uDDFA\uD83C\uDDF8"
        "Spanish" -> "\uD83C\uDDEA\uD83C\uDDF8"
        "French" -> "\uD83C\uDDEB\uD83C\uDDF7"
        "German" -> "\uD83C\uDDE9\uD83C\uDDEA"
        "Italian" -> "\uD83C\uDDEE\uD83C\uDDF9"
        "Portuguese" -> "\uD83C\uDDF5\uD83C\uDDF9"
        "Russian" -> "\uD83C\uDDF7\uD83C\uDDFA"
        "Chinese" -> "\uD83C\uDDE8\uD83C\uDDF3"
        "Japanese" -> "\uD83C\uDDEF\uD83C\uDDF5"
        "Korean" -> "\uD83C\uDDF0\uD83C\uDDF7"
        "Arabic" -> "\uD83C\uDDF8\uD83C\uDDE6"
        "Hindi" -> "\uD83C\uDDEE\uD83C\uDDF3"
        "Bengali" -> "\uD83C\uDDE7\uD83C\uDDE9"
        "Turkish" -> "\uD83C\uDDF9\uD83C\uDDF7"
        "Dutch" -> "\uD83C\uDDF3\uD83C\uDDF1"
        "Polish" -> "\uD83C\uDDF5\uD83C\uDDF1"
        "Swedish" -> "\uD83C\uDDF8\uD83C\uDDEA"
        "Greek" -> "\uD83C\uDDEC\uD83C\uDDF7"
        "Thai" -> "\uD83C\uDDF9\uD83C\uDDED"
        "Vietnamese" -> "\uD83C\uDDFB\uD83C\uDDF3"
        else -> "\uD83C\uDF10"
    }
}



