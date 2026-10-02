import 'package:usaweather/core/constants/api_constants.dart';
// lib/ui/screens/home/home_screen.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:usaweather/core/constants/spain_cities.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/core/theme/app_theme.dart';
import 'package:usaweather/data/models/location_data.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';
import 'package:usaweather/data/repositories/weather_repository.dart';
import 'package:usaweather/ui/components/glass_card.dart';
import 'package:usaweather/ui/components/weather_icon.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';
import 'package:usaweather/ui/screens/forecast_detail/daily_forecast_detail_screen.dart';
import 'package:usaweather/ui/components/weather_chart_painters.dart';
import 'package:usaweather/core/services/ad_service.dart';
import 'package:usaweather/ui/widgets/sport_outdoor_widget.dart';

/// ─────────────────────────────────────────────────────────────────────────
/// UHD Design Theme Tokens for High-Contrast Light & Dark Weather Modes
/// ─────────────────────────────────────────────────────────────────────────
class UhdTheme {
  final bool isDark;
  const UhdTheme(this.isDark);

  // High-contrast typography & glass tokens
  Color get cardTitle => isDark ? Colors.white : const Color(0xFF0F172A);
  Color get cardValue => isDark ? Colors.white : const Color(0xFF0F172A);
  Color get cardLabel => isDark ? Colors.white.withValues(alpha: 0.82) : const Color(0xFF334155);
  Color get cardSubtitle => isDark ? Colors.white.withValues(alpha: 0.72) : const Color(0xFF475569);
  Color get cardDivider => isDark ? Colors.white.withValues(alpha: 0.12) : const Color(0xFFCBD5E1).withValues(alpha: 0.6);
  Color get podBackground => isDark ? Colors.white.withValues(alpha: 0.14) : Colors.white.withValues(alpha: 0.65);
  Color get podBorder => isDark ? Colors.white.withValues(alpha: 0.28) : Colors.white.withValues(alpha: 0.85);
  Color get accentCyan => isDark ? const Color(0xFF38BDF8) : const Color(0xFF0284C7);
  Color get accentPill => isDark ? Colors.white.withValues(alpha: 0.18) : const Color(0xFFE0F2FE);
  Color get accentPillText => isDark ? Colors.white : const Color(0xFF0369A1);
  Color get searchBarBg => isDark ? Colors.white.withValues(alpha: 0.16) : Colors.white.withValues(alpha: 0.75);
  Color get searchBarText => isDark ? Colors.white : const Color(0xFF0F172A);
  Color get searchBarHint => isDark ? Colors.white60 : const Color(0xFF64748B);

