// lib/ui/components/weather_chart_painters.dart
import 'dart:math' as math;
import 'package:flutter/material.dart';

/// 1. Golden Temperature Line connecting points across the hourly pods
class HourlyTempLinePainter extends CustomPainter {
  final List<double> temperatures;
  final double itemWidth;
  final double podWidth;
  final Color lineColor;
  final bool isDark;

  HourlyTempLinePainter({
    required this.temperatures,
    required this.itemWidth,
    this.podWidth = 68.0,
    this.lineColor = const Color(0xFFFBBF24),
    this.isDark = true,
  });

  @override
  void paint(Canvas canvas, Size size) {
    if (temperatures.isEmpty) return;

    final paint = Paint()
      ..color = lineColor
      ..strokeWidth = 3.2
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;

    final glowPaint = Paint()
      ..color = lineColor.withValues(alpha: isDark ? 0.40 : 0.25)
      ..strokeWidth = 6.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;

    final dotPaint = Paint()
      ..color = isDark ? const Color(0xFF1E293B) : Colors.white
      ..style = PaintingStyle.fill;

    final dotBorderPaint = Paint()
      ..color = lineColor
      ..strokeWidth = 2.8
      ..style = PaintingStyle.stroke;

    final double minT = temperatures.reduce(math.min);
    final double maxT = temperatures.reduce(math.max);
    final double rawRange = (maxT - minT).abs();

    // Use a wide range denominator (14.0°) so the rate of temperature change (slope)
    // is low, producing a gentle, elegant, calm curve without steep vertical jumps.
    final double range = math.max(rawRange, 14.0);

    // Center Y at 114.0px (middle dot zone between icon and rain indicator)
    // Max vertical amplitude = 12.0px (gentle low rate of change)
    const double centerY = 114.0;
    const double maxAmplitude = 12.0;

    final path = Path();
    final points = <Offset>[];

    for (int i = 0; i < temperatures.length; i++) {
      final x = (i * itemWidth) + (podWidth / 2);
      final normalized = range > 0
          ? ((temperatures[i] - minT) / range).clamp(0.0, 1.0)
          : 0.5;
      final y = centerY - ((normalized - 0.5) * maxAmplitude * 2);
      points.add(Offset(x, y));

      if (i == 0) {
        path.moveTo(x, y);
      } else {
        final prev = points[i - 1];
        final midX = (prev.dx + x) / 2;
        path.cubicTo(midX, prev.dy, midX, y, x, y);
      }
    }

    // Draw subtle gradient glow fill under the curve
    if (points.isNotEmpty) {
      final fillPath = Path.from(path)
        ..lineTo(points.last.dx, size.height)
        ..lineTo(points.first.dx, size.height)
        ..close();

      final fillPaint = Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            lineColor.withValues(alpha: isDark ? 0.22 : 0.15),
            lineColor.withValues(alpha: 0.0),
          ],
        ).createShader(Rect.fromLTWH(0, 0, size.width, size.height))
        ..style = PaintingStyle.fill;

      canvas.drawPath(fillPath, fillPaint);
    }

    // Draw glowing background stroke for high foreground visibility
    canvas.drawPath(path, glowPaint);

    // Draw main continuous golden temperature curve in foreground
    canvas.drawPath(path, paint);

    // Draw glowing foreground dots on each hourly card
    for (final p in points) {
      canvas.drawCircle(p, 4.0, dotPaint);
      canvas.drawCircle(p, 4.0, dotBorderPaint);
    }
  }

  @override
  bool shouldRepaint(covariant HourlyTempLinePainter oldDelegate) =>
      oldDelegate.temperatures != temperatures ||
      oldDelegate.lineColor != lineColor ||
      oldDelegate.itemWidth != itemWidth ||
      oldDelegate.podWidth != podWidth ||
      oldDelegate.isDark != isDark;
}

/// 2. Smooth Humidity Wave Graph
class HumidityWavePainter extends CustomPainter {
  final Color waveColor;
  final Color fillColor;

  HumidityWavePainter({
    this.waveColor = const Color(0xFF38BDF8),
    this.fillColor = const Color(0x1F38BDF8),
  });

