package com.example.smartfit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.WorkoutRepository
import com.example.smartfit.data.datastore.PreferencesManager
import com.example.smartfit.di.AppContainer
import com.example.smartfit.ui.screens.home.HomeScreen
import com.example.smartfit.ui.screens.home.HomeViewModel
import com.example.smartfit.ui.screens.home.HomeViewModelFactory
import com.example.smartfit.ui.screens.activitylog.ActivityLogScreen
import com.example.smartfit.ui.screens.activitylog.ActivityLogViewModel
import com.example.smartfit.ui.screens.activitylog.ActivityLogViewModelFactory
import com.example.smartfit.ui.screens.addactivity.AddActivityScreen
import com.example.smartfit.ui.screens.addactivity.AddActivityViewModel
import com.example.smartfit.ui.screens.addactivity.AddActivityViewModelFactory
import com.example.smartfit.ui.screens.profile.ProfileScreen
import com.example.smartfit.ui.screens.profile.ProfileViewModel
import com.example.smartfit.ui.screens.profile.ProfileViewModelFactory

/**
 * NAVIGATION GRAPH - App Navigation Setup
 *
 * This composable sets up the entire navigation structure for the app.
 * It defines all screens, routes, and how they connect to each other.
 *
 * KEY NAVIGATION CONCEPTS:
 *
 * NAVHOST:
 * The container that displays the current screen
 * Only one screen is visible at a time
 *
 * NAVHOSTCONTROLLER:
 * Controls navigation (navigate, pop, etc.)
 * Passed to screens that need to navigate
 *
 * COMPOSABLE():
 * Defines a destination in the navigation graph
 * Maps route string to Composable screen
 *
 * VIEWMODEL CREATION:
 * ViewModels are created using factories that inject dependencies
 * ViewModels survive configuration changes automatically
 *
 * ARCHITECTURE FLOW:
 * NavGraph → Screen → ViewModel (with dependencies) → Repository → Database/API
 *
 * USAGE IN MAINACTIVITY:
 * ```kotlin
 * setContent {
 *     SmartFitTheme {
 *         val navController = rememberNavController()
 *         val appContainer = (application as SmartFitApplication).appContainer
 *
 *         SmartFitNavGraph(
 *             navController = navController,
 *             appContainer = appContainer
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun SmartFitNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    /**
     * NAVHOST SETUP
     *
     * Parameters:
     * - navController: Controls navigation between screens
     * - startDestination: Initial screen when app launches
     * - modifier: Optional styling/layout modifier
     *
     * Each composable{} block defines a screen:
     * 1. Create ViewModel with dependencies from AppContainer
     * 2. Pass ViewModel and NavController to screen
     * 3. Screen uses ViewModel for data, NavController for navigation
     */
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ========================================================================
        // HOME SCREEN
        // ========================================================================

        /**
         * HOME SCREEN - Main Dashboard
         *
         * Shows:
         * - Daily statistics (calories, steps, activity count)
         * - Recent activities
         * - Workout suggestions
         * - FAB to add activity
         *
         * VIEWMODEL FACTORY:
         * Creates HomeViewModel with ActivityRepository and WorkoutRepository
         * viewModel() with factory ensures single instance per navigation entry
         */
        composable(route = Screen.Home.route) {
            // Create ViewModel with dependencies
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    activityRepository = appContainer.activityRepository,
                    workoutRepository = appContainer.workoutRepository
                )
            )

            // Render screen
            HomeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ========================================================================
        // ACTIVITY LOG SCREEN
        // ========================================================================

        /**
         * ACTIVITY LOG SCREEN - Full Activity List
         *
         * Shows:
         * - All recorded activities in scrollable list
         * - Each activity displayed with ActivityCard
         * - Click activity to view details or edit
         * - Swipe to delete
         * - Filter/sort options
         *
         * NAVIGATION FROM HOME:
         * IconButton or bottom nav item navigates here
         */
        composable(route = Screen.ActivityLog.route) {
            val viewModel: ActivityLogViewModel = viewModel(
                factory = ActivityLogViewModelFactory(
                    activityRepository = appContainer.activityRepository
                )
            )

            ActivityLogScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ========================================================================
        // ADD ACTIVITY SCREEN
        // ========================================================================

        /**
         * ADD ACTIVITY SCREEN - Create New Activity
         *
         * Shows:
         * - Form with fields for activity details
         * - Validation before saving
         * - Auto-calculates calories if needed
         * - Navigates back on success
         *
         * NAVIGATION FROM HOME:
         * FAB button on HomeScreen
         *
         * FUTURE: Can be extended to edit existing activities by passing activityId
         */
        composable(route = Screen.AddActivity.route) {
            val viewModel: AddActivityViewModel = viewModel(
                factory = AddActivityViewModelFactory(
                    activityRepository = appContainer.activityRepository
                )
            )

            AddActivityScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ========================================================================
        // PROFILE SCREEN
        // ========================================================================

        /**
         * PROFILE SCREEN - User Settings
         *
         * Shows:
         * - Dark mode toggle (saved to DataStore)
         * - Daily step goal input
         * - User statistics
         * - About section
         *
         * NAVIGATION FROM HOME:
         * Bottom nav item or top bar action
         *
         * DATASTORE INTEGRATION:
         * ProfileViewModel uses PreferencesManager to read/write settings
         */
        composable(route = Screen.Profile.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(
                    preferencesManager = appContainer.preferencesManager,
                    activityRepository = appContainer.activityRepository
                )
            )

            ProfileScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // ========================================================================
        // FUTURE SCREENS (Add as needed)
        // ========================================================================

        /**
         * ACTIVITY DETAIL SCREEN (Example for future expansion)
         *
         * Shows detailed view of single activity with edit/delete options
         *
         * Usage:
         * ```kotlin
         * composable(
         *     route = Screen.ActivityDetail.route,
         *     arguments = listOf(
         *         navArgument(Screen.ActivityDetail.ARG_ACTIVITY_ID) {
         *             type = NavType.IntType
         *         }
         *     )
         * ) { backStackEntry ->
         *     val activityId = backStackEntry.arguments?.getInt(
         *         Screen.ActivityDetail.ARG_ACTIVITY_ID
         *     ) ?: 0
         *
         *     val viewModel: ActivityDetailViewModel = viewModel(
         *         factory = ActivityDetailViewModelFactory(
         *             activityId = activityId,
         *             activityRepository = appContainer.activityRepository
         *         )
         *     )
         *
         *     ActivityDetailScreen(
         *         viewModel = viewModel,
         *         navController = navController
         *     )
         * }
         * ```
         */
    }
}

