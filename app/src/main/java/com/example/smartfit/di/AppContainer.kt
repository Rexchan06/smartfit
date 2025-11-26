package com.example.smartfit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartfit.data.datastore.PreferencesManager
import com.example.smartfit.data.local.database.SmartFitDatabase
import com.example.smartfit.data.remote.api.FitnessApiService
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.WorkoutRepository
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * DEPENDENCY INJECTION CONTAINER
 *
 * This is a manual dependency injection container that creates and manages all app dependencies.
 * It follows the principle of "Dependency Inversion" - high-level code depends on abstractions
 * (interfaces) rather than concrete implementations.
 *
 * WHY MANUAL DI INSTEAD OF HILT/DAGGER?
 * ✓ Simpler to understand - you see exactly how objects are created
 * ✓ No annotation processing complexity
 * ✓ Full control over object lifecycle
 * ✓ Perfect for learning DI concepts
 * ✓ Sufficient for small to medium apps
 *
 * HOW IT WORKS:
 * 1. SmartFitApplication creates ONE instance of AppContainer when app starts
 * 2. AppContainer creates singletons (database, network) that live for entire app lifecycle
 * 3. Repositories are created once and reused
 * 4. ViewModels get dependencies from AppContainer via factory functions
 *
 * OBJECT LIFECYCLE:
 * - AppContainer: Lives as long as the Application (entire app lifecycle)
 * - Database, API service: Created once, shared across app
 * - Repositories: Created once, shared across ViewModels
 * - ViewModels: Created per screen, survive configuration changes
 */
class AppContainer(private val context: Context) {

    // ============================================================================
    // NETWORK LAYER - API Communication
    // ============================================================================

    /**
     * JSON serializer configuration for Retrofit
     *
     * ignoreUnknownKeys = true: Won't crash if API returns extra fields we don't need
     * This is important because APIs can add fields without breaking our app
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * HTTP Logging Interceptor - Logs all network requests and responses
     *
     * LOGGING LEVELS:
     * - NONE: No logging (production)
     * - BASIC: Request method, URL, response code, time
     * - HEADERS: Request and response headers
     * - BODY: Full request and response body (what we use for debugging)
     *
     * This will help you debug API calls by seeing exactly what's sent/received
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp Client - Handles HTTP connections
     *
     * Interceptors process every request/response (like middleware)
     * Timeouts prevent hanging connections if network is slow
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Add logging to see network traffic
        .connectTimeout(30, TimeUnit.SECONDS)  // Wait max 30s to establish connection
        .readTimeout(30, TimeUnit.SECONDS)     // Wait max 30s to read response
        .writeTimeout(30, TimeUnit.SECONDS)    // Wait max 30s to send request
        .build()

    /**
     * Retrofit Instance - Converts API interface to working implementation
     *
     * WHAT IS RETROFIT?
     * Retrofit turns your API interface (with @GET, @POST annotations) into actual
     * working code that makes HTTP requests. You just define the interface, Retrofit
     * generates the implementation.
     *
     * BASE URL: wger.de API - Free exercise and nutrition database
     * - No API key required
     * - 800+ exercises with images
     * - Nutrition data
     * - Well-documented
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wger.de/api/v2/")  // All API calls start with this URL
        .client(okHttpClient)  // Use our configured OkHttp client
        .addConverterFactory(  // Convert JSON ↔ Kotlin objects
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    /**
     * Fitness API Service - The actual API interface
     *
     * Retrofit.create() generates the implementation of FitnessApiService
     * You define the interface, Retrofit does the networking!
     */
    val fitnessApiService: FitnessApiService = retrofit.create(FitnessApiService::class.java)

    // ============================================================================
    // DATABASE LAYER - Local Storage
    // ============================================================================

    /**
     * Room Database Instance - Local SQLite database
     *
     * LAZY INITIALIZATION:
     * 'lazy' means the database isn't created until first access
     * This improves app startup time
     *
     * SINGLETON:
     * Only ONE database instance exists for entire app
     * Multiple instances can cause data corruption
     */
    val database: SmartFitDatabase by lazy {
        SmartFitDatabase.getDatabase(context)
    }

    /**
     * DataStore for Preferences - Modern SharedPreferences replacement
     *
     * DATASTORE VS SHAREDPREFERENCES:
     * ✓ Type-safe (compile-time checking)
     * ✓ Async (doesn't block UI thread)
     * ✓ Observable (emits Flow when data changes)
     * ✓ Handles errors better
     *
     * USED FOR:
     * - User settings (dark mode, notifications)
     * - Daily step goal
     * - Last sync time
     * - Any user preferences
     */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "smartfit_preferences"
    )

    val dataStore: DataStore<Preferences> = context.dataStore

    /**
     * Preferences Manager - Type-safe wrapper around DataStore
     *
     * Provides clean API for user preferences:
     * - Dark theme toggle
     * - Daily step goal
     * - Other user settings
     *
     * This makes it easy for ViewModels to read/write preferences
     * without dealing with DataStore complexity
     */
    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(dataStore)
    }

    // ============================================================================
    // REPOSITORY LAYER - Data Access Abstraction
    // ============================================================================

    /**
     * Activity Repository - Manages fitness activity data
     *
     * WHY REPOSITORY PATTERN?
     * 1. Single Source of Truth: UI doesn't know if data is from DB or network
     * 2. Offline First: Can return cached data while fetching updates
     * 3. Business Logic: Combines, filters, or transforms data
     * 4. Testability: Easy to mock in tests
     *
     * The repository decides:
     * - When to fetch from network vs cache
     * - How to handle errors
     * - How to combine multiple data sources
     */
    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(
            activityDao = database.activityDao(),  // Local storage
            // In a real app, you might add API service here for syncing
        )
    }

    /**
     * Workout Repository - Manages workout suggestions from API
     *
     * This repository handles:
     * - Fetching exercises from wger.de API
     * - Caching workout data
     * - Searching exercises by muscle group or category
     */
    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            apiService = fitnessApiService  // Remote data source
        )
    }
}

/**
 * DEPENDENCY FLOW EXAMPLE:
 *
 * 1. App starts → SmartFitApplication created
 * 2. SmartFitApplication creates AppContainer
 * 3. User opens HomeScreen → HomeViewModel created
 * 4. HomeViewModel needs ActivityRepository
 * 5. HomeViewModel gets repository from AppContainer
 * 6. Repository uses DAO to query database
 * 7. Database returns Flow<List<Activity>>
 * 8. Repository returns Flow to ViewModel
 * 9. ViewModel exposes StateFlow to UI
 * 10. UI observes StateFlow and updates automatically
 *
 * This chain ensures:
 * ✓ No tight coupling between layers
 * ✓ Easy to test (mock any layer)
 * ✓ Clear data flow
 * ✓ Single responsibility for each class
 */
