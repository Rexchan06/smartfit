package com.example.smartfit.util

import kotlin.math.round

/**
 * CALORIE CALCULATOR - Business logic for fitness calculations
 *
 * This utility provides methods for calculating calories burned during activities.
 * These calculations use MET (Metabolic Equivalent of Task) values, which are
 * scientifically standardized measures of exercise intensity.
 *
 * WHY A SEPARATE UTILITY CLASS?
 * ✓ Reusable across ViewModels and Repositories
 * ✓ Easy to test (pure functions, no dependencies)
 * ✓ Centralized business logic
 * ✓ Can be unit tested independently
 *
 * MET (METABOLIC EQUIVALENT OF TASK):
 * - 1 MET = energy burned at rest (about 1 calorie per kg per hour)
 * - 3 METs = light activity (walking)
 * - 6 METs = moderate activity (jogging)
 * - 10 METs = vigorous activity (running)
 *
 * FORMULA:
 * Calories = MET × Weight(kg) × Duration(hours)
 *
 * EXAMPLE:
 * A 70kg person running (10 METs) for 30 minutes:
 * Calories = 10 × 70 × 0.5 = 350 calories
 *
 * NOTE: These are estimates. Actual calorie burn varies by:
 * - Individual metabolism
 * - Fitness level
 * - Exercise intensity
 * - Environmental conditions
 * - Body composition
 */
object CalorieCalculator {

    /**
     * MET VALUES - Standard metabolic equivalents for common activities
     *
     * Source: Compendium of Physical Activities (Ainsworth et al.)
     * https://sites.google.com/site/compendiumofphysicalactivities/
     */
    private val MET_VALUES = mapOf(
        // Cardio activities
        "walking" to 3.5,
        "jogging" to 7.0,
        "running" to 10.0,
        "cycling" to 8.0,
        "swimming" to 8.0,
        "hiking" to 6.0,

        // Gym activities
        "weight training" to 6.0,
        "circuit training" to 8.0,
        "crossfit" to 10.0,

        // Sports
        "basketball" to 8.0,
        "soccer" to 10.0,
        "tennis" to 7.0,
        "volleyball" to 4.0,

        // Mind-body
        "yoga" to 2.5,
        "pilates" to 3.0,
        "stretching" to 2.3,

        // Other
        "dancing" to 5.0,
        "jumping rope" to 12.0,
        "rowing" to 7.0,
        "elliptical" to 5.0,
        "stair climbing" to 9.0
    )

    /**
     * CALCULATE CALORIES BURNED - Main calculation method
     *
     * Uses MET formula: Calories = MET × Weight(kg) × Duration(hours)
     *
     * @param activityType - Type of activity (case-insensitive)
     * @param durationMinutes - Duration in minutes
     * @param weightKg - User's weight in kilograms
     * @return Int - Estimated calories burned (rounded)
     *
     * EXAMPLE USAGE IN REPOSITORY:
     * ```kotlin
     * suspend fun insertActivity(
     *     type: String,
     *     durationMinutes: Int,
     *     userWeightKg: Double
     * ) {
     *     val caloriesBurned = CalorieCalculator.calculateCalories(
     *         activityType = type,
     *         durationMinutes = durationMinutes,
     *         weightKg = userWeightKg
     *     )
     *
     *     val activity = Activity(
     *         type = type,
     *         durationMinutes = durationMinutes,
     *         caloriesBurned = caloriesBurned,
     *         timestamp = System.currentTimeMillis()
     *     )
     *
     *     activityDao.insert(activity.toEntity())
     * }
     * ```
     *
     * EXAMPLE USAGE IN VIEWMODEL:
     * ```kotlin
     * fun estimateCalories() {
     *     val calories = CalorieCalculator.calculateCalories(
     *         activityType = selectedActivity,
     *         durationMinutes = duration,
     *         weightKg = userWeight
     *     )
     *     _estimatedCalories.value = calories
     * }
     * ```
     */
    fun calculateCalories(
        activityType: String,
        durationMinutes: Int,
        weightKg: Double
    ): Int {
        // Get MET value for activity (case-insensitive lookup)
        val met = getMETValue(activityType)

        // Convert duration to hours
        val durationHours = durationMinutes / 60.0

        // Calculate: MET × Weight × Duration
        val calories = met * weightKg * durationHours

        // Round to nearest integer
        return round(calories).toInt()
    }

    /**
     * GET MET VALUE - Look up MET value for activity type
     *
     * @param activityType - Activity name (case-insensitive)
     * @return Double - MET value (default 5.0 if unknown)
     *
     * CASE-INSENSITIVE MATCHING:
     * "Running", "running", "RUNNING" all work
     *
     * UNKNOWN ACTIVITIES:
     * Returns 5.0 (moderate intensity) as default
     */
    fun getMETValue(activityType: String): Double {
        return MET_VALUES[activityType.lowercase()] ?: 5.0
    }

