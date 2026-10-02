package com.ukweather.live.ui.screens.radar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ukweather.live.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.maps.android.compose.*
import com.ukweather.live.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL
import java.net.MalformedURLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RadarScreen(viewModel: com.ukweather.live.ui.screens.home.WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Default location: Entire United Kingdom overview map & radar center
    val ukCenter = LatLng(54.6, -2.8)
    val successState = uiState as? com.ukweather.live.ui.screens.home.WeatherUiState.Success
    val defaultUkZoom = 5.2f
    
    // Instead of a complex injection, let's implement the RadarManager logic inside a state holder
    var radarFrames by remember { mutableStateOf<List<com.ukweather.live.data.api.RadarItem>>(emptyList()) }
    var radarHost by remember { mutableStateOf("https://tilecache.rainviewer.com") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    var isRadarEnabled by remember { mutableStateOf(true) }
    var opacity by remember { mutableFloatStateOf(0.7f) }
    var isLoading by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ukCenter, defaultUkZoom)
    }

    // Animation Loop (300-800ms)
    LaunchedEffect(isAnimating, radarFrames) {
        if (isAnimating && radarFrames.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(500)
                currentIndex = (currentIndex + 1) % radarFrames.size
            }
        }
    }

    // Initial Data Fetch & Auto Refresh (every 5 mins)
    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            try {
                val data = viewModel.getRadarData()
                radarHost = data?.host ?: "https://tilecache.rainviewer.com"
                
                val pastFrames = data?.radar?.past?.toList() ?: emptyList<com.ukweather.live.data.api.RadarItem>()
                val futureFrames = data?.radar?.nowcast?.toList() ?: emptyList<com.ukweather.live.data.api.RadarItem>()
                
                // Combine: Last 8 past frames + 4 future frames (Total 12)
                radarFrames = pastFrames.takeLast(8) + futureFrames.take(4)
                
                if (radarFrames.isNotEmpty() && !isAnimating) {
                    // Set current index to the last 'past' frame (which corresponds to 'now')
                    currentIndex = pastFrames.takeLast(8).size - 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
            kotlinx.coroutines.delay(300_000)
        }
    }

    // Permission Check
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = isLocationPermissionGranted,
                minZoomPreference = 2.0f,
                maxZoomPreference = 12f
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = false
            )
        ) {
            if (isRadarEnabled && radarFrames.isNotEmpty()) {
                val frame = radarFrames[currentIndex]
                val tileProvider = remember(frame.path, radarHost) {
                    RainViewerTileProvider(radarHost, frame.path)
                }
                TileOverlay(
                    tileProvider = tileProvider,
                    transparency = (1f - opacity).coerceIn(0f, 1f)
                )
            }
        }

        // Header Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.8f),
                tonalElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(if (isRadarEnabled) com.ukweather.live.ui.theme.StatusSuccess else Color.Gray, CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.radar_title), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        if (radarFrames.isNotEmpty() && isRadarEnabled) {
                            val frame = radarFrames[currentIndex]
                            val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(frame.time * 1000))
                            
                            // Check if this frame is in the past or nowcast
                            // This is a bit heuristic but since we merged pastframes.takeLast(8) + futureFrames.take(4)
                            // Index 0-7 are past, 8-11 are future.
                            val isForecast = currentIndex >= 8
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(if (isForecast) com.ukweather.live.ui.theme.WeatherRainLight else com.ukweather.live.ui.theme.StatusSuccess, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isForecast) "${stringResource(R.string.radar_forecast)}: $timeText" 
                                    else "${stringResource(R.string.radar_live)}: $timeText", 
                                    color = Color.White.copy(alpha = 0.8f), 
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right-side Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .zIndex(10f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ZOOM IN
            FloatingActionButton(
                onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                containerColor = Color.White.copy(alpha = 0.9f),
                contentColor = com.ukweather.live.ui.theme.TextPrimaryLight,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Zoom In")
            }

            // ZOOM OUT
            FloatingActionButton(
                onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } },
                containerColor = Color.White.copy(alpha = 0.9f),
                contentColor = com.ukweather.live.ui.theme.TextPrimaryLight,
                shape = CircleShape
            ) {
                // Remove (Minus) icon manually to avoid extended library dependency
                Text("-", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-2).dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            FloatingActionButton(
                onClick = {
                    val target = if (successState != null) {
                        LatLng(successState.latitude, successState.longitude)
                    } else {
                        ukCenter
                    }
                    val targetZoom = if (successState != null) 8f else 6f
                    
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, targetZoom))
                    }
                },
                containerColor = Color.White,
                contentColor = com.ukweather.live.ui.theme.BrandPrimaryLight,
                shape = CircleShape
            ) {
                Icon(Icons.Default.LocationOn, "Center")
            }

            FloatingActionButton(
                onClick = { isRadarEnabled = !isRadarEnabled },
                containerColor = if (isRadarEnabled) com.ukweather.live.ui.theme.StatusSuccess else Color.White,
                contentColor = if (isRadarEnabled) Color.White else com.ukweather.live.ui.theme.TextPrimaryLight,
                shape = CircleShape
            ) {
                Icon(if (isRadarEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle Radar")
            }

            FloatingActionButton(
                onClick = { if (isRadarEnabled) isAnimating = !isAnimating },
                containerColor = if (isAnimating) com.ukweather.live.ui.theme.BrandPrimaryLight else Color.White,
                contentColor = if (isAnimating) Color.White else com.ukweather.live.ui.theme.TextPrimaryLight,
                shape = CircleShape
            ) {
                Icon(if (isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow, "Animate")
            }
        }

        // Bottom Controls (Opacity)
        if (isRadarEnabled) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Opacity", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.1f..0.9f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = com.ukweather.live.ui.theme.BrandPrimaryLight,
                            activeTrackColor = com.ukweather.live.ui.theme.BrandPrimaryLight
                        )
                    )
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = com.ukweather.live.ui.theme.BrandPrimaryLight)
        }
    }
}

@androidx.annotation.Keep
class RainViewerTileProvider(
    private val host: String,
    private val path: String
) : UrlTileProvider(256, 256) {
    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
        val baseUrl = if (host.startsWith("http")) host else "https://$host"
        // Ensure no double slashes between host and path
        val normalizedHost = baseUrl.trimEnd('/')
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        
        // Final Radar Tile URL format: {host}{path}/256/{z}/{x}/{y}/{color}/{options}.png
        val finalUrl = "$normalizedHost$normalizedPath/256/$zoom/$x/$y/4/1_1.png"
        
        return try {
            URL(finalUrl)
        } catch (e: Exception) {
             null
        }
    }
}




