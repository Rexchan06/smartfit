package com.example.smartfit.util

import android.util.Log

/**
 * LOGGING UTILITY - Centralized logging for SmartFit
 *
 * WHY A CUSTOM LOGGER?
 * ✓ Consistent log format across the app
 * ✓ Easy to disable logs in production
 * ✓ Can add crash reporting integration (Firebase Crashlytics, etc.)
 * ✓ Filter logs by feature (Network, Database, UI, etc.)
 * ✓ Add timestamps, thread info, etc.
 *
 * LOGGING LEVELS:
 * - VERBOSE (v): Detailed debug info (rarely used)
 * - DEBUG (d): General debug messages
 * - INFO (i): Informational messages
 * - WARN (w): Warnings (recoverable errors)
 * - ERROR (e): Errors (exceptions, failures)
 *
 * WHERE TO ADD LOGS:
 * 1. Network requests (before/after API calls)
 * 2. Database operations (insert, update, delete)
 * 3. Navigation events
 * 4. ViewModel state changes
 * 5. Error conditions
 * 6. Important user actions
 *
 * EXAMPLE USAGE:
 * ```kotlin
 * Logger.d("HomeViewModel", "Loading activities from database")
 * Logger.i("ActivityDao", "Inserted activity with ID: $id")
 * Logger.w("WorkoutRepository", "API call failed, using cached data")
 * Logger.e("MainActivity", "Failed to initialize database", exception)
 * Logger.network("GET", "/api/exercises", "Success: 200")
 * ```
 */
object Logger {

    /**
     * App tag prefix - appears in all logs for easy filtering
     *
     * In Android Studio Logcat, filter by "SmartFit" to see only your app's logs
     */
    private const val TAG_PREFIX = "SmartFit"

    /**
     * Enable/disable logging
     *
     * Set to false in production builds to:
     * - Improve performance
     * - Reduce APK size
     * - Hide sensitive information from logs
     *
     * In production, use BuildConfig.DEBUG:
     * ```kotlin
     * private val isLoggingEnabled = BuildConfig.DEBUG
     * ```
     */
    private const val isLoggingEnabled = true

    /**
     * DEBUG - General debug messages
     *
     * Use for: Flow control, method entry/exit, variable values
     *
     * @param tag - Component name (e.g., "HomeViewModel", "ActivityDao")
     * @param message - Log message
     *
     * EXAMPLE:
     * ```kotlin
     * fun loadActivities() {
     *     Logger.d("HomeViewModel", "loadActivities() called")
     *     // ... code ...
     *     Logger.d("HomeViewModel", "Loaded ${activities.size} activities")
     * }
     * ```
     */
    fun d(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.d("$TAG_PREFIX:$tag", message)
        }
    }

    /**
     * INFO - Informational messages
     *
     * Use for: Important app events, successful operations
     *
     * EXAMPLE:
     * ```kotlin
     * suspend fun insertActivity(activity: Activity) {
     *     val id = dao.insert(activity.toEntity())
     *     Logger.i("ActivityRepository", "Activity saved successfully (ID: $id)")
     * }
     * ```
     */
    fun i(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.i("$TAG_PREFIX:$tag", message)
        }
    }

    /**
     * WARN - Warning messages
     *
     * Use for: Recoverable errors, deprecated usage, potential issues
     *
     * EXAMPLE:
     * ```kotlin
     * fun getWorkouts(): Flow<Result<List<Workout>>> = flow {
     *     try {
     *         val response = api.getExercises()
     *         emit(Result.Success(response))
     *     } catch (e: IOException) {
     *         Logger.w("WorkoutRepository", "API call failed: ${e.message}")
     *         // Return cached data instead
     *         emit(Result.Success(getCachedWorkouts()))
     *     }
     * }
     * ```
     */
    fun w(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.w("$TAG_PREFIX:$tag", message)
        }
    }

    /**
     * ERROR - Error messages
     *
     * Use for: Exceptions, failures, critical errors
     *
     * @param tag - Component name
     * @param message - Error description
     * @param throwable - Exception (optional)
     *
     * EXAMPLE:
     * ```kotlin
     * fun createDatabase(context: Context): SmartFitDatabase {
     *     return try {
     *         SmartFitDatabase.getDatabase(context)
     *     } catch (e: Exception) {
     *         Logger.e("AppContainer", "Failed to create database", e)
     *         throw e
     *     }
     * }
     * ```
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.e("$TAG_PREFIX:$tag", message, throwable)
            } else {
                Log.e("$TAG_PREFIX:$tag", message)
            }
        }
    }

    /**
     * VERBOSE - Very detailed debug info
     *
     * Use for: Deep debugging, tracing execution flow
     * Usually disabled in development too (too noisy)
     */
    fun v(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.v("$TAG_PREFIX:$tag", message)
        }
    }

    // ============================================================================
    // SPECIALIZED LOGGING - For specific operations
    // ============================================================================

    /**
     * LOG NETWORK REQUESTS
     *
     * Logs HTTP requests/responses in a consistent format
     *
     * @param method - HTTP method (GET, POST, etc.)
     * @param endpoint - API endpoint
     * @param result - Result message
     *
     * EXAMPLE:
     * ```kotlin
     * suspend fun getExercises(): ExerciseListResponse {
     *     Logger.network("GET", "/api/v2/exercise", "Starting request")
     *     try {
     *         val response = apiService.getExercises()
     *         Logger.network("GET", "/api/v2/exercise", "Success: ${response.count} results")
     *         return response
     *     } catch (e: HttpException) {
     *         Logger.network("GET", "/api/v2/exercise", "Failed: HTTP ${e.code()}")
     *         throw e
     *     }
     * }
     * ```
     *
     * LOGCAT OUTPUT:
     * SmartFit:Network | GET /api/v2/exercise | Success: 20 results
     */
    fun network(method: String, endpoint: String, result: String) {
        if (isLoggingEnabled) {
            Log.i("$TAG_PREFIX:Network", "$method $endpoint | $result")
        }
    }

    /**
     * LOG DATABASE OPERATIONS
     *
     * Logs Room database operations
     *
     * @param operation - Operation type (INSERT, UPDATE, DELETE, QUERY)
     * @param table - Table name
     * @param details - Operation details
     *
     * EXAMPLE:
     * ```kotlin
     * @Insert
     * suspend fun insert(activity: ActivityEntity): Long {
     *     Logger.database("INSERT", "activities", "Adding: ${activity.type}")
     *     val id = // ... actual insert
     *     Logger.database("INSERT", "activities", "Success: ID=$id")
     *     return id
     * }
     * ```
     *
     * LOGCAT OUTPUT:
     * SmartFit:Database | INSERT into activities | Adding: Running
     * SmartFit:Database | INSERT into activities | Success: ID=42
     */
    fun database(operation: String, table: String, details: String) {
        if (isLoggingEnabled) {
            Log.d("$TAG_PREFIX:Database", "$operation into $table | $details")
        }
    }

    /**
     * LOG NAVIGATION EVENTS
     *
     * Logs screen navigation for debugging flow
     *
     * @param from - Source screen
     * @param to - Destination screen
     * @param params - Navigation parameters (optional)
     *
     * EXAMPLE:
     * ```kotlin
     * navController.navigate("activityDetail/$activityId") {
     *     Logger.navigation("ActivityLog", "ActivityDetail", "id=$activityId")
     * }
     * ```
     *
     * LOGCAT OUTPUT:
     * SmartFit:Navigation | ActivityLog → ActivityDetail | id=42
     */
    fun navigation(from: String, to: String, params: String? = null) {
        if (isLoggingEnabled) {
            val message = if (params != null) {
                "$from → $to | $params"
            } else {
                "$from → $to"
            }
            Log.i("$TAG_PREFIX:Navigation", message)
        }
    }

    /**
     * LOG UI EVENTS
     *
     * Logs user interactions and UI state changes
     *
     * @param screen - Screen name
     * @param event - Event description
     *
     * EXAMPLE:
     * ```kotlin
     * Button(
     *     onClick = {
     *         Logger.ui("AddActivity", "Save button clicked")
     *         viewModel.saveActivity()
     *     }
     * ) {
     *     Text("Save")
     * }
     * ```
     */
    fun ui(screen: String, event: String) {
        if (isLoggingEnabled) {
            Log.d("$TAG_PREFIX:UI", "$screen | $event")
        }
    }
}

