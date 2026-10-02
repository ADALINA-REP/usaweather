// lib/data/repositories/weather_repository.dart
import 'package:usaweather/data/api/open_meteo_api.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/data/models/location_data.dart';

// ─── Result type (generic) ────────────────────────────────────────────────────
sealed class WeatherResult<T> {
  const WeatherResult();
}

final class WeatherResultSuccess<T> extends WeatherResult<T> {
  final T data;
  const WeatherResultSuccess(this.data);
}

final class WeatherResultError<T> extends WeatherResult<T> {
  final String message;
  const WeatherResultError(this.message);
}

// ─── Repository ───────────────────────────────────────────────────────────────
class WeatherRepository {
  final OpenMeteoApiClient _api;

  WeatherRepository(this._api);

  Future<WeatherResult<WeatherDomain>> getWeatherData(
      double lat, double lon) async {
    try {
      final raw = await _api.getWeather(lat: lat, lon: lon);
      final domain = _normalize(raw);
      return WeatherResultSuccess(domain);
    } catch (e) {
      return WeatherResultError('Weather API error: $e');
    }
  }

  Future<AirQualityData?> getAirQuality(double lat, double lon) async {
    try {
      final raw = await _api.getAirQuality(lat: lat, lon: lon);
      final current = raw['current'] as Map<String, dynamic>?;
      if (current == null) return null;
      return AirQualityData(
        europeanAqi: (current['european_aqi'] as num?)?.toDouble(),
        usAqi: (current['us_aqi'] as num?)?.toDouble(),
        pm25: (current['pm2_5'] as num?)?.toDouble(),
        pm10: (current['pm10'] as num?)?.toDouble(),
        ozone: (current['ozone'] as num?)?.toDouble(),
        no2: (current['nitrogen_dioxide'] as num?)?.toDouble(),
        so2: (current['sulphur_dioxide'] as num?)?.toDouble(),
        co: (current['carbon_monoxide'] as num?)?.toDouble(),
      );
    } catch (_) {
      return null;
    }
  }

  Future<List<LocationData>> searchCity(String name,
      {String language = 'en'}) async {
    try {
      final raw = await _api.searchCity(name: name, language: language);
      final results = raw['results'] as List<dynamic>?;
      if (results == null) return [];
      return results.map((r) {
        final m = r as Map<String, dynamic>;
        return LocationData(
          name: m['name'] as String,
          latitude: (m['latitude'] as num).toDouble(),
          longitude: (m['longitude'] as num).toDouble(),
          country: m['country'] as String?,
          admin1: m['admin1'] as String?,
        );
      }).toList();
    } catch (_) {
      return [];
    }
  }

  Future<String?> reverseGeocode(double lat, double lon,
      {String language = 'en'}) async {
    try {
      final raw =
          await _api.reverseGeocode(lat: lat, lon: lon, language: language);
      if (raw == null) return null;
      final address = raw['address'] as Map<String, dynamic>?;
      if (address == null) return null;
      return address['city'] as String? ??
          address['town'] as String? ??
          address['village'] as String? ??
          address['municipality'] as String? ??
          address['county'] as String?;
    } catch (_) {
      return null;
    }
  }

  Future<Map<String, dynamic>?> getRainViewerData() async {
    try {
      return await _api.getRainViewerMaps();
    } catch (_) {
      return null;
    }
  }

