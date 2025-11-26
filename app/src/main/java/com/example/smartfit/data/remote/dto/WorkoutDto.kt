package com.example.smartfit.data.remote.dto

import com.example.smartfit.domain.model.DifficultyLevel
import com.example.smartfit.domain.model.Workout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DATA TRANSFER OBJECT (DTO) - API Response Models
 *
 * DTOs represent the structure of data received from the API (JSON).
 * They're separate from domain models because:
 * ✓ API structure might not match UI needs
 * ✓ API might change without breaking domain model
 * ✓ Can have different field names (@SerialName mapping)
 * ✓ May contain unnecessary fields we don't need
 *
 * KOTLIN SERIALIZATION:
 * @Serializable tells Kotlin Serialization how to convert JSON ↔ Kotlin objects
 * @SerialName maps JSON field names to Kotlin property names
 *
 * EXAMPLE API RESPONSE FROM WGER:
 * ```json
 * {
 *   "count": 100,
 *   "next": "https://wger.de/api/v2/exercise/?page=2",
 *   "results": [
 *     {
 *       "id": 345,
 *       "name": "Bench press",
 *       "description": "Lie on bench, lower bar to chest...",
 *       "category": {
 *         "id": 11,
 *         "name": "Chest"
 *       },
 *       "muscles": [
 *         { "id": 4, "name": "Pectoralis major", "name_en": "Pectorals" }
 *       ],
 *       "equipment": [
 *         { "id": 1, "name": "Barbell" }
 *       ],
 *       "images": [
 *         { "id": 123, "image": "https://wger.de/media/image.png" }
 *       ]
 *     }
 *   ]
 * }
 * ```
 */

/**
 * EXERCISE LIST RESPONSE - Top-level API response
 *
 * The Wger API returns paginated results with this structure
 */
@Serializable
data class ExerciseListResponse(
    /**
     * Total number of exercises available
     */
    @SerialName("count")
    val count: Int,

    /**
     * URL for next page of results (null if no more pages)
     */
    @SerialName("next")
    val next: String? = null,

    /**
     * URL for previous page (null if first page)
     */
    @SerialName("previous")
    val previous: String? = null,

    /**
     * Array of exercise objects
     */
    @SerialName("results")
    val results: List<ExerciseDto>
)

/**
 * EXERCISE DTO - Individual exercise data from API
 *
 * Maps directly to JSON structure from Wger API
 */
@Serializable
data class ExerciseDto(
    /**
     * Unique exercise ID from API
     */
    @SerialName("id")
    val id: Int,

    /**
     * Exercise name
     * Example: "Bench press", "Pull-ups", "Squats"
     */
    @SerialName("name")
    val name: String,

    /**
     * HTML description of how to perform the exercise
     * Note: Contains HTML tags like <p>, <ol>, <li>
     * You'll need to strip HTML or render it properly in UI
     */
    @SerialName("description")
    val description: String? = null,

    /**
     * Category object containing category info
     */
    @SerialName("category")
    val category: CategoryDto? = null,

    /**
     * List of muscles targeted by this exercise
     */
    @SerialName("muscles")
    val muscles: List<MuscleDto> = emptyList(),

    /**
     * Secondary muscles worked
     */
    @SerialName("muscles_secondary")
    val musclesSecondary: List<MuscleDto> = emptyList(),

    /**
     * Equipment required for this exercise
     */
    @SerialName("equipment")
    val equipment: List<EquipmentDto> = emptyList(),

    /**
     * Images showing the exercise
     */
    @SerialName("images")
    val images: List<ImageDto> = emptyList(),

    /**
     * Language code (e.g., "en", "de")
     */
    @SerialName("language")
    val language: Int? = null
) {
    /**
     * CONVERSION TO DOMAIN MODEL
     *
     * Transforms API DTO → Domain Model
     * This is where you clean up the data for UI consumption
     *
     * WHY CONVERT?
     * - UI doesn't need nested objects (category.name → just categoryName)
     * - Strip HTML from description
     * - Flatten equipment/muscle lists
     * - Map difficulty levels
     * - Handle null/missing fields gracefully
     *
     * USAGE IN REPOSITORY:
     * ```kotlin
     * suspend fun getWorkouts(): List<Workout> {
     *     val response = api.getExercises()
     *     return response.results.map { it.toDomain() }
     * }
     * ```
     */
    fun toDomain(): Workout {
        return Workout(
            id = this.id,
            name = this.name,
            // Strip HTML tags from description
            description = this.description?.replace(Regex("<[^>]*>"), "")?.trim() ?: "No description available",
            category = this.category?.name ?: "General",
            muscles = this.muscles.map { it.nameEn ?: it.name },
            equipment = this.equipment.firstOrNull()?.name ?: "",
            // Wger doesn't provide difficulty, so we'll default to BEGINNER
            // You could infer difficulty based on equipment or muscle groups
            difficulty = inferDifficulty(),
            imageUrl = this.images.firstOrNull()?.image
        )
    }

    /**
     * INFER DIFFICULTY LEVEL
     *
     * Wger API doesn't provide difficulty, so we make educated guesses:
     * - Bodyweight exercises → BEGINNER
     * - Simple equipment → INTERMEDIATE
     * - Complex equipment or many muscle groups → ADVANCED
     *
     * This is a simplified heuristic. In a production app, you might:
     * - Maintain your own difficulty database
     * - Use machine learning to classify
     * - Allow users to rate difficulty
     */
    private fun inferDifficulty(): DifficultyLevel {
        return when {
            // No equipment = bodyweight = beginner friendly
            equipment.isEmpty() -> DifficultyLevel.BEGINNER

            // Barbells and complex movements = advanced
            equipment.any { it.name.contains("barbell", ignoreCase = true) } -> DifficultyLevel.ADVANCED

            // Multiple muscle groups = more complex
            muscles.size + musclesSecondary.size >= 3 -> DifficultyLevel.INTERMEDIATE

            // Default to beginner
            else -> DifficultyLevel.BEGINNER
        }
    }
}

