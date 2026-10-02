package com.ukweather.live.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.ukweather.live.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

@Composable
fun SplashScreen(
    viewModel: com.ukweather.live.ui.screens.home.WeatherViewModel,
    onNavigateToHome: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Background particles state
    val particles = remember { List(15) { ParticleData() } }
    
    LaunchedEffect(uiState) {
        // Animation sequence (only once)
        if (alpha.value == 0f) {
            launch {
                alpha.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 1000, easing = EaseInOutQuart)
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutBack)
                )
            }
        }

        // Logic: 2.5 seconds or until weather data is fetched (whichever is faster)
        val isDataReady = uiState is com.ukweather.live.ui.screens.home.WeatherUiState.Success
        
        if (isDataReady) {
            onNavigateToHome()
        }
    }

    // Fallback: 2.5 seconds timeout
    LaunchedEffect(Unit) {
        delay(2500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BrandPrimaryDarkVariant, BrandPrimaryLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Particle Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val progress = (System.currentTimeMillis() % 5000) / 5000f
                val yOffset = (progress * particle.speed * size.height) % size.height
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f * particle.opacity),
                    radius = particle.radius,
                    center = Offset(particle.x * size.width, (particle.y * size.height + yOffset) % size.height)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // App Logo Centered at roughly 30% scale
            Box(
                modifier = Modifier
                    .size(150.dp) // Adjusted size for better impact
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.ukweather.live.R.drawable.skycast_logo),
                    contentDescription = "Weather UK App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Text(
                androidx.compose.ui.res.stringResource(id = com.ukweather.live.R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 10f),
                        blurRadius = 20f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                androidx.compose.ui.res.stringResource(id = com.ukweather.live.R.string.app_slogan),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

data class ParticleData(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val radius: Float = Random.nextFloat() * 15 + 5,
    val speed: Float = Random.nextFloat() * 0.1f + 0.05f,
    val opacity: Float = Random.nextFloat() * 0.5f + 0.5f
)



