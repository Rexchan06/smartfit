

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)  // For Retrofit JSON serialization
    alias(libs.plugins.kotlin.compose)  // Compose Compiler plugin (required for Kotlin 2.0+)
    alias(libs.plugins.ksp)  // Kotlin Symbol Processing for Room annotation processing
}

android {
    namespace = "com.example.smartfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartfit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable Jetpack Compose
    buildFeatures {
        compose = true
    }

    // Disable packaging of unnecessary files
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core Android dependencies
    // Kotlin extensions for core Android APIs - makes code more concise and idiomatic
    implementation(libs.androidx.core.ktx)

    // Jetpack Compose - Modern declarative UI framework
    // BOM (Bill of Materials) manages compatible versions of all Compose libraries automatically
    implementation(platform(libs.androidx.compose.bom))

    // Core Compose UI libraries
    implementation(libs.androidx.compose.ui)  // Base UI toolkit for building interfaces
    implementation(libs.androidx.compose.ui.graphics)  // Graphics primitives and utilities
    implementation(libs.androidx.compose.ui.tooling.preview)  // Preview support in Android Studio

    // Material Design 3 - Modern Material Design components for Compose
    implementation(libs.androidx.compose.material3)
    // Extended icon set - includes more icons beyond basic Material icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Activity integration with Compose - enables setContent {} in Activities
    implementation(libs.androidx.activity.compose)

    // ViewModel integration with Compose
    // Provides viewModel() function to get/create ViewModels in Composables
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Allows collecting StateFlow/Flow as State in Composables with lifecycle awareness
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation for Compose - handles screen navigation and back stack
    implementation(libs.androidx.navigation.compose)

    // Room Database - Local SQLite database with compile-time verification
    implementation(libs.androidx.room.runtime)  // Room runtime library
    implementation(libs.androidx.room.ktx)  // Kotlin extensions (coroutines, Flow support)
    // KSP processes @Entity, @Dao, @Database annotations at compile time
    // Generates implementation code for your database operations
    ksp(libs.androidx.room.compiler)

    // Retrofit - Type-safe HTTP client for REST API calls
    implementation(libs.retrofit)  // Core Retrofit library
    // Converter that uses Kotlin Serialization for JSON parsing (faster than Gson)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)  // Kotlin Serialization library

    // OkHttp - HTTP client underlying Retrofit
    implementation(libs.okhttp)
    // Logging interceptor - logs all HTTP requests/responses (useful for debugging)
    implementation(libs.okhttp.logging.interceptor)

    // Coil - Image loading library optimized for Compose
    implementation(libs.coil.compose)  // Coil integration for Compose (AsyncImage composable)
    implementation(libs.coil.network.okhttp)  // Use OkHttp for network requests

    // DataStore - Modern, asynchronous preference storage (replaces SharedPreferences)
    // Stores key-value pairs with type safety and Flow support
    implementation(libs.androidx.datastore.preferences)

    // Coroutines - Kotlin's way of handling asynchronous code
    // Used by Room, Retrofit, DataStore, and ViewModels for async operations
    implementation(libs.kotlinx.coroutines.android)  // Android-specific coroutines support
    implementation(libs.kotlinx.coroutines.core)  // Core coroutines library

    // Testing dependencies

    // Unit testing (runs on JVM, fast)
    testImplementation(libs.junit)  // Basic unit testing framework
    testImplementation(libs.mockk)  // Mocking library for Kotlin
    testImplementation(libs.kotlinx.coroutines.test)  // Test utilities for coroutines
    testImplementation(libs.turbine)  // Testing library for Flow (makes testing Flows easy)
    testImplementation(libs.androidx.room.testing)  // Room testing utilities

    // Instrumented testing (runs on Android device/emulator)
    androidTestImplementation(libs.androidx.junit)  // AndroidX JUnit extensions
    androidTestImplementation(libs.androidx.espresso.core)  // UI testing framework
    androidTestImplementation(platform(libs.androidx.compose.bom))  // Compose testing uses BOM
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)  // Compose testing library

    // Debug-only dependencies (not included in release builds)
    debugImplementation(libs.androidx.compose.ui.tooling)  // Compose preview tooling
    debugImplementation(libs.androidx.compose.ui.test.manifest)  // Test manifest for Compose
}