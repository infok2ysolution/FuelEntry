package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val plateNumber: String,
    val type: String, // "Bike", "Car", "Van", "Truck"
    val fuelAverageKmPerLiter: Double = 30.0, // Default 30 km/L
    val isFixedRate: Boolean = false, // If true, calculates based on fixed PKR per km
    val fixedRatePerKm: Double = 0.0, // e.g. 10 PKR/km
    val assignedEmployee: String = "",
    val defaultFuelType: String = "Petrol"
)
