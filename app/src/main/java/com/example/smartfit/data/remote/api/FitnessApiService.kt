package com.example.smartfit.data.remote.api

import com.example.smartfit.data.remote.dto.ExerciseListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * RETROFIT API SERVICE - FitnessApiService
 *
 * This is where you define your API endpoints using annotations.
 * Retrofit turns this interface into working code that makes HTTP requests.
 *
 * HOW RETROFIT WORKS:
 * 1. You define an interface with methods for each API endpoint
 * 2. You annotate methods with HTTP method (@GET, @POST, @PUT, @DELETE)
 * 3. You specify the endpoint path
 * 4. Retrofit generates the implementation that makes the actual HTTP requests
 * 5. You just call the methods, Retrofit handles networking!
 *
 * BASE URL: https://wger.de/api/v2/
 * Configured in AppContainer.kt
 *
 * WGER API DOCUMENTATION:
 * https://wger.de/api/v2/
 * - Free, no API key required
 * - 800+ exercises with images
 * - REST API with JSON responses
 * - Well-documented endpoints
 *
 * KEY CONCEPTS:
 *
 * SUSPEND FUNCTIONS:
 * - Methods marked 'suspend' work with Kotlin coroutines
 * - Can only be called from other suspend functions or coroutines
 * - Non-blocking - doesn't freeze the UI thread
 * - Use viewModelScope.launch { } to call from ViewModels
 *
 * @Query ANNOTATION:
 * - Adds query parameters to the URL
 * - Example: @Query("language") language: Int
 * - Becomes: /exercise/?language=2
 * - Multiple @Query parameters are joined with &
 *
 * RETURN TYPES:
 * - Directly return the data class (Retrofit handles deserialization)
 * - If request fails, Retrofit throws an exception
 * - Catch exceptions in Repository layer for error handling
 */
interface FitnessApiService {