/**
 * CATEGORY DTO - Exercise category data
 */
@Serializable
data class CategoryDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String
)

/**
 * MUSCLE DTO - Muscle group data
 */
@Serializable
data class MuscleDto(
    @SerialName("id")
    val id: Int,

    /**
     * Muscle name in original language
     */
    @SerialName("name")
    val name: String,

    /**
     * Muscle name in English (preferred for consistency)
     */
    @SerialName("name_en")
    val nameEn: String? = null
)

/**
 * EQUIPMENT DTO - Equipment data
 */
@Serializable
data class EquipmentDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String
)

/**
 * IMAGE DTO - Exercise image data
 */
@Serializable
data class ImageDto(
    @SerialName("id")
    val id: Int,

    /**
     * Full URL to the image
     * Example: "https://wger.de/media/exercise-images/123/image.png"
     *
     * Use with Coil in Compose:
     * ```kotlin
     * AsyncImage(
     *     model = imageUrl,
     *     contentDescription = "Exercise demonstration"
     * )
     * ```
     */
    @SerialName("image")
    val image: String
)

/**
 * EXAMPLE USAGE:
 *
 * 1. IN RETROFIT SERVICE:
 * ```kotlin
 * @GET("exercise/")
 * suspend fun getExercises(
 *     @Query("language") language: Int = 2,  // 2 = English
 *     @Query("limit") limit: Int = 20
 * ): ExerciseListResponse
 * ```
 *
 * 2. IN REPOSITORY:
 * ```kotlin
 * class WorkoutRepository(private val api: FitnessApiService) {
 *
 *     suspend fun getWorkouts(): Result<List<Workout>> {
 *         return try {
 *             val response = api.getExercises()
 *             val workouts = response.results.map { it.toDomain() }
 *             Result.success(workouts)
 *         } catch (e: Exception) {
 *             Result.failure(e)
 *         }
 *     }
 * }
 * ```
 *
 * 3. IN VIEWMODEL:
 * ```kotlin
 * viewModelScope.launch {
 *     _isLoading.value = true
 *     when (val result = repository.getWorkouts()) {
 *         is Result.Success -> {
 *             _workouts.value = result.data
 *         }
 *         is Result.Failure -> {
 *             _error.value = result.exception.message
 *         }
 *     }
 *     _isLoading.value = false
 * }
 * ```
 *
 * KEY POINTS:
 * ✓ DTOs mirror API JSON structure
 * ✓ @Serializable enables JSON parsing
 * ✓ @SerialName maps JSON fields to Kotlin properties
 * ✓ toDomain() converts DTO → Domain Model
 * ✓ Nullable fields with defaults handle missing data gracefully
 * ✓ Separation of concerns: API structure ≠ App structure
 */
