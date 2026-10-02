// lib/core/router/app_router.dart
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/ui/screens/cities/cities_screen.dart';
import 'package:usaweather/ui/screens/favorites/favorites_screen.dart';
import 'package:usaweather/ui/screens/home/home_screen.dart';
import 'package:usaweather/ui/screens/radar/radar_screen.dart';
import 'package:usaweather/ui/screens/settings/settings_screen.dart';
import 'package:usaweather/ui/screens/splash/splash_screen.dart';
import 'package:usaweather/core/services/ad_service.dart';
import 'package:usaweather/ui/widgets/ad_banner_widget.dart';

class AppRoutes {
  static const splash = '/';
  static const home = '/home';
  static const favorites = '/favorites';
  static const cities = '/cities';
  static const radar = '/radar';
  static const settings = '/settings';
}

final GlobalKey<NavigatorState> rootNavigatorKey =
    GlobalKey<NavigatorState>(debugLabel: 'root');
final GlobalKey<NavigatorState> shellNavigatorKey =
    GlobalKey<NavigatorState>(debugLabel: 'shell');

final appRouter = GoRouter(
  navigatorKey: rootNavigatorKey,
  initialLocation: AppRoutes.splash,
  routes: [
    GoRoute(
      path: AppRoutes.splash,
      builder: (context, state) => const SplashScreen(),
    ),
    ShellRoute(
      navigatorKey: shellNavigatorKey,
      builder: (context, state, child) => MainShell(child: child),
      routes: [
        GoRoute(
          path: AppRoutes.home,
          pageBuilder: (context, state) => const NoTransitionPage(
            child: HomeScreen(),
          ),
        ),
        GoRoute(
          path: AppRoutes.favorites,
          pageBuilder: (context, state) => const NoTransitionPage(
            child: FavoritesScreen(),
          ),
        ),
        GoRoute(
          path: AppRoutes.cities,
          pageBuilder: (context, state) => const NoTransitionPage(
            child: CitiesScreen(),
          ),
        ),
        GoRoute(
          path: AppRoutes.radar,
          pageBuilder: (context, state) => const NoTransitionPage(
            child: RadarScreen(),
          ),
        ),
        GoRoute(
          path: AppRoutes.settings,
          pageBuilder: (context, state) => const NoTransitionPage(
            child: SettingsScreen(),
          ),
        ),
      ],
    ),
  ],
);

class MainShell extends StatefulWidget {
  final Widget child;
  const MainShell({super.key, required this.child});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  final List<String> _routes = [
    AppRoutes.home,
    AppRoutes.favorites,
    AppRoutes.cities,
    AppRoutes.radar,
    AppRoutes.settings,
  ];

  int _calculateSelectedIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    if (location.startsWith(AppRoutes.favorites)) return 1;
    if (location.startsWith(AppRoutes.cities)) return 2;
    if (location.startsWith(AppRoutes.radar)) return 3;
    if (location.startsWith(AppRoutes.settings)) return 4;
    return 0; // Default to Home
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: widget.child,
      bottomNavigationBar: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const AdBannerWidget(),
          _buildNavBar(context),
        ],
      ),
    );
  }

  Widget _buildNavBar(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final currentIndex = _calculateSelectedIndex(context);
    final l10n = AppLocalizations.of(context);

    return Container(
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF131738) : Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.15),
            blurRadius: 20,
            offset: const Offset(0, -5),
          ),
        ],
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _NavItem(
                icon: Icons.home_rounded,
                label: l10n.navHome,
                index: 0,
                currentIndex: currentIndex,
                onTap: _onTap,
              ),
              _NavItem(
                icon: Icons.favorite_rounded,
                label: l10n.navFavorites,
                index: 1,
                currentIndex: currentIndex,
                onTap: _onTap,
              ),
              _NavItem(
                icon: Icons.location_city_rounded,
                label: l10n.navCities,
                index: 2,
                currentIndex: currentIndex,
                onTap: _onTap,
              ),
              _NavItem(
                icon: Icons.radar_rounded,
                label: l10n.navRadar,
                index: 3,
                currentIndex: currentIndex,
                onTap: _onTap,
              ),
              _NavItem(
                icon: Icons.settings_rounded,
                label: l10n.navSettings,
                index: 4,
                currentIndex: currentIndex,
                onTap: _onTap,
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _onTap(int index) {
    if (index == _calculateSelectedIndex(context)) return;

    if (shellNavigatorKey.currentState != null &&
        shellNavigatorKey.currentState!.canPop()) {
      shellNavigatorKey.currentState!.popUntil((route) => route.isFirst);
    }
    if (rootNavigatorKey.currentState != null &&
        rootNavigatorKey.currentState!.canPop()) {
      rootNavigatorKey.currentState!.popUntil((route) => route.isFirst);
    }

    // Interstitial opportunity with cooldown on switching to Radar (index 3) or Cities (index 2)
    if (index == 2 || index == 3) {
      AdService.instance.showInterstitialAd(
        onDismissed: () {
          if (mounted) context.go(_routes[index]);
        },
      );
    } else {
      context.go(_routes[index]);
    }
  }
}

class _NavItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final int index;
  final int currentIndex;
  final Function(int) onTap;

  const _NavItem({
    required this.icon,
    required this.label,
    required this.index,
    required this.currentIndex,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isSelected = index == currentIndex;
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final selectedColor =
        isDark ? const Color(0xFF00B4D8) : const Color(0xFF0284C7);
    final unselectedColor =
        isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B);

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () => onTap(index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 250),
        curve: Curves.easeInOut,
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected
              ? selectedColor.withValues(alpha: 0.15)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon,
                color: isSelected ? selectedColor : unselectedColor, size: 24),
            const SizedBox(height: 2),
            Text(
              label,
              style: TextStyle(
                color: isSelected ? selectedColor : unselectedColor,
                fontSize: 10,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.w400,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
