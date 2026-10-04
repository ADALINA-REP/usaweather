// lib/ui/screens/cities/cities_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usaweather/core/constants/us_cities.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/core/router/app_router.dart';
import 'package:usaweather/data/models/location_data.dart';
import 'package:usaweather/data/repositories/weather_repository.dart';
import 'package:usaweather/data/repositories/settings_repository.dart';
import 'package:usaweather/ui/components/glass_card.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';

final _searchProvider = StateProvider<String>((ref) => '');
final _selectedRegionProvider = StateProvider<String>((ref) => 'All');

final _onlineResultsProvider =
    FutureProvider.family<List<LocationData>, String>((ref, query) async {
  if (query.trim().length < 2) return [];
  final repo = getIt<WeatherRepository>();
  final lang = getIt<SettingsRepository>().getSelectedLanguageCode();
  return repo.searchCity(query.trim(), language: lang);
});

class CitiesScreen extends ConsumerStatefulWidget {
  const CitiesScreen({super.key});

  @override
  ConsumerState<CitiesScreen> createState() => _CitiesScreenState();
}

class _CitiesScreenState extends ConsumerState<CitiesScreen> {
  final _ctrl = TextEditingController();

  static const List<String> _regions = [
    'All',
    'East',
    'West',
    'Midwest',
    'South',
    'Pacific & Offshore',
  ];

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final query = ref.watch(_searchProvider);
    final selectedRegion = ref.watch(_selectedRegionProvider);
    final onlineAsync = ref.watch(_onlineResultsProvider(query));
    final l10n = AppLocalizations.of(context);

    // Filter local US cities
    final filteredUsCities = UsCities.cities.where((city) {
      final matchesRegion =
          selectedRegion == 'All' || selectedRegion == 'Todas' || city.regionGroup == selectedRegion;
      final q = query.trim().toLowerCase();
      if (q.isEmpty) return matchesRegion;
      final matchesQuery = city.name.toLowerCase().contains(q) ||
          city.state.toLowerCase().contains(q) ||
          city.stateCode.toLowerCase().contains(q);
      return matchesQuery;
    }).toList();

