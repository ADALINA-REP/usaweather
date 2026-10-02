// lib/data/repositories/settings_repository.dart
import 'package:shared_preferences/shared_preferences.dart';
import 'package:usaweather/data/models/location_data.dart';

class SettingsRepository {
  final SharedPreferences _prefs;

  SettingsRepository(this._prefs);

  // --- Temperature Unit ---
  bool get isCelsius => _prefs.getBool('use_celsius') ?? false;
  set isCelsius(bool v) => _prefs.setBool('use_celsius', v);

  // --- Dark Mode ---
  bool get isDarkMode => _prefs.getBool('dark_mode') ?? true;
  set isDarkMode(bool v) => _prefs.setBool('dark_mode', v);

  // --- Pressure Unit ---
  bool get isMb => _prefs.getBool('use_mb') ?? true;
  set isMb(bool v) => _prefs.setBool('use_mb', v);

  // --- Language ---
  // If not manually chosen, defaults to 'System Default' which auto-detects device language
  String get selectedLanguage =>
      _prefs.getString('selected_language') ?? 'System Default';
  set selectedLanguage(String v) => _prefs.setString('selected_language', v);

  bool get isSystemLanguage =>
      selectedLanguage == 'System Default' || selectedLanguage == 'system';

  static const List<String> supportedLanguageCodes = [
    'es',
    'en',
    'fr',
    'de',
    'it',
    'pt',
    'ru',
    'zh',
    'ja',
    'ko',
    'ar',
    'hi',
    'bn',
    'tr',
    'nl',
    'pl',
    'sv',
    'el',
    'th',
    'vi'
  ];

  static const Map<String, String> languageMap = {
    'System Default': 'system',
    'Spanish': 'es',
    'English': 'en',
    'French': 'fr',
    'German': 'de',
    'Italian': 'it',
    'Portuguese': 'pt',
    'Russian': 'ru',
    'Chinese': 'zh',
    'Japanese': 'ja',
    'Korean': 'ko',
    'Arabic': 'ar',
    'Hindi': 'hi',
    'Bengali': 'bn',
    'Turkish': 'tr',
    'Dutch': 'nl',
    'Polish': 'pl',
    'Swedish': 'sv',
    'Greek': 'el',
    'Thai': 'th',
    'Vietnamese': 'vi',
  };

  /// Resolves the effective language code according to the strict priority:
  /// 1. User's manually selected language
  /// 2. Device system language (if supported)
  /// 3. Application default fallback ('es')
  String resolveEffectiveLanguageCode([String? deviceLanguageCode]) {
    if (!isSystemLanguage) {
      return languageMap[selectedLanguage] ?? 'es';
    }
    if (deviceLanguageCode != null &&
        supportedLanguageCodes.contains(deviceLanguageCode.toLowerCase())) {
      return deviceLanguageCode.toLowerCase();
    }
    return 'en';
  }

  String getLanguageCode(String name) => languageMap[name] ?? 'en';
  String getSelectedLanguageCode([String? deviceLanguageCode]) =>
      resolveEffectiveLanguageCode(deviceLanguageCode);

  List<String> getLanguages() => languageMap.keys.toList();

  // --- Visible Widgets ---
  Set<WeatherWidget> get visibleWidgets {
    final saved = _prefs.getStringList('visible_widgets');
    if (saved == null) return WeatherWidget.values.toSet();
    return saved
        .map((id) => WeatherWidget.values.firstWhere(
              (w) => w.id == id,
              orElse: () => WeatherWidget.hourly,
            ))
        .toSet();
  }

  void removeWidget(WeatherWidget widget) {
    final current = visibleWidgets.toSet();
    current.remove(widget);
    _prefs.setStringList('visible_widgets', current.map((w) => w.id).toList());
  }

  void restoreWidgets() {
    _prefs.setStringList(
        'visible_widgets', WeatherWidget.values.map((w) => w.id).toList());
  }

  // --- General Alerts ---
  bool get generalAlerts => _prefs.getBool('general_alerts') ?? true;
  set generalAlerts(bool v) => _prefs.setBool('general_alerts', v);

  bool get rainAlerts => _prefs.getBool('rain_alerts') ?? true;
  set rainAlerts(bool v) => _prefs.setBool('rain_alerts', v);

  bool get tempAlerts => _prefs.getBool('temp_alerts') ?? true;
  set tempAlerts(bool v) => _prefs.setBool('temp_alerts', v);
}
