package com.example.smartfit.ui.screens.addactivity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * ADD ACTIVITY SCREEN - Form to Create New Activity
 *
 * Features:
 * - Dropdown for activity type selection
 * - Input fields with validation
 * - Auto-calculate calories option
 * - Optional fields (distance, steps, notes)
 * - Save button with loading state
 * - Success navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    viewModel: AddActivityViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Collect state
    val activityType by viewModel.activityType.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val notes by viewModel.notes.collectAsState()

    val typeError by viewModel.typeError.collectAsState()
    val durationError by viewModel.durationError.collectAsState()
    val caloriesError by viewModel.caloriesError.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val error by viewModel.error.collectAsState()

    // Navigate back when saved
    LaunchedEffect(isSaved) {
        if (isSaved) {
            navController.navigateUp()
        }
    }

    // Activity types
    val activityTypes = listOf(
        "Running",
        "Cycling",
        "Walking",
        "Swimming",
        "Gym",
        "Yoga",
        "Hiking",
        "Dancing",
        "Sports"
    )

    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Activity",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel and go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fill in the details of your fitness activity. Calories will be estimated automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Activity type dropdown
            ExposedDropdownMenuBox(
                expanded = showDropdown,
                onExpandedChange = { showDropdown = it }
            ) {
                OutlinedTextField(
                    value = activityType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Type *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Activity type"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                    },
                    isError = typeError != null,
                    supportingText = typeError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    activityTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.updateActivityType(type)
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            // Duration field
            OutlinedTextField(
                value = duration,
                onValueChange = { viewModel.updateDuration(it) },
                label = { Text("Duration (minutes) *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Duration"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = durationError != null,
                supportingText = durationError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Calories field
            OutlinedTextField(
                value = calories,
                onValueChange = { viewModel.updateCalories(it) },
                label = { Text("Calories Burned *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Calories"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = caloriesError != null,
                supportingText = caloriesError?.let { { Text(it) } }
                    ?: { Text("Auto-calculated based on activity type", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // Optional fields section
            Text(
                text = "Optional Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Distance field
            OutlinedTextField(
                value = distance,
                onValueChange = { viewModel.updateDistance(it) },
                label = { Text("Distance (km)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Distance"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Steps field
            OutlinedTextField(
                value = steps,
                onValueChange = { viewModel.updateSteps(it) },
                label = { Text("Steps") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Steps"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = "Notes"
                    )
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = { viewModel.saveActivity() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Activity",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Required fields note
            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Error snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
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
