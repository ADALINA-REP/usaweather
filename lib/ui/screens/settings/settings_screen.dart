// lib/ui/screens/settings/settings_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/data/models/location_data.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = ref.watch(isDarkModeProvider);
    final isCelsius = ref.watch(isCelsiusProvider);
    final isMb = ref.watch(isMbProvider);
    final language = ref.watch(languageProvider);
    final settings = getIt<SettingsRepository>();
    final l10n = AppLocalizations.of(context);

    final bgColor = isDark ? const Color(0xFF0A0E2E) : const Color(0xFFF0F4FF);
    final cardColor = isDark ? const Color(0xFF131738) : Colors.white;
    final textColor = isDark ? Colors.white : Colors.black87;
    final subtextColor = isDark ? Colors.white54 : Colors.black45;

    return Scaffold(
      backgroundColor: bgColor,
      appBar: AppBar(
        title: Text(l10n.settingsTitle),
        backgroundColor: isDark ? const Color(0xFF131738) : Colors.white,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _SectionHeader(title: l10n.sectionDisplay, color: textColor),
          _SettingsTile(
            icon: Icons.dark_mode_rounded,
            iconColor: const Color(0xFF7C4DFF),
            title: l10n.darkMode,
            subtitle: l10n.useDarkTheme,
            cardColor: cardColor,
            textColor: textColor,
            subtextColor: subtextColor,
            trailing: Switch.adaptive(
              value: isDark,
              onChanged: (v) {
                settings.isDarkMode = v;
                ref.read(isDarkModeProvider.notifier).state = v;
              },
              activeTrackColor: const Color(0xFF00B4D8),
            ),
          ),
          const SizedBox(height: 8),
          _SectionHeader(title: l10n.sectionUnits, color: textColor),
          _SettingsTile(
            icon: Icons.thermostat_rounded,
            iconColor: const Color(0xFFFF7043),
            title: l10n.temperatureUnit,
            subtitle: isCelsius ? 'Celsius (°C)' : 'Fahrenheit (°F)',
            cardColor: cardColor,
            textColor: textColor,
            subtextColor: subtextColor,
            trailing: Switch.adaptive(
              value: isCelsius,
              onChanged: (v) {
                settings.isCelsius = v;
                ref.read(isCelsiusProvider.notifier).state = v;
              },
              activeTrackColor: const Color(0xFF00B4D8),
            ),
          ),
          const SizedBox(height: 4),
          _SettingsTile(
            icon: Icons.speed_rounded,
            iconColor: const Color(0xFF42A5F5),
            title: l10n.pressureUnit,
            subtitle: isMb ? 'Millibar (mb)' : 'Hectopascal (hPa)',
            cardColor: cardColor,
            textColor: textColor,
            subtextColor: subtextColor,
            trailing: Switch.adaptive(
              value: isMb,
              onChanged: (v) {
                settings.isMb = v;
                ref.read(isMbProvider.notifier).state = v;
              },
              activeTrackColor: const Color(0xFF00B4D8),
            ),
          ),
          const SizedBox(height: 8),
          _SectionHeader(title: l10n.sectionLanguage, color: textColor),
          _LanguageTile(
            cardColor: cardColor,
            textColor: textColor,
            subtextColor: subtextColor,
            selected: language,
            onChanged: (lang) {
              settings.selectedLanguage = lang;
              ref.read(languageProvider.notifier).state = lang;
            },
          ),
          const SizedBox(height: 8),
          _SectionHeader(title: l10n.widgetsTitle, color: textColor),
          _WidgetsTile(
            cardColor: cardColor,
            textColor: textColor,
            subtextColor: subtextColor,
          ),
          const SizedBox(height: 8),
          _SectionHeader(title: l10n.sectionAbout, color: textColor),
          _AboutTile(
              cardColor: cardColor,
              textColor: textColor,
              subtextColor: subtextColor),
          const SizedBox(height: 80),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  final Color color;
  const _SectionHeader({required this.title, required this.color});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8, top: 8),
      child: Text(title.toUpperCase(),
          style: TextStyle(
              color: color.withValues(alpha: 0.5),
              fontSize: 11,
              fontWeight: FontWeight.w700,
              letterSpacing: 1.2)),
    );
  }
}

