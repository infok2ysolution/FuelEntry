package com.example.data.repository

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
import com.example.data.network.PsoRateService
import kotlinx.coroutines.flow.Flow

class FuelRepository(
    private val vehicleDao: VehicleDao,
    private val fuelReceiptDao: FuelReceiptDao,
    private val travelExpenseDao: TravelExpenseDao,
    private val psoRateDao: PsoRateDao,
    private val paymentVoucherDao: PaymentVoucherDao,
    private val psoRateService: PsoRateService = PsoRateService()
) {
    // Vehicles
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    suspend fun insertVehicle(vehicle: VehicleEntity): Long = vehicleDao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: VehicleEntity) = vehicleDao.updateVehicle(vehicle)
    suspend fun deleteVehicle(vehicle: VehicleEntity) = vehicleDao.deleteVehicle(vehicle)
    suspend fun getVehicleById(id: Long): VehicleEntity? = vehicleDao.getVehicleById(id)

    // Receipts
    val allReceipts: Flow<List<FuelReceiptEntity>> = fuelReceiptDao.getAllReceipts()
    fun getReceiptsByEmployee(employeeName: String): Flow<List<FuelReceiptEntity>> =
        fuelReceiptDao.getReceiptsByEmployee(employeeName)
    fun getUnpaidReceipts(): Flow<List<FuelReceiptEntity>> = fuelReceiptDao.getUnpaidReceipts()
    fun getUnpaidReceiptsByEmployee(employeeName: String): Flow<List<FuelReceiptEntity>> =
        fuelReceiptDao.getUnpaidReceiptsByEmployee(employeeName)
    suspend fun insertReceipt(receipt: FuelReceiptEntity): Long = fuelReceiptDao.insertReceipt(receipt)
    suspend fun deleteReceipt(receipt: FuelReceiptEntity) = fuelReceiptDao.deleteReceipt(receipt)
    val totalFuelSpentFlow: Flow<Double?> = fuelReceiptDao.getTotalFuelAmountFlow()
    fun totalFuelSpentByEmployeeFlow(employeeName: String): Flow<Double?> =
        fuelReceiptDao.getTotalFuelAmountByEmployeeFlow(employeeName)
    val totalLitersFlow: Flow<Double?> = fuelReceiptDao.getTotalLitersFlow()
    fun totalLitersByEmployeeFlow(employeeName: String): Flow<Double?> =
        fuelReceiptDao.getTotalLitersByEmployeeFlow(employeeName)
    val totalUnpaidFuelAmountFlow: Flow<Double?> = fuelReceiptDao.getTotalUnpaidFuelAmountFlow()

    // Travel Expenses
    val allTravelExpenses: Flow<List<TravelExpenseEntity>> = travelExpenseDao.getAllExpenses()
    fun getTravelExpensesByEmployee(employeeName: String): Flow<List<TravelExpenseEntity>> =
        travelExpenseDao.getExpensesByEmployee(employeeName)
    fun getUnpaidTravelExpenses(): Flow<List<TravelExpenseEntity>> = travelExpenseDao.getUnpaidExpenses()
    fun getUnpaidTravelExpensesByEmployee(employeeName: String): Flow<List<TravelExpenseEntity>> =
        travelExpenseDao.getUnpaidExpensesByEmployee(employeeName)
    suspend fun insertTravelExpense(expense: TravelExpenseEntity): Long =
        travelExpenseDao.insertExpense(expense)
    suspend fun updateTravelExpense(expense: TravelExpenseEntity) =
        travelExpenseDao.updateExpense(expense)
    suspend fun deleteTravelExpense(expense: TravelExpenseEntity) =
        travelExpenseDao.deleteExpense(expense)
    suspend fun updateExpenseStatus(id: Long, status: String) =
        travelExpenseDao.updateStatus(id, status)
    val totalTravelExpenseFlow: Flow<Double?> = travelExpenseDao.getTotalExpenseAmountFlow()
    fun totalTravelExpenseByEmployeeFlow(employeeName: String): Flow<Double?> =
        travelExpenseDao.getTotalExpenseAmountByEmployeeFlow(employeeName)
    val totalDistanceTravelledFlow: Flow<Double?> = travelExpenseDao.getTotalDistanceKmFlow()
    fun totalDistanceTravelledByEmployeeFlow(employeeName: String): Flow<Double?> =
        travelExpenseDao.getTotalDistanceKmByEmployeeFlow(employeeName)
    val totalUnpaidTravelExpenseFlow: Flow<Double?> = travelExpenseDao.getTotalUnpaidExpenseAmountFlow()

    // Payments & Disbursements
    val allVouchers: Flow<List<PaymentVoucherEntity>> = paymentVoucherDao.getAllVouchers()
    fun getVouchersByEmployee(employeeName: String): Flow<List<PaymentVoucherEntity>> =
        paymentVoucherDao.getVouchersByEmployee(employeeName)
    val totalDisbursedPkrFlow: Flow<Double?> = paymentVoucherDao.getTotalDisbursedPkrFlow()

    suspend fun processBatchPayment(
        employeeName: String,
        tripIds: List<Long>,
        receiptIds: List<Long>,
        totalAmountPkr: Double,
        paymentMethod: String,
        referenceNumber: String,
        periodDescription: String,
        notes: String,
        paidBy: String = "Corporate Finance & Admin"
    ): Result<PaymentVoucherEntity> {
        val now = System.currentTimeMillis()
        val batchId = "PV-" + (now % 10000000)

        // Mark trips as paid (Only unpaid ones are updated to enforce once-paid constraint)
        if (tripIds.isNotEmpty()) {
            travelExpenseDao.markExpensesAsPaid(
                ids = tripIds,
                paymentDateMillis = now,
                batchId = batchId,
                paymentMethod = paymentMethod,
                reference = referenceNumber,
                paidBy = paidBy
            )
        }

        // Mark receipts as paid (Only unpaid ones are updated)
        if (receiptIds.isNotEmpty()) {
            fuelReceiptDao.markReceiptsAsPaid(
                ids = receiptIds,
                paymentDateMillis = now,
                batchId = batchId,
                paymentMethod = paymentMethod,
                reference = referenceNumber,
                paidBy = paidBy
            )
        }

        // Insert Voucher
        val voucher = PaymentVoucherEntity(
            batchId = batchId,
            employeeName = employeeName,
            paymentDateMillis = now,
            totalAmountPkr = totalAmountPkr,
            tripExpenseCount = tripIds.size,
            receiptCount = receiptIds.size,
            paymentMethod = paymentMethod,
            referenceNumber = referenceNumber,
            paidBy = paidBy,
            periodDescription = periodDescription,
            notes = notes
        )
        paymentVoucherDao.insertVoucher(voucher)
        return Result.success(voucher)
    }

    // PSO Fuel Rates
    val allPsoRates: Flow<List<PsoFuelRateEntity>> = psoRateDao.getAllRatesFlow()
    suspend fun getRateByType(fuelType: String): PsoFuelRateEntity? = psoRateDao.getRateByType(fuelType)
    fun getRateByTypeFlow(fuelType: String): Flow<PsoFuelRateEntity?> = psoRateDao.getRateByTypeFlow(fuelType)
    suspend fun updateRate(rate: PsoFuelRateEntity) = psoRateDao.update(rate)
    suspend fun insertOrUpdateRate(rate: PsoFuelRateEntity) = psoRateDao.insertOrUpdate(rate)
    suspend fun updateGlobalAddFactor(factor: Double) = psoRateDao.updateGlobalAddFactor(factor)

    suspend fun syncPsoRates(currentAddFactor: Double = 1.40): Result<List<PsoFuelRateEntity>> {
        val result = psoRateService.fetchLivePsoRates(currentAddFactor)
        if (result.isSuccess) {
            val rates = result.getOrNull().orEmpty()
            if (rates.isNotEmpty()) {
                psoRateDao.insertAll(rates)
            }
        }
        return result
    }
}

