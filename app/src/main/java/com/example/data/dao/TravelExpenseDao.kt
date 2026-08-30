package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TravelExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelExpenseDao {
    @Query("SELECT * FROM travel_expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<TravelExpenseEntity>>

    @Query("SELECT * FROM travel_expenses WHERE employeeName = :employeeName ORDER BY dateMillis DESC")
    fun getExpensesByEmployee(employeeName: String): Flow<List<TravelExpenseEntity>>

    @Query("SELECT * FROM travel_expenses WHERE isPaid = 0 ORDER BY dateMillis DESC")
    fun getUnpaidExpenses(): Flow<List<TravelExpenseEntity>>

    @Query("SELECT * FROM travel_expenses WHERE employeeName = :employeeName AND isPaid = 0 ORDER BY dateMillis DESC")
    fun getUnpaidExpensesByEmployee(employeeName: String): Flow<List<TravelExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: TravelExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<TravelExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: TravelExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: TravelExpenseEntity)

    @Query("UPDATE travel_expenses SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("""
        UPDATE travel_expenses 
        SET isPaid = 1, status = 'Paid', paymentDateMillis = :paymentDateMillis, 
            paymentBatchId = :batchId, paymentMethod = :paymentMethod, 
            paymentReference = :reference, paidBy = :paidBy 
        WHERE id IN (:ids) AND isPaid = 0
    """)
    suspend fun markExpensesAsPaid(
        ids: List<Long>,
        paymentDateMillis: Long,
        batchId: String,
        paymentMethod: String,
        reference: String,
        paidBy: String
    ): Int

    @Query("SELECT SUM(calculatedExpensePkr) FROM travel_expenses")
    fun getTotalExpenseAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(calculatedExpensePkr) FROM travel_expenses WHERE employeeName = :employeeName")
    fun getTotalExpenseAmountByEmployeeFlow(employeeName: String): Flow<Double?>

    @Query("SELECT SUM(calculatedExpensePkr) FROM travel_expenses WHERE isPaid = 1")
    fun getTotalPaidExpenseAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(calculatedExpensePkr) FROM travel_expenses WHERE isPaid = 0")
    fun getTotalUnpaidExpenseAmountFlow(): Flow<Double?>

    @Query("SELECT SUM(totalDistanceKm) FROM travel_expenses")
    fun getTotalDistanceKmFlow(): Flow<Double?>

    @Query("SELECT SUM(totalDistanceKm) FROM travel_expenses WHERE employeeName = :employeeName")
    fun getTotalDistanceKmByEmployeeFlow(employeeName: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM travel_expenses")
    suspend fun getCount(): Int
}

