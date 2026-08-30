package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.entity.VehicleEntity
import com.example.ui.components.PsoRateHeaderBanner
import com.example.ui.components.VehicleCard
import com.example.ui.dialogs.AddVehicleDialog
import com.example.ui.dialogs.GoogleDriveSyncDialog
import com.example.ui.dialogs.PsoRateSettingsDialog
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.viewmodel.FuelRecordViewModel

@Composable
fun AdminVehiclesScreen(
    viewModel: FuelRecordViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val psoRates by viewModel.psoRates.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncingPsoRates.collectAsStateWithLifecycle()

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
    var showPsoSettingsDialog by remember { mutableStateOf(false) }
    var showGoogleDriveSyncDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Admin Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = FuelGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Fleet & Rate Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set vehicle mileage parameters (e.g. 30 km/L), configure Fixed Average Rates per km, and manage Pakistan PSO fuel rates with +1.40 PKR factor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Live PSO Fuel Rates Manager Banner
            item {
                PsoRateHeaderBanner(
                    rates = psoRates,
                    isSyncing = isSyncing,
                    onSyncClick = { viewModel.syncLivePsoRates(showToast = true) },
                    onOpenSettingsClick = { showPsoSettingsDialog = true }
                )
            }

            // Google Drive & Google Sheets Sync Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FuelGreenPrimary.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FuelGreenPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = FuelGreenPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Drive (vehicle dashboard)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FuelGreenPrimary
                                )
                                Text(
                                    text = "Sync & upload data from Google Sheet",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showGoogleDriveSyncDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_open_gdrive_sync_btn")
                        ) {
                            Text("Sync Sheet", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Vehicle Fleet List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Vehicles (${vehicles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            vehicleToEdit = null
                            showAddVehicleDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_add_vehicle_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Vehicle")
                    }
                }
            }

            if (vehicles.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No vehicles configured",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add company motorcycles (30 km/L default), cars, or fixed rate vehicles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(vehicles, key = { it.id }) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onEdit = {
                            vehicleToEdit = vehicle
                            showAddVehicleDialog = true
                        },
                        onDelete = { viewModel.deleteVehicle(vehicle) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                vehicleToEdit = null
                showAddVehicleDialog = true
            },
            containerColor = FuelGreenPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("admin_add_vehicle_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Vehicle")
        }
    }

    if (showAddVehicleDialog) {
        AddVehicleDialog(
            initialVehicle = vehicleToEdit,
            knownEmployees = viewModel.knownEmployees,
            onDismiss = {
                showAddVehicleDialog = false
                vehicleToEdit = null
            },
            onConfirm = { id, name, plate, type, avg, isFixed, fixedRate, emp, fuelType ->
                viewModel.saveVehicle(id, name, plate, type, avg, isFixed, fixedRate, emp, fuelType)
                showAddVehicleDialog = false
                vehicleToEdit = null
            }
        )
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

    if (showGoogleDriveSyncDialog) {
        GoogleDriveSyncDialog(
            viewModel = viewModel,
            onDismiss = { showGoogleDriveSyncDialog = false }
        )
    }
}
