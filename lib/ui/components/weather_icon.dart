// lib/ui/components/weather_icon.dart
import 'package:flutter/material.dart';
import 'package:usaweather/core/localization/app_localizations.dart';

class WeatherIcon extends StatelessWidget {
  final int weatherCode;
  final int isDay;
  final double size;
  final bool animated;

  const WeatherIcon({
    super.key,
    required this.weatherCode,
    required this.isDay,
    this.size = 64,
    this.animated = false,
  });

  @override
  Widget build(BuildContext context) {
    final emoji = _getEmoji();
    return Text(
      emoji,
      style: TextStyle(fontSize: size * 0.9),
      textAlign: TextAlign.center,
    );
  }

  String _getEmoji() {
    final night = isDay == 0;
    return switch (weatherCode) {
      0 => night ? '🌙' : '☀️',
      1 => night ? '🌙' : '🌤️',
      2 => '⛅',
      3 => '☁️',
      45 || 48 => '🌫️',
      51 || 53 => '🌦️',
      55 => '🌧️',
      56 || 57 => '🌨️',
      61 || 63 => '🌧️',
      65 => '🌊',
      66 || 67 => '🌨️',
      71 || 73 => '❄️',
      75 => '🌨️',
      77 => '🌨️',
      80 || 81 => '🌦️',
      82 => '⛈️',
      85 || 86 => '🌨️',
      95 => '⛈️',
      96 || 99 => '⛈️',
      _ => night ? '🌙' : '☀️',
    };
  }

  static String getLocalizedDescription(int code, AppLocalizations l10n) {
    return l10n.getWeatherCondition(code);
  }

  static String getDescription(int code) {
    return switch (code) {
      0 => 'Clear Sky',
      1 => 'Mainly Clear',
      2 => 'Partly Cloudy',
      3 => 'Overcast',
      45 || 48 => 'Foggy',
      51 => 'Light Drizzle',
      53 => 'Moderate Drizzle',
      55 => 'Dense Drizzle',
      56 || 57 => 'Freezing Drizzle',
      61 => 'Light Rain',
      63 => 'Moderate Rain',
      65 => 'Heavy Rain',
      66 || 67 => 'Freezing Rain',
      71 => 'Light Snow',
      73 => 'Moderate Snow',
      75 => 'Heavy Snow',
      77 => 'Snow Grains',
      80 => 'Light Showers',
      81 => 'Moderate Showers',
      82 => 'Heavy Showers',
      85 || 86 => 'Snow Showers',
      95 => 'Thunderstorm',
      96 || 99 => 'Thunderstorm with Hail',
      _ => 'Unknown',
    };
  }

  static Color getConditionColor(int code) {
    return switch (code) {
      0 || 1 => const Color(0xFFFFB300),
      2 || 3 => const Color(0xFF78909C),
      45 || 48 => const Color(0xFFB0BEC5),
      51 || 53 || 55 || 56 || 57 => const Color(0xFF42A5F5),
      61 || 63 || 65 || 66 || 67 || 80 || 81 || 82 => const Color(0xFF1E88E5),
      71 || 73 || 75 || 77 || 85 || 86 => const Color(0xFFB3E5FC),
      95 || 96 || 99 => const Color(0xFF5C6BC0),
      _ => const Color(0xFF42A5F5),
    };
  }
}
