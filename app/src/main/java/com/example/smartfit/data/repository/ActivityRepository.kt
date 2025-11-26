package com.example.smartfit.data.repository

import com.example.smartfit.data.local.dao.ActivityDao
import com.example.smartfit.data.local.entity.ActivityEntity
import com.example.smartfit.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * REPOSITORY PATTERN - ActivityRepository
 *
 * The Repository is the "single source of truth" for data in your app.
 * It sits between the ViewModel and the data sources (Database, API, etc.).
 *
 * WHY USE REPOSITORY PATTERN?
 *
 * 1. ABSTRACTION:
 *    ViewModels don't know WHERE data comes from (database, API, cache)
 *    They just ask the repository for data
 *
 * 2. SINGLE SOURCE OF TRUTH:
 *    All data access goes through one place
 *    Makes it easy to:
 *    - Add caching
 *    - Switch data sources
 *    - Handle offline mode
 *    - Implement sync logic
 *
 * 3. SEPARATION OF CONCERNS:
 *    ViewModel: UI logic and state management
 *    Repository: Data fetching and caching logic
 *    DAO/API: Actual data operations
 *
 * 4. TESTABILITY:
 *    Easy to mock repository in ViewModel tests
 *    Easy to test repository with fake DAO/API
 *
 * 5. CLEAN ARCHITECTURE:
 *    Domain layer (ViewModels) depends on Repository interface
 *    Data layer (Room, Retrofit) implements the interface
 *    UI never directly accesses database or network
 *
 * DATA FLOW:
 *
 * READING DATA:
 * Database → DAO → Repository → ViewModel → UI
 * Flow<ActivityEntity> → Flow<Activity> → StateFlow<List<Activity>> → State
 *
 * WRITING DATA:
 * UI → ViewModel → Repository → DAO → Database
 * User action → suspend function → suspend function → Room insert
 *
 * KEY CONCEPTS:
 *
 * FLOW TRANSFORMATION:
 * - DAO returns Flow<List<ActivityEntity>> (database entities)
 * - Repository maps to Flow<List<Activity>> (domain models)
 * - ViewModel collects as StateFlow<List<Activity>>
 * - UI observes StateFlow and updates automatically
 *
 * WHY TRANSFORM?
 * - Keeps database implementation details out of UI layer
 * - Domain models can have computed properties
 * - Can combine multiple data sources
 * - Easier to test with simple domain models
 */
