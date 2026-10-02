package com.ukweather.live.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ukweather.live.util.WeatherState

/**
 * A robust, vector-drawn weather icon component that avoids emoji rendering issues
 * and provides a consistent premium look across all devices.
 */
@Composable
fun WeatherIcon(
    state: WeatherState,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (state) {
            WeatherState.SUNNY -> {
                drawUHDSun(w, h)
            }
            WeatherState.NIGHT -> {
                drawUHDMoonCore(w, h, Offset(w * 0.5f, h * 0.5f), w * 0.35f, Color(0xFFE0E0E0))
            }
            WeatherState.CLOUDY, WeatherState.FOG -> {
                // Background cloud (darker/recessed)
                drawUHDCloud(w, h, Color(0xFF90A4AE), xOffset = -w * 0.1f, yOffset = -h * 0.15f, scale = 0.75f)
                // Foreground cloud (bright/prominent)
                drawUHDCloud(w, h, Color(0xFFFFFFFF))
            }
            WeatherState.RAIN, WeatherState.DRIZZLE -> {
                // Diagonal heavy rain streaks
                drawUHDRainDrops(w, h)
                // Ominous layered clouds
                drawUHDCloud(w, h, Color(0xFF607D8B), xOffset = -w * 0.15f, yOffset = -h * 0.1f, scale = 0.85f)
                drawUHDCloud(w, h, Color(0xFFB0BEC5))
            }
            WeatherState.STORM -> {
                // Background dark mass
                drawUHDCloud(w, h, Color(0xFF37474F), xOffset = -w * 0.1f, yOffset = -h * 0.1f, scale = 0.8f)
                
                // Aggressive Lightning
                val bolt = Path().apply {
                    moveTo(w * 0.55f, h * 0.4f)
                    lineTo(w * 0.35f, h * 0.75f)
                    lineTo(w * 0.5f, h * 0.75f)
                    lineTo(w * 0.45f, h * 1.05f)
                    lineTo(w * 0.7f, h * 0.65f)
                    lineTo(w * 0.55f, h * 0.65f)
                    close()
                }
                
                // Bolt Glow
                drawPath(bolt, androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF176).copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.7f),
                    radius = w * 0.4f
                ))
                // Bolt Core
                drawPath(bolt, androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color.White, Color(0xFFFFD54F)),
                    start = Offset(w * 0.5f, h * 0.4f),
                    end = Offset(w * 0.5f, h * 1f)
                ), style = Fill)
                
                // Foreground stormy cloud
                drawUHDCloud(w, h, Color(0xFF78909C))
            }
            WeatherState.SNOW -> {
                drawUHDCloud(w, h, Color(0xFFCFD8DC), xOffset = -w * 0.1f, yOffset = -h * 0.1f, scale = 0.8f)
                drawUHDSnowOrbs(w, h)
                drawUHDCloud(w, h, Color(0xFFFFFFFF))
            }
        }
    }
}

