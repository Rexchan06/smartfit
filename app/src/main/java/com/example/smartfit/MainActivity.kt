package com.example.smartfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartfit.ui.navigation.SmartFitNavGraph
import com.example.smartfit.ui.theme.SmartFitTheme
import com.example.smartfit.util.Logger

/**
 * MAIN ACTIVITY - Entry point of SmartFit app
 *
 * This is where the app starts. It sets up:
 * 1. Jetpack Compose UI
 * 2. Material Design 3 theme
 * 3. Dependency injection (gets AppContainer)
 * 4. Navigation (when you add it)
 *
 * COMPONENTACTIVITY VS APPCOMPATACTIVITY:
 * - ComponentActivity: Base for Compose apps
 * - AppCompatActivity: Base for XML views
 * - setContent { } is Compose equivalent of setContentView()
 *
 * HOW IT WORKS:
 * 1. Android creates MainActivity
 * 2. onCreate() called
 * 3. setContent { } sets up Compose UI tree
 * 4. SmartFitTheme wraps everything with Material Design 3
 * 5. UI renders on screen
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.i("MainActivity", "App started")

        /**
         * EDGE-TO-EDGE DISPLAY
         *
         * Makes app draw behind status bar and navigation bar
         * Creates modern, immersive experience
         */
        enableEdgeToEdge()

        /**
         * GET APP CONTAINER
         *
         * Access the DI container from Application class
         * All dependencies (repositories, database, etc.) come from here
         */
        val appContainer = (application as SmartFitApplication).appContainer

        /**
         * SET CONTENT - Jetpack Compose UI
         *
         * setContent { } is the Compose equivalent of setContentView()
         * Everything inside this block is Composable UI
         */
        setContent {
            /**
             * SMARTFITTHEME
             *
             * Wraps entire app to apply Material Design 3 theme
             * - Auto-detects light/dark mode from system (can be overridden by user preference)
             * - Provides colors, typography, shapes to all components
             * - Makes components themeable
             */
            SmartFitTheme {
                /**
                 * NAVIGATION SETUP
                 *
                 * rememberNavController() creates navigation controller
                 * SmartFitNavGraph sets up all screens and routes
                 */
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartFitNavGraph(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("MainActivity", "App destroyed")
    }
}

/**
 * SMARTFIT APP ARCHITECTURE - Fully Implemented!
 *
 * The complete app structure is now in place:
 *
 * MainActivity (Entry Point)
 *     ↓
 * SmartFitTheme (Material Design 3)
 *     ↓
 * SmartFitNavGraph (Navigation)
 *     ↓
 * 4 Screens (Home, ActivityLog, AddActivity, Profile)
 *     ↓
 * ViewModels (State Management)
 *     ↓
 * Repositories (Data Access)
 *     ↓
 * Room Database & Retrofit API (Data Sources)
 *
 * FEATURES IMPLEMENTED:
 * ✓ Full navigation between 4 screens
 * ✓ Room database for local data
 * ✓ Retrofit API integration
 * ✓ DataStore for user preferences
 * ✓ Material Design 3 with dark mode
 * ✓ Reactive UI with Flow/StateFlow
 * ✓ Form validation
 * ✓ Reusable UI components
 * ✓ Adaptive layouts (phone/tablet)
 * ✓ FAB animation
 * ✓ Accessibility (content descriptions)
 *
 * TESTING NEXT:
 * - Unit tests for business logic
 * - UI tests for user flows
 */
