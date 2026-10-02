package com.ukweather.live.ui.screens.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ukweather.live.ui.components.AnimatedClouds
import com.ukweather.live.ui.components.GlassCard
import com.ukweather.live.ui.theme.*
import com.ukweather.live.ui.screens.home.WeatherViewModel
import com.ukweather.live.R
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@Composable
fun CitiesScreen(
    onCityClick: () -> Unit = {},
    viewModel: CitiesViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            weatherViewModel.requestLocationAndLoadWeather()
            onCityClick()
        }
    }

    // Using a vibrant cloudy dynamic background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyCastRoyalBlueStart, SkyCastRoyalBlueEnd)))
    ) {
        // Add subtle moving clouds over the list
        AnimatedClouds(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(stringResource(R.string.cities_title), style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_cities), color = Color.White.copy(alpha=0.7f)) },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            tint = Color.White, 
                            contentDescription = "Search"
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Default.Close, 
                                    tint = Color.White.copy(alpha = 0.8f), 
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        
                        if (fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED) {
                            weatherViewModel.followCurrentLocation()
                            onCityClick()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryLight,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.use_location), fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Loading State
            if (uiState is CitiesUiState.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            // Success State (Search Results)
            if (uiState is CitiesUiState.Success) {
                val cities = (uiState as CitiesUiState.Success).cities
                items(cities) { city ->
                    CityCard(
                        name = city.name,
                        admin = if (city.admin1 != null) "${city.admin1}, ${city.country ?: ""}" else city.country,
                        onClick = { 
                            weatherViewModel.selectCity(city.name, city.latitude, city.longitude)
                            onCityClick()
                        }
                    )
                }
            }

            // Error State
            if (uiState is CitiesUiState.Error) {
                item {
                    Text((uiState as CitiesUiState.Error).message, color = Color.White)
                }
            }

            // Idle State (Famous Cities)
            if (uiState is CitiesUiState.Idle) {
                item {
                    Text(
                        stringResource(R.string.famous_cities),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                items(viewModel.famousCities) { city ->
                    CityCard(
                        name = city.name,
                        admin = if (city.admin1 != null) "${city.admin1}, ${city.country ?: ""}" else city.country,
                        onClick = {
                            weatherViewModel.selectCity(city.name, city.latitude, city.longitude)
                            onCityClick()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun CityCard(name: String, temp: String? = null, admin: String? = null, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                if (admin != null) {
                    Text(admin, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (temp != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☁️", fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))
                    Text(temp, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Text(stringResource(R.string.select_city_arrow), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}



