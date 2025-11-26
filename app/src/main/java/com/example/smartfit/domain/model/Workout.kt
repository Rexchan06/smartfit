package com.example.smartfit.domain.model

/**
 * DOMAIN MODEL - Workout
 *
 * Represents a workout/exercise suggestion from the API.
 * Used to show users exercise ideas, instructions, and demonstrations.
 *
 * This is different from Activity:
 * - Activity: User's completed workouts (stored in Room database)
 * - Workout: Exercise suggestions from API (cached temporarily)
 *
 * DATA SOURCE: Wger Workout Manager API (wger.de/api/v2/)
 * - Free, no API key required
 * - 800+ exercises with images and descriptions
 * - Categorized by muscle group, equipment, difficulty
 */
data class Workout(
    /**
     * Unique ID from the API
     */
    val id: Int,

    /**
     * Exercise name
     * Example: "Bench Press", "Pull-ups", "Squats", "Plank"
     */
    val name: String,

    /**
     * Detailed description of how to perform the exercise
     * Usually includes step-by-step instructions
     */
    val description: String,

    /**
     * Category/type of exercise
     * Example: "Arms", "Legs", "Chest", "Back", "Core", "Cardio"
     */
    val category: String,

    /**
     * Muscle groups targeted
     * Example: ["Biceps", "Forearms"], ["Quadriceps", "Glutes"]
     */
    val muscles: List<String> = emptyList(),

    /**
     * Equipment needed
     * Example: "Barbell", "Dumbbells", "Bodyweight", "Resistance Bands"
     * Empty string if no equipment needed
     */
    val equipment: String = "",

    /**
     * Difficulty level
     * Example: "Beginner", "Intermediate", "Advanced"
     */
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,

    /**
     * Image URL showing the exercise
     * Use with Coil to load the image:
     * AsyncImage(model = workout.imageUrl, contentDescription = workout.name)
     */
    val imageUrl: String? = null,

    /**
     * YouTube video ID or URL (if available)
     * Can be used to show video demonstrations
     */
    val videoUrl: String? = null
) {
    /**
     * COMPUTED PROPERTY - Check if equipment-free
     * Useful for filtering "at-home" workouts
     */
    val isBodyweightExercise: Boolean
        get() = equipment.isEmpty() || equipment.equals("bodyweight", ignoreCase = true)

    /**
     * COMPUTED PROPERTY - Primary muscle targeted
     * Returns the first muscle in the list (usually the primary focus)
     */
    val primaryMuscle: String?
        get() = muscles.firstOrNull()
}

/**
 * Difficulty level enum
 */
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * EXAMPLE API RESPONSE FROM WGER:
 * ```json
 * {
 *   "id": 123,
 *   "name": "Bench press",
 *   "description": "Lie on bench, lower bar to chest...",
 *   "category": {
 *     "name": "Chest"
 *   },
 *   "muscles": [
 *     {
 *       "name": "Pectoralis major",
 *       "name_en": "Pectorals"
 *     }
 *   ],
 *   "equipment": [
 *     {
 *       "name": "Barbell"
 *     }
 *   ],
 *   "images": [
 *     {
 *       "image": "https://wger.de/media/exercise-images/123/image.png"
 *     }
 *   ]
 * }
 * ```
 *
 * CONVERSION IN REPOSITORY:
 * WorkoutDto (API response) → Workout (domain model) → UI
 *
 * EXAMPLE USAGE IN UI:
 * ```kotlin
 * @Composable
 * fun WorkoutCard(workout: Workout) {
 *     Card(
 *         modifier = Modifier.fillMaxWidth()
 *     ) {
 *         Row {
 *             // Load image with Coil
 *             AsyncImage(
 *                 model = workout.imageUrl,
 *                 contentDescription = workout.name,
 *                 modifier = Modifier.size(100.dp)
 *             )
 *
 *             Column {
 *                 Text(workout.name, style = MaterialTheme.typography.titleMedium)
 *                 Text(workout.category, style = MaterialTheme.typography.bodySmall)
 *
 *                 // Show difficulty badge
 *                 Badge(
 *                     containerColor = when(workout.difficulty) {
 *                         DifficultyLevel.BEGINNER -> Color.Green
 *                         DifficultyLevel.INTERMEDIATE -> Color.Yellow
 *                         DifficultyLevel.ADVANCED -> Color.Red
 *                     }
 *                 ) {
 *                     Text(workout.difficulty.name)
 *                 }
 *
 *                 // Show muscles targeted
 *                 FlowRow {
 *                     workout.muscles.forEach { muscle ->
 *                         AssistChip(
 *                             onClick = { },
 *                             label = { Text(muscle) }
 *                         )
 *                     }
 *                 }
 *
 *                 // Show if bodyweight exercise
 *                 if (workout.isBodyweightExercise) {
 *                     Icon(Icons.Default.Home, contentDescription = "At-home exercise")
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * MOCK DATA FOR TESTING:
 * ```kotlin
 * val sampleWorkouts = listOf(
 *     Workout(
 *         id = 1,
 *         name = "Push-ups",
 *         description = "Start in plank position, lower body until chest nearly touches floor...",
 *         category = "Chest",
 *         muscles = listOf("Pectorals", "Triceps", "Shoulders"),
 *         equipment = "Bodyweight",
 *         difficulty = DifficultyLevel.BEGINNER,
 *         imageUrl = "https://example.com/pushup.jpg"
 *     ),
 *     Workout(
 *         id = 2,
 *         name = "Deadlift",
 *         description = "Stand with feet hip-width apart, bend at hips to grasp bar...",
 *         category = "Back",
 *         muscles = listOf("Lower Back", "Hamstrings", "Glutes"),
 *         equipment = "Barbell",
 *         difficulty = DifficultyLevel.ADVANCED,
 *         imageUrl = "https://example.com/deadlift.jpg"
 *     )
 * )
 * ```
 */
