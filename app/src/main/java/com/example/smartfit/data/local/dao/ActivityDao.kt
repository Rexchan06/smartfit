package com.example.smartfit.data.local.dao

import androidx.room.*
import com.example.smartfit.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DATA ACCESS OBJECT (DAO) - ActivityDao
 *
 * This is where you define all database operations (queries, inserts, updates, deletes).
 * Think of it as your database API - you define WHAT you want, Room handles HOW.
 *
 * WHAT IS A DAO?
 * DAO = Data Access Object
 * It's an interface that defines methods for accessing the database.
 * Room generates the implementation at compile time using KSP.
 *
 * HOW IT WORKS:
 * 1. You write an interface with @Dao annotation
 * 2. You add methods with annotations (@Query, @Insert, @Update, @Delete)
 * 3. Room generates the actual SQL and implementation code
 * 4. You just call the methods like any other Kotlin function
 *
 * KEY CONCEPTS:
 *
 * SUSPEND FUNCTIONS:
 * - Functions marked with 'suspend' can be called from coroutines
 * - Room executes them on a background thread automatically
 * - NEVER blocks the UI thread (prevents app freezing)
 * - Use for: insert, update, delete, single-shot queries
 *
 * FLOW RETURN TYPE:
 * - Flow is a stream of data that emits values over time
 * - Room automatically updates the Flow when database changes
 * - UI observes Flow and updates automatically when data changes
 * - This is REACTIVE programming - UI reacts to data changes
 * - Use for: queries that need live updates (getAllActivities, stats, etc.)
 *
 * @Query ANNOTATION:
 * - Takes a SQL query string
 * - Room verifies it at COMPILE TIME (catches errors before running)
 * - Can use method parameters as query parameters with :paramName
 * - Returns are automatically converted from database to Kotlin objects
 */
@Dao
interface ActivityDao {

    // ============================================================================
    // CREATE - Insert operations
    // ============================================================================

    /**
     * INSERT - Add a single activity to database
     *
     * @Insert annotation tells Room to generate INSERT SQL
     * OnConflictStrategy.REPLACE: If an activity with same ID exists, replace it
     *
     * SUSPEND: Runs on background thread, doesn't block UI
     *
     * RETURNS: Long - the row ID of inserted item (or -1 if failed)
     *
     * GENERATED SQL:
     * INSERT OR REPLACE INTO activities VALUES (?, ?, ?, ?, ?, ?, ?, ?)
     *
     * USAGE:
     * ```kotlin
     * viewModelScope.launch {  // Run in coroutine
     *     val activity = ActivityEntity(
     *         type = "Running",
     *         durationMinutes = 30,
     *         caloriesBurned = 300,
     *         timestamp = System.currentTimeMillis()
     *     )
     *     val id = activityDao.insert(activity)
     *     println("Inserted activity with ID: $id")
     * }
     * ```
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    /**
     * INSERT MULTIPLE - Add a list of activities
     *
     * More efficient than calling insert() multiple times
     * Room batches them into a single transaction
     *
     * USAGE:
     * ```kotlin
     * val activities = listOf(activity1, activity2, activity3)
     * activityDao.insertAll(activities)
     * ```
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)

    // ============================================================================
    // READ - Query operations
    // ============================================================================

    /**
     * GET ALL ACTIVITIES - Returns Flow of all activities, sorted by newest first
     *
     * FLOW<List<ActivityEntity>>:
     * - Emits the current list of activities immediately
     * - Automatically emits a new list whenever database changes
     * - UI observes this Flow and updates automatically
     * - No need to manually refresh!
     *
     * ORDER BY timestamp DESC:
     * - DESC = Descending (newest first)
     * - ASC = Ascending (oldest first)
     *
     * SQL GENERATED:
     * SELECT * FROM activities ORDER BY timestamp DESC
     *
     * USAGE IN REPOSITORY:
     * ```kotlin
     * fun getAllActivities(): Flow<List<Activity>> {
     *     return activityDao.getAllActivities()
     *         .map { entities -> entities.map { it.toDomain() } }
     * }
     * ```
     *
     * USAGE IN VIEWMODEL:
     * ```kotlin
     * val activities: StateFlow<List<Activity>> = repository
     *     .getAllActivities()
     *     .stateIn(
     *         scope = viewModelScope,
     *         started = SharingStarted.WhileSubscribed(5000),
     *         initialValue = emptyList()
     *     )
     * ```
     *
     * USAGE IN UI:
     * ```kotlin
     * @Composable
     * fun ActivityListScreen(viewModel: ActivityViewModel) {
     *     val activities by viewModel.activities.collectAsState()
     *
     *     LazyColumn {
     *         items(activities) { activity ->
     *             ActivityCard(activity)
     *         }
     *     }
     * }
     * // When you insert/delete an activity, this list automatically updates!
     * ```
     */
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    /**
     * GET ACTIVITY BY ID - Single activity lookup
     *
     * :id is a query parameter - Room replaces it with the method parameter value
     *
     * FLOW<ActivityEntity?>:
     * - Returns null if not found
     * - Updates automatically if the activity changes
     *
     * SQL: SELECT * FROM activities WHERE id = ?
     *
     * USAGE:
     * ```kotlin
     * @Composable
     * fun ActivityDetailScreen(activityId: Int, dao: ActivityDao) {
     *     val activity by dao.getActivityById(activityId).collectAsState(initial = null)
     *
     *     activity?.let {
     *         // Display activity details
     *         Text(it.type)
     *         Text("${it.durationMinutes} minutes")
     *     } ?: Text("Loading...")
     * }
     * ```
     */
    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivityById(id: Int): Flow<ActivityEntity?>