class ActivityRepository(
    private val activityDao: ActivityDao
) {

    // ============================================================================
    // READ OPERATIONS - Query data
    // ============================================================================

    /**
     * GET ALL ACTIVITIES - Returns Flow of all activities
     *
     * FLOW TRANSFORMATION:
     * 1. DAO emits Flow<List<ActivityEntity>> from database
     * 2. .map { } transforms each emission
     * 3. entities.map { it.toDomain() } converts each entity to domain model
     * 4. Returns Flow<List<Activity>> to ViewModel
     *
     * REACTIVE UPDATES:
     * - When database changes (insert/update/delete), DAO emits new list
     * - Repository transforms it to domain models
     * - ViewModel's StateFlow updates
     * - UI recomposes automatically
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * class ActivityLogViewModel(
     *     private val repository: ActivityRepository
     * ) : ViewModel() {
     *
     *     val activities: StateFlow<List<Activity>> = repository
     *         .getAllActivities()
     *         .stateIn(
     *             scope = viewModelScope,
     *             started = SharingStarted.WhileSubscribed(5000),
     *             initialValue = emptyList()
     *         )
     * }
     * ```
     *
     * USAGE IN UI:
     * ```kotlin
     * @Composable
     * fun ActivityLogScreen(viewModel: ActivityLogViewModel) {
     *     val activities by viewModel.activities.collectAsState()
     *
     *     LazyColumn {
     *         items(activities) { activity ->
     *             ActivityCard(activity)
     *         }
     *     }
     * }
     * ```
     */
    fun getAllActivities(): Flow<List<Activity>> {
        return activityDao.getAllActivities()
            .map { entities ->
                // Convert each ActivityEntity to Activity domain model
                entities.map { it.toDomain() }
            }
    }

    /**
     * GET ACTIVITY BY ID - Returns single activity by ID
     *
     * @param id - Activity ID
     * @return Flow<Activity?> - null if not found
     *
     * USAGE:
     * ```kotlin
     * // In ViewModel
     * val activity: StateFlow<Activity?> = repository
     *     .getActivityById(activityId)
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
     *
     * // In UI
     * val activity by viewModel.activity.collectAsState()
     * activity?.let {
     *     ActivityDetailCard(it)
     * } ?: Text("Loading...")
     * ```
     */
    fun getActivityById(id: Int): Flow<Activity?> {
        return activityDao.getActivityById(id)
            .map { entity ->
                entity?.toDomain()  // Convert to domain model, or null if not found
            }
    }

    /**
     * GET ACTIVITIES BY TYPE - Filter by activity type
     *
     * SUSPEND FUNCTION (not Flow):
     * - Single-shot query, doesn't observe changes
     * - Returns once, then completes
     * - Use for one-time operations
     *
     * @param type - Activity type (e.g., "Running", "Cycling")
     * @return List<Activity> - Filtered activities
     *
     * USAGE:
     * ```kotlin
     * // In ViewModel
     * fun loadRunningActivities() {
     *     viewModelScope.launch {
     *         val runs = repository.getActivitiesByType("Running")
     *         _runningActivities.value = runs
     *     }
     * }
     * ```
     */
    suspend fun getActivitiesByType(type: String): List<Activity> {
        return activityDao.getActivitiesByType(type)
            .map { it.toDomain() }
    }

    /**
     * GET ACTIVITIES IN DATE RANGE - Filter by time period
     *
     * @param startTime - Start timestamp (milliseconds)
     * @param endTime - End timestamp (milliseconds)
     * @return Flow<List<Activity>> - Activities in range
     *
     * USAGE:
     * ```kotlin
     * // Get this week's activities
     * val calendar = Calendar.getInstance()
     * calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
     * calendar.set(Calendar.HOUR_OF_DAY, 0)
     * val weekStart = calendar.timeInMillis
     * val weekEnd = System.currentTimeMillis()
     *
     * val weekActivities = repository.getActivitiesInDateRange(weekStart, weekEnd)
     * ```
     */
    fun getActivitiesInDateRange(startTime: Long, endTime: Long): Flow<List<Activity>> {
        return activityDao.getActivitiesInDateRange(startTime, endTime)
            .map { entities -> entities.map { it.toDomain() } }
    }

    /**
     * GET RECENT ACTIVITIES - Last N activities
     *
     * @param limit - Number of activities to return
     * @return Flow<List<Activity>> - Most recent activities
     *
     * USAGE:
     * ```kotlin
     * // Show last 5 activities on home screen
     * val recentActivities = repository.getRecentActivities(5)
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
     * ```
     */
    fun getRecentActivities(limit: Int = 10): Flow<List<Activity>> {
        return activityDao.getRecentActivities(limit)
            .map { entities -> entities.map { it.toDomain() } }
    }

    // ============================================================================
    // STATISTICS - Aggregate data
    // ============================================================================

    /**
     * GET TOTAL CALORIES - Sum of all calories burned
     *
     * @return Flow<Int> - Total calories
     *
     * NO TRANSFORMATION NEEDED:
     * DAO returns Int, we don't need to convert to domain model
     *
     * USAGE:
     * ```kotlin
     * val totalCalories: StateFlow<Int> = repository.getTotalCalories()
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
     * ```
     */
    fun getTotalCalories(): Flow<Int> {
        return activityDao.getTotalCalories()
    }

    /**
     * GET TOTAL DISTANCE - Sum of all distances
     *
     * @return Flow<Double> - Total distance in km
     */
    fun getTotalDistance(): Flow<Double> {
        return activityDao.getTotalDistance()
    }

    /**
     * GET TOTAL DURATION - Sum of all activity durations
     *
     * @return Flow<Int> - Total duration in minutes
     */
    fun getTotalDuration(): Flow<Int> {
        return activityDao.getTotalDuration()
    }

    /**
     * GET ACTIVITY COUNT - Total number of activities
     *
     * @return Flow<Int> - Count of activities
     */
    fun getActivityCount(): Flow<Int> {
        return activityDao.getActivityCount()
    }

    /**
     * GET CALORIES IN DATE RANGE - Calories for specific period
     *
     * @param startTime - Start timestamp
     * @param endTime - End timestamp
     * @return Flow<Int> - Calories in range
     */
    fun getCaloriesInDateRange(startTime: Long, endTime: Long): Flow<Int> {
        return activityDao.getCaloriesInDateRange(startTime, endTime)
    }

    // ============================================================================
    // WRITE OPERATIONS - Modify data
    // ============================================================================

    /**
     * INSERT ACTIVITY - Add new activity to database
     *
     * SUSPEND FUNCTION:
     * - Must be called from coroutine
     * - Runs on background thread (Room handles this)
     * - Non-blocking
     *
     * DOMAIN → ENTITY CONVERSION:
     * - UI/ViewModel works with Activity (domain model)
     * - Repository converts to ActivityEntity for database
     * - Keeps database implementation details out of UI layer
     *
     * @param activity - Domain model from UI
     * @return Long - Row ID of inserted activity
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * fun addActivity(
     *     type: String,
     *     duration: Int,
     *     calories: Int
     * ) {
     *     viewModelScope.launch {
     *         val activity = Activity(
     *             type = type,
     *             durationMinutes = duration,
     *             caloriesBurned = calories,
     *             timestamp = System.currentTimeMillis()
     *         )
     *
     *         try {
     *             val id = repository.insertActivity(activity)
     *             _message.value = "Activity saved! (ID: $id)"
     *         } catch (e: Exception) {
     *             _error.value = "Failed to save: ${e.message}"
     *         }
     *     }
     * }
     * ```
     *
     * USAGE IN UI:
     * ```kotlin
     * Button(
     *     onClick = {
     *         viewModel.addActivity(
     *             type = "Running",
     *             duration = 30,
     *             calories = 300
     *         )
     *     }
     * ) {
     *     Text("Save Activity")
     * }
     * ```
     */
    suspend fun insertActivity(activity: Activity): Long {
        // Convert domain model to entity
        val entity = ActivityEntity.fromDomain(activity)
        // Insert into database
        return activityDao.insert(entity)
    }

    /**
     * INSERT ALL ACTIVITIES - Batch insert
     *
     * More efficient than inserting one by one
     * Room executes in a single transaction
     *
     * @param activities - List of activities to insert
     *
     * USAGE:
     * ```kotlin
     * // Pre-populate with sample data
     * suspend fun addSampleData() {
     *     val samples = listOf(
     *         Activity(type = "Running", durationMinutes = 30, ...),
     *         Activity(type = "Cycling", durationMinutes = 45, ...),
     *         Activity(type = "Swimming", durationMinutes = 60, ...)
     *     )
     *     repository.insertAllActivities(samples)
     * }
     * ```
     */
    suspend fun insertAllActivities(activities: List<Activity>) {
        val entities = activities.map { ActivityEntity.fromDomain(it) }
        activityDao.insertAll(entities)
    }

    /**
     * UPDATE ACTIVITY - Modify existing activity
     *
     * @param activity - Updated activity (must have existing ID)
     *
     * USAGE:
     * ```kotlin
     * // Edit activity notes
     * fun updateNotes(activityId: Int, newNotes: String) {
     *     viewModelScope.launch {
     *         val activity = repository.getActivityById(activityId).first()
     *         activity?.let {
     *             val updated = it.copy(notes = newNotes)
     *             repository.updateActivity(updated)
     *         }
     *     }
     * }
     * ```
     */
    suspend fun updateActivity(activity: Activity) {
        val entity = ActivityEntity.fromDomain(activity)
        activityDao.update(entity)
    }

    /**
     * DELETE ACTIVITY - Remove activity from database
     *
     * @param activity - Activity to delete
     *
     * USAGE:
     * ```kotlin
     * // Delete button in UI
     * IconButton(
     *     onClick = {
     *         viewModel.deleteActivity(activity)
     *     }
     * ) {
     *     Icon(Icons.Default.Delete, "Delete activity")
     * }
     *
     * // In ViewModel
     * fun deleteActivity(activity: Activity) {
     *     viewModelScope.launch {
     *         repository.deleteActivity(activity)
     *         _message.value = "Activity deleted"
     *     }
     * }
     * ```
     */
    suspend fun deleteActivity(activity: Activity) {
        val entity = ActivityEntity.fromDomain(activity)
        activityDao.delete(entity)
    }

    /**
     * DELETE ACTIVITY BY ID - Delete by ID only
     *
     * @param id - Activity ID
     */
    suspend fun deleteActivityById(id: Int) {
        activityDao.deleteById(id)
    }

    /**
     * DELETE ALL ACTIVITIES - Clear all data
     *
     * DANGEROUS! Use with caution.
     *
     * USAGE:
     * ```kotlin
     * // Settings screen
     * Button(
     *     onClick = {
     *         showDialog = true  // Confirm first!
     *     }
     * ) {
     *     Text("Clear All Activities")
     * }
     *
     * if (showDialog) {
     *     AlertDialog(
     *         onDismissRequest = { showDialog = false },
     *         title = { Text("Clear All Data?") },
     *         text = { Text("This cannot be undone!") },
     *         confirmButton = {
     *             TextButton(
     *                 onClick = {
     *                     viewModel.deleteAllActivities()
     *                     showDialog = false
     *                 }
     *             ) {
     *                 Text("Delete All")
     *             }
     *         }
     *     )
     * }
     * ```
     */
    suspend fun deleteAllActivities() {
        activityDao.deleteAll()
    }

    /**
     * DELETE OLD ACTIVITIES - Auto-cleanup
     *
     * @param timestamp - Delete activities older than this
     *
     * USAGE:
     * ```kotlin
     * // Delete activities older than 90 days
     * suspend fun cleanupOldData() {
     *     val ninetyDaysAgo = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
     *     repository.deleteOldActivities(ninetyDaysAgo)
     * }
     * ```
     */
    suspend fun deleteOldActivities(timestamp: Long) {
        activityDao.deleteOlderThan(timestamp)
    }
}

