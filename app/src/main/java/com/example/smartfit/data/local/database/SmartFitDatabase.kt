package com.example.smartfit.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartfit.data.local.dao.ActivityDao
import com.example.smartfit.data.local.entity.ActivityEntity

/**
 * ROOM DATABASE - SmartFitDatabase
 *
 * This is the main database class that brings together all Entities and DAOs.
 * It's the gateway to access your Room database.
 *
 * @Database ANNOTATION EXPLAINED:
 *
 * entities = [ActivityEntity::class]:
 * - List all Entity classes (tables) in your database
 * - Room creates tables for each Entity
 * - If you add more entities (UserEntity, WorkoutPlanEntity, etc.), add them here
 *
 * version = 1:
 * - Database schema version number
 * - Start with 1, increment when you change schema (add columns, tables, etc.)
 * - Room uses this to detect when migrations are needed
 *
 * exportSchema = false:
 * - Set to true in production to export schema for version control
 * - Set to false during development to avoid extra files
 * - When true, Room saves schema JSON for each version (useful for migrations)
 *
 * HOW ROOM DATABASE WORKS:
 *
 * 1. You define Entities (@Entity) - the tables
 * 2. You define DAOs (@Dao) - the queries
 * 3. You define Database (@Database) - connects everything
 * 4. Room generates all the implementation code using KSP
 * 5. You get a database instance and call DAO methods
 *
 * SINGLETON PATTERN:
 * Only ONE instance of the database should exist (prevents corruption, resource waste)
 * We use companion object + @Volatile + synchronized to ensure thread-safe singleton
 */
@Database(
    entities = [ActivityEntity::class],  // Add all entities here
    version = 1,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {

    /**
     * DAO ACCESSORS - Define abstract functions for each DAO
     *
     * Room generates the implementation of these functions
     * They return DAO instances that you can use to query the database
     *
     * USAGE:
     * ```kotlin
     * val db = SmartFitDatabase.getDatabase(context)
     * val activities = db.activityDao().getAllActivities()
     * ```
     *
     * If you add more DAOs (UserDao, WorkoutPlanDao, etc.), add them here:
     * ```kotlin
     * abstract fun userDao(): UserDao
     * abstract fun workoutPlanDao(): WorkoutPlanDao
     * ```
     */
    abstract fun activityDao(): ActivityDao

    companion object {
        /**
         * VOLATILE ensures that INSTANCE is always up-to-date across all threads
         *
         * Without volatile, one thread might cache an old value of INSTANCE
         * and not see the new instance created by another thread
         */
        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        /**
         * GET DATABASE - Thread-safe singleton instance
         *
         * This is called from AppContainer to get the database instance
         *
         * HOW IT WORKS:
         * 1. Check if instance already exists → return it (fast path)
         * 2. If not, synchronize to prevent multiple threads creating instance
         * 3. Double-check inside synchronized block (another thread might have created it)
         * 4. Create new instance with Room.databaseBuilder()
         * 5. Store and return the instance
         *
         * THREAD SAFETY:
         * - Multiple threads can call this simultaneously
         * - Only ONE thread can execute the synchronized block at a time
         * - Ensures only ONE database instance is ever created
         *
         * WHY DOUBLE-CHECK?
         * ```kotlin
         * // Thread A and B both reach here when INSTANCE is null
         * INSTANCE ?: synchronized(this) {
         *     // Thread A enters first, creates instance
         *     // Thread B waits...
         *     // When B enters, instance already exists (from A)
         *     // Without double-check, B would create a second instance!
         *     INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
         * }
         * ```
         *
         * PARAMETERS:
         * @param context - Application context (never use Activity context!)
         *                  Activity context can leak memory
         *
         * @return SmartFitDatabase - The singleton database instance
         */
        fun getDatabase(context: Context): SmartFitDatabase {
            // Fast path: if instance exists, return it immediately
            return INSTANCE ?: synchronized(this) {
                // Slow path: create instance if it doesn't exist
                // Double-check inside synchronized block
                val instance = INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
                instance
            }
        }

        /**
         * BUILD DATABASE - Creates the Room database instance
         *
         * ROOM.DATABASEBUILDER PARAMETERS:
         *
         * context.applicationContext:
         * - Use application context (not activity context)
         * - Prevents memory leaks
         *
         * SmartFitDatabase::class.java:
         * - The database class to instantiate
         *
         * "smartfit_database":
         * - The filename of the SQLite database file
         * - Stored in /data/data/com.example.smartfit/databases/smartfit_database
         *
         * BUILDER OPTIONS (commented out, but useful to know):
         *
         * .fallbackToDestructiveMigration()
         * - If schema changes and no migration provided, delete and recreate database
         * - LOSES ALL DATA! Only use during development
         * - In production, provide proper migrations
         *
         * .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         * - Add migration strategies for schema changes
         * - Preserves user data when upgrading
         *
         * .addCallback(roomCallback)
         * - Execute code when database is created or opened
         * - Useful for pre-populating data
         *
         * EXAMPLE WITH PRE-POPULATION:
         * ```kotlin
         * private val roomCallback = object : RoomDatabase.Callback() {
         *     override fun onCreate(db: SupportSQLiteDatabase) {
         *         super.onCreate(db)
         *         // Pre-populate database with sample data
         *         CoroutineScope(Dispatchers.IO).launch {
         *             val dao = INSTANCE?.activityDao()
         *             dao?.insert(sampleActivity1)
         *             dao?.insert(sampleActivity2)
         *         }
         *     }
         * }
         * ```
         */
        private fun buildDatabase(context: Context): SmartFitDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SmartFitDatabase::class.java,
                "smartfit_database"
            )
                // DEVELOPMENT ONLY: Uncomment to allow destructive migration
                // WARNING: This deletes all data on schema changes!
                // .fallbackToDestructiveMigration()

                // PRODUCTION: Add proper migrations when changing schema
                // Example:
                // .addMigrations(MIGRATION_1_2)

                .build()
        }

        /**
         * EXAMPLE MIGRATION (for future reference)
         *
         * When you change the database schema (add column, table, etc.),
         * you need a migration to preserve user data.
         *
         * Example: Adding a "heart_rate" column to activities table
         * ```kotlin
         * val MIGRATION_1_2 = object : Migration(1, 2) {
         *     override fun migrate(database: SupportSQLiteDatabase) {
         *         // SQL to alter table
         *         database.execSQL(
         *             "ALTER TABLE activities ADD COLUMN heart_rate INTEGER"
         *         )
         *     }
         * }
         *
         * // Then in buildDatabase():
         * Room.databaseBuilder(...)
         *     .addMigrations(MIGRATION_1_2)
         *     .build()
         * ```
         *
         * MIGRATION CHECKLIST:
         * 1. Increment version number in @Database annotation
         * 2. Create Migration object with old and new version numbers
         * 3. Write SQL to transform schema from old to new
         * 4. Add migration to databaseBuilder
         * 5. Test thoroughly! Migrations can corrupt data if wrong
         */
    }
}

