package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.FuelReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelReceiptDao {
    @Query("SELECT * FROM fuel_receipts ORDER BY dateMillis DESC")
    fun getAllReceipts(): Flow<List<FuelReceiptEntity>>

    @Query("SELECT * FROM fuel_receipts WHERE employeeName = :employeeName ORDER BY dateMillis DESC")
    fun getReceiptsByEmployee(employeeName: String): Flow<List<FuelReceiptEntity>>

    @Query("SELECT * FROM fuel_receipts WHERE isPaid = 0 ORDER BY dateMillis DESC")
    fun getUnpaidReceipts(): Flow<List<FuelReceiptEntity>>

    @Query("SELECT * FROM fuel_receipts WHERE employeeName = :employeeName AND isPaid = 0 ORDER BY dateMillis DESC")
    fun getUnpaidReceiptsByEmployee(employeeName: String): Flow<List<FuelReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: FuelReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<FuelReceiptEntity>)

    @Update
    suspend fun updateReceipt(receipt: FuelReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: FuelReceiptEntity)

    @Query("""
        UPDATE fuel_receipts 
        SET isPaid = 1, paymentDateMillis = :paymentDateMillis, 
            paymentBatchId = :batchId, paymentMethod = :paymentMethod, 
            paymentReference = :reference, paidBy = :paidBy 
        WHERE id IN (:ids) AND isPaid = 0
    """)
    suspend fun markReceiptsAsPaid(
        ids: List<Long>,
        paymentDateMillis: Long,
        batchId: String,
        paymentMethod: String,
        reference: String,
        paidBy: String
    ): Int

    @Query("SELECT SUM(totalAmount) FROM fuel_receipts")
    fun getTotalFuelAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM fuel_receipts WHERE employeeName = :employeeName")
    fun getTotalFuelAmountByEmployeeFlow(employeeName: String): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM fuel_receipts WHERE isPaid = 1")
    fun getTotalPaidFuelAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM fuel_receipts WHERE isPaid = 0")
    fun getTotalUnpaidFuelAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM fuel_receipts")
    fun getTotalLitersFlow(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM fuel_receipts WHERE employeeName = :employeeName")
    fun getTotalLitersByEmployeeFlow(employeeName: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM fuel_receipts")
    suspend fun getCount(): Int
}