    return Scaffold(
      backgroundColor:
          isDark ? const Color(0xFF0A0E2E) : const Color(0xFFF1F5F9),
      appBar: AppBar(
        title: Row(
          children: [
            const Text('🇺🇸 ', style: TextStyle(fontSize: 22)),
            Text(
              l10n.citiesTitle,
              style: TextStyle(
                color: isDark ? Colors.white : const Color(0xFF0F172A),
                fontWeight: FontWeight.w800,
                fontSize: 20,
              ),
            ),
          ],
        ),
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF131738) : Colors.white,
      ),
      body: Column(
        children: [
          // Search input
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
            child: Container(
              decoration: BoxDecoration(
                color: isDark
                    ? const Color(0xFF1E293B)
                    : Colors.white,
                borderRadius: BorderRadius.circular(24),
                border: Border.all(
                  color: isDark
                      ? Colors.white.withValues(alpha: 0.12)
                      : const Color(0xFFE2E8F0),
                  width: 1.0,
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: isDark ? 0.20 : 0.04),
                    blurRadius: 10,
                    offset: const Offset(0, 3),
                  ),
                ],
              ),
              child: TextField(
                controller: _ctrl,
                onChanged: (v) => ref.read(_searchProvider.notifier).state = v,
                style: TextStyle(color: isDark ? Colors.white : const Color(0xFF0F172A)),
                decoration: InputDecoration(
                  hintText: l10n.searchCitiesHint,
                  hintStyle: TextStyle(
                    color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                    fontSize: 14,
                  ),
                  prefixIcon: const Icon(
                    Icons.search_rounded,
                    color: Color(0xFF00B4D8),
                    size: 22,
                  ),
                  suffixIcon: query.isNotEmpty
                      ? IconButton(
                          icon: Icon(
                            Icons.clear_rounded,
                            color: isDark ? Colors.white70 : Colors.black54,
                            size: 18,
                          ),
                          onPressed: () {
                            _ctrl.clear();
                            ref.read(_searchProvider.notifier).state = '';
                          },
                        )
                      : null,
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                ),
              ),
            ),
          ),

          // Region filter chips (custom seamless pill styling - zero black borders)
          if (query.isEmpty) ...[
            SizedBox(
              height: 44,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _regions.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, i) {
                  final reg = _regions[i];
                  final isSelected = reg == selectedRegion;
                  final displayLabel = (reg == 'All' || reg == 'Todas') ? l10n.allRegions : reg;

                  return GestureDetector(
                    onTap: () {
                      ref.read(_selectedRegionProvider.notifier).state = reg;
                    },
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      decoration: BoxDecoration(
                        color: isSelected
                            ? const Color(0xFF00B4D8)
                            : (isDark
                                ? Colors.white.withValues(alpha: 0.08)
                                : Colors.white),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: isSelected
                              ? const Color(0xFF00B4D8)
                              : (isDark
                                  ? Colors.white.withValues(alpha: 0.12)
                                  : const Color(0xFFCBD5E1)),
                          width: 1.0,
                        ),
                        boxShadow: isSelected
                            ? [
                                BoxShadow(
                                  color: const Color(0xFF00B4D8).withValues(alpha: 0.35),
                                  blurRadius: 8,
                                  offset: const Offset(0, 2),
                                ),
                              ]
                            : null,
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (isSelected) ...[
                            const Icon(Icons.check_rounded, size: 14, color: Colors.white),
                            const SizedBox(width: 4),
                          ],
                          Text(
                            displayLabel,
                            style: TextStyle(
                              color: isSelected
                                  ? Colors.white
                                  : (isDark ? Colors.white70 : const Color(0xFF334155)),
                              fontWeight: isSelected ? FontWeight.w700 : FontWeight.w600,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 10),
          ],

          // Results list
          Expanded(
            child: ListView(
              physics: const BouncingScrollPhysics(),
              padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
              children: [
                // Header for US cities
                if (filteredUsCities.isNotEmpty) ...[
                  Padding(
                    padding: const EdgeInsets.only(left: 4, bottom: 8, top: 4),
                    child: Text(
                      query.isEmpty
                          ? '${selectedRegion == "All" ? l10n.allRegions : selectedRegion} (${filteredUsCities.length})'
                              .toUpperCase()
                          : '${l10n.citiesTitle} (${filteredUsCities.length})'
                              .toUpperCase(),
                      style: TextStyle(
                        color: (isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B)),
                        fontSize: 11,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 1.0,
                      ),
                    ),
                  ),
                  ...filteredUsCities.map((city) => _buildUsCityCard(
                        context,
                        city,
                        isDark,
                      )),
                ],

                // Online search results (if searching and online results returned)
                if (query.trim().length >= 2) ...[
                  const SizedBox(height: 16),
                  onlineAsync.when(
                    data: (results) {
                      final nonDuplicates = results.where((r) {
                        return !filteredUsCities.any((local) =>
                            local.name.toLowerCase() ==
                            r.name.toLowerCase());
                      }).toList();

                      if (nonDuplicates.isEmpty) {
                        return const SizedBox.shrink();
                      }

                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: const EdgeInsets.only(left: 4, bottom: 8),
                            child: Text(
                              'MORE RESULTS'.toUpperCase(),
                              style: TextStyle(
                                color: (isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B)),
                                fontSize: 11,
                                fontWeight: FontWeight.w800,
                                letterSpacing: 1.0,
                              ),
                            ),
                          ),
                          ...nonDuplicates.map((loc) => _buildOnlineCityCard(
                                context,
                                loc,
                                isDark,
                              )),
                        ],
                      );
                    },
                    loading: () => const Padding(
                      padding: EdgeInsets.symmetric(vertical: 24),
                      child: Center(
                        child: CircularProgressIndicator(
                          color: Color(0xFF00B4D8),
                          strokeWidth: 2.5,
                        ),
                      ),
                    ),
                    error: (_, __) => const SizedBox.shrink(),
                  ),
                ],

                // Empty state
                if (filteredUsCities.isEmpty && query.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 48),
                    child: Center(
                      child: Column(
                        children: [
                          const Text('🇺🇸', style: TextStyle(fontSize: 48)),
                          const SizedBox(height: 12),
                          Text(
                            '${l10n.noCitiesFound} "$query"',
                            style: TextStyle(
                              color: isDark ? Colors.white70 : const Color(0xFF64748B),
                              fontSize: 15,
                              fontWeight: FontWeight.w500,
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
    );
  }

  Widget _buildUsCityCard(
      BuildContext context, UsCityItem city, bool isDark) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: EdgeInsets.zero,
        borderRadius: 20,
        onTap: () {
          ref.read(homeProvider.notifier).selectCity(city.toLocationData());
          context.go(AppRoutes.home);
        },
        child: ListTile(
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          leading: Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: isDark
                  ? Colors.white.withValues(alpha: 0.10)
                  : const Color(0xFFF1F5F9),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: isDark
                    ? Colors.white.withValues(alpha: 0.12)
                    : const Color(0xFFE2E8F0),
                width: 1.0,
              ),
            ),
            child: const Center(
              child: Text('🇺🇸', style: TextStyle(fontSize: 20)),
            ),
          ),
          title: Text(
            city.name,
            style: TextStyle(
              color: isDark ? Colors.white : const Color(0xFF0F172A),
              fontWeight: FontWeight.w700,
              fontSize: 16,
            ),
          ),
          subtitle: Text(
            '${city.state} · ${city.stateCode}',
            style: TextStyle(
              color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
          trailing: Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: const Color(0xFF00B4D8).withValues(alpha: 0.12),
              shape: BoxShape.circle,
            ),
            child: const Center(
              child: Icon(
                Icons.arrow_forward_ios_rounded,
                size: 13,
                color: Color(0xFF00B4D8),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildOnlineCityCard(
      BuildContext context, LocationData loc, bool isDark) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: EdgeInsets.zero,
        borderRadius: 20,
        onTap: () {
          ref.read(homeProvider.notifier).selectCity(loc);
          context.go(AppRoutes.home);
        },
        child: ListTile(
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          leading: Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: const Color(0xFF7C4DFF).withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: const Color(0xFF7C4DFF).withValues(alpha: 0.30),
                width: 1.0,
              ),
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
              color: isDark ? Colors.white : const Color(0xFF0F172A),
              fontWeight: FontWeight.w700,
              fontSize: 16,
            ),
          ),
          subtitle: Text(
            [loc.admin1, loc.country].whereType<String>().join(', '),
            style: TextStyle(
              color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
              fontSize: 12,
            ),
          ),
          trailing: Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: const Color(0xFF7C4DFF).withValues(alpha: 0.12),
              shape: BoxShape.circle,
            ),
            child: const Center(
              child: Icon(
                Icons.arrow_forward_ios_rounded,
                size: 13,
                color: Color(0xFF7C4DFF),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
