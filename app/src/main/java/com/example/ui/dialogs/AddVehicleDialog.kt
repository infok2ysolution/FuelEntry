package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.VehicleEntity
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddVehicleDialog(
    initialVehicle: VehicleEntity? = null,
    knownEmployees: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Long,
        name: String,
        plateNumber: String,
        type: String,
        fuelAverageKmPerLiter: Double,
        isFixedRate: Boolean,
        fixedRatePerKm: Double,
        assignedEmployee: String,
        defaultFuelType: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialVehicle?.name ?: "Honda CD 70") }
    var plateNumber by remember { mutableStateOf(initialVehicle?.plateNumber ?: "KHI-${(1000..9999).random()}") }
    var type by remember { mutableStateOf(initialVehicle?.type ?: "Bike") }
    var fuelAverageText by remember { mutableStateOf(initialVehicle?.fuelAverageKmPerLiter?.toString() ?: "30.0") }
    var isFixedRate by remember { mutableStateOf(initialVehicle?.isFixedRate ?: false) }
    var fixedRateText by remember { mutableStateOf(initialVehicle?.fixedRatePerKm?.toString() ?: "12.0") }
    var assignedEmployee by remember { mutableStateOf(initialVehicle?.assignedEmployee ?: knownEmployees.firstOrNull() ?: "") }
    var defaultFuelType by remember { mutableStateOf(initialVehicle?.defaultFuelType ?: "Petrol") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (type == "Bike") Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = FuelGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (initialVehicle == null) "Admin: Add Vehicle" else "Admin: Edit Vehicle")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vehicle Presets
                Text(text = "Quick Vehicle Preset:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        Triple("Honda CD 70 (Bike)", "Bike", "30.0"),
                        Triple("Yamaha YBR (Bike)", "Bike", "30.0"),
                        Triple("Suzuki Alto 660cc", "Car", "18.5"),
                        Triple("Toyota Corolla", "Car", "12.5"),
                        Triple("Suzuki Bolan Van", "Van", "11.0")
                    ).forEach { (presetName, presetType, avg) ->
                        AssistChip(
                            onClick = {
                                name = presetName
                                type = presetType
                                fuelAverageText = avg
                            },
                            label = { Text("$presetName ($avg km/L)", fontSize = 10.sp) }
                        )
                    }
                }

                // Vehicle Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vehicle Name / Model") },
                    placeholder = { Text("e.g. Honda CD 70, Suzuki Alto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Plate Number
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = { Text("Number Plate / Registration") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_plate_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Vehicle Type
                Text(text = "Vehicle Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Bike", "Car", "Van", "Truck").forEach { cat ->
                        FilterChip(
                            selected = type == cat,
                            onClick = {
                                type = cat
                                if (cat == "Bike" && fuelAverageText == "12.5") fuelAverageText = "30.0"
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                // Fuel Average (Km per liter) - with 30 km/L preset buttons
                OutlinedTextField(
                    value = fuelAverageText,
                    onValueChange = { fuelAverageText = it },
                    label = { Text("Fuel Average (km per liter)") },
                    supportingText = { Text("Standard bike setting: 30 km/L") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_mileage_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("30.0", "25.0", "18.0", "14.0", "11.0").forEach { avg ->
                        AssistChip(
                            onClick = { fuelAverageText = avg },
                            label = { Text("$avg km/L", fontSize = 11.sp) }
                        )
                    }
                }

                // Rate Calculation Mode: Dynamic vs Fixed Average Rate
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isFixedRate = !isFixedRate },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fixed Average Rate Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isFixedRate) "Admin set fixed PKR per km reimbursement" else "Dynamic PSO market fuel rate (+1.4 factor)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Switch(
                                checked = isFixedRate,
                                onCheckedChange = { isFixedRate = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = FuelGoldSecondary),
                                modifier = Modifier.testTag("fixed_rate_switch")
                            )
                        }

                        if (isFixedRate) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = fixedRateText,
                                onValueChange = { fixedRateText = it },
                                label = { Text("Fixed Rate (PKR per km)") },
                                placeholder = { Text("e.g. 10.0 or 15.0 PKR/km") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fixed_rate_km_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Fuel Type
                Text(text = "Default Fuel Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Petrol", "Diesel", "HOBC").forEach { ft ->
                        FilterChip(
                            selected = defaultFuelType == ft,
                            onClick = { defaultFuelType = ft },
                            label = { Text(ft) }
                        )
                    }
                }

                // Assigned Employee
                Text(text = "Assigned Employee:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (listOf("General Pool / Any") + knownEmployees).forEach { emp ->
                        FilterChip(
                            selected = assignedEmployee == emp,
                            onClick = { assignedEmployee = emp },
                            label = { Text(emp.substringBefore("(").trim(), fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val avgVal = fuelAverageText.toDoubleOrNull() ?: 30.0
                    val fixedVal = fixedRateText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(
                            initialVehicle?.id ?: 0L,
                            name,
                            plateNumber,
                            type,
                            avgVal,
                            isFixedRate,
                            if (isFixedRate) fixedVal else 0.0,
                            assignedEmployee,
                            defaultFuelType
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                modifier = Modifier.testTag("confirm_save_vehicle_btn")
            ) {
                Text(if (initialVehicle == null) "Add Vehicle" else "Save Vehicle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
