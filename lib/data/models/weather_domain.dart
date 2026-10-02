// lib/data/models/weather_domain.dart
class WeatherDomain {
  final CurrentWeatherDomain current;
  final List<HourlyWeatherDomain> hourly;
  final List<DailyWeatherDomain> daily;
  final String source;
  final int utcOffsetSeconds;

  const WeatherDomain({
    required this.current,
    required this.hourly,
    required this.daily,
    required this.source,
    this.utcOffsetSeconds = 0,
  });
}

class CurrentWeatherDomain {
  final double temperature;
  final double? feelsLike;
  final int? humidity;
  final double windSpeed;
  final int windDirection;
  final double? windGusts;
  final double pressure;
  final int weatherCode;
  final int isDay;
  final double? precipitation;
  final int? cloudCover;
  final double? visibility;
  final String time;

  const CurrentWeatherDomain({
    required this.temperature,
    this.feelsLike,
    this.humidity,
    required this.windSpeed,
    this.windDirection = 0,
    this.windGusts,
    this.pressure = 1013.2,
    required this.weatherCode,
    required this.isDay,
    this.precipitation,
    this.cloudCover,
    this.visibility,
    required this.time,
  });
}

class HourlyWeatherDomain {
  final String time;
  final double temperature;
  final int weatherCode;
  final int precipitationProbability;
  final int? humidity;
  final double windSpeed;
  final int windDirection;
  final double? windGusts;
  final double pressure;
  final double visibility;
  final int isDay;
  final int? cloudCover;

  const HourlyWeatherDomain({
    required this.time,
    required this.temperature,
    required this.weatherCode,
    required this.precipitationProbability,
    this.humidity,
    this.windSpeed = 0.0,
    this.windDirection = 0,
    this.windGusts,
    this.pressure = 1013.2,
    this.visibility = 10000.0,
    this.isDay = 1,
    this.cloudCover,
  });
}

class DailyWeatherDomain {
  final String time;
  final double maxTemp;
  final double minTemp;
  final int weatherCode;
  final String? sunrise;
  final String? sunset;
  final String? moonrise;
  final String? moonset;
  final double uvIndex;
  final int precipitationProbability;
  final double precipitationSum;
  final double windSpeedMax;
  final double apparentTempMax;
  final double moonPhase;

  const DailyWeatherDomain({
    required this.time,
    required this.maxTemp,
    required this.minTemp,
    required this.weatherCode,
    this.sunrise,
    this.sunset,
    this.moonrise,
    this.moonset,
    this.uvIndex = 0.0,
    this.precipitationProbability = 0,
    this.precipitationSum = 0.0,
    this.windSpeedMax = 0.0,
    this.apparentTempMax = 0.0,
    this.moonPhase = 0.0,
  });
}
