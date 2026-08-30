package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReceiptDialog(
    currentEmployee: String,
    knownEmployees: List<String>,
    psoRates: List<PsoFuelRateEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        employeeName: String,
        stationName: String,
        fuelType: String,
        liters: Double,
        ratePerLiter: Double,
        odometerReading: Double?,
        dateMillis: Long,
        notes: String,
        receiptNumber: String
    ) -> Unit
) {
    var employee by remember { mutableStateOf(currentEmployee.ifBlank { knownEmployees.firstOrNull() ?: "Employee" }) }
    var stationName by remember { mutableStateOf("PSO Service Station") }
    var fuelType by remember { mutableStateOf("Petrol") }
    var litersText by remember { mutableStateOf("10.0") }

    // Auto calculate initial rate from PSO rates
    val defaultRate = psoRates.firstOrNull { it.fuelType.equals("Petrol", ignoreCase = true) }?.effectiveRatePkr ?: 262.00
    var rateText by remember { mutableStateOf(defaultRate.toString()) }
    var odometerText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var receiptNumber by remember { mutableStateOf("REC-${(1000..9999).random()}") }

    val litersVal = litersText.toDoubleOrNull() ?: 0.0
    val rateVal = rateText.toDoubleOrNull() ?: 0.0
    val totalAmount = litersVal * rateVal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = FuelGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Gas Station Receipt")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Employee Selector
                Text(text = "Employee:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    knownEmployees.forEach { emp ->
                        FilterChip(
                            selected = employee == emp,
                            onClick = { employee = emp },
                            label = { Text(emp.substringBefore("(").trim(), fontSize = 11.sp) }
                        )
                    }
                }

                // Station Name
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Gas Station Name") },
                    placeholder = { Text("e.g. PSO Clifton, PSO Blue Area") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receipt_station_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Station quick suggestions
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("PSO Clifton, Karachi", "PSO Blue Area, ISB", "PSO Gulberg, LHR", "Shell Station", "Total Parco").forEach { s ->
                        AssistChip(
                            onClick = { stationName = s },
                            label = { Text(s, fontSize = 10.sp) }
                        )
                    }
                }

                // Fuel Type Selection
                Text(text = "Fuel Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Petrol", "Diesel", "HOBC").forEach { type ->
                        FilterChip(
                            selected = fuelType == type,
                            onClick = {
                                fuelType = type
                                val eff = psoRates.firstOrNull { it.fuelType.equals(type, ignoreCase = true) }?.effectiveRatePkr
                                if (eff != null) {
                                    rateText = eff.toString()
                                }
                            },
                            label = { Text(type) }
                        )
                    }
                }

                // Liters & Rate Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = { litersText = it },
                        label = { Text("Liters") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("receipt_liters_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Rate / Liter (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("receipt_rate_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Total Amount preview card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FuelGreenPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Receipt Amount:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "PKR ${"%.2f".format(totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = FuelGreenPrimary
                        )
                    }
                }

                // Odometer reading
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it },
                    label = { Text("Odometer Reading (Optional km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Receipt No & Notes
                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("Receipt Slip # / Bill ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (litersVal > 0 && rateVal > 0) {
                        onConfirm(
                            employee,
                            stationName.ifBlank { "Gas Station" },
                            fuelType,
                            litersVal,
                            rateVal,
                            odometerText.toDoubleOrNull(),
                            System.currentTimeMillis(),
                            notes,
                            receiptNumber
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                modifier = Modifier.testTag("save_receipt_confirm_btn")
            ) {
                Text("Save Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
