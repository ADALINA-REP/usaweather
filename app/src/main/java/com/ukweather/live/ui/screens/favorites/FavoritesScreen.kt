package com.ukweather.live.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun FavoritesScreen(
    weatherViewModel: WeatherViewModel,
    onCityClick: () -> Unit
) {
    val favorites by weatherViewModel.favorites.collectAsState()
    var itemToDelete by remember { mutableStateOf<com.ukweather.live.data.repository.LocationData?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyCastRoyalBlueStart, SkyCastRoyalBlueEnd)))
    ) {
        // Subtle moving clouds over favorites
        AnimatedClouds(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite, 
                            contentDescription = null, 
                            tint = Color.Red, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.nav_favorites).uppercase(), 
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp), 
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.favorites_title), 
                            style = MaterialTheme.typography.headlineMedium, 
                            color = Color.White, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                
                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Favorite, 
                                contentDescription = null, 
                                tint = Color.White.copy(alpha = 0.1f), 
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.fav_no_items), color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.fav_add_hint), color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                        }
                    }
                }
            }

            items(favorites) { location ->
                FavoriteCityCard(
                    name = location.name,
                    onClick = {
                        weatherViewModel.selectCity(location.name, location.latitude, location.longitude)
                        onCityClick()
                    },
                    onDelete = {
                        itemToDelete = location
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Beautiful and Friendly Confirmation Dialog
        if (itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDangerLight)
                },
                title = {
                    Text(
                        stringResource(R.string.remove_fav),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to remove ${itemToDelete?.name} from your favorites?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            itemToDelete?.let {
                                weatherViewModel.removeFavorite(it.name, it.latitude, it.longitude)
                            }
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusDangerLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Remove", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
fun FavoriteCityCard(name: String, onClick: () -> Unit, onDelete: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
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
                Text(
                    name, 
                    fontSize = 24.sp, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite, 
                        contentDescription = null, 
                        tint = StatusDangerLight.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.fav_saved_location), 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Redesigned Delete Button - More prominent and friendly
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = stringResource(R.string.remove_fav), 
                    tint = StatusDangerLight, 
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}



