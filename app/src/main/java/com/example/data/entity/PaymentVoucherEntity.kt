package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_vouchers")
data class PaymentVoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: String, // e.g. "PV-2026-08-001"
    val employeeName: String,
    val paymentDateMillis: Long = System.currentTimeMillis(),
    val totalAmountPkr: Double,
    val tripExpenseCount: Int,
    val receiptCount: Int,
    val paymentMethod: String, // "Bank Transfer (1Link/IBFT)", "Cash Voucher", etc.
    val referenceNumber: String, // e.g. "FT-98214-PK"
    val paidBy: String = "Corporate Finance & Admin",
    val periodDescription: String = "", // e.g. "Weekly Reimbursement (Aug 22 - Aug 29)"
    val notes: String = ""
)