class _SettingsTile extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final String title;
  final String subtitle;
  final Color cardColor;
  final Color textColor;
  final Color subtextColor;
  final Widget trailing;

  const _SettingsTile({
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.subtitle,
    required this.cardColor,
    required this.textColor,
    required this.subtextColor,
    required this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 8,
          ),
        ],
      ),
      child: ListTile(
        leading: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: iconColor.withValues(alpha: 0.15),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: iconColor, size: 22),
        ),
        title: Text(title,
            style: TextStyle(color: textColor, fontWeight: FontWeight.w600)),
        subtitle:
            Text(subtitle, style: TextStyle(color: subtextColor, fontSize: 12)),
        trailing: trailing,
      ),
    );
  }
}

class _LanguageTile extends ConsumerWidget {
  final Color cardColor;
  final Color textColor;
  final Color subtextColor;
  final String selected;
  final Function(String) onChanged;

  const _LanguageTile({
    required this.cardColor,
    required this.textColor,
    required this.subtextColor,
    required this.selected,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final languages = getIt<SettingsRepository>().getLanguages();
    final l10n = AppLocalizations.of(context);
    final displaySelected =
        selected == 'System Default' ? l10n.systemDefault : selected;

    return Container(
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 8,
          ),
        ],
      ),
      child: ListTile(
        leading: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: const Color(0xFF00BFA5).withValues(alpha: 0.15),
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Icon(Icons.language_rounded,
              color: Color(0xFF00BFA5), size: 22),
        ),
        title: Text(l10n.appLanguage,
            style: TextStyle(color: textColor, fontWeight: FontWeight.w600)),
        subtitle: Text(displaySelected,
            style: TextStyle(color: subtextColor, fontSize: 12)),
        trailing: const Icon(Icons.arrow_forward_ios_rounded,
            size: 14, color: Colors.grey),
        onTap: () => showModalBottomSheet(
          context: context,
          backgroundColor: cardColor,
          shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
          builder: (_) => _LanguagePicker(
            languages: languages,
            selected: selected,
            textColor: textColor,
            onChanged: onChanged,
          ),
        ),
      ),
    );
  }
}

class _LanguagePicker extends StatelessWidget {
  final List<String> languages;
  final String selected;
  final Color textColor;
  final Function(String) onChanged;

