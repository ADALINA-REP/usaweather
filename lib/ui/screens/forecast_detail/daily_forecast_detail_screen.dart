// lib/ui/screens/forecast_detail/daily_forecast_detail_screen.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/core/theme/app_theme.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/ui/components/glass_card.dart';
import 'package:usaweather/ui/components/weather_chart_painters.dart';
import 'package:usaweather/ui/components/weather_icon.dart';
import 'package:usaweather/ui/widgets/sport_outdoor_widget.dart';
import 'package:usaweather/ui/screens/home/home_screen.dart';

/// Arguments passed to [DailyForecastDetailScreen]
class DailyForecastDetailArgs {
  final String cityName;
  final String selectedDate;
  final List<DailyWeatherDomain> dailyForecasts;
  final List<HourlyWeatherDomain> hourlyForecasts;
  final bool isCelsius;

  const DailyForecastDetailArgs({
    required this.cityName,
    required this.selectedDate,
    required this.dailyForecasts,
    required this.hourlyForecasts,
    required this.isCelsius,
  });
}

class DailyForecastDetailScreen extends StatefulWidget {
  final DailyForecastDetailArgs args;

  const DailyForecastDetailScreen({
    super.key,
    required this.args,
  });

  @override
  State<DailyForecastDetailScreen> createState() =>
      _DailyForecastDetailScreenState();
}

class _DailyForecastDetailScreenState extends State<DailyForecastDetailScreen> {
  late String _activeDate;
  late int _activeDayIndex;

  @override
  void initState() {
    super.initState();
    _activeDate = widget.args.selectedDate;
    _activeDayIndex = _findDayIndex(_activeDate);
  }

