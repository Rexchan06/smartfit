package com.example.smartfit.ui.screens.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartfit.data.repository.ActivityRepository

/**
 * ADD ACTIVITY VIEWMODEL FACTORY
 *
 * Creates AddActivityViewModel instances with ActivityRepository dependency
 */
class AddActivityViewModelFactory(
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddActivityViewModel::class.java)) {
            return AddActivityViewModel(activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