/**
 * HOW TO USE IN YOUR CODE:
 *
 * 1. VIEWMODEL:
 * ```kotlin
 * class HomeViewModel(private val repository: ActivityRepository) : ViewModel() {
 *     init {
 *         Logger.d("HomeViewModel", "Initialized")
 *         loadActivities()
 *     }
 *
 *     private fun loadActivities() {
 *         Logger.d("HomeViewModel", "Loading activities")
 *         viewModelScope.launch {
 *             repository.getAllActivities()
 *                 .collect { activities ->
 *                     Logger.i("HomeViewModel", "Received ${activities.size} activities")
 *                     _activities.value = activities
 *                 }
 *         }
 *     }
 * }
 * ```
 *
 * 2. REPOSITORY:
 * ```kotlin
 * suspend fun insertActivity(activity: Activity): Long {
 *     Logger.d("ActivityRepository", "Inserting activity: ${activity.type}")
 *     return try {
 *         val entity = ActivityEntity.fromDomain(activity)
 *         val id = activityDao.insert(entity)
 *         Logger.i("ActivityRepository", "Activity inserted with ID: $id")
 *         id
 *     } catch (e: Exception) {
 *         Logger.e("ActivityRepository", "Failed to insert activity", e)
 *         throw e
 *     }
 * }
 * ```
 *
 * 3. API CALLS:
 * ```kotlin
 * fun getWorkouts(): Flow<Result<List<Workout>>> = flow {
 *     Logger.network("GET", "/exercise", "Request started")
 *     emit(Result.Loading)
 *
 *     try {
 *         val response = apiService.getExercises()
 *         Logger.network("GET", "/exercise", "Success: ${response.count} exercises")
 *         emit(Result.Success(response.results.map { it.toDomain() }))
 *     } catch (e: IOException) {
 *         Logger.e("WorkoutRepository", "Network error", e)
 *         emit(Result.Error("No internet connection"))
 *     }
 * }
 * ```
 *
 * VIEWING LOGS IN ANDROID STUDIO:
 *
 * 1. Open Logcat panel (View → Tool Windows → Logcat)
 * 2. Filter by "SmartFit" to see only your app's logs
 * 3. Use regex filters for specific tags:
 *    - "SmartFit:Network" for network logs
 *    - "SmartFit:Database" for database logs
 *    - "SmartFit:HomeViewModel" for specific ViewModel
 * 4. Set log level filter (Debug, Info, Warn, Error)
 *
 * BEST PRACTICES:
 * ✓ Log at entry/exit of important methods
 * ✓ Log all network requests and responses
 * ✓ Log database operations (insert, update, delete)
 * ✓ Log error conditions with exceptions
 * ✓ Don't log sensitive data (passwords, tokens, etc.)
 * ✓ Use appropriate log levels
 * ✓ Keep messages concise but informative
 * ✓ Disable logging in production builds
 */
