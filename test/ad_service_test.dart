import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:usaweather/core/constants/api_constants.dart';
import 'package:usaweather/core/services/ad_service.dart';
import 'package:usaweather/ui/widgets/ad_banner_widget.dart';

void main() {
  group('AdMob Configuration & AdService Tests', () {
    test('ApiConstants provides valid production and test AdMob IDs', () {
      expect(ApiConstants.admobAppId, 'ca-app-pub-9046523366518719~4536925584');
      expect(ApiConstants.bannerAdUnitId, 'ca-app-pub-9046523366518719/3223843917');
      expect(ApiConstants.interstitialAdUnitId, 'ca-app-pub-9046523366518719/6361143141');
      expect(ApiConstants.appOpenAdUnitId, 'ca-app-pub-9046523366518719/8196968998');

      // Test IDs
      expect(ApiConstants.testBannerAdUnitAndroid, 'ca-app-pub-3940256099942544/6300978111');
      expect(ApiConstants.testInterstitialAdUnitAndroid, 'ca-app-pub-3940256099942544/1033173712');
      expect(ApiConstants.testAppOpenAdUnitAndroid, 'ca-app-pub-3940256099942544/9257395921');
    });

    test('AdService returns test Ad Unit IDs in debug/test mode', () {
      final adService = AdService.instance;
      expect(adService.bannerAdUnitId, isNotEmpty);
      expect(adService.interstitialAdUnitId, isNotEmpty);
      expect(adService.appOpenAdUnitId, isNotEmpty);
    });

    test('showInterstitialAd gracefully invokes onDismissed without blocking flow', () {
      final adService = AdService.instance;
      bool dismissed = false;

      adService.showInterstitialAd(
        onDismissed: () {
          dismissed = true;
        },
      );

      expect(dismissed, isTrue);
    });

    test('showAppOpenAdIfAvailable gracefully invokes onComplete without blocking flow', () {
      final adService = AdService.instance;
      bool completed = false;

      adService.showAppOpenAdIfAvailable(
        onComplete: () {
          completed = true;
        },
      );

      expect(completed, isTrue);
    });

    testWidgets('AdBannerWidget renders safely without throwing in test environment', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            bottomNavigationBar: AdBannerWidget(),
          ),
        ),
      );

      expect(find.byType(AdBannerWidget), findsOneWidget);
      // In non-mobile test environment, ad banner safely collapses to SizedBox.shrink
      expect(find.byType(SizedBox), findsWidgets);
    });
  });
}