/**
 * NAVIGATION BEST PRACTICES:
 *
 * 1. PASS NAVCONTROLLER TO SCREENS:
 *    Screens need NavController to navigate to other screens
 *    Example: navController.navigate(Screen.AddActivity.route)
 *
 * 2. USE VIEWMODEL FACTORIES:
 *    Don't create ViewModels manually with constructors
 *    Use factory pattern to inject dependencies properly
 *
 * 3. KEEP NAVGRAPH SIMPLE:
 *    Don't put business logic here
 *    Just wire screens to routes and create ViewModels
 *
 * 4. HANDLE BACK NAVIGATION:
 *    ```kotlin
 *    IconButton(onClick = { navController.navigateUp() }) {
 *        Icon(Icons.Default.ArrowBack, "Back")
 *    }
 *    ```
 *
 * 5. PREVENT DUPLICATE NAVIGATION:
 *    ```kotlin
 *    navController.navigate(Screen.Profile.route) {
 *        launchSingleTop = true  // Don't create duplicate if already there
 *    }
 *    ```
 *
 * 6. CLEAR BACK STACK WHEN NEEDED:
 *    ```kotlin
 *    navController.navigate(Screen.Home.route) {
 *        popUpTo(Screen.Home.route) { inclusive = true }
 *    }
 *    ```
 *
 * TESTING NAVIGATION:
 *
 * ```kotlin
 * @Test
 * fun testNavigateToAddActivity() {
 *     val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
 *
 *     composeTestRule.setContent {
 *         navController.navigatorProvider.addNavigator(ComposeNavigator())
 *         SmartFitNavGraph(navController, mockAppContainer)
 *     }
 *
 *     // Verify starts at Home
 *     assertEquals(Screen.Home.route, navController.currentDestination?.route)
 *
 *     // Navigate to AddActivity
 *     composeTestRule.onNodeWithContentDescription("Add Activity").performClick()
 *
 *     // Verify navigation
 *     assertEquals(Screen.AddActivity.route, navController.currentDestination?.route)
 * }
 * ```
 *
 * DEPENDENCY FLOW:
 *
 * AppContainer
 *     ↓ provides
 * Repositories & PreferencesManager
 *     ↓ injected via
 * ViewModel Factories
 *     ↓ creates
 * ViewModels
 *     ↓ used by
 * Screens
 *     ↓ navigated by
 * NavController
 */
