package com.example.smartfit.ui.navigation

/**
 * NAVIGATION SCREENS - Type-safe Navigation Routes
 *
 * This sealed class defines all app screens and their routes.
 * Using sealed class ensures type-safety and compile-time checking.
 *
 * WHY SEALED CLASS?
 * ✓ Type-safe - can't pass invalid screen
 * ✓ Exhaustive when - compiler ensures you handle all cases
 * ✓ Clear structure - all screens in one place
 * ✓ Easy to add arguments to specific screens
 * ✓ Autocomplete support in IDE
 *
 * NAVIGATION CONCEPTS:
 *
 * ROUTE:
 * A string that uniquely identifies a screen (like a URL path)
 * Example: "home", "activity_log", "add_activity/{activityId}"
 *
 * ARGUMENTS:
 * Data passed between screens
 * Example: activityId when navigating to edit screen
 *
 * USAGE IN NAVHOST:
 * ```kotlin
 * NavHost(startDestination = Screen.Home.route) {
 *     composable(Screen.Home.route) { HomeScreen() }
 *     composable(Screen.ActivityLog.route) { ActivityLogScreen() }
 * }
 * ```
 *
 * USAGE TO NAVIGATE:
 * ```kotlin
 * navController.navigate(Screen.AddActivity.route)
 * navController.navigate(Screen.ActivityDetail.createRoute(activityId = 123))
 * ```
 */
sealed class Screen(val route: String) {

    /**
     * HOME SCREEN
     * Main dashboard showing:
     * - Daily statistics (calories, steps, activities)
     * - Recent activities list
     * - Workout suggestions from API
     * - FAB to add new activity
     *
     * Route: "home"
     * Arguments: None
     */
    data object Home : Screen("home")

    /**
     * ACTIVITY LOG SCREEN
     * Full list of all recorded activities
     * Features:
     * - Scrollable list of activities
     * - Filter by date range
     * - Sort by date/calories/duration
     * - Swipe to delete
     * - Click to view/edit details
     *
     * Route: "activity_log"
     * Arguments: None
     */
    data object ActivityLog : Screen("activity_log")

    /**
     * ADD ACTIVITY SCREEN
     * Form to create new fitness activity
     * Fields:
     * - Activity type (Running, Cycling, Walking, etc.)
     * - Duration (minutes)
     * - Calories burned (or auto-calculated)
     * - Distance (optional)
     * - Steps (optional)
     * - Notes (optional)
     *
     * Route: "add_activity"
     * Arguments: None
     *
     * Can be extended to support editing:
     * Route: "add_activity?activityId={activityId}"
     * Then check if activityId is present to determine add vs edit mode
     */
    data object AddActivity : Screen("add_activity")

    /**
     * PROFILE SCREEN
     * User settings and preferences
     * Features:
     * - Dark mode toggle (saves to DataStore)
     * - Daily step goal setting
     * - User statistics (total activities, calories this week)
     * - About section (app version)
     * - Clear all data option
     *
     * Route: "profile"
     * Arguments: None
     */
    data object Profile : Screen("profile")

    // ============================================================================
    // FUTURE SCREENS (Examples for expansion)
    // ============================================================================

    /**
     * ACTIVITY DETAIL SCREEN (Optional - for future)
     *
     * Show detailed view of a single activity with option to edit/delete
     *
     * Route: "activity_detail/{activityId}"
     * Argument: activityId (Int)
     *
     * Usage:
     * ```kotlin
     * data object ActivityDetail : Screen("activity_detail/{activityId}") {
     *     const val ARG_ACTIVITY_ID = "activityId"
     *
     *     fun createRoute(activityId: Int): String {
     *         return "activity_detail/$activityId"
     *     }
     * }
     * ```
     *
     * Then in NavHost:
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
     *     ActivityDetailScreen(activityId = activityId)
     * }
     * ```
     */

    /**
     * STATISTICS SCREEN (Optional - for future)
     *
     * Data visualization screen with charts:
     * - Weekly calorie burn chart
     * - Activity type breakdown (pie chart)
     * - Progress over time (line chart)
     * - Personal records
     *
     * Route: "statistics"
     */

    /**
     * WORKOUT LIBRARY SCREEN (Optional - for future)
     *
     * Browse exercises from API:
     * - Search exercises
     * - Filter by muscle group
     * - View exercise details with images
     * - Add to favorites
     *
     * Route: "workout_library"
     */
}

/**
 * NAVIGATION BEST PRACTICES:
 *
 * 1. USE TYPE-SAFE ROUTES:
 *    ✓ Screen.Home.route
 *    ✗ "home" (string literal)
 *
 * 2. HANDLE BACK NAVIGATION:
 *    ```kotlin
 *    IconButton(onClick = { navController.navigateUp() }) {
 *        Icon(Icons.Default.ArrowBack, "Back")
 *    }
 *    ```
 *
 * 3. POP TO SPECIFIC SCREEN:
 *    ```kotlin
 *    navController.navigate(Screen.Home.route) {
 *        // Clear back stack up to Home
 *        popUpTo(Screen.Home.route) { inclusive = true }
 *    }
 *    ```
 *
 * 4. SINGLE TOP (prevent duplicates):
 *    ```kotlin
 *    navController.navigate(Screen.Profile.route) {
 *        launchSingleTop = true  // Reuse existing Profile screen if already on top
 *    }
 *    ```
 *
 * 5. PASSING COMPLEX DATA:
 *    For complex objects, use a shared ViewModel or pass ID and fetch data in destination screen
 *    Don't try to serialize entire objects in navigation arguments
 *
 * TESTING NAVIGATION:
 *
 * ```kotlin
 * @Test
 * fun testNavigationToAddActivity() {
 *     val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
 *     composeTestRule.setContent {
 *         navController.navigatorProvider.addNavigator(ComposeNavigator())
 *         NavHost(navController, startDestination = Screen.Home.route) {
 *             composable(Screen.Home.route) { HomeScreen(navController) }
 *             composable(Screen.AddActivity.route) { AddActivityScreen() }
 *         }
 *     }
 *
 *     // Click FAB on Home
 *     composeTestRule.onNodeWithContentDescription("Add Activity").performClick()
 *
 *     // Verify navigated to AddActivity
 *     assertEquals(Screen.AddActivity.route, navController.currentDestination?.route)
 * }
 * ```
 */
