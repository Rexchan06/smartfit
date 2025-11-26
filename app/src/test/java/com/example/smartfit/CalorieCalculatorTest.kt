package com.example.smartfit

import com.example.smartfit.util.CalorieCalculator
import org.junit.Assert.*
import org.junit.Test

/**
 * CALORIE CALCULATOR UNIT TESTS
 *
 * Tests the CalorieCalculator utility class
 * Testing pure functions with known inputs/outputs
 */
class CalorieCalculatorTest {

    @Test
    fun `calculateCalories for running returns correct value`() {
        // Arrange: 30 minutes running, 70kg person
        val activityType = "Running"
        val durationMinutes = 30
        val weightKg = 70.0

        // Act
        val calories = CalorieCalculator.calculateCalories(
            activityType = activityType,
            durationMinutes = durationMinutes,
            weightKg = weightKg
        )

        // Assert: Running MET is ~10, so 30 min * 70kg * 10 / 60 = ~350 calories
        assertTrue("Calories should be between 300-400 for 30 min running", calories in 300..400)
    }

    @Test
    fun `calculateCalories for walking returns lower calories than running`() {
        val duration = 30
        val weight = 70.0

        val runningCalories = CalorieCalculator.calculateCalories("Running", duration, weight)
        val walkingCalories = CalorieCalculator.calculateCalories("Walking", duration, weight)

        assertTrue("Running should burn more calories than walking", runningCalories > walkingCalories)
    }

    @Test
    fun `calculateCalories for cycling returns positive value`() {
        val calories = CalorieCalculator.calculateCalories("Cycling", 45, 75.0)

        assertTrue("Calories must be positive", calories > 0)
        assertTrue("Calories should be reasonable", calories < 1000)
    }

    @Test
    fun `calculateCaloriesFromDistance for running returns correct value`() {
        // 5km run at 70kg should burn ~350 calories
        val calories = CalorieCalculator.calculateCaloriesFromDistance(
            activityType = "Running",
            distanceKm = 5.0,
            weightKg = 70.0
        )

        assertTrue("5km running should burn 300-500 calories", calories in 300..500)
    }

    @Test
    fun `calculateBMR for adult returns reasonable value`() {
        // Male, 30 years, 70kg, 175cm
        val bmr = CalorieCalculator.calculateBMR(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 30,
            isMale = true
        )

        // BMR should be roughly 1500-2000 for average adult
        assertTrue("BMR should be reasonable", bmr in 1500.0..2000.0)
    }

    @Test
    fun `calculateTDEE for sedentary person returns correct value`() {
        val bmr = 1700.0
        val tdee = CalorieCalculator.calculateTDEE(bmr, "sedentary")

        // Sedentary multiplier is 1.2
        val expected = bmr * 1.2
        assertEquals("TDEE should be BMR * 1.2", expected, tdee, 1.0)
    }

    @Test
    fun `calculateStepGoalProgress returns correct percentage`() {
        val currentSteps = 7500
        val goalSteps = 10000

        val progress = CalorieCalculator.calculateStepGoalProgress(currentSteps, goalSteps)

        assertEquals("75% progress for 7500/10000 steps", 75, progress)
    }

    @Test
    fun `calculateStepGoalProgress handles over 100 percent`() {
        val progress = CalorieCalculator.calculateStepGoalProgress(12000, 10000)

        assertTrue("Progress should be capped or shown as >100%", progress >= 100)
    }

    @Test
    fun `estimateDistanceFromSteps returns reasonable value`() {
        // 10,000 steps ≈ 8km (average stride)
        val distance = CalorieCalculator.estimateDistanceFromSteps(10000)

        assertTrue("10k steps should be 6-10km", distance in 6.0..10.0)
    }
}
