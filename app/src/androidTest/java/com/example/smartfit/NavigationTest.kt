package com.example.smartfit

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.di.AppContainer
import com.example.smartfit.ui.navigation.Screen
import com.example.smartfit.ui.navigation.SmartFitNavGraph
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NAVIGATION UI TESTS
 *
 * Tests navigation between all screens
 * Verifies routes and screen transitions
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navHost_startsAtHomeScreen() {
        lateinit var navController: TestNavHostController

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = LocalContext.current.applicationContext as SmartFitApplication
            SmartFitNavGraph(navController = navController, appContainer = appContainer.appContainer)
        }

        // Verify we start at Home
        assertEquals(Screen.Home.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navHost_clickAddActivityFAB_navigatesToAddActivityScreen() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Click the FAB on Home screen
        composeTestRule.onNodeWithContentDescription("Add new activity").performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify we navigated to AddActivity
        composeTestRule.onNodeWithText("Add Activity").assertExists()
    }

    @Test
    fun navHost_clickProfile_navigatesToProfileScreen() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Click profile icon
        composeTestRule.onNodeWithContentDescription("Profile settings").performClick()

        composeTestRule.waitForIdle()

        // Verify we navigated to Profile
        composeTestRule.onNodeWithText("Profile & Settings").assertExists()
    }

    @Test
    fun navHost_clickViewAll_navigatesToActivityLog() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Click "View All" button
        composeTestRule.onNodeWithText("View All").performClick()

        composeTestRule.waitForIdle()

        // Verify we navigated to Activity Log
        composeTestRule.onNodeWithText("Activity Log").assertExists()
    }

    @Test
    fun navHost_clickBack_returnsToHome() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Profile
        composeTestRule.onNodeWithContentDescription("Profile settings").performClick()
        composeTestRule.waitForIdle()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Verify we're back at Home
        composeTestRule.onNodeWithText("SmartFit").assertExists()
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }
}
