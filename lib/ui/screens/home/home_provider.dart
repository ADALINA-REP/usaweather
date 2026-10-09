import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:usaweather/core/constants/api_constants.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/data/models/location_data.dart';
import 'package:usaweather/data/repositories/weather_repository.dart';
import 'package:usaweather/data/repositories/location_repository.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';

// ─── State ────────────────────────────────────────────────────────────────────
sealed class WeatherUiState {}

class WeatherLoading extends WeatherUiState {}

class WeatherSuccess extends WeatherUiState {
  final String cityName;
  final WeatherDomain weather;
  final double latitude;
  final double longitude;
  final AirQualityData? airQuality;
  final bool isFavorite;
  final List<LocationData> favorites;

  WeatherSuccess({
    required this.cityName,
    required this.weather,
    required this.latitude,
    required this.longitude,
    this.airQuality,
    this.isFavorite = false,
    this.favorites = const [],
  });

  WeatherSuccess copyWith({
    String? cityName,
    WeatherDomain? weather,
    double? latitude,
    double? longitude,
    AirQualityData? airQuality,
    bool? isFavorite,
    List<LocationData>? favorites,
  }) {
    return WeatherSuccess(
      cityName: cityName ?? this.cityName,
      weather: weather ?? this.weather,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      airQuality: airQuality ?? this.airQuality,
      isFavorite: isFavorite ?? this.isFavorite,
      favorites: favorites ?? this.favorites,
    );
  }
}

class WeatherError extends WeatherUiState {
  final String message;
  WeatherError(this.message);
}

// ─── Notifier ─────────────────────────────────────────────────────────────────
class HomeNotifier extends Notifier<WeatherUiState> {
  WeatherRepository get _weather => getIt<WeatherRepository>();
  LocationRepository get _location => getIt<LocationRepository>();
  SettingsRepository get _settings => getIt<SettingsRepository>();

  @override
  WeatherUiState build() {
    Future.microtask(() => _initialize());
    return WeatherLoading();
  }

  void _initialize() {
    if (_location.shouldFollowGps) {
      requestLocationAndLoad();
    } else {
      final last = _location.getLastLocation();
      if (last != null) {
        _loadFromCoords(last.latitude, last.longitude, last.name);
      } else {
        loadWeatherForCity(ApiConstants.defaultCity);
      }
    }
  }

  Future<void> requestLocationAndLoad() async {
    state = WeatherLoading();
    try {
      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.deniedForever ||
          permission == LocationPermission.denied) {
        _loadFallback();
        return;
      }

      final pos = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      ).timeout(const Duration(seconds: 12), onTimeout: () async {
        final last = await Geolocator.getLastKnownPosition();
        return last ?? Position(
          latitude: ApiConstants.defaultLat,
          longitude: ApiConstants.defaultLon,
          timestamp: DateTime.now(),
          accuracy: 0,
          altitude: 0,
          heading: 0,
          speed: 0,
          speedAccuracy: 0,
          altitudeAccuracy: 0,
          headingAccuracy: 0,
        );
      });

      await _loadFromCoords(pos.latitude, pos.longitude);
    } catch (e) {
      _loadFallback();
    }
  }

  Future<void> loadWeatherForCity(String cityName) async {
    state = WeatherLoading();
    final langCode = _settings.getSelectedLanguageCode();
    final results = await _weather.searchCity(cityName, language: langCode);
    if (results.isEmpty) {
      state = WeatherError('City not found: $cityName');
      return;
    }
    final city = results.first;
    _location.shouldFollowGps = false;
    _location.saveLocation(city.name, city.latitude, city.longitude);
    await _loadFromCoords(city.latitude, city.longitude, city.name);
  }

  Future<void> selectCity(LocationData loc) async {
    state = WeatherLoading();
    _location.shouldFollowGps = false;
    _location.saveLocation(loc.name, loc.latitude, loc.longitude);
    await _loadFromCoords(loc.latitude, loc.longitude, loc.name);
  }

  Future<void> _loadFromCoords(double lat, double lon, [String? storedName]) async {
    final weatherResult = await _weather.getWeatherData(lat, lon);
    final aqResult = await _weather.getAirQuality(lat, lon);

    if (weatherResult is WeatherResultError) {
      state = WeatherError((weatherResult as WeatherResultError<WeatherDomain>).message);
      return;
    }

    final data = (weatherResult as WeatherResultSuccess<WeatherDomain>).data;
    final langCode = _settings.getSelectedLanguageCode();
    final cityName = storedName ??
        await _weather.reverseGeocode(lat, lon, language: langCode) ??
        'Current Location';

    final favs = _location.getFavorites();
    state = WeatherSuccess(
      cityName: cityName,
      weather: data,
      latitude: lat,
      longitude: lon,
      airQuality: aqResult,
      isFavorite: _location.isFavorite(cityName, lat, lon),
      favorites: favs,
    );
  }

  void _loadFallback() {
    final last = _location.getLastLocation();
    if (last != null) {
      _loadFromCoords(last.latitude, last.longitude, last.name);
    } else {
      loadWeatherForCity(ApiConstants.defaultCity);
    }
  }

  Future<void> refresh() async {
    final s = state;
    if (s is! WeatherSuccess) return;
    await _loadFromCoords(s.latitude, s.longitude, s.cityName);
  }

  void toggleFavorite() {
    final s = state;
    if (s is! WeatherSuccess) return;
    _location.toggleFavorite(s.cityName, s.latitude, s.longitude);
    final favs = _location.getFavorites();
    state = s.copyWith(
      isFavorite: _location.isFavorite(s.cityName, s.latitude, s.longitude),
      favorites: favs,
    );
  }

  void followCurrentLocation() {
    _location.shouldFollowGps = true;
    requestLocationAndLoad();
  }
}

final homeProvider = NotifierProvider<HomeNotifier, WeatherUiState>(HomeNotifier.new);

// Settings providers
final settingsProvider = Provider<SettingsRepository>((ref) => getIt<SettingsRepository>());
final isCelsiusProvider = StateProvider<bool>((ref) => getIt<SettingsRepository>().isCelsius);
final isDarkModeProvider = StateProvider<bool>((ref) => getIt<SettingsRepository>().isDarkMode);
final isMbProvider = StateProvider<bool>((ref) => getIt<SettingsRepository>().isMb);
final languageProvider = StateProvider<String>((ref) => getIt<SettingsRepository>().selectedLanguage);

final appLocaleProvider = Provider<Locale>((ref) {
  // Watch languageProvider so changes trigger immediate app-wide locale update
  ref.watch(languageProvider);
  final repo = ref.watch(settingsProvider);
  String? deviceLang;
  try {
    deviceLang = WidgetsBinding.instance.platformDispatcher.locale.languageCode;
  } catch (_) {
    deviceLang = 'en';
  }
  final code = repo.resolveEffectiveLanguageCode(deviceLang);
  return Locale(code);
});

