// lib/ui/components/glass_card.dart
import 'dart:ui';
import 'package:flutter/material.dart';

/// ─────────────────────────────────────────────────────────────────────────────
/// Centralized Master GlassCard Component for Premium Ultra-Clean Glass Weather UI
/// Pure single-surface transparent glass without inner shadows or plastic sheen.
/// ─────────────────────────────────────────────────────────────────────────────
class GlassCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final double borderRadius;
  final double? width;
  final double? height;
  final VoidCallback? onTap;
  final Color? backgroundColor;
  final Border? border;
  final bool isSelected;
  final Color? accentColor;
  final double glassOpacity;
  final double glassBlur;
  final double glassBorderOpacity;
  final bool showHighlight;

  const GlassCard({
    super.key,
    required this.child,
    this.padding,
    this.margin,
    this.borderRadius = 24,
    this.width,
    this.height,
    this.onTap,
    this.backgroundColor,
    this.border,
    this.isSelected = false,
    this.accentColor,
    this.glassOpacity = 0.15,
    this.glassBlur = 20.0,
    this.glassBorderOpacity = 0.25,
    this.showHighlight = false,
  });

  @override
  Widget build(BuildContext context) {
    // True transparent glass surface color (12% to 20% white opacity)
    final double effectiveOpacity = isSelected
        ? 0.20
        : (backgroundColor != null
            ? backgroundColor!.a
            : glassOpacity);

    final Color baseBgColor = backgroundColor != null
        ? backgroundColor!.withValues(alpha: effectiveOpacity)
        : Colors.white.withValues(alpha: effectiveOpacity);

    // Subtle cyan or crisp white border (never heavy or dark)
    final Color effectiveAccent = accentColor ?? const Color(0xFF00B4D8);
    final Border defaultBorder = Border.all(
      color: isSelected
          ? effectiveAccent.withValues(alpha: 0.55)
          : Colors.white.withValues(alpha: glassBorderOpacity),
      width: isSelected ? 1.5 : 1.0,
    );

    return Container(
      margin: margin,
      width: width,
      height: height,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: glassBlur, sigmaY: glassBlur),
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: onTap,
              borderRadius: BorderRadius.circular(borderRadius),
              highlightColor: Colors.white.withValues(alpha: 0.08),
              splashColor: Colors.white.withValues(alpha: 0.05),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                padding: padding ?? const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: baseBgColor,
                  borderRadius: BorderRadius.circular(borderRadius),
                  border: border ?? defaultBorder,
                  // NO inner/clipped shadows to keep glass ultra-clean and seamless
                ),
                child: child,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
