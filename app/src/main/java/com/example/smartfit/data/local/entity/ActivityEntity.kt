package com.example.smartfit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartfit.domain.model.Activity

/**
 * ROOM DATABASE ENTITY - ActivityEntity
 *
 * This represents a table in the SQLite database.
 * Each instance of this class = one row in the "activities" table.
 *
 * WHAT IS ROOM?
 * Room is an abstraction layer over SQLite that provides:
 * ✓ Compile-time verification of SQL queries (catches errors before runtime)
 * ✓ Type-safe database operations
 * ✓ Automatic conversion between Kotlin objects and database rows
 * ✓ Observable queries with Flow (UI updates automatically when data changes)
 * ✓ Less boilerplate than raw SQLite
 *
 * HOW ROOM WORKS:
 * 1. You define Entity classes (tables) with @Entity annotation
 * 2. You define DAO interfaces (queries) with @Dao annotation
 * 3. You define Database class with @Database annotation
 * 4. Room generates all the SQL code at compile time using KSP
 * 5. You just call simple Kotlin functions, Room handles the SQL
 *
 * ANNOTATIONS EXPLAINED:
 *
 * @Entity - Marks this class as a database table
 * - tableName: Name of the table in SQLite (default is class name)
 * - Room creates: CREATE TABLE activities (...)
 *
 * @PrimaryKey - Marks the unique identifier for each row
 * - autoGenerate = true: Room auto-increments this value (1, 2, 3, ...)
 * - You can insert with id = 0, Room will assign the next available ID
 *
 * @ColumnInfo - Customizes column properties (optional)
 * - name: Column name in database (default is field name)
 * - Useful for matching API field names or using SQL-friendly names
 */
@Entity(tableName = "activities")
data class ActivityEntity(
    /**
     * PRIMARY KEY - Unique identifier
     *
     * autoGenerate = true means:
     * - First insert gets id = 1
     * - Second insert gets id = 2
     * - And so on...
     *
     * When inserting a new activity, use id = 0:
     * ```kotlin
     * val newActivity = ActivityEntity(
     *     id = 0,  // Room will replace with auto-generated ID
     *     type = "Running",
     *     ...
     * )
     * ```
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Activity type (Running, Cycling, Swimming, etc.)
     *
     * Room automatically creates a column named "type" of type TEXT
     * SQL: type TEXT NOT NULL
     */
    @ColumnInfo(name = "type")
    val type: String,

    /**
     * Duration in minutes
     *
     * Room maps Kotlin Int to SQLite INTEGER
     * SQL: duration_minutes INTEGER NOT NULL
     */
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    /**
     * Calories burned
     *
     * SQL: calories_burned INTEGER NOT NULL
     */
    @ColumnInfo(name = "calories_burned")
    val caloriesBurned: Int,

    /**
     * Distance in kilometers (nullable for non-distance activities)
     *
     * Room maps Kotlin Double? to SQLite REAL (floating point)
     * Nullable means this column allows NULL values
     * SQL: distance_km REAL (NULL allowed)
     *
     * WHY NULLABLE?
     * Weight training doesn't have distance, but running does
     * Null represents "not applicable" vs 0.0 which means "zero distance"
     */
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double? = null,

    /**
     * Number of steps (nullable)
     *
     * SQL: steps INTEGER (NULL allowed)
     */
    @ColumnInfo(name = "steps")
    val steps: Int? = null,

    /**
     * Unix timestamp (milliseconds since Jan 1, 1970)
     *
     * Room maps Long to SQLite INTEGER
     * We store timestamp as Long for easy sorting and filtering
     * SQL: timestamp INTEGER NOT NULL
     *
     * USAGE:
     * - Current time: System.currentTimeMillis()
     * - Convert to Date: Date(timestamp)
     * - Format for display: SimpleDateFormat("MMM dd, yyyy").format(Date(timestamp))
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    /**
     * Optional notes (nullable)
     *
     * SQL: notes TEXT (NULL allowed)
     */
    @ColumnInfo(name = "notes")
    val notes: String? = null
) {
    /**
     * CONVERSION FUNCTION - Entity to Domain Model
     *
     * WHY CONVERT?
     * The UI doesn't need to know about Room (@Entity, @ColumnInfo, etc.)
     * We convert database entities to domain models for the UI layer
     *
     * DATA FLOW:
     * Database → ActivityEntity → Activity (domain) → UI
     *
     * This follows the Repository Pattern:
     * - Repository fetches ActivityEntity from DAO
     * - Repository converts to Activity domain model
     * - ViewModel receives Activity (no Room dependencies)
     * - UI displays Activity
     *
     * USAGE IN REPOSITORY:
     * ```kotlin
     * fun getAllActivities(): Flow<List<Activity>> {
     *     return activityDao.getAllActivities()
     *         .map { entities ->
     *             entities.map { it.toDomain() }
     *         }
     * }
     * ```
     */
    fun toDomain(): Activity {
        return Activity(
            id = this.id,
            type = this.type,
            durationMinutes = this.durationMinutes,
            caloriesBurned = this.caloriesBurned,
            distanceKm = this.distanceKm,
            steps = this.steps,
            timestamp = this.timestamp,
            notes = this.notes
        )
    }

    companion object {
        /**
         * CONVERSION FUNCTION - Domain Model to Entity
         *
         * Converts from UI-friendly Activity to database ActivityEntity
         *
         * DATA FLOW:
         * UI → Activity (domain) → ActivityEntity → Database
         *
         * USAGE IN REPOSITORY:
         * ```kotlin
         * suspend fun insertActivity(activity: Activity) {
         *     val entity = ActivityEntity.fromDomain(activity)
         *     activityDao.insert(entity)
         * }
         * ```
         */
        fun fromDomain(activity: Activity): ActivityEntity {
            return ActivityEntity(
                id = activity.id,
                type = activity.type,
                durationMinutes = activity.durationMinutes,
                caloriesBurned = activity.caloriesBurned,
                distanceKm = activity.distanceKm,
                steps = activity.steps,
                timestamp = activity.timestamp,
                notes = activity.notes
            )
        }
    }
}

