# State which classes should be kept
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class com.ukweather.live.data.** { *; }
-keepclassmembers class com.ukweather.live.data.** { *; }

# Preserve all members of our data package and subpackages
-keep class com.ukweather.live.data.model.** { *; }
-keepclassmembers class com.ukweather.live.data.model.** { *; }
-keep class com.ukweather.live.data.api.** { *; }
-keep interface com.ukweather.live.data.api.** { *; }
-keepclassmembers class com.ukweather.live.data.api.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keep @interface retrofit2.http.*
-keep @interface retrofit2.http.** { *; }

# Moshi & Kotlin-Reflect
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep @com.squareup.moshi.Json class * { *; }
-keep class * implements com.squareup.moshi.JsonAdapter { *; }
-keep class *JsonAdapter { *; }
-keep @androidx.annotation.Keep class **
-keepclassmembers class ** {
    @androidx.annotation.Keep *;
}

# Preserve members with @Json
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    private final *;
}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep @javax.inject.Inject class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# Google Play Services
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep interface com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# Proactive protection for GMS callbacks
-keepclassmembers class * extends com.google.android.gms.common.api.internal.LifecycleCallback {
    public <init>(com.google.android.gms.common.api.internal.LifecycleFragment);
}
-keepclassmembers class * extends com.google.android.gms.location.LocationCallback {
    public void onLocationResult(com.google.android.gms.location.LocationResult);
    public void onLocationAvailability(com.google.android.gms.location.LocationAvailability);
}

-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class * implements com.google.android.gms.maps.model.TileProvider { *; }
-keep class * extends com.google.android.gms.maps.model.UrlTileProvider { *; }
-keepclassmembers class * extends com.google.android.gms.maps.model.UrlTileProvider {
    public <init>(...);
    public java.net.URL getTileUrl(int, int, int);
}
# Keep internal classes that the TileOverlay might need via reflection
-keep class com.google.android.gms.internal.maps.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$HandlerPost {
    public <init>(android.os.Handler, java.lang.String);
}
-dontwarn kotlinx.coroutines.**