/**
 * COMPLETE ROOM SETUP SUMMARY:
 *
 * 1. ENTITY (ActivityEntity.kt):
 *    - Defines the table structure
 *    - @Entity, @PrimaryKey, @ColumnInfo annotations
 *    - Represents a row in the database
 *
 * 2. DAO (ActivityDao.kt):
 *    - Defines database operations
 *    - @Dao, @Query, @Insert, @Update, @Delete annotations
 *    - Interface that Room implements
 *
 * 3. DATABASE (SmartFitDatabase.kt - this file):
 *    - Brings together Entities and DAOs
 *    - @Database annotation
 *    - Singleton pattern for single instance
 *    - Provides DAO accessors
 *
 * 4. USAGE (in AppContainer):
 *    ```kotlin
 *    val database: SmartFitDatabase by lazy {
 *        SmartFitDatabase.getDatabase(context)
 *    }
 *
 *    val activityRepository: ActivityRepository by lazy {
 *        ActivityRepository(database.activityDao())
 *    }
 *    ```
 *
 * 5. USAGE (in ViewModel):
 *    ```kotlin
 *    class ActivityViewModel(
 *        private val repository: ActivityRepository
 *    ) : ViewModel() {
 *
 *        val activities = repository.getAllActivities()
 *            .stateIn(
 *                scope = viewModelScope,
 *                started = SharingStarted.WhileSubscribed(5000),
 *                initialValue = emptyList()
 *            )
 *
 *        fun addActivity(activity: Activity) {
 *            viewModelScope.launch {
 *                repository.insertActivity(activity)
 *            }
 *        }
 *    }
 *    ```
 *
 * 6. USAGE (in UI):
 *    ```kotlin
 *    @Composable
 *    fun ActivityListScreen(viewModel: ActivityViewModel) {
 *        val activities by viewModel.activities.collectAsState()
 *
 *        LazyColumn {
 *            items(activities) { activity ->
 *                ActivityCard(activity = activity)
 *            }
 *        }
 *    }
 *    ```
 *
 * DATA FLOW:
 * UI → ViewModel → Repository → DAO → Database (SQLite)
 * Database → DAO (Flow) → Repository → ViewModel (StateFlow) → UI (State)
 *
 * KEY BENEFITS:
 * ✓ Type-safe database operations
 * ✓ Compile-time SQL verification
 * ✓ Automatic threading (no UI blocking)
 * ✓ Reactive with Flow (UI auto-updates)
 * ✓ No boilerplate SQL code
 * ✓ Easy to test with in-memory database
 */
