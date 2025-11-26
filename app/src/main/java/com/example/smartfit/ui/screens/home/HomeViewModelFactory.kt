package com.example.smartfit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.WorkoutRepository

/**
 * HOME VIEWMODEL FACTORY
 *
 * Factory class that creates HomeViewModel instances with required dependencies.
 *
 * WHY DO WE NEED FACTORIES?
 * ✓ ViewModels can't have constructor parameters directly
 * ✓ Need to inject dependencies (repositories) into ViewModel
 * ✓ ViewModelProvider uses factories to create ViewModels
 * ✓ Ensures proper lifecycle management
 *
 * HOW IT WORKS:
 * 1. Factory receives dependencies (repositories) in constructor
 * 2. ViewModelProvider calls create() when ViewModel is needed
 * 3. Factory creates ViewModel with dependencies
 * 4. ViewModel is cached and reused across configuration changes
 *
 * USAGE:
 * ```kotlin
 * val viewModel: HomeViewModel = viewModel(
 *     factory = HomeViewModelFactory(
 *         activityRepository = appContainer.activityRepository,
 *         workoutRepository = appContainer.workoutRepository
 *     )
 * )
 * ```
 */
class HomeViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {

    /**
     * CREATE - Called by ViewModelProvider to create ViewModel instance
     *
     * @param modelClass - The ViewModel class to create
     * @return Instance of the ViewModel
     *
     * Type checking ensures we only create HomeViewModel
     * Throws exception if wrong ViewModel class requested
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                activityRepository = activityRepository,
                workoutRepository = workoutRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