  @override
  void paint(Canvas canvas, Size size) {
    final strokePaint = Paint()
      ..color = waveColor
      ..strokeWidth = 3.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final fillPaint = Paint()
      ..color = fillColor
      ..style = PaintingStyle.fill;

    final path = Path();
    final w = size.width;
    final h = size.height;

    path.moveTo(0, h * 0.45);
    path.cubicTo(w * 0.25, h * 0.40, w * 0.35, h * 0.50, w * 0.55, h * 0.48);
    path.cubicTo(w * 0.70, h * 0.55, w * 0.85, h * 0.85, w, h * 0.65);

    final closedPath = Path.from(path)
      ..lineTo(w, h)
      ..lineTo(0, h)
      ..close();

    canvas.drawPath(closedPath, fillPaint);
    canvas.drawPath(path, strokePaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

/// 3. Circular Pressure Gauge (Speedometer arc)
class PressureGaugePainter extends CustomPainter {
  final double pressure; // e.g. 980 to 1040
  final bool isDark;

  PressureGaugePainter({
    required this.pressure,
    required this.isDark,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height * 0.6);
    final radius = size.width * 0.38;

    final trackPaint = Paint()
      ..color = (isDark ? Colors.white : const Color(0xFF0F172A)).withValues(alpha: isDark ? 0.12 : 0.08)
      ..strokeWidth = 5.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final activePaint = Paint()
      ..shader = const LinearGradient(
        colors: [Color(0xFF38BDF8), Color(0xFF818CF8)],
      ).createShader(Rect.fromCircle(center: center, radius: radius))
      ..strokeWidth = 5.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    const startAngle = math.pi * 0.8;
    const sweepAngle = math.pi * 1.4;

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle,
      false,
      trackPaint,
    );

    // Normalize pressure between 970 and 1050
    final ratio = ((pressure - 970) / (1050 - 970)).clamp(0.1, 0.95);
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepAngle * ratio,
      false,
      activePaint,
    );

    // Tip dot
    final tipAngle = startAngle + (sweepAngle * ratio);
    final tipX = center.dx + radius * math.cos(tipAngle);
    final tipY = center.dy + radius * math.sin(tipAngle);
    final dotPaint = Paint()..color = isDark ? Colors.white : const Color(0xFF0284C7);
    canvas.drawCircle(Offset(tipX, tipY), 3.5, dotPaint);
  }

  @override
  bool shouldRepaint(covariant PressureGaugePainter oldDelegate) =>
      oldDelegate.pressure != pressure || oldDelegate.isDark != isDark;
}

/// 4. UV Rainbow Spectrum Bar with Indicator Marker
class UvSpectrumPainter extends CustomPainter {
  final double uvIndex;
  final bool isDark;

  UvSpectrumPainter({
    required this.uvIndex,
    this.isDark = true,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final rrect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, 0, size.width, size.height),
      const Radius.circular(4),
    );

    final gradPaint = Paint()
      ..shader = const LinearGradient(
        colors: [
          Color(0xFF22C55E), // Green (Low)
          Color(0xFFEAB308), // Yellow (Moderate)
          Color(0xFFF97316), // Orange (High)
          Color(0xFFEF4444), // Red (Very High)
          Color(0xFF9333EA), // Purple (Extreme)
        ],
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height));

    canvas.drawRRect(rrect, gradPaint);

    // Pointer/marker line
    final ratio = (uvIndex / 11.0).clamp(0.05, 0.95);
    final markerX = size.width * ratio;

    final markerPaint = Paint()
      ..color = isDark ? Colors.white : const Color(0xFF0F172A)
      ..strokeWidth = 2.5
      ..strokeCap = StrokeCap.round;

    canvas.drawLine(
      Offset(markerX, -2),
      Offset(markerX, size.height + 6),
      markerPaint,
    );
  }

  @override
  bool shouldRepaint(covariant UvSpectrumPainter oldDelegate) =>
      oldDelegate.uvIndex != uvIndex || oldDelegate.isDark != isDark;
}

/// 5. Circular Compass Dial with Direction Needle
class WindCompassPainter extends CustomPainter {
  final int degrees;
  final bool isDark;

  WindCompassPainter({
    required this.degrees,
    required this.isDark,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width * 0.42;

    // Outer circle
    final circlePaint = Paint()
      ..color = (isDark ? Colors.white : const Color(0xFF0F172A)).withValues(alpha: isDark ? 0.10 : 0.05)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, radius, circlePaint);

    final borderPaint = Paint()
      ..color = (isDark ? Colors.white : const Color(0xFF0F172A)).withValues(alpha: isDark ? 0.18 : 0.14)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0;
    canvas.drawCircle(center, radius, borderPaint);

    // North label
    final textPainter = TextPainter(
      text: TextSpan(
        text: 'N',
        style: TextStyle(
          color: isDark ? Colors.white70 : const Color(0xFF0F172A),
          fontSize: 10,
          fontWeight: FontWeight.bold,
        ),
      ),
      textDirection: TextDirection.ltr,
    )..layout();
    textPainter.paint(
      canvas,
      Offset(center.dx - (textPainter.width / 2), center.dy - radius + 4),
    );

    // Arrow pointing to degrees
    final rad = (degrees - 90) * (math.pi / 180.0);
    final arrowLength = radius * 0.60;
    final tip = Offset(
      center.dx + arrowLength * math.cos(rad),
      center.dy + arrowLength * math.sin(rad),
    );
    final baseRad1 = rad + (math.pi * 0.85);
    final baseRad2 = rad - (math.pi * 0.85);
    final base1 = Offset(
      center.dx + (radius * 0.28) * math.cos(baseRad1),
      center.dy + (radius * 0.28) * math.sin(baseRad1),
    );
    final base2 = Offset(
      center.dx + (radius * 0.28) * math.cos(baseRad2),
      center.dy + (radius * 0.28) * math.sin(baseRad2),
    );

    final arrowPath = Path()
      ..moveTo(tip.dx, tip.dy)
      ..lineTo(base1.dx, base1.dy)
      ..lineTo(center.dx, center.dy)
      ..lineTo(base2.dx, base2.dy)
      ..close();

    final arrowPaint = Paint()
      ..color = const Color(0xFF0284C7)
      ..style = PaintingStyle.fill;

    canvas.drawPath(arrowPath, arrowPaint);
  }