/**
 * EXAMPLE: What Room generates from this Entity
 *
 * SQL TABLE CREATION:
 * ```sql
 * CREATE TABLE activities (
 *     id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *     type TEXT NOT NULL,
 *     duration_minutes INTEGER NOT NULL,
 *     calories_burned INTEGER NOT NULL,
 *     distance_km REAL,
 *     steps INTEGER,
 *     timestamp INTEGER NOT NULL,
 *     notes TEXT
 * );
 * ```
 *
 * EXAMPLE DATA:
 * ```
 * | id | type    | duration_minutes | calories_burned | distance_km | steps | timestamp      | notes           |
 * |----|---------|------------------|-----------------|-------------|-------|----------------|-----------------|
 * | 1  | Running | 30               | 300             | 5.0         | 6500  | 1700000000000  | Morning run     |
 * | 2  | Yoga    | 45               | 150             | NULL        | NULL  | 1700001000000  | Relaxing session|
 * | 3  | Cycling | 60               | 450             | 20.0        | NULL  | 1700002000000  | Bike trail      |
 * ```
 *
 * COMMON OPERATIONS:
 *
 * Insert:
 * ```kotlin
 * val activity = ActivityEntity(
 *     type = "Running",
 *     durationMinutes = 30,
 *     caloriesBurned = 300,
 *     distanceKm = 5.0,
 *     steps = 6500,
 *     timestamp = System.currentTimeMillis(),
 *     notes = "Morning run"
 * )
 * activityDao.insert(activity)  // Room auto-assigns ID
 * ```
 *
 * Query:
 * ```kotlin
 * // Get all activities
 * val activities: Flow<List<ActivityEntity>> = activityDao.getAllActivities()
 *
 * // Get activities by type
 * val runs: List<ActivityEntity> = activityDao.getActivitiesByType("Running")
 *
 * // Get total calories burned
 * val totalCalories: Int = activityDao.getTotalCalories()
 * ```
 *
 * Update:
 * ```kotlin
 * val updated = activity.copy(notes = "Updated note")
 * activityDao.update(updated)
 * ```
 *
 * Delete:
 * ```kotlin
 * activityDao.delete(activity)
 * ```
 */
