package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FuelReceiptDao
import com.example.data.dao.PaymentVoucherDao
import com.example.data.dao.PsoRateDao
import com.example.data.dao.TravelExpenseDao
import com.example.data.dao.VehicleDao
import com.example.data.entity.FuelReceiptEntity
import com.example.data.entity.PaymentVoucherEntity
import com.example.data.entity.PsoFuelRateEntity
import com.example.data.entity.TravelExpenseEntity
import com.example.data.entity.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VehicleEntity::class,
        FuelReceiptEntity::class,
        TravelExpenseEntity::class,
        PsoFuelRateEntity::class,
        PaymentVoucherEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelReceiptDao(): FuelReceiptDao
    abstract fun travelExpenseDao(): TravelExpenseDao
    abstract fun psoRateDao(): PsoRateDao
    abstract fun paymentVoucherDao(): PaymentVoucherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_record_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                // 1. Initial PSO Fuel Rates (with +1.40 PKR factor)
                val initialRates = listOf(
                    PsoFuelRateEntity(
                        fuelType = "Petrol",
                        baseRatePkr = 260.60,
                        addFactorPkr = 1.40,
                        effectiveRatePkr = 262.00,
                        lastUpdatedMillis = System.currentTimeMillis(),
                        source = "PSO Portal & OGRA Gazette (Auto-Sync)"
                    ),
                    PsoFuelRateEntity(
                        fuelType = "Diesel",
                        baseRatePkr = 265.80,
                        addFactorPkr = 1.40,
                        effectiveRatePkr = 267.20,
                        lastUpdatedMillis = System.currentTimeMillis(),
                        source = "PSO Portal & OGRA Gazette (Auto-Sync)"
                    ),
                    PsoFuelRateEntity(
                        fuelType = "HOBC",
                        baseRatePkr = 285.50,
                        addFactorPkr = 1.40,
                        effectiveRatePkr = 286.90,
                        lastUpdatedMillis = System.currentTimeMillis(),
                        source = "PSO Al-Tron Premium (Auto-Sync)"
                    )
                )
                db.psoRateDao().insertAll(initialRates)

                // 2. Initial Preconfigured Vehicles (Including 30 km/L default motorcycle)
                val initialVehicles = listOf(
                    VehicleEntity(
                        name = "Honda CD 70 (Company Bike)",
                        plateNumber = "KHI-7824",
                        type = "Bike",
                        fuelAverageKmPerLiter = 30.0, // 30 km/L as requested in prompt
                        isFixedRate = false,
                        fixedRatePerKm = 0.0,
                        assignedEmployee = "Ahmed Khan (Field Officer)",
                        defaultFuelType = "Petrol"
                    ),
                    VehicleEntity(
                        name = "Yamaha YBR 125 (Courier Bike)",
                        plateNumber = "KHI-3190",
                        type = "Bike",
                        fuelAverageKmPerLiter = 30.0, // 30 km/L default
                        isFixedRate = false,
                        fixedRatePerKm = 0.0,
                        assignedEmployee = "Bilal Tariq (Dispatch)",
                        defaultFuelType = "Petrol"
                    ),
                    VehicleEntity(
                        name = "Suzuki Alto 660cc (Sales Car)",
                        plateNumber = "LEA-5521",
                        type = "Car",
                        fuelAverageKmPerLiter = 18.5,
                        isFixedRate = false,
                        fixedRatePerKm = 0.0,
                        assignedEmployee = "Usman Farooq (Area Manager)",
                        defaultFuelType = "Petrol"
                    ),
                    VehicleEntity(
                        name = "Toyota Corolla GLi (Executive)",
                        plateNumber = "ISB-8902",
                        type = "Car",
                        fuelAverageKmPerLiter = 13.0,
                        isFixedRate = true, // Fixed average rate example for admin
                        fixedRatePerKm = 22.0, // PKR 22/km fixed allowance
                        assignedEmployee = "Zainab Malik (Operations)",
                        defaultFuelType = "Petrol"
                    ),
                    VehicleEntity(
                        name = "Suzuki Bolan (Delivery Van)",
                        plateNumber = "KHI-6091",
                        type = "Van",
                        fuelAverageKmPerLiter = 12.0,
                        isFixedRate = false,
                        fixedRatePerKm = 0.0,
                        assignedEmployee = "Rashid Mehmood (Logistics)",
                        defaultFuelType = "Petrol"
                    )
                )
                db.vehicleDao().insertAll(initialVehicles)

                // 3. Sample initial Fuel Receipts across employees
                val sampleReceipts = listOf(
                    FuelReceiptEntity(
                        employeeName = "Ahmed Khan (Field Officer)",
                        stationName = "PSO Clifton Filling Station, Karachi",
                        fuelType = "Petrol",
                        liters = 5.0,
                        ratePerLiter = 262.00,
                        totalAmount = 1310.0,
                        odometerReading = 14250.0,
                        dateMillis = System.currentTimeMillis() - 86400000L * 2,
                        notes = "Weekly field visit fuel refilling",
                        receiptNumber = "PSO-REC-4921",
                        isPaid = false
                    ),
                    FuelReceiptEntity(
                        employeeName = "Bilal Tariq (Dispatch)",
                        stationName = "PSO Shahrah-e-Faisal Pump",
                        fuelType = "Petrol",
                        liters = 4.5,
                        ratePerLiter = 262.00,
                        totalAmount = 1179.0,
                        odometerReading = 8920.0,
                        dateMillis = System.currentTimeMillis() - 86400000L * 3,
                        notes = "Daily parcel dispatch refuel",
                        receiptNumber = "PSO-REC-5512",
                        isPaid = false
                    ),
                    FuelReceiptEntity(
                        employeeName = "Usman Farooq (Area Manager)",
                        stationName = "PSO Blue Area Station, Islamabad",
                        fuelType = "Petrol",
                        liters = 20.0,
                        ratePerLiter = 262.00,
                        totalAmount = 5240.0,
                        odometerReading = 58200.0,
                        dateMillis = System.currentTimeMillis() - 86400000L * 5,
                        notes = "Client site tour refuel",
                        receiptNumber = "PSO-REC-8904",
                        isPaid = true,
                        paymentDateMillis = System.currentTimeMillis() - 86400000L * 4,
                        paymentBatchId = "PV-2026-W34-012",
                        paymentMethod = "Online Bank Transfer (1Link / IBFT)",
                        paymentReference = "IBFT-9912048",
                        paidBy = "Corporate Finance & Admin"
                    )
                )
                db.fuelReceiptDao().insertAll(sampleReceipts)

                // 4. Sample initial Travel Logs with multi-day expenses
                val destinations1 = """[{"id":"1","name":"Head Office (I.I. Chundrigar)","distanceKm":0.0},{"id":"2","name":"Clifton DHA Hub","distanceKm":8.5},{"id":"3","name":"Port Qasim Industrial","distanceKm":38.0},{"id":"4","name":"Return to Head Office","distanceKm":34.5}]"""
                val destinations2 = """[{"id":"1","name":"Head Office","distanceKm":0.0},{"id":"2","name":"Korangi Industrial Sector","distanceKm":19.5},{"id":"3","name":"S.I.T.E Area Hub","distanceKm":24.0},{"id":"4","name":"Return to Office","distanceKm":21.0}]"""
                val destinations3 = """[{"id":"1","name":"Gulberg Corporate Center","distanceKm":0.0},{"id":"2","name":"Sundar Industrial Estate","distanceKm":32.0},{"id":"3","name":"DHA Phase 5 Tech Hub","distanceKm":28.0}]"""

                val sampleExpenses = listOf(
                    TravelExpenseEntity(
                        employeeName = "Ahmed Khan (Field Officer)",
                        vehicleId = 1,
                        vehicleName = "Honda CD 70 (Company Bike)",
                        vehicleType = "Bike",
                        dateMillis = System.currentTimeMillis() - 86400000L * 1, // Yesterday
                        purpose = "Client Inspection & Delivery at Port Qasim",
                        startLocation = "Head Office (I.I. Chundrigar)",
                        destinationsJson = destinations1,
                        totalDistanceKm = 81.0,
                        fuelAverageKmPerLiter = 30.0, // 30 km/L standard
                        fuelRatePerLiter = 262.00,
                        fuelLitersConsumed = 2.70,
                        calculatedExpensePkr = 707.40,
                        rateTypeDescription = "PSO Petrol @ PKR 260.60 + 1.40 factor = PKR 262.00/L (30.0 km/L)",
                        status = "Pending",
                        isPaid = false
                    ),
                    TravelExpenseEntity(
                        employeeName = "Ahmed Khan (Field Officer)",
                        vehicleId = 1,
                        vehicleName = "Honda CD 70 (Company Bike)",
                        vehicleType = "Bike",
                        dateMillis = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
                        purpose = "Korangi & S.I.T.E Vendor Meetings",
                        startLocation = "Head Office",
                        destinationsJson = destinations2,
                        totalDistanceKm = 64.5,
                        fuelAverageKmPerLiter = 30.0,
                        fuelRatePerLiter = 262.00,
                        fuelLitersConsumed = 2.15,
                        calculatedExpensePkr = 563.30,
                        rateTypeDescription = "PSO Petrol @ PKR 260.60 + 1.40 factor = PKR 262.00/L (30.0 km/L)",
                        status = "Pending",
                        isPaid = false
                    ),
                    TravelExpenseEntity(
                        employeeName = "Bilal Tariq (Dispatch)",
                        vehicleId = 2,
                        vehicleName = "Yamaha YBR 125 (Courier Bike)",
                        vehicleType = "Bike",
                        dateMillis = System.currentTimeMillis() - 86400000L * 1,
                        purpose = "City Wide Express Parcel Deliveries",
                        startLocation = "Dispatch Hub",
                        destinationsJson = destinations2,
                        totalDistanceKm = 52.0,
                        fuelAverageKmPerLiter = 30.0,
                        fuelRatePerLiter = 262.00,
                        fuelLitersConsumed = 1.73,
                        calculatedExpensePkr = 454.13,
                        rateTypeDescription = "PSO Petrol @ PKR 260.60 + 1.40 factor = PKR 262.00/L (30.0 km/L)",
                        status = "Pending",
                        isPaid = false
                    ),
                    TravelExpenseEntity(
                        employeeName = "Usman Farooq (Area Manager)",
                        vehicleId = 3,
                        vehicleName = "Suzuki Alto 660cc (Sales Car)",
                        vehicleType = "Car",
                        dateMillis = System.currentTimeMillis() - 86400000L * 6,
                        purpose = "Sundar & DHA Corporate Sales Tour",
                        startLocation = "Gulberg Corporate Center",
                        destinationsJson = destinations3,
                        totalDistanceKm = 60.0,
                        fuelAverageKmPerLiter = 18.5,
                        fuelRatePerLiter = 262.00,
                        fuelLitersConsumed = 3.24,
                        calculatedExpensePkr = 849.73,
                        rateTypeDescription = "PSO Petrol @ PKR 260.60 + 1.40 factor = PKR 262.00/L (18.5 km/L)",
                        status = "Paid",
                        isPaid = true,
                        paymentDateMillis = System.currentTimeMillis() - 86400000L * 4,
                        paymentBatchId = "PV-2026-W34-012",
                        paymentMethod = "Online Bank Transfer (1Link / IBFT)",
                        paymentReference = "IBFT-9912048",
                        paidBy = "Corporate Finance & Admin"
                    )
                )
                db.travelExpenseDao().insertAll(sampleExpenses)

                // 5. Sample Past Payment Voucher
                val sampleVoucher = PaymentVoucherEntity(
                    batchId = "PV-2026-W34-012",
                    employeeName = "Usman Farooq (Area Manager)",
                    paymentDateMillis = System.currentTimeMillis() - 86400000L * 4,
                    totalAmountPkr = 6089.73,
                    tripExpenseCount = 1,
                    receiptCount = 1,
                    paymentMethod = "Online Bank Transfer (1Link / IBFT)",
                    referenceNumber = "IBFT-9912048",
                    paidBy = "Corporate Finance & Admin",
                    periodDescription = "Previous Week Settled Expense Batch",
                    notes = "Bank settlement transfer to employee salary account via HBL"
                )
                db.paymentVoucherDao().insertVoucher(sampleVoucher)
            }
        }
    }
}

