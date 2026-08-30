package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.FuelReceiptEntity
import com.example.data.entity.PaymentVoucherEntity
import com.example.data.entity.PsoFuelRateEntity
import com.example.data.entity.TravelExpenseEntity
import com.example.data.entity.VehicleEntity
import com.example.data.repository.FuelRepository
import com.example.data.sync.GoogleDriveSheetsSyncService
import com.example.data.sync.SheetImportResult
import com.example.model.AppUsers
import com.example.model.FuelType
import com.example.model.RouteStop
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TravelCalculatorState(
    val employeeName: String = "Ahmed Khan (Field Officer)",
    val selectedVehicleId: Long? = null,
    val purpose: String = "Client Visit & Delivery",
    val startLocation: String = "Head Office (I.I. Chundrigar)",
    val stops: List<RouteStop> = listOf(
        RouteStop(name = "Clifton DHA Hub", distanceKm = 8.5),
        RouteStop(name = "Port Qasim Industrial", distanceKm = 38.0)
    ),
    val isRoundTrip: Boolean = true,
    val selectedFuelType: FuelType = FuelType.PETROL,
    val customMileageKmPerLiter: Double? = null, // if null, uses vehicle's mileage (default 30 km/L)
    val customFixedRatePerKm: Double? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class FuelRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FuelRepository(
        vehicleDao = database.vehicleDao(),
        fuelReceiptDao = database.fuelReceiptDao(),
        travelExpenseDao = database.travelExpenseDao(),
        psoRateDao = database.psoRateDao(),
        paymentVoucherDao = database.paymentVoucherDao()
    )

    // User authentication / active profile
    private val _currentUser = MutableStateFlow<UserProfile>(AppUsers.EMPLOYEES.first())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    val isAdmin: StateFlow<Boolean> = _currentUser
        .combine(MutableStateFlow(Unit)) { user, _ -> user.role == UserRole.ADMIN_FINANCE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Filter for Admin view (allows selecting a specific employee or "All")
    private val _selectedEmployeeFilter = MutableStateFlow("All")
    val selectedEmployeeFilter: StateFlow<String> = _selectedEmployeeFilter.asStateFlow()

    // Syncing state & notifications
    private val _isSyncingPsoRates = MutableStateFlow(false)
    val isSyncingPsoRates: StateFlow<Boolean> = _isSyncingPsoRates.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Travel calculator working state
    private val _calculatorState = MutableStateFlow(
        TravelCalculatorState(employeeName = AppUsers.EMPLOYEES.first().name)
    )
    val calculatorState: StateFlow<TravelCalculatorState> = _calculatorState.asStateFlow()

    // Room flows - Master datasets
    val vehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val psoRates: StateFlow<List<PsoFuelRateEntity>> = repository.allPsoRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<FuelReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTravelExpenses: StateFlow<List<TravelExpenseEntity>> = repository.allTravelExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVouchers: StateFlow<List<PaymentVoucherEntity>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Role-Scoped Filtered Flows
    // If User is Employee: ONLY sees their own records!
    // If User is Admin: Sees entire company, filtered by selectedEmployeeFilter if set!
    val visibleTravelExpenses: StateFlow<List<TravelExpenseEntity>> = combine(
        _currentUser,
        _selectedEmployeeFilter,
        repository.allTravelExpenses
    ) { user, filter, list ->
        if (user.role == UserRole.EMPLOYEE) {
            list.filter { it.employeeName.contains(user.name.substringBefore("(").trim(), ignoreCase = true) }
        } else {
            if (filter == "All") list
            else list.filter { it.employeeName.contains(filter.substringBefore("(").trim(), ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleReceipts: StateFlow<List<FuelReceiptEntity>> = combine(
        _currentUser,
        _selectedEmployeeFilter,
        repository.allReceipts
    ) { user, filter, list ->
        if (user.role == UserRole.EMPLOYEE) {
            list.filter { it.employeeName.contains(user.name.substringBefore("(").trim(), ignoreCase = true) }
        } else {
            if (filter == "All") list
            else list.filter { it.employeeName.contains(filter.substringBefore("(").trim(), ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleVouchers: StateFlow<List<PaymentVoucherEntity>> = combine(
        _currentUser,
        _selectedEmployeeFilter,
        repository.allVouchers
    ) { user, filter, list ->
        if (user.role == UserRole.EMPLOYEE) {
            list.filter { it.employeeName.contains(user.name.substringBefore("(").trim(), ignoreCase = true) }
        } else {
            if (filter == "All") list
            else list.filter { it.employeeName.contains(filter.substringBefore("(").trim(), ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for visible scope
    val totalFuelSpent: StateFlow<Double> = visibleReceipts.combine(MutableStateFlow(0.0)) { list, _ ->
        list.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLiters: StateFlow<Double> = visibleReceipts.combine(MutableStateFlow(0.0)) { list, _ ->
        list.sumOf { it.liters }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalTravelExpenses: StateFlow<Double> = visibleTravelExpenses.combine(MutableStateFlow(0.0)) { list, _ ->
        list.sumOf { it.calculatedExpensePkr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDistanceKm: StateFlow<Double> = visibleTravelExpenses.combine(MutableStateFlow(0.0)) { list, _ ->
        list.sumOf { it.totalDistanceKm }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val knownEmployees: List<String> = AppUsers.EMPLOYEES.map { it.name }

    init {
        // Automatically fetch live rates on startup if needed
        syncLivePsoRates(showToast = false)
        // Bind default vehicle if available
        viewModelScope.launch {
            vehicles.collect { vList ->
                if (_calculatorState.value.selectedVehicleId == null && vList.isNotEmpty()) {
                    val assigned = vList.firstOrNull { it.assignedEmployee.contains(_currentUser.value.name.substringBefore("(").trim(), ignoreCase = true) }
                    if (assigned != null) {
                        _calculatorState.update { it.copy(selectedVehicleId = assigned.id) }
                    }
                }
            }
        }
    }

    fun switchUser(user: UserProfile) {
        _currentUser.value = user
        if (user.role == UserRole.EMPLOYEE) {
            _selectedEmployeeFilter.value = user.name
            _calculatorState.update { it.copy(employeeName = user.name) }
            // Auto select matching vehicle
            val matchedVehicle = vehicles.value.firstOrNull {
                it.assignedEmployee.contains(user.name.substringBefore("(").trim(), ignoreCase = true)
            }
            if (matchedVehicle != null) {
                _calculatorState.update { it.copy(selectedVehicleId = matchedVehicle.id) }
            }
            viewModelScope.launch {
                _toastEvent.emit("Switched to Employee View: ${user.name.substringBefore("(").trim()} (${user.department})")
            }
        } else {
            _selectedEmployeeFilter.value = "All"
            viewModelScope.launch {
                _toastEvent.emit("Switched to Admin / Finance Full Access View")
            }
        }
    }

    fun setEmployeeFilter(filter: String) {
        _selectedEmployeeFilter.value = filter
    }

    fun syncLivePsoRates(showToast: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingPsoRates.value = true
            val currentAddFactor = psoRates.value.firstOrNull()?.addFactorPkr ?: 1.40
            val result = repository.syncPsoRates(currentAddFactor)
            _isSyncingPsoRates.value = false
            if (showToast) {
                if (result.isSuccess) {
                    _toastEvent.emit("PSO fuel rates successfully updated with +Rs $currentAddFactor factor")
                } else {
                    _toastEvent.emit("Using latest Pakistan PSO official rate baseline (+Rs $currentAddFactor factor)")
                }
            }
        }
    }

    fun updateGlobalAddFactor(newFactor: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGlobalAddFactor(newFactor)
            _toastEvent.emit("Add factor updated to Rs $newFactor per liter")
        }
    }

    fun updatePsoRate(rate: PsoFuelRateEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdateRate(rate)
            _toastEvent.emit("${rate.fuelType} rate updated: Rs ${rate.effectiveRatePkr}/L")
        }
    }

    // Travel Calculator actions
    fun setCalculatorEmployee(name: String) {
        _calculatorState.update { it.copy(employeeName = name) }
    }

    fun setCalculatorVehicle(vehicleId: Long) {
        _calculatorState.update { it.copy(selectedVehicleId = vehicleId) }
    }

    fun setCalculatorPurpose(purpose: String) {
        _calculatorState.update { it.copy(purpose = purpose) }
    }

    fun setCalculatorStartLocation(start: String) {
        _calculatorState.update { it.copy(startLocation = start) }
    }

    fun setCalculatorFuelType(fuelType: FuelType) {
        _calculatorState.update { it.copy(selectedFuelType = fuelType) }
    }

    fun setCalculatorMileage(mileage: Double?) {
        _calculatorState.update { it.copy(customMileageKmPerLiter = mileage) }
    }

    fun setCalculatorRoundTrip(isRoundTrip: Boolean) {
        _calculatorState.update { it.copy(isRoundTrip = isRoundTrip) }
    }

    fun addStop(name: String, distanceKm: Double, notes: String = "") {
        _calculatorState.update {
            it.copy(stops = it.stops + RouteStop(name = name, distanceKm = distanceKm, notes = notes))
        }
    }

    fun removeStop(stopId: String) {
        _calculatorState.update {
            it.copy(stops = it.stops.filter { stop -> stop.id != stopId })
        }
    }

    fun updateStopDistance(stopId: String, newDistance: Double) {
        _calculatorState.update {
            it.copy(stops = it.stops.map { stop ->
                if (stop.id == stopId) stop.copy(distanceKm = newDistance) else stop
            })
        }
    }

    fun clearStops() {
        _calculatorState.update { it.copy(stops = emptyList()) }
    }

    fun calculateCurrentTrip(): TripCalculationResult {
        val state = _calculatorState.value
        val vehicle = vehicles.value.firstOrNull { it.id == state.selectedVehicleId }
            ?: vehicles.value.firstOrNull()

        val rawStopsDistance = state.stops.sumOf { it.distanceKm }
        val totalDistance = if (state.isRoundTrip) rawStopsDistance * 2 else rawStopsDistance

        // Determine rate & mileage
        val mileageKmPerLiter = state.customMileageKmPerLiter
            ?: vehicle?.fuelAverageKmPerLiter
            ?: 30.0 // Default 30 km/L as requested in prompt

        val fuelTypeStr = vehicle?.defaultFuelType ?: state.selectedFuelType.shortCode
        val psoRateEntity = psoRates.value.firstOrNull { it.fuelType.equals(fuelTypeStr, ignoreCase = true) }
            ?: PsoFuelRateEntity(
                fuelType = fuelTypeStr,
                baseRatePkr = state.selectedFuelType.defaultBaseRatePkr,
                addFactorPkr = 1.40,
                effectiveRatePkr = state.selectedFuelType.defaultBaseRatePkr + 1.40
            )

        val isFixedRate = vehicle?.isFixedRate == true
        val fixedRatePerKm = vehicle?.fixedRatePerKm ?: 0.0

        val (expensePkr, litersConsumed, rateDescription) = if (isFixedRate && fixedRatePerKm > 0.0) {
            val expense = totalDistance * fixedRatePerKm
            val liters = if (mileageKmPerLiter > 0) totalDistance / mileageKmPerLiter else 0.0
            Triple(expense, liters, "Fixed Rate @ PKR ${"%.2f".format(fixedRatePerKm)}/km (Vehicle: ${vehicle?.name})")
        } else {
            val liters = if (mileageKmPerLiter > 0) totalDistance / mileageKmPerLiter else 0.0
            val expense = liters * psoRateEntity.effectiveRatePkr
            val desc = "PSO $fuelTypeStr @ PKR ${"%.2f".format(psoRateEntity.baseRatePkr)} + Rs ${"%.2f".format(psoRateEntity.addFactorPkr)} = PKR ${"%.2f".format(psoRateEntity.effectiveRatePkr)}/L (${"%.1f".format(mileageKmPerLiter)} km/L)"
            Triple(expense, liters, desc)
        }

        return TripCalculationResult(
            totalDistanceKm = totalDistance,
            mileageKmPerLiter = mileageKmPerLiter,
            fuelLitersConsumed = litersConsumed,
            fuelRatePerLiter = psoRateEntity.effectiveRatePkr,
            calculatedExpensePkr = expensePkr,
            rateDescription = rateDescription,
            vehicle = vehicle
        )
    }

    fun saveCurrentTripExpense() {
        val state = _calculatorState.value
        val calc = calculateCurrentTrip()
        if (calc.totalDistanceKm <= 0) {
            viewModelScope.launch { _toastEvent.emit("Please add destinations with distance > 0 km") }
            return
        }

        val effectiveEmployee = if (_currentUser.value.role == UserRole.EMPLOYEE) {
            _currentUser.value.name
        } else {
            state.employeeName
        }

        val stopsJsonArray = JSONArray()
        stopsJsonArray.put(JSONObject().apply {
            put("id", "start")
            put("name", state.startLocation)
            put("distanceKm", 0.0)
        })
        state.stops.forEach { stop ->
            stopsJsonArray.put(JSONObject().apply {
                put("id", stop.id)
                put("name", stop.name)
                put("distanceKm", stop.distanceKm)
                put("notes", stop.notes)
            })
        }
        if (state.isRoundTrip) {
            stopsJsonArray.put(JSONObject().apply {
                put("id", "return")
                put("name", "Return to ${state.startLocation}")
                put("distanceKm", state.stops.sumOf { it.distanceKm })
            })
        }

        val expense = TravelExpenseEntity(
            employeeName = effectiveEmployee,
            vehicleId = calc.vehicle?.id ?: 0,
            vehicleName = calc.vehicle?.name ?: "Personal Vehicle",
            vehicleType = calc.vehicle?.type ?: "Bike",
            dateMillis = System.currentTimeMillis(),
            purpose = state.purpose.ifBlank { "Official Duty Travel" },
            startLocation = state.startLocation,
            destinationsJson = stopsJsonArray.toString(),
            totalDistanceKm = calc.totalDistanceKm,
            fuelAverageKmPerLiter = calc.mileageKmPerLiter,
            fuelRatePerLiter = calc.fuelRatePerLiter,
            fuelLitersConsumed = calc.fuelLitersConsumed,
            calculatedExpensePkr = calc.calculatedExpensePkr,
            rateTypeDescription = calc.rateDescription,
            status = "Pending",
            isPaid = false
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTravelExpense(expense)
            _toastEvent.emit("Trip recorded! Expense: PKR ${"%.2f".format(calc.calculatedExpensePkr)} (Pending Finance Approval)")
        }
    }

    // Receipt actions
    fun addFuelReceipt(
        employeeName: String,
        stationName: String,
        fuelType: String,
        liters: Double,
        ratePerLiter: Double,
        odometerReading: Double?,
        dateMillis: Long,
        notes: String,
        receiptNumber: String
    ) {
        val effectiveEmployee = if (_currentUser.value.role == UserRole.EMPLOYEE) {
            _currentUser.value.name
        } else {
            employeeName
        }

        val totalAmount = liters * ratePerLiter
        val receipt = FuelReceiptEntity(
            employeeName = effectiveEmployee,
            stationName = stationName,
            fuelType = fuelType,
            liters = liters,
            ratePerLiter = ratePerLiter,
            totalAmount = totalAmount,
            odometerReading = odometerReading,
            dateMillis = dateMillis,
            notes = notes,
            receiptNumber = receiptNumber,
            isPaid = false
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertReceipt(receipt)
            _toastEvent.emit("Fuel Receipt saved: PKR ${"%.2f".format(totalAmount)} ($liters L)")
        }
    }

    fun deleteReceipt(receipt: FuelReceiptEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReceipt(receipt)
            _toastEvent.emit("Receipt deleted")
        }
    }

    fun deleteTravelExpense(expense: TravelExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTravelExpense(expense)
            _toastEvent.emit("Trip expense log deleted")
        }
    }

    fun updateExpenseStatus(id: Long, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExpenseStatus(id, status)
            _toastEvent.emit("Status updated to $status")
        }
    }

    // Batch Payment Disbursement (Admin / Finance Feature)
    // Ensures: "Admin or finance will make payment multiple days or weekly exps data once made could not be paid again"
    fun processPaymentDisbursement(
        employeeName: String,
        selectedTripIds: List<Long>,
        selectedReceiptIds: List<Long>,
        totalAmount: Double,
        paymentMethod: String,
        referenceNumber: String,
        periodDescription: String,
        notes: String
    ) {
        if (selectedTripIds.isEmpty() && selectedReceiptIds.isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("Please select at least one unpaid expense or receipt to disburse payment.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.processBatchPayment(
                employeeName = employeeName,
                tripIds = selectedTripIds,
                receiptIds = selectedReceiptIds,
                totalAmountPkr = totalAmount,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber.ifBlank { "FT-${System.currentTimeMillis() % 1000000}" },
                periodDescription = periodDescription.ifBlank { "Multi-Day Travel Expense Settlement" },
                notes = notes,
                paidBy = _currentUser.value.name
            )

            if (result.isSuccess) {
                val voucher = result.getOrThrow()
                _toastEvent.emit("Payment of PKR ${"%.2f".format(totalAmount)} disbursed to $employeeName! Voucher: ${voucher.batchId}. Items marked PAID.")
            } else {
                _toastEvent.emit("Payment failed to process. Please try again.")
            }
        }
    }

    // Google Drive & Google Sheets Sync
    private val gDriveSyncService = GoogleDriveSheetsSyncService(repository)
    private val _isSyncingSheet = MutableStateFlow(false)
    val isSyncingSheet: StateFlow<Boolean> = _isSyncingSheet.asStateFlow()

    private val _sheetSyncResult = MutableStateFlow<SheetImportResult?>(null)
    val sheetSyncResult: StateFlow<SheetImportResult?> = _sheetSyncResult.asStateFlow()

    // Sync from public/shared Google Sheet URL or ID (from folder 'vehicle dashboard')
    fun syncWithGoogleDriveSheet(sheetUrlOrId: String, defaultEmployee: String = _currentUser.value.name) {
        if (sheetUrlOrId.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("Please enter a Google Sheet URL or Sheet ID") }
            return
        }
        viewModelScope.launch {
            _isSyncingSheet.value = true
            try {
                val csvResult = gDriveSyncService.fetchSheetCsv(sheetUrlOrId)
                if (csvResult.isSuccess) {
                    val csvData = csvResult.getOrNull().orEmpty()
                    val importResult = gDriveSyncService.importCsvContent(csvData, defaultEmployee)
                    _sheetSyncResult.value = importResult
                    _toastEvent.emit(importResult.message)
                } else {
                    val errorMsg = csvResult.exceptionOrNull()?.message ?: "Failed to sync sheet"
                    _sheetSyncResult.value = SheetImportResult(success = false, message = errorMsg)
                    _toastEvent.emit(errorMsg)
                }
            } catch (e: Exception) {
                _sheetSyncResult.value = SheetImportResult(success = false, message = e.message ?: "Unknown error")
                _toastEvent.emit("Error syncing Google Sheet: ${e.localizedMessage}")
            } finally {
                _isSyncingSheet.value = false
            }
        }
    }

    // Direct manual paste import from Google Sheet / CSV
    fun importRawSheetData(rawCsvOrTsv: String, defaultEmployee: String = _currentUser.value.name) {
        if (rawCsvOrTsv.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("No spreadsheet data provided") }
            return
        }
        viewModelScope.launch {
            _isSyncingSheet.value = true
            try {
                val importResult = gDriveSyncService.importCsvContent(rawCsvOrTsv, defaultEmployee)
                _sheetSyncResult.value = importResult
                _toastEvent.emit(importResult.message)
            } catch (e: Exception) {
                _sheetSyncResult.value = SheetImportResult(success = false, message = e.message ?: "Error importing data")
                _toastEvent.emit("Import error: ${e.localizedMessage}")
            } finally {
                _isSyncingSheet.value = false
            }
        }
    }

    fun clearSyncResult() {
        _sheetSyncResult.value = null
    }

    // Generate CSV data for export to Google Drive / Sheets
    fun generateGoogleSheetExportData(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val sb = StringBuilder()
        sb.append("Type,Date,Employee,Vehicle/Station,Origin/FuelType,Destination/Liters,Distance/Rate,Total Amount (PKR),Status,Payment Ref\n")

        val trips = allTravelExpenses.value
        for (trip in trips) {
            val dateStr = dateFormat.format(Date(trip.dateMillis))
            sb.append("TRIP,\"${dateStr}\",\"${trip.employeeName}\",\"${trip.vehicleName}\",\"${trip.startLocation}\",\"${trip.destinationsJson}\",\"${trip.totalDistanceKm} km\",${"%.2f".format(trip.calculatedExpensePkr)},\"${if (trip.isPaid) "PAID" else trip.status}\",\"${trip.paymentBatchId ?: ""}\"\n")
        }

        val receipts = allReceipts.value
        for (rec in receipts) {
            val dateStr = dateFormat.format(Date(rec.dateMillis))
            sb.append("RECEIPT,\"${dateStr}\",\"${rec.employeeName}\",\"${rec.stationName}\",\"${rec.fuelType}\",\"${rec.liters} L\",\"${rec.ratePerLiter} PKR/L\",${"%.2f".format(rec.totalAmount)},\"${if (rec.isPaid) "PAID" else "Pending"}\",\"${rec.paymentBatchId ?: ""}\"\n")
        }

        return sb.toString()
    }

    // Vehicle Management
    fun saveVehicle(
        id: Long = 0,
        name: String,
        plateNumber: String,
        type: String,
        fuelAverageKmPerLiter: Double,
        isFixedRate: Boolean,
        fixedRatePerKm: Double,
        assignedEmployee: String,
        defaultFuelType: String
    ) {
        val vehicle = VehicleEntity(
            id = id,
            name = name,
            plateNumber = plateNumber,
            type = type,
            fuelAverageKmPerLiter = fuelAverageKmPerLiter,
            isFixedRate = isFixedRate,
            fixedRatePerKm = fixedRatePerKm,
            assignedEmployee = assignedEmployee,
            defaultFuelType = defaultFuelType
        )
        viewModelScope.launch(Dispatchers.IO) {
            if (id == 0L) {
                repository.insertVehicle(vehicle)
                _toastEvent.emit("Vehicle '$name' added successfully")
            } else {
                repository.updateVehicle(vehicle)
                _toastEvent.emit("Vehicle '$name' updated")
            }
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVehicle(vehicle)
            _toastEvent.emit("Vehicle removed")
        }
    }
}

data class TripCalculationResult(
    val totalDistanceKm: Double,
    val mileageKmPerLiter: Double,
    val fuelLitersConsumed: Double,
    val fuelRatePerLiter: Double,
    val calculatedExpensePkr: Double,
    val rateDescription: String,
    val vehicle: VehicleEntity?
)