    /**
     * CALCULATE CALORIES WITH DISTANCE - For running/cycling
     *
     * Alternative formula using distance instead of duration
     * More accurate for distance-based activities
     *
     * FORMULA:
     * Running: ~1 calorie per kilogram per kilometer
     * Cycling: ~0.5 calories per kilogram per kilometer
     *
     * @param activityType - "running" or "cycling"
     * @param distanceKm - Distance in kilometers
     * @param weightKg - User's weight in kilograms
     * @return Int - Estimated calories burned
     *
     * EXAMPLE:
     * ```kotlin
     * val calories = CalorieCalculator.calculateCaloriesWithDistance(
     *     activityType = "running",
     *     distanceKm = 5.0,
     *     weightKg = 70.0
     * )
     * // Result: ~350 calories
     * ```
     */
    fun calculateCaloriesWithDistance(
        activityType: String,
        distanceKm: Double,
        weightKg: Double
    ): Int {
        val caloriesPerKgKm = when (activityType.lowercase()) {
            "running", "jogging" -> 1.0  // 1 cal/kg/km
            "cycling" -> 0.5              // 0.5 cal/kg/km
            "walking" -> 0.5              // 0.5 cal/kg/km
            else -> 0.75                  // Average
        }

        val calories = caloriesPerKgKm * weightKg * distanceKm
        return round(calories).toInt()
    }

    /**
     * CALCULATE BMR - Basal Metabolic Rate
     *
     * BMR = calories burned at rest per day
     * Useful for calculating daily calorie goals
     *
     * MIFFLIN-ST JEOR EQUATION:
     * Men: BMR = (10 × weight) + (6.25 × height) − (5 × age) + 5
     * Women: BMR = (10 × weight) + (6.25 × height) − (5 × age) − 161
     *
     * @param weightKg - Weight in kilograms
     * @param heightCm - Height in centimeters
     * @param age - Age in years
     * @param isMale - true for male, false for female
     * @return Int - BMR in calories per day
     *
     * EXAMPLE:
     * ```kotlin
     * val bmr = CalorieCalculator.calculateBMR(
     *     weightKg = 70.0,
     *     heightCm = 175.0,
     *     age = 25,
     *     isMale = true
     * )
     * // Result: ~1700 calories/day
     * ```
     */
    fun calculateBMR(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        isMale: Boolean
    ): Int {
        val baseBMR = (10 * weightKg) + (6.25 * heightCm) - (5 * age)

        val bmr = if (isMale) {
            baseBMR + 5
        } else {
            baseBMR - 161
        }

        return round(bmr).toInt()
    }

    /**
     * CALCULATE TDEE - Total Daily Energy Expenditure
     *
     * TDEE = BMR × Activity Factor
     * Total calories burned per day including activities
     *
     * ACTIVITY FACTORS:
     * - Sedentary (little/no exercise): BMR × 1.2
     * - Lightly active (1-3 days/week): BMR × 1.375
     * - Moderately active (3-5 days/week): BMR × 1.55
     * - Very active (6-7 days/week): BMR × 1.725
     * - Extremely active (athlete): BMR × 1.9
     *
     * @param bmr - Basal Metabolic Rate
     * @param activityLevel - Activity level (0-4)
     * @return Int - TDEE in calories per day
     *
     * EXAMPLE:
     * ```kotlin
     * val bmr = CalorieCalculator.calculateBMR(70.0, 175.0, 25, true)
     * val tdee = CalorieCalculator.calculateTDEE(bmr, activityLevel = 2)
     * // Result: ~2635 calories/day
     * ```
     */
    fun calculateTDEE(bmr: Int, activityLevel: Int): Int {
        val activityFactor = when (activityLevel) {
            0 -> 1.2    // Sedentary
            1 -> 1.375  // Lightly active
            2 -> 1.55   // Moderately active
            3 -> 1.725  // Very active
            4 -> 1.9    // Extremely active
            else -> 1.55  // Default to moderately active
        }

        return round(bmr * activityFactor).toInt()
    }