  WeatherDomain _normalize(Map<String, dynamic> raw) {
    final currentRaw = raw['current'] as Map<String, dynamic>;
    final hourlyRaw = raw['hourly'] as Map<String, dynamic>;
    final dailyRaw = raw['daily'] as Map<String, dynamic>;
    final utcOffset = (raw['utc_offset_seconds'] as num?)?.toInt() ?? 0;

    final current = CurrentWeatherDomain(
      temperature: (currentRaw['temperature_2m'] as num).toDouble(),
      feelsLike: (currentRaw['apparent_temperature'] as num?)?.toDouble(),
      humidity: (currentRaw['relative_humidity_2m'] as num?)?.toInt(),
      windSpeed: (currentRaw['wind_speed_10m'] as num).toDouble(),
      windDirection: (currentRaw['wind_direction_10m'] as num?)?.toInt() ?? 0,
      windGusts: (currentRaw['wind_gusts_10m'] as num?)?.toDouble(),
      pressure: (currentRaw['surface_pressure'] as num?)?.toDouble() ?? 1013.2,
      weatherCode: (currentRaw['weather_code'] as num).toInt(),
      isDay: (currentRaw['is_day'] as num).toInt(),
      precipitation: (currentRaw['precipitation'] as num?)?.toDouble(),
      cloudCover: (currentRaw['cloud_cover'] as num?)?.toInt(),
      visibility: (currentRaw['visibility'] as num?)?.toDouble(),
      time: currentRaw['time'] as String,
    );

    final hourlyTimes = hourlyRaw['time'] as List<dynamic>;
    final hourly = List.generate(hourlyTimes.length, (i) {
      num? _n(String k) =>
          (hourlyRaw[k] as List?)?.elementAtOrNull(i) as num?;
      return HourlyWeatherDomain(
        time: hourlyTimes[i] as String,
        temperature: _n('temperature_2m')?.toDouble() ?? 0.0,
        weatherCode: _n('weather_code')?.toInt() ?? 0,
        precipitationProbability:
            _n('precipitation_probability')?.toInt() ?? 0,
        humidity: _n('relative_humidity_2m')?.toInt(),
        windSpeed: _n('wind_speed_10m')?.toDouble() ?? 0.0,
        windDirection: _n('wind_direction_10m')?.toInt() ?? 0,
        windGusts: _n('wind_gusts_10m')?.toDouble(),
        pressure: _n('surface_pressure')?.toDouble() ?? 1013.2,
        visibility: _n('visibility')?.toDouble() ?? 10000.0,
        isDay: _n('is_day')?.toInt() ?? 1,
        cloudCover: _n('cloud_cover')?.toInt(),
      );
    });

    final dailyTimes = dailyRaw['time'] as List<dynamic>;
    final daily = List.generate(dailyTimes.length, (i) {
      num? _d(String k) =>
          (dailyRaw[k] as List?)?.elementAtOrNull(i) as num?;
      String? _s(String k) =>
          (dailyRaw[k] as List?)?.elementAtOrNull(i) as String?;
      return DailyWeatherDomain(
        time: dailyTimes[i] as String,
        maxTemp: _d('temperature_2m_max')?.toDouble() ?? 0.0,
        minTemp: _d('temperature_2m_min')?.toDouble() ?? 0.0,
        weatherCode: _d('weather_code')?.toInt() ?? 0,
        sunrise: _s('sunrise'),
        sunset: _s('sunset'),
        uvIndex: _d('uv_index_max')?.toDouble() ?? 0.0,
        precipitationProbability:
            _d('precipitation_probability_max')?.toInt() ?? 0,
        precipitationSum: _d('precipitation_sum')?.toDouble() ?? 0.0,
        windSpeedMax: _d('wind_speed_10m_max')?.toDouble() ?? 0.0,
        apparentTempMax: _d('apparent_temperature_max')?.toDouble() ?? 0.0,
        moonPhase: _calculateMoonPhase(dailyTimes[i] as String),
      );
    });

    return WeatherDomain(
      current: current,
      hourly: hourly,
      daily: daily,
      source: 'Open-Meteo',
      utcOffsetSeconds: utcOffset,
    );
  }

  double _calculateMoonPhase(String dateStr) {
    try {
      final date = DateTime.parse(dateStr);
      final knownNewMoon = DateTime(2000, 1, 6);
      final diff = date.difference(knownNewMoon).inDays;
      const cycle = 29.53058770576;
      return ((diff % cycle) / cycle).abs();
    } catch (_) {
      return 0.0;
    }
  }
}
