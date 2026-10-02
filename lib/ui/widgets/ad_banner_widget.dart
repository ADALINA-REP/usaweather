// lib/ui/widgets/ad_banner_widget.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:usaweather/core/services/ad_service.dart';

/// Reusable banner ad widget that displays seamlessly above the navigation bar.
/// Automatically retries loading if AdMob returns no-fill (code 3) during initial index period.
class AdBannerWidget extends StatefulWidget {
  final AdSize adSize;
  const AdBannerWidget({
    super.key,
    this.adSize = AdSize.banner,
  });

  @override
  State<AdBannerWidget> createState() => _AdBannerWidgetState();
}

class _AdBannerWidgetState extends State<AdBannerWidget> {
  BannerAd? _bannerAd;
  bool _isLoaded = false;
  Timer? _retryTimer;
  int _retryCount = 0;

  @override
  void initState() {
    super.initState();
    _loadBanner();
  }

  void _loadBanner() {
    if (!AdService.instance.isPlatformSupported) return;

    _bannerAd?.dispose();
    _bannerAd = AdService.instance.createBannerAd(
      size: widget.adSize,
      onLoaded: () {
        debugPrint('[AdBannerWidget] Banner ad loaded successfully.');
        if (mounted) {
          setState(() {
            _isLoaded = true;
          });
        }
      },
      onFailed: (error) {
        debugPrint('[AdBannerWidget] Banner ad failed to load: ${error.code} - ${error.message}');
        if (mounted) {
          setState(() {
            _isLoaded = false;
          });
          // Auto-retry up to 5 times with exponential backoff for new AdMob unit indexing
          if (_retryCount < 5) {
            _retryCount++;
            final delay = Duration(seconds: 10 * _retryCount);
            _retryTimer?.cancel();
            _retryTimer = Timer(delay, () {
              if (mounted) _loadBanner();
            });
          }
        }
      },
    );

    _bannerAd?.load();
  }

  @override
  void dispose() {
    _retryTimer?.cancel();
    _bannerAd?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isLoaded || _bannerAd == null) {
      return const SizedBox.shrink();
    }

    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      width: double.infinity,
      height: _bannerAd!.size.height.toDouble(),
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF0F172A) : Colors.grey.shade50,
        border: Border(
          top: BorderSide(
            color: isDark
                ? Colors.white.withValues(alpha: 0.08)
                : Colors.black.withValues(alpha: 0.05),
            width: 0.5,
          ),
        ),
      ),
      child: AdWidget(ad: _bannerAd!),
    );
  }
}
