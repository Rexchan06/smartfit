package com.example.smartfit

import com.example.smartfit.data.local.entity.ActivityEntity
import com.example.smartfit.domain.model.Activity
import org.junit.Assert.*
import org.junit.Test

/**
 * ACTIVITY ENTITY UNIT TESTS
 *
 * Tests Activity entity and domain model transformations
 */
class ActivityEntityTest {

    @Test
    fun `ActivityEntity creation with all fields`() {
        val entity = ActivityEntity(
            id = 1,
            type = "Running",
            durationMinutes = 30,
            caloriesBurned = 300,
            distanceKm = 5.0,
            steps = 6500,
            timestamp = System.currentTimeMillis(),
            notes = "Morning run"
        )

        assertEquals("Running", entity.type)
        assertEquals(30, entity.durationMinutes)
        assertEquals(300, entity.caloriesBurned)
        assertEquals(5.0, entity.distanceKm, 0.01)
        assertEquals(6500, entity.steps)
        assertEquals("Morning run", entity.notes)
    }

    @Test
    fun `Activity domain model with computed properties`() {
        val activity = Activity(
            id = 1,
            type = "Running",
            durationMinutes = 30,
            caloriesBurned = 300,
            distanceKm = 5.0,
            steps = 6500,
            timestamp = System.currentTimeMillis(),
            notes = "Test"
        )

        // Test computed property: caloriesPerMinute
        val caloriesPerMin = activity.caloriesPerMinute
        assertEquals("Should be 10 cal/min", 10.0, caloriesPerMin, 0.1)
    }

    @Test
    fun `Activity intensityLevel for high intensity`() {
        val activity = Activity(
            type = "Running",
            durationMinutes = 30,
            caloriesBurned = 400,  // High calories = high intensity
            timestamp = System.currentTimeMillis()
        )

        // High intensity if > 10 cal/min
        val intensity = activity.intensityLevel
        assertEquals(Activity.IntensityLevel.HIGH, intensity)
    }

    @Test
    fun `Activity intensityLevel for medium intensity`() {
        val activity = Activity(
            type = "Walking",
            durationMinutes = 30,
            caloriesBurned = 180,  // 6 cal/min = medium
            timestamp = System.currentTimeMillis()
        )

        val intensity = activity.intensityLevel
        assertEquals(Activity.IntensityLevel.MEDIUM, intensity)
    }

    @Test
    fun `Activity intensityLevel for low intensity`() {
        val activity = Activity(
            type = "Yoga",
            durationMinutes = 30,
            caloriesBurned = 90,  // 3 cal/min = low
            timestamp = System.currentTimeMillis()
        )

        val intensity = activity.intensityLevel
        assertEquals(Activity.IntensityLevel.LOW, intensity)
    }

    @Test
    fun `Activity with nullable fields`() {
        val activity = Activity(
            type = "Gym",
            durationMinutes = 45,
            caloriesBurned = 250,
            distanceKm = null,  // No distance for gym
            steps = null,  // No steps tracked
            timestamp = System.currentTimeMillis(),
            notes = null  // No notes
        )

        assertNull("Distance should be null", activity.distanceKm)
        assertNull("Steps should be null", activity.steps)
        assertNull("Notes should be null", activity.notes)
    }

    @Test
    fun `Activity timestamp is valid`() {
        val now = System.currentTimeMillis()
        val activity = Activity(
            type = "Test",
            durationMinutes = 10,
            caloriesBurned = 50,
            timestamp = now
        )

        assertTrue("Timestamp should be recent", activity.timestamp > now - 1000)
        assertTrue("Timestamp should not be future", activity.timestamp <= System.currentTimeMillis())
    }
}
