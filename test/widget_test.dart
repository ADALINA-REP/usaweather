import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:usaweather/data/models/location_data.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/ui/components/weather_icon.dart';
import 'package:usaweather/ui/screens/forecast_detail/daily_forecast_detail_screen.dart';
import 'package:usaweather/ui/components/weather_chart_painters.dart';
import 'package:go_router/go_router.dart';
import 'package:usaweather/core/router/app_router.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:usaweather/ui/screens/radar/radar_screen.dart';

void main() {
  setUpAll(() async {
    TestWidgetsFlutterBinding.ensureInitialized();
    await initializeDateFormatting();
  });

  group('WeatherDomain Model Tests', () {
    test('Can construct WeatherDomain and read properties', () {
      const current = CurrentWeatherDomain(
        temperature: 24.5,
        windSpeed: 12.0,
        weatherCode: 0,
        isDay: 1,
        time: '2026-09-02T12:00',
      );

      const domain = WeatherDomain(
        current: current,
        hourly: [],
        daily: [],
        source: 'Open-Meteo',
      );

      expect(domain.current.temperature, 24.5);
      expect(domain.current.weatherCode, 0);
      expect(domain.source, 'Open-Meteo');
    });

    test('LocationData JSON serialization and deserialization', () {
      const loc = LocationData(
        name: 'Madrid',
        latitude: 40.4168,
        longitude: -3.7038,
        country: 'Spain',
        admin1: 'Madrid',
      );

      final jsonStr = loc.toJsonString();
      final restored = LocationData.fromJsonString(jsonStr);

      expect(restored.name, 'Madrid');
      expect(restored.latitude, 40.4168);
      expect(restored.country, 'Spain');
    });

    test('WeatherIcon maps WMO codes correctly', () {
      expect(WeatherIcon.getDescription(0), 'Clear Sky');
      expect(WeatherIcon.getDescription(3), 'Overcast');
      expect(WeatherIcon.getDescription(95), 'Thunderstorm');
    });
  });

  group('Daily Forecast Selection Tests', () {
    // 7 Complete Days
    final full7DaysList = [
      const DailyWeatherDomain(
        time: '2026-09-01',
        maxTemp: 31.0,
        minTemp: 17.0,
        weatherCode: 0,
        precipitationProbability: 0,
        precipitationSum: 0.0,
        windSpeedMax: 12.0,
        apparentTempMax: 29.0,
        uvIndex: 7.2,
      ),
      const DailyWeatherDomain(
        time: '2026-09-02',
        maxTemp: 32.0,
        minTemp: 18.0,
        weatherCode: 1,
        precipitationProbability: 5,
        precipitationSum: 0.0,
        windSpeedMax: 14.0,
        apparentTempMax: 30.0,
        uvIndex: 7.5,
      ),
      const DailyWeatherDomain(
        time: '2026-09-03',
        maxTemp: 36.0,
        minTemp: 21.0,
        weatherCode: 2,
        precipitationProbability: 15,
        precipitationSum: 0.5,
        windSpeedMax: 18.0,
        apparentTempMax: 35.0,
        uvIndex: 8.0,
      ),
      const DailyWeatherDomain(
        time: '2026-09-04',
        maxTemp: 28.0,
        minTemp: 16.0,
        weatherCode: 61,
        precipitationProbability: 80,
        precipitationSum: 12.4,
        windSpeedMax: 25.0,
        apparentTempMax: 27.0,
        uvIndex: 4.1,
      ),
      const DailyWeatherDomain(
        time: '2026-09-05',
        maxTemp: 26.0,
        minTemp: 15.0,
        weatherCode: 63,
        precipitationProbability: 90,
        precipitationSum: 18.2,
        windSpeedMax: 28.0,
        apparentTempMax: 25.0,
        uvIndex: 3.5,
      ),
      const DailyWeatherDomain(
        time: '2026-09-06',
        maxTemp: 29.0,
        minTemp: 17.0,
        weatherCode: 3,
        precipitationProbability: 20,
        precipitationSum: 1.0,
        windSpeedMax: 16.0,
        apparentTempMax: 28.0,
        uvIndex: 6.0,
      ),
      const DailyWeatherDomain(
        time: '2026-09-07',
        maxTemp: 33.0,
        minTemp: 19.0,
        weatherCode: 0,
        precipitationProbability: 0,
        precipitationSum: 0.0,
        windSpeedMax: 15.0,
        apparentTempMax: 32.0,
        uvIndex: 7.8,
      ),
    ];

    final hourlyList = [
      const HourlyWeatherDomain(
        time: '2026-09-03T10:00',
        temperature: 28.0,
        weatherCode: 1,
        precipitationProbability: 10,
      ),
      const HourlyWeatherDomain(
        time: '2026-09-03T15:00',
        temperature: 36.0,
        weatherCode: 2,
        precipitationProbability: 15,
      ),
      const HourlyWeatherDomain(
        time: '2026-09-04T12:00',
        temperature: 22.0,
        weatherCode: 61,
        precipitationProbability: 80,
      ),
    ];

    testWidgets('DailyForecastDetailScreen displays exact selected day details',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          home: DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Salamanca',
              selectedDate: '2026-09-03',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Verifies city name
      expect(find.text('Salamanca'), findsOneWidget);

      // Verifies selected day 3 (2026-09-03) data: max 36°, min 21°
      expect(find.text('36°'), findsAtLeastNWidgets(1));
      expect(find.text('21°'), findsAtLeastNWidgets(1));

      // Verifies hourly forecasts for 2026-09-03 are displayed
      expect(find.text('10:00'), findsOneWidget);
      expect(find.text('15:00'), findsOneWidget);
    });

    testWidgets('UI Verification: Each day from Day 1 to Day 7 displays its exact respective data',
        (WidgetTester tester) async {
      // Loop across each of the 7 days and verify that instantiating with that day reflects its exact values
      for (int dayIndex = 0; dayIndex < full7DaysList.length; dayIndex++) {
        final expectedDay = full7DaysList[dayIndex];

        await tester.pumpWidget(
          MaterialApp(
            localizationsDelegates: const [
              AppLocalizations.delegate,
              GlobalMaterialLocalizations.delegate,
              GlobalWidgetsLocalizations.delegate,
              GlobalCupertinoLocalizations.delegate,
            ],
            supportedLocales: AppLocalizations.supportedLocales,
            home: DailyForecastDetailScreen(
              args: DailyForecastDetailArgs(
                cityName: 'Valencia',
                selectedDate: expectedDay.time,
                dailyForecasts: full7DaysList,
                hourlyForecasts: hourlyList,
                isCelsius: true,
              ),
            ),
          ),
        );
        await tester.pumpAndSettle();

        // City
        expect(find.text('Valencia'), findsOneWidget);

        // Temperatures for this specific day
        final expectedMaxStr = '${expectedDay.maxTemp.round()}°';
        final expectedMinStr = '${expectedDay.minTemp.round()}°';
        expect(find.text(expectedMaxStr), findsAtLeastNWidgets(1),
            reason: 'Day $dayIndex (${expectedDay.time}) must display max temp $expectedMaxStr');
        expect(find.text(expectedMinStr), findsAtLeastNWidgets(1),
            reason: 'Day $dayIndex (${expectedDay.time}) must display min temp $expectedMinStr');

        // Precipitation for this specific day (in Hero Card)
        final expectedPrecipStr = '${expectedDay.precipitationProbability}%';
        expect(find.text(expectedPrecipStr), findsAtLeastNWidgets(1),
            reason: 'Day $dayIndex (${expectedDay.time}) must display precipitation $expectedPrecipStr');
      }
    });

    testWidgets('Back navigation returns cleanly without mutating host state',
        (WidgetTester tester) async {
      final key = GlobalKey<NavigatorState>();

      await tester.pumpWidget(
        MaterialApp(
          navigatorKey: key,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          home: Scaffold(
            body: Center(
              child: ElevatedButton(
                onPressed: () {
                  key.currentState!.push(
                    MaterialPageRoute(
                      builder: (_) => DailyForecastDetailScreen(
                        args: DailyForecastDetailArgs(
                          cityName: 'Sevilla',
                          selectedDate: '2026-09-04',
                          dailyForecasts: full7DaysList,
                          hourlyForecasts: hourlyList,
                          isCelsius: true,
                        ),
                      ),
                    ),
                  );
                },
                child: const Text('Open Detail'),
              ),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Tap Open Detail
      await tester.tap(find.text('Open Detail'));
      await tester.pumpAndSettle();

      // In detail screen
      expect(find.text('Sevilla'), findsOneWidget);
      expect(find.text('28°'), findsAtLeastNWidgets(1));

      // Tap Back button
      await tester.tap(find.byIcon(Icons.arrow_back_ios_new_rounded));
      await tester.pumpAndSettle();

      // Back on root screen
      expect(find.text('Open Detail'), findsOneWidget);
    });

    testWidgets('Light Mode UI verification: DailyForecastDetailScreen renders with high-contrast light theme',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          theme: ThemeData.light(),
          locale: const Locale('es'),
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          home: DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Madrid',
              selectedDate: '2026-09-02',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Screen title and city
      expect(find.text('Madrid'), findsOneWidget);

      // Hero weather card exists and renders in light mode
      expect(find.text('Hoy'), findsOneWidget);
      expect(find.text('32°'), findsAtLeastNWidgets(1));
      expect(find.text('18°'), findsAtLeastNWidgets(1));
    });

    testWidgets('DailyForecastDetailScreen contains same zones as Home Screen: Health & Environment, Details Grid, Solar Cycle, Sport & Outdoor',
        (WidgetTester tester) async {
      final l10n = AppLocalizations(const Locale('es'));

      await tester.pumpWidget(
        MaterialApp(
          locale: const Locale('es'),
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          home: DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Salamanca',
              selectedDate: '2026-09-03',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Hourly Forecast section
      expect(find.text(l10n.hourlyForecastTitle), findsOneWidget);

      // Scroll to reveal Health & Environment and Details Grid
      final scrollableFinder = find.descendant(
        of: find.byKey(const ValueKey('daily_detail_listview')),
        matching: find.byType(Scrollable),
      ).first;
      await tester.scrollUntilVisible(find.text(l10n.healthEnvironment), 100, scrollable: scrollableFinder);
      expect(find.text(l10n.healthEnvironment), findsOneWidget);
      expect(find.text(l10n.humidity), findsOneWidget);

      await tester.scrollUntilVisible(find.text(l10n.pressure), 100, scrollable: scrollableFinder);
      expect(find.text(l10n.pressure), findsOneWidget);
      expect(find.text(l10n.uvIndex), findsOneWidget);

      // Scroll to reveal Solar Cycle & Sport
      await tester.scrollUntilVisible(find.text(l10n.solarCycle), 100, scrollable: scrollableFinder);
      expect(find.text(l10n.solarCycle), findsOneWidget);

      await tester.scrollUntilVisible(find.text(l10n.sportOutdoor), 100, scrollable: scrollableFinder);
      expect(find.text(l10n.sportOutdoor), findsOneWidget);
      expect(find.text(l10n.running), findsOneWidget);
    });

    testWidgets('Tapping bottom navigation bar items from a pushed detail screen pops and changes pages', (tester) async {
      final testRouter = GoRouter(
        navigatorKey: rootNavigatorKey,
        initialLocation: AppRoutes.home,
        routes: [
          ShellRoute(
            navigatorKey: shellNavigatorKey,
            builder: (context, state, child) => MainShell(child: child),
            routes: [
              GoRoute(
                path: AppRoutes.home,
                builder: (context, state) => const Scaffold(body: Text('Home Screen Content')),
              ),
              GoRoute(
                path: AppRoutes.favorites,
                builder: (context, state) => const Scaffold(body: Text('Favorites Screen Content')),
              ),
              GoRoute(
                path: AppRoutes.cities,
                builder: (context, state) => const Scaffold(body: Text('Cities Screen Content')),
              ),
            ],
          ),
        ],
      );

      await tester.pumpWidget(
        MaterialApp.router(
          routerConfig: testRouter,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
        ),
      );
      await tester.pump();

      expect(find.text('Home Screen Content'), findsOneWidget);

      // Push DailyForecastDetailScreen inside shell navigator
      shellNavigatorKey.currentState!.push(
        MaterialPageRoute(
          builder: (_) => DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Madrid',
              selectedDate: '2026-09-02',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pump();
      await tester.pump(const Duration(milliseconds: 300));

      expect(find.byType(DailyForecastDetailScreen), findsOneWidget);

      // Tap on Favorites tab in bottom navigation bar
      await tester.tap(find.byIcon(Icons.favorite_rounded));
      await tester.pumpAndSettle();

      // Detail screen must be popped and Favorites screen displayed!
      expect(find.byType(DailyForecastDetailScreen), findsNothing);
      expect(find.text('Favorites Screen Content'), findsOneWidget);

      // Push Detail screen again
      shellNavigatorKey.currentState!.push(
        MaterialPageRoute(
          builder: (_) => DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Madrid',
              selectedDate: '2026-09-02',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.byType(DailyForecastDetailScreen), findsOneWidget);

      // Tap on Cities tab in bottom navigation bar
      await tester.tap(find.byIcon(Icons.location_city_rounded));
      await tester.pumpAndSettle();

      // Detail screen popped and Cities screen displayed!
      expect(find.byType(DailyForecastDetailScreen), findsNothing);
      expect(find.text('Cities Screen Content'), findsOneWidget);

      // Push Detail screen again
      shellNavigatorKey.currentState!.push(
        MaterialPageRoute(
          builder: (_) => DailyForecastDetailScreen(
            args: DailyForecastDetailArgs(
              cityName: 'Madrid',
              selectedDate: '2026-09-02',
              dailyForecasts: full7DaysList,
              hourlyForecasts: hourlyList,
              isCelsius: true,
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.byType(DailyForecastDetailScreen), findsOneWidget);

      // Tap on Home tab in bottom navigation bar
      await tester.tap(find.byIcon(Icons.home_rounded));
      await tester.pumpAndSettle();

      // Detail screen popped and Home screen displayed!
      expect(find.byType(DailyForecastDetailScreen), findsNothing);
      expect(find.text('Home Screen Content'), findsOneWidget);
    });
  });

  group('Weather Chart Painters & UX Widgets', () {
    testWidgets('Custom painters render successfully without throw', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Column(
              children: [
                CustomPaint(
                  size: const Size(200, 40),
                  painter: HourlyTempLinePainter(
                    temperatures: [18.0, 19.5, 21.0, 22.0, 20.0],
                    itemWidth: 40.0,
                    isDark: false,
                  ),
                ),
                CustomPaint(
                  size: const Size(100, 30),
                  painter: HumidityWavePainter(
                    waveColor: const Color(0xFF0284C7),
                    fillColor: const Color(0x280284C7),
                  ),
                ),
                CustomPaint(
                  size: const Size(80, 80),
                  painter: PressureGaugePainter(pressure: 1014.0, isDark: false),
                ),
                CustomPaint(
                  size: const Size(100, 10),
                  painter: UvSpectrumPainter(uvIndex: 4.5, isDark: false),
                ),
                CustomPaint(
                  size: const Size(60, 60),
                  painter: WindCompassPainter(degrees: 180, isDark: false),
                ),
                CustomPaint(
                  size: const Size(200, 80),
                  painter: SolarArcPainter(progress: 0.65, isNight: false, isDark: false),
                ),
              ],
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byType(CustomPaint), findsAtLeastNWidgets(6));
    });
  });

  group('Multilingual Localization & Device Language Priority Tests', () {
    test('AppLocalizations supports all 20 languages and localized appTitle', () {
      final languages = [
        'es', 'en', 'fr', 'de', 'it', 'pt', 'nl', 'pl', 'ru', 'sv',
        'tr', 'el', 'ar', 'zh', 'ja', 'ko', 'hi', 'bn', 'th', 'vi'
      ];

      for (final lang in languages) {
        final l10n = AppLocalizations(Locale(lang));
        expect(l10n.appTitle, isNotEmpty, reason: 'appTitle for $lang must not be empty');
        expect(l10n.today, isNotEmpty, reason: 'today for $lang must not be empty');
        expect(l10n.getWeatherCondition(0), isNotEmpty, reason: 'weatherCode 0 for $lang must not be empty');
        expect(l10n.getWeatherCondition(95), isNotEmpty, reason: 'weatherCode 95 for $lang must not be empty');
      }

      // Verify specific expected titles
      expect(AppLocalizations(const Locale('es')).appTitle, 'Tiempo España');
      expect(AppLocalizations(const Locale('fr')).appTitle, 'Météo Espagne');
      expect(AppLocalizations(const Locale('en')).appTitle, 'Spain Weather');
      expect(AppLocalizations(const Locale('de')).appTitle, 'Wetter Spanien');
      expect(AppLocalizations(const Locale('it')).appTitle, 'Meteo Spagna');
      expect(AppLocalizations(const Locale('pt')).appTitle, 'Tempo Espanha');
    });

    test('SettingsRepository language priority hierarchy', () async {
      SharedPreferences.setMockInitialValues({});
      final prefs = await SharedPreferences.getInstance();
      final settings = SettingsRepository(prefs);

      // Default is System Default
      expect(settings.selectedLanguage, 'System Default');

      // 1. First launch with French device language -> App resolves to French
      expect(settings.resolveEffectiveLanguageCode('fr'), 'fr');

      // 2. First launch with Spanish device language -> App resolves to Spanish
      expect(settings.resolveEffectiveLanguageCode('es'), 'es');

      // 3. First launch with German device language -> App resolves to German
      expect(settings.resolveEffectiveLanguageCode('de'), 'de');

      // 4. First launch with Arabic device language -> App resolves to Arabic
      expect(settings.resolveEffectiveLanguageCode('ar'), 'ar');

      // 5. First launch with unsupported/unknown device language -> Fallback to 'es'
      expect(settings.resolveEffectiveLanguageCode('xx'), 'es');

      // 6. User manually selects English -> Overrides device language
      settings.selectedLanguage = 'English';
      expect(settings.resolveEffectiveLanguageCode('fr'), 'en');
      expect(settings.resolveEffectiveLanguageCode('es'), 'en');

      // 7. User manually selects German -> Overrides device language
      settings.selectedLanguage = 'German';
      expect(settings.resolveEffectiveLanguageCode('fr'), 'de');

      // 8. User reverts to System Default -> Follows device language again
      settings.selectedLanguage = 'System Default';
      expect(settings.resolveEffectiveLanguageCode('it'), 'it');
      expect(settings.resolveEffectiveLanguageCode('zh'), 'zh');
    });

    testWidgets('MaterialApp boots with AppLocalizations and displays translated UI', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          locale: const Locale('fr'),
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          home: Builder(
            builder: (context) {
              final l10n = AppLocalizations.of(context);
              return Scaffold(
                appBar: AppBar(title: Text(l10n.appTitle)),
                body: Center(child: Text(l10n.today)),
              );
            },
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Météo Espagne'), findsOneWidget);
      expect(find.text('Aujourd\'hui'), findsOneWidget);
    });
  });

  group('Weather Radar Play/Pause & Animation Tests', () {
    test('RadarFrame model correctly stores time, path, and produces DateTime', () {
      const frame = RadarFrame(
        time: 1788385800,
        path: '/v2/radar/56ed63b02625',
        isNowcast: false,
      );

      expect(frame.time, 1788385800);
      expect(frame.path, '/v2/radar/56ed63b02625');
      expect(frame.isNowcast, isFalse);
      expect(frame.dateTime.millisecondsSinceEpoch, 1788385800 * 1000);
    });

    test('Radar localized strings exist for play, pause, radarLive, rainIntensity, lightToHeavy', () {
      final es = AppLocalizations(const Locale('es'));
      expect(es.play, 'Reproducir');
      expect(es.pause, 'Pausar');
      expect(es.radarLive, 'EN DIRECTO');
      expect(es.rainIntensity, 'Intensidad de lluvia');
      expect(es.lightToHeavy, 'Ligera → Fuerte');

      final en = AppLocalizations(const Locale('en'));
      expect(en.play, 'Play');
      expect(en.pause, 'Pause');
      expect(en.radarLive, 'LIVE');
      expect(en.rainIntensity, 'Rain Intensity');
      expect(en.lightToHeavy, 'Light → Heavy');

      final fr = AppLocalizations(const Locale('fr'));
      expect(fr.play, 'Lecture');
      expect(fr.pause, 'Pause');
      expect(fr.radarLive, 'EN DIRECT');
    });
  });
}


