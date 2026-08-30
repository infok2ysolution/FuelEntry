package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PakistanPresets
import com.example.model.RouteStop
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteStopBuilder(
    startLocation: String,
    onStartLocationChange: (String) -> Unit,
    stops: List<RouteStop>,
    onAddStop: (name: String, distanceKm: Double, notes: String) -> Unit,
    onRemoveStop: (stopId: String) -> Unit,
    onUpdateStopDistance: (stopId: String, distance: Double) -> Unit,
    isRoundTrip: Boolean,
    onRoundTripChange: (Boolean) -> Unit,
    totalDistanceKm: Double,
    modifier: Modifier = Modifier
) {
    var showAddStopDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = FuelGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Multi-Destination Route (${stops.size} Stops)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FuelGreenPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${"%.1f".format(totalDistanceKm)} km Total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = FuelGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start Location
            OutlinedTextField(
                value = startLocation,
                onValueChange = onStartLocationChange,
                label = { Text("Starting Location / Office") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Start",
                        tint = FuelGreenPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_location_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Start presets
            Text(
                text = "Quick Origin / Base Presets:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "Head Office (I.I. Chundrigar)",
                    "Gulberg Office, Lahore",
                    "Blue Area Tower, Islamabad",
                    "Regional Warehouse"
                ).forEach { preset ->
                    AssistChip(
                        onClick = { onStartLocationChange(preset) },
                        label = { Text(preset, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Destination Stops List
            Text(
                text = "Destination Stops (N Stops):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (stops.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No destinations added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add client sites, branches or stops to calculate distance & fuel cost automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stops.forEachIndexed { index, stop ->
                        RouteStopItem(
                            index = index + 1,
                            stop = stop,
                            onRemove = { onRemoveStop(stop.id) },
                            onDistanceChange = { newDist -> onUpdateStopDistance(stop.id, newDist) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Round trip toggle & Add Stop Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onRoundTripChange(!isRoundTrip) }
                ) {
                    Switch(
                        checked = isRoundTrip,
                        onCheckedChange = onRoundTripChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = FuelGreenPrimary),
                        modifier = Modifier.testTag("round_trip_switch")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Round Trip (Return to base)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isRoundTrip) "Doubles stop distance automatically" else "One-way travel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showAddStopDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_destination_stop_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary)
            ) {
                Icon(imageVector = Icons.Default.AddLocationAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Destination Stop ($ {stops.size + 1})")
            }
        }
    }

    if (showAddStopDialog) {
        AddDestinationStopDialog(
            onDismiss = { showAddStopDialog = false },
            onConfirm = { name, dist, notes ->
                onAddStop(name, dist, notes)
                showAddStopDialog = false
            }
        )
    }
}

@Composable
fun RouteStopItem(
    index: Int,
    stop: RouteStop,
    onRemove: () -> Unit,
    onDistanceChange: (Double) -> Unit
) {
    var isEditingDistance by remember { mutableStateOf(false) }
    var distanceInput by remember(stop.distanceKm) { mutableStateOf(stop.distanceKm.toString()) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(FuelGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (stop.notes.isNotBlank()) {
                        Text(
                            text = stop.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditingDistance) {
                    OutlinedTextField(
                        value = distanceInput,
                        onValueChange = {
                            distanceInput = it
                            it.toDoubleOrNull()?.let { d -> onDistanceChange(d) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp)
                    )
                    IconButton(onClick = { isEditingDistance = false }) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = "Done", tint = FuelGreenPrimary)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { isEditingDistance = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${"%.1f".format(stop.distanceKm)} km",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = FuelGoldSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit km",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Stop",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDestinationStopDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, distanceKm: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var distanceKmText by remember { mutableStateOf("10.0") }
    var notes by remember { mutableStateOf("") }
    var selectedCityTab by remember { mutableStateOf("Karachi") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = FuelGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Destination Stop")
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Destination / Client Name") },
                    placeholder = { Text("e.g. Site Office, DHA Phase 6, Port Qasim") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = distanceKmText,
                    onValueChange = { distanceKmText = it },
                    label = { Text("Distance from Previous Stop (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_distance_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Purpose (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Pakistan Destination Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Karachi", "Lahore", "Islamabad", "Intercity").forEach { city ->
                        FilterChip(
                            selected = selectedCityTab == city,
                            onClick = { selectedCityTab = city },
                            label = { Text(city, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val currentPresets = when (selectedCityTab) {
                    "Karachi" -> PakistanPresets.commonKarachiStops
                    "Lahore" -> PakistanPresets.commonLahoreStops
                    "Islamabad" -> PakistanPresets.commonIslamabadStops
                    else -> PakistanPresets.intercityPresets
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentPresets.forEach { preset ->
                        AssistChip(
                            onClick = {
                                name = preset.landmark
                                if (preset.standardDistanceKm > 0) {
                                    distanceKmText = preset.standardDistanceKm.toString()
                                }
                            },
                            label = { Text("${preset.landmark} (${preset.standardDistanceKm} km)", fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dist = distanceKmText.toDoubleOrNull() ?: 1.0
                    val finalName = name.ifBlank { "Destination Stop" }
                    onConfirm(finalName, dist, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                modifier = Modifier.testTag("confirm_add_stop_btn")
            ) {
                Text("Add Stop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
