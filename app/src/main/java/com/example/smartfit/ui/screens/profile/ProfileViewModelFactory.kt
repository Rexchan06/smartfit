package com.example.smartfit.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartfit.data.datastore.PreferencesManager
import com.example.smartfit.data.repository.ActivityRepository

/**
 * PROFILE VIEWMODEL FACTORY
 *
 * Creates ProfileViewModel instances with PreferencesManager and ActivityRepository dependencies
 *
 * DEPENDENCIES:
 * - PreferencesManager: For reading/writing user preferences (dark mode, step goal)
 * - ActivityRepository: For displaying user statistics
 */
class ProfileViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                preferencesManager = preferencesManager,
                activityRepository = activityRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
