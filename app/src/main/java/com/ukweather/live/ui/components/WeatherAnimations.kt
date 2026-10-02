package com.ukweather.live.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun AnimatedRain(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rain_drop"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(42)
        for (i in 0..100) {
            val x = random.nextFloat() * size.width
            val yOffset = random.nextFloat() * size.height * 2
            val length = random.nextFloat() * 30f + 20f
            val dropY = (offsetY + yOffset) % size.height
            val alpha = random.nextFloat() * 0.4f + 0.1f
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x, dropY),
                end = Offset(x, dropY + length),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun AnimatedSun(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sun_glow"
    )

    Canvas(modifier = modifier) {
        // Outer glow
        drawCircle(
            color = com.ukweather.live.ui.theme.WeatherSunnyLight.copy(alpha = 0.3f),
            radius = size.minDimension / 2 * glow
        )
        // Inner sun
        drawCircle(
            color = com.ukweather.live.ui.theme.WeatherSunnyLight,
            radius = size.minDimension / 2.5f
        )
    }
}

@Composable
fun AnimatedClouds(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "cloud_move"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(123)
        for (i in 0..5) {
            val y = random.nextFloat() * size.height * 0.4f
            val startX = random.nextFloat() * size.width
            val speedMultip = random.nextFloat() * 0.5f + 0.5f
            val cloudX = (startX + offsetX * speedMultip) % (size.width + 400f) - 200f
            
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 120f + random.nextFloat() * 80f,
                center = Offset(cloudX, y)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 90f + random.nextFloat() * 60f,
                center = Offset(cloudX + 100f, y + 40f)
            )
        }
    }
}
@Composable
fun AnimatedSnow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "snow_fall"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(99)
        for (i in 0..80) {
            val x = random.nextFloat() * size.width
            val startY = random.nextFloat() * size.height * 2
            val radius = random.nextFloat() * 4f + 2f
            val snowY = (offsetY + startY) % size.height
            
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = radius,
                center = Offset(x, snowY)
            )
        }
    }
}

@Composable
fun AnimatedNight(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "night")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "star_twinkle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(7)
        for (i in 0..60) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            val starAlpha = random.nextFloat() * 0.6f + 0.2f
            val baseRadius = random.nextFloat() * 2f + 1f
            
            drawCircle(
                color = Color.White.copy(alpha = starAlpha * twinkle),
                radius = baseRadius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun AnimatedThunderstorm(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "storm")
    
    // Rain part
    val rainY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "storm_rain"
    )
    
    // Lightning part
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0
                0f at 4000
                1f at 4050
                0f at 4100
                1f at 4150
                0f at 4300
            },
            repeatMode = RepeatMode.Restart
        ), label = "lightning"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw flashes
        if (lightningAlpha > 0.1f) {
            drawRect(
                color = Color.White.copy(alpha = lightningAlpha * 0.2f),
                size = size
            )
        }
        
        // Draw heavy rain
        val random = Random(42)
        for (i in 0..150) {
            val x = random.nextFloat() * size.width
            val yOffset = random.nextFloat() * size.height * 2
            val length = random.nextFloat() * 40f + 30f
            val dropY = (rainY + yOffset) % size.height
            
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(x, dropY),
                end = Offset(x + 5f, dropY + length), // Slightly slanted
                strokeWidth = 4f
            )
        }
    }
}

@Composable
fun AnimatedFog(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog")
    val translation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "fog_move"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(555)
        for (i in 0..15) {
            val startX = random.nextFloat() * size.width 
            val y = size.height * 0.3f + random.nextFloat() * size.height * 0.7f
            val speed = random.nextFloat() * 0.4f + 0.3f
            val fogX = (startX + translation * speed) % (size.width + 600f) - 300f
            
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = 300f + random.nextFloat() * 200f,
                center = Offset(fogX, y)
            )
        }
    }
}

@Composable
fun AnimatedDrizzle(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "drizzle")
    val rainY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "drizzle_rain"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val random = Random(123)
        // Fewer and thinner drops for drizzle
        for (i in 0..40) {
            val x = random.nextFloat() * size.width
            val yOffset = random.nextFloat() * size.height * 2
            val length = random.nextFloat() * 10f + 5f
            val dropY = (rainY + yOffset) % size.height
            
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(x, dropY),
                end = Offset(x, dropY + length),
                strokeWidth = 2f
            )
        }
    }
}



