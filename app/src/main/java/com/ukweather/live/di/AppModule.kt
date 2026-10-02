package com.ukweather.live.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ukweather.live.data.api.GeocodingApi
import com.ukweather.live.data.api.OpenMeteoAirQualityApi
import com.ukweather.live.data.api.OpenMeteoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @javax.inject.Named("openWeatherApiKey")
    fun provideOpenWeatherApiKey(): String {
        return com.ukweather.live.BuildConfig.OPENWEATHER_API_KEY
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenMeteoApi(moshi: Moshi): OpenMeteoApi {
        return Retrofit.Builder()
            .baseUrl("https://open-meteo.com/") // General base
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenMeteoAirQualityApi(moshi: Moshi): OpenMeteoAirQualityApi {
        return Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoAirQualityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingApi(moshi: Moshi): GeocodingApi {
        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeocodingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRainViewerApi(moshi: Moshi): com.ukweather.live.data.api.RainViewerApi {
        return Retrofit.Builder()
            .baseUrl("https://api.rainviewer.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.ukweather.live.data.api.RainViewerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenWeatherApi(moshi: Moshi): com.ukweather.live.data.api.OpenWeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.ukweather.live.data.api.OpenWeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSunriseSunsetApi(moshi: Moshi): com.ukweather.live.data.api.SunriseSunsetApi {
        return Retrofit.Builder()
            .baseUrl("https://api.sunrisesunset.io/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.ukweather.live.data.api.SunriseSunsetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}



