package com.example.data.sync

import android.util.Log
import com.example.data.entity.FuelReceiptEntity
import com.example.data.entity.TravelExpenseEntity
import com.example.data.entity.VehicleEntity
import com.example.data.repository.FuelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class SheetImportResult(
    val vehiclesImported: Int = 0,
    val tripsImported: Int = 0,
    val receiptsImported: Int = 0,
    val rawRowsProcessed: Int = 0,
    val success: Boolean = true,
    val message: String = ""
)

class GoogleDriveSheetsSyncService(
    private val fuelRepository: FuelRepository,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GDriveSheetsSync"
    }

    /**
     * Extracts Google Spreadsheet ID from full URL or returns the ID directly.
     */
    fun extractSheetId(input: String): String {
        val trimmed = input.trim()
        val matcher = Pattern.compile("/d/([a-zA-Z0-9-_]+)").matcher(trimmed)
        return if (matcher.find()) {
            matcher.group(1) ?: trimmed
        } else {
            trimmed
        }
    }

    /**
     * Fetches CSV from a public or shared Google Spreadsheet URL / ID.
     */
    suspend fun fetchSheetCsv(sheetIdOrUrl: String, gid: String = "0"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sheetId = extractSheetId(sheetIdOrUrl)
            val exportUrl = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=$gid"
            
            Log.d(TAG, "Fetching Google Sheet from: $exportUrl")
            val request = Request.Builder()
                .url(exportUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to load sheet (HTTP ${response.code}). Make sure the Google Sheet in 'vehicle dashboard' is set to 'Anyone with the link can view'.")
                )
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank() || body.contains("<!DOCTYPE html>", ignoreCase = true)) {
                return@withContext Result.failure(
                    Exception("Sheet returned HTML login page. Please check sharing permissions (Share -> Anyone with the link can view).")
                )
            }

            Result.success(body)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sheet", e)
            Result.failure(e)
        }
    }

    /**
     * Parses CSV / Tabular content and populates database.
     * Auto-detects columns for Vehicles, Travel Logs, or Fuel Receipts.
     */
    suspend fun importCsvContent(csvData: String, defaultEmployee: String = "Admin"): SheetImportResult = withContext(Dispatchers.IO) {
        var vehiclesCount = 0
        var tripsCount = 0
        var receiptsCount = 0
        var rowsProcessed = 0

        val lines = csvData.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return@withContext SheetImportResult(message = "Sheet is empty or has no readable rows", success = false)
        }

        val header = parseCsvLine(lines.first().lowercase())
        val isVehicleSheet = header.any { it.contains("plate") || it.contains("vehicle") || it.contains("model") || it.contains("km/l") || it.contains("average") }
        val isReceiptSheet = header.any { it.contains("receipt") || it.contains("station") || it.contains("liters") || it.contains("fuel rate") || it.contains("pump") }
        val isTripSheet = header.any { it.contains("trip") || it.contains("destination") || it.contains("from") || it.contains("to") || it.contains("distance") }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateFormatAlt = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val dateFormatAlt2 = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)

        for (i in 1 until lines.size) {
            val line = lines[i]
            val cols = parseCsvLine(line)
            if (cols.isEmpty() || cols.all { it.isBlank() }) continue
            rowsProcessed++

            try {
                // If it's a Vehicle table or contains plate/model info
                if (isVehicleSheet && !isReceiptSheet && !isTripSheet) {
                    val plateIndex = header.indexOfFirst { it.contains("plate") || it.contains("reg") || it.contains("number") }
                    val modelIndex = header.indexOfFirst { it.contains("model") || it.contains("name") || it.contains("vehicle") }
                    val avgIndex = header.indexOfFirst { it.contains("km/l") || it.contains("average") || it.contains("mileage") }
                    val rateIndex = header.indexOfFirst { it.contains("fixed") || it.contains("rate") || it.contains("pkr/km") }
                    val fuelTypeIndex = header.indexOfFirst { it.contains("fuel") || it.contains("type") }

                    val plate = cols.getOrNull(if (plateIndex >= 0) plateIndex else 0)?.trim().orEmpty()
                    val model = cols.getOrNull(if (modelIndex >= 0) modelIndex else 1)?.trim().orEmpty()
                    val avgKml = cols.getOrNull(if (avgIndex >= 0) avgIndex else 2)?.toDoubleOrNull() ?: 30.0
                    val fixedRate = cols.getOrNull(if (rateIndex >= 0) rateIndex else 3)?.toDoubleOrNull()
                    val fuelType = cols.getOrNull(if (fuelTypeIndex >= 0) fuelTypeIndex else 4)?.trim() ?: "Petrol"

                    if (plate.isNotBlank() || model.isNotBlank()) {
                        val entity = VehicleEntity(
                            name = model.ifBlank { "Vehicle $plate" },
                            plateNumber = plate.ifBlank { "VEH-${System.currentTimeMillis() % 1000}" },
                            type = if (avgKml >= 25.0) "Bike" else "Car",
                            fuelAverageKmPerLiter = avgKml,
                            isFixedRate = (fixedRate != null && fixedRate > 0),
                            fixedRatePerKm = fixedRate ?: 0.0,
                            assignedEmployee = defaultEmployee,
                            defaultFuelType = fuelType
                        )
                        fuelRepository.insertVehicle(entity)
                        vehiclesCount++
                    }
                }
                // If it's a Fuel Receipts table
                else if (isReceiptSheet) {
                    val empIndex = header.indexOfFirst { it.contains("employee") || it.contains("driver") || it.contains("user") || it.contains("name") }
                    val stationIndex = header.indexOfFirst { it.contains("station") || it.contains("pump") || it.contains("vendor") }
                    val litersIndex = header.indexOfFirst { it.contains("liter") || it.contains("quantity") || it.contains("volume") }
                    val rateIndex = header.indexOfFirst { it.contains("rate") || it.contains("price") }
                    val amountIndex = header.indexOfFirst { it.contains("total") || it.contains("amount") || it.contains("pkr") }
                    val dateIndex = header.indexOfFirst { it.contains("date") }
                    val recNoIndex = header.indexOfFirst { it.contains("rec") || it.contains("invoice") || it.contains("bill") }

                    val empName = cols.getOrNull(if (empIndex >= 0) empIndex else 0)?.trim().orEmpty().ifBlank { defaultEmployee }
                    val station = cols.getOrNull(if (stationIndex >= 0) stationIndex else 1)?.trim().orEmpty().ifBlank { "PSO Station" }
                    val liters = cols.getOrNull(if (litersIndex >= 0) litersIndex else 2)?.toDoubleOrNull() ?: 5.0
                    val rate = cols.getOrNull(if (rateIndex >= 0) rateIndex else 3)?.toDoubleOrNull() ?: 268.50
                    val amount = cols.getOrNull(if (amountIndex >= 0) amountIndex else 4)?.toDoubleOrNull() ?: (liters * rate)
                    val recNo = cols.getOrNull(if (recNoIndex >= 0) recNoIndex else 5)?.trim().orEmpty().ifBlank { "REC-${System.currentTimeMillis() % 10000}" }

                    val dateStr = cols.getOrNull(if (dateIndex >= 0) dateIndex else 6)?.trim().orEmpty()
                    val dateMillis = parseDateToMillis(dateStr, dateFormat, dateFormatAlt, dateFormatAlt2)

                    val entity = FuelReceiptEntity(
                        employeeName = empName,
                        stationName = station,
                        fuelType = "Petrol",
                        liters = liters,
                        ratePerLiter = rate,
                        totalAmount = amount,
                        odometerReading = null,
                        dateMillis = dateMillis,
                        notes = "Imported from GDrive/Sheets (vehicle dashboard)",
                        receiptNumber = recNo,
                        isPaid = false
                    )
                    fuelRepository.insertReceipt(entity)
                    receiptsCount++
                }
                // General or Trip table
                else {
                    val empIndex = header.indexOfFirst { it.contains("employee") || it.contains("driver") || it.contains("user") || it.contains("name") }
                    val fromIndex = header.indexOfFirst { it.contains("from") || it.contains("start") || it.contains("origin") }
                    val toIndex = header.indexOfFirst { it.contains("to") || it.contains("dest") || it.contains("stop") }
                    val distIndex = header.indexOfFirst { it.contains("distance") || it.contains("km") }
                    val totalIndex = header.indexOfFirst { it.contains("total") || it.contains("amount") || it.contains("pkr") || it.contains("expense") }
                    val dateIndex = header.indexOfFirst { it.contains("date") }

                    val empName = cols.getOrNull(if (empIndex >= 0) empIndex else 0)?.trim().orEmpty().ifBlank { defaultEmployee }
                    val origin = cols.getOrNull(if (fromIndex >= 0) fromIndex else 1)?.trim().orEmpty().ifBlank { "Office" }
                    val dest = cols.getOrNull(if (toIndex >= 0) toIndex else 2)?.trim().orEmpty().ifBlank { "Client Site" }
                    val distance = cols.getOrNull(if (distIndex >= 0) distIndex else 3)?.toDoubleOrNull() ?: 15.0
                    val total = cols.getOrNull(if (totalIndex >= 0) totalIndex else 4)?.toDoubleOrNull() ?: (distance * 9.5)

                    val dateStr = cols.getOrNull(if (dateIndex >= 0) dateIndex else 5)?.trim().orEmpty()
                    val dateMillis = parseDateToMillis(dateStr, dateFormat, dateFormatAlt, dateFormatAlt2)

                    val entity = TravelExpenseEntity(
                        employeeName = empName,
                        vehicleId = 1L,
                        vehicleName = "Standard Vehicle (30 km/L)",
                        vehicleType = "Bike",
                        dateMillis = dateMillis,
                        purpose = "Duty travel (from GDrive Sheet)",
                        startLocation = origin,
                        destinationsJson = "[{\"name\":\"$dest\",\"distanceKm\":$distance}]",
                        totalDistanceKm = distance,
                        fuelAverageKmPerLiter = 30.0,
                        fuelRatePerLiter = 268.50,
                        fuelLitersConsumed = distance / 30.0,
                        calculatedExpensePkr = total,
                        rateTypeDescription = "PSO Petrol @ 268.50 PKR/L (30.0 km/L standard)",
                        status = "Pending",
                        isPaid = false
                    )
                    fuelRepository.insertTravelExpense(entity)
                    tripsCount++
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed parsing row $i: $line", e)
            }
        }

        SheetImportResult(
            vehiclesImported = vehiclesCount,
            tripsImported = tripsCount,
            receiptsImported = receiptsCount,
            rawRowsProcessed = rowsProcessed,
            success = true,
            message = "Successfully imported from Google Sheets: $tripsCount trips, $receiptsCount receipts, $vehiclesCount vehicles."
        )
    }

    private fun parseDateToMillis(
        dateStr: String,
        vararg formats: SimpleDateFormat
    ): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        for (fmt in formats) {
            try {
                val parsed = fmt.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return System.currentTimeMillis()
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if ((c == ',' || c == '\t') && !inQuotes) {
                result.add(cur.toString())
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString())
        return result
    }
}
