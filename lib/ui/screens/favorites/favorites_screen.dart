// lib/ui/screens/favorites/favorites_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/core/router/app_router.dart';
import 'package:usaweather/data/repositories/location_repository.dart';
import 'package:usaweather/ui/components/glass_card.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';

class FavoritesScreen extends ConsumerWidget {
  const FavoritesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final l10n = AppLocalizations.of(context);
    final homeState = ref.watch(homeProvider);
    final favorites = homeState is WeatherSuccess
        ? homeState.favorites
        : getIt<LocationRepository>().getFavorites();

    return Scaffold(
      backgroundColor:
          isDark ? const Color(0xFF0A0E2E) : const Color(0xFFF1F5F9),
      appBar: AppBar(
        title: Text(
          l10n.favoritesTitle,
          style: TextStyle(
            color: isDark ? Colors.white : const Color(0xFF0F172A),
            fontWeight: FontWeight.w800,
            fontSize: 20,
          ),
        ),
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF131738) : Colors.white,
      ),
      body: favorites.isEmpty
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(32),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Container(
                      width: 80,
                      height: 80,
                      decoration: BoxDecoration(
                        color: const Color(0xFFE11D48).withValues(alpha: 0.12),
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: const Color(0xFFE11D48).withValues(alpha: 0.25),
                          width: 1.5,
                        ),
                      ),
                      child: const Center(
                        child: Icon(
                          Icons.favorite_border_rounded,
                          size: 40,
                          color: Color(0xFFE11D48),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                    Text(
                      l10n.noFavoritesYet,
                      style: TextStyle(
                        color: isDark ? Colors.white : const Color(0xFF0F172A),
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      l10n.tapHeartHint,
                      style: TextStyle(
                        color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            )
          : ListView.separated(
              physics: const BouncingScrollPhysics(),
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
              itemCount: favorites.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (context, i) {
                final city = favorites[i];
                return Dismissible(
                  key: Key('${city.name}_${city.latitude}'),
                  direction: DismissDirection.endToStart,
                  onDismissed: (_) {
                    getIt<LocationRepository>().removeFavorite(
                      city.name,
                      lat: city.latitude,
                      lon: city.longitude,
                    );
                    ref.read(homeProvider.notifier).refresh();
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content:
                            Text('${city.name}: ${l10n.removedFromFavorites}'),
                        duration: const Duration(seconds: 2),
                      ),
                    );
                  },
                  background: Container(
                    alignment: Alignment.centerRight,
                    padding: const EdgeInsets.only(right: 20),
                    decoration: BoxDecoration(
                      color: const Color(0xFFEF4444),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child:
                        const Icon(Icons.delete_rounded, color: Colors.white, size: 24),
                  ),
                  child: GlassCard(
                    padding: EdgeInsets.zero,
                    borderRadius: 20,
                    onTap: () {
                      ref.read(homeProvider.notifier).selectCity(city);
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
                              ? const Color(0xFFE11D48).withValues(alpha: 0.18)
                              : const Color(0xFFFFF1F2),
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(
                            color: isDark
                                ? const Color(0xFFE11D48).withValues(alpha: 0.35)
                                : const Color(0xFFFECDD3),
                            width: 1.0,
                          ),
                        ),
                        child: const Center(
                          child: Icon(
                            Icons.favorite_rounded,
                            color: Color(0xFFE11D48),
                            size: 20,
                          ),
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
                        '${city.latitude.toStringAsFixed(2)}°, ${city.longitude.toStringAsFixed(2)}°',
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
              },
            ),
    );
  }
}
