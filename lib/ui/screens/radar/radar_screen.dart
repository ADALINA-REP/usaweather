// lib/ui/screens/radar/radar_screen.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:usaweather/core/constants/api_constants.dart';
import 'package:usaweather/core/di/injection.dart';
import 'package:usaweather/core/localization/app_localizations.dart';
import 'package:usaweather/data/repositories/weather_repository.dart';
import 'package:usaweather/ui/screens/home/home_provider.dart';

final _radarDataProvider =
    FutureProvider<Map<String, dynamic>?>((ref) async {
  return getIt<WeatherRepository>().getRainViewerData();
});

class RadarFrame {
  final int time;
  final String path;
  final bool isNowcast;

  const RadarFrame({
    required this.time,
    required this.path,
    this.isNowcast = false,
  });

  DateTime get dateTime => DateTime.fromMillisecondsSinceEpoch(time * 1000);
}

class RadarScreen extends ConsumerStatefulWidget {
  const RadarScreen({super.key});
  @override
  ConsumerState<RadarScreen> createState() => _RadarScreenState();
}

class _RadarScreenState extends ConsumerState<RadarScreen> {
  final MapController _mapController = MapController();
  bool _showRadar = true;
  List<RadarFrame> _frames = [];
  String _host = 'https://tilecache.rainviewer.com';
  int _currentIndex = 0;
  bool _isPlaying = false;
  Timer? _playbackTimer;
  bool _initialDataApplied = false;
  bool _useSatelliteMode = false;

  @override
  void dispose() {
    _playbackTimer?.cancel();
    _mapController.dispose();
    super.dispose();
  }

  void _parseAndApplyRadarData(Map<String, dynamic>? data) {
    if (data == null || _initialDataApplied) return;
    try {
      final host = (data['host'] as String?) ?? 'https://tilecache.rainviewer.com';
      final radar = data['radar'] as Map<String, dynamic>?;
      final past = radar?['past'] as List<dynamic>?;
      final nowcast = radar?['nowcast'] as List<dynamic>?;

      final frames = <RadarFrame>[];
      if (past != null) {
        for (final item in past) {
          if (item is Map<String, dynamic> &&
              item['path'] != null &&
              item['time'] != null) {
            frames.add(RadarFrame(
              time: (item['time'] as num).toInt(),
              path: item['path'] as String,
              isNowcast: false,
            ));
          }
        }
      }
      if (nowcast != null) {
        for (final item in nowcast) {
          if (item is Map<String, dynamic> &&
              item['path'] != null &&
              item['time'] != null) {
            frames.add(RadarFrame(
              time: (item['time'] as num).toInt(),
              path: item['path'] as String,
              isNowcast: true,
            ));
          }
        }
      }

      if (frames.isEmpty) return;

      _host = host;
      _frames = frames;
      _initialDataApplied = true;

      final pastCount = past?.length ?? 0;
      _currentIndex = pastCount > 0 ? pastCount - 1 : 0;
      setState(() {});
    } catch (e) {
      debugPrint('Radar parsing error: $e');
    }
  }

  void _togglePlayPause() {
    if (_isPlaying) {
      _pause();
    } else {
      _play();
    }
  }

  void _play() {
    if (_frames.isEmpty) return;
    _playbackTimer?.cancel();
    setState(() => _isPlaying = true);
    _playbackTimer = Timer.periodic(const Duration(milliseconds: 750), (timer) {
      if (!mounted || _frames.isEmpty) {
        timer.cancel();
        return;
      }
      setState(() {
        _currentIndex = (_currentIndex + 1) % _frames.length;
      });
    });
  }

  void _pause() {
    _playbackTimer?.cancel();
    _playbackTimer = null;
    setState(() => _isPlaying = false);
  }

  void _stepFrame(int delta) {
    if (_frames.isEmpty) return;
    _pause();
    setState(() {
      _currentIndex = (_currentIndex + delta).clamp(0, _frames.length - 1);
    });
  }