/**
 * OFFLINE-FIRST WITH API SYNC (Future Enhancement):
 *
 * You could extend this repository to sync with an API:
 *
 * ```kotlin
 * class ActivityRepository(
 *     private val activityDao: ActivityDao,
 *     private val apiService: ActivityApiService,  // Add API
 *     private val networkMonitor: NetworkMonitor   // Check connectivity
 * ) {
 *
 *     // Offline-first: Return local data immediately, refresh from API in background
 *     fun getAllActivities(): Flow<List<Activity>> {
 *         // Launch background sync
 *         viewModelScope.launch {
 *             if (networkMonitor.isConnected) {
 *                 try {
 *                     val remoteActivities = apiService.getActivities()
 *                     // Update local database
 *                     activityDao.insertAll(remoteActivities.map { it.toEntity() })
 *                 } catch (e: Exception) {
 *                     // Ignore network errors, use cached data
 *                 }
 *             }
 *         }
 *
 *         // Return local data (will update automatically when sync completes)
 *         return activityDao.getAllActivities()
 *             .map { it.map { entity -> entity.toDomain() } }
 *     }
 * }
 * ```
 *
 * KEY ARCHITECTURE PRINCIPLES:
 *
 * ✓ Single Responsibility: Repository only handles data operations
 * ✓ Separation of Concerns: UI doesn't know about database/network
 * ✓ Testability: Easy to mock for testing
 * ✓ Maintainability: Changes to data layer don't affect UI
 * ✓ Scalability: Easy to add caching, sync, offline support
 */