  // Hero section typography (sits on atmospheric sky gradient)
  Color get heroCity => Colors.white;
  Color get heroDate => Colors.white.withValues(alpha: 0.85);
  Color get heroTemp => Colors.white;
  Color get heroCondition => Colors.white;
  Color get heroFeels => Colors.white.withValues(alpha: 0.90);
}

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen>
    with TickerProviderStateMixin {
  late AnimationController _fadeController;
  late AnimationController _pulseController;
  late Animation<double> _fadeAnim;
  late Animation<double> _pulseAnim;

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 700),
    );
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
      lowerBound: 0.95,
      upperBound: 1.05,
    )..repeat(reverse: true);
    _fadeAnim = CurvedAnimation(parent: _fadeController, curve: Curves.easeOutCubic);
    _pulseAnim = _pulseController;
  }

  @override
  void dispose() {
    _fadeController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final weatherState = ref.watch(homeProvider);
    final isCelsius = ref.watch(isCelsiusProvider);
    final isDark = ref.watch(isDarkModeProvider);
    final uhd = UhdTheme(isDark);

    return Scaffold(
      body: _buildBody(context, weatherState, isCelsius, isDark, uhd),
    );
  }

  Widget _buildBody(BuildContext context, WeatherUiState state,
      bool isCelsius, bool isDark, UhdTheme uhd) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 400),
      child: switch (state) {
        WeatherLoading() => _buildLoading(context),
        WeatherError(:final message) => _buildError(context, message),
        WeatherSuccess() => _buildSuccess(context, state, isCelsius, isDark, uhd),
      },
    );
  }

  Widget _buildLoading(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Container(
      key: const ValueKey('loading'),
      decoration: _bgGradient(),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const SizedBox(
              width: 54,
              height: 54,
              child: CircularProgressIndicator(
                strokeWidth: 3.5,
                valueColor: AlwaysStoppedAnimation(Color(0xFF00B4D8)),
              ),
            ),
            const SizedBox(height: 24),
            Text(
              l10n.loading,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w600,
                letterSpacing: 0.3,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildError(BuildContext context, String message) {
    final l10n = AppLocalizations.of(context);
    return Container(
      key: const ValueKey('error'),
      decoration: _bgGradient(),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text('⛈️', style: TextStyle(fontSize: 64)),
              const SizedBox(height: 16),
              Text(
                message,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              ElevatedButton.icon(
                onPressed: () =>
                    ref.read(homeProvider.notifier).loadWeatherForCity(ApiConstants.defaultCity),
                icon: const Icon(Icons.refresh_rounded),
                label: Text(l10n.retry),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF00B4D8),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSuccess(BuildContext context, WeatherSuccess state,
      bool isCelsius, bool isDark, UhdTheme uhd) {
    _fadeController.forward();
    final current = state.weather.current;
    final daily = state.weather.daily;
    final hourly = state.weather.hourly;

    return Container(
      key: const ValueKey('success'),
      decoration: _weatherGradient(current.weatherCode, current.isDay, isDark),
      child: SafeArea(
        top: true,
        bottom: false,
        child: RefreshIndicator(
          onRefresh: () => ref.read(homeProvider.notifier).refresh(),
          color: const Color(0xFF00B4D8),
          backgroundColor: Colors.white,
          child: FadeTransition(
            opacity: _fadeAnim,
            child: CustomScrollView(
              physics: const AlwaysScrollableScrollPhysics(
                parent: BouncingScrollPhysics(),
              ),
              slivers: [
                // 1. Pinned Safe Header: Search + Actions (Never clips into status bar!)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
                    child: _buildTopBar(context, state, uhd),
                  ),
                ),

                // 2. Hero Weather Section: City name, condition, big temp
                SliverToBoxAdapter(
                  child: _buildHeroSection(context, state, current, isCelsius, uhd),
                ),

                // 3. Quick Metrics Pods: Wind, Humidity, Visibility
                SliverToBoxAdapter(
                  child: _buildQuickMetricsRow(context, current, uhd),
                ),

                // 4. Hourly Forecast Zone (Screenshot 1)
                SliverToBoxAdapter(
                  child: _buildHourlyForecast(context, current, state, hourly, isCelsius, uhd),
                ),

                // 5. 7-Day Forecast Zone (Screenshot 1)
                SliverToBoxAdapter(
                  child: _buildDailyForecast(context, state, daily, isCelsius, uhd),
                ),

                // 6. Section: Health & Environment (Screenshots 1 & 2)
                SliverToBoxAdapter(
                  child: _buildHealthAndEnvironmentSection(context, current, state, uhd),
                ),

                // 7. Details Grid (Pressure, UV, Wind, Visibility, Pollen, Clothing) (Screenshots 2 & 3)
                SliverToBoxAdapter(
                  child: _buildDetailsGrid(context, current, state, isCelsius, uhd),
                ),

                // 8. Solar Cycle (Screenshot 4)
                SliverToBoxAdapter(
                  child: _buildSolarCycleCard(context, state, uhd),
                ),

                // 9. Sport & Outdoor (Screenshot 4)
                SliverToBoxAdapter(
                  child: _buildSportAndOutdoorSection(context, current, hourly, uhd),
                ),

                // Bottom padding for navigation bar
                const SliverToBoxAdapter(
                  child: SizedBox(height: 110),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  // ─── Safe Top Bar (Search + Heart + GPS) ──────────────────────────────────
  Widget _buildTopBar(BuildContext context, WeatherSuccess state, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    return Row(
      children: [
        // Search bar button
        Expanded(
          child: GestureDetector(
            onTap: () => _showCitySearch(context),
            child: Container(
              height: 44,
              padding: const EdgeInsets.symmetric(horizontal: 14),
              decoration: BoxDecoration(
                color: uhd.searchBarBg,
                borderRadius: BorderRadius.circular(22),
                border: Border.all(
                  color: Colors.white.withValues(alpha: uhd.isDark ? 0.20 : 0.90),
                  width: 1.2,
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: uhd.isDark ? 0.25 : 0.08),
                    blurRadius: 12,
                    offset: const Offset(0, 3),
                  ),
                ],
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.search_rounded,
                    size: 20,
                    color: uhd.searchBarHint,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      '${l10n.searchCityInUsaTitle}...',
                      style: TextStyle(
                        color: uhd.searchBarHint,
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 8),

        // Favorite Toggle Button
        _buildActionButton(
          onTap: () => ref.read(homeProvider.notifier).toggleFavorite(),
          icon: Icon(
            state.isFavorite ? Icons.favorite_rounded : Icons.favorite_border_rounded,
            color: state.isFavorite ? const Color(0xFFFF3366) : (uhd.isDark ? Colors.white : const Color(0xFF0F172A)),
            size: 20,
          ),
          uhd: uhd,
        ),
        const SizedBox(width: 8),

        // Follow GPS Button
        _buildActionButton(
          onTap: () => ref.read(homeProvider.notifier).followCurrentLocation(),
          icon: Icon(
            Icons.my_location_rounded,
            color: uhd.isDark ? Colors.white : const Color(0xFF0F172A),
            size: 20,
          ),
          uhd: uhd,
        ),
      ],
    );
  }

  Widget _buildActionButton({
    required VoidCallback onTap,
    required Widget icon,
    required UhdTheme uhd,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          color: uhd.searchBarBg,
          shape: BoxShape.circle,
          border: Border.all(
            color: Colors.white.withValues(alpha: uhd.isDark ? 0.20 : 0.90),
            width: 1.2,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: uhd.isDark ? 0.25 : 0.08),
              blurRadius: 12,
              offset: const Offset(0, 3),
            ),
          ],
        ),
        child: Center(child: icon),
      ),
    );
  }

  // ─── Hero Section ─────────────────────────────────────────────────────────
  Widget _buildHeroSection(BuildContext context, WeatherSuccess state,
      CurrentWeatherDomain current, bool isCelsius, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final temp = isCelsius
        ? current.temperature.round()
        : (current.temperature * 9 / 5 + 32).round();
    final feelsLike = current.feelsLike != null
        ? isCelsius
            ? current.feelsLike!.round()
            : (current.feelsLike! * 9 / 5 + 32).round()
        : null;

    final conditionName = l10n.getWeatherCondition(current.weatherCode);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Column(
        children: [
          // City Name
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.location_on_rounded,
                color: Color(0xFFFFD166),
                size: 22,
              ),
              const SizedBox(width: 4),
              Flexible(
                child: Text(
                  state.cityName,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 30,
                    fontWeight: FontWeight.w800,
                    letterSpacing: 0.2,
                    shadows: [
                      Shadow(
                        color: Color(0x66000000),
                        blurRadius: 12,
                        offset: Offset(0, 2),
                      ),
                    ],
                  ),
                  textAlign: TextAlign.center,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),

          // Date Subtitle
          Text(
            _formattedDate(l10n),
            style: TextStyle(
              color: uhd.heroDate,
              fontSize: 14,
              fontWeight: FontWeight.w500,
              shadows: const [
                Shadow(
                  color: Color(0x44000000),
                  blurRadius: 8,
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),

          // Weather Icon with Pulsing Glow
          ScaleTransition(
            scale: _pulseAnim,
            child: WeatherIcon(
              weatherCode: current.weatherCode,
              isDay: current.isDay,
              size: 104,
            ),
          ),
          const SizedBox(height: 12),

          // Big Temperature
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '$temp',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 76,
                  fontWeight: FontWeight.w800,
                  height: 1.0,
                  shadows: [
                    Shadow(
                      color: Color(0x55000000),
                      blurRadius: 16,
                      offset: Offset(0, 4),
                    ),
                  ],
                ),
              ),
              Text(
                isCelsius ? '°C' : '°F',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 38,
                  fontWeight: FontWeight.w400,
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),

          // Weather Condition Badge
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            decoration: BoxDecoration(
              color: Colors.black.withValues(alpha: 0.22),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: Colors.white.withValues(alpha: 0.25),
                width: 1,
              ),
            ),
            child: Text(
              conditionName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.4,
              ),
            ),
          ),

          if (feelsLike != null) ...[
            const SizedBox(height: 6),
            Text(
              '${l10n.feelsLike}: $feelsLike${isCelsius ? "°C" : "°F"}',
              style: TextStyle(
                color: uhd.heroFeels,
                fontSize: 14,
                fontWeight: FontWeight.w600,
                shadows: const [
                  Shadow(
                    color: Color(0x44000000),
                    blurRadius: 6,
                  ),
                ],
              ),
            ),
          ],
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  // ─── Quick Metrics Pods (Wind, Humidity, Visibility) ──────────────────────
  Widget _buildQuickMetricsRow(BuildContext context, CurrentWeatherDomain current, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          Expanded(
            child: _quickMetricPod(
              icon: Icons.air_rounded,
              iconColor: const Color(0xFF38BDF8),
              value: '${current.windSpeed.round()} km/h',
              label: l10n.wind,
              uhd: uhd,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: _quickMetricPod(
              icon: Icons.water_drop_rounded,
              iconColor: const Color(0xFF60A5FA),
              value: '${current.humidity ?? '-'}%',
              label: l10n.humidity,
              uhd: uhd,
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: _quickMetricPod(
              icon: Icons.visibility_rounded,
              iconColor: const Color(0xFF34D399),
              value: '${((current.visibility ?? 10000) / 1000).toStringAsFixed(1)} km',
              label: l10n.visibility,
              uhd: uhd,
            ),
          ),
        ],
      ),
    );
  }

  Widget _quickMetricPod({
    required IconData icon,
    required Color iconColor,
    required String value,
    required String label,
    required UhdTheme uhd,
  }) {
    return GlassCard(
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
      child: Column(
        children: [
          _buildIconBadge(icon, iconColor, 34, 18),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              color: uhd.cardValue,
              fontSize: 14,
              fontWeight: FontWeight.w800,
            ),
            textAlign: TextAlign.center,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: TextStyle(
              color: uhd.cardLabel,
              fontSize: 11,
              fontWeight: FontWeight.w600,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  // ─── 4. Hourly Forecast Zone (Screenshot 1) ──────────────────────────────
  Widget _buildHourlyForecast(
      BuildContext context,
      CurrentWeatherDomain current,
      WeatherSuccess state,
      List<HourlyWeatherDomain> hourly,
      bool isCelsius,
      UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final now = DateTime.now();
    final upcoming = hourly
        .where((h) =>
            DateTime.tryParse(h.time)
                ?.isAfter(now.subtract(const Duration(hours: 1))) ??
            false)
        .take(24)
        .toList();

    if (upcoming.isEmpty) return const SizedBox.shrink();

    final todayDaily = state.weather.daily.isNotEmpty ? state.weather.daily.first : null;
    final summaryText = _naturalSummary(current, todayDaily, isCelsius, l10n);

    final temps = upcoming.map((h) => h.temperature).toList();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: GlassCard(
        borderRadius: 28,
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Natural language forecast summary header (Screenshot 1)
            Text(
              summaryText,
              style: TextStyle(
                color: uhd.cardTitle,
                fontWeight: FontWeight.w600,
                fontSize: 14,
                height: 1.35,
              ),
            ),
            const SizedBox(height: 18),

            // Horizontal Capsules with continuous temperature line
            SizedBox(
              height: 176,
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                physics: const BouncingScrollPhysics(),
                child: Stack(
                  children: [
                    // Capsule Pods Row (Background pods)
                    Row(
                      children: [
                        for (int i = 0; i < upcoming.length; i++) ...[
                          () {
                            final h = upcoming[i];
                            final dt = DateTime.tryParse(h.time);
                            final hourStr = i == 0
                                ? l10n.now
                                : (dt != null ? DateFormat('h a').format(dt) : h.time);
                            final temp = isCelsius
                                ? h.temperature.round()
                                : (h.temperature * 9 / 5 + 32).round();

                            return Container(
                              width: 68,
                              height: 174,
                              padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 4),
                              decoration: BoxDecoration(
                                color: i == 0
                                    ? Colors.white.withValues(alpha: 0.18)
                                    : Colors.white.withValues(alpha: 0.05),
                                borderRadius: BorderRadius.circular(22),
                                border: Border.all(
                                  color: i == 0
                                      ? const Color(0xFF00B4D8).withValues(alpha: 0.60)
                                      : Colors.white.withValues(alpha: 0.12),
                                  width: i == 0 ? 1.5 : 1.0,
                                ),
                              ),
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  // Temperature — hero at top, never overlaps the line
                                  Text(
                                    '$temp${isCelsius ? "°C" : "°F"}',
                                    style: TextStyle(
                                      color: uhd.cardValue,
                                      fontWeight: FontWeight.w800,
                                      fontSize: 17,
                                    ),
                                  ),

                                  // Time label
                                  Text(
                                    hourStr,
                                    style: TextStyle(
                                      color: uhd.cardLabel,
                                      fontWeight: FontWeight.w600,
                                      fontSize: 11,
                                    ),
                                  ),

                                  // Weather Icon
                                  WeatherIcon(
                                    weatherCode: h.weatherCode,
                                    isDay: h.isDay,
                                    size: 26,
                                  ),

                                  // Line dot zone — golden curve passes through here
                                  const SizedBox(height: 18),

                                  // Rain probability
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Icon(
                                        Icons.water_drop_rounded,
                                        size: 11,
                                        color: uhd.accentCyan,
                                      ),
                                      const SizedBox(width: 2),
                                      Text(
                                        '${h.precipitationProbability}%',
                                        style: TextStyle(
                                          color: uhd.accentCyan,
                                          fontWeight: FontWeight.w700,
                                          fontSize: 10,
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            );
                          }(),
                          if (i < upcoming.length - 1) const SizedBox(width: 8),
                        ],
                      ],
                    ),

                    // Golden trend line (FOREGROUND overlay — on top of pods!)
                    Positioned(
                      left: 0,
                      top: 0,
                      width: upcoming.length * 76.0,
                      height: 174,
                      child: IgnorePointer(
                        child: CustomPaint(
                          size: Size(upcoming.length * 76.0, 174),
                          painter: HourlyTempLinePainter(
                            temperatures: temps,
                            itemWidth: 76.0,
                            podWidth: 68.0,
                            lineColor: const Color(0xFFFBBF24),
                            isDark: uhd.isDark,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ─── 5. 7-Day Forecast Zone (Screenshot 1) ────────────────────────────────
  Widget _buildDailyForecast(BuildContext context, WeatherSuccess state,
      List<DailyWeatherDomain> daily, bool isCelsius, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final weekList = daily.take(7).toList();
    if (weekList.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: GlassCard(
        borderRadius: 28,
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Zone Header
            Row(
              children: [
                Text(
                  l10n.sevenDayForecast,
                  style: TextStyle(
                    color: uhd.cardTitle,
                    fontWeight: FontWeight.w800,
                    fontSize: 14,
                    letterSpacing: 0.8,
                  ),
                ),
                const Spacer(),
                Icon(
                  Icons.arrow_forward_ios_rounded,
                  size: 13,
                  color: uhd.cardSubtitle,
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Horizontal Scrollable Day Cards (Screenshot 1)
            SizedBox(
              height: 154,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: weekList.length,
                separatorBuilder: (_, __) => const SizedBox(width: 10),
                itemBuilder: (context, i) {
                  final d = weekList[i];
                  final dt = DateTime.tryParse(d.time);
                  final today = DateTime.now();
                  final isToday = dt != null &&
                      dt.year == today.year &&
                      dt.month == today.month &&
                      dt.day == today.day;

                  final dayStr = isToday
                      ? l10n.today
                      : (dt != null
                          ? DateFormat.E(l10n.locale.languageCode).format(dt)
                          : d.time);

                  final dayNum = dt != null ? DateFormat('dd').format(dt) : '';

                  final maxT = isCelsius
                      ? d.maxTemp.round()
                      : (d.maxTemp * 9 / 5 + 32).round();
                  final minT = isCelsius
                      ? d.minTemp.round()
                      : (d.minTemp * 9 / 5 + 32).round();
                  final unitStr = isCelsius ? '°C' : '°F';

                  return GestureDetector(
                    onTap: () {
                      void navigateToDetail() {
                        if (!context.mounted) return;
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => DailyForecastDetailScreen(
                              args: DailyForecastDetailArgs(
                                cityName: state.cityName,
                                selectedDate: d.time,
                                dailyForecasts: weekList,
                                hourlyForecasts: state.weather.hourly,
                                isCelsius: isCelsius,
                              ),
                            ),
                          ),
                        );
                      }

                      AdService.instance.showInterstitialAd(
                        onDismissed: navigateToDetail,
                      );
                    },
                    child: Container(
                      width: 86,
                      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 6),
                      decoration: BoxDecoration(
                        color: isToday
                            ? Colors.white.withValues(alpha: 0.18)
                            : Colors.white.withValues(alpha: 0.05),
                        borderRadius: BorderRadius.circular(22),
                        border: Border.all(
                          color: isToday
                              ? const Color(0xFF00B4D8).withValues(alpha: 0.60)
                              : Colors.white.withValues(alpha: 0.12),
                          width: isToday ? 1.5 : 1.0,
                        ),
                      ),
                      child: Column(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                          // Day name & date number
                          Column(
                            children: [
                              Text(
                                dayStr,
                                style: TextStyle(
                                  color: isToday
                                      ? uhd.accentCyan
                                      : uhd.cardTitle,
                                  fontWeight: isToday ? FontWeight.w800 : FontWeight.w700,
                                  fontSize: 14,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                dayNum,
                                style: TextStyle(
                                  color: uhd.cardSubtitle,
                                  fontSize: 11,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),

                          // Weather Icon
                          WeatherIcon(
                            weatherCode: d.weatherCode,
                            isDay: 1,
                            size: 32,
                          ),

                          // Short description
                          Text(
                            _shortDescription(d.weatherCode, l10n),
                            style: TextStyle(
                              color: uhd.cardLabel,
                              fontSize: 11,
                              fontWeight: FontWeight.w500,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            textAlign: TextAlign.center,
                          ),

                          // Min/Max
                          Text(
                            '$maxT$unitStr/$minT$unitStr',
                            style: TextStyle(
                              color: uhd.cardValue,
                              fontWeight: FontWeight.w700,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ─── 6. Section: Health & Environment (Screenshots 1 & 2) ──────────────────
  Widget _buildHealthAndEnvironmentSection(
      BuildContext context,
      CurrentWeatherDomain current,
      WeatherSuccess state,
      UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final aqiIndex = (state.airQuality?.europeanAqi ?? 33).round();
    final (aqiStatus, aqiColor) = _aqiLabel(aqiIndex, l10n);
    final humidity = current.humidity ?? 83;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
          child: Text(
            l10n.healthEnvironment,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 22,
              fontWeight: FontWeight.w800,
              letterSpacing: -0.3,
            ),
          ),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          child: Row(
            children: [
              // AQI Card
              Expanded(
                child: GlassCard(
                  borderRadius: 28,
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'AQI($aqiStatus)',
                        style: TextStyle(
                          color: uhd.cardLabel,
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 10),
                      Text(
                        '$aqiStatus ($aqiIndex)',
                        style: TextStyle(
                          color: uhd.cardValue,
                          fontSize: 20,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      const SizedBox(height: 18),
                      // Progress Bar with dot
                      Container(
                        height: 6,
                        decoration: BoxDecoration(
                          color: uhd.isDark ? uhd.podBackground : const Color(0xFFE2E8F0),
                          borderRadius: BorderRadius.circular(3),
                        ),
                        child: FractionallySizedBox(
                          widthFactor: (aqiIndex / 100.0).clamp(0.1, 1.0),
                          alignment: Alignment.centerLeft,
                          child: Container(
                            decoration: BoxDecoration(
                              color: aqiColor,
                              borderRadius: BorderRadius.circular(3),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: 10),

              // Humidity Card
              Expanded(
                child: GlassCard(
                  borderRadius: 28,
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Icon(
                            Icons.water_drop_rounded,
                            size: 14,
                            color: uhd.accentCyan,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            l10n.humidity,
                            style: TextStyle(
                              color: uhd.cardLabel,
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      SizedBox(
                        height: 36,
                        width: double.infinity,
                        child: CustomPaint(
                          painter: HumidityWavePainter(
                            waveColor: uhd.accentCyan,
                            fillColor: uhd.accentCyan.withValues(alpha: 0.18),
                          ),
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '$humidity%: ${l10n.steady}',
                        style: TextStyle(
                          color: uhd.cardValue,
                          fontSize: 16,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  // ─── 7. 2-Column Details Grid (Screenshots 2 & 3) ──────────────────────────
  Widget _buildDetailsGrid(BuildContext context, CurrentWeatherDomain current,
      WeatherSuccess state, bool isCelsius, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final todayDaily = state.weather.daily.isNotEmpty ? state.weather.daily.first : null;
    final uv = todayDaily?.uvIndex ?? 2.2;
    final gusts = current.windGusts?.round() ?? 34;
    final visKm = current.visibility != null
        ? (current.visibility! / 1000).toStringAsFixed(1)
        : '18.7';
    final pressureVal = current.pressure.round();

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: GridView.count(
        crossAxisCount: 2,
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
        childAspectRatio: 1.05,
        children: [
          // 1. PRESSURE (Circular Arc Speedometer)
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.speed_rounded, size: 16, color: uhd.accentCyan),
                    const SizedBox(width: 6),
                    Text(
                      l10n.pressure,
                      style: TextStyle(
                        color: uhd.cardLabel,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                Expanded(
                  child: Center(
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        CustomPaint(
                          size: const Size(100, 80),
                          painter: PressureGaugePainter(
                            pressure: current.pressure,
                            isDark: uhd.isDark,
                          ),
                        ),
                        Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              '$pressureVal',
                              style: TextStyle(
                                color: uhd.cardValue,
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                            Text(
                              'mb',
                              style: TextStyle(
                                color: uhd.cardSubtitle,
                                fontSize: 11,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
                Center(
                  child: Text(
                    l10n.steady,
                    style: TextStyle(
                      color: uhd.cardSubtitle,
                      fontSize: 11,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // 2. UV INDEX (Rainbow Spectrum Bar)
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.wb_sunny_rounded, size: 15, color: Color(0xFFFBBF24)),
                        const SizedBox(width: 6),
                        Text(
                          l10n.uvIndex,
                          style: TextStyle(
                            color: uhd.cardLabel,
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                    ),
                    Text(
                      uv.toStringAsFixed(1),
                      style: TextStyle(
                        color: uhd.cardValue,
                        fontSize: 15,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),
                Text(
                  _uvStatus(uv, l10n),
                  style: TextStyle(
                    color: uhd.cardValue,
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${uv.round()}',
                      style: TextStyle(
                        color: uhd.cardSubtitle,
                        fontSize: 10,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    SizedBox(
                      height: 6,
                      width: double.infinity,
                      child: CustomPaint(
                        painter: UvSpectrumPainter(
                          uvIndex: uv,
                          isDark: uhd.isDark,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // 3. WIND (Compass Dial)
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${l10n.wind} : ${current.windSpeed.round()} km/h',
                  style: TextStyle(
                    color: uhd.cardLabel,
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                Expanded(
                  child: Center(
                    child: CustomPaint(
                      size: const Size(68, 68),
                      painter: WindCompassPainter(
                        degrees: current.windDirection,
                        isDark: uhd.isDark,
                      ),
                    ),
                  ),
                ),
                Center(
                  child: Text(
                    '${l10n.windGusts}: $gusts km/h',
                    style: TextStyle(
                      color: uhd.cardSubtitle,
                      fontSize: 11,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // 4. VISIBILITY
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Icon(Icons.remove_red_eye_rounded, size: 15, color: Color(0xFFF97316)),
                    const SizedBox(width: 6),
                    Text(
                      l10n.visibility,
                      style: TextStyle(
                        color: uhd.cardLabel,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.baseline,
                  textBaseline: TextBaseline.alphabetic,
                  children: [
                    Text(
                      '$visKm km',
                      style: TextStyle(
                        color: uhd.cardValue,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(width: 4),
                    Text(
                      l10n.aqiLevel(10),
                      style: const TextStyle(
                        color: Color(0xFF22C55E),
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      height: 4,
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: const Color(0xFF22C55E),
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      l10n.clearVisibility,
                      style: TextStyle(
                        color: uhd.cardSubtitle,
                        fontSize: 11,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // 5. POLLEN & ALLERGY
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Icon(Icons.eco_rounded, size: 15, color: Color(0xFF22C55E)),
                    const SizedBox(width: 6),
                    Text(
                      l10n.pollenAllergy,
                      style: TextStyle(
                        color: uhd.cardLabel,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                Text(
                  l10n.uvLow,
                  style: const TextStyle(
                    color: Color(0xFF22C55E),
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.pollenTypes,
                      style: TextStyle(
                        color: uhd.cardSubtitle,
                        fontSize: 10,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Container(
                      height: 4,
                      decoration: BoxDecoration(
                        color: uhd.isDark ? uhd.podBackground : const Color(0xFFE2E8F0),
                        borderRadius: BorderRadius.circular(2),
                      ),
                      child: FractionallySizedBox(
                        widthFactor: 0.25,
                        alignment: Alignment.centerLeft,
                        child: Container(
                          decoration: BoxDecoration(
                            color: const Color(0xFF22C55E),
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          // 6. CLOTHING SUGGESTION
          GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Icon(Icons.checkroom_rounded, size: 15, color: uhd.accentCyan),
                    const SizedBox(width: 6),
                    Text(
                      l10n.clothingSuggestion,
                      style: TextStyle(
                        color: uhd.cardLabel,
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Icon(
                      current.temperature < 16
                          ? Icons.dry_cleaning_rounded
                          : Icons.person_rounded,
                      size: 32,
                      color: uhd.isDark ? const Color(0xFF93C5FD) : const Color(0xFF0284C7),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _clothingSuggestion(current.temperature, l10n),
                        style: TextStyle(
                          color: uhd.cardValue,
                          fontSize: 14,
                          fontWeight: FontWeight.w800,
                          height: 1.2,
                        ),
                      ),
                    ),
                  ],
                ),
                Text(
                  l10n.basedOnForecast,
                  style: TextStyle(
                    color: uhd.cardSubtitle,
                    fontSize: 10,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ─── 8. Solar Cycle Card (Screenshot 4) ────────────────────────────────────
  Widget _buildSolarCycleCard(BuildContext context, WeatherSuccess state, UhdTheme uhd) {
    final l10n = AppLocalizations.of(context);
    final todayDaily = state.weather.daily.isNotEmpty ? state.weather.daily.first : null;
    final sunriseRaw = todayDaily?.sunrise;
    final sunsetRaw = todayDaily?.sunset;
    final sunriseStr = _formatTimeAmPm(sunriseRaw) ?? '6:14 AM';
    final sunsetStr = _formatTimeAmPm(sunsetRaw) ?? '7:45 PM';

    final now = DateTime.now();
    final isNight = now.hour < 7 || now.hour >= 20;
    final progress = _solarProgress(sunriseRaw, sunsetRaw);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: GlassCard(
        borderRadius: 28,
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                _buildIconBadge(Icons.wb_sunny_rounded, const Color(0xFFF59E0B), 28, 16),
                const SizedBox(width: 8),
                Text(
                  l10n.solarCycle,
                  style: TextStyle(
                    color: uhd.cardLabel,
                    fontWeight: FontWeight.w700,
                    fontSize: 12,
                    letterSpacing: 0.5,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),

            // Parabolic Glowing Sun Arc
            SizedBox(
              height: 80,
              width: double.infinity,
              child: CustomPaint(
                painter: SolarArcPainter(
                  progress: progress,
                  isNight: isNight,
                  isDark: uhd.isDark,
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Bottom Sunrise / Badge / Sunset
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.sunrise.toUpperCase(),
                      style: TextStyle(
                        color: uhd.cardSubtitle,
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.5,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      sunriseStr,
                      style: TextStyle(
                        color: uhd.cardValue,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),

                // Center Pill Badge
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                  decoration: BoxDecoration(
                    color: uhd.accentPill,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: uhd.isDark ? Colors.transparent : const Color(0xFFBAE6FD),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        isNight ? Icons.nightlight_round : Icons.wb_sunny_rounded,
                        size: 14,
                        color: isNight
                            ? (uhd.isDark ? const Color(0xFFFBBF24) : const Color(0xFFD97706))
                            : const Color(0xFFF59E0B),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        isNight ? l10n.nightPill : l10n.dayPill,
                        style: TextStyle(
                          color: uhd.accentPillText,
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 0.5,
                        ),
                      ),
                    ],
                  ),
                ),

                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      l10n.sunset.toUpperCase(),
                      style: TextStyle(
                        color: uhd.cardSubtitle,
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.5,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      sunsetStr,
                      style: TextStyle(
                        color: uhd.cardValue,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // ─── 9. Sport & Outdoor Section (Interactive Multi-Sport) ───────────────────
  Widget _buildSportAndOutdoorSection(
      BuildContext context, CurrentWeatherDomain current, List<HourlyWeatherDomain> hourly, UhdTheme uhd) {
    return SportOutdoorWidget(
      hourly: hourly,
      uhd: uhd,
      isDark: uhd.isDark,
    );
  }

  // ─── Reusable Icon Badge ───────────────────────────────────────────────────
  Widget _buildIconBadge(IconData icon, Color color, double size, double iconSize) {
    final isIconDark = color.computeLuminance() < 0.40;
    final effectiveIconColor = isIconDark ? Colors.white : color;

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.28),
        shape: BoxShape.circle,
        border: Border.all(
          color: Colors.white.withValues(alpha: 0.50),
          width: 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.35),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Center(
        child: Icon(icon, size: iconSize, color: effectiveIconColor),
      ),
    );
  }

  // ─── Gradients & Descriptions ─────────────────────────────────────────────
  BoxDecoration _bgGradient() => const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF0A0E2E), Color(0xFF1A237E)],
        ),
      );

  BoxDecoration _weatherGradient(int code, int isDay, bool isDark) {
    final List<Color> colors;

    if (!isDark) {
      // Light Mode: Radiant Spanish Sky & Daylight gradients
      colors = switch (code) {
        0 || 1 when isDay == 1 => [
            const Color(0xFF38BDF8), // Radiant Spanish Sky Blue
            const Color(0xFF0284C7), // Deep Azure
            const Color(0xFF0369A1),
          ],
        0 || 1 => [ // Twilight/Night in Light Mode
            const Color(0xFF475569),
            const Color(0xFF334155),
            const Color(0xFF1E293B),
          ],
        2 || 3 when isDay == 1 => [ // Partly cloudy
            const Color(0xFF60A5FA),
            const Color(0xFF3B82F6),
            const Color(0xFF1D4ED8),
          ],
        2 || 3 => [
            const Color(0xFF64748B),
            const Color(0xFF475569),
          ],
        45 || 48 => [ // Fog
            const Color(0xFF94A3B8),
            const Color(0xFF64748B),
          ],
        51 || 53 || 55 || 61 || 63 || 65 || 80 || 81 || 82 => [ // Rain
            const Color(0xFF60A5FA),
            const Color(0xFF2563EB),
            const Color(0xFF1D4ED8),
          ],
        71 || 73 || 75 || 77 || 85 || 86 => [ // Snow
            const Color(0xFFBAE6FD),
            const Color(0xFF7DD3FC),
            const Color(0xFF38BDF8),
          ],
        95 || 96 || 99 => [ // Thunderstorm
            const Color(0xFF6366F1),
            const Color(0xFF4338CA),
            const Color(0xFF312E81),
          ],
        _ => [
            const Color(0xFF38BDF8),
            const Color(0xFF0284C7),
          ],
      };
    } else {
      // Dark Mode: Midnight & Deep Atmospheric palettes
      colors = switch (code) {
        0 || 1 when isDay == 1 => [
            const Color(0xFF0284C7),
            const Color(0xFF0369A1),
          ],
        0 || 1 => [
            const Color(0xFF0A0E2E),
            const Color(0xFF1E1B4B),
          ],
        2 || 3 when isDay == 1 => [
            const Color(0xFF3B82F6),
            const Color(0xFF1D4ED8),
          ],
        2 || 3 => [
            const Color(0xFF1E293B),
            const Color(0xFF0F172A),
          ],
        45 || 48 => [
            const Color(0xFF475569),
            const Color(0xFF334155),
          ],
        51 || 53 || 55 || 61 || 63 || 65 || 80 || 81 || 82 => [
            const Color(0xFF1E3A8A),
            const Color(0xFF172554),
          ],
        71 || 73 || 75 || 77 || 85 || 86 => [
            const Color(0xFF334155),
            const Color(0xFF64748B),
          ],
        95 || 96 || 99 => [
            const Color(0xFF311042),
            const Color(0xFF0F051D),
          ],
        _ => [
            const Color(0xFF0284C7),
            const Color(0xFF0369A1),
          ],
      };
    }

    return BoxDecoration(
      gradient: LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: colors,
      ),
    );
  }

  String _formattedDate([AppLocalizations? l10n]) {
    final lang = l10n?.locale.languageCode ?? 'es';
    return DateFormat('EEEE, d MMMM', lang).format(DateTime.now());
  }

  String _weatherDescription(int code, [AppLocalizations? l10n]) {
    if (l10n != null) return l10n.getWeatherCondition(code);
    return switch (code) {
      0 => 'Clear Sky',
      1 => 'Mainly Clear',
      2 => 'Partly Cloudy',
      3 => 'Overcast',
      45 => 'Fog',
      48 => 'Freezing Fog',
      51 => 'Light Drizzle',
      53 => 'Moderate Drizzle',
      55 => 'Dense Drizzle',
      61 => 'Light Rain',
      63 => 'Moderate Rain',
      65 => 'Heavy Rain',
      71 => 'Light Snow',
      73 => 'Moderate Snow',
      75 => 'Heavy Snow',
      77 => 'Snow Grains',
      80 => 'Light Showers',
      81 => 'Moderate Showers',
      82 => 'Violent Showers',
      85 => 'Snow Showers',
      86 => 'Heavy Snow Showers',
      95 => 'Thunderstorm',
      96 => 'Thunderstorm with Hail',
      99 => 'Heavy Thunderstorm with Hail',
      _ => 'Variable',
    };
  }

  (String, Color) _aqiLabel(int index, [AppLocalizations? l10n]) {
    final label = l10n != null
        ? l10n.aqiLevel(index)
        : (index <= 20
            ? 'Excellent'
            : (index <= 40
                ? 'Good'
                : (index <= 60
                    ? 'Moderate'
                    : (index <= 80 ? 'Poor' : 'Very Poor'))));
    if (index <= 20) return (label, AppColors.aqiGood);
    if (index <= 40) return (label, AppColors.aqiFair);
    if (index <= 60) return (label, AppColors.aqiModerate);
    if (index <= 80) return (label, AppColors.aqiPoor);
    return (label, AppColors.aqiVeryPoor);
  }

  void _showCitySearch(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => const _CitySearchSheet(),
    );
  }

  String _naturalSummary(
      CurrentWeatherDomain current, DailyWeatherDomain? todayDaily, bool isCelsius,
      [AppLocalizations? l10n]) {
    final condition = l10n != null
        ? l10n.getWeatherCondition(current.weatherCode)
        : _weatherDescription(current.weatherCode);
    if (todayDaily == null) return '$condition.';
    final maxT = isCelsius
        ? todayDaily.maxTemp.round()
        : (todayDaily.maxTemp * 9 / 5 + 32).round();
    final minT = isCelsius
        ? todayDaily.minTemp.round()
        : (todayDaily.minTemp * 9 / 5 + 32).round();
    final unitStr = isCelsius ? '°C' : '°F';
    return '$condition · $minT$unitStr / $maxT$unitStr';
  }

  String _shortDescription(int code, [AppLocalizations? l10n]) {
    if (l10n != null) return l10n.getWeatherCondition(code);
    return switch (code) {
      0 => 'Clear',
      1 => 'Mainly Clear',
      2 => 'Partly Cloudy',
      3 => 'Overcast',
      45 || 48 => 'Fog',
      51 || 53 || 55 => 'Drizzle',
      61 || 63 => 'Rain',
      65 => 'Heavy Rain',
      71 || 73 || 75 => 'Snow',
      80 || 81 || 82 => 'Showers',
      95 || 96 || 99 => 'Thunderstorm',
      _ => 'Variable',
    };
  }

  String _uvStatus(double uv, [AppLocalizations? l10n]) {
    if (l10n != null) return l10n.uvLevel(uv);
    if (uv <= 2) return 'Low';
    if (uv <= 5) return 'Moderate';
    if (uv <= 7) return 'High';
    if (uv <= 10) return 'Very High';
    return 'Extreme';
  }

  String _clothingSuggestion(double temp, [AppLocalizations? l10n]) {
    if (l10n != null) return l10n.clothingForTemp(temp);
    if (temp < 12) return 'Light Jacket & Layers';
    if (temp < 18) return 'Light Jacket & Shirt';
    if (temp < 24) return 'T-Shirt & Jeans';
    return 'Light Shirt & Shorts';
  }

  String? _formatTimeAmPm(String? raw) {
    if (raw == null) return null;
    try {
      final dt = DateTime.parse(raw);
      return DateFormat('h:mm a').format(dt);
    } catch (_) {
      return raw;
    }
  }

  double _solarProgress(String? sunriseRaw, String? sunsetRaw) {
    if (sunriseRaw == null || sunsetRaw == null) return 0.5;
    try {
      final sunrise = DateTime.parse(sunriseRaw);
      final sunset = DateTime.parse(sunsetRaw);
      final now = DateTime.now();
      if (now.isBefore(sunrise)) return 0.0;
      if (now.isAfter(sunset)) return 1.0;
      final total = sunset.difference(sunrise).inMinutes;
      final current = now.difference(sunrise).inMinutes;
      if (total <= 0) return 0.5;
      return (current / total).clamp(0.0, 1.0);
    } catch (_) {
      return 0.5;
    }
  }
}

// ─── City Search Bottom Sheet ──────────────────────────────────────────────
class _CitySearchSheet extends ConsumerStatefulWidget {
  const _CitySearchSheet();

  @override
  ConsumerState<_CitySearchSheet> createState() => _CitySearchSheetState();
}

class _CitySearchSheetState extends ConsumerState<_CitySearchSheet> {
  final TextEditingController _controller = TextEditingController();
  Timer? _debounceTimer;
  String _query = '';
  String _selectedRegion = 'Todas';
  List<LocationData> _onlineResults = [];
  bool _isSearchingOnline = false;

  static const List<String> _regions = [
    'Todas',
    'Centro',
    'Cataluña & Aragón',
    'Andalucía',
    'Levante',
    'Norte',
    'Islas',
  ];

  @override
  void dispose() {
    _debounceTimer?.cancel();
    _controller.dispose();
    super.dispose();
  }

  void _onQueryChanged(String val) {
    setState(() {
      _query = val;
    });

    _debounceTimer?.cancel();
    if (val.trim().length >= 2) {
      _debounceTimer = Timer(const Duration(milliseconds: 350), () {
        _performOnlineSearch(val.trim());
      });
    } else {
      setState(() {
        _onlineResults = [];
        _isSearchingOnline = false;
      });
    }
  }

  Future<void> _performOnlineSearch(String q) async {
    if (!mounted) return;
    setState(() {
      _isSearchingOnline = true;
    });

    try {
      final repo = getIt<WeatherRepository>();
      final lang = getIt<SettingsRepository>().getSelectedLanguageCode();
      final results = await repo.searchCity(q, language: lang);
      if (mounted && _query.trim() == q) {
        setState(() {
          _onlineResults = results;
          _isSearchingOnline = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _isSearchingOnline = false;
        });
      }
    }
  }

  void _selectCityItem(LocationData loc) {
    ref.read(homeProvider.notifier).selectCity(loc);
    Navigator.pop(context);
  }

  void _submit(List<SpainCityItem> filteredLocal) {
    final q = _query.trim();
    if (q.isEmpty) return;

    if (filteredLocal.isNotEmpty) {
      _selectCityItem(filteredLocal.first.toLocationData());
    } else if (_onlineResults.isNotEmpty) {
      _selectCityItem(_onlineResults.first);
    } else {
      ref.read(homeProvider.notifier).loadWeatherForCity(q);
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final l10n = AppLocalizations.of(context);

    final qLower = _query.trim().toLowerCase();
    final filteredSpainCities = SpainCities.cities.where((city) {
      final matchesRegion =
          _selectedRegion == 'Todas' || city.regionGroup == _selectedRegion;
      if (qLower.isEmpty) return matchesRegion;
      return city.name.toLowerCase().contains(qLower) ||
          city.province.toLowerCase().contains(qLower) ||
          city.community.toLowerCase().contains(qLower);
    }).toList();

    // Filter out online duplicates
    final nonDuplicateOnline = _onlineResults.where((r) {
      return !filteredSpainCities.any((local) =>
          local.name.toLowerCase() == r.name.toLowerCase());
    }).toList();

    return DraggableScrollableSheet(
      initialChildSize: 0.75,
      maxChildSize: 0.95,
      minChildSize: 0.40,
      builder: (_, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF131738) : Colors.white,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
            boxShadow: const [
              BoxShadow(
                color: Colors.black38,
                blurRadius: 24,
                offset: Offset(0, -6),
              ),
            ],
          ),
          child: Column(
            children: [
              const SizedBox(height: 12),
              // Drag indicator bar
              Container(
                width: 44,
                height: 5,
                decoration: BoxDecoration(
                  color: isDark ? Colors.white24 : Colors.black12,
                  borderRadius: BorderRadius.circular(3),
                ),
              ),
              const SizedBox(height: 14),

              // Title Header
              Text(
                l10n.searchCityInUsaTitle,
                style: TextStyle(
                  color: isDark ? Colors.white : const Color(0xFF0F172A),
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.2,
                ),
              ),
              const SizedBox(height: 12),

              // Search Input Field
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: TextField(
                  controller: _controller,
                  autofocus: true,
                  onChanged: _onQueryChanged,
                  onSubmitted: (_) => _submit(filteredSpainCities),
                  style: TextStyle(
                    color: isDark ? Colors.white : const Color(0xFF0F172A),
                    fontWeight: FontWeight.w600,
                    fontSize: 15,
                  ),
                  decoration: InputDecoration(
                    hintText: l10n.searchHint,
                    hintStyle: TextStyle(
                      color: isDark ? Colors.white38 : const Color(0xFF94A3B8),
                    ),
                    prefixIcon: const Icon(Icons.search_rounded, color: Color(0xFF00B4D8)),
                    filled: true,
                    fillColor: isDark
                        ? Colors.white.withValues(alpha: 0.08)
                        : const Color(0xFFF1F5F9),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
                    suffixIcon: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (_query.isNotEmpty)
                          IconButton(
                            icon: const Icon(Icons.close_rounded, size: 20),
                            color: isDark ? Colors.white60 : Colors.black54,
                            onPressed: () {
                              _controller.clear();
                              _onQueryChanged('');
                            },
                          ),
                        IconButton(
                          icon: const Icon(Icons.arrow_forward_rounded, color: Color(0xFF00B4D8)),
                          onPressed: () => _submit(filteredSpainCities),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 10),

              // Region Filter Chips (When Query is Empty)
              if (_query.isEmpty) ...[
                SizedBox(
                  height: 38,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: _regions.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 8),
                    itemBuilder: (context, i) {
                      final reg = _regions[i];
                      final isSelected = reg == _selectedRegion;
                      final displayLabel = reg == 'Todas' ? l10n.allRegions : reg;
                      return ChoiceChip(
                        label: Text(displayLabel),
                        selected: isSelected,
                        onSelected: (_) {
                          setState(() {
                            _selectedRegion = reg;
                          });
                        },
                        selectedColor: const Color(0xFF00B4D8),
                        backgroundColor: isDark
                            ? Colors.white.withValues(alpha: 0.08)
                            : const Color(0xFFF1F5F9),
                        labelStyle: TextStyle(
                          color: isSelected
                              ? Colors.white
                              : (isDark ? Colors.white70 : Colors.black87),
                          fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                          fontSize: 12,
                        ),
                        visualDensity: VisualDensity.compact,
                        padding: const EdgeInsets.symmetric(horizontal: 10),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(18),
                        ),
                      );
                    },
                  ),
                ),
                const SizedBox(height: 10),
              ],

              // Results List
              Expanded(
                child: ListView(
                  controller: scrollController,
                  padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
                  children: [
                    // Section Header for Spanish Cities
                    if (filteredSpainCities.isNotEmpty) ...[
                      Padding(
                        padding: const EdgeInsets.only(left: 4, bottom: 8, top: 4),
                        child: Text(
                          _query.isEmpty
                              ? 'US CITIES (${filteredSpainCities.length})'
                              : 'CIUDADES ENCONTRADAS (${filteredSpainCities.length})',
                          style: TextStyle(
                            color: (isDark ? Colors.white70 : Colors.black54)
                                .withValues(alpha: 0.6),
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                            letterSpacing: 1.1,
                          ),
                        ),
                      ),
                      ...filteredSpainCities.map(
                        (city) => _buildSpainCityItem(context, city, isDark),
                      ),
                    ],

                    // Section Header for Online Search Results
                    if (_query.trim().length >= 2) ...[
                      if (_isSearchingOnline)
                        const Padding(
                          padding: EdgeInsets.symmetric(vertical: 16),
                          child: Center(
                            child: SizedBox(
                              width: 24,
                              height: 24,
                              child: CircularProgressIndicator(
                                strokeWidth: 2.5,
                                color: Color(0xFF00B4D8),
                              ),
                            ),
                          ),
                        ),
                      if (nonDuplicateOnline.isNotEmpty) ...[
                        const SizedBox(height: 12),
                        Padding(
                          padding: const EdgeInsets.only(left: 4, bottom: 8),
                          child: Text(
                            'OTRAS UBICACIONES / MÁS RESULTADOS'.toUpperCase(),
                            style: TextStyle(
                              color: (isDark ? Colors.white70 : Colors.black54)
                                  .withValues(alpha: 0.6),
                              fontSize: 11,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 1.1,
                            ),
                          ),
                        ),
                        ...nonDuplicateOnline.map(
                          (loc) => _buildOnlineCityItem(context, loc, isDark),
                        ),
                      ],
                    ],

                    // Empty State
                    if (filteredSpainCities.isEmpty &&
                        nonDuplicateOnline.isEmpty &&
                        !_isSearchingOnline &&
                        _query.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 40),
                        child: Center(
                          child: Column(
                            children: [
                              const Text('🇺🇸', style: TextStyle(fontSize: 44)),
                              const SizedBox(height: 12),
                              Text(
                                '${l10n.noCitiesFound} "$_query"',
                                style: TextStyle(
                                  color: isDark ? Colors.white70 : Colors.black54,
                                  fontSize: 14,
                                  fontWeight: FontWeight.w500,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildSpainCityItem(
      BuildContext context, SpainCityItem city, bool isDark) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: EdgeInsets.zero,
        onTap: () => _selectCityItem(city.toLocationData()),
        child: ListTile(
          dense: true,
          contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
          leading: Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: const Color(0xFF00B4D8).withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Center(
              child: Text('🇺🇸', style: TextStyle(fontSize: 20)),
            ),
          ),
          title: Text(
            city.name,
            style: TextStyle(
              color: isDark ? Colors.white : Colors.black87,
              fontWeight: FontWeight.w700,
              fontSize: 15,
            ),
          ),
          subtitle: Text(
            '${city.province} · ${city.community}',
            style: TextStyle(
              color: isDark ? Colors.white60 : Colors.black54,
              fontSize: 12,
            ),
          ),
          trailing: const Icon(
            Icons.arrow_forward_ios_rounded,
            size: 14,
            color: Color(0xFF00B4D8),
          ),
        ),
      ),
    );
  }

  Widget _buildOnlineCityItem(
      BuildContext context, LocationData loc, bool isDark) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: EdgeInsets.zero,
        onTap: () => _selectCityItem(loc),
        child: ListTile(
          dense: true,
          contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
          leading: Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: const Color(0xFF7C4DFF).withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(
              Icons.location_city_rounded,
              color: Color(0xFF7C4DFF),
              size: 20,
            ),
          ),
          title: Text(
            loc.name,
            style: TextStyle(
              color: isDark ? Colors.white : Colors.black87,
              fontWeight: FontWeight.w600,
              fontSize: 15,
            ),
          ),
          subtitle: Text(
            [loc.admin1, loc.country].whereType<String>().join(', '),
            style: TextStyle(
              color: isDark ? Colors.white60 : Colors.black54,
              fontSize: 12,
            ),
          ),
          trailing: const Icon(
            Icons.arrow_forward_ios_rounded,
            size: 14,
            color: Color(0xFF7C4DFF),
          ),
        ),
      ),
    );
  }
}
