package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_receipts")
data class FuelReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeName: String,
    val stationName: String,
    val fuelType: String, // "Petrol", "Diesel", "HOBC"
    val liters: Double,
    val ratePerLiter: Double,
    val totalAmount: Double,
    val odometerReading: Double? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receiptNumber: String = "",
    val isPaid: Boolean = false,
    val paymentDateMillis: Long? = null,
    val paymentBatchId: String? = null,
    val paymentMethod: String? = null,
    val paymentReference: String? = null,
    val paidBy: String? = null
)

