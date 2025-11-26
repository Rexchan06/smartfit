package com.example.smartfit.data.repository

import com.example.smartfit.data.remote.api.FitnessApiService
import com.example.smartfit.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * WORKOUT REPOSITORY - Handles workout suggestions from API
 *
 * This repository focuses on fetching workout/exercise data from the Wger API.
 * Unlike ActivityRepository (which uses local database), this primarily uses network.
 *
 * KEY DIFFERENCES FROM ACTIVITYREPOSITORY:
 * - ActivityRepository: User's logged activities (local database, persistent)
 * - WorkoutRepository: Exercise suggestions (API, cached temporarily)
 *
 * OFFLINE STRATEGY:
 * For a production app, you might:
 * 1. Cache API responses in Room database
 * 2. Return cached data if network fails
 * 3. Refresh cache periodically
 *
 * For this university project, we're keeping it simple:
 * - Fetch from API
 * - Handle errors gracefully
 * - Return error state to UI
 *
 * ERROR HANDLING:
 * We use Result<T> sealed class pattern:
 * - Success: Contains the data
 * - Error: Contains error message
 * - Loading: Indicates operation in progress
 */
class WorkoutRepository(
    private val apiService: FitnessApiService
) {

    // ============================================================================
    // FETCH WORKOUTS - Get exercises from API
    // ============================================================================

    /**
     * GET WORKOUTS - Fetch list of exercises
     *
     * RETURNS FLOW:
     * Flow emits Result states: Loading → Success/Error
     * UI can show loading spinner, then data or error message
     *
     * WHY FLOW FOR NETWORK CALLS?
     * - Can emit Loading state first
     * - Then emit Success or Error
     * - UI observes and updates accordingly
     * - Could add retry logic, polling, etc.
     *
     * @param limit - Number of exercises to fetch
     * @return Flow<Result<List<Workout>>> - Stream of states
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
     *
     *     private val _workoutsState = MutableStateFlow<Result<List<Workout>>>(Result.Loading)
     *     val workoutsState: StateFlow<Result<List<Workout>>> = _workoutsState
     *
     *     fun loadWorkouts() {
     *         viewModelScope.launch {
     *             repository.getWorkouts(limit = 20)
     *                 .collect { result ->
     *                     _workoutsState.value = result
     *                 }
     *         }
     *     }
     * }
     * ```
     *
     * USAGE IN UI:
     * ```kotlin
     * @Composable
     * fun WorkoutListScreen(viewModel: WorkoutViewModel) {
     *     val state by viewModel.workoutsState.collectAsState()
     *
     *     when (state) {
     *         is Result.Loading -> CircularProgressIndicator()
     *         is Result.Success -> {
     *             val workouts = (state as Result.Success).data
     *             LazyColumn {
     *                 items(workouts) { workout ->
     *                     WorkoutCard(workout)
     *                 }
     *             }
     *         }
     *         is Result.Error -> {
     *             val error = (state as Result.Error).message
     *             Text("Error: $error")
     *         }
     *     }
     * }
     * ```
     */
    fun getWorkouts(limit: Int = 20): Flow<Result<List<Workout>>> = flow {
        // Emit loading state first
        emit(Result.Loading)

        try {
            // Make API call (suspend function)
            val response = apiService.getExercises(limit = limit)

            // Convert DTOs to domain models
            val workouts = response.results.map { it.toDomain() }

            // Emit success with data
            emit(Result.Success(workouts))

        } catch (e: retrofit2.HttpException) {
            // HTTP errors (404, 500, etc.)
            val errorMessage = when (e.code()) {
                404 -> "Exercises not found"
                500 -> "Server error. Please try again later"
                503 -> "Service unavailable. Check your connection"
                else -> "HTTP error: ${e.code()}"
            }
            emit(Result.Error(errorMessage))

        } catch (e: java.io.IOException) {
            // Network errors (no internet, timeout, etc.)
            emit(Result.Error("No internet connection. Please check your network"))

        } catch (e: Exception) {
            // Other errors (JSON parsing, etc.)
            emit(Result.Error("Unexpected error: ${e.message ?: "Unknown error"}"))
        }
    }

    /**
     * GET EXERCISES - Simple suspend function for direct data fetch
     *
     * This is a simpler version for cases where you don't need Flow
     * Returns the data directly or throws exception on error
     *
     * @param limit - Number of exercises to fetch
     * @return List<Workout> - List of workouts
     * @throws Exception - On any error
     */
    suspend fun getExercises(limit: Int = 20): List<Workout> {
        return try {
            val response = apiService.getExercises(limit = limit)
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            // Rethrow - caller handles error
            throw e
        }
    }

    /**
     * GET WORKOUTS BY CATEGORY - Filter by muscle group
     *
     * CATEGORY IDs:
     * - 10: Abs
     * - 8: Arms
     * - 12: Back
     * - 11: Chest
     * - 9: Legs
     * - 13: Shoulders
     *
     * @param categoryId - Category ID to filter
     * @param limit - Number of results
     * @return Flow<Result<List<Workout>>>
     *
     * USAGE:
     * ```kotlin
     * fun loadChestWorkouts() {
     *     viewModelScope.launch {
     *         repository.getWorkoutsByCategory(categoryId = 11)
     *             .collect { _workoutsState.value = it }
     *     }
     * }
     * ```
     */
    fun getWorkoutsByCategory(categoryId: Int, limit: Int = 20): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getExercisesByCategory(categoryId, limit = limit)
            val workouts = response.results.map { it.toDomain() }
            emit(Result.Success(workouts))
        } catch (e: retrofit2.HttpException) {
            emit(Result.Error("HTTP error: ${e.code()}"))
        } catch (e: java.io.IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * GET BODYWEIGHT EXERCISES - Filter by equipment (bodyweight = no equipment)
     *
     * Perfect for users who want at-home workouts
     *
     * @param limit - Number of results
     * @return Flow<Result<List<Workout>>>
     *
     * USAGE:
     * ```kotlin
     * // Home screen: Show "At-Home Workouts" section
     * fun loadAtHomeWorkouts() {
     *     viewModelScope.launch {
     *         repository.getBodyweightExercises(limit = 10)
     *             .collect { result ->
     *                 when (result) {
     *                     is Result.Success -> _atHomeWorkouts.value = result.data
     *                     is Result.Error -> _error.value = result.message
     *                     Result.Loading -> { /* show loading */ }
     *                 }
     *             }
     *     }
     * }
     * ```
     */
    fun getBodyweightExercises(limit: Int = 20): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        try {
            // Equipment ID 7 = Bodyweight
            val response = apiService.getExercisesByEquipment(equipmentId = 7, limit = limit)
            val workouts = response.results.map { it.toDomain() }
            emit(Result.Success(workouts))
        } catch (e: retrofit2.HttpException) {
            emit(Result.Error("HTTP error: ${e.code()}"))
        } catch (e: java.io.IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * SEARCH WORKOUTS - Search by name
     *
     * @param query - Search term
     * @param limit - Number of results
     * @return Flow<Result<List<Workout>>>
     *
     * USAGE:
     * ```kotlin
     * // Search bar in UI
     * var searchQuery by remember { mutableStateOf("") }
     *
     * TextField(
     *     value = searchQuery,
     *     onValueChange = { query ->
     *         searchQuery = query
     *         if (query.length >= 3) {  // Search after 3 chars
     *             viewModel.searchWorkouts(query)
     *         }
     *     }
     * )
     *
     * // In ViewModel
     * fun searchWorkouts(query: String) {
     *     viewModelScope.launch {
     *         repository.searchWorkouts(query)
     *             .collect { _searchResults.value = it }
     *     }
     * }
     * ```
     */
    fun searchWorkouts(query: String, limit: Int = 20): Flow<Result<List<Workout>>> = flow {
        if (query.isBlank()) {
            emit(Result.Success(emptyList()))
            return@flow
        }

        emit(Result.Loading)
        try {
            val response = apiService.searchExercises(query, limit = limit)
            val workouts = response.results.map { it.toDomain() }
            emit(Result.Success(workouts))
        } catch (e: retrofit2.HttpException) {
            emit(Result.Error("HTTP error: ${e.code()}"))
        } catch (e: java.io.IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * GET WORKOUTS BY MUSCLE GROUP - Target specific muscles
     *
     * MUSCLE IDs:
     * - 1: Biceps
     * - 2: Front Shoulders
     * - 4: Chest (Pectorals)
     * - 3: Back (Lats)
     * - 10: Quadriceps
     * - 11: Hamstrings
     * - 8: Triceps
     * - 6: Abs
     *
     * @param muscleId - Muscle group ID
     * @param limit - Number of results
     * @return Flow<Result<List<Workout>>>
     */
    fun getWorkoutsByMuscle(muscleId: Int, limit: Int = 20): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getExercisesByMuscle(muscleId, limit = limit)
            val workouts = response.results.map { it.toDomain() }
            emit(Result.Success(workouts))
        } catch (e: retrofit2.HttpException) {
            emit(Result.Error("HTTP error: ${e.code()}"))
        } catch (e: java.io.IOException) {
            emit(Result.Error("No internet connection"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}

/**
 * RESULT SEALED CLASS - Represents state of an operation
 *
 * Sealed classes are perfect for representing states because:
 * - Compiler knows all possible types (exhaustive when)
 * - Type-safe (can't create invalid states)
 * - Clear and readable
 */
sealed class Result<out T> {
    /**
     * LOADING - Operation in progress
     * Use to show loading spinner
     */
    object Loading : Result<Nothing>()

    /**
     * SUCCESS - Operation completed successfully
     * Contains the data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * ERROR - Operation failed
     * Contains error message
     */
    data class Error(val message: String) : Result<Nothing>()
}

/**
 * EXTENSION FUNCTIONS FOR RESULT (Optional but useful):
 *
 * ```kotlin
 * fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
 * fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
 * fun <T> Result<T>.isError(): Boolean = this is Result.Error
 *
 * fun <T> Result<T>.getOrNull(): T? = when (this) {
 *     is Result.Success -> data
 *     else -> null
 * }
 *
 * fun <T> Result<T>.errorOrNull(): String? = when (this) {
 *     is Result.Error -> message
 *     else -> null
 * }
 * ```
 *
 * USAGE IN UI WITH EXTENSIONS:
 * ```kotlin
 * when {
 *     state.isLoading() -> CircularProgressIndicator()
 *     state.isSuccess() -> WorkoutList(state.getOrNull()!!)
 *     state.isError() -> ErrorMessage(state.errorOrNull()!!)
 * }
 * ```
 */

/**
 * CACHING STRATEGY (Future Enhancement):
 *
 * For better performance, you could add caching:
 *
 * ```kotlin
 * class WorkoutRepository(
 *     private val apiService: FitnessApiService,
 *     private val workoutDao: WorkoutDao  // Add Room caching
 * ) {
 *     fun getWorkouts(): Flow<Result<List<Workout>>> = flow {
 *         emit(Result.Loading)
 *
 *         // 1. Return cached data immediately
 *         val cached = workoutDao.getAllWorkouts().first()
 *         if (cached.isNotEmpty()) {
 *             emit(Result.Success(cached))
 *         }
 *
 *         // 2. Fetch fresh data from API
 *         try {
 *             val response = apiService.getExercises()
 *             val workouts = response.results.map { it.toDomain() }
 *
 *             // 3. Update cache
 *             workoutDao.insertAll(workouts.map { WorkoutEntity.fromDomain(it) })
 *
 *             // 4. Emit fresh data
 *             emit(Result.Success(workouts))
 *         } catch (e: Exception) {
 *             // If network fails and we have cache, that's ok
 *             if (cached.isEmpty()) {
 *                 emit(Result.Error("No data available"))
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * BENEFITS:
 * ✓ Offline support
 * ✓ Faster loading (show cache while fetching)
 * ✓ Reduced API calls
 * ✓ Better user experience
 */