    /**
     * CALCULATE STEP GOAL PROGRESS - Percentage of daily step goal
     *
     * @param currentSteps - Steps taken today
     * @param goalSteps - Daily step goal (default 10,000)
     * @return Int - Progress percentage (0-100+)
     *
     * EXAMPLE:
     * ```kotlin
     * val progress = CalorieCalculator.calculateStepGoalProgress(
     *     currentSteps = 7500,
     *     goalSteps = 10000
     * )
     * // Result: 75%
     *
     * // In UI
     * LinearProgressIndicator(progress = progress / 100f)
     * Text("${progress}% of daily goal")
     * ```
     */
    fun calculateStepGoalProgress(currentSteps: Int, goalSteps: Int = 10000): Int {
        if (goalSteps <= 0) return 0
        val percentage = (currentSteps.toDouble() / goalSteps * 100).toInt()
        return percentage  // Can exceed 100%
    }

    /**
     * ESTIMATE DISTANCE FROM STEPS - Convert steps to kilometers
     *
     * Average stride length:
     * - ~0.78 meters per step
     * - ~1280 steps per kilometer
     *
     * @param steps - Number of steps
     * @return Double - Estimated distance in kilometers
     *
     * EXAMPLE:
     * ```kotlin
     * val distance = CalorieCalculator.estimateDistanceFromSteps(10000)
     * // Result: ~7.8 km
     * ```
     */
    fun estimateDistanceFromSteps(steps: Int): Double {
        val metersPerStep = 0.78
        val meters = steps * metersPerStep
        val kilometers = meters / 1000.0
        return round(kilometers * 10) / 10.0  // Round to 1 decimal place
    }

    /**
     * GET ALL ACTIVITY TYPES - Returns list of supported activities
     *
     * @return List<String> - All activity types with known MET values
     *
     * USAGE:
     * ```kotlin
     * // In UI - Activity type dropdown
     * ExposedDropdownMenuBox {
     *     CalorieCalculator.getAllActivityTypes().forEach { activity ->
     *         DropdownMenuItem(
     *             text = { Text(activity.capitalize()) },
     *             onClick = { selectedActivity = activity }
     *         )
     *     }
     * }
     * ```
     */
    fun getAllActivityTypes(): List<String> {
        return MET_VALUES.keys.sorted()
    }

    /**
     * FORMAT CALORIES - Pretty print calories with "cal" suffix
     *
     * @param calories - Number of calories
     * @return String - Formatted string (e.g., "350 cal")
     */
    fun formatCalories(calories: Int): String {
        return "$calories cal"
    }

    /**
     * FORMAT DISTANCE - Pretty print distance in km
     *
     * @param distanceKm - Distance in kilometers
     * @return String - Formatted string (e.g., "5.2 km")
     */
    fun formatDistance(distanceKm: Double): String {
        return "%.1f km".format(distanceKm)
    }
}

/**
 * UNIT TESTING THIS CLASS:
 *
 * This is a perfect candidate for unit testing because:
 * ✓ Pure functions (no side effects)
 * ✓ No dependencies (no mocking needed)
 * ✓ Predictable outputs for given inputs
 * ✓ Business logic that needs to be correct
 *
 * EXAMPLE TESTS:
 * ```kotlin
 * class CalorieCalculatorTest {
 *
 *     @Test
 *     fun `calculateCalories for 30min running at 70kg returns correct value`() {
 *         val result = CalorieCalculator.calculateCalories(
 *             activityType = "running",
 *             durationMinutes = 30,
 *             weightKg = 70.0
 *         )
 *         // MET=10, Weight=70, Duration=0.5h → 10*70*0.5 = 350
 *         assertEquals(350, result)
 *     }
 *
 *     @Test
 *     fun `getMETValue returns correct MET for running`() {
 *         assertEquals(10.0, CalorieCalculator.getMETValue("running"))
 *         assertEquals(10.0, CalorieCalculator.getMETValue("Running"))  // Case-insensitive
 *         assertEquals(10.0, CalorieCalculator.getMETValue("RUNNING"))
 *     }
 *
 *     @Test
 *     fun `getMETValue returns default for unknown activity`() {
 *         assertEquals(5.0, CalorieCalculator.getMETValue("unknown"))
 *     }
 *
 *     @Test
 *     fun `calculateStepGoalProgress returns correct percentage`() {
 *         assertEquals(75, CalorieCalculator.calculateStepGoalProgress(7500, 10000))
 *         assertEquals(100, CalorieCalculator.calculateStepGoalProgress(10000, 10000))
 *         assertEquals(150, CalorieCalculator.calculateStepGoalProgress(15000, 10000))
 *     }
 *
 *     @Test
 *     fun `estimateDistanceFromSteps converts correctly`() {
 *         val distance = CalorieCalculator.estimateDistanceFromSteps(10000)
 *         assertEquals(7.8, distance, 0.1)
 *     }
 * }
 * ```
 *
 * These tests ensure the calculations are accurate and handle edge cases correctly.
 * This is perfect for your university project's testing requirements!
 */