    /**
     * GET ACTIVITIES BY TYPE - Filter by activity type (Running, Cycling, etc.)
     *
     * SUSPEND + List (not Flow):
     * - Single-shot query that returns once
     * - Use when you don't need live updates
     * - More efficient for one-time operations
     *
     * SQL: SELECT * FROM activities WHERE type = ? ORDER BY timestamp DESC
     */
    @Query("SELECT * FROM activities WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getActivitiesByType(type: String): List<ActivityEntity>

    /**
     * GET ACTIVITIES BY DATE RANGE - Filter by timestamp
     *
     * Used for: "Show this week's activities", "Monthly summary", etc.
     *
     * BETWEEN operator: inclusive range [startTime, endTime]
     *
     * SQL: SELECT * FROM activities WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC
     *
     * USAGE:
     * ```kotlin
     * // Get this week's activities
     * val weekStart = Calendar.getInstance().apply {
     *     set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
     *     set(Calendar.HOUR_OF_DAY, 0)
     * }.timeInMillis
     * val weekEnd = System.currentTimeMillis()
     *
     * val thisWeek = dao.getActivitiesInDateRange(weekStart, weekEnd)
     * ```
     */
    @Query("SELECT * FROM activities WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getActivitiesInDateRange(startTime: Long, endTime: Long): Flow<List<ActivityEntity>>

    // ============================================================================
    // STATISTICS QUERIES - Aggregate data
    // ============================================================================

    /**
     * GET TOTAL CALORIES - Sum of all calories burned
     *
     * COALESCE(SUM(...), 0):
     * - SUM returns null if table is empty
     * - COALESCE returns 0 instead of null
     * - Prevents crashes and makes the return type non-nullable Int
     *
     * SQL: SELECT COALESCE(SUM(calories_burned), 0) FROM activities
     *
     * FLOW<Int>: Updates automatically when activities are added/removed
     */
    @Query("SELECT COALESCE(SUM(calories_burned), 0) FROM activities")
    fun getTotalCalories(): Flow<Int>

    /**
     * GET TOTAL DISTANCE - Sum of all distances
     *
     * WHERE distance_km IS NOT NULL:
     * - Only includes activities that have distance (excludes weight training, yoga, etc.)
     *
     * SQL: SELECT COALESCE(SUM(distance_km), 0) FROM activities WHERE distance_km IS NOT NULL
     */
    @Query("SELECT COALESCE(SUM(distance_km), 0) FROM activities WHERE distance_km IS NOT NULL")
    fun getTotalDistance(): Flow<Double>

    /**
     * GET TOTAL DURATION - Sum of all activity durations
     *
     * SQL: SELECT COALESCE(SUM(duration_minutes), 0) FROM activities
     */
    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM activities")
    fun getTotalDuration(): Flow<Int>

    /**
     * GET ACTIVITY COUNT - Total number of activities logged
     *
     * COUNT(*) counts all rows
     *
     * SQL: SELECT COUNT(*) FROM activities
     */
    @Query("SELECT COUNT(*) FROM activities")
    fun getActivityCount(): Flow<Int>

    /**
     * GET CALORIES FOR DATE RANGE - Statistics for specific period
     *
     * Useful for: "Calories burned this week", "Monthly calorie goal progress"
     *
     * SQL: SELECT COALESCE(SUM(calories_burned), 0) FROM activities
     *      WHERE timestamp BETWEEN ? AND ?
     */
    @Query("SELECT COALESCE(SUM(calories_burned), 0) FROM activities WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getCaloriesInDateRange(startTime: Long, endTime: Long): Flow<Int>

    /**
     * GET RECENT ACTIVITIES - Last N activities
     *
     * LIMIT clause restricts number of results
     *
     * SQL: SELECT * FROM activities ORDER BY timestamp DESC LIMIT ?
     *
     * USAGE:
     * ```kotlin
     * // Show last 10 activities on home screen
     * val recentActivities = dao.getRecentActivities(10)
     * ```
     */
    @Query("SELECT * FROM activities ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int): Flow<List<ActivityEntity>>

    // ============================================================================
    // UPDATE - Modify existing data
    // ============================================================================

    /**
     * UPDATE - Modify an existing activity
     *
     * @Update annotation tells Room to generate UPDATE SQL
     * Matches by primary key (id)
     *
     * GENERATED SQL:
     * UPDATE activities SET type=?, duration_minutes=?, ... WHERE id=?
     *
     * USAGE:
     * ```kotlin
     * // Get activity, modify it, update it
     * val activity = dao.getActivityById(1)
     * val updated = activity.copy(notes = "Updated notes")
     * dao.update(updated)
     * ```
     */
    @Update
    suspend fun update(activity: ActivityEntity)

    /**
     * UPDATE MULTIPLE - Update a list of activities
     */
    @Update
    suspend fun updateAll(activities: List<ActivityEntity>)

    // ============================================================================
    // DELETE - Remove data
    // ============================================================================

    /**
     * DELETE - Remove a specific activity
     *
     * @Delete annotation tells Room to generate DELETE SQL
     * Matches by primary key (id)
     *
     * GENERATED SQL:
     * DELETE FROM activities WHERE id=?
     *
     * USAGE:
     * ```kotlin
     * dao.delete(activity)  // Activity is removed from database
     * // Any Flow observing activities automatically updates!
     * ```
     */
    @Delete
    suspend fun delete(activity: ActivityEntity)

    /**
     * DELETE BY ID - Remove activity by its ID
     *
     * Sometimes you only have the ID, not the full object
     *
     * SQL: DELETE FROM activities WHERE id = ?
     */
    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * DELETE ALL - Clear all activities
     *
     * DANGEROUS! No confirmation. Use carefully.
     *
     * SQL: DELETE FROM activities
     *
     * USAGE:
     * ```kotlin
     * // Reset button in settings
     * Button(onClick = {
     *     viewModelScope.launch {
     *         dao.deleteAll()
     *     }
     * }) {
     *     Text("Clear All Activities")
     * }
     * ```
     */
    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    /**
     * DELETE OLD ACTIVITIES - Remove activities older than timestamp
     *
     * Useful for: Auto-cleanup, "Keep only last 3 months of data"
     *
     * SQL: DELETE FROM activities WHERE timestamp < ?
     *
     * USAGE:
     * ```kotlin
     * // Delete activities older than 90 days
     * val ninetyDaysAgo = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000)
     * dao.deleteOlderThan(ninetyDaysAgo)
     * ```
     */
    @Query("DELETE FROM activities WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

/**
 * KEY TAKEAWAYS:
 *
 * 1. SUSPEND for one-time operations (insert, update, delete)
 * 2. FLOW for observable queries (UI automatically updates)
 * 3. Room verifies SQL at COMPILE TIME (catches errors early)
 * 4. No need to write SQL boilerplate - Room generates it
 * 5. Thread-safe by default - Room handles threading
 *
 * TESTING DAOs:
 * ```kotlin
 * @Test
 * fun testInsertAndRead() = runTest {
 *     val activity = ActivityEntity(
 *         type = "Running",
 *         durationMinutes = 30,
 *         caloriesBurned = 300,
 *         timestamp = System.currentTimeMillis()
 *     )
 *
 *     // Insert
 *     dao.insert(activity)
 *
 *     // Read
 *     val activities = dao.getAllActivities().first()
 *     assertEquals(1, activities.size)
 *     assertEquals("Running", activities[0].type)
 * }
 * ```
 */
