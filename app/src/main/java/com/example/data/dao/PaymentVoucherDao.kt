package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.PaymentVoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentVoucherDao {
    @Query("SELECT * FROM payment_vouchers ORDER BY paymentDateMillis DESC")
    fun getAllVouchers(): Flow<List<PaymentVoucherEntity>>

    @Query("SELECT * FROM payment_vouchers WHERE employeeName = :employeeName ORDER BY paymentDateMillis DESC")
    fun getVouchersByEmployee(employeeName: String): Flow<List<PaymentVoucherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: PaymentVoucherEntity): Long

    @Delete
    suspend fun deleteVoucher(voucher: PaymentVoucherEntity)

    @Query("SELECT SUM(totalAmountPkr) FROM payment_vouchers")
    fun getTotalDisbursedPkrFlow(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM payment_vouchers")
    suspend fun getCount(): Int
}
