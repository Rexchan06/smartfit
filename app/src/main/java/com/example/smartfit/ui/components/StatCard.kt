package com.example.smartfit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * STAT CARD - Reusable Component for Displaying Statistics
 *
 * A compact Material 3 card that displays a single statistic with:
 * - Icon
 * - Label (metric name)
 * - Value (number)
 * - Optional unit
 *
 * DESIGN:
 * Clean, minimal card perfect for dashboard/summary views
 * Uses Material 3 color scheme for automatic theming
 *
 * USAGE:
 * ```kotlin
 * StatCard(
 *     icon = Icons.Default.LocalFireDepartment,
 *     label = "Calories",
 *     value = "1,234",
 *     unit = "kcal",
 *     iconContentDescription = "Calories burned"
 * )
 * ```
 *
 * GRID LAYOUT EXAMPLE:
 * ```kotlin
 * LazyVerticalGrid(columns = GridCells.Fixed(2)) {
 *     item { StatCard(icon = Icons.Default.LocalFireDepartment, ...) }
 *     item { StatCard(icon = Icons.Default.DirectionsWalk, ...) }
 *     item { StatCard(icon = Icons.Default.Route, ...) }
 *     item { StatCard(icon = Icons.Default.Timer, ...) }
 * }
 * ```
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    iconContentDescription: String,
    modifier: Modifier = Modifier,
    unit: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Value
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Unit (if provided)
                unit?.let {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * STAT CARD HORIZONTAL - Alternative Layout
 *
 * Horizontal layout variant of StatCard for different UI arrangements
 */
@Composable
fun StatCardHorizontal(
    icon: ImageVector,
    label: String,
    value: String,
    iconContentDescription: String,
    modifier: Modifier = Modifier,
    unit: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Label and value
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    unit?.let {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
