// lib/data/api/open_meteo_api.dart
import 'package:dio/dio.dart';
import 'package:usaweather/core/constants/api_constants.dart';

class OpenMeteoApiClient {
  final Dio _dio;

  OpenMeteoApiClient(this._dio);

  Future<Map<String, dynamic>> getWeather({
    required double lat,
    required double lon,
    String timezone = 'auto',
  }) async {
    final response = await _dio.get(
      '${ApiConstants.openMeteoBase}/forecast',
      queryParameters: {
        'latitude': lat,
        'longitude': lon,
        'current': ApiConstants.currentParams,
        'hourly': ApiConstants.hourlyParams,
        'daily': ApiConstants.dailyParams,
        'timezone': timezone,
      },
    );
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> searchCity({
    required String name,
    String language = 'en',
    int count = 10,
  }) async {
    final response = await _dio.get(
      '${ApiConstants.geocodingBase}/search',
      queryParameters: {
        'name': name,
        'count': count,
        'language': language,
        'format': 'json',
      },
    );
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> getAirQuality({
    required double lat,
    required double lon,
  }) async {
    final response = await _dio.get(
      '${ApiConstants.airQualityBase}/air-quality',
      queryParameters: {
        'latitude': lat,
        'longitude': lon,
        'current': ApiConstants.airQualityParams,
        'timezone': 'auto',
      },
    );
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> getRainViewerMaps() async {
    final response = await _dio.get(ApiConstants.rainViewerBase);
    return response.data as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>?> reverseGeocode({
    required double lat,
    required double lon,
    String language = 'en',
  }) async {
    try {
      final response = await _dio.get(
        '${ApiConstants.nominatimBase}/reverse',
        queryParameters: {
          'lat': lat,
          'lon': lon,
          'format': 'json',
          'accept-language': language,
        },
        options: Options(headers: {'User-Agent': 'USA Weather/1.0'}),
      );
      return response.data as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }
}