  void _seekToFrame(int index) {
    if (_frames.isEmpty) return;
    _pause();
    setState(() {
      _currentIndex = index.clamp(0, _frames.length - 1);
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final homeState = ref.watch(homeProvider);
    final radarAsync = ref.watch(_radarDataProvider);
    final l10n = AppLocalizations.of(context);

    double lat = ApiConstants.defaultLat;
    double lon = ApiConstants.defaultLon;
    if (homeState is WeatherSuccess) {
      lat = homeState.latitude;
      lon = homeState.longitude;
    }

    radarAsync.whenData((data) => _parseAndApplyRadarData(data));

    final currentFrame = _frames.isNotEmpty && _currentIndex < _frames.length
        ? _frames[_currentIndex]
        : null;
    final isLive = currentFrame != null &&
        !currentFrame.isNowcast &&
        (_currentIndex ==
            _frames.where((f) => !f.isNowcast).length - 1);

    // Map Tiles URLs (100% Free Public Tile Servers - No API Keys Required)
    final String baseTileUrl = _useSatelliteMode
        ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
        : (isDark
            ? 'https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}'
            : 'https://tile.openstreetmap.org/{z}/{x}/{y}.png');

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0A0E2E) : Colors.white,
      appBar: AppBar(
        title: Text(l10n.radarTitle),
        backgroundColor: isDark ? const Color(0xFF131738) : Colors.white,
        actions: [
          IconButton(
            icon: Icon(
              _useSatelliteMode ? Icons.satellite_alt : Icons.map_outlined,
              color: _useSatelliteMode
                  ? const Color(0xFF00B4D8)
                  : (isDark ? Colors.white70 : Colors.black87),
            ),
            onPressed: () => setState(() => _useSatelliteMode = !_useSatelliteMode),
            tooltip: 'Toggle Satellite View',
          ),
          IconButton(
            icon: Icon(
              _showRadar ? Icons.layers : Icons.layers_outlined,
              color: _showRadar
                  ? const Color(0xFF00B4D8)
                  : (isDark ? Colors.white54 : Colors.black45),
            ),
            onPressed: () => setState(() => _showRadar = !_showRadar),
            tooltip: l10n.toggleRadarTooltip,
          ),
        ],
      ),
      body: Stack(
        children: [
          // ─── FlutterMap Widget ──────────────────────────────────────────────
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(
              initialCenter: LatLng(lat, lon),
              initialZoom: 6.5,
              minZoom: 3.0,
              maxZoom: 13.0,
            ),
            children: [
              // Base Map Layer (Esri Dark Canvas / OpenStreetMap / Esri Satellite)
              TileLayer(
                urlTemplate: baseTileUrl,
                userAgentPackageName: 'com.usweather.radarforecast',
                tileProvider: NetworkTileProvider(),
                tileBuilder: isDark && !_useSatelliteMode
                    ? (context, tileWidget, tile) {
                        return ColorFiltered(
                          colorFilter: const ColorFilter.matrix(<double>[
                            0.45, 0.0,  0.0,  0.0, 4,
                            0.0,  0.50, 0.0,  0.0, 8,
                            0.0,  0.0,  0.75, 0.0, 22,
                            0.0,  0.0,  0.0,  1.0, 0,
                          ]),
                          child: tileWidget,
                        );
                      }
                    : null,
              ),

              // RainViewer Precipitation Overlay Layer
              if (_showRadar && currentFrame != null)
                Opacity(
                  opacity: 0.75,
                  child: TileLayer(
                    urlTemplate:
                        '$_host${currentFrame.path}/256/{z}/{x}/{y}/2/1_1.png',
                    userAgentPackageName: 'com.usweather.radarforecast',
                    tileProvider: NetworkTileProvider(),
                  ),
                ),

              // User Location Pin / Marker
              MarkerLayer(
                markers: [
                  Marker(
                    point: LatLng(lat, lon),
                    width: 48,
                    height: 48,
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        Container(
                          width: 32,
                          height: 32,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: const Color(0xFF00B4D8).withValues(alpha: 0.3),
                          ),
                        ),
                        Container(
                          width: 16,
                          height: 16,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: const Color(0xFF00B4D8),
                            border: Border.all(color: Colors.white, width: 2.5),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withValues(alpha: 0.3),
                                blurRadius: 6,
                              )
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),

          // ─── Map Controls (Zoom / Location Recenter) ───────────────────────
          Positioned(
            top: 16,
            right: 16,
            child: Column(
              children: [
                _mapControlButton(
                  icon: Icons.add_rounded,
                  isDark: isDark,
                  onPressed: () {
                    _mapController.move(
                      _mapController.camera.center,
                      (_mapController.camera.zoom + 1).clamp(3.0, 13.0),
                    );
                  },
                ),
                const SizedBox(height: 8),
                _mapControlButton(
                  icon: Icons.remove_rounded,
                  isDark: isDark,
                  onPressed: () {
                    _mapController.move(
                      _mapController.camera.center,
                      (_mapController.camera.zoom - 1).clamp(3.0, 13.0),
                    );
                  },
                ),
                const SizedBox(height: 8),
                _mapControlButton(
                  icon: Icons.my_location_rounded,
                  isDark: isDark,
                  onPressed: () {
                    _mapController.move(LatLng(lat, lon), 7.5);
                  },
                ),
              ],
            ),
          ),

          // ─── Floating Radar Playback Controller ────────────────────────────
          if (_showRadar && _frames.isNotEmpty && currentFrame != null)
            Positioned(
              bottom: 96,
              left: 16,
              right: 16,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: (isDark
                          ? const Color(0xFF131738)
                          : Colors.white)
                      .withValues(alpha: 0.94),
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(
                    color: (isDark ? Colors.white : Colors.black)
                        .withValues(alpha: 0.12),
                    width: 1,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color:
                          Colors.black.withValues(alpha: isDark ? 0.35 : 0.12),
                      blurRadius: 16,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Row(
                      children: [
                        // Play / Pause Circular Button
                        Material(
                          color: Colors.transparent,
                          child: InkWell(
                            onTap: _togglePlayPause,
                            borderRadius: BorderRadius.circular(24),
                            child: Container(
                              width: 44,
                              height: 44,
                              decoration: BoxDecoration(
                                gradient: const LinearGradient(
                                  colors: [
                                    Color(0xFF00B4D8),
                                    Color(0xFF0077B6)
                                  ],
                                  begin: Alignment.topLeft,
                                  end: Alignment.bottomRight,
                                ),
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: const Color(0xFF00B4D8)
                                        .withValues(alpha: 0.4),
                                    blurRadius: 8,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: Icon(
                                _isPlaying
                                    ? Icons.pause_rounded
                                    : Icons.play_arrow_rounded,
                                color: Colors.white,
                                size: 26,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        // Time Display & Status Badge
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Row(
                                children: [
                                  // Pulsing indicator dot
                                  Container(
                                    width: 8,
                                    height: 8,
                                    decoration: BoxDecoration(
                                      shape: BoxShape.circle,
                                      color: isLive
                                          ? const Color(0xFFEF4444)
                                          : (currentFrame.isNowcast
                                              ? const Color(0xFF38BDF8)
                                              : const Color(0xFF00B4D8)),
                                      boxShadow: [
                                        BoxShadow(
                                          color: (isLive
                                                  ? const Color(0xFFEF4444)
                                                  : const Color(0xFF00B4D8))
                                              .withValues(alpha: 0.6),
                                          blurRadius: 6,
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    DateFormat('HH:mm').format(
                                        currentFrame.dateTime.toLocal()),
                                    style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                      letterSpacing: 0.5,
                                      color: isDark
                                          ? Colors.white
                                          : const Color(0xFF0F172A),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                        horizontal: 6, vertical: 2),
                                    decoration: BoxDecoration(
                                      color: (isLive
                                              ? const Color(0xFFEF4444)
                                              : const Color(0xFF00B4D8))
                                          .withValues(alpha: 0.15),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Text(
                                      isLive
                                          ? l10n.radarLive
                                          : _getRelativeTimeLabel(
                                              currentFrame, l10n),
                                      style: TextStyle(
                                        fontSize: 10,
                                        fontWeight: FontWeight.w700,
                                        color: isLive
                                            ? const Color(0xFFEF4444)
                                            : const Color(0xFF00B4D8),
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                        // Previous Frame Button
                        IconButton(
                          icon: const Icon(Icons.skip_previous_rounded),
                          iconSize: 22,
                          color: isDark ? Colors.white70 : Colors.black87,
                          onPressed: _currentIndex > 0
                              ? () => _stepFrame(-1)
                              : null,
                          tooltip: 'Previous frame',
                        ),
                        // Next Frame Button
                        IconButton(
                          icon: const Icon(Icons.skip_next_rounded),
                          iconSize: 22,
                          color: isDark ? Colors.white70 : Colors.black87,
                          onPressed: _currentIndex < _frames.length - 1
                              ? () => _stepFrame(1)
                              : null,
                          tooltip: 'Next frame',
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    // Timeline Scrubber Slider
                    SliderTheme(
                      data: SliderThemeData(
                        trackHeight: 4,
                        activeTrackColor: const Color(0xFF00B4D8),
                        inactiveTrackColor:
                            (isDark ? Colors.white : Colors.black)
                                .withValues(alpha: 0.12),
                        thumbColor: const Color(0xFF00B4D8),
                        thumbShape:
                            const RoundSliderThumbShape(enabledThumbRadius: 6),
                        overlayShape:
                            const RoundSliderOverlayShape(overlayRadius: 12),
                        trackShape: const RectangularSliderTrackShape(),
                      ),
                      child: Slider(
                        value: _currentIndex.toDouble(),
                        min: 0,
                        max: (_frames.length > 1 ? _frames.length - 1 : 1)
                            .toDouble(),
                        divisions:
                            _frames.length > 1 ? _frames.length - 1 : 1,
                        onChanged: _frames.length > 1
                            ? (val) => _seekToFrame(val.round())
                            : null,
                      ),
                    ),
                  ],
                ),
              ),
            ),

          // ─── Rain Intensity Legend ─────────────────────────────────────────
          Positioned(
            bottom: 24,
            left: 16,
            child: Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: (isDark ? const Color(0xFF131738) : Colors.white)
                    .withValues(alpha: 0.9),
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.2),
                    blurRadius: 8,
                  )
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    l10n.rainIntensity,
                    style: TextStyle(
                      color: isDark ? Colors.white : Colors.black87,
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Row(children: [
                    _legendDot(const Color(0xFF0000FF)),
                    _legendDot(const Color(0xFF00FF00)),
                    _legendDot(const Color(0xFFFFFF00)),
                    _legendDot(const Color(0xFFFFA500)),
                    _legendDot(const Color(0xFFFF0000)),
                    const SizedBox(width: 4),
                    Text(
                      l10n.lightToHeavy,
                      style: TextStyle(
                        color: isDark ? Colors.white70 : Colors.black54,
                        fontSize: 10,
                      ),
                    ),
                  ]),
                ],
              ),
            ),
          ),

          // Loading indicator
          if (radarAsync.isLoading)
            Positioned(
              top: 16,
              left: 16,
              child: Card(
                color: isDark ? const Color(0xFF131738) : Colors.white,
                child: Padding(
                  padding: const EdgeInsets.all(8),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'Loading radar...',
                        style: TextStyle(
                          fontSize: 12,
                          color: isDark ? Colors.white : Colors.black87,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _mapControlButton({
    required IconData icon,
    required bool isDark,
    required VoidCallback onPressed,
  }) {
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: (isDark ? const Color(0xFF131738) : Colors.white).withValues(alpha: 0.9),
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.2),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: IconButton(
        icon: Icon(icon, size: 20),
        color: isDark ? Colors.white : Colors.black87,
        onPressed: onPressed,
        padding: EdgeInsets.zero,
      ),
    );
  }

  String _getRelativeTimeLabel(RadarFrame frame, AppLocalizations l10n) {
    final now = DateTime.now();
    final diff = now.difference(frame.dateTime);
    if (frame.isNowcast) {
      final minutes = (-diff.inMinutes).clamp(0, 120);
      return '+$minutes min';
    } else {
      final minutes = diff.inMinutes.clamp(0, 240);
      return '-$minutes min';
    }
  }

  Widget _legendDot(Color color) {
    return Container(
      width: 14,
      height: 14,
      margin: const EdgeInsets.only(right: 2),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(3),
      ),
    );
  }
}