private fun buildVolumetricCloudPath(w: Float, h: Float): Path {
    val c1 = Path().apply { addOval(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.45f, w * 0.55f, h * 0.85f)) }
    val c2 = Path().apply { addOval(androidx.compose.ui.geometry.Rect(w * 0.35f, h * 0.25f, w * 0.85f, h * 0.75f)) }
    val c3 = Path().apply { addOval(androidx.compose.ui.geometry.Rect(w * 0.6f, h * 0.5f, w * 0.95f, h * 0.85f)) }
    val base = Path().apply { addRect(androidx.compose.ui.geometry.Rect(w * 0.35f, h * 0.6f, w * 0.75f, h * 0.85f)) }
    
    // Merge paths cleanly without internal overlaps bridging when rendered
    val p1 = Path().apply { op(c1, c2, androidx.compose.ui.graphics.PathOperation.Union) }
    val p2 = Path().apply { op(p1, c3, androidx.compose.ui.graphics.PathOperation.Union) }
    return Path().apply { op(p2, base, androidx.compose.ui.graphics.PathOperation.Union) }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUHDCloud(w: Float, h: Float, color: Color, xOffset: Float = 0f, yOffset: Float = 0f, scale: Float = 1f) {
    drawContext.canvas.save()
    drawContext.transform.translate(xOffset, yOffset)
    drawContext.transform.translate(w/2 * (1-scale), h/2 * (1-scale))
    drawContext.transform.scale(scale, scale, Offset(0f, 0f))

    val cloudPath = buildVolumetricCloudPath(w, h)

    // Deep Volumetric Shadow
    drawPath(
        cloudPath, 
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.65f),
            radius = w * 0.8f
        )
    )

    // Glassmorphic Frost Fill
    drawPath(
        cloudPath, 
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.95f), color.copy(alpha = 0.85f), color.copy(alpha = 0.6f)),
            start = Offset(w * 0.2f, h * 0.1f),
            end = Offset(w * 0.8f, h * 0.9f)
        )
    )

    // Surgical Rim Light (Specularity edge reflection)
    drawPath(
        cloudPath, 
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color.White, Color.Transparent, Color.White.copy(alpha = 0.5f)),
            start = Offset(w * 0.1f, h * 0.2f),
            end = Offset(w * 0.9f, h * 0.8f)
        ), 
        style = Stroke(width = w * 0.025f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    )
    
    drawContext.canvas.restore()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUHDSun(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    
    // Corona Scattering (Massive Glow)
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color(0xFFFFB300).copy(alpha = 0.4f), Color(0xFFFF9800).copy(alpha = 0.1f), Color.Transparent),
            center = center,
            radius = w * 0.7f
        ),
        radius = w * 0.7f,
        center = center
    )
    
    // Core Fusion (Intense internal gradient)
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFCA28), Color(0xFFFF8F00)),
            center = Offset(w * 0.45f, h * 0.45f), // Offset center for 3D sphere feel
            radius = w * 0.35f
        ),
        radius = w * 0.35f,
        center = center
    )
    
    // Plasma Ring edge highlight
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent),
            start = Offset(w * 0.2f, h * 0.2f),
            end = Offset(w * 0.8f, h * 0.8f)
        ),
        radius = w * 0.35f,
        center = center,
        style = Stroke(width = w * 0.02f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUHDMoonCore(w: Float, h: Float, center: Offset, r: Float, color: Color) {
    // Silver ambient glow
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
            center = center,
            radius = r * 1.5f
        ),
        radius = r * 1.5f,
        center = center
    )
    
    // Moon Body 3D gradient
    val moonBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(Color(0xFFF5F5F5), Color(0xFF90A4AE)),
        start = Offset(center.x - r, center.y - r),
        end = Offset(center.x + r, center.y + r)
    )
    drawCircle(brush = moonBrush, radius = r, center = center)
    
    // Subtle Craters for UHD aesthetic
    drawCircle(Color.Black.copy(alpha = 0.07f), radius = r * 0.15f, center = Offset(center.x + r * 0.2f, center.y - r * 0.3f))
    drawCircle(Color.Black.copy(alpha = 0.05f), radius = r * 0.1f, center = Offset(center.x - r * 0.3f, center.y + r * 0.4f))
    drawCircle(Color.Black.copy(alpha = 0.06f), radius = r * 0.25f, center = Offset(center.x + r * 0.4f, center.y + r * 0.2f))
    
    // Eclipse/Shadow bite
    drawCircle(
        color = Color.Transparent, 
        radius = r * 0.85f,
        center = Offset(center.x + r * 0.6f, center.y - r * 0.3f),
        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUHDRainDrops(w: Float, h: Float) {
    val dropBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(com.ukweather.live.ui.theme.WeatherSnowLight.copy(alpha=0.6f), com.ukweather.live.ui.theme.WeatherRainLight.copy(alpha=0.9f)),
        start = Offset(0f, 0f),
        end = Offset(w * 0.1f, h * 0.3f)
    )
    
    val drawDrop = { cx: Float, cy: Float, radius: Float, length: Float ->
        val dropPath = Path().apply {
            moveTo(cx, cy - length)
            cubicTo(cx + radius, cy, cx + radius, cy + length, cx, cy + length)
            cubicTo(cx - radius, cy + length, cx - radius, cy, cx, cy - length)
            close()
        }
        drawContext.canvas.save()
        drawContext.transform.rotate(20f, Offset(cx, cy))
        drawPath(dropPath, brush = dropBrush)
        drawContext.canvas.restore()
    }

    drawDrop(w * 0.35f, h * 0.8f, w * 0.04f, h * 0.15f)
    drawDrop(w * 0.55f, h * 0.95f, w * 0.035f, h * 0.12f)
    drawDrop(w * 0.75f, h * 0.85f, w * 0.045f, h * 0.18f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUHDSnowOrbs(w: Float, h: Float) {
    val snowBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(Color.White, Color.White.copy(alpha=0.1f), Color.Transparent)
    )
    
    val drawOrb = { cx: Float, cy: Float, radius: Float ->
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color.White, Color.White.copy(alpha=0.2f), Color.Transparent),
                center = Offset(cx, cy),
                radius = radius * 2.5f
            ),
            radius = radius * 2f,
            center = Offset(cx, cy)
        )
        drawCircle(Color.White.copy(alpha=0.9f), radius = radius, center = Offset(cx, cy))
    }

    drawOrb(w * 0.3f, h * 0.85f, w * 0.025f)
    drawOrb(w * 0.55f, h * 0.95f, w * 0.02f)
    drawOrb(w * 0.75f, h * 0.88f, w * 0.03f)
}
/**
 * A dynamic, high-fidelity moon phase icon that programmatically draws the 
 * lunar surface and its shadow based on the current cycle (0.0 to 1.0).
 */
