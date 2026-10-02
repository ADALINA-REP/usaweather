// lib/core/services/ad_service.dart
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:usaweather/core/constants/api_constants.dart';

/// Central service for Google AdMob initialization and management.
class AdService {
  AdService._();
  static final AdService instance = AdService._();

  bool _isInitialized = false;
  bool get isInitialized => _isInitialized;

  // Interstitial state
  InterstitialAd? _interstitialAd;
  bool _isInterstitialLoading = false;
  DateTime? _lastInterstitialShown;
  static const Duration _interstitialCooldown = Duration(minutes: 3);

  // App Open ad state
  AppOpenAd? _appOpenAd;
  bool _isAppOpenLoading = false;
  bool _isShowingAppOpenAd = false;
  DateTime? _lastAppOpenShown;
  static const Duration _appOpenCooldown = Duration(hours: 4);

  /// True if current platform supports Google Mobile Ads (Android & iOS)
  bool get isPlatformSupported {
    if (kIsWeb) return false;
    try {
      return Platform.isAndroid || Platform.isIOS;
    } catch (_) {
      return false;
    }
  }

  /// Banner Ad Unit ID based on platform
  String get bannerAdUnitId {
    return Platform.isIOS
        ? ApiConstants.bannerAdUnitIos
        : ApiConstants.bannerAdUnitAndroid;
  }

  /// Interstitial Ad Unit ID based on platform
  String get interstitialAdUnitId {
    return Platform.isIOS
        ? ApiConstants.interstitialAdUnitIos
        : ApiConstants.interstitialAdUnitAndroid;
  }

  /// App Open Ad Unit ID based on platform
  String get appOpenAdUnitId {
    return Platform.isIOS
        ? ApiConstants.appOpenAdUnitIos
        : ApiConstants.appOpenAdUnitAndroid;
  }

