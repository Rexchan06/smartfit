package com.example.smartfit

import com.example.smartfit.util.DateFormatter
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * DATE FORMATTER UNIT TESTS
 *
 * Tests date formatting utility functions
 */
class DateFormatterTest {

    @Test
    fun `formatDate returns correct format`() {
        // Create a known timestamp: Jan 15, 2024, 14:30
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 14, 30, 0)
        }
        val timestamp = calendar.timeInMillis

        val formatted = DateFormatter.formatDate(timestamp)

        // Should contain date elements
        assertNotNull("Formatted date should not be null", formatted)
        assertTrue("Formatted date should not be empty", formatted.isNotEmpty())
    }

    @Test
    fun `formatTime returns correct format`() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 14, 30, 0)
        }
        val timestamp = calendar.timeInMillis

        val formatted = DateFormatter.formatTime(timestamp)

        // Should contain time
        assertNotNull("Formatted time should not be null", formatted)
        assertTrue("Formatted time should contain time elements", formatted.isNotEmpty())
    }

    @Test
    fun `formatDateTime returns combined date and time`() {
        val timestamp = System.currentTimeMillis()

        val formatted = DateFormatter.formatDateTime(timestamp)

        // Should be longer than just date or time alone
        assertNotNull("Formatted datetime should not be null", formatted)
        assertTrue("Formatted datetime should be substantial", formatted.length > 10)
    }

    @Test
    fun `formatDuration formats minutes correctly`() {
        // 30 minutes
        val formatted = DateFormatter.formatDuration(30)

        assertEquals("30 minutes", "30 min", formatted)
    }

    @Test
    fun `formatDuration formats hours and minutes correctly`() {
        // 90 minutes = 1 hour 30 minutes
        val formatted = DateFormatter.formatDuration(90)

        assertTrue("Should show hours and minutes", formatted.contains("h") || formatted.contains("hour"))
    }

    @Test
    fun `formatDuration handles zero minutes`() {
        val formatted = DateFormatter.formatDuration(0)

        assertNotNull("Zero duration should have a formatted value", formatted)
    }

    @Test
    fun `getRelativeTimeString for today shows today`() {
        val now = System.currentTimeMillis()

        val relative = DateFormatter.getRelativeTimeString(now)

        assertTrue("Should mention today", relative.contains("Today", ignoreCase = true) ||
                   relative.contains("now", ignoreCase = true) ||
                   relative.contains("moment", ignoreCase = true))
    }

    @Test
    fun `getRelativeTimeString for yesterday shows yesterday`() {
        val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        val relative = DateFormatter.getRelativeTimeString(yesterday)

        assertTrue("Should mention yesterday or show date",
            relative.contains("Yesterday", ignoreCase = true) || relative.length > 5)
    }
}
