// lib/core/di/injection.dart
import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:usaweather/data/api/open_meteo_api.dart';
import 'package:usaweather/data/repositories/weather_repository.dart';
import 'package:usaweather/data/repositories/location_repository.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';
import 'package:usaweather/core/services/ad_service.dart';

final getIt = GetIt.instance;

Future<void> configureDependencies() async {
  // SharedPreferences
  final prefs = await SharedPreferences.getInstance();
  getIt.registerSingleton<SharedPreferences>(prefs);

  // Dio HTTP client
  final dio = Dio(BaseOptions(
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 15),
    headers: {'Accept': 'application/json'},
  ));
  dio.interceptors.add(LogInterceptor(responseBody: false, requestBody: false));
  getIt.registerSingleton<Dio>(dio);

  // API Client
  getIt.registerSingleton<OpenMeteoApiClient>(
    OpenMeteoApiClient(getIt<Dio>()),
  );

  // Repositories
  getIt.registerSingleton<WeatherRepository>(
    WeatherRepository(getIt<OpenMeteoApiClient>()),
  );
  getIt.registerSingleton<LocationRepository>(
    LocationRepository(getIt<SharedPreferences>()),
  );
  getIt.registerSingleton<SettingsRepository>(
    SettingsRepository(getIt<SharedPreferences>()),
  );

  // Services
  getIt.registerSingleton<AdService>(AdService.instance);
}
