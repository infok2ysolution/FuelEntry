package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.entity.PsoFuelRateEntity
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.PsoGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PsoRateSettingsDialog(
    rates: List<PsoFuelRateEntity>,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
    onSaveAddFactor: (newFactor: Double) -> Unit,
    onSaveCustomRate: (PsoFuelRateEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val petrolRate = rates.firstOrNull { it.fuelType.equals("Petrol", ignoreCase = true) }
    val dieselRate = rates.firstOrNull { it.fuelType.equals("Diesel", ignoreCase = true) }
    val hobcRate = rates.firstOrNull { it.fuelType.equals("HOBC", ignoreCase = true) }

    val currentAddFactor = petrolRate?.addFactorPkr ?: 1.40
    var addFactorText by remember(currentAddFactor) { mutableStateOf(currentAddFactor.toString()) }

    var petrolBaseText by remember(petrolRate?.baseRatePkr) { mutableStateOf((petrolRate?.baseRatePkr ?: 260.60).toString()) }
    var dieselBaseText by remember(dieselRate?.baseRatePkr) { mutableStateOf((dieselRate?.baseRatePkr ?: 265.80).toString()) }
    var hobcBaseText by remember(hobcRate?.baseRatePkr) { mutableStateOf((hobcRate?.baseRatePkr ?: 285.50).toString()) }

    val factorVal = addFactorText.toDoubleOrNull() ?: 1.40
    val pBase = petrolBaseText.toDoubleOrNull() ?: 260.60
    val dBase = dieselBaseText.toDoubleOrNull() ?: 265.80
    val hBase = hobcBaseText.toDoubleOrNull() ?: 285.50

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val lastUpdated = dateFormat.format(Date(petrolRate?.lastUpdatedMillis ?: System.currentTimeMillis()))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = PsoGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pakistan PSO Fuel Rates & Factor")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Auto Sync Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PsoGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live PSO Portal Sync",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PsoGreen
                            )
                            Text(
                                text = "Last synced: $lastUpdated",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = onSyncNow,
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = PsoGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("dialog_sync_now_btn")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fetch")
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Add Factor (1.4 Rupees per liter)
                Text(
                    text = "Add Factor / Surcharge per Liter:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = addFactorText,
                    onValueChange = { addFactorText = it },
                    label = { Text("Add Factor (PKR / Liter)") },
                    supportingText = { Text("Default: +1.40 PKR added to PSO base rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_factor_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                HorizontalDivider()

                // Individual Fuel Rates
                Text(
                    text = "Fuel Base Rates (PKR / Liter):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Petrol
                FuelRateInputRow(
                    title = "Petrol (Super 92)",
                    baseValue = petrolBaseText,
                    onBaseChange = { petrolBaseText = it },
                    effective = pBase + factorVal
                )

                // Diesel
                FuelRateInputRow(
                    title = "High Speed Diesel (HSD)",
                    baseValue = dieselBaseText,
                    onBaseChange = { dieselBaseText = it },
                    effective = dBase + factorVal
                )

                // HOBC
                FuelRateInputRow(
                    title = "Hi-Octane / HOBC",
                    baseValue = hobcBaseText,
                    onBaseChange = { hobcBaseText = it },
                    effective = hBase + factorVal
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAddFactor(factorVal)
                    // Save individual rates
                    onSaveCustomRate(
                        PsoFuelRateEntity(
                            fuelType = "Petrol",
                            baseRatePkr = pBase,
                            addFactorPkr = factorVal,
                            effectiveRatePkr = pBase + factorVal,
                            lastUpdatedMillis = System.currentTimeMillis(),
                            source = "Manual / PSO Sync + Rs $factorVal factor",
                            isManualOverride = true
                        )
                    )
                    onSaveCustomRate(
                        PsoFuelRateEntity(
                            fuelType = "Diesel",
                            baseRatePkr = dBase,
                            addFactorPkr = factorVal,
                            effectiveRatePkr = dBase + factorVal,
                            lastUpdatedMillis = System.currentTimeMillis(),
                            source = "Manual / PSO Sync + Rs $factorVal factor",
                            isManualOverride = true
                        )
                    )
                    onSaveCustomRate(
                        PsoFuelRateEntity(
                            fuelType = "HOBC",
                            baseRatePkr = hBase,
                            addFactorPkr = factorVal,
                            effectiveRatePkr = hBase + factorVal,
                            lastUpdatedMillis = System.currentTimeMillis(),
                            source = "Manual / PSO Sync + Rs $factorVal factor",
                            isManualOverride = true
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                modifier = Modifier.testTag("save_pso_settings_btn")
            ) {
                Text("Save Rates & Factor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun FuelRateInputRow(
    title: String,
    baseValue: String,
    onBaseChange: (String) -> Unit,
    effective: Double
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Effective: PKR ${"%.2f".format(effective)}/L",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FuelGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = baseValue,
                onValueChange = onBaseChange,
                label = { Text("Base PSO Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}
