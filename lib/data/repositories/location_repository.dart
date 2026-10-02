// lib/data/repositories/location_repository.dart
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:usaweather/data/models/location_data.dart';

class LocationRepository {
  final SharedPreferences _prefs;

  LocationRepository(this._prefs);

  bool get shouldFollowGps => _prefs.getBool('follow_gps') ?? false;
  set shouldFollowGps(bool v) => _prefs.setBool('follow_gps', v);

  bool get isFirstLaunch => _prefs.getBool('first_launch') ?? true;
  set isFirstLaunch(bool v) => _prefs.setBool('first_launch', v);

  LocationData? getLastLocation() {
    final str = _prefs.getString('last_location');
    if (str == null) return null;
    try {
      return LocationData.fromJson(jsonDecode(str));
    } catch (_) {
      return null;
    }
  }

  void saveLocation(String name, double lat, double lon) {
    final loc = LocationData(name: name, latitude: lat, longitude: lon);
    _prefs.setString('last_location', jsonEncode(loc.toJson()));
  }

  List<LocationData> getFavorites() {
    final list = _prefs.getStringList('favorites') ?? [];
    return list.map((s) {
      try {
        return LocationData.fromJson(jsonDecode(s));
      } catch (_) {
        return null;
      }
    }).whereType<LocationData>().toList();
  }

  bool isFavorite(String name, double lat, double lon) {
    return getFavorites().any((f) =>
        (f.name.trim().toLowerCase() == name.trim().toLowerCase()) ||
        (_coordsMatch(f.latitude, lat) && _coordsMatch(f.longitude, lon)));
  }

  void toggleFavorite(String name, double lat, double lon) {
    final favs = getFavorites();
    final existingIndex = favs.indexWhere((f) =>
        _coordsMatch(f.latitude, lat) && _coordsMatch(f.longitude, lon));

    if (existingIndex >= 0) {
      favs.removeAt(existingIndex);
    } else {
      favs.add(LocationData(name: name, latitude: lat, longitude: lon));
    }
    _saveFavorites(favs);
  }

  void removeFavorite(String name, {double? lat, double? lon}) {
    final favs = getFavorites();
    favs.removeWhere((f) {
      if (lat != null && lon != null) {
        return _coordsMatch(f.latitude, lat) && _coordsMatch(f.longitude, lon);
      }
      return f.name.trim().toLowerCase() == name.trim().toLowerCase();
    });
    _saveFavorites(favs);
  }

  void _saveFavorites(List<LocationData> favs) {
    _prefs.setStringList(
        'favorites', favs.map((f) => jsonEncode(f.toJson())).toList());
  }

  bool _coordsMatch(double a, double b) => (a - b).abs() < 0.12;
}
