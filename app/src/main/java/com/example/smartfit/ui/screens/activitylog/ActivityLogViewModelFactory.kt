package com.example.smartfit.ui.screens.activitylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartfit.data.repository.ActivityRepository

/**
 * ACTIVITY LOG VIEWMODEL FACTORY
 *
 * Creates ActivityLogViewModel instances with ActivityRepository dependency
 */
class ActivityLogViewModelFactory(
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityLogViewModel::class.java)) {
            return ActivityLogViewModel(activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
