package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.PsoFuelRateEntity
import com.example.data.entity.VehicleEntity
import com.example.model.FuelType
import com.example.ui.components.PsoRateHeaderBanner
import com.example.ui.components.RouteStopBuilder
import com.example.ui.components.TripCalculationCard
import com.example.ui.dialogs.PsoRateSettingsDialog
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.viewmodel.FuelRecordViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TravelCalculatorScreen(
    viewModel: FuelRecordViewModel,
    modifier: Modifier = Modifier
) {
    val calculatorState by viewModel.calculatorState.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val psoRates by viewModel.psoRates.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncingPsoRates.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

    var showPsoSettingsDialog by remember { mutableStateOf(false) }

    val calcResult = viewModel.calculateCurrentTrip()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Pakistan PSO Fuel Rate Banner
        PsoRateHeaderBanner(
            rates = psoRates,
            isSyncing = isSyncing,
            onSyncClick = { viewModel.syncLivePsoRates(showToast = true) },
            onOpenSettingsClick = { showPsoSettingsDialog = true }
        )

        // 2. Employee Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FuelGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdmin) "Select Employee (Admin Mode)" else "Logged-in Employee",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isAdmin) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.knownEmployees.forEach { emp ->
                            val isSelected = calculatorState.employeeName == emp
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCalculatorEmployee(emp) },
                                label = { Text(emp, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FuelGreenPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = FuelGreenPrimary
                                ),
                                modifier = Modifier.testTag("employee_chip_${emp.take(5)}")
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FuelGreenPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentUser.name} (${currentUser.department})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FuelGreenPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = calculatorState.purpose,
                    onValueChange = { viewModel.setCalculatorPurpose(it) },
                    label = { Text("Trip Purpose / Duty Assignment") },
                    placeholder = { Text("e.g. Client inspection, delivery, vendor meeting") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Work, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_purpose_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // 3. Vehicle Selection Card (Default 30 km/L)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = FuelGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Vehicle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FuelGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Standard: 30 km/L",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FuelGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (vehicles.isEmpty()) {
                    Text(
                        text = "Loading vehicles...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vehicles.forEach { vehicle ->
                            val isSelected = (calculatorState.selectedVehicleId == vehicle.id) ||
                                    (calculatorState.selectedVehicleId == null && vehicles.firstOrNull()?.id == vehicle.id)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FuelGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FuelGreenPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setCalculatorVehicle(vehicle.id) }
                                    .testTag("vehicle_select_${vehicle.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (vehicle.type.contains("Bike", ignoreCase = true)) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isSelected) FuelGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = vehicle.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (vehicle.isFixedRate) "Fixed Rate: PKR ${"%.1f".format(vehicle.fixedRatePerKm)}/km" else "PSO Rate (${vehicle.defaultFuelType})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (vehicle.isFixedRate) FuelGoldSecondary else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) FuelGreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${"%.0f".format(vehicle.fuelAverageKmPerLiter)} km/L",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Multi-Destination Route Stop Planner (N Stops)
        RouteStopBuilder(
            startLocation = calculatorState.startLocation,
            onStartLocationChange = { viewModel.setCalculatorStartLocation(it) },
            stops = calculatorState.stops,
            onAddStop = { name, dist, notes -> viewModel.addStop(name, dist, notes) },
            onRemoveStop = { viewModel.removeStop(it) },
            onUpdateStopDistance = { id, dist -> viewModel.updateStopDistance(id, dist) },
            isRoundTrip = calculatorState.isRoundTrip,
            onRoundTripChange = { viewModel.setCalculatorRoundTrip(it) },
            totalDistanceKm = calcResult.totalDistanceKm
        )

        // 5. Automated Travel Expense Calculation Summary
        TripCalculationCard(
            calculationResult = calcResult,
            onSaveTripClick = { viewModel.saveCurrentTripExpense() }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showPsoSettingsDialog) {
        PsoRateSettingsDialog(
            rates = psoRates,
            isSyncing = isSyncing,
            onSyncNow = { viewModel.syncLivePsoRates(showToast = true) },
            onSaveAddFactor = { viewModel.updateGlobalAddFactor(it) },
            onSaveCustomRate = { viewModel.updatePsoRate(it) },
            onDismiss = { showPsoSettingsDialog = false }
        )
    }
}
