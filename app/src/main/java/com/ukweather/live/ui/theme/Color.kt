package com.ukweather.live.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 🎨 NEW UK WEATHER COLOR SYSTEM
// ==========================================

// --- Light Mode ---
val BrandPrimaryLight = Color(0xFF2563EB)
val BrandPrimaryDarkVariant = Color(0xFF1D4ED8)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SecondaryBackgroundLight = Color(0xFFEFF6FF)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

val BorderLight = Color(0xFFE2E8F0)

// --- Dark Mode ---
val BackgroundDark = Color(0xFF0B1220)
val SurfaceDark = Color(0xFF111827)
val SecondaryCardDark = Color(0xFF172033)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

val BorderDark = Color(0xFF1E293B)
val BrandPrimaryDark = Color(0xFF3B82F6)

// --- Status Colors ---
val StatusSuccess = Color(0xFF22C55E)
val StatusWarning = Color(0xFFF59E0B)
val StatusDangerLight = Color(0xFFEF4444)
val StatusDangerDark = Color(0xFFF87171)

// --- Weather Colors (Light) ---
val WeatherRainLight = Color(0xFF0EA5E9)
val WeatherSnowLight = Color(0xFF38BDF8)
val WeatherThunderstormLight = Color(0xFF7C3AED)
val WeatherCloudyLight = Color(0xFF64748B)
val WeatherSunnyLight = Color(0xFFFBBF24)

// --- Weather Colors (Dark) ---
val WeatherRainDark = Color(0xFF38BDF8)
val WeatherSnowDark = Color(0xFF38BDF8)
val WeatherThunderstormDark = Color(0xFF8B5CF6)
val WeatherCloudyDark = Color(0xFF94A3B8)
val WeatherSunnyDark = Color(0xFFFBBF24)

// --- Legacy & Compatibility Mappings ---
val PrimaryBlue = BrandPrimaryLight
val SecondaryBlue = BrandPrimaryDark
val SkyCastRoyalBlueStart = BrandPrimaryDarkVariant
val SkyCastRoyalBlueEnd = BrandPrimaryLight
val SkyCastRoyalBlueMiddle = Color(0xFF1E40AF)
val SkyCastSolarYellow = WeatherSunnyLight
val SkyCastAccent = StatusWarning
val TextLight = TextPrimaryLight
val TextDark = TextPrimaryDark

// Glassmorphism tokens
val GlassLight = Color(0x1A0F172A)
val GlassDark = Color(0x33000000)
val SurfaceGlassLight = Color(0xCCFFFFFF)
val SurfaceGlassDark = Color(0xD9111827)

// --- Weather-Specific Background Gradients ---
val WeatherSunnyStart = Color(0xFF1D4ED8)
val WeatherSunnyEnd = Color(0xFF3B82F6)

val WeatherRainyStart = Color(0xFF1E293B)
val WeatherRainyEnd = Color(0xFF0EA5E9)

val WeatherCloudyStart = Color(0xFF334155)
val WeatherCloudyEnd = Color(0xFF64748B)

val WeatherStormyStart = Color(0xFF0B1220)
val WeatherStormyEnd = Color(0xFF7C3AED)

val WeatherNightStart = Color(0xFF0B1220)
val WeatherNightEnd = Color(0xFF172033)

val WeatherSnowStart = Color(0xFF334155)
val WeatherSnowEnd = Color(0xFF38BDF8)

val WeatherFogStart = Color(0xFF475569)
val WeatherFogEnd = Color(0xFF64748B)