  /// Initialize AdMob SDK
  Future<void> initialize() async {
    if (!isPlatformSupported) return;
    if (_isInitialized) return;

    try {
      await MobileAds.instance.initialize();
      _isInitialized = true;
      debugPrint('[AdService] MobileAds initialized successfully.');

      // Preload first ads
      loadInterstitialAd();
      loadAppOpenAd();
    } catch (e) {
      debugPrint('[AdService] Error initializing MobileAds: $e');
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Banner Ad Factory
  // ──────────────────────────────────────────────────────────────────────────

  /// Creates a BannerAd configured with appropriate listeners.
  /// Returns null if platform is unsupported.
  BannerAd? createBannerAd({
    required VoidCallback onLoaded,
    required void Function(LoadAdError) onFailed,
    AdSize size = AdSize.banner,
  }) {
    if (!isPlatformSupported) return null;

    return BannerAd(
      adUnitId: bannerAdUnitId,
      size: size,
      request: const AdRequest(),
      listener: BannerAdListener(
        onAdLoaded: (ad) {
          debugPrint('[AdService] Banner loaded successfully.');
          onLoaded();
        },
        onAdFailedToLoad: (ad, error) {
          debugPrint('[AdService] Banner failed to load: ${error.message}');
          ad.dispose();
          onFailed(error);
        },
      ),
    );
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Interstitial Ad Management
  // ──────────────────────────────────────────────────────────────────────────

  /// Pre-loads an interstitial ad into cache
  void loadInterstitialAd() {
    if (!isPlatformSupported || _isInterstitialLoading || _interstitialAd != null) {
      return;
    }

    _isInterstitialLoading = true;
    InterstitialAd.load(
      adUnitId: interstitialAdUnitId,
      request: const AdRequest(),
      adLoadCallback: InterstitialAdLoadCallback(
        onAdLoaded: (ad) {
          _interstitialAd = ad;
          _isInterstitialLoading = false;
          debugPrint('[AdService] Interstitial ad loaded and cached.');
        },
        onAdFailedToLoad: (error) {
          _interstitialAd = null;
          _isInterstitialLoading = false;
          debugPrint('[AdService] Failed to load interstitial: ${error.message}');
        },
      ),
    );
  }

  /// Displays an interstitial ad if available and cooldown duration has passed.
  /// [onDismissed] is guaranteed to be called immediately if ad cannot be shown,
  /// ensuring UI/navigation transitions are never blocked.
  void showInterstitialAd({VoidCallback? onDismissed}) {
    if (!isPlatformSupported || _interstitialAd == null) {
      loadInterstitialAd();
      onDismissed?.call();
      return;
    }

    // Cooldown check
    final now = DateTime.now();
    if (_lastInterstitialShown != null &&
        now.difference(_lastInterstitialShown!) < _interstitialCooldown) {
      debugPrint('[AdService] Interstitial skipped: cooldown active.');
      onDismissed?.call();
      return;
    }

    final ad = _interstitialAd!;
    _interstitialAd = null;

    ad.fullScreenContentCallback = FullScreenContentCallback(
      onAdShowedFullScreenContent: (ad) {
        _lastInterstitialShown = DateTime.now();
      },
      onAdDismissedFullScreenContent: (ad) {
        ad.dispose();
        loadInterstitialAd();
        onDismissed?.call();
      },
      onAdFailedToShowFullScreenContent: (ad, error) {
        debugPrint('[AdService] Interstitial failed to show: ${error.message}');
        ad.dispose();
        loadInterstitialAd();
        onDismissed?.call();
      },
    );

    ad.show();
  }

  // ──────────────────────────────────────────────────────────────────────────
  // App Open Ad Management
  // ──────────────────────────────────────────────────────────────────────────

  /// Pre-loads an App Open ad into cache
  void loadAppOpenAd() {
    if (!isPlatformSupported || _isAppOpenLoading || _appOpenAd != null) {
      return;
    }

    _isAppOpenLoading = true;
    AppOpenAd.load(
      adUnitId: appOpenAdUnitId,
      request: const AdRequest(),
      adLoadCallback: AppOpenAdLoadCallback(
        onAdLoaded: (ad) {
          _appOpenAd = ad;
          _isAppOpenLoading = false;
          debugPrint('[AdService] App Open ad loaded and cached.');
        },
        onAdFailedToLoad: (error) {
          _appOpenAd = null;
          _isAppOpenLoading = false;
          debugPrint('[AdService] Failed to load App Open ad: ${error.message}');
        },
      ),
    );
  }

  /// Displays App Open ad on app launch or foreground resume if cooldown passed
  void showAppOpenAdIfAvailable({VoidCallback? onComplete}) {
    if (!isPlatformSupported) {
      onComplete?.call();
      return;
    }

    if (_isShowingAppOpenAd) {
      onComplete?.call();
      return;
    }

    if (_appOpenAd == null) {
      loadAppOpenAd();
      onComplete?.call();
      return;
    }

    final now = DateTime.now();
    if (_lastAppOpenShown != null &&
        now.difference(_lastAppOpenShown!) < _appOpenCooldown) {
      debugPrint('[AdService] App Open ad skipped: cooldown active.');
      onComplete?.call();
      return;
    }

    final ad = _appOpenAd!;
    _appOpenAd = null;
    _isShowingAppOpenAd = true;

    ad.fullScreenContentCallback = FullScreenContentCallback(
      onAdShowedFullScreenContent: (ad) {
        _lastAppOpenShown = DateTime.now();
      },
      onAdDismissedFullScreenContent: (ad) {
        _isShowingAppOpenAd = false;
        ad.dispose();
        loadAppOpenAd();
        onComplete?.call();
      },
      onAdFailedToShowFullScreenContent: (ad, error) {
        debugPrint('[AdService] App Open ad failed to show: ${error.message}');
        _isShowingAppOpenAd = false;
        ad.dispose();
        loadAppOpenAd();
        onComplete?.call();
      },
    );

    ad.show();
  }
}
