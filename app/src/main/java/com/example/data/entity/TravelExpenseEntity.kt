package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_expenses")
data class TravelExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeName: String,
    val vehicleId: Long,
    val vehicleName: String,
    val vehicleType: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val purpose: String,
    val startLocation: String,
    val destinationsJson: String, // JSON array of stops
    val totalDistanceKm: Double,
    val fuelAverageKmPerLiter: Double, // e.g. 30.0 km/L
    val fuelRatePerLiter: Double, // Effective rate including +1.4 PKR
    val fuelLitersConsumed: Double,
    val calculatedExpensePkr: Double,
    val rateTypeDescription: String,
    val status: String = "Pending", // "Pending", "Approved", "Paid"
    val isPaid: Boolean = false,
    val paymentDateMillis: Long? = null,
    val paymentBatchId: String? = null, // e.g. "PV-2026-08-001"
    val paymentMethod: String? = null,
    val paymentReference: String? = null,
    val paidBy: String? = null
)

