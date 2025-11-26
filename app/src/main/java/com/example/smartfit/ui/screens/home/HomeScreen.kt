package com.example.smartfit.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartfit.ui.components.ActivityCard
import com.example.smartfit.ui.components.StatCard
import com.example.smartfit.ui.components.WorkoutCardCompact
import com.example.smartfit.ui.navigation.Screen

/**
 * HOME SCREEN - Main Dashboard
 *
 * The main screen of the app showing:
 * - Daily statistics (calories, steps, activities)
 * - Recent activities list
 * - Workout suggestions from API
 * - FAB to add new activity (with animation)
 *
 * FEATURES:
 * ✓ Reactive UI - automatically updates when data changes
 * ✓ FAB animation - scales based on scroll
 * ✓ Adaptive layout - ready for tablet support
 * ✓ Loading states
 * ✓ Error handling
 * ✓ Accessibility - content descriptions on all icons
 *
 * ARCHITECTURE:
 * Screen → ViewModel (StateFlow) → Repository → Database/API
 * Changes in database automatically trigger UI updates via Flow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel
    val recentActivities by viewModel.recentActivities.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()
    val totalDistance by viewModel.totalDistance.collectAsState()
    val activityCount by viewModel.activityCount.collectAsState()
    val workoutSuggestions by viewModel.workoutSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Scroll state for FAB animation
    val listState = rememberLazyListState()
    val fabScale by animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) 0.85f else 1f,
        label = "FAB scale animation"
    )

    // Use BoxWithConstraints for adaptive layout support
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "SmartFit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // Profile button
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Profile.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile settings"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddActivity.route)
                    },
                    modifier = Modifier.scale(fabScale),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new activity"
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ============================================================
                // DAILY STATISTICS SECTION
                // ============================================================
                item {
                    Text(
                        text = "Today's Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    // Statistics grid - 2 columns on phone, 4 on tablet
                    if (isCompact) {
                        // Phone layout - 2 columns
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.LocalFireDepartment,
                                    label = "Calories",
                                    value = totalCalories.toString(),
                                    unit = "kcal",
                                    iconContentDescription = "Calories burned",
                                    modifier = Modifier.weight(1f)
                                )

                                StatCard(
                                    icon = Icons.Default.FitnessCenter,
                                    label = "Activities",
                                    value = activityCount.toString(),
                                    iconContentDescription = "Total activities",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.Route,
                                    label = "Distance",
                                    value = String.format("%.1f", totalDistance),
                                    unit = "km",
                                    iconContentDescription = "Total distance",
                                    modifier = Modifier.weight(1f)
                                )

                                StatCard(
                                    icon = Icons.Default.TrendingUp,
                                    label = "Avg Intensity",
                                    value = if (activityCount > 0) {
                                        val avgCalPerActivity = totalCalories / activityCount
                                        when {
                                            avgCalPerActivity > 300 -> "High"
                                            avgCalPerActivity > 150 -> "Med"
                                            else -> "Low"
                                        }
                                    } else {
                                        "-"
                                    },
                                    iconContentDescription = "Average intensity",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Tablet layout - 4 columns
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.LocalFireDepartment,
                                label = "Calories",
                                value = totalCalories.toString(),
                                unit = "kcal",
                                iconContentDescription = "Calories burned",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.FitnessCenter,
                                label = "Activities",
                                value = activityCount.toString(),
                                iconContentDescription = "Total activities",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Route,
                                label = "Distance",
                                value = String.format("%.1f", totalDistance),
                                unit = "km",
                                iconContentDescription = "Total distance",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.TrendingUp,
                                label = "Avg Intensity",
                                value = if (activityCount > 0) "Med" else "-",
                                iconContentDescription = "Average intensity",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ============================================================
                // RECENT ACTIVITIES SECTION
                // ============================================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activities",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = {
                                navController.navigate(Screen.ActivityLog.route)
                            }
                        ) {
                            Text("View All")
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View all activities",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (recentActivities.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = "No activities",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No activities yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the + button to add your first activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(recentActivities.take(5)) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = {
                                // Could navigate to detail screen if implemented
                            }
                        )
                    }
                }

                // ============================================================
                // WORKOUT SUGGESTIONS SECTION
                // ============================================================
                if (workoutSuggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Workout Ideas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    item {
                        Text(
                            text = "Try these exercises to mix up your routine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(workoutSuggestions) { workout ->
                                WorkoutCardCompact(
                                    workout = workout,
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error snackbar
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    }
}
