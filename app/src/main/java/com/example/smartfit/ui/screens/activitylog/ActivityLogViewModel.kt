package com.example.smartfit.ui.screens.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.domain.model.Activity
import com.example.smartfit.util.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ACTIVITY LOG VIEWMODEL
 *
 * Manages state for ActivityLogScreen
 * - All activities list
 * - Delete activity
 * - Filter by type (optional feature)
 */
class ActivityLogViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    init {
        Logger.d("ActivityLogViewModel", "Initialized")
    }

    // ============================================================================
    // UI STATE
    // ============================================================================

    /**
     * ALL ACTIVITIES - Complete list of activities
     *
     * Reactive Flow from database
     * Automatically updates when activities are added/deleted
     */
    val allActivities: StateFlow<List<Activity>> = activityRepository
        .getAllActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _deleteConfirmation = MutableStateFlow<Activity?>(null)
    val deleteConfirmation: StateFlow<Activity?> = _deleteConfirmation.asStateFlow()

    // ============================================================================
    // ACTIONS
    // ============================================================================

    /**
     * SHOW DELETE CONFIRMATION
     *
     * Shows dialog asking user to confirm deletion
     */
    fun showDeleteConfirmation(activity: Activity) {
        _deleteConfirmation.value = activity
    }

    /**
     * CANCEL DELETE
     *
     * Dismiss delete confirmation dialog
     */
    fun cancelDelete() {
        _deleteConfirmation.value = null
    }

    /**
     * DELETE ACTIVITY
     *
     * Removes activity from database
     * Automatically updates UI via reactive Flow
     */
    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _deleteConfirmation.value = null

            try {
                activityRepository.deleteActivity(activity)
                Logger.i("ActivityLogViewModel", "Deleted activity: ${activity.type} - ${activity.id}")
            } catch (e: Exception) {
                Logger.e("ActivityLogViewModel", "Failed to delete activity", e)
                _error.value = "Failed to delete activity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * CLEAR ERROR
     */
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d("ActivityLogViewModel", "Cleared")
    }
}
