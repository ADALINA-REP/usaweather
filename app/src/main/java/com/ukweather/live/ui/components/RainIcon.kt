package com.ukweather.live.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RainIcon(
    modifier: Modifier = Modifier,
    dropCount: Int = 3,
    iconSize: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain_animation")
    
    // Animate drops falling
    val animationStates = (0 until dropCount).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000 + (i * 200), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "drop_$i"
        )
    }

    Canvas(modifier = modifier.size(iconSize)) {
        val width = size.width
        val height = size.height
        
        // 1. Draw Cloud
        val cloudPath = Path().apply {
            val startX = width * 0.2f
            val startY = height * 0.6f
            moveTo(startX, startY)
            
            // Bottom line
            lineTo(width * 0.8f, startY)
            
            // Arches
            cubicTo(width * 0.95f, startY, width * 0.95f, height * 0.3f, width * 0.7f, height * 0.35f)
            cubicTo(width * 0.65f, height * 0.1f, width * 0.35f, height * 0.1f, width * 0.3f, height * 0.35f)
            cubicTo(width * 0.05f, height * 0.3f, width * 0.05f, startY, startX, startY)
            close()
        }
        
        drawPath(
            path = cloudPath,
            color = Color.White,
            style = Fill
        )
        
        // 2. Draw animated raindrops
        val dropWidth = width * 0.05f
        val dropHeight = height * 0.18f
        val startY = height * 0.62f
        val maxY = height * 0.98f
        
        // Intensity logic spacing (2, 3, 5 drops)
        val dropAreaWidth = width * 0.5f
        val startX = (width - dropAreaWidth) / 2f
        val spacing = if (dropCount > 1) dropAreaWidth / (dropCount - 1) else 0f

        animationStates.forEachIndexed { i, anim ->
            val x = if (dropCount == 1) width / 2f else startX + (i * spacing)
            val currentY = startY + (anim.value * (maxY - startY))
            val alpha = (1f - (anim.value * 0.8f)).coerceAtLeast(0.4f) // Better visibility
            
            drawRoundRect(
                color = com.ukweather.live.ui.theme.WeatherRainLight.copy(alpha = alpha),
                topLeft = Offset(x - dropWidth/2, currentY),
                size = Size(dropWidth, dropHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(dropWidth / 2)
            )
        }
    }
}