    /**
     * GET EXERCISES - Fetch list of exercises
     *
     * ENDPOINT: GET /api/v2/exercise/
     * FULL URL: https://wger.de/api/v2/exercise/?language=2&limit=20
     *
     * @GET annotation:
     * - "exercise/" is the path (relative to base URL)
     * - Retrofit generates HTTP GET request
     *
     * QUERY PARAMETERS:
     *
     * @param language - Language ID (1=German, 2=English, etc.)
     *                   Filters exercises to specified language
     *                   Default: 2 (English)
     *
     * @param limit - Number of results per page
     *                Default: 20
     *                Max: 100 (API limit)
     *
     * @param offset - Pagination offset
     *                Skip first N results
     *                Used for loading more pages
     *
     * @return ExerciseListResponse - Parsed JSON response
     *
     * EXAMPLE API REQUEST:
     * ```
     * GET https://wger.de/api/v2/exercise/?language=2&limit=20&offset=0
     * ```
     *
     * EXAMPLE RESPONSE: (see WorkoutDto.kt for full structure)
     * ```json
     * {
     *   "count": 842,
     *   "next": "https://wger.de/api/v2/exercise/?language=2&limit=20&offset=20",
     *   "previous": null,
     *   "results": [ { exercise data } ]
     * }
     * ```
     *
     * USAGE IN REPOSITORY:
     * ```kotlin
     * suspend fun getWorkouts(limit: Int = 20): Result<List<Workout>> {
     *     return try {
     *         val response = apiService.getExercises(limit = limit)
     *         val workouts = response.results.map { it.toDomain() }
     *         Result.success(workouts)
     *     } catch (e: IOException) {
     *         // Network error (no internet, timeout, etc.)
     *         Result.failure(NetworkException("No internet connection"))
     *     } catch (e: HttpException) {
     *         // HTTP error (404, 500, etc.)
     *         Result.failure(ApiException("Server error: ${e.code()}"))
     *     } catch (e: Exception) {
     *         // Other errors
     *         Result.failure(e)
     *     }
     * }
     * ```
     */
    @GET("exercise/")
    suspend fun getExercises(
        @Query("language") language: Int = 2,  // Default to English
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ExerciseListResponse

    /**
     * GET EXERCISES BY CATEGORY - Filter by category (Chest, Back, Legs, etc.)
     *
     * ENDPOINT: GET /api/v2/exercise/
     * FULL URL: https://wger.de/api/v2/exercise/?language=2&category=11
     *
     * CATEGORY IDs:
     * - 10: Abs
     * - 8: Arms (Biceps, Triceps)
     * - 12: Back
     * - 14: Calves
     * - 11: Chest
     * - 9: Legs (Quads, Hamstrings)
     * - 13: Shoulders
     *
     * @param categoryId - Category ID to filter by
     * @param language - Language ID (default: English)
     * @param limit - Results per page
     *
     * USAGE:
     * ```kotlin
     * // Get chest exercises
     * val chestExercises = apiService.getExercisesByCategory(categoryId = 11)
     * ```
     */
    @GET("exercise/")
    suspend fun getExercisesByCategory(
        @Query("category") categoryId: Int,
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20
    ): ExerciseListResponse

    /**
     * GET EXERCISES BY MUSCLE - Filter by muscle group
     *
     * ENDPOINT: GET /api/v2/exercise/
     * FULL URL: https://wger.de/api/v2/exercise/?language=2&muscles=4
     *
     * MUSCLE IDs:
     * - 1: Biceps brachii
     * - 2: Anterior deltoid (Front shoulder)
     * - 4: Pectoralis major (Chest)
     * - 3: Latissimus dorsi (Back)
     * - 10: Quadriceps (Front thigh)
     * - 11: Hamstrings (Back thigh)
     * - 8: Triceps brachii
     * - 6: Rectus abdominis (Abs)
     *
     * @param muscleId - Muscle ID to filter by
     * @param language - Language ID
     * @param limit - Results per page
     *
     * USAGE:
     * ```kotlin
     * // Get exercises targeting chest
     * val chestWorkouts = apiService.getExercisesByMuscle(muscleId = 4)
     * ```
     */
    @GET("exercise/")
    suspend fun getExercisesByMuscle(
        @Query("muscles") muscleId: Int,
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20
    ): ExerciseListResponse

    /**
     * GET EXERCISES BY EQUIPMENT - Filter by equipment type
     *
     * ENDPOINT: GET /api/v2/exercise/
     * FULL URL: https://wger.de/api/v2/exercise/?language=2&equipment=7
     *
     * EQUIPMENT IDs:
     * - 7: Bodyweight (no equipment)
     * - 1: Barbell
     * - 3: Dumbbell
     * - 4: Gym mat
     * - 8: Bench
     * - 9: Pull-up bar
     * - 10: Swiss Ball
     *
     * @param equipmentId - Equipment ID to filter by
     * @param language - Language ID
     * @param limit - Results per page
     *
     * USAGE:
     * ```kotlin
     * // Get bodyweight exercises (at-home workouts)
     * val bodyweightWorkouts = apiService.getExercisesByEquipment(equipmentId = 7)
     * ```
     */
    @GET("exercise/")
    suspend fun getExercisesByEquipment(
        @Query("equipment") equipmentId: Int,
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20
    ): ExerciseListResponse

    /**
     * SEARCH EXERCISES - Search by name
     *
     * ENDPOINT: GET /api/v2/exercise/
     * FULL URL: https://wger.de/api/v2/exercise/?language=2&term=bench
     *
     * @param searchTerm - Search query (exercise name)
     * @param language - Language ID
     * @param limit - Results per page
     *
     * USAGE:
     * ```kotlin
     * // Search for "push up" exercises
     * val results = apiService.searchExercises(searchTerm = "push")
     * ```
     */
    @GET("exercise/")
    suspend fun searchExercises(
        @Query("term") searchTerm: String,
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20
    ): ExerciseListResponse

    /**
     * EXAMPLE: More API endpoints you could add
     *
     * // Get nutrition info
     * @GET("ingredient/")
     * suspend fun getNutritionInfo(@Query("name") name: String): NutritionResponse
     *
     * // Get workout plans
     * @GET("workout/")
     * suspend fun getWorkoutPlans(): WorkoutPlanResponse
     *
     * // Get specific exercise details
     * @GET("exercise/{id}/")
     * suspend fun getExerciseDetails(@Path("id") id: Int): ExerciseDto
     */
}

/**
 * ERROR HANDLING STRATEGY:
 *
 * Retrofit can throw these exceptions:
 * - IOException: Network errors (no internet, timeout, DNS failure)
 * - HttpException: HTTP errors (404, 500, 401, etc.)
 * - JsonDecodingException: JSON parsing errors
 *
 * BEST PRACTICE: Catch in Repository, return Result<T>
 *
 * ```kotlin
 * sealed class Result<out T> {
 *     data class Success<T>(val data: T) : Result<T>()
 *     data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
 *     object Loading : Result<Nothing>()
 * }
 *
 * suspend fun getWorkouts(): Result<List<Workout>> {
 *     return try {
 *         val response = api.getExercises()
 *         Result.Success(response.results.map { it.toDomain() })
 *     } catch (e: IOException) {
 *         Result.Error("No internet connection", e)
 *     } catch (e: HttpException) {
 *         Result.Error("Server error: ${e.code()}", e)
 *     } catch (e: Exception) {
 *         Result.Error("Unknown error: ${e.message}", e)
 *     }
 * }
 * ```
 *
 * LOGGING:
 *
 * HTTP logging is configured in AppContainer.kt:
 * ```kotlin
 * val loggingInterceptor = HttpLoggingInterceptor().apply {
 *     level = HttpLoggingInterceptor.Level.BODY
 * }
 * ```
 *
 * This logs:
 * - Request URL, method, headers, body
 * - Response code, headers, body
 * - Request/response time
 *
 * Check Logcat filter for "OkHttp" to see network logs!
 *
 * TESTING:
 *
 * Mock the API in tests:
 * ```kotlin
 * @Test
 * fun `getExercises returns list of workouts`() = runTest {
 *     val mockApi = mockk<FitnessApiService>()
 *     coEvery { mockApi.getExercises() } returns ExerciseListResponse(
 *         count = 1,
 *         results = listOf(mockExerciseDto)
 *     )
 *
 *     val repository = WorkoutRepository(mockApi)
 *     val result = repository.getWorkouts()
 *
 *     assertTrue(result.isSuccess)
 *     assertEquals(1, result.getOrNull()?.size)
 * }
 * ```
 *
 * RATE LIMITING:
 *
 * Wger API doesn't have strict rate limits, but be respectful:
 * - Cache responses when possible
 * - Don't hammer the API with rapid requests
 * - Use pagination (limit/offset) for large datasets
 * - Consider implementing exponential backoff for retries
 */
