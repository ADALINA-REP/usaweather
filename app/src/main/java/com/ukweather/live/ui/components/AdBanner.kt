package com.ukweather.live.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ukweather.live.util.AdConfig

import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import android.util.Log

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adView: AdView? = null
) {
    val context = LocalContext.current
    val currentAdView = remember {
        adView ?: AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdConfig.BANNER_ID
            loadAd(AdRequest.Builder().build())
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { currentAdView },
        update = {
            // Already initialized
        }
    )
}



