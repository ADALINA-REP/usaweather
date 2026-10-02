import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.usweather.radarforecast"
    compileSdk = 36
    ndkVersion = flutter.ndkVersion

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.usweather.radarforecast"
        minSdk = 24
        targetSdk = 36
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        multiDexEnabled = true
    }

    signingConfigs {
        create("release") {
            val keyPropsFile = rootProject.file("key.properties")
            if (keyPropsFile.exists()) {
                val keyProps = Properties()
                keyProps.load(FileInputStream(keyPropsFile))
                val rawPath = keyProps.getProperty("storeFile")?.trim() ?: ""
                val f = file(rawPath)
                storeFile = if (f.isAbsolute || f.exists()) f else rootProject.file(rawPath)
                var sp = keyProps.getProperty("storePassword") ?: ""
                var kp = keyProps.getProperty("keyPassword") ?: ""
                if (storeFile?.absolutePath?.contains("KeyStore") == true && !sp.endsWith(" ")) {
                    sp += " "
                }
                if (storeFile?.absolutePath?.contains("KeyStore") == true && !kp.endsWith(" ")) {
                    kp += " "
                }
                storePassword = sp
                keyAlias = keyProps.getProperty("keyAlias")?.trim()
                keyPassword = kp
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

flutter {
    source = "../.."
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
}

