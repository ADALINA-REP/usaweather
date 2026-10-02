// lib/data/models/location_data.dart
import 'dart:convert';

class LocationData {
  final String name;
  final double latitude;
  final double longitude;
  final String? country;
  final String? admin1;

  const LocationData({
    required this.name,
    required this.latitude,
    required this.longitude,
    this.country,
    this.admin1,
  });

  Map<String, dynamic> toJson() => {
        'name': name,
        'latitude': latitude,
        'longitude': longitude,
        if (country != null) 'country': country,
        if (admin1 != null) 'admin1': admin1,
      };

  factory LocationData.fromJson(Map<String, dynamic> json) => LocationData(
        name: json['name'] as String,
        latitude: (json['latitude'] as num).toDouble(),
        longitude: (json['longitude'] as num).toDouble(),
        country: json['country'] as String?,
        admin1: json['admin1'] as String?,
      );

  String toJsonString() => jsonEncode(toJson());
  factory LocationData.fromJsonString(String s) =>
      LocationData.fromJson(jsonDecode(s) as Map<String, dynamic>);
}

class AirQualityData {
  final double? europeanAqi;
  final double? usAqi;
  final double? pm25;
  final double? pm10;
  final double? ozone;
  final double? no2;
  final double? so2;
  final double? co;

  const AirQualityData({
    this.europeanAqi,
    this.usAqi,
    this.pm25,
    this.pm10,
    this.ozone,
    this.no2,
    this.so2,
    this.co,
  });
}

enum WeatherWidget {
  hourly('hourly'),
  forecast7day('forecast_7day'),
  aqi('aqi'),
  humidity('humidity'),
  pressure('pressure'),
  uvIndex('uv_index'),
  wind('wind'),
  sportAdvice('sport_advice'),
  visibility('visibility'),
  sunset('sunset'),
  pollen('pollen'),
  clothing('clothing');

  final String id;
  const WeatherWidget(this.id);
}

enum TemperatureUnit { celsius, fahrenheit }
enum PressureUnit { mb, hpa }