  const _LanguagePicker({
    required this.languages,
    required this.selected,
    required this.textColor,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const SizedBox(height: 12),
        Container(
            width: 36,
            height: 4,
            decoration: BoxDecoration(
                color: Colors.grey.shade400,
                borderRadius: BorderRadius.circular(2))),
        const SizedBox(height: 12),
        Text(l10n.selectLanguage,
            style: TextStyle(
                color: textColor, fontWeight: FontWeight.w700, fontSize: 16)),
        const SizedBox(height: 8),
        SizedBox(
          height: 340,
          child: ListView(
            children: languages.map((lang) {
              const flagMap = {
                'System Default': '🌐',
                'Spanish':    '🇺🇸',
                'English':    '🇬🇧',
                'French':     '🇫🇷',
                'German':     '🇩🇪',
                'Italian':    '🇮🇹',
                'Portuguese': '🇵🇹',
                'Russian':    '🇷🇺',
                'Chinese':    '🇨🇳',
                'Japanese':   '🇯🇵',
                'Korean':     '🇰🇷',
                'Arabic':     '🇸🇦',
                'Hindi':      '🇮🇳',
                'Bengali':    '🇧🇩',
                'Turkish':    '🇹🇷',
                'Dutch':      '🇳🇱',
                'Polish':     '🇵🇱',
                'Swedish':    '🇸🇪',
                'Greek':      '🇬🇷',
                'Thai':       '🇹🇭',
                'Vietnamese': '🇻🇳',
              };
              final isSystem = lang == 'System Default';
              final displayName = isSystem ? l10n.systemDefault : lang;
              final isSelected = lang == selected;
              final flag = flagMap[lang] ?? '🌐';

              return ListTile(
                leading: Container(
                  width: 36,
                  height: 36,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: isSelected
                        ? const Color(0xFF00B4D8).withValues(alpha: 0.15)
                        : Colors.transparent,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    flag,
                    style: const TextStyle(fontSize: 22),
                  ),
                ),
                title: Text(
                  displayName,
                  style: TextStyle(
                    color: textColor,
                    fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                  ),
                ),
                trailing: isSelected
                    ? const Icon(Icons.check_circle_rounded,
                        color: Color(0xFF00B4D8))
                    : null,
                onTap: () {
                  onChanged(lang);
                  Navigator.pop(context);
                },
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}

class _WidgetsTile extends ConsumerWidget {
  final Color cardColor;
  final Color textColor;
  final Color subtextColor;

  const _WidgetsTile({
    required this.cardColor,
    required this.textColor,
    required this.subtextColor,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = getIt<SettingsRepository>();
    final visible = settings.visibleWidgets;
    final l10n = AppLocalizations.of(context);

    return Container(
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 8,
          ),
        ],
      ),
      child: Column(
        children: [
          ...WeatherWidget.values.map((w) {
            final isVisible = visible.contains(w);
            return SwitchListTile.adaptive(
              value: isVisible,
              onChanged: (v) {
                if (v) {
                  settings.restoreWidgets();
                } else {
                  settings.removeWidget(w);
                }
                (context as Element).markNeedsBuild();
              },
              title: Text(_widgetLabel(w, l10n),
                  style: TextStyle(color: textColor, fontSize: 14)),
              activeTrackColor: const Color(0xFF00B4D8),
            );
          }),
        ],
      ),
    );
  }

  String _widgetLabel(WeatherWidget w, AppLocalizations l10n) {
    return switch (w) {
      WeatherWidget.hourly => l10n.hourlyForecastTitle,
      WeatherWidget.forecast7day => l10n.sevenDayForecast,
      WeatherWidget.aqi => l10n.healthEnvironment,
      WeatherWidget.humidity => l10n.humidity,
      WeatherWidget.pressure => l10n.pressure,
      WeatherWidget.uvIndex => l10n.uvIndex,
      WeatherWidget.wind => l10n.wind,
      WeatherWidget.sportAdvice => l10n.sportOutdoor,
      WeatherWidget.visibility => l10n.visibility,
      WeatherWidget.sunset => l10n.solarCycle,
      WeatherWidget.pollen => l10n.pollenAllergy,
      WeatherWidget.clothing => l10n.clothingSuggestion,
    };
  }
}

class _AboutTile extends StatelessWidget {
  final Color cardColor;
  final Color textColor;
  final Color subtextColor;
  const _AboutTile(
      {required this.cardColor,
      required this.textColor,
      required this.subtextColor});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Container(
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 8,
          ),
        ],
      ),
      child: Column(
        children: [
          ListTile(
            leading: ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Image.asset(
                'assets/images/icon.png',
                width: 40,
                height: 40,
                fit: BoxFit.cover,
              ),
            ),
            title: Text(l10n.appTitle,
                style:
                    TextStyle(color: textColor, fontWeight: FontWeight.w600)),
            subtitle: Text('${l10n.version} 1.0.0 · Flutter Multiplatform',
                style: TextStyle(color: subtextColor, fontSize: 12)),
          ),
          ListTile(
            leading: const SizedBox(width: 40),
            title: Text('Data powered by Open-Meteo',
                style: TextStyle(color: subtextColor, fontSize: 12)),
            subtitle: Text('Free & open-source weather API',
                style: TextStyle(
                    color: subtextColor.withValues(alpha: 0.6), fontSize: 11)),
          ),
        ],
      ),
    );
  }
}
