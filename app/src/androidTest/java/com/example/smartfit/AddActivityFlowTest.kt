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
 * ADD ACTIVITY FLOW UI TESTS
 *
 * Tests the complete flow of adding a new activity
 * Verifies form validation and successful save
 */
@RunWith(AndroidJUnit4::class)
class AddActivityFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addActivity_fillFormAndSave_activityIsCreated() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Add Activity screen
        composeTestRule.onNodeWithContentDescription("Add new activity").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on Add Activity screen
        composeTestRule.onNodeWithText("Add Activity").assertExists()

        // Fill in the form
        // Activity type dropdown
        composeTestRule.onNodeWithText("Activity Type *").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Running").performClick()

        // Duration
        composeTestRule.onNodeWithText("Duration (minutes) *").performTextInput("30")

        // Calories (should auto-calculate, but we can override)
        composeTestRule.onNodeWithText("Calories Burned *").performTextClearance()
        composeTestRule.onNodeWithText("Calories Burned *").performTextInput("300")

        // Distance (optional)
        composeTestRule.onNodeWithText("Distance (km)").performTextInput("5.0")

        // Click Save
        composeTestRule.onNodeWithText("Save Activity").performClick()

        // Wait for save operation
        composeTestRule.waitForIdle()

        // Should navigate back to Home screen
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }

    @Test
    fun addActivity_emptyForm_showsValidationErrors() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Add Activity
        composeTestRule.onNodeWithContentDescription("Add new activity").performClick()
        composeTestRule.waitForIdle()

        // Try to save without filling form
        composeTestRule.onNodeWithText("Save Activity").performClick()
        composeTestRule.waitForIdle()

        // Should show validation errors
        composeTestRule.onNodeWithText("Please select an activity type").assertExists()
    }

    @Test
    fun addActivity_clickCancel_returnsToHome() {
        composeTestRule.setContent {
            val context = LocalContext.current
            val navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val appContainer = (context.applicationContext as SmartFitApplication).appContainer
            SmartFitNavGraph(navController = navController, appContainer = appContainer)
        }

        // Navigate to Add Activity
        composeTestRule.onNodeWithContentDescription("Add new activity").performClick()
        composeTestRule.waitForIdle()

        // Click cancel (X button)
        composeTestRule.onNodeWithContentDescription("Cancel and go back").performClick()
        composeTestRule.waitForIdle()

        // Should be back on Home
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }
}
