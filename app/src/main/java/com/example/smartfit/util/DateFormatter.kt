package com.example.smartfit.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * DATE FORMATTER - Consistent date formatting across the app
 *
 * Centralized date formatting ensures:
 * ✓ Consistent format throughout UI
 * ✓ Localization support (dates in user's language)
 * ✓ Easy to change format app-wide
 * ✓ Handles time zones correctly
 *
 * TIMESTAMP FORMAT:
 * SmartFit stores dates as Unix timestamps (Long):
 * - System.currentTimeMillis() returns current time
 * - Number of milliseconds since January 1, 1970, 00:00:00 GMT
 * - Easy to compare, sort, and store in database
 */
object DateFormatter {

    /**
     * FORMAT PATTERNS:
     *
     * Common patterns:
     * - "MMM dd, yyyy" → Jan 25, 2025
     * - "dd/MM/yyyy" → 25/01/2025
     * - "EEEE, MMM dd" → Saturday, Jan 25
     * - "HH:mm" → 14:30 (24-hour)
     * - "hh:mm a" → 02:30 PM (12-hour)
     *
     * Pattern letters:
     * - y: year
     * - M: month (MM = 01, MMM = Jan, MMMM = January)
     * - d: day of month
     * - E: day of week (EEE = Sat, EEEE = Saturday)
     * - H: hour (0-23)
     * - h: hour (1-12)
     * - m: minute
     * - s: second
     * - a: AM/PM marker
     */

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    /**
     * FORMAT TO DATE - Full date format
     *
     * @param timestamp - Unix timestamp in milliseconds
     * @return String - Formatted date (e.g., "Jan 25, 2025")
     *
     * EXAMPLE:
     * ```kotlin
     * Text(text = "Date: ${DateFormatter.formatToDate(activity.timestamp)}")
     * // Output: Date: Jan 25, 2025
     * ```
     */
    fun formatToDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * FORMAT TO TIME - Time only
     *
     * @param timestamp - Unix timestamp in milliseconds
     * @return String - Formatted time (e.g., "14:30")
     */
    fun formatToTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * FORMAT TO DATE TIME - Full date and time
     *
     * @param timestamp - Unix timestamp in milliseconds
     * @return String - Formatted date and time (e.g., "Jan 25, 2025 14:30")
     */
    fun formatToDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * FORMAT TO SHORT DATE - Month and day only
     *
     * @param timestamp - Unix timestamp in milliseconds
     * @return String - Formatted short date (e.g., "Jan 25")
     *
     * USAGE: Good for lists where year is implied
     */
    fun formatToShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    /**
     * FORMAT TO RELATIVE TIME - "Today", "Yesterday", or date
     *
     * @param timestamp - Unix timestamp in milliseconds
     * @return String - Relative time or date
     *
     * EXAMPLE OUTPUT:
     * - "Today" (if same day)
     * - "Yesterday" (if previous day)
     * - "Jan 23, 2025" (if older)
     *
     * USAGE:
     * ```kotlin
     * LazyColumn {
     *     items(activities) { activity ->
     *         Text(DateFormatter.formatToRelativeTime(activity.timestamp))
     *     }
     * }
     * ```
     */
    fun formatToRelativeTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return when {
            isSameDay(now, date) -> "Today"
            isYesterday(now, date) -> "Yesterday"
            else -> formatToDate(timestamp)
        }
    }

    /**
     * GET WEEK START - Get timestamp for start of current week (Monday)
     *
     * @return Long - Timestamp for Monday 00:00:00
     */
    fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * GET MONTH START - Get timestamp for start of current month
     *
     * @return Long - Timestamp for 1st day 00:00:00
     */
    fun getMonthStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * GET DAY START - Get timestamp for start of today
     *
     * @return Long - Timestamp for today 00:00:00
     */
    fun getDayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Helper functions
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, date)
    }
}
