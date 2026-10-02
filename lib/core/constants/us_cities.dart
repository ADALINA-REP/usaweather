// lib/core/constants/us_cities.dart
import 'package:usaweather/data/models/location_data.dart';

class UsCityItem {
  final String name;
  final String state;
  final String stateCode;
  final String country;
  final String countryCode;
  final double latitude;
  final double longitude;
  final String timezone;
  final String regionGroup;

  const UsCityItem({
    required this.name,
    required this.state,
    required this.stateCode,
    this.country = 'United States',
    this.countryCode = 'US',
    required this.latitude,
    required this.longitude,
    required this.timezone,
    required this.regionGroup,
  });

  // Backward compatibility getters
  String get province => state;
  String get community => stateCode;

  LocationData toLocationData() => LocationData(
        name: name,
        latitude: latitude,
        longitude: longitude,
        country: country,
        admin1: ' ()',
      );
}

// Compatibility aliases
typedef SpainCityItem = UsCityItem;

class UsCities {
  static const List<UsCityItem> cities = [
    // East
    UsCityItem(name: 'New York City', state: 'New York', stateCode: 'NY', latitude: 40.7128, longitude: -74.0060, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Philadelphia', state: 'Pennsylvania', stateCode: 'PA', latitude: 39.9526, longitude: -75.1652, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Charlotte', state: 'North Carolina', stateCode: 'NC', latitude: 35.2271, longitude: -80.8431, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Washington, D.C.', state: 'District of Columbia', stateCode: 'DC', latitude: 38.9072, longitude: -77.0369, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Boston', state: 'Massachusetts', stateCode: 'MA', latitude: 42.3601, longitude: -71.0589, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Baltimore', state: 'Maryland', stateCode: 'MD', latitude: 39.2904, longitude: -76.6122, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Pittsburgh', state: 'Pennsylvania', stateCode: 'PA', latitude: 40.4406, longitude: -79.9959, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Richmond', state: 'Virginia', stateCode: 'VA', latitude: 37.5407, longitude: -77.4360, timezone: 'America/New_York', regionGroup: 'East'),
    UsCityItem(name: 'Raleigh', state: 'North Carolina', stateCode: 'NC', latitude: 35.7796, longitude: -78.6382, timezone: 'America/New_York', regionGroup: 'East'),

    // West
    UsCityItem(name: 'Los Angeles', state: 'California', stateCode: 'CA', latitude: 34.0522, longitude: -118.2437, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Phoenix', state: 'Arizona', stateCode: 'AZ', latitude: 33.4484, longitude: -112.0740, timezone: 'America/Phoenix', regionGroup: 'West'),
    UsCityItem(name: 'San Diego', state: 'California', stateCode: 'CA', latitude: 32.7157, longitude: -117.1611, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'San Jose', state: 'California', stateCode: 'CA', latitude: 37.3382, longitude: -121.8863, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Seattle', state: 'Washington', stateCode: 'WA', latitude: 47.6062, longitude: -122.3321, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Denver', state: 'Colorado', stateCode: 'CO', latitude: 39.7392, longitude: -104.9903, timezone: 'America/Denver', regionGroup: 'West'),
    UsCityItem(name: 'Portland', state: 'Oregon', stateCode: 'OR', latitude: 45.5152, longitude: -122.6784, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Las Vegas', state: 'Nevada', stateCode: 'NV', latitude: 36.1699, longitude: -115.1398, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Albuquerque', state: 'New Mexico', stateCode: 'NM', latitude: 35.0844, longitude: -106.6504, timezone: 'America/Denver', regionGroup: 'West'),
    UsCityItem(name: 'Tucson', state: 'Arizona', stateCode: 'AZ', latitude: 32.2226, longitude: -110.9747, timezone: 'America/Phoenix', regionGroup: 'West'),
    UsCityItem(name: 'Fresno', state: 'California', stateCode: 'CA', latitude: 36.7468, longitude: -119.7726, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Sacramento', state: 'California', stateCode: 'CA', latitude: 38.5816, longitude: -121.4944, timezone: 'America/Los_Angeles', regionGroup: 'West'),
    UsCityItem(name: 'Salt Lake City', state: 'Utah', stateCode: 'UT', latitude: 40.7608, longitude: -111.8910, timezone: 'America/Denver', regionGroup: 'West'),
    UsCityItem(name: 'San Francisco', state: 'California', stateCode: 'CA', latitude: 37.7749, longitude: -122.4194, timezone: 'America/Los_Angeles', regionGroup: 'West'),

    // Midwest
    UsCityItem(name: 'Chicago', state: 'Illinois', stateCode: 'IL', latitude: 41.8781, longitude: -87.6298, timezone: 'America/Chicago', regionGroup: 'Midwest'),
    UsCityItem(name: 'Columbus', state: 'Ohio', stateCode: 'OH', latitude: 39.9612, longitude: -82.9988, timezone: 'America/New_York', regionGroup: 'Midwest'),
    UsCityItem(name: 'Indianapolis', state: 'Indiana', stateCode: 'IN', latitude: 39.7684, longitude: -86.1581, timezone: 'America/Indiana/Indianapolis', regionGroup: 'Midwest'),
    UsCityItem(name: 'Detroit', state: 'Michigan', stateCode: 'MI', latitude: 42.3314, longitude: -83.0458, timezone: 'America/Detroit', regionGroup: 'Midwest'),
    UsCityItem(name: 'Milwaukee', state: 'Wisconsin', stateCode: 'WI', latitude: 43.0389, longitude: -87.9065, timezone: 'America/Chicago', regionGroup: 'Midwest'),
    UsCityItem(name: 'Minneapolis', state: 'Minnesota', stateCode: 'MN', latitude: 44.9778, longitude: -93.2650, timezone: 'America/Chicago', regionGroup: 'Midwest'),
    UsCityItem(name: 'Cleveland', state: 'Ohio', stateCode: 'OH', latitude: 41.4993, longitude: -81.6944, timezone: 'America/New_York', regionGroup: 'Midwest'),
    UsCityItem(name: 'Cincinnati', state: 'Ohio', stateCode: 'OH', latitude: 39.1031, longitude: -84.5120, timezone: 'America/New_York', regionGroup: 'Midwest'),
    UsCityItem(name: 'St. Louis', state: 'Missouri', stateCode: 'MO', latitude: 38.6270, longitude: -90.1994, timezone: 'America/Chicago', regionGroup: 'Midwest'),
    UsCityItem(name: 'Kansas City', state: 'Missouri', stateCode: 'MO', latitude: 39.0997, longitude: -94.5786, timezone: 'America/Chicago', regionGroup: 'Midwest'),

    // South
    UsCityItem(name: 'Houston', state: 'Texas', stateCode: 'TX', latitude: 29.7604, longitude: -95.3698, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'San Antonio', state: 'Texas', stateCode: 'TX', latitude: 29.4241, longitude: -98.4936, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Dallas', state: 'Texas', stateCode: 'TX', latitude: 32.7767, longitude: -96.7970, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Austin', state: 'Texas', stateCode: 'TX', latitude: 30.2672, longitude: -97.7431, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Jacksonville', state: 'Florida', stateCode: 'FL', latitude: 30.3322, longitude: -81.6557, timezone: 'America/New_York', regionGroup: 'South'),
    UsCityItem(name: 'Fort Worth', state: 'Texas', stateCode: 'TX', latitude: 32.7555, longitude: -97.3308, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Nashville', state: 'Tennessee', stateCode: 'TN', latitude: 36.1627, longitude: -86.7816, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Oklahoma City', state: 'Oklahoma', stateCode: 'OK', latitude: 35.4676, longitude: -97.5164, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Memphis', state: 'Tennessee', stateCode: 'TN', latitude: 35.1495, longitude: -90.0490, timezone: 'America/Chicago', regionGroup: 'South'),
    UsCityItem(name: 'Louisville', state: 'Kentucky', stateCode: 'KY', latitude: 38.2527, longitude: -85.7585, timezone: 'America/Kentucky/Louisville', regionGroup: 'South'),
    UsCityItem(name: 'Atlanta', state: 'Georgia', stateCode: 'GA', latitude: 33.7490, longitude: -84.3880, timezone: 'America/New_York', regionGroup: 'South'),
    UsCityItem(name: 'Miami', state: 'Florida', stateCode: 'FL', latitude: 25.7617, longitude: -80.1918, timezone: 'America/New_York', regionGroup: 'South'),
    UsCityItem(name: 'Tampa', state: 'Florida', stateCode: 'FL', latitude: 27.9506, longitude: -82.4572, timezone: 'America/New_York', regionGroup: 'South'),
    UsCityItem(name: 'Orlando', state: 'Florida', stateCode: 'FL', latitude: 28.5383, longitude: -81.3792, timezone: 'America/New_York', regionGroup: 'South'),
    UsCityItem(name: 'New Orleans', state: 'Louisiana', stateCode: 'LA', latitude: 29.9511, longitude: -90.0715, timezone: 'America/Chicago', regionGroup: 'South'),

    // Pacific / Alaska / Hawaii
    UsCityItem(name: 'Honolulu', state: 'Hawaii', stateCode: 'HI', latitude: 21.3069, longitude: -157.8583, timezone: 'Pacific/Honolulu', regionGroup: 'Pacific & Offshore'),
    UsCityItem(name: 'Anchorage', state: 'Alaska', stateCode: 'AK', latitude: 61.2181, longitude: -149.9003, timezone: 'America/Anchorage', regionGroup: 'Pacific & Offshore'),
  ];
}

typedef SpainCities = UsCities;
