package com.example.smartfit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.WorkoutRepository
import com.example.smartfit.domain.model.Activity
import com.example.smartfit.domain.model.Workout
import com.example.smartfit.util.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - HomeViewModel
 *
 * ViewModel holds UI state and handles business logic for HomeScreen.
 *
 * KEY CONCEPTS:
 *
 * WHAT IS A VIEWMODEL?
 * - Survives configuration changes (screen rotation, language change)
 * - Holds UI state that outlives the composable lifecycle
 * - Executes business logic (data fetching, calculations)
 * - Connects UI to Repository (data layer)
 * - Manages coroutines with viewModelScope
 *
 * WHY USE VIEWMODEL?
 * ✓ Survives screen rotation (data isn't lost)
 * ✓ Separates UI logic from UI presentation
 * ✓ Makes UI stateless and testable
 * ✓ Manages lifecycle-aware coroutines
 * ✓ Single source of truth for UI state
 *
 * STATEFLOW VS FLOW:
 * - StateFlow: Hot flow, always has a value, UI can collect
 * - Flow: Cold flow, emits when collected
 * - Use StateFlow for UI state
 * - Use Flow for one-shot operations
 *
 * DATA FLOW:
 * Database → DAO → Repository (Flow) → ViewModel (StateFlow) → UI (State)
 *
 * When database changes:
 * 1. Room emits new Flow value
 * 2. Repository transforms to domain model
 * 3. ViewModel updates StateFlow
 * 4. UI recomposes automatically
 */
class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    /**
     * INITIALIZATION
     *
     * init block runs once when ViewModel is created
     * Perfect for loading initial data
     */
    init {
        Logger.d("HomeViewModel", "Initialized")
        loadStats()
        loadWorkoutSuggestions()
    }

    // ============================================================================
    // UI STATE - What the UI observes
    // ============================================================================

    /**
     * RECENT ACTIVITIES - Last 10 activities
     *
     * STATEFLOW<LIST<ACTIVITY>>:
     * - StateFlow: Always has a value, starts with emptyList()
     * - UI collects this and recomposes when it changes
     * - Survives configuration changes
     *
     * STATEFLOW VS MUTABLESTATEFLOW:
     * - _recentActivities: Private, mutable, only ViewModel can change
     * - recentActivities: Public, read-only, UI observes
     * - This prevents UI from accidentally modifying state
     *
     * STATEFLOW CREATION:
     * .stateIn() converts Flow<List<Activity>> to StateFlow<List<Activity>>
     *
     * PARAMETERS:
     * - scope: viewModelScope (cancelled when ViewModel dies)
     * - started: SharingStarted.WhileSubscribed(5000)
     *   - Starts collecting when UI subscribes
     *   - Stops collecting 5 seconds after last subscriber leaves
     *   - Saves resources when app in background
     * - initialValue: emptyList() (shown until first emission)
     */
    val recentActivities: StateFlow<List<Activity>> = activityRepository
        .getRecentActivities(limit = 10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * STATISTICS - Total calories, distance, count
     *
     * MutableStateFlow allows ViewModel to update values
     * Initialized with default values (0)
     */
    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private val _activityCount = MutableStateFlow(0)
    val activityCount: StateFlow<Int> = _activityCount.asStateFlow()

    /**
     * LOADING STATE
     *
     * Indicates whether data is being fetched
     * Used to show loading spinner
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * ERROR STATE
     *
     * Holds error message if something goes wrong
     * null means no error
     */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * WORKOUT SUGGESTIONS - Exercises from API
     *
     * List of workout suggestions to display on home screen
     */
    private val _workoutSuggestions = MutableStateFlow<List<Workout>>(emptyList())
    val workoutSuggestions: StateFlow<List<Workout>> = _workoutSuggestions.asStateFlow()

    // ============================================================================
    // ACTIONS - What the UI can trigger
    // ============================================================================

    /**
     * LOAD STATS - Fetch statistics from repository
     *
     * VIEWMODELSCOPE.LAUNCH:
     * - Launches a coroutine in the ViewModel's scope
     * - Automatically cancelled when ViewModel is destroyed
     * - Suspends without blocking UI thread
     *
     * COLLECT:
     * - Flow terminal operator that receives emitted values
     * - Suspends until Flow completes or scope is cancelled
     * - Each emission updates the StateFlow
     */
    private fun loadStats() {
        viewModelScope.launch {
            Logger.d("HomeViewModel", "Loading statistics")

            try {
                // Collect multiple Flows in parallel using combine
                combine(
                    activityRepository.getTotalCalories(),
                    activityRepository.getTotalDistance(),
                    activityRepository.getActivityCount()
                ) { calories, distance, count ->
                    // This block runs whenever any Flow emits a new value
                    Triple(calories, distance, count)
                }.collect { (calories, distance, count) ->
                    // Update state
                    _totalCalories.value = calories
                    _totalDistance.value = distance
                    _activityCount.value = count

                    Logger.i("HomeViewModel", "Stats updated: $calories cal, $distance km, $count activities")
                }
            } catch (e: Exception) {
                Logger.e("HomeViewModel", "Failed to load stats", e)
                _error.value = "Failed to load statistics"
            }
        }
    }

    /**
     * ADD SAMPLE ACTIVITY - For testing (demonstrates insert operation)
     *
     * This is how you write data to the repository
     *
     * USAGE IN UI:
     * ```kotlin
     * Button(onClick = { viewModel.addSampleActivity() }) {
     *     Text("Add Sample Activity")
     * }
     * ```
     */
    fun addSampleActivity() {
        viewModelScope.launch {
            Logger.d("HomeViewModel", "Adding sample activity")
            _isLoading.value = true

            try {
                val sampleActivity = Activity(
                    type = "Running",
                    durationMinutes = 30,
                    caloriesBurned = 300,
                    distanceKm = 5.0,
                    steps = 6500,
                    timestamp = System.currentTimeMillis(),
                    notes = "Morning run"
                )

                val id = activityRepository.insertActivity(sampleActivity)
                Logger.i("HomeViewModel", "Sample activity added with ID: $id")

                // recentActivities StateFlow automatically updates!
                // No need to manually refresh

            } catch (e: Exception) {
                Logger.e("HomeViewModel", "Failed to add sample activity", e)
                _error.value = "Failed to add activity: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * CLEAR ERROR - Dismiss error message
     *
     * USAGE IN UI:
     * ```kotlin
     * error?.let { errorMessage ->
     *     Snackbar(
     *         action = {
     *             TextButton(onClick = { viewModel.clearError() }) {
     *                 Text("OK")
     *             }
     *         }
     *     ) {
     *         Text(errorMessage)
     *     }
     * }
     * ```
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * LOAD WORKOUT SUGGESTIONS - Fetch exercises from API
     *
     * Fetches workout suggestions to display on home screen
     * Silently fails if network unavailable (optional feature)
     */
    private fun loadWorkoutSuggestions() {
        viewModelScope.launch {
            Logger.d("HomeViewModel", "Starting to load workout suggestions...")
            try {
                val workouts = workoutRepository.getExercises(limit = 5)
                _workoutSuggestions.value = workouts
                Logger.i("HomeViewModel", "✅ Successfully loaded ${workouts.size} workout suggestions")
                workouts.forEach { workout ->
                    Logger.d("HomeViewModel", "  - ${workout.name} (${workout.category})")
                }
            } catch (e: Exception) {
                Logger.e("HomeViewModel", "❌ Failed to load workout suggestions: ${e.message}", e)
                Logger.e("HomeViewModel", "Exception type: ${e.javaClass.simpleName}")
                e.printStackTrace()

                // TEMPORARY: Show sample workouts if API fails (for testing UI)
                Logger.w("HomeViewModel", "Loading sample workout data for testing...")
                _workoutSuggestions.value = getSampleWorkouts()
            }
        }
    }

    /**
     * ON CLEARED - Called when ViewModel is destroyed
     *
     * Use for cleanup (closing connections, cancelling jobs, etc.)
     * viewModelScope automatically cancels all coroutines
     */
    override fun onCleared() {
        super.onCleared()
        Logger.d("HomeViewModel", "Cleared - cleaning up resources")
    }

    /**
     * GET SAMPLE WORKOUTS - Fallback test data
     *
     * TEMPORARY function for testing UI when API is unavailable
     * Remove this once API is working properly
     */
    private fun getSampleWorkouts(): List<Workout> {
        return listOf(
            Workout(
                id = 1,
                name = "Push-ups",
                description = "Start in plank position, lower body until chest nearly touches floor, push back up. Great for building upper body strength.",
                category = "Chest",
                muscles = listOf("Pectorals", "Triceps", "Shoulders"),
                equipment = "Bodyweight",
                difficulty = com.example.smartfit.domain.model.DifficultyLevel.BEGINNER
            ),
            Workout(
                id = 2,
                name = "Squats",
                description = "Stand with feet shoulder-width apart, lower hips back and down, push through heels to return. Essential for leg development.",
                category = "Legs",
                muscles = listOf("Quadriceps", "Glutes", "Hamstrings"),
                equipment = "Bodyweight",
                difficulty = com.example.smartfit.domain.model.DifficultyLevel.BEGINNER
            ),
            Workout(
                id = 3,
                name = "Plank",
                description = "Hold a push-up position with forearms on ground. Keep body straight from head to heels. Core stability exercise.",
                category = "Core",
                muscles = listOf("Abs", "Lower Back"),
                equipment = "Bodyweight",
                difficulty = com.example.smartfit.domain.model.DifficultyLevel.BEGINNER
            ),
            Workout(
                id = 4,
                name = "Lunges",
                description = "Step forward with one leg, lower hips until both knees are bent at 90 degrees, push back to start. Alternating legs.",
                category = "Legs",
                muscles = listOf("Quadriceps", "Glutes"),
                equipment = "Bodyweight",
                difficulty = com.example.smartfit.domain.model.DifficultyLevel.INTERMEDIATE
            ),
            Workout(
                id = 5,
                name = "Burpees",
                description = "From standing, drop to plank, do a push-up, jump feet to hands, jump up with arms overhead. Full body cardio.",
                category = "Cardio",
                muscles = listOf("Full Body"),
                equipment = "Bodyweight",
                difficulty = com.example.smartfit.domain.model.DifficultyLevel.ADVANCED
            )
        )
    }
}

/**
 * COMPLETE DATA FLOW EXAMPLE:
 *
 * 1. USER OPENS HOMESCREEN:
 *    ```kotlin
 *    @Composable
 *    fun HomeScreen(viewModel: HomeViewModel) {
 *        val activities by viewModel.recentActivities.collectAsState()
 *        // UI renders activities
 *    }
 *    ```
 *
 * 2. VIEWMODEL CREATED:
 *    - init { } runs
 *    - Collects repository.getRecentActivities()
 *
 * 3. REPOSITORY RETURNS FLOW:
 *    - dao.getRecentActivities() returns Flow<List<ActivityEntity>>
 *    - repository.map { } converts to Flow<List<Activity>>
 *
 * 4. VIEWMODEL CONVERTS TO STATEFLOW:
 *    - .stateIn() converts Flow to StateFlow
 *    - Starts with empty list
 *
 * 5. DATABASE EMITS DATA:
 *    - Room detects data in database
 *    - Emits List<ActivityEntity> through Flow
 *
 * 6. REPOSITORY TRANSFORMS:
 *    - Converts entities to domain models
 *    - Emits List<Activity>
 *
 * 7. STATEFLOW UPDATES:
 *    - New List<Activity> becomes current value
 *
 * 8. UI RECOMPOSES:
 *    - collectAsState() triggers recomposition
 *    - LazyColumn displays activities
 *
 * 9. USER ADDS ACTIVITY:
 *    - viewModel.addSampleActivity()
 *    - Inserts into database via repository
 *
 * 10. DATABASE CHANGES:
 *     - Room detects new row
 *     - Flow emits updated list
 *     - Steps 6-8 repeat automatically!
 *
 * BENEFITS:
 * ✓ UI always shows current data
 * ✓ No manual refresh needed
 * ✓ Survives configuration changes
 * ✓ Automatic updates when data changes
 * ✓ Clean separation of concerns
 * ✓ Easy to test
 */

/**
 * TESTING VIEWMODELS:
 *
 * ```kotlin
 * @Test
 * fun `loadStats updates state correctly`() = runTest {
 *     // Mock repository
 *     val mockRepo = mockk<ActivityRepository>()
 *     coEvery { mockRepo.getTotalCalories() } returns flowOf(500)
 *     coEvery { mockRepo.getTotalDistance() } returns flowOf(10.0)
 *     coEvery { mockRepo.getActivityCount() } returns flowOf(5)
 *
 *     // Create ViewModel with mock
 *     val viewModel = HomeViewModel(mockRepo)
 *
 *     // Wait for coroutines to complete
 *     advanceUntilIdle()
 *
 *     // Assert state
 *     assertEquals(500, viewModel.totalCalories.value)
 *     assertEquals(10.0, viewModel.totalDistance.value)
 *     assertEquals(5, viewModel.activityCount.value)
 * }
 * ```
 */
