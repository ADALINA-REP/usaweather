package com.ukweather.live.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.ukweather.live.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.border
import androidx.compose.ui.draw.*
import coil.compose.AsyncImage
import com.ukweather.live.data.model.WeatherDomain
import com.ukweather.live.data.model.DailyWeatherDomain
import com.ukweather.live.data.model.HourlyWeatherDomain
import com.ukweather.live.data.api.OpenMeteoAirQualityResponse
import com.ukweather.live.ui.components.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import com.ukweather.live.ui.theme.*
import com.ukweather.live.ui.components.RainIcon
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdRequest
import com.ukweather.live.util.AdConfig
import com.ukweather.live.data.model.WeatherWidget
import com.ukweather.live.util.WeatherState
import com.ukweather.live.util.WeatherUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    @Suppress("UNUSED_PARAMETER") onAirQualityClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onAdvancedDetailsClick: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel(),
    settingsViewModel: com.ukweather.live.ui.screens.settings.SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isCelsius by settingsViewModel.isCelsius.collectAsState()
    val isMb by settingsViewModel.isMb.collectAsState()
    val visibleWidgets by viewModel.visibleWidgets.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val isPremium by settingsViewModel.isPremium.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        

        if (granted) {
            viewModel.followCurrentLocation()
        } else {
            if (uiState is WeatherUiState.Loading) {
                viewModel.requestLocationAndLoadWeather()
            }
        }
    }

    val showPermissionRationale by viewModel.showPermissionRationale.collectAsState()
    val showGpsRationale by viewModel.showGpsRationale.collectAsState()

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRationale() },
            title = { Text(stringResource(R.string.location_access_title)) },
            text = { Text(stringResource(R.string.location_access_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissRationale()
                    permissionLauncher.launch(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    )
                }) { Text(stringResource(R.string.authorize)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRationale() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showGpsRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRationale() },
            title = { Text(stringResource(R.string.gps_disabled_title)) },
            text = { Text(stringResource(R.string.gps_disabled_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissRationale()
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text(stringResource(R.string.activate)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRationale() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (viewModel.shouldFollowGps()) {
                    viewModel.requestLocationAndLoadWeather()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                   ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasLocationPermission) {
             viewModel.requestLocationAndLoadWeather()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyCastRoyalBlueStart, SkyCastRoyalBlueEnd))), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is WeatherUiState.Error -> {
                Column(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyCastRoyalBlueStart, SkyCastRoyalBlueEnd))), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.White)
                    Button(onClick = { viewModel.requestLocationAndLoadWeather() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
            is WeatherUiState.Success -> {
                val weather = state.weather
                var selectedDayIndexState by rememberSaveable { mutableStateOf<Int?>(null) }
                
                LaunchedEffect(isRefreshing) {
                    if (isRefreshing) pullToRefreshState.startRefresh()
                    else pullToRefreshState.endRefresh()
                }

                if (pullToRefreshState.isRefreshing) {
                    LaunchedEffect(Unit) {
                        viewModel.refreshWeather()
                    }
                }
    
                LaunchedEffect(state.cityName) {
                    selectedDayIndexState = null
                }
                
                val currentHourIdx = weather.hourly.indexOfFirst { 
                    it.time.startsWith(weather.current.time.substring(0, 13)) 
                }.coerceAtLeast(0)
                val currentRainChance = weather.hourly.getOrNull(currentHourIdx)?.precipitationProbability ?: 0
                
                val currentIconData = WeatherUtils.getWeatherIcon(
                    weather.current.weatherCode, 
                    isDay = weather.current.isDay,
                    humidity = weather.current.humidity ?: 50
                )
                val activeState = currentIconData.animation
                
                val infoWithPotentialOverride = WeatherUtils.getWeatherInfo(weather.current.weatherCode, isDay = weather.current.isDay)
                val bgResId = WeatherUtils.getBackgroundResourceId(context, infoWithPotentialOverride.backgroundResName)

                Box(modifier = Modifier.fillMaxSize()) {
                    if (bgResId != 0) {
                        Crossfade(targetState = bgResId, animationSpec = tween(1500), label = "bg_fade") { targetResId ->
                            Image(
                                painter = painterResource(id = targetResId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        val gradientColors = WeatherUtils.getGradientForState(activeState)
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
                    }

                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
                    when (activeState) {
                        WeatherState.RAIN -> AnimatedRain(modifier = Modifier.fillMaxSize())
                        WeatherState.CLOUDY -> AnimatedClouds(modifier = Modifier.fillMaxSize())
                        WeatherState.SUNNY -> AnimatedSun(modifier = Modifier.padding(top = 100.dp, end = 40.dp).size(150.dp).align(Alignment.TopEnd))
                        WeatherState.SNOW -> AnimatedSnow(modifier = Modifier.fillMaxSize())
                        WeatherState.NIGHT -> AnimatedNight(modifier = Modifier.fillMaxSize())
                        WeatherState.STORM -> AnimatedThunderstorm(modifier = Modifier.fillMaxSize())
                        WeatherState.FOG -> AnimatedFog(modifier = Modifier.fillMaxSize())
                        WeatherState.DRIZZLE -> AnimatedDrizzle(modifier = Modifier.fillMaxSize())
                    }

                    AnimatedVisibility(
                        visible = selectedDayIndexState != null,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ) {
                        if (selectedDayIndexState != null) {
                            BackHandler { selectedDayIndexState = null }
                            DayDetailsScreen(
                                cityName = state.cityName,
                                dayIndex = selectedDayIndexState!!, 
                                isCelsius = isCelsius, daily = weather.daily, 
                                hourly = weather.hourly,
                                airQuality = state.airQuality,
                                currentIsDay = weather.current.isDay,
                                currentTime = weather.current.time,
                                currentTemp = weather.current.temperature,
                                currentWeatherCode = weather.current.weatherCode,
                                currentPrecipitation = weather.current.precipitation,
                                sunrise = weather.daily.getOrNull(0)?.sunrise,
                                sunset = weather.daily.getOrNull(0)?.sunset,
                                currentWindSpeed = weather.current.windSpeed,
                                currentHumidity = weather.current.humidity,
                                isPremium = isPremium,
                                onBack = { selectedDayIndexState = null },
                                isMb = isMb,
                                viewModel = viewModel,
                                utcOffsetSeconds = weather.utcOffsetSeconds
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).nestedScroll(pullToRefreshState.nestedScrollConnection)) {
                            if (selectedDayIndexState == null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .padding(horizontal = 24.dp, vertical = 20.dp)
                                        .widthIn(max = 600.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                     val currentHourIndex = weather.hourly.indexOfFirst { 
                                         it.time.startsWith(weather.current.time.substring(0, 13)) 
                                      }.coerceAtLeast(0)
                                      
                                      val next24HoursCount = 24
                                      val currentRainChance = weather.hourly.getOrNull(currentHourIndex)?.precipitationProbability ?: 0
                                      val currentVisibility = weather.current.visibility ?: 10000.0
                                     
                                      Column(
                                          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                          horizontalAlignment = Alignment.Start
                                      ) {
                                          Row(verticalAlignment = Alignment.CenterVertically) {
                                              Image(
                                                  painter = painterResource(id = R.drawable.skycast_logo),
                                                  contentDescription = "Weather UK Logo",
                                                  modifier = Modifier.size(28.dp),
                                                  contentScale = ContentScale.Fit
                                              )
                                              Text(
                                                  buildAnnotatedString {
                                                      withStyle(SpanStyle(color = BrandPrimaryDark)) { append("Weather") }
                                                      withStyle(SpanStyle(color = Color.White)) { append(" UK") }
                                                  },
                                                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                              )
                                              Spacer(modifier = Modifier.weight(1f))
                                              Row {
                                                  IconButton(onClick = { viewModel.followCurrentLocation() }, modifier = Modifier.size(36.dp)) {
                                                      Icon(
                                                          imageVector = Icons.Filled.LocationOn, 
                                                          contentDescription = "Follow Location", 
                                                          tint = Color.White.copy(alpha = 0.9f), 
                                                          modifier = Modifier.size(22.dp)
                                                      )
                                                  }
                                                  IconButton(onClick = { 
                                                      viewModel.toggleFavorite(state.cityName, state.latitude, state.longitude) 
                                                  }, modifier = Modifier.size(36.dp)) {
                                                      Icon(
                                                          imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                          contentDescription = null,
                                                          tint = if (state.isFavorite) Color.Red else Color.White,
                                                          modifier = Modifier.size(24.dp)
                                                      )
                                                  }
                                              }
                                          }
                                          
                                          Spacer(modifier = Modifier.height(16.dp))
                                          
                                          Text(
                                              state.cityName, 
                                              style = MaterialTheme.typography.headlineLarge.copy(
                                                  fontWeight = FontWeight.Medium,
                                                  letterSpacing = (-0.5).sp,
                                                  fontSize = 34.sp
                                              ), 
                                              color = Color.White
                                          )
                                          
                                          val cityTime = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(weather.utcOffsetSeconds.toLong())
                                          val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d | h:mm a", java.util.Locale.getDefault())
                                          Text(
                                              cityTime.format(formatter),
                                              style = MaterialTheme.typography.bodyLarge,
                                              color = Color.White.copy(alpha = 0.75f)
                                          )
                                          
                                          Spacer(modifier = Modifier.height(8.dp))
                                          
                                          Row(
                                              modifier = Modifier.fillMaxWidth(),
                                              horizontalArrangement = Arrangement.SpaceBetween,
                                              verticalAlignment = Alignment.CenterVertically
                                          ) {
                                              Column(horizontalAlignment = Alignment.Start) {
                                                  val rawTempValue = if (isCelsius) weather.current.temperature.toInt() else (weather.current.temperature * 9 / 5 + 32).toInt()
                                                  Row(verticalAlignment = Alignment.Top) {
                                                      Text(
                                                          rawTempValue.toString(),
                                                          style = MaterialTheme.typography.displayLarge.copy(
                                                              fontSize = 90.sp,
                                                              fontWeight = FontWeight.Normal,
                                                          ),
                                                          color = Color.White
                                                      )
                                                      Text("°${if (isCelsius) "C" else "F"}", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(top = 16.dp))
                                                  }
                                                  
                                                  Spacer(modifier = Modifier.height(4.dp))
                                                  
                                                  Text(
                                                      stringResource(currentIconData.descriptionResId), 
                                                      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), 
                                                      color = Color.White
                                                  )
                                                  
                                                  Spacer(modifier = Modifier.height(8.dp))
                                                  
                                                  val maxT = WeatherUtils.formatTemperature(weather.daily.getOrNull(0)?.maxTemp ?: 0.0, isCelsius)
                                                  val minT = WeatherUtils.formatTemperature(weather.daily.getOrNull(0)?.minTemp ?: 0.0, isCelsius)
                                                  Text(
                                                      "H $maxT   L $minT",
                                                      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
                                                      color = Color.White.copy(alpha = 0.9f)
                                                  )
                                                  
                                                  Spacer(modifier = Modifier.height(2.dp))
                                                  
                                                  Text(
                                                      "Feels ${WeatherUtils.formatTemperature(weather.current.feelsLike ?: weather.current.temperature, isCelsius)}",
                                                      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                                      color = Color.White.copy(alpha = 0.9f)
                                                  )
                                              }

                                              Box(modifier = Modifier.size(170.dp).offset(x = 10.dp), contentAlignment = Alignment.Center) {
                                                  if (activeState == WeatherState.FOG) {
                                                      FogIcon(iconSize = 140.dp)
                                                  } else {
                                                      WeatherIcon(state = activeState, size = 160.dp)
                                                  }
                                              }
                                          }
                                      }


                                     Spacer(modifier = Modifier.height(16.dp))

                                      if (visibleWidgets.contains(WeatherWidget.HOURLY)) {
                                          WidgetContainer(widget = WeatherWidget.HOURLY, onRemove = { viewModel.removeWidget(it) }) {
                                              GlassCard(modifier = Modifier.fillMaxWidth()) {
                                                  HourlyTrendChart(
                                                      hourly = weather.hourly, 
                                                      daily = weather.daily.getOrNull(0),
                                                      isCelsius = isCelsius,
                                                      startIndex = currentHourIndex, 
                                                      maxHours = 24
                                                  )
                                              }
                                          }
                                          Spacer(modifier = Modifier.height(16.dp))
                                      }

                                      if (visibleWidgets.contains(WeatherWidget.FORECAST_7DAY)) {
                                          WidgetContainer(widget = WeatherWidget.FORECAST_7DAY, onRemove = { viewModel.removeWidget(it) }) {
                                              GlassCard(modifier = Modifier.fillMaxWidth()) {
                                                  Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                      Row(
                                                          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                                                          horizontalArrangement = Arrangement.SpaceBetween,
                                                          verticalAlignment = Alignment.CenterVertically
                                                      ) {
                                                          Text(
                                                              stringResource(R.string.forecast_7day).uppercase(),
                                                              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                                              color = Color.White.copy(alpha = 0.7f)
                                                          )
                                                          Icon(
                                                              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                              contentDescription = null,
                                                              tint = Color.White.copy(alpha = 0.5f),
                                                              modifier = Modifier.size(18.dp)
                                                          )
                                                      }

                                                      Row(
                                                          modifier = Modifier
                                                              .fillMaxWidth()
                                                              .horizontalScroll(rememberScrollState())
                                                              .padding(horizontal = 16.dp),
                                                          horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                      ) {
                                                          for (index in 0 until 7) {
                                                              DailyForecastHorizontalItem(
                                                                  index = index, 
                                                                  isCelsius = isCelsius, 
                                                                  daily = weather.daily, 
                                                                  onClick = { selectedDayIndexState = index }
                                                              )
                                                          }
                                                      }
                                                  }
                                              }
                                          }
                                          Spacer(modifier = Modifier.height(16.dp))
                                      }
                                      

                                      val metricsWidgets = listOf(
                                          WeatherWidget.AQI,
                                          WeatherWidget.HUMIDITY,
                                          WeatherWidget.PRESSURE,
                                          WeatherWidget.UV_INDEX,
                                          WeatherWidget.WIND,
                                          WeatherWidget.VISIBILITY,
                                          WeatherWidget.POLLEN,
                                          WeatherWidget.CLOTHING
                                      ).filter { visibleWidgets.contains(it) }

                                      if (metricsWidgets.isNotEmpty()) {
                                          SectionTitle(stringResource(R.string.health_environment))
                                          
                                          metricsWidgets.chunked(2).forEach { pair ->
                                              Row(
                                                  modifier = Modifier.fillMaxWidth(), 
                                                  horizontalArrangement = Arrangement.spacedBy(16.dp)
                                              ) {
                                                  pair.forEach { widget ->
                                                      Box(modifier = Modifier.weight(1f)) {
                                                          WidgetContainer(widget = widget, onRemove = { viewModel.removeWidget(it) }) {
                                                              when (widget) {
                                                                  WeatherWidget.AQI -> {
                                                                      val aqiVal = state.airQuality?.current?.usAqi ?: state.airQuality?.current?.aqi ?: 49
                                                                      AQICard(aqiValue = aqiVal, modifier = Modifier.height(150.dp))
                                                                  }
                                                                  WeatherWidget.HUMIDITY -> {
                                                                      HumidityGraphCard(
                                                                          humidity = weather.current.humidity ?: 50,
                                                                          hourly = weather.hourly,
                                                                          startIndex = currentHourIndex,
                                                                          modifier = Modifier.height(150.dp)
                                                                      )
                                                                  }
                                                                  WeatherWidget.PRESSURE -> {
                                                                      PressureGaugeCard(weather.current.pressure, isMb, modifier = Modifier.height(150.dp))
                                                                  }
                                                                  WeatherWidget.UV_INDEX -> {
                                                                      UVIndexCard(weather.daily.getOrNull(0)?.uvIndex ?: 0.0, modifier = Modifier.height(150.dp))
                                                                  }
                                                                  WeatherWidget.WIND -> {
                                                                      val windDir = weather.current.windDirection
                                                                      val gusts = weather.current.windGusts ?: 0.0
                                                                      val speed = weather.current.windSpeed
                                                                      GlassCard(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                                                                           Box(modifier = Modifier.padding(10.dp).fillMaxSize()) {
                                                                               Text("${stringResource(R.string.wind).uppercase()} : ${speed.toInt()} ${stringResource(R.string.unit_km_h)}", 
                                                                                   style = MaterialTheme.typography.labelSmall, 
                                                                                   color = Color.White.copy(alpha = 0.5f), 
                                                                                   fontWeight = FontWeight.Bold,
                                                                                   modifier = Modifier.align(Alignment.TopCenter))
                                                                               
                                                                               Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp).align(Alignment.Center)) {
                                                                                   Canvas(modifier = Modifier.fillMaxSize()) {
                                                                                       drawCircle(color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                                                                                   }
                                                                                   Text(stringResource(R.string.north), modifier = Modifier.align(Alignment.TopCenter), fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                                                                                   Icon(
                                                                                       imageVector = Icons.Default.Navigation,
                                                                                       contentDescription = null,
                                                                                       tint = Color.Cyan,
                                                                                       modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = windDir.toFloat())
                                                                                   )
                                                                               }
                                                                               
                                                                               Text("${stringResource(R.string.wind_gusts)}: ${gusts.toInt()} ${stringResource(R.string.unit_km_h)}", 
                                                                                   style = MaterialTheme.typography.labelSmall, 
                                                                                   color = Color.White.copy(alpha = 0.6f),
                                                                                   modifier = Modifier.align(Alignment.BottomCenter))
                                                                           }
                                                                       }
                                                                  }
                                                                  WeatherWidget.VISIBILITY -> {
                                                                      VisibilityCard(visibilityMeters = currentVisibility, modifier = Modifier.height(150.dp))
                                                                  }
                                                                  WeatherWidget.POLLEN -> {
                                                                      PollenCard(
                                                                          airQuality = state.airQuality,
                                                                          modifier = Modifier.height(150.dp)
                                                                      )
                                                                  }
                                                                  WeatherWidget.CLOTHING -> {
                                                                      ClothingSuggestionCard(
                                                                          temp = weather.current.temperature,
                                                                          rainProb = currentRainChance,
                                                                          windSpeed = weather.current.windSpeed,
                                                                          uvIndex = weather.daily.getOrNull(0)?.uvIndex ?: 0.0,
                                                                          isDay = weather.current.isDay == 1,
                                                                          weatherCode = weather.current.weatherCode,
                                                                          modifier = Modifier.height(150.dp)
                                                                      )
                                                                  }
                                                                  else -> { /* No-op */ }
                                                              }
                                                          }
                                                      }
                                                  }
                                                  if (pair.size == 1) {
                                                      Spacer(modifier = Modifier.weight(1f))
                                                  }
                                              }
                                              Spacer(modifier = Modifier.height(16.dp))
                                          }
                                      }

                                      if (visibleWidgets.contains(WeatherWidget.SUNSET)) {
                                          val todayDaily = weather.daily.getOrNull(0)
                                          WidgetContainer(widget = WeatherWidget.SUNSET, onRemove = { viewModel.removeWidget(it) }) {
                                              SunsetCard(
                                                  sunrise = todayDaily?.sunrise,
                                                  sunset = todayDaily?.sunset,
                                                  utcOffsetSeconds = weather.utcOffsetSeconds
                                              )
                                          }
                                          Spacer(modifier = Modifier.height(16.dp))
                                       }


                                      if (visibleWidgets.contains(WeatherWidget.SPORT_ADVICE)) {
                                          SectionTitle(stringResource(R.string.sport_outdoor))
                                          WidgetContainer(widget = WeatherWidget.SPORT_ADVICE, onRemove = { viewModel.removeWidget(it) }) {
                                              SportAdvicePager(
                                                  temp = weather.current.temperature,
                                                  rainProb = currentRainChance,
                                                  wind = weather.current.windSpeed,
                                                  humidity = weather.current.humidity ?: 50,
                                                  weatherCode = weather.current.weatherCode,
                                                  hourly = weather.hourly,
                                                  currentHourIdx = currentHourIndex
                                              )
                                          }
                                          Spacer(modifier = Modifier.height(24.dp))
                                      }


                                      Spacer(modifier = Modifier.height(40.dp))
                                }
                            }

                            PullToRefreshContainer(
                                state = pullToRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter),
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        }
                        
                    }
                }
            }
        }
    }
}

// Helper functions removed and moved to WeatherUtils.kt

fun getAqiDescription(aqi: Int): Int {
    return when {
        aqi <= 50 -> R.string.aqi_good
        aqi <= 100 -> R.string.aqi_moderate
        aqi <= 150 -> R.string.aqi_unhealthy_sensitive
        aqi <= 200 -> R.string.aqi_unhealthy
        aqi <= 300 -> R.string.aqi_very_unhealthy
        else -> R.string.aqi_extremely_poor
    }
}

fun getAqiColor(aqiValue: Int): Color {
    return when {
        aqiValue <= 50 -> StatusSuccess
        aqiValue <= 100 -> WeatherSunnyLight
        aqiValue <= 150 -> StatusWarning
        aqiValue <= 200 -> StatusDangerLight
        aqiValue <= 300 -> WeatherThunderstormLight
        else -> WeatherThunderstormLight
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
}

@Composable
fun WeatherMetric(label: String, value: String, icon: String, dropCount: Int? = null, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
        if (dropCount != null) {
            if (label == stringResource(R.string.humidity)) {
               HumidityIcon(iconSize = 22.dp)
            } else {
               RainIcon(dropCount = dropCount.coerceAtLeast(1), iconSize = 22.dp)
            }
        } else {
            Text(icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            label, 
            color = Color.White.copy(alpha = 0.7f), 
            fontSize = 10.sp, 
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HourlyForecastTimeline(hourly: List<HourlyWeatherDomain>, @Suppress("UNUSED_PARAMETER") isCelsius: Boolean, startIndex: Int = 0, maxHours: Int = 24) {
    val totalHours = maxHours.coerceAtMost(maxOf(0, hourly.size - startIndex))
    val hoursData = (0 until totalHours).map { i ->
        val idx = startIndex + i
        val item = hourly[idx]
        val timeLabel = item.time.substring(11, 16)
        val temp = item.temperature
        val pProb = item.precipitationProbability
        val iconData = WeatherUtils.getWeatherIcon(
            item.weatherCode, 
            isDay = item.isDay,
            humidity = item.humidity ?: 50
        )
        
        object {
            val time = timeLabel
            val temperature = temp
            val iconEmoji = iconData.iconEmoji
            val rainProb = pProb
        }
    }
    
    if (hoursData.isEmpty()) return

    // Dynamic scaling for better fluctuation visibility
    val dataMinTemp = hoursData.minOf { it.temperature.toInt().toDouble() }
    val dataMaxTemp = hoursData.maxOf { it.temperature.toInt().toDouble() }
    val minTemp = dataMinTemp - 1.0
    val maxTemp = dataMaxTemp + 1.0
    val tempRange = (maxTemp - minTemp).coerceAtLeast(3.0)
    
    val scrollState = rememberScrollState()
    val itemWidth = 72.dp
    val chartHeight = 110.dp

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            val totalWidth = itemWidth * hoursData.size
            
            // 1. Draw the background line trend
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(chartHeight + 95.dp)
                    .padding(top = 70.dp, bottom = 10.dp) // Moved up from 85.dp
            ) {
                val stepX = itemWidth.toPx()
                val height = chartHeight.toPx()
                
                val points = hoursData.mapIndexed { index, data ->
                    val x = index * stepX + (stepX / 2)
                    // Use integer temperature for points to ensure stability (horizontal lines for same degree)
                    val stableTemp = data.temperature.toInt().toDouble()
                    val y = height - ((stableTemp - minTemp) / tempRange * height).toFloat()
                    Offset(x, y)
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cp1x = (p0.x + p1.x) / 2f
                            cubicTo(cp1x, p0.y, cp1x, p1.y, p1.x, p1.y)
                        }
                    }
                }

                // Line with glow
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                        startY = 0f,
                        endY = height * 1.5f
                    ),
                    style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round)
                )

                // Highlighting points
                points.forEach { point ->
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = point)
                    drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 10.dp.toPx(), center = point)
                }
            }

            // 2. Place Labels and Icons
            Row(modifier = Modifier.width(totalWidth)) {
                hoursData.forEachIndexed { index, data ->
                    Column(
                        modifier = Modifier.width(itemWidth),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(data.time, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(top = 8.dp))
                        Text(data.iconEmoji, fontSize = 22.sp, modifier = Modifier.padding(top = 4.dp))
                        
                        if (data.rainProb > 0) {
                            Text("${data.rainProb}%", style = MaterialTheme.typography.labelSmall, color = Color.Cyan.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Temperature degree is now ABOVE the curve
                        Text(
                            "${data.temperature.toInt()}°", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Reserve space for the fluctuation curve (which is now under the degree)
                        Spacer(modifier = Modifier.height(chartHeight - 15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DayForecastCard(
    @Suppress("UNUSED_PARAMETER") index: Int,
    daily: List<DailyWeatherDomain>,
    hourly: List<HourlyWeatherDomain>,
    isCelsius: Boolean,
    currentTime: String?,
    onClick: () -> Unit
) {
    val item = daily.getOrNull(index) ?: return
    val dateString = item.time
    val parsedDate = LocalDate.parse(dateString)
    val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("EEE(dd/MM)", java.util.Locale.getDefault())).uppercase()
    
    val maxTemp = item.maxTemp.toInt()
    val minTemp = item.minTemp.toInt()
    
    val currentHourStr = currentTime?.substring(0, 13) ?: ""
    val hourIdx = if (currentHourStr.isNotEmpty()) hourly.indexOfFirst { it.time.startsWith(currentHourStr) } else -1

    val pProb = if (index == 0 && hourIdx != -1) {
        hourly.getOrNull(hourIdx)?.precipitationProbability ?: item.precipitationProbability
    } else {
        item.precipitationProbability
    }
    
    val code = if (index == 0 && hourIdx != -1) {
        hourly.getOrNull(hourIdx)?.weatherCode ?: item.weatherCode
    } else {
        item.weatherCode
    }
    
    val iconData = WeatherUtils.getWeatherIcon(code, isDay = 1, humidity = pProb) // Using pProb as humidity fallback if needed or just 50
    val emoji = iconData.iconEmoji

    GlassCard(modifier = Modifier.width(140.dp).clickable { onClick() }) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formattedDate, color = Color.White.copy(alpha=0.9f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("${WeatherUtils.formatTemperature(maxTemp.toDouble(), isCelsius)} / ${WeatherUtils.formatTemperature(minTemp.toDouble(), isCelsius)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    cityName: String,
    dayIndex: Int,
    daily: List<DailyWeatherDomain>,
    hourly: List<HourlyWeatherDomain>,
    isCelsius: Boolean,
    airQuality: com.ukweather.live.data.api.OpenMeteoAirQualityResponse?,
    currentIsDay: Int,
    currentTime: String?,
    currentTemp: Double,
    currentWeatherCode: Int,
    @Suppress("UNUSED_PARAMETER") currentPrecipitation: Double?,
    @Suppress("UNUSED_PARAMETER") sunrise: String?,
    @Suppress("UNUSED_PARAMETER") sunset: String?,
    @Suppress("UNUSED_PARAMETER") currentWindSpeed: Double? = null,
    @Suppress("UNUSED_PARAMETER") currentHumidity: Int? = null,
    @Suppress("UNUSED_PARAMETER") isPremium: Boolean,
    onBack: () -> Unit,
    isMb: Boolean = true,
    viewModel: WeatherViewModel,
    utcOffsetSeconds: Int = 0
) {
    if (dayIndex >= daily.size) return
    val scrollState = rememberScrollState()
    
    val dayItem = daily.getOrNull(dayIndex) ?: return
    val dateString = dayItem.time
    val parsedDate = LocalDate.parse(dateString)
    val todayStr = stringResource(R.string.today)
    val formattedDate = if (dayIndex == 0) "$todayStr, ${parsedDate.dayOfMonth} ${parsedDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())}" 
                        else parsedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.getDefault()))
    
    val maxTemp = dayItem.maxTemp.toInt()
    val minTemp = dayItem.minTemp.toInt()
    
    val currentHourStr = currentTime?.substring(0, 13) ?: ""
    val hourIdx = if (currentHourStr.isNotEmpty()) hourly.indexOfFirst { it.time.startsWith(currentHourStr) } else -1

    val pProb = if (dayIndex == 0 && hourIdx != -1) {
        hourly.getOrNull(hourIdx)?.precipitationProbability
    } else {
        daily.getOrNull(dayIndex)?.precipitationProbability
    }
    
    val code = if (dayIndex == 0) {
        currentWeatherCode
    } else {
        daily.getOrNull(dayIndex)?.weatherCode ?: 0
    }
    
    val maxTempVal = daily.getOrNull(dayIndex)?.maxTemp?.toInt() ?: 0
    val displayTemp = if (dayIndex == 0) currentTemp.toInt() else maxTempVal
    val dayMaxTemp = daily.getOrNull(dayIndex)?.maxTemp ?: 0.0
    val dayWind = daily.getOrNull(dayIndex)?.windSpeedMax ?: 0.0
    val dayPrecipSum = daily.getOrNull(dayIndex)?.precipitationSum ?: 0.0
    
    val iconData = WeatherUtils.getWeatherIcon(
        code, 
        isDay = if (dayIndex == 0) currentIsDay else 1,
        humidity = if (dayIndex == 0) (currentHumidity ?: 50) else 50
    )
    val dayIconData = iconData
    val activeState = dayIconData.animation
    val context = LocalContext.current
    val infoWithPotentialOverride = WeatherUtils.getWeatherInfo(code, isDay = if (dayIndex == 0) currentIsDay else 1)
    val bgResId = WeatherUtils.getBackgroundResourceId(context, infoWithPotentialOverride.backgroundResName)

    // Logic: Start the hourly reel at 00:00 of the selected day
    val todayStartIndex = (dayIndex * 24).coerceIn(0, (hourly.size - 24))

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgResId != 0) {
            Image(painter = painterResource(id = bgResId), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            val gradientColors = WeatherUtils.getGradientForState(activeState)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
        when (activeState) {
            WeatherState.RAIN -> AnimatedRain(modifier = Modifier.fillMaxSize())
            WeatherState.CLOUDY -> AnimatedClouds(modifier = Modifier.fillMaxSize())
            WeatherState.SUNNY -> AnimatedSun(modifier = Modifier.padding(top = 100.dp, end = 40.dp).size(150.dp).align(Alignment.TopEnd))
            WeatherState.SNOW -> AnimatedSnow(modifier = Modifier.fillMaxSize())
            WeatherState.NIGHT -> AnimatedNight(modifier = Modifier.fillMaxSize())
            WeatherState.STORM -> AnimatedThunderstorm(modifier = Modifier.fillMaxSize())
            WeatherState.FOG -> AnimatedFog(modifier = Modifier.fillMaxSize())
            WeatherState.DRIZZLE -> AnimatedDrizzle(modifier = Modifier.fillMaxSize())
        }

        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val pullToRefreshState = rememberPullToRefreshState()
        
        LaunchedEffect(isRefreshing) {
            if (isRefreshing) pullToRefreshState.startRefresh()
            else pullToRefreshState.endRefresh()
        }

        if (pullToRefreshState.isRefreshing) {
            LaunchedEffect(Unit) {
               viewModel.refreshWeather()
            }
        }

        Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- PREMIUM TOP BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack, 
                        modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.skycast_logo),
                            contentDescription = "Weather UK Logo",
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = BrandPrimaryDark)) { append("Weather") }
                                withStyle(SpanStyle(color = Color.White)) { append(" UK") }
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // CITY & DATE (Left Aligned)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Local Time calculation
                    val cityTime = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(utcOffsetSeconds.toLong())
                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.getDefault())
                    
                    Text(
                        cityName.uppercase(), 
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black), 
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (dayIndex == 0) "$formattedDate | ${cityTime.format(timeFormatter)}" else formattedDate, 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // --- SPLIT-COLUMN HERO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Temp + Condition Stack
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Giant Temperature
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "${displayTemp}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-2).sp
                                ), 
                                color = Color.White
                            )
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text("°", style = MaterialTheme.typography.displaySmall, color = Color.White)
                                Text(if (isCelsius) "C" else "F", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // Condition Text & Min/Max Stack
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                stringResource(dayIconData.descriptionResId),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "H:${maxTemp}°  L:${minTemp}°",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Right Column: Giant UHD Weather Icon
                    Box(modifier = Modifier.size(170.dp).padding(end = 16.dp), contentAlignment = Alignment.Center) {
                        WeatherIcon(state = activeState, size = 160.dp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // HOURLY FORECAST

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    HourlyTrendChart(
                        hourly = hourly, 
                        daily = daily.getOrNull(dayIndex),
                        isCelsius = isCelsius, 
                        startIndex = todayStartIndex, 
                        maxHours = 24
                    )
                }

                // HEALTH & ENVIRONMENT
                val dayHumidity = (0 until 24).mapNotNull { h -> 
                    hourly.getOrNull(dayIndex * 24 + h)?.humidity 
                }.let { if(it.isNotEmpty()) it.average().toInt() else 50 }
                
                SectionTitle(stringResource(R.string.health_environment))
                
                val rawUs = airQuality?.current?.usAqi ?: -1
                val rawEu = airQuality?.current?.aqi ?: -1
                val aqiVal = if (rawUs != -1) rawUs else if (rawEu != -1) rawEu else 49
                AQICard(aqiValue = aqiVal, modifier = Modifier.fillMaxWidth().height(120.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        HumidityGraphCard(
                            humidity = dayHumidity,
                            hourly = hourly,
                            startIndex = dayIndex * 24,
                            modifier = Modifier.height(150.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) { 
                        val hIndex = if (dayIndex == 0 && hourIdx != -1) hourIdx else dayIndex * 24 + 12
                        PressureGaugeCard(hourly.getOrNull(hIndex)?.pressure ?: 1013.2, isMb, modifier = Modifier.height(150.dp)) 
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        UVIndexCard(daily.getOrNull(dayIndex)?.uvIndex ?: 0.0, modifier = Modifier.height(150.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                          // Wind Compass dashboard card (Synced with Home)
                          val hourlyIndex = if (dayIndex == 0 && hourIdx != -1) hourIdx else dayIndex * 24 + 12
                          val item = hourly.getOrNull(hourlyIndex)
                          val windDir = item?.windDirection ?: 0
                          val gusts = item?.windGusts ?: 0.0
                          val speed = item?.windSpeed ?: 0.0
                          
                           GlassCard(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                               Box(modifier = Modifier.padding(10.dp).fillMaxSize()) {
                                   Text("${stringResource(R.string.wind).uppercase()} : ${speed.toInt()} ${stringResource(R.string.unit_km_h)}", 
                                       style = MaterialTheme.typography.labelSmall, 
                                       color = Color.White.copy(alpha = 0.5f), 
                                       fontWeight = FontWeight.Bold,
                                       modifier = Modifier.align(Alignment.TopCenter))
                                   
                                   // Advanced Compass UI with Cardinal Points - ABSOLUTELY CENTERED
                                   Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp).align(Alignment.Center)) {
                                       Canvas(modifier = Modifier.fillMaxSize()) {
                                           drawCircle(color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                                       }
                                       
                                       // Cardinal Labels
                                       Text(stringResource(R.string.north), modifier = Modifier.align(Alignment.TopCenter), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black)
                                       Text(stringResource(R.string.south), modifier = Modifier.align(Alignment.BottomCenter), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black)
                                       Text(stringResource(R.string.east), modifier = Modifier.align(Alignment.CenterEnd), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black)
                                       Text(stringResource(R.string.west), modifier = Modifier.align(Alignment.CenterStart), fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Black)

                                       // Rotating Needle
                                       Icon(
                                           imageVector = Icons.Default.Navigation,
                                           contentDescription = null,
                                           tint = Color.Cyan,
                                           modifier = Modifier.size(32.dp).graphicsLayer(rotationZ = windDir.toFloat())
                                       )
                                       
                                       // Center Dot
                                       Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
                                   }
                                   
                                   Text("${stringResource(R.string.wind_gusts)}: ${gusts.toInt()} ${stringResource(R.string.unit_km_h)}", 
                                       style = MaterialTheme.typography.labelSmall, 
                                       color = Color.White.copy(alpha = 0.6f),
                                       modifier = Modifier.align(Alignment.BottomCenter))
                               }
                           }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // SPORT & OUTDOOR
                SectionTitle(stringResource(R.string.sport_outdoor))
                SportAdvicePager(
                    temp = displayTemp.toDouble(),
                    rainProb = (pProb ?: 0).toInt(),
                    wind = dayWind,
                    humidity = dayHumidity,
                    weatherCode = code,
                    hourly = hourly,
                    currentHourIdx = todayStartIndex
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                // OTHER METRICS
                val hourlyIndex = if (dayIndex == 0 && hourIdx != -1) hourIdx else dayIndex * 24 + 12
                val currentVisibilityForDay = hourly.getOrNull(hourlyIndex)?.visibility ?: 10000.0
                VisibilityCard(visibilityMeters = currentVisibilityForDay, modifier = Modifier.fillMaxWidth().height(150.dp))

                Spacer(modifier = Modifier.height(16.dp))
                SunsetCard(daily.getOrNull(dayIndex)?.sunrise, daily.getOrNull(dayIndex)?.sunset)
                
                
                Spacer(modifier = Modifier.height(40.dp))
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun InsightRow(label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun VisualInsightRow(icon: String, label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.width(90.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MiniScore(score: Int, message: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
            CircularProgressIndicator(
                progress = { score / 10f },
                color = if (score >= 8) Color.Green else if (score >= 5) Color.Yellow else Color.Red,
                strokeWidth = 5.dp,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxSize()
            )
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "$score/10", 
            style = MaterialTheme.typography.titleMedium, 
            color = Color.White, 
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            message, 
            style = MaterialTheme.typography.bodySmall, 
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun UVIndexCard(uvIndex: Double, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WbSunny, contentDescription = null, tint = WeatherSunnyLight, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.uv_index).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                }
                Text(
                    String.format("%.1f", uvIndex),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                when {
                    uvIndex < 3 -> stringResource(R.string.low)
                    uvIndex < 6 -> stringResource(R.string.moderate)
                    uvIndex < 8 -> stringResource(R.string.high)
                    else -> stringResource(R.string.very_high)
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                // Gradient Background Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(StatusSuccess, WeatherSunnyLight, StatusWarning, StatusDangerLight)
                            ),
                            shape = CircleShape
                        )
                )
                
                val progress = (uvIndex / 12.0).coerceIn(0.0, 1.0).toFloat()
                
                // Needle Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .align(Alignment.CenterStart)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                         Text(
                             uvIndex.toInt().toString(),
                             style = MaterialTheme.typography.labelSmall,
                             color = Color.White,
                             fontWeight = FontWeight.ExtraBold,
                             modifier = Modifier.offset(y = (-4).dp)
                         )
                         // Vertical Needle Line
                         Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(Color.White, RoundedCornerShape(2.dp))
                                .shadow(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HumidityGraphCard(humidity: Int, hourly: List<HourlyWeatherDomain>, startIndex: Int, modifier: Modifier = Modifier) {
    val next24Hours = (0 until 24).map { i ->
        val idx = startIndex + i
        if (idx < hourly.size) hourly[idx].humidity?.toDouble() ?: 50.0 else 50.0
    }
    
    MetricGraphCard(
        title = stringResource(R.string.humidity),
        value = "$humidity%",
        icon = { HumidityIcon(iconSize = 14.dp) },
        dataPoints = next24Hours,
        graphColor = WeatherRainLight,
        modifier = modifier
    )
}

@Composable
fun HumidityIcon(modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 24.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "humidity_pulse")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        
        // 1. The Main Droplet Shape
        val dripPath = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.5f, h * 0.05f, w * 0.95f, h * 0.55f, w * 0.95f, h * 0.72f)
            cubicTo(w * 0.95f, h * 0.92f, w * 0.05f, h * 0.92f, w * 0.05f, h * 0.72f)
            cubicTo(w * 0.05f, h * 0.55f, w * 0.5f, h * 0.05f, w * 0.5f, h * 0.05f)
            close()
        }
        
        // Base Gradient (Deep Water)
        drawPath(
            path = dripPath,
            brush = Brush.verticalGradient(listOf(WeatherRainLight, BrandPrimaryLight))
        )
        
        // 2. Internal Animated Wave (The 'Fluid' feel)
        clipPath(dripPath) {
            val waveWidth = w * 2
            val waveX = -w + (waveOffset * waveWidth)
            val wavePath = Path().apply {
                moveTo(waveX, h * 0.65f)
                quadraticBezierTo(waveX + w * 0.25f, h * 0.60f, waveX + w * 0.5f, h * 0.65f)
                quadraticBezierTo(waveX + w * 0.75f, h * 0.70f, waveX + w, h * 0.65f)
                quadraticBezierTo(waveX + w * 1.25f, h * 0.60f, waveX + w * 1.5f, h * 0.65f)
                quadraticBezierTo(waveX + w * 1.75f, h * 0.70f, waveX + w * 2, h * 0.65f)
                lineTo(waveX + w * 2, h)
                lineTo(waveX, h)
                close()
            }
            drawPath(path = wavePath, color = Color.White.copy(alpha = 0.15f))
        }

        // 3. Ultra-Sophisticated Highlights (Glass Effect)
        // Top-Left Sparkle
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = w * 0.08f,
            center = Offset(w * 0.35f, h * 0.45f)
        )
        
        // Side Reflection
        val highlightPath = Path().apply {
            moveTo(w * 0.25f, h * 0.6f)
            cubicTo(w * 0.25f, h * 0.6f, w * 0.3f, h * 0.75f, w * 0.25f, h * 0.82f)
        }
        drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = 0.4f),
            style = Stroke(width = (w * 0.05f), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FogIcon(modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 24.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog_pulse")
    val mistOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mist_float"
    )

    Canvas(modifier = modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        
        // 1. Ambient Glow Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                center = center,
                radius = w * 0.6f
            )
        )
        
        // 2. Sophisticated Mist Layers
        val strokeWidth = (iconSize.toPx() * 0.07f).coerceAtLeast(1f)
        
        // Layer 1: Top Mist (Light)
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(w * 0.25f + mistOffset, h * 0.45f),
            end = Offset(w * 0.75f + mistOffset, h * 0.45f),
            strokeWidth = strokeWidth * 0.8f,
            cap = StrokeCap.Round
        )
        
        // Layer 2: Main Mist (Heavy)
        drawLine(
            color = Color.White.copy(alpha = 0.95f),
            start = Offset(w * 0.15f - mistOffset, h * 0.65f),
            end = Offset(w * 0.85f - mistOffset, h * 0.65f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Layer 3: Bottom Mist (Medium)
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.2f + mistOffset * 0.5f, h * 0.85f),
            end = Offset(w * 0.8f + mistOffset * 0.5f, h * 0.85f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
        
        // 3. Subtle depth dots (Mist condensation)
        drawCircle(color = Color.White.copy(alpha = 0.4f), radius = strokeWidth * 0.3f, center = Offset(w * 0.4f, h * 0.55f))
        drawCircle(color = Color.White.copy(alpha = 0.4f), radius = strokeWidth * 0.3f, center = Offset(w * 0.65f, h * 0.75f))
    }
}

@Composable
fun MetricGraphCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    dataPoints: List<Double>,
    graphColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Icon and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Big Center Graph
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp)) {
                    if (dataPoints.isNotEmpty()) {
                        val min = dataPoints.minOrNull() ?: 0.0
                        val max = dataPoints.maxOrNull() ?: 100.0
                        val range = (max - min).coerceAtLeast(20.0)
                        val stepX = size.width / (dataPoints.size - 1)
                        
                        val path = Path().apply {
                            dataPoints.forEachIndexed { i, d ->
                                val x = i * stepX
                                val y = size.height - ((d - min) / range * size.height).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }
                        
                        // Fill Under Graph for UHD look
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(graphColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        
                        drawPath(
                            path = path,
                            color = graphColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Text Under Graph
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    value, 
                    style = MaterialTheme.typography.titleLarge, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold
                )
                
                
                val trend = if (dataPoints.size >= 6) {
                    val current = dataPoints.first()
                    val next6 = dataPoints.getOrElse(6) { current }
                    if (next6 > current + 5) stringResource(R.string.pressure_rising) 
                    else if (next6 < current - 5) stringResource(R.string.pressure_falling) 
                    else stringResource(R.string.pressure_steady)
                } else stringResource(R.string.pressure_steady)
                
                Text(
                    ": $trend", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MoonCard(moonrise: String?, moonset: String?, phase: Double = 0.5, utcOffsetSeconds: Int = 0) {
    // Hidden for production stability
}

@Composable
fun SunsetCard(sunrise: String?, sunset: String?, utcOffsetSeconds: Int = 0) {
    val currentTime = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(utcOffsetSeconds.toLong()).toLocalTime()
    val sunriseTime = try { java.time.LocalTime.parse(sunrise?.substringAfterLast("T")?.substring(0, 5)) } catch (e: Exception) { java.time.LocalTime.of(6, 30) }
    val sunsetTime = try { java.time.LocalTime.parse(sunset?.substringAfterLast("T")?.substring(0, 5)) } catch (e: Exception) { java.time.LocalTime.of(19, 30) }
    val amPmFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.getDefault())

    val totalSeconds = sunsetTime.toSecondOfDay() - sunriseTime.toSecondOfDay()
    val currentSeconds = currentTime.toSecondOfDay() - sunriseTime.toSecondOfDay()
    val progress = if (totalSeconds > 0) (currentSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f
    
    val isNight = currentTime.isAfter(sunsetTime) || currentTime.isBefore(sunriseTime)

    GlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Square Icon like in screenshot
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.verticalGradient(listOf(WeatherSunnyLight, WeatherRainLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\u2600\uFE0F", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.solar_cycle).uppercase(), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.White.copy(alpha = 0.6f), 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 1.sp
                )
            }
            
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val infiniteTransition = rememberInfiniteTransition(label = "solar_atmosphere")
                val sunPulse by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "pulse"
                )

                Canvas(modifier = Modifier.fillMaxSize().padding(top = 20.dp, bottom = 20.dp)) {
                    val w = size.width
                    val h = size.height
                    val baselineY = h - 10.dp.toPx()
                    
                    // 1. ATMOSPHERIC SKY GRADIENT (Inside the curve)
                    val skyPath = Path().apply {
                        moveTo(0f, baselineY)
                        cubicTo(w * 0.25f, -h * 0.3f, w * 0.75f, -h * 0.3f, w, baselineY)
                        close()
                    }
                    val skyGradient = Brush.verticalGradient(
                        colors = listOf(
                            WeatherSunnyLight.copy(alpha = 0.2f), 
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = baselineY
                    )
                    drawPath(path = skyPath, brush = skyGradient)

                    // 2. THE PATHS
                    val mainPath = Path().apply {
                        moveTo(0f, baselineY)
                        cubicTo(w * 0.25f, -h * 0.3f, w * 0.75f, -h * 0.3f, w, baselineY)
                    }

                    val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
                    pathMeasure.setPath(mainPath, false)
                    val length = pathMeasure.length
                    
                    // Inactive (Full) Path - Dashed Semi-transparent White
                    drawPath(
                        path = mainPath,
                        color = Color.White.copy(alpha = 0.2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx(), 
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // Active (Past) Path - Glowing Solid WeatherSunny
                    val activePath = Path()
                    pathMeasure.getSegment(0f, length * progress, activePath, true)
                    drawPath(
                        path = activePath,
                        color = WeatherSunnyLight,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. HORIZON LINE
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, baselineY),
                        end = Offset(w, baselineY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 4. THE SUN (UHD Lens Flare Look)
                    if (!isNight) {
                        val sunPos = pathMeasure.getPosition(length * progress)
                        
                        // Large Soft Outer Glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(WeatherSunnyLight.copy(alpha = 0.3f * sunPulse), Color.Transparent),
                                center = sunPos,
                                radius = 45.dp.toPx()
                            ),
                            center = sunPos,
                            radius = 45.dp.toPx()
                        )

                        // Secondary Ring Flare
                        drawCircle(
                            color = WeatherSunnyLight.copy(alpha = 0.3f),
                            radius = 16.dp.toPx() * sunPulse,
                            center = sunPos,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )

                        // Core Sun
                        drawCircle(
                            color = WeatherSunnyLight,
                            radius = 8.dp.toPx(),
                            center = sunPos
                        )
                    }

                    // 5. Sunrise/Sunset Dots
                    drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 3.dp.toPx(), center = Offset(0f, baselineY))
                    drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 3.dp.toPx(), center = Offset(w, baselineY))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(stringResource(R.string.sunrise).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    Text(sunriseTime.format(amPmFormatter), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
                }
                
                // Night/Day status indicator (Pill style from screenshot)
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(
                            if(isNight) SecondaryCardDark.copy(alpha=0.8f) else BrandPrimaryLight.copy(alpha=0.8f), 
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        if(isNight) "\uD83C\uDF19 NIGHT" else "\u2600\uFE0F DAY", 
                        color = Color.White, 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.sunset).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    Text(sunsetTime.format(amPmFormatter), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}



@Composable
fun MetricSmallCard(title: String, value: String, subtitle: String, icon: String) {
    GlassCard(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun VisibilityCard(visibilityMeters: Double, modifier: Modifier = Modifier) {
    val km = visibilityMeters / 1000.0
    val level = WeatherUtils.getVisibilityLevel(km)
    val color = WeatherUtils.getVisibilityColor(km)
    val message = WeatherUtils.getVisibilityMessage(km)
    val icon = WeatherUtils.getVisibilityIcon(km)
    
    GlassCard(modifier = modifier.fillMaxWidth().height(150.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\uD83D\uDC41\uFE0F", fontSize = 14.sp)
                Text(stringResource(R.string.visibility).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    String.format("%.1f %s", km, stringResource(R.string.km_unit)), 
                    style = MaterialTheme.typography.titleLarge, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(level), 
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Minimal progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                val progress = (km / 15.0).coerceIn(0.0, 1.0).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(color, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(message), 
                style = MaterialTheme.typography.bodySmall, 
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DailyForecastHorizontalItem(index: Int, daily: List<DailyWeatherDomain>, isCelsius: Boolean, onClick: () -> Unit) {
    val dayData = daily.getOrNull(index) ?: return
    val formatDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
    val formatDate = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault())
    
    val dateParsed = try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dayData.time) } catch(e:Exception){java.util.Date()}
    val dayName = formatDay.format(dateParsed ?: java.util.Date())
    val dateNum = formatDate.format(dateParsed ?: java.util.Date())
    
    val pProb = dayData.precipitationProbability
    val iconData = WeatherUtils.getWeatherIcon(
        dayData.weatherCode, 
        isDay = 1,
        humidity = pProb // or daily model humidity if available
    )
    
    Box(
        modifier = Modifier
            .width(86.dp)
            .height(130.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.SpaceBetween, 
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dayName, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(dateNum, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.7f))
            }
            
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                 WeatherIcon(state = iconData.animation, size = 28.dp)
            }
            
            Text(
                stringResource(iconData.descriptionResId), 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.White.copy(alpha = 0.9f), 
                fontSize = 11.sp, 
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, 
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            val maxT = WeatherUtils.formatTemperature(dayData.maxTemp, isCelsius)
            val minT = WeatherUtils.formatTemperature(dayData.minTemp, isCelsius)
            Text("$maxT/$minT", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun HourlyTrendChart(
    hourly: List<HourlyWeatherDomain>, 
    daily: DailyWeatherDomain?,
    isCelsius: Boolean, 
    startIndex: Int, 
    maxHours: Int
) {
    val totalHours = maxHours.coerceAtMost(maxOf(0, hourly.size - startIndex))
    val hoursData = (0 until totalHours).map { i ->
        val idx = startIndex + i
        val hourData = hourly[idx]
        val hourStr = try { hourData.time.substringAfter("T").substring(0, 2).toInt() } catch(e: Exception) { 0 }
        val amPm = if (hourStr >= 12) "PM" else "AM"
        val displayTime = if (hourStr > 12) hourStr - 12 else if (hourStr == 0) 12 else hourStr
        
        val temp = hourData.temperature
        val pProb = hourData.precipitationProbability
        val iconData = WeatherUtils.getWeatherIcon(
            hourData.weatherCode, 
            isDay = hourData.isDay,
            humidity = pProb
        )
        object {
            val time = "$displayTime $amPm"
            val temp = temp
            val icon = iconData.iconEmoji
            val rainProb = pProb
        }
    }
    
    if (hoursData.isEmpty()) return

    val maxTempVal = hoursData.maxOf { it.temp }
    val minTempVal = hoursData.minOf { it.temp }
    val rangeVal = (maxTempVal - minTempVal).coerceAtLeast(15.0)
    
    val itemWidth = 65.dp
    val chartHeight = 40.dp

    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        // SUMMARY LINE
        if (daily != null) {
            val info = WeatherUtils.getWeatherInfo(daily.weatherCode, isDay = 1)
            val high = daily.maxTemp.toInt()
            val low = daily.minTemp.toInt()
            Text(
                stringResource(
                    R.string.highs_lows_summary, 
                    stringResource(info.descriptionResId),
                    high, high + 2, 
                    low - 1, low + 1
                ),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, letterSpacing = (-0.2).sp),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            val totalWidth = itemWidth * hoursData.size
            
            // 1. Place Labels and Framed Backgrounds
            Row(modifier = Modifier.width(totalWidth)) {
                hoursData.forEachIndexed { index, data ->
                    Box(
                        modifier = Modifier.width(itemWidth).padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (index == 0) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(data.time, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            WeatherIcon(
                                state = WeatherUtils.mapWeatherCode(hourly[startIndex + index].weatherCode, isDay = hourly[startIndex + index].isDay), 
                                size = 20.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("${data.temp.toInt()}°", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            
                            // Reserve space for Graph line
                            Spacer(modifier = Modifier.height(chartHeight + 10.dp))
                            
                            // Rain Probability
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("\uD83D\uDCA6", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${data.rainProb}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // 2. Draw the Graph Line using Canvas (Drawn ON TOP of the frames)
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(chartHeight + 50.dp) 
                    .padding(top = 88.dp) // Moved up by 10dp to be extremely close to the temp text
            ) {
                val stepX = itemWidth.toPx()
                val height = chartHeight.toPx()
                
                val points = hoursData.mapIndexed { index, data ->
                    val x = index * stepX + (stepX / 2)
                    val stableTemp = data.temp.toInt().toDouble()
                    val stableMin = minTempVal.toInt().toDouble()
                    val stableRange = rangeVal.toInt().toDouble().coerceAtLeast(1.0)
                    val y = height - ((stableTemp - stableMin) / stableRange * height).toFloat()
                    Offset(x, y)
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cp1x = (p0.x + p1.x) / 2f
                            cubicTo(cp1x, p0.y, cp1x, p1.y, p1.x, p1.y)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = WeatherSunnyLight,
                    style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                )

                points.forEach { point ->
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = point)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SportAdvicePager(
    temp: Double,
    rainProb: Int,
    wind: Double,
    humidity: Int,
    weatherCode: Int,
    hourly: List<HourlyWeatherDomain>,
    currentHourIdx: Int
) {
    val sports = listOf(
        WeatherUtils.SportType.RUNNING,
        WeatherUtils.SportType.CYCLING,
        WeatherUtils.SportType.HIKING
    )
    val pagerState = rememberPagerState(pageCount = { sports.size })
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            SportAdviceCard(
                temp = temp,
                rainProb = rainProb,
                wind = wind,
                humidity = humidity,
                weatherCode = weatherCode,
                hourly = hourly,
                currentHourIdx = currentHourIdx,
                sportType = sports[page]
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Dots indicator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(sports.size) { i ->
                val color = if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.3f)
                val width = if (pagerState.currentPage == i) 16.dp else 6.dp
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(6.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

@Composable
fun SportAdviceCard(
    temp: Double,
    rainProb: Int,
    wind: Double,
    humidity: Int,
    weatherCode: Int,
    hourly: List<HourlyWeatherDomain>,
    currentHourIdx: Int,
    sportType: WeatherUtils.SportType
) {
    val advice = WeatherUtils.getWeatherAdvice(temp, rainProb, wind, humidity, weatherCode, sportType)
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row: Sport Name & Smart Advice
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(advice.sportNameResId), style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                
                // Smart Advice Badge (Hydration, clothing, etc.)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(advice.smartAdviceResId), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Middle Main Row: Icon & Dynamic Hourly Indicators side-by-side
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Large Rounded Sport Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(advice.sportIcon, fontSize = 28.sp)
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                // Dynamic Hourly Indicators (Next 24 hours)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.weight(1f), 
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(24) { index ->
                        val i = index + 1
                        val idx = currentHourIdx + i
                        if (idx < hourly.size) {
                            val hourData = hourly[idx]
                            val fullTime = hourData.time
                            val hourNum = fullTime.substringAfter("T").substring(0, 2).toInt()
                            val displayTime = if (hourNum > 12) hourNum - 12 else if (hourNum == 0) 12 else hourNum
                            val pmAm = if (hourNum >= 12) stringResource(R.string.pm) else stringResource(R.string.am)
                            
                            val hTemp = hourData.temperature
                            val hRain = hourData.precipitationProbability
                            val hAdvice = WeatherUtils.getWeatherAdvice(hTemp, hRain, 10.0, 50, hourData.weatherCode, sportType)
                            
                            val emoji = when (hAdvice.sportStatusResId) {
                                R.string.status_excellent -> "\uD83E\uDD29"
                                R.string.status_good -> "\uD83D\uDE0A"
                                R.string.status_decent -> "\uD83D\uDE10"
                                else -> "\uD83D\uDE15"
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                                Text("$displayTime $pmAm", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    stringResource(hAdvice.sportStatusResId).uppercase(), 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.White.copy(alpha = 0.8f), 
                                    fontSize = 8.sp, 
                                    maxLines = 1,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Status Text Section: First word is bigger and all bold as requested
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)) {
                        append(stringResource(advice.sportStatusResId))
                    }
                    append(": ")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))) {
                        append(stringResource(advice.sportAdviceResId))
                    }
                },
                color = Color.White,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CircularAQIGauge(aqiValue: Int) {
    val aqiColor = getAqiColor(aqiValue)
    val progress = (aqiValue / 500f).coerceIn(0.01f, 1f)
    
    Box(
        contentAlignment = Alignment.Center, 
        modifier = Modifier.size(85.dp)
    ) {
        // High contrast track
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = 8.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
            )
            drawArc(
                color = aqiColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        Text(
            aqiValue.toString(), 
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp), 
            color = Color.White, 
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AQICard(aqiValue: Int, modifier: Modifier = Modifier) {
    val description = getAqiDescription(aqiValue)
    val aqiColor = getAqiColor(aqiValue)
    val progress = aqiValue / 500f // Common US AQI Max for visualization

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AQI(${stringResource(description)})", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${stringResource(description)} ($aqiValue)", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(18.dp))
            
            // Premium Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(aqiColor.copy(alpha = 0.8f), aqiColor)),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun AQIInfoDialog(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    stringResource(R.string.aqi_levels_title), 
                    style = MaterialTheme.typography.headlineSmall, 
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                AQIReferenceRow("0 - 50", stringResource(R.string.aqi_good), StatusSuccess)
                AQIReferenceRow("51 - 100", stringResource(R.string.aqi_moderate), WeatherSunnyLight)
                AQIReferenceRow("101 - 150", stringResource(R.string.aqi_unhealthy_sensitive), StatusWarning)
                AQIReferenceRow("151 - 200", stringResource(R.string.aqi_unhealthy), StatusDangerLight)
                AQIReferenceRow("201 - 300", stringResource(R.string.aqi_very_unhealthy), WeatherThunderstormLight)
                AQIReferenceRow("301 - 500", stringResource(R.string.aqi_hazardous), StatusDangerLight)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                ) {
                    Text(stringResource(R.string.got_it), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AQIReferenceRow(range: String, description: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, CircleShape)
                .shadow(elevation = 4.dp, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(description, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(range, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun PollenCard(airQuality: OpenMeteoAirQualityResponse?, modifier: Modifier = Modifier) {
    val current = airQuality?.current
    val treePollen = (current?.alder ?: 0.0) + (current?.birch ?: 0.0) + (current?.olive ?: 0.0)
    val grassPollen = current?.grass ?: 0.0
    val weedPollen = (current?.mugwort ?: 0.0) + (current?.ragweed ?: 0.0)
    
    val totalPollen = treePollen + grassPollen + weedPollen
    
    // Thresholds based on common grains/m3 levels
    val levelRes = when {
        totalPollen > 100 -> R.string.pollen_high
        totalPollen > 20 -> R.string.pollen_moderate
        else -> R.string.pollen_low
    }
    val color = when {
        totalPollen > 100 -> StatusDangerLight
        totalPollen > 20 -> WeatherSunnyLight
        else -> StatusSuccess
    }
    
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌿", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.pollen_allergy).uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(levelRes), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.tree_grass_weed), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                val progress = (totalPollen / 250.0).coerceIn(0.1, 1.0).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(color.copy(alpha=0.5f), color)), CircleShape)
                )
            }
        }
    }
}

@Composable
fun ClothingSuggestionCard(
    temp: Double, 
    rainProb: Int, 
    windSpeed: Double, 
    uvIndex: Double,
    isDay: Boolean,
    weatherCode: Int,
    modifier: Modifier = Modifier
) {
    val wState = WeatherUtils.mapWeatherCode(weatherCode, 1)
    val isRainy = rainProb > 35 || wState == WeatherState.RAIN || wState == WeatherState.DRIZZLE || wState == WeatherState.STORM
    val isSnowy = wState == WeatherState.SNOW
    val isVeryCold = temp < 5.0
    val isCold = temp < 15.0
    val isHot = temp > 26.0
    val isWindy = windSpeed > 28.0
    val isSunny = uvIndex > 5.5 && isDay

    val suggestionRes = when {
        isSnowy -> R.string.suggestion_cold
        isRainy && isCold -> R.string.suggestion_rain_cold
        isRainy -> R.string.suggestion_rain
        isVeryCold -> R.string.suggestion_very_cold
        isWindy -> R.string.suggestion_windy
        isSunny -> R.string.suggestion_sunny
        isCold -> R.string.suggestion_cold
        isHot && !isRainy -> R.string.suggestion_hot
        else -> R.string.suggestion_moderate
    }

    val icon = when {
        isSnowy -> "❄️"
        isRainy && isCold -> "🧥"
        isRainy -> "☂️"
        isVeryCold -> "🧤"
        isWindy -> "🌬️"
        isSunny -> "🕶️"
        isCold -> "🧣"
        isHot && !isRainy -> "👕"
        else -> "👕"
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👕", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.clothing).uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(suggestionRes), style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.based_on_current_weather), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun PressureGaugeCard(pressure: Double, isMb: Boolean = true, modifier: Modifier = Modifier) {
    val progress = ((pressure - 960) / (1060 - 960)).coerceIn(0.01, 1.0).toFloat()
    val statusRes = when {
        pressure > 1022 -> R.string.high
        pressure < 1005 -> R.string.low
        else -> R.string.steady
    }

    GlassCard(modifier = modifier.fillMaxWidth().height(150.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u23F2\uFE0F", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.pressure).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
            
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(85.dp).padding(top = 10.dp)) {
                    val strokeW = 4.dp.toPx()
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    val radius = (size.width / 2) - (strokeW / 2)
                    
                    // Background Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    
                    // Progress Arc with Gradient
                    drawArc(
                        brush = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White)),
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    
                    // Indicator Dot at Arc End
                    val angleRad = Math.toRadians((135f + 270f * progress).toDouble())
                    val dotX = (center.x + radius * Math.cos(angleRad)).toFloat()
                    val dotY = (center.y + radius * Math.sin(angleRad)).toFloat()
                    drawCircle(color = Color.White, radius = 5f, center = androidx.compose.ui.geometry.Offset(dotX, dotY))
                    
                    // Subtle Tick Marks
                    val tickAngles = listOf(135f, 202.5f, 270f, 337.5f, 405f)
                    tickAngles.forEach { angle ->
                        val rad = Math.toRadians(angle.toDouble())
                        val startX = (center.x + (radius - 8) * Math.cos(rad)).toFloat()
                        val startY = (center.y + (radius - 8) * Math.sin(rad)).toFloat()
                        val endX = (center.x + (radius - 1) * Math.cos(rad)).toFloat()
                        val endY = (center.y + (radius - 1) * Math.sin(rad)).toFloat()
                        drawLine(color = Color.White.copy(alpha = 0.2f), start = androidx.compose.ui.geometry.Offset(startX, startY), end = androidx.compose.ui.geometry.Offset(endX, endY), strokeWidth = 1.dp.toPx())
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) {
                    Text(pressure.toInt().toString(), style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = Color.White)
                    Text(if (isMb) "mb" else "hPa", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                }
            }
            Text(stringResource(statusRes), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun WidgetContainer(
    widget: WeatherWidget,
    onRemove: (WeatherWidget) -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        content()
    }
}




