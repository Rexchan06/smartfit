package com.example.smartfit.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.datastore.PreferencesManager
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.util.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * PROFILE VIEWMODEL
 *
 * Manages user settings and profile data:
 * - Dark theme toggle (DataStore)
 * - Daily step goal (DataStore)
 * - User statistics
 */
class ProfileViewModel(
    private val preferencesManager: PreferencesManager,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    init {
        Logger.d("ProfileViewModel", "Initialized")
        loadStatistics()
    }

    // ============================================================================
    // PREFERENCES (DataStore)
    // ============================================================================

    /**
     * DARK THEME PREFERENCE
     *
     * Reads from DataStore, UI observes this
     * When user toggles switch, updates DataStore
     */
    val isDarkTheme: Flow<Boolean> = preferencesManager.isDarkTheme

    /**
     * DAILY STEP GOAL
     *
     * Reads from DataStore
     */
    val dailyStepGoal: Flow<Int> = preferencesManager.dailyStepGoal

    /**
     * TOGGLE THEME
     *
     * Updates dark theme preference in DataStore
     */
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updateTheme(isDark)
                Logger.i("ProfileViewModel", "Theme updated: isDark=$isDark")
            } catch (e: Exception) {
                Logger.e("ProfileViewModel", "Failed to update theme", e)
                _error.value = "Failed to save theme preference"
            }
        }
    }

    /**
     * UPDATE STEP GOAL
     *
     * Updates daily step goal in DataStore
     */
    fun updateStepGoal(goal: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.updateStepGoal(goal)
                Logger.i("ProfileViewModel", "Step goal updated: $goal")
            } catch (e: Exception) {
                Logger.e("ProfileViewModel", "Failed to update step goal", e)
                _error.value = "Failed to save step goal: ${e.message}"
            }
        }
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    private val _totalActivities = MutableStateFlow(0)
    val totalActivities: StateFlow<Int> = _totalActivities.asStateFlow()

    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * LOAD STATISTICS
     *
     * Fetches user's all-time statistics
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                combine(
                    activityRepository.getActivityCount(),
                    activityRepository.getTotalCalories(),
                    activityRepository.getTotalDistance()
                ) { count, calories, distance ->
                    Triple(count, calories, distance)
                }.collect { (count, calories, distance) ->
                    _totalActivities.value = count
                    _totalCalories.value = calories
                    _totalDistance.value = distance
                }
            } catch (e: Exception) {
                Logger.e("ProfileViewModel", "Failed to load statistics", e)
                _error.value = "Failed to load statistics"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d("ProfileViewModel", "Cleared")
    }
}