  @override
  void didUpdateWidget(covariant DailyForecastDetailScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.args.selectedDate != widget.args.selectedDate) {
      _activeDate = widget.args.selectedDate;
      _activeDayIndex = _findDayIndex(_activeDate);
    }
  }

  int _findDayIndex(String date) {
    final idx = widget.args.dailyForecasts.indexWhere((d) => d.time == date);
    return idx >= 0 ? idx : 0;
  }

  void _selectDay(int index) {
    if (index >= 0 && index < widget.args.dailyForecasts.length) {
      setState(() {
        _activeDayIndex = index;
        _activeDate = widget.args.dailyForecasts[index].time;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final isCelsius = widget.args.isCelsius;
    final daily = widget.args.dailyForecasts[_activeDayIndex];

    // Filter hourly data specifically for this selected day (24 hours)
    final dayHourly = widget.args.hourlyForecasts
        .where((h) => h.time.startsWith(_activeDate))
        .toList();

    // High-contrast color tokens (mirroring HomeScreen UhdTheme)
    final titleColor = isDark ? Colors.white : const Color(0xFF0F172A);
    final valueColor = isDark ? Colors.white : const Color(0xFF0F172A);
    final labelColor =
        isDark ? const Color(0xFF94A3B8) : const Color(0xFF334155);
    const subtitleColor = Color(0xFF64748B);
    final podBg = isDark
        ? const Color(0xFF1E2E4A).withValues(alpha: 0.65)
        : const Color(0xFFF1F5F9);
    final accentCyan =
        isDark ? const Color(0xFF38BDF8) : const Color(0xFF0284C7);
    final accentPill =
        isDark ? const Color(0xFF1E2E5D) : const Color(0xFFE0F2FE);
    final accentPillText = isDark ? Colors.white : const Color(0xFF0369A1);
    final cardDivider = isDark
        ? Colors.white.withValues(alpha: 0.08)
        : const Color(0xFFCBD5E1).withValues(alpha: 0.6);

    final maxTemp = isCelsius
        ? daily.maxTemp.round()
        : (daily.maxTemp * 9 / 5 + 32).round();
    final minTemp = isCelsius
        ? daily.minTemp.round()
        : (daily.minTemp * 9 / 5 + 32).round();
    final apparentMax = isCelsius
        ? daily.apparentTempMax.round()
        : (daily.apparentTempMax * 9 / 5 + 32).round();
    final unitStr = isCelsius ? '°C' : '°F';

    final l10n = AppLocalizations.of(context);
    final conditionDesc = l10n.getWeatherCondition(daily.weatherCode);

    // Weather metrics calculation
    final avgHumidity = dayHourly.isNotEmpty
        ? (dayHourly.map((h) => h.humidity ?? 65).reduce((a, b) => a + b) /
                dayHourly.length)
            .round()
        : 65;
    final avgPressure = dayHourly.isNotEmpty
        ? (dayHourly.map((h) => h.pressure).reduce((a, b) => a + b) /
            dayHourly.length)
        : 1013.2;
    final temps = dayHourly.map((h) => h.temperature).toList();
    final avgVisibilityKm = dayHourly.isNotEmpty
        ? (dayHourly.map((h) => h.visibility).reduce((a, b) => a + b) /
                dayHourly.length /
                1000)
            .toStringAsFixed(1)
        : '18.7';
    final gusts = (daily.windSpeedMax * 1.35).round();
    final windDirection = dayHourly.isNotEmpty
        ? dayHourly[dayHourly.length ~/ 2].windDirection
        : 180;
    final uv = daily.uvIndex;

    // European AQI Calculation
    final aqiIndex = (28 +
            (daily.weatherCode > 40 ? 15 : 0) +
            (daily.windSpeedMax > 25 ? 8 : 0))
        .clamp(15, 80);
    final (aqiStatus, aqiColor) = _aqiLabel(aqiIndex, l10n);

    // Solar cycle calculations
    final now = DateTime.now();
    final isNight = now.hour < 7 || now.hour >= 20;
    final progress = _solarProgress(daily.sunrise, daily.sunset);
    final sunriseStr = _formatTimeAmPm(daily.sunrise) ?? '7:50 AM';
    final sunsetStr = _formatTimeAmPm(daily.sunset) ?? '8:54 PM';

    return Scaffold(
      body: Container(
        decoration: _weatherGradient(daily.weatherCode, isDark),
        child: SafeArea(
          child: Column(
            children: [
              // ─── Top App Bar ─────────────────────────────────────────────
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Row(
                  children: [
                    // Back Button
                    GestureDetector(
                      onTap: () => Navigator.of(context).pop(),
                      child: Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          color: isDark
                              ? Colors.white.withValues(alpha: 0.12)
                              : Colors.white.withValues(alpha: 0.85),
                          shape: BoxShape.circle,
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withValues(alpha: 0.1),
                              blurRadius: 10,
                              offset: const Offset(0, 2),
                            ),
                          ],
                        ),
                        child: Center(
                          child: Icon(
                            Icons.arrow_back_ios_new_rounded,
                            size: 18,
                            color:
                                isDark ? Colors.white : const Color(0xFF0F172A),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),

                    // City Name & Date Subtitle
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              const Text('🇺🇸 ',
                                  style: TextStyle(fontSize: 16)),
                              Flexible(
                                child: Text(
                                  widget.args.cityName,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 20,
                                    fontWeight: FontWeight.w800,
                                    shadows: [
                                      Shadow(
                                        color: Colors.black38,
                                        blurRadius: 8,
                                      ),
                                    ],
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                          Text(
                            _formatSelectedDate(_activeDate, l10n),
                            style: TextStyle(
                              color: Colors.white.withValues(alpha: 0.85),
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // ─── Horizontal 7-Day Switcher Tabs ──────────────────────────
              SizedBox(
                height: 52,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: widget.args.dailyForecasts.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (context, i) {
                    final d = widget.args.dailyForecasts[i];
                    final isSelected = i == _activeDayIndex;
                    final dt = DateTime.tryParse(d.time);
                    final today = DateTime.now();
                    final isToday = dt != null &&
                        dt.year == today.year &&
                        dt.month == today.month &&
                        dt.day == today.day;
                    final dayTitle = isToday
                        ? l10n.today
                        : (dt != null
                            ? DateFormat.E(l10n.locale.languageCode).format(dt)
                            : d.time);

                    return GestureDetector(
                      onTap: () => _selectDay(i),
                      child: GlassCard(
                        isSelected: isSelected,
                        borderRadius: 16,
                        padding: const EdgeInsets.symmetric(
                            horizontal: 14, vertical: 8),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              dayTitle,
                              style: TextStyle(
                                color: isSelected
                                    ? Colors.white
                                    : (isDark
                                        ? Colors.white
                                        : const Color(0xFF0F172A)),
                                fontWeight: isSelected
                                    ? FontWeight.w800
                                    : FontWeight.w600,
                                fontSize: 13,
                              ),
                            ),
                            const SizedBox(width: 6),
                            WeatherIcon(
                              weatherCode: d.weatherCode,
                              isDay: 1,
                              size: 18,
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),

              // ─── Scrollable Day Weather Details ─
              Expanded(
                child: ListView(
                  key: const ValueKey('daily_detail_listview'),
                  physics: const BouncingScrollPhysics(),
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
                  children: [
                    // 1. Hero Card for Selected Day
                    GlassCard(
                      borderRadius: 28,
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    _formatDayFullDate(_activeDate, l10n),
                                    style: TextStyle(
                                      color: labelColor,
                                      fontSize: 14,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    conditionDesc,
                                    style: TextStyle(
                                      color: titleColor,
                                      fontSize: 22,
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  Text(
                                    '${l10n.feelsLike}: $apparentMax$unitStr',
                                    style: const TextStyle(
                                      color: subtitleColor,
                                      fontSize: 13,
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                ],
                              ),
                              WeatherIcon(
                                weatherCode: daily.weatherCode,
                                isDay: 1,
                                size: 76,
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          Divider(height: 1, color: cardDivider),
                          const SizedBox(height: 16),
                          // Min / Max Temp Bar
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceAround,
                            children: [
                              _buildHeroMetric(
                                label: l10n.minTemp,
                                value: '$minTemp$unitStr',
                                icon: Icons.arrow_downward_rounded,
                                iconColor: const Color(0xFF0284C7),
                                valueColor: valueColor,
                                labelColor: labelColor,
                              ),
                              Container(
                                width: 1,
                                height: 40,
                                color: cardDivider,
                              ),
                              _buildHeroMetric(
                                label: l10n.maxTemp,
                                value: '$maxTemp$unitStr',
                                icon: Icons.arrow_upward_rounded,
                                iconColor: const Color(0xFFEF4444),
                                valueColor: valueColor,
                                labelColor: labelColor,
                              ),
                              Container(
                                width: 1,
                                height: 40,
                                color: cardDivider,
                              ),
                              _buildHeroMetric(
                                label: l10n.rain,
                                value: '${daily.precipitationProbability}%',
                                icon: Icons.water_drop_rounded,
                                iconColor: const Color(0xFF2563EB),
                                valueColor: valueColor,
                                labelColor: labelColor,
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 14),

                    // 2. 24-Hour Breakdown
                    if (dayHourly.isNotEmpty) ...[
                      GlassCard(
                        borderRadius: 28,
                        padding: const EdgeInsets.all(18),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                _buildIconBadge(
                                    Icons.schedule_rounded,
                                    const Color(0xFF6366F1),
                                    32,
                                    18),
                                const SizedBox(width: 10),
                                Text(
                                  l10n.hourlyForecastTitle,
                                  style: TextStyle(
                                    color: titleColor,
                                    fontWeight: FontWeight.w700,
                                    fontSize: 16,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 16),
                            SizedBox(
                              height: 176,
                              child: SingleChildScrollView(
                                scrollDirection: Axis.horizontal,
                                physics: const BouncingScrollPhysics(),
                                child: Stack(
                                  children: [
                                    // Capsule Pods Row that scrolls synchronously with the line
                                    Row(
                                      children: [
                                        for (int i = 0; i < dayHourly.length; i++) ...[
                                          () {
                                            final h = dayHourly[i];
                                            final dt = DateTime.tryParse(h.time);
                                            final hourStr = dt != null
                                                ? DateFormat('HH:mm').format(dt)
                                                : h.time;
                                            final hTemp = isCelsius
                                                ? h.temperature.round()
                                                : (h.temperature * 9 / 5 + 32)
                                                    .round();

                                            return Container(
                                              width: 68,
                                              height: 174,
                                              padding: const EdgeInsets.symmetric(
                                                  vertical: 12, horizontal: 4),
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
                                                mainAxisAlignment:
                                                    MainAxisAlignment.spaceBetween,
                                                children: [
                                                  // Temperature — hero at top
                                                  Text(
                                                    '$hTemp$unitStr',
                                                    style: TextStyle(
                                                      color: valueColor,
                                                      fontWeight: FontWeight.w800,
                                                      fontSize: 17,
                                                    ),
                                                  ),
                                                  // Time label
                                                  Text(
                                                    hourStr,
                                                    style: TextStyle(
                                                      color: labelColor,
                                                      fontWeight: FontWeight.w600,
                                                      fontSize: 11,
                                                    ),
                                                  ),
                                                  WeatherIcon(
                                                    weatherCode: h.weatherCode,
                                                    isDay: h.isDay,
                                                    size: 26,
                                                  ),
                                                  // Line dot zone
                                                  const SizedBox(height: 18),
                                                  Row(
                                                    mainAxisAlignment:
                                                        MainAxisAlignment.center,
                                                    children: [
                                                      Icon(
                                                        Icons.water_drop_rounded,
                                                        size: 11,
                                                        color: accentCyan,
                                                      ),
                                                      const SizedBox(width: 2),
                                                      Text(
                                                        '${h.precipitationProbability}%',
                                                        style: TextStyle(
                                                          color: accentCyan,
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
                                          if (i < dayHourly.length - 1)
                                            const SizedBox(width: 8),
                                        ],
                                      ],
                                    ),

                                    // Golden trend line (FOREGROUND overlay — on top of pods!)
                                    Positioned(
                                      left: 0,
                                      top: 0,
                                      width: dayHourly.length * 76.0,
                                      height: 174,
                                      child: IgnorePointer(
                                        child: CustomPaint(
                                          size: Size(dayHourly.length * 76.0, 174),
                                          painter: HourlyTempLinePainter(
                                            temperatures: temps,
                                            itemWidth: 76.0,
                                            podWidth: 68.0,
                                            lineColor: const Color(0xFFFBBF24),
                                            isDark: isDark,
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
                      const SizedBox(height: 14),
                    ],

                    // ─── 3. Health & Environment Section (Screenshot 2) ───────
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.fromLTRB(4, 16, 4, 8),
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
                        Row(
                          children: [
                            // AQI Card
                            Expanded(
                              child: GlassCard(
                                borderRadius: 28,
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(
                                      'AQI($aqiStatus)',
                                      style: TextStyle(
                                        color: labelColor,
                                        fontSize: 13,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    const SizedBox(height: 10),
                                    Text(
                                      '$aqiStatus ($aqiIndex)',
                                      style: TextStyle(
                                        color: valueColor,
                                        fontSize: 20,
                                        fontWeight: FontWeight.w800,
                                      ),
                                    ),
                                    const SizedBox(height: 18),
                                    Container(
                                      height: 6,
                                      decoration: BoxDecoration(
                                        color: isDark
                                            ? podBg
                                            : const Color(0xFFE2E8F0),
                                        borderRadius: BorderRadius.circular(3),
                                      ),
                                      child: FractionallySizedBox(
                                        widthFactor: (aqiIndex / 100.0)
                                            .clamp(0.1, 1.0),
                                        alignment: Alignment.centerLeft,
                                        child: Container(
                                          decoration: BoxDecoration(
                                            color: aqiColor,
                                            borderRadius:
                                                BorderRadius.circular(3),
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
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceBetween,
                                  children: [
                                    Row(
                                      children: [
                                        Icon(
                                          Icons.water_drop_rounded,
                                          size: 14,
                                          color: accentCyan,
                                        ),
                                        const SizedBox(width: 4),
                                        Text(
                                          l10n.humidity,
                                          style: TextStyle(
                                            color: labelColor,
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
                                          waveColor: accentCyan,
                                          fillColor: accentCyan
                                              .withValues(alpha: 0.18),
                                        ),
                                      ),
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      '$avgHumidity%: ${l10n.steady}',
                                      style: TextStyle(
                                        color: valueColor,
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
                      ],
                    ),
                    const SizedBox(height: 14),

                    // ─── 4. 2-Column Details Grid (Screenshots 2 & 3) ─────────
                    GridView.count(
                      crossAxisCount: 2,
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      crossAxisSpacing: 10,
                      mainAxisSpacing: 10,
                      childAspectRatio: 1.05,
                      children: [
                        // 1. PRESSURE
                        GlassCard(
                          borderRadius: 28,
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Icon(Icons.speed_rounded,
                                      size: 16, color: accentCyan),
                                  const SizedBox(width: 6),
                                  Text(
                                    l10n.pressure,
                                    style: TextStyle(
                                      color: labelColor,
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
                                          pressure: avgPressure,
                                          isDark: isDark,
                                        ),
                                      ),
                                      Column(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Text(
                                            '${avgPressure.round()}',
                                            style: TextStyle(
                                              color: valueColor,
                                              fontSize: 18,
                                              fontWeight: FontWeight.w800,
                                            ),
                                          ),
                                          const Text(
                                            'mb',
                                            style: TextStyle(
                                              color: subtitleColor,
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
                                  style: const TextStyle(
                                    color: subtitleColor,
                                    fontSize: 11,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),

                        // 2. UV INDEX
                        GlassCard(
                          borderRadius: 28,
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.spaceBetween,
                                children: [
                                  Row(
                                    children: [
                                      const Icon(Icons.wb_sunny_rounded,
                                          size: 15, color: Color(0xFFFBBF24)),
                                      const SizedBox(width: 6),
                                      Text(
                                        l10n.uvIndex,
                                        style: TextStyle(
                                          color: labelColor,
                                          fontSize: 11,
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                    ],
                                  ),
                                  Text(
                                    uv.toStringAsFixed(1),
                                    style: TextStyle(
                                      color: valueColor,
                                      fontSize: 15,
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                                ],
                              ),
                              Text(
                                _uvStatus(uv, l10n),
                                style: TextStyle(
                                  color: valueColor,
                                  fontSize: 20,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    '${uv.round()}',
                                    style: const TextStyle(
                                      color: subtitleColor,
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
                                        isDark: isDark,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),

                        // 3. WIND
                        GlassCard(
                          borderRadius: 28,
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '${l10n.wind} : ${daily.windSpeedMax.round()} km/h',
                                style: TextStyle(
                                  color: labelColor,
                                  fontSize: 11,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              Expanded(
                                child: Center(
                                  child: CustomPaint(
                                    size: const Size(68, 68),
                                    painter: WindCompassPainter(
                                      degrees: windDirection,
                                      isDark: isDark,
                                    ),
                                  ),
                                ),
                              ),
                              Center(
                                child: Text(
                                  '${l10n.windGusts}: $gusts km/h',
                                  style: const TextStyle(
                                    color: subtitleColor,
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
                                  const Icon(Icons.remove_red_eye_rounded,
                                      size: 15, color: Color(0xFFF97316)),
                                  const SizedBox(width: 6),
                                  Text(
                                    l10n.visibility,
                                    style: TextStyle(
                                      color: labelColor,
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
                                    '$avgVisibilityKm km',
                                    style: TextStyle(
                                      color: valueColor,
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
                                    style: const TextStyle(
                                      color: subtitleColor,
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
                                  const Icon(Icons.eco_rounded,
                                      size: 15, color: Color(0xFF22C55E)),
                                  const SizedBox(width: 6),
                                  Text(
                                    l10n.pollenAllergy,
                                    style: TextStyle(
                                      color: labelColor,
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
                                    style: const TextStyle(
                                      color: subtitleColor,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  Container(
                                    height: 4,
                                    decoration: BoxDecoration(
                                      color: isDark
                                          ? podBg
                                          : const Color(0xFFE2E8F0),
                                      borderRadius: BorderRadius.circular(2),
                                    ),
                                    child: FractionallySizedBox(
                                      widthFactor: 0.25,
                                      alignment: Alignment.centerLeft,
                                      child: Container(
                                        decoration: BoxDecoration(
                                          color: const Color(0xFF22C55E),
                                          borderRadius:
                                              BorderRadius.circular(2),
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
                                  Icon(Icons.checkroom_rounded,
                                      size: 15, color: accentCyan),
                                  const SizedBox(width: 6),
                                  Text(
                                    l10n.clothingSuggestion,
                                    style: TextStyle(
                                      color: labelColor,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w700,
                                    ),
                                  ),
                                ],
                              ),
                              Row(
                                children: [
                                  Icon(
                                    daily.maxTemp < 16
                                        ? Icons.dry_cleaning_rounded
                                        : Icons.person_rounded,
                                    size: 32,
                                    color: isDark
                                        ? const Color(0xFF93C5FD)
                                        : const Color(0xFF0284C7),
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      _clothingSuggestion(daily.maxTemp, l10n),
                                      style: TextStyle(
                                        color: valueColor,
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
                                style: const TextStyle(
                                  color: subtitleColor,
                                  fontSize: 10,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),

                    // ─── 5. Solar Cycle Card (Screenshot 3) ───────────────────
                    GlassCard(
                      borderRadius: 28,
                      padding: const EdgeInsets.all(18),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              _buildIconBadge(Icons.wb_sunny_rounded,
                                  const Color(0xFFF59E0B), 28, 16),
                              const SizedBox(width: 8),
                              Text(
                                l10n.solarCycle,
                                style: TextStyle(
                                  color: labelColor,
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
                                isDark: isDark,
                              ),
                            ),
                          ),
                          const SizedBox(height: 14),

                          // Bottom Row: Sunrise / Center Pill / Sunset
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    l10n.sunrise.toUpperCase(),
                                    style: const TextStyle(
                                      color: subtitleColor,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w700,
                                      letterSpacing: 0.5,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    sunriseStr,
                                    style: TextStyle(
                                      color: valueColor,
                                      fontSize: 18,
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                                ],
                              ),

                              // Center Pill Badge
                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 14, vertical: 6),
                                decoration: BoxDecoration(
                                  color: accentPill,
                                  borderRadius: BorderRadius.circular(16),
                                  border: Border.all(
                                    color: isDark
                                        ? Colors.transparent
                                        : const Color(0xFFBAE6FD),
                                    width: 1,
                                  ),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Icon(
                                      isNight
                                          ? Icons.nightlight_round
                                          : Icons.wb_sunny_rounded,
                                      size: 14,
                                      color: isNight
                                          ? (isDark
                                              ? const Color(0xFFFBBF24)
                                              : const Color(0xFFD97706))
                                          : const Color(0xFFF59E0B),
                                    ),
                                    const SizedBox(width: 6),
                                    Text(
                                      isNight ? l10n.nightPill : l10n.dayPill,
                                      style: TextStyle(
                                        color: accentPillText,
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
                                    style: const TextStyle(
                                      color: subtitleColor,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w700,
                                      letterSpacing: 0.5,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    sunsetStr,
                                    style: TextStyle(
                                      color: valueColor,
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
                    const SizedBox(height: 14),

                    // ─── 6. Sport & Outdoor Section (Interactive Multi-Sport) ────────────
                    SportOutdoorWidget(
                      hourly: dayHourly,
                      uhd: UhdTheme(isDark),
                      isDark: isDark,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeroMetric({
    required String label,
    required String value,
    required IconData icon,
    required Color iconColor,
    required Color valueColor,
    required Color labelColor,
  }) {
    return Column(
      children: [
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 16, color: iconColor),
            const SizedBox(width: 4),
            Text(
              label,
              style: TextStyle(
                color: labelColor,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            color: valueColor,
            fontSize: 20,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }

  Widget _buildIconBadge(
      IconData icon, Color color, double size, double iconSize) {
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

  BoxDecoration _weatherGradient(int code, bool isDark) {
    if (!isDark) {
      return const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Color(0xFF38BDF8),
            Color(0xFF0284C7),
          ],
        ),
      );
    }
    return const BoxDecoration(
      gradient: LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [
          Color(0xFF0A0E2E),
          Color(0xFF1E1B4B),
        ],
      ),
    );
  }

  String _formatSelectedDate(String rawDate, [AppLocalizations? l10n]) {
    final dt = DateTime.tryParse(rawDate);
    if (dt == null) return rawDate;
    final lang = l10n?.locale.languageCode ?? 'es';
    return DateFormat('EEEE, d MMMM', lang).format(dt);
  }

  String _formatDayFullDate(String rawDate, [AppLocalizations? l10n]) {
    final dt = DateTime.tryParse(rawDate);
    if (dt == null) return rawDate;
    final lang = l10n?.locale.languageCode ?? 'es';
    return DateFormat('EEEE d', lang).format(dt);
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
