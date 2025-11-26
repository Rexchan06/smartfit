package com.example.smartfit

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.navigation.SmartFitNavGraph
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ACTIVITY LOG DISPLAY UI TESTS
 *
 * Tests displaying activities in the activity log
 * Verifies empty state and list display
 */
@RunWith(AndroidJUnit4::class)
class ActivityLogDisplayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityLog_noActivities_showsEmptyState() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Activity Log
        composeTestRule.onNodeWithText("View All").performClick()
        composeTestRule.waitForIdle()

        // Should show empty state
        composeTestRule.onNodeWithText("No Activities Yet").assertExists()
    }

    @Test
    fun activityLog_hasActivities_displaysActivityCards() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // First, add an activity
        composeTestRule.onNodeWithContentDescription("Add new activity").performClick()
        composeTestRule.waitForIdle()

        // Fill form
        composeTestRule.onNodeWithText("Activity Type *").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Running").performClick()
        composeTestRule.onNodeWithText("Duration (minutes) *").performTextInput("30")
        composeTestRule.onNodeWithText("Calories Burned *").performTextClearance()
        composeTestRule.onNodeWithText("Calories Burned *").performTextInput("300")

        // Save
        composeTestRule.onNodeWithText("Save Activity").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Activity Log
        composeTestRule.onNodeWithText("View All").performClick()
        composeTestRule.waitForIdle()

        // Should show activity card
        composeTestRule.onNodeWithText("Running").assertExists()
        composeTestRule.onNodeWithText("30 min").assertExists()
    }

    @Test
    fun activityLog_clickBack_returnsToHome() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Activity Log
        composeTestRule.onNodeWithText("View All").performClick()
        composeTestRule.waitForIdle()

        // Click back
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Should be on Home
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }
}
