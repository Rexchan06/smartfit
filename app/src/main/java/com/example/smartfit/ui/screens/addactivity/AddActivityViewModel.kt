package com.example.smartfit.ui.screens.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.domain.model.Activity
import com.example.smartfit.util.CalorieCalculator
import com.example.smartfit.util.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ADD ACTIVITY VIEWMODEL
 *
 * Manages form state for adding new activities
 * - Form field values
 * - Validation
 * - Auto-calculate calories
 * - Save to database
 */
class AddActivityViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    init {
        Logger.d("AddActivityViewModel", "Initialized")
    }

    // ============================================================================
    // FORM STATE
    // ============================================================================

    private val _activityType = MutableStateFlow("")
    val activityType: StateFlow<String> = _activityType.asStateFlow()

    private val _duration = MutableStateFlow("")
    val duration: StateFlow<String> = _duration.asStateFlow()

    private val _calories = MutableStateFlow("")
    val calories: StateFlow<String> = _calories.asStateFlow()

    private val _distance = MutableStateFlow("")
    val distance: StateFlow<String> = _distance.asStateFlow()

    private val _steps = MutableStateFlow("")
    val steps: StateFlow<String> = _steps.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    // Validation errors
    private val _typeError = MutableStateFlow<String?>(null)
    val typeError: StateFlow<String?> = _typeError.asStateFlow()

    private val _durationError = MutableStateFlow<String?>(null)
    val durationError: StateFlow<String?> = _durationError.asStateFlow()

    private val _caloriesError = MutableStateFlow<String?>(null)
    val caloriesError: StateFlow<String?> = _caloriesError.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ============================================================================
    // FORM ACTIONS
    // ============================================================================

    fun updateActivityType(value: String) {
        _activityType.value = value
        _typeError.value = null
    }

    fun updateDuration(value: String) {
        _duration.value = value
        _durationError.value = null

        // Auto-calculate calories if type and duration are valid
        autoCalculateCalories()
    }

    fun updateCalories(value: String) {
        _calories.value = value
        _caloriesError.value = null
    }

    fun updateDistance(value: String) {
        _distance.value = value
    }

    fun updateSteps(value: String) {
        _steps.value = value
    }

    fun updateNotes(value: String) {
        _notes.value = value
    }

    /**
     * AUTO-CALCULATE CALORIES
     *
     * Uses CalorieCalculator to estimate calories based on activity type and duration
     * Assumes average weight of 70kg
     */
    private fun autoCalculateCalories() {
        val type = _activityType.value
        val durationStr = _duration.value

        if (type.isNotBlank() && durationStr.isNotBlank()) {
            durationStr.toIntOrNull()?.let { duration ->
                if (duration > 0) {
                    try {
                        val estimatedCalories = CalorieCalculator.calculateCalories(
                            activityType = type,
                            durationMinutes = duration,
                            weightKg = 70.0  // Average weight
                        )
                        _calories.value = estimatedCalories.toString()
                    } catch (e: Exception) {
                        // If activity type not recognized, leave calories empty
                        Logger.d("AddActivityViewModel", "Could not auto-calculate calories for type: $type")
                    }
                }
            }
        }
    }

    /**
     * VALIDATE FORM
     *
     * Returns true if form is valid, false otherwise
     * Sets error messages for invalid fields
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate activity type
        if (_activityType.value.isBlank()) {
            _typeError.value = "Please select an activity type"
            isValid = false
        }

        // Validate duration
        val durationInt = _duration.value.toIntOrNull()
        if (_duration.value.isBlank() || durationInt == null || durationInt <= 0) {
            _durationError.value = "Please enter a valid duration"
            isValid = false
        } else if (durationInt > 1440) {  // Max 24 hours
            _durationError.value = "Duration cannot exceed 24 hours"
            isValid = false
        }

        // Validate calories
        val caloriesInt = _calories.value.toIntOrNull()
        if (_calories.value.isBlank() || caloriesInt == null || caloriesInt <= 0) {
            _caloriesError.value = "Please enter valid calories"
            isValid = false
        } else if (caloriesInt > 10000) {
            _caloriesError.value = "Calories seem too high"
            isValid = false
        }

        return isValid
    }

    /**
     * SAVE ACTIVITY
     *
     * Validates form and saves to database
     * Navigates back on success
     */
    fun saveActivity() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val activity = Activity(
                    type = _activityType.value.trim(),
                    durationMinutes = _duration.value.toInt(),
                    caloriesBurned = _calories.value.toInt(),
                    distanceKm = _distance.value.toDoubleOrNull(),
                    steps = _steps.value.toIntOrNull(),
                    timestamp = System.currentTimeMillis(),
                    notes = _notes.value.trim().ifBlank { null }
                )

                val id = activityRepository.insertActivity(activity)
                Logger.i("AddActivityViewModel", "Activity saved with ID: $id")

                _isSaved.value = true

            } catch (e: Exception) {
                Logger.e("AddActivityViewModel", "Failed to save activity", e)
                _error.value = "Failed to save activity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * RESET FORM
     */
    fun resetForm() {
        _activityType.value = ""
        _duration.value = ""
        _calories.value = ""
        _distance.value = ""
        _steps.value = ""
        _notes.value = ""
        _typeError.value = null
        _durationError.value = null
        _caloriesError.value = null
        _error.value = null
        _isSaved.value = false
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d("AddActivityViewModel", "Cleared")
    }
}
