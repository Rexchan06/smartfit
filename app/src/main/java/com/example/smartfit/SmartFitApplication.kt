package com.example.smartfit

import android.app.Application
import com.example.smartfit.di.AppContainer

/**
 * CUSTOM APPLICATION CLASS
 *
 * This is the entry point of your app - created before any Activity or Service.
 * It lives for the entire app lifecycle (from launch to termination).
 *
 * WHY CREATE A CUSTOM APPLICATION CLASS?
 * 1. Initialize dependencies that need to live for entire app lifecycle
 * 2. Create the DI container that all components will use
 * 3. Set up global configuration (logging, crash reporting, etc.)
 * 4. Provide a single instance that's accessible throughout the app
 *
 * LIFECYCLE:
 * 1. App starts → onCreate() called → AppContainer created
 * 2. User navigates app → AppContainer provides dependencies
 * 3. App terminated → Application destroyed
 *
 * HOW TO USE IN YOUR CODE:
 * In any Activity or Fragment:
 * ```kotlin
 * val appContainer = (application as SmartFitApplication).appContainer
 * val repository = appContainer.activityRepository
 * ```
 *
 * DON'T FORGET:
 * You must register this in AndroidManifest.xml:
 * <application android:name=".SmartFitApplication" ...>
 */
class SmartFitApplication : Application() {

    /**
     * AppContainer - Holds all app dependencies
     *
     * LATEINIT:
     * We use 'lateinit' because we can't initialize it until onCreate() is called
     * (need Context which is only available then)
     *
     * SINGLETON:
     * Only ONE AppContainer exists for entire app
     * All ViewModels, Repositories, etc. come from here
     */
    lateinit var appContainer: AppContainer
        private set  // Only this class can set it, others can only read

    /**
     * onCreate() - Called when app process is created
     *
     * This runs BEFORE any Activity is created
     * Perfect place to initialize dependencies
     *
     * IMPORTANT:
     * - Keep this fast! Slow initialization delays app startup
     * - Don't do heavy work here (database creation is lazy, so it's ok)
     * - Exceptions here will crash the app
     */
    override fun onCreate() {
        super.onCreate()

        // Create the dependency injection container
        // All dependencies are initialized lazily when first accessed
        appContainer = AppContainer(applicationContext)

        // Log app initialization (useful for debugging)
        android.util.Log.d("SmartFitApp", "Application started - AppContainer initialized")
    }
}

/**
 * HOW THIS FITS INTO THE ARCHITECTURE:
 *
 * SmartFitApplication
 *        ↓ creates
 *   AppContainer
 *        ↓ provides
 *   Repositories
 *        ↓ injected into
 *    ViewModels
 *        ↓ observed by
 *    UI (Compose)
 *
 * EXAMPLE USAGE:
 *
 * In MainActivity.kt:
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // Get the app container
 *         val appContainer = (application as SmartFitApplication).appContainer
 *
 *         setContent {
 *             SmartFitTheme {
 *                 // Pass container to navigation, which passes to ViewModels
 *                 SmartFitNavigation(appContainer = appContainer)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * In a ViewModel Factory:
 * ```kotlin
 * class HomeViewModel(
 *     private val activityRepository: ActivityRepository
 * ) : ViewModel() {
 *     // ViewModel implementation
 * }
 *
 * // Factory to create ViewModel with dependencies
 * class HomeViewModelFactory(
 *     private val appContainer: AppContainer
 * ) : ViewModelProvider.Factory {
 *     override fun <T : ViewModel> create(modelClass: Class<T>): T {
 *         if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
 *             @Suppress("UNCHECKED_CAST")
 *             return HomeViewModel(appContainer.activityRepository) as T
 *         }
 *         throw IllegalArgumentException("Unknown ViewModel class")
 *     }
 * }
 * ```
 *
 * BENEFITS OF THIS APPROACH:
 * ✓ Single source for all dependencies
 * ✓ Easy to test (replace AppContainer with mock version)
 * ✓ Clear dependency graph
 * ✓ No magic/hidden dependencies (unlike Hilt)
 * ✓ Easy to debug (can add breakpoints in AppContainer)
 */
