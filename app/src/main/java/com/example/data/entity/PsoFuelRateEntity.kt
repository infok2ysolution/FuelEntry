package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pso_fuel_rates")
data class PsoFuelRateEntity(
    @PrimaryKey
    val fuelType: String, // "Petrol", "Diesel", "HOBC"
    val baseRatePkr: Double,
    val addFactorPkr: Double = 1.40, // Specifically requested: "+1.4 rupees add factor"
    val effectiveRatePkr: Double = baseRatePkr + addFactorPkr,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val source: String = "PSO Official Portal (Auto-fetch)",
    val isManualOverride: Boolean = false
)