@Composable
fun MoonPhaseIcon(
    phase: Double, 
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = w * 0.45f
        val center = Offset(w / 2, h / 2)

        // 1. Base Layer (Shadow/Dark side)
        drawCircle(
            color = Color(0xFF263238), 
            radius = r, 
            center = center
        )

        // 2. Surface Layer (The lit part)
        // Logic for Moon Geometry: 
        // 0.0: New Moon
        // 0.25: First Quarter
        // 0.5: Full Moon
        // 0.75: Last Quarter
        
        val path = Path()
        if (phase <= 0.5) {
            // Waxing (Filling from right)
            val factor = (phase / 0.5).toFloat() * 2f - 1f // -1 to 1
            // Left half is shadow, right half depends
            path.addArc(
                androidx.compose.ui.geometry.Rect(center.x - r, center.y - r, center.x + r, center.y + r),
                90f,
                180f
            )
            // The terminator arc
            val terminatorWidth = r * factor
            path.addArc(
                androidx.compose.ui.geometry.Rect(center.x - terminatorWidth, center.y - r, center.x + terminatorWidth, center.y + r),
                270f,
                180f
            )
        } else {
            // Waning (Emptying from left)
            val factor = ((phase - 0.5) / 0.5).toFloat() * 2f - 1f // -1 to 1
            path.addArc(
                androidx.compose.ui.geometry.Rect(center.x - r, center.y - r, center.x + r, center.y + r),
                270f,
                180f
            )
            val terminatorWidth = r * factor
            path.addArc(
                androidx.compose.ui.geometry.Rect(center.x - terminatorWidth, center.y - r, center.x + terminatorWidth, center.y + r),
                90f,
                180f
            )
        }

        // Apply Gold/Silver gradient for the lit part
        val moonBrush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F)),
            center = Offset(w * 0.4f, h * 0.4f),
            radius = r * 1.2f
        )
        
        drawPath(path, brush = moonBrush)
        
        // 3. Craters for UHD realism (only on lit part)
        drawContext.canvas.save()
        drawContext.canvas.clipPath(path)
        drawCircle(Color.Black.copy(alpha = 0.05f), radius = r * 0.15f, center = Offset(w * 0.45f, h * 0.42f))
        drawCircle(Color.Black.copy(alpha = 0.05f), radius = r * 0.1f, center = Offset(w * 0.55f, h * 0.55f))
        drawCircle(Color.Black.copy(alpha = 0.05f), radius = r * 0.08f, center = Offset(w * 0.4f, h * 0.65f))
        drawContext.canvas.restore()

        // 4. Outer Glow
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.15f), Color.Transparent),
                center = center,
                radius = r * 1.5f
            ),
            radius = r * 1.3f,
            center = center
        )
    }
}
