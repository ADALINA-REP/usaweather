import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:usaweather/app.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/services/ad_service.dart';
import 'package:usaweather/core/services/notification_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting();
  await configureDependencies();

  // Initialize AdMob & Local Notifications in background without blocking app startup
  AdService.instance.initialize();
  NotificationService().initialize();

  runApp(
    const ProviderScope(
      child: WeatherApp(),
    ),
  );
}
