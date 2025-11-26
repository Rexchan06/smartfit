package com.example.smartfit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartfit.domain.model.Activity
import com.example.smartfit.util.DateFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * ACTIVITY CARD - Reusable Component for Displaying Activity
 *
 * A Material 3 card that shows a fitness activity with:
 * - Activity type with icon
 * - Duration and calories
 * - Distance and steps (if available)
 * - Timestamp
 * - Optional click handler
 * - Optional delete button
 *
 * REUSABLE COMPONENT BENEFITS:
 * ✓ Consistent UI across screens
 * ✓ Single place to update design
 * ✓ Easier to test
 * ✓ Reduces code duplication
 *
 * USAGE:
 * ```kotlin
 * ActivityCard(
 *     activity = activity,
 *     onClick = { navController.navigate(Screen.ActivityDetail.createRoute(activity.id)) },
 *     onDelete = { viewModel.deleteActivity(activity.id) }
 * )
 * ```
 */
@Composable
fun ActivityCard(
    activity: Activity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Icon(
                imageVector = getActivityIcon(activity.type),
                contentDescription = "${activity.type} icon",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Activity details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Activity type
                Text(
                    text = activity.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Duration and calories
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Duration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duration",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.durationMinutes} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Calories
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calories",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activity.caloriesBurned} cal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Distance and steps (if available)
                if (activity.distanceKm != null || activity.steps != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Distance
                        activity.distanceKm?.let { distance ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = "Distance",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.2f km", distance),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Steps
                        activity.steps?.let { steps ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = "Steps",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$steps steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button (if provided)
            onDelete?.let { deleteHandler ->
                IconButton(
                    onClick = deleteHandler
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete activity",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * GET ACTIVITY ICON - Returns appropriate icon for activity type
 *
 * Maps activity type string to Material Icon
 */
private fun getActivityIcon(activityType: String): ImageVector {
    return when (activityType.lowercase()) {
        "running" -> Icons.Default.DirectionsRun
        "cycling", "biking" -> Icons.Default.DirectionsBike
        "walking" -> Icons.Default.DirectionsWalk
        "swimming" -> Icons.Default.Pool
        "gym", "weightlifting", "strength" -> Icons.Default.FitnessCenter
        "yoga" -> Icons.Default.SelfImprovement
        "hiking" -> Icons.Default.Hiking
        else -> Icons.Default.FitnessCenter  // Default icon
    }
}

/**
 * FORMAT TIMESTAMP - Converts timestamp to readable format
 *
 * Examples:
 * - "Today at 2:30 PM"
 * - "Yesterday at 10:15 AM"
 * - "Jan 15 at 9:00 AM"
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeString = timeFormat.format(Date(timestamp))

    return when {
        // Today
        diff < 24 * 60 * 60 * 1000 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) -> {
            "Today at $timeString"
        }
        // Yesterday
        diff < 48 * 60 * 60 * 1000 -> {
            "Yesterday at $timeString"
        }
        // This week
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            "${dayFormat.format(Date(timestamp))} at $timeString"
        }
        // Older
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            "${dateFormat.format(Date(timestamp))} at $timeString"
        }
    }
}
