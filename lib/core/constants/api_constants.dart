// lib/core/constants/api_constants.dart
class ApiConstants {
  static const String openMeteoBase = 'https://api.open-meteo.com/v1';
  static const String geocodingBase = 'https://geocoding-api.open-meteo.com/v1';
  static const String airQualityBase = 'https://air-quality-api.open-meteo.com/v1';
  static const String rainViewerBase = 'https://api.rainviewer.com/public/weather-maps.json';
  static const String nominatimBase = 'https://nominatim.openstreetmap.org';

  static const String currentParams =
      'temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,'
      'wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,surface_pressure,'
      'cloud_cover,visibility';

  static const String hourlyParams =
      'temperature_2m,relative_humidity_2m,weather_code,precipitation_probability,'
      'surface_pressure,is_day,cloud_cover,visibility,wind_speed_10m,'
      'wind_direction_10m,wind_gusts_10m';

  static const String dailyParams =
      'weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,'
      'uv_index_max,precipitation_probability_max,wind_speed_10m_max,'
      'apparent_temperature_max,precipitation_sum';

  static const String airQualityParams =
      'pm2_5,pm10,european_aqi,us_aqi,ozone,nitrogen_dioxide,sulphur_dioxide,carbon_monoxide';

  // AdMob IDs (Production - Android)
  static const String admobAndroidAppId = 'ca-app-pub-9046523366518719~4536925584';
  static const String bannerAdUnitAndroid = 'ca-app-pub-9046523366518719/3223843917';
  static const String interstitialAdUnitAndroid = 'ca-app-pub-9046523366518719/6361143141';
  static const String appOpenAdUnitAndroid = 'ca-app-pub-9046523366518719/8196968998';

  // AdMob IDs (Production - iOS)
  static const String admobIosAppId = 'ca-app-pub-9046523366518719~4777347163';
  static const String bannerAdUnitIos = 'ca-app-pub-9046523366518719/2482356318';
  static const String interstitialAdUnitIos = 'ca-app-pub-9046523366518719/6501492371';
  static const String appOpenAdUnitIos = 'ca-app-pub-9046523366518719/8856192974';

  // Default aliases
  static const String admobAppId = admobAndroidAppId;
  static const String bannerAdUnitId = bannerAdUnitAndroid;
  static const String interstitialAdUnitId = interstitialAdUnitAndroid;
  static const String appOpenAdUnitId = appOpenAdUnitAndroid;

  // Google Test Ad Unit IDs
  static const String testBannerAdUnitAndroid = 'ca-app-pub-3940256099942544/6300978111';
  static const String testBannerAdUnitIos = 'ca-app-pub-3940256099942544/2934735716';
  static const String testInterstitialAdUnitAndroid = 'ca-app-pub-3940256099942544/1033173712';
  static const String testInterstitialAdUnitIos = 'ca-app-pub-3940256099942544/4411468910';
  static const String testAppOpenAdUnitAndroid = 'ca-app-pub-3940256099942544/9257395921';
  static const String testAppOpenAdUnitIos = 'ca-app-pub-3940256099942544/5662855259';

  // Google Maps
  static const String googleMapsApiKey = 'AIzaSyBCbRqBJBzqc6sBBPiWAwUmkpXbYU8LJkE';

  // Default location (New York City, USA)
  static const String defaultCity = 'New York City';
  static const double defaultLat = 40.7128;
  static const double defaultLon = -74.0060;
}
