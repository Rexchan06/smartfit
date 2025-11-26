package com.example.smartfit.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PREFERENCES MANAGER - DataStore Wrapper
 *
 * This class provides a clean, type-safe API for accessing user preferences stored in DataStore.
 *
 * WHY THIS CLASS EXISTS:
 * ✓ Encapsulates DataStore complexity - UI doesn't deal with Preferences keys directly
 * ✓ Type-safe API - Returns Flow<Boolean> not Flow<Preferences>
 * ✓ Single source of truth for preference keys
 * ✓ Easy to test - can mock this class in tests
 * ✓ Centralized - all preference logic in one place
 *
 * DATASTORE VS SHAREDPREFERENCES:
 * ✓ Async - doesn't block UI thread
 * ✓ Type-safe with Kotlin Flows
 * ✓ Transactional - updates are atomic
 * ✓ Observable - UI automatically updates when data changes
 *
 * USAGE IN VIEWMODEL:
 * ```kotlin
 * class ProfileViewModel(
 *     private val preferencesManager: PreferencesManager
 * ) : ViewModel() {
 *     // Observe dark theme preference
 *     val isDarkTheme: Flow<Boolean> = preferencesManager.isDarkTheme
 *
 *     // Update preference
 *     fun toggleTheme(isDark: Boolean) {
 *         viewModelScope.launch {
 *             preferencesManager.updateTheme(isDark)
 *         }
 *     }
 * }
 * ```
 *
 * USAGE IN UI:
 * ```kotlin
 * @Composable
 * fun ProfileScreen(viewModel: ProfileViewModel) {
 *     val isDarkTheme by viewModel.isDarkTheme.collectAsState(initial = false)
 *
 *     Switch(
 *         checked = isDarkTheme,
 *         onCheckedChange = { viewModel.toggleTheme(it) }
 *     )
 * }
 * ```
 */
class PreferencesManager(private val dataStore: DataStore<Preferences>) {

    // ============================================================================
    // PREFERENCE KEYS - Define all keys in one place
    // ============================================================================

    /**
     * Preference keys are used to identify values in DataStore
     * Using companion object makes them accessible but not instantiable
     */
    companion object {
        // Theme preference key
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")

        // Daily step goal key
        private val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")

        // Default values
        const val DEFAULT_STEP_GOAL = 10000  // WHO recommends 10,000 steps/day
    }

    // ============================================================================
    // THEME PREFERENCE
    // ============================================================================

    /**
     * Observe dark theme preference
     *
     * Returns Flow<Boolean> that emits whenever the preference changes
     * UI can collect this and automatically update when user toggles theme
     *
     * DEFAULT: false (light mode)
     * If user hasn't set preference, returns false
     */
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME] ?: false  // Default to light mode
    }

    /**
     * Update theme preference
     *
     * This is a suspend function because DataStore operations are async
     * Call from coroutine scope (e.g., viewModelScope)
     *
     * @param isDark - true for dark mode, false for light mode
     *
     * EXAMPLE:
     * ```kotlin
     * viewModelScope.launch {
     *     preferencesManager.updateTheme(true)  // Enable dark mode
     * }
     * ```
     */
    suspend fun updateTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }

    // ============================================================================
    // STEP GOAL PREFERENCE
    // ============================================================================

    /**
     * Observe daily step goal preference
     *
     * Returns Flow<Int> that emits whenever the goal changes
     * UI can display progress: currentSteps / dailyStepGoal
     *
     * DEFAULT: 10,000 steps (WHO recommendation)
     */
    val dailyStepGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_STEP_GOAL] ?: DEFAULT_STEP_GOAL
    }

    /**
     * Update daily step goal
     *
     * @param goal - Number of steps (e.g., 5000, 10000, 15000)
     *
     * VALIDATION:
     * - Goal must be positive
     * - Reasonable range: 1,000 - 50,000 steps
     *
     * EXAMPLE:
     * ```kotlin
     * viewModelScope.launch {
     *     preferencesManager.updateStepGoal(12000)
     * }
     * ```
     */
    suspend fun updateStepGoal(goal: Int) {
        require(goal > 0) { "Step goal must be positive" }
        require(goal in 1000..50000) { "Step goal must be between 1,000 and 50,000" }

        dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL] = goal
        }
    }

    // ============================================================================
    // FUTURE PREFERENCES (Examples for expansion)
    // ============================================================================

    /**
     * You can easily add more preferences by following the same pattern:
     *
     * 1. Define key in companion object:
     *    private val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
     *
     * 2. Create Flow to observe:
     *    val weightUnit: Flow<String> = dataStore.data.map { prefs ->
     *        prefs[WEIGHT_UNIT] ?: "kg"
     *    }
     *
     * 3. Create update function:
     *    suspend fun updateWeightUnit(unit: String) {
     *        dataStore.edit { prefs -> prefs[WEIGHT_UNIT] = unit }
     *    }
     *
     * OTHER USEFUL PREFERENCES:
     * - Notification enabled/disabled
     * - Weight unit (kg/lbs)
     * - Distance unit (km/miles)
     * - First name / user profile info
     * - Last sync timestamp
     * - Onboarding completed flag
     */
}

/**
 * TESTING PREFERENCESMANAGER:
 *
 * ```kotlin
 * @Test
 * fun `updateTheme saves to datastore`() = runTest {
 *     // Create test DataStore
 *     val testDataStore = TestDataStore()
 *     val prefsManager = PreferencesManager(testDataStore)
 *
 *     // Update theme
 *     prefsManager.updateTheme(true)
 *
 *     // Verify it was saved
 *     val isDark = prefsManager.isDarkTheme.first()
 *     assertTrue(isDark)
 * }
 * ```
 *
 * ARCHITECTURE BENEFITS:
 * ✓ ViewModels depend on PreferencesManager, not DataStore directly
 * ✓ Easy to mock PreferencesManager in tests
 * ✓ All preference keys in one place - no magic strings
 * ✓ Type-safe API - can't accidentally store wrong type
 * ✓ Validation logic centralized (e.g., step goal range check)
 */
