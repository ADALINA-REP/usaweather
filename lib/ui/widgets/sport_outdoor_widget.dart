import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/data/models/weather_domain.dart';
import 'package:usaweather/ui/components/glass_card.dart';
import 'package:usaweather/ui/screens/home/home_screen.dart';

enum SportType {
  running,
  hiking,
  cycling,
  surfing,
  skiing,
  golf,
  tennis,
}

class SportOutdoorWidget extends StatefulWidget {
  final List<HourlyWeatherDomain> hourly;
  final UhdTheme uhd;
  final bool isDark;

  const SportOutdoorWidget({
    super.key,
    required this.hourly,
    required this.uhd,
    this.isDark = true,
  });

  @override
  State<SportOutdoorWidget> createState() => _SportOutdoorWidgetState();
}

class _SportOutdoorWidgetState extends State<SportOutdoorWidget> {
  SportType _selectedSport = SportType.running;

  List<SportItem> _getSportsList(AppLocalizations l10n) {
    return [
      SportItem(type: SportType.running, name: l10n.running, emoji: '🏃'),
      SportItem(type: SportType.hiking, name: l10n.hiking, emoji: '🥾'),
      SportItem(type: SportType.cycling, name: l10n.cycling, emoji: '🚴'),
      SportItem(type: SportType.surfing, name: l10n.surfing, emoji: '🏄'),
      SportItem(type: SportType.skiing, name: l10n.skiing, emoji: '🎿'),
      SportItem(type: SportType.golf, name: l10n.golf, emoji: '⛳'),
      SportItem(type: SportType.tennis, name: l10n.tennis, emoji: '🎾'),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final sports = _getSportsList(l10n);
    final activeSportItem = sports.firstWhere(
      (s) => s.type == _selectedSport,
      orElse: () => sports.first,
    );

    final now = DateTime.now();
    // Filter future hours and take up to 12 future hours
    final upcomingHours = widget.hourly
        .where((h) => DateTime.tryParse(h.time)?.isAfter(now) ?? false)
        .take(12)
        .toList();

    // Fallback if list is empty (e.g. night time or end of day dataset)
    final hoursToShow = upcomingHours.isNotEmpty
        ? upcomingHours
        : widget.hourly.take(12).toList();

    final currentEval = _evaluateCondition(
      _selectedSport,
      hoursToShow.isNotEmpty ? hoursToShow.first : null,
    );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Title
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 10),
          child: Row(
            children: [
              Text(
                l10n.sportOutdoor,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.3,
                ),
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: const Color(0xFF00B4D8).withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: const Color(0xFF00B4D8).withValues(alpha: 0.3),
                    width: 1,
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(
                      Icons.schedule_rounded,
                      size: 13,
                      color: Color(0xFF00B4D8),
                    ),
                    const SizedBox(width: 4),
                    Text(
                      '12h Forecast',
                      style: TextStyle(
                        color: widget.uhd.accentCyan,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),

        // Sports Selector Bar
        SizedBox(
          height: 42,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            scrollDirection: Axis.horizontal,
            itemCount: sports.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, index) {
              final sport = sports[index];
              final isSelected = sport.type == _selectedSport;

              return Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: () {
                    setState(() => _selectedSport = sport.type);
                  },
                  borderRadius: BorderRadius.circular(20),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                    decoration: BoxDecoration(
                      gradient: isSelected
                          ? const LinearGradient(
                              colors: [Color(0xFF00B4D8), Color(0xFF0077B6)],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                            )
                          : null,
                      color: isSelected
                          ? null
                          : (widget.uhd.isDark
                              ? Colors.white.withValues(alpha: 0.08)
                              : Colors.white.withValues(alpha: 0.7)),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: isSelected
                            ? Colors.transparent
                            : (widget.uhd.isDark
                                ? Colors.white.withValues(alpha: 0.15)
                                : const Color(0xFFCBD5E1)),
                        width: 1,
                      ),
                      boxShadow: isSelected
                          ? [
                              BoxShadow(
                                color:
                                    const Color(0xFF00B4D8).withValues(alpha: 0.35),
                                blurRadius: 8,
                                offset: const Offset(0, 3),
                              )
                            ]
                          : [],
                    ),
                    child: Row(
                      children: [
                        Text(
                          sport.emoji,
                          style: const TextStyle(fontSize: 16),
                        ),
                        const SizedBox(width: 6),
                        Text(
                          sport.name,
                          style: TextStyle(
                            color: isSelected
                                ? Colors.white
                                : widget.uhd.cardTitle,
                            fontSize: 13,
                            fontWeight: isSelected
                                ? FontWeight.w800
                                : FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),

        const SizedBox(height: 12),

        // Main Card
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: GlassCard(
            borderRadius: 28,
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header (Sport Avatar + Title + Dynamic Condition Pill)
                Row(
                  children: [
                    Container(
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          colors: [
                            widget.uhd.accentCyan.withValues(alpha: 0.25),
                            widget.uhd.accentCyan.withValues(alpha: 0.08),
                          ],
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                        ),
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: widget.uhd.accentCyan.withValues(alpha: 0.4),
                          width: 1.5,
                        ),
                      ),
                      child: Center(
                        child: Text(
                          activeSportItem.emoji,
                          style: const TextStyle(fontSize: 24),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          activeSportItem.name,
                          style: TextStyle(
                            color: widget.uhd.cardTitle,
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        Text(
                          currentEval.tagline,
                          style: TextStyle(
                            color: widget.uhd.cardSubtitle,
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                    const Spacer(),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 10, vertical: 5),
                      decoration: BoxDecoration(
                        color: currentEval.badgeColor.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: currentEval.badgeColor.withValues(alpha: 0.4),
                          width: 1,
                        ),
                      ),
                      child: Text(
                        currentEval.statusPill,
                        style: TextStyle(
                          color: currentEval.badgeColor,
                          fontSize: 11,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 18),

                // 12-Hour Scrollable Timeline
                SizedBox(
                  height: 105,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: hoursToShow.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 12),
                    itemBuilder: (context, idx) {
                      final h = hoursToShow[idx];
                      final dt = DateTime.tryParse(h.time);
                      final timeLabel = dt != null
                          ? DateFormat('h a').format(dt)
                          : '${(now.hour + idx + 1) % 24}:00';

                      final eval = _evaluateCondition(_selectedSport, h);

                      return Container(
                        width: 72,
                        padding: const EdgeInsets.symmetric(
                            vertical: 8, horizontal: 6),
                        decoration: BoxDecoration(
                          color: widget.uhd.isDark
                              ? Colors.white.withValues(alpha: 0.05)
                              : const Color(0xFFF8FAFC),
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                            color: widget.uhd.isDark
                                ? Colors.white.withValues(alpha: 0.08)
                                : const Color(0xFFE2E8F0),
                            width: 1,
                          ),
                        ),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              timeLabel,
                              style: TextStyle(
                                color: widget.uhd.cardLabel,
                                fontSize: 10,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                            Text(
                              eval.emoji,
                              style: const TextStyle(fontSize: 22),
                            ),
                            Text(
                              eval.ratingText.toUpperCase(),
                              style: TextStyle(
                                color: eval.badgeColor,
                                fontSize: 8,
                                fontWeight: FontWeight.w800,
                                letterSpacing: 0.3,
                              ),
                            ),
                            Text(
                              '${h.temperature.round()}°',
                              style: TextStyle(
                                color: widget.uhd.cardTitle,
                                fontSize: 11,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                ),

                const SizedBox(height: 16),

                // Footer Summary
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: widget.uhd.isDark
                        ? Colors.white.withValues(alpha: 0.04)
                        : const Color(0xFFF1F5F9),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      Icon(
                        Icons.info_outline_rounded,
                        size: 16,
                        color: widget.uhd.accentCyan,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: RichText(
                          text: TextSpan(
                            children: [
                              TextSpan(
                                text: '${currentEval.ratingText}: ',
                                style: TextStyle(
                                  color: currentEval.badgeColor,
                                  fontWeight: FontWeight.w800,
                                  fontSize: 13,
                                ),
                              ),
                              TextSpan(
                                text: currentEval.description,
                                style: TextStyle(
                                  color: widget.uhd.cardSubtitle,
                                  fontWeight: FontWeight.w500,
                                  fontSize: 13,
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
            ),
          ),
        ),
      ],
    );
  }

  // ─── Weather Condition Evaluator Per Sport ─────────────────────────────────
  ConditionEval _evaluateCondition(SportType sport, HourlyWeatherDomain? h) {
    if (h == null) {
      return ConditionEval(
        emoji: '🤩',
        ratingText: 'Excellent',
        badgeColor: const Color(0xFF00B4D8),
        statusPill: 'Good Conditions',
        tagline: 'Optimal weather',
        description: 'Favorable outdoor weather conditions.',
      );
    }

    final temp = h.temperature;
    final wind = h.windSpeed;
    final rain = h.precipitationProbability;
    final humidity = h.humidity ?? 50;

    switch (sport) {
      case SportType.running:
        if (rain > 60 || temp > 34) {
          return ConditionEval(
            emoji: '🌧️',
            ratingText: 'Poor',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Rain / Extreme Heat 🌧️',
            tagline: 'High rain or heat risk',
            description: 'Unfavorable running conditions due to rain/heat.',
          );
        } else if (humidity > 75 || temp > 28) {
          return ConditionEval(
            emoji: '😐',
            ratingText: 'Fair',
            badgeColor: const Color(0xFFFFB703),
            statusPill: 'High Humidity 💧',
            tagline: 'Stay hydrated',
            description: 'Moderate conditions with high humidity.',
          );
        } else {
          return ConditionEval(
            emoji: '🤩',
            ratingText: 'Excellent',
            badgeColor: const Color(0xFF00B4D8),
            statusPill: 'Optimal Air 👟',
            tagline: 'Perfect temperature',
            description: 'Ideal conditions for running right now.',
          );
        }

      case SportType.hiking:
        if (rain > 40 || wind > 40) {
          return ConditionEval(
            emoji: '⛈️',
            ratingText: 'Unfair',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'High Wind / Rain 💨',
            tagline: 'Trail safety warning',
            description: 'Difficult mountain and trail weather conditions.',
          );
        } else if (temp > 30 || rain > 20) {
          return ConditionEval(
            emoji: '😊',
            ratingText: 'Good',
            badgeColor: const Color(0xFFFFB703),
            statusPill: 'Moderate Trail 🥾',
            tagline: 'Good trail visibility',
            description: 'Good hiking conditions, bring extra water.',
          );
        } else {
          return ConditionEval(
            emoji: '🤩',
            ratingText: 'Ideal',
            badgeColor: const Color(0xFF2EC4B6),
            statusPill: 'Clear Trails 🌲',
            tagline: 'Crisp mountain air',
            description: 'Clear skies and ideal temperature for hiking.',
          );
        }

      case SportType.cycling:
        if (rain > 30 || wind > 35) {
          return ConditionEval(
            emoji: '🌬️',
            ratingText: 'Poor',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Strong Headwind 💨',
            tagline: 'Wet / windy road',
            description: 'High wind gusts and slippery road conditions.',
          );
        } else if (wind > 20) {
          return ConditionEval(
            emoji: '😐',
            ratingText: 'Fair',
            badgeColor: const Color(0xFFFFB703),
            statusPill: 'Breezy Road 🚴',
            tagline: 'Moderate wind',
            description: 'Fair cycling conditions with noticeable wind.',
          );
        } else {
          return ConditionEval(
            emoji: '🤩',
            ratingText: 'Excellent',
            badgeColor: const Color(0xFF00B4D8),
            statusPill: 'Low Wind 🚴',
            tagline: 'Dry tarmac',
            description: 'Smooth riding with calm winds and dry roads.',
          );
        }

      case SportType.surfing:
        if (wind > 20 && wind < 45) {
          return ConditionEval(
            emoji: '🏄',
            ratingText: 'Epic',
            badgeColor: const Color(0xFF00B4D8),
            statusPill: 'Good Offshore Wind 🌊',
            tagline: 'Great swell energy',
            description: 'Strong offshore breeze ideal for wave formation.',
          );
        } else if (wind >= 45) {
          return ConditionEval(
            emoji: '⚠️',
            ratingText: 'Rough',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Rough Sea 🌊',
            tagline: 'High choppy sea',
            description: 'Choppy water and hazardous wave conditions.',
          );
        } else {
          return ConditionEval(
            emoji: '😊',
            ratingText: 'Good',
            badgeColor: const Color(0xFF2EC4B6),
            statusPill: 'Light Swell 🌊',
            tagline: 'Calm waters',
            description: 'Fun, accessible waves for session surfers.',
          );
        }

      case SportType.skiing:
        if (temp < 2 && rain < 20) {
          return ConditionEval(
            emoji: '⛷️',
            ratingText: 'Excellent',
            badgeColor: const Color(0xFF00B4D8),
            statusPill: 'Freezing Powder ❄️',
            tagline: 'Sub-zero snow',
            description: 'Optimal snow cover and freezing temperatures.',
          );
        } else if (temp > 8) {
          return ConditionEval(
            emoji: '🫠',
            ratingText: 'Poor',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Snow Melting ☀️',
            tagline: 'Warm slush risk',
            description: 'High temperatures melting resort slopes.',
          );
        } else {
          return ConditionEval(
            emoji: '🎿',
            ratingText: 'Fair',
            badgeColor: const Color(0xFFFFB703),
            statusPill: 'Crisp Slopes 🎿',
            tagline: 'Moderate chill',
            description: 'Decent slope conditions with mild cold.',
          );
        }

      case SportType.golf:
        if (rain > 20 || wind > 25) {
          return ConditionEval(
            emoji: '🌧️',
            ratingText: 'Poor',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Rain / Wind Hazard ⛳',
            tagline: 'Unfavorable green',
            description: 'Wet fairways and high ball trajectory drift.',
          );
        } else {
          return ConditionEval(
            emoji: '⛳',
            ratingText: 'Excellent',
            badgeColor: const Color(0xFF2EC4B6),
            statusPill: 'Calm Fairways ⛳',
            tagline: 'Ideal green speed',
            description: 'Calm winds and dry fairways for golf.',
          );
        }

      case SportType.tennis:
        if (rain > 10) {
          return ConditionEval(
            emoji: '🛑',
            ratingText: 'Poor',
            badgeColor: const Color(0xFFFF4D4D),
            statusPill: 'Wet Court 🎾',
            tagline: 'Slippery hardcourt',
            description: 'Precipitation makes outdoor courts unplayable.',
          );
        } else if (wind > 20) {
          return ConditionEval(
            emoji: '😐',
            ratingText: 'Fair',
            badgeColor: const Color(0xFFFFB703),
            statusPill: 'Breezy Court 🎾',
            tagline: 'Wind drift',
            description: 'Dry court but wind affects ball trajectory.',
          );
        } else {
          return ConditionEval(
            emoji: '🎾',
            ratingText: 'Excellent',
            badgeColor: const Color(0xFF00B4D8),
            statusPill: 'Dry Court 🎾',
            tagline: 'Perfect rally weather',
            description: 'Dry surface and mild wind for tennis match.',
          );
        }
    }
  }
}

class SportItem {
  final SportType type;
  final String name;
  final String emoji;

  SportItem({
    required this.type,
    required this.name,
    required this.emoji,
  });
}

class ConditionEval {
  final String emoji;
  final String ratingText;
  final Color badgeColor;
  final String statusPill;
  final String tagline;
  final String description;

  ConditionEval({
    required this.emoji,
    required this.ratingText,
    required this.badgeColor,
    required this.statusPill,
    required this.tagline,
    required this.description,
  });
}
