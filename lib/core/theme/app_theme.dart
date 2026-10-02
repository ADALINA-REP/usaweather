// lib/core/theme/app_theme.dart
import 'package:flutter/material.dart';

class AppColors {
  // Primary gradient (deep blue-violet)
  static const Color primary = Color(0xFF1A237E);
  static const Color primaryLight = Color(0xFF3949AB);
  static const Color accent = Color(0xFF00B4D8);
  static const Color accentLight = Color(0xFF90E0EF);

  // Background (dark)
  static const Color bgDark = Color(0xFF0A0E2E);
  static const Color bgCard = Color(0xFF131738);
  static const Color bgCardLight = Color(0xFF1C2048);
  static const Color surface = Color(0xFF1E2254);

  // Background (light)
  static const Color bgLight = Color(0xFFF0F4FF);
  static const Color bgCardWhite = Color(0xFFFFFFFF);
  static const Color surfaceLight = Color(0xFFE8EEFF);

  // Text
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFFB0BEC5);
  static const Color textPrimaryDark = Color(0xFF0A0E2E);
  static const Color textSecondaryDark = Color(0xFF546E7A);

  // Semantic
  static const Color success = Color(0xFF00C853);
  static const Color warning = Color(0xFFFFAB00);
  static const Color error = Color(0xFFFF5252);
  static const Color info = Color(0xFF00B4D8);

  // AQI Colors
  static const Color aqiGood = Color(0xFF00C853);
  static const Color aqiFair = Color(0xFF69F0AE);
  static const Color aqiModerate = Color(0xFFFFD740);
  static const Color aqiPoor = Color(0xFFFF6D00);
  static const Color aqiVeryPoor = Color(0xFFD50000);
}

class AppTheme {
  static ThemeData darkTheme = ThemeData(
    brightness: Brightness.dark,
    colorScheme: const ColorScheme.dark(
      primary: AppColors.accent,
      secondary: AppColors.accentLight,
      surface: AppColors.bgCard,
      error: AppColors.error,
    ),
    scaffoldBackgroundColor: AppColors.bgDark,
    cardTheme: CardThemeData(
      color: AppColors.bgCard,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      elevation: 0,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
      iconTheme: IconThemeData(color: AppColors.textPrimary),
    ),
    iconTheme: const IconThemeData(color: AppColors.textSecondary),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: AppColors.bgCard,
      selectedItemColor: AppColors.accent,
      unselectedItemColor: AppColors.textSecondary,
      elevation: 0,
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: AppColors.bgCardLight,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
      hintStyle: const TextStyle(color: AppColors.textSecondary),
    ),
    useMaterial3: true,
  );

  static ThemeData lightTheme = ThemeData(
    brightness: Brightness.light,
    colorScheme: const ColorScheme.light(
      primary: AppColors.primaryLight,
      secondary: AppColors.accent,
      surface: AppColors.bgCardWhite,
      error: AppColors.error,
    ),
    scaffoldBackgroundColor: AppColors.bgLight,
    cardTheme: CardThemeData(
      color: AppColors.bgCardWhite,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      elevation: 0,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
      iconTheme: IconThemeData(color: AppColors.textPrimaryDark),
    ),
    iconTheme: const IconThemeData(color: AppColors.textSecondaryDark),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: AppColors.surfaceLight,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide.none,
      ),
    ),
    useMaterial3: true,
  );
}