  @override
  bool shouldRepaint(covariant WindCompassPainter oldDelegate) =>
      oldDelegate.degrees != degrees || oldDelegate.isDark != isDark;
}

/// 6. Solar Cycle Glowing Parabolic Sun Arc
class SolarArcPainter extends CustomPainter {
  final double progress; // 0.0 (sunrise) to 1.0 (sunset)
  final bool isNight;
  final bool isDark;

  SolarArcPainter({
    required this.progress,
    required this.isNight,
    this.isDark = true,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;

    final baseLinePaint = Paint()
      ..color = (isDark ? Colors.white : const Color(0xFF0F172A)).withValues(alpha: isDark ? 0.15 : 0.12)
      ..strokeWidth = 1.0;
    canvas.drawLine(Offset(12, h - 14), Offset(w - 12, h - 14), baseLinePaint);

    final arcPath = Path();
    arcPath.moveTo(16, h - 14);
    arcPath.quadraticBezierTo(w / 2, -10, w - 16, h - 14);

    // Glowing gradient fill under arc
    final fillPath = Path.from(arcPath)
      ..lineTo(w - 16, h - 14)
      ..lineTo(16, h - 14)
      ..close();

    final fillPaint = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [
          const Color(0xFFF59E0B).withValues(alpha: isDark ? 0.25 : 0.18),
          const Color(0xFFF59E0B).withValues(alpha: 0.0),
        ],
      ).createShader(Rect.fromLTWH(0, 0, w, h));

    canvas.drawPath(fillPath, fillPaint);

    // Glowing Arc stroke
    final arcPaint = Paint()
      ..color = const Color(0xFFF59E0B)
      ..strokeWidth = 3.5
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;
    canvas.drawPath(arcPath, arcPaint);

    // Start & End Dots
    final dotPaint = Paint()..color = isDark ? Colors.white : const Color(0xFFD97706);
    canvas.drawCircle(Offset(16, h - 14), 4.0, dotPaint);
    canvas.drawCircle(Offset(w - 16, h - 14), 4.0, dotPaint);

    // Sun / Moon on arc
    if (!isNight) {
      final t = progress.clamp(0.0, 1.0);
      // Quadratic bezier point: (1-t)^2*P0 + 2(1-t)t*P1 + t^2*P2
      final p0 = Offset(16, h - 14);
      final p1 = Offset(w / 2, -10);
      final p2 = Offset(w - 16, h - 14);
      final sunX = math.pow(1 - t, 2) * p0.dx + 2 * (1 - t) * t * p1.dx + math.pow(t, 2) * p2.dx;
      final sunY = math.pow(1 - t, 2) * p0.dy + 2 * (1 - t) * t * p1.dy + math.pow(t, 2) * p2.dy;

      // Sun glow
      final glowPaint = Paint()
        ..color = const Color(0xFFF59E0B).withValues(alpha: 0.4)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);
      canvas.drawCircle(Offset(sunX, sunY), 10, glowPaint);

      final sunPaint = Paint()..color = isDark ? Colors.white : const Color(0xFFFFFBEB);
      canvas.drawCircle(Offset(sunX, sunY), 6, sunPaint);

      if (!isDark) {
        final sunBorderPaint = Paint()
          ..color = const Color(0xFFD97706)
          ..style = PaintingStyle.stroke
          ..strokeWidth = 1.5;
        canvas.drawCircle(Offset(sunX, sunY), 6, sunBorderPaint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant SolarArcPainter oldDelegate) =>
      oldDelegate.progress != progress ||
      oldDelegate.isNight != isNight ||
      oldDelegate.isDark != isDark;
}

