// lib/app.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/core/router/app_router.dart';
import 'package:usaweather/core/theme/app_theme.dart';
import 'package:usaweather/core/services/ad_service.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';

class WeatherApp extends ConsumerStatefulWidget {
  const WeatherApp({super.key});

  @override
  ConsumerState<WeatherApp> createState() => _WeatherAppState();
}

class _WeatherAppState extends ConsumerState<WeatherApp>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      AdService.instance.showAppOpenAdIfAvailable();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = ref.watch(isDarkModeProvider);
    final locale = ref.watch(appLocaleProvider);

    return MaterialApp.router(
      title: 'USA Weather',
      onGenerateTitle: (context) => AppLocalizations.of(context).appTitle,
      locale: locale,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: isDark ? ThemeMode.dark : ThemeMode.light,
      routerConfig: appRouter,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('en'),
        Locale('es'),
        Locale('fr'),
        Locale('de'),
        Locale('it'),
        Locale('pt'),
        Locale('ar'),
        Locale('zh'),
        Locale('ja'),
        Locale('ko'),
        Locale('ru'),
        Locale('hi'),
        Locale('tr'),
        Locale('nl'),
        Locale('pl'),
        Locale('sv'),
        Locale('el'),
        Locale('th'),
        Locale('vi'),
        Locale('bn'),
      ],
    );
  }
}
