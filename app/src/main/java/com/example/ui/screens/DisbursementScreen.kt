package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PaymentVoucherEntity
import com.example.model.AppUsers
import com.example.model.UserRole
import com.example.ui.components.ReceiptCard
import com.example.ui.components.TripHistoryCard
import com.example.ui.dialogs.ProcessPaymentDialog
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusReimbursed
import com.example.ui.viewmodel.FuelRecordViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DisbursementScreen(
    viewModel: FuelRecordViewModel,
    onNavigateToHome: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val visibleExpenses by viewModel.visibleTravelExpenses.collectAsState()
    val visibleReceipts by viewModel.visibleReceipts.collectAsState()
    val visibleVouchers by viewModel.visibleVouchers.collectAsState()
    val selectedFilter by viewModel.selectedEmployeeFilter.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Pending Settlement, 1: Paid Vouchers
    var showPaymentDialog by remember { mutableStateOf(false) }

    // Multi-selection states for batch settlement
    val selectedTripIds = remember { mutableStateListOf<Long>() }
    val selectedReceiptIds = remember { mutableStateListOf<Long>() }

    // Filter unpaid items for processing
    val unpaidExpenses = remember(visibleExpenses) { visibleExpenses.filter { !it.isPaid } }
    val unpaidReceipts = remember(visibleReceipts) { visibleReceipts.filter { !it.isPaid } }

    // Calculate selected totals
    val selectedTripsTotal = remember(selectedTripIds.toList(), unpaidExpenses) {
        unpaidExpenses.filter { it.id in selectedTripIds }.sumOf { it.calculatedExpensePkr }
    }
    val selectedReceiptsTotal = remember(selectedReceiptIds.toList(), unpaidReceipts) {
        unpaidReceipts.filter { it.id in selectedReceiptIds }.sumOf { it.totalAmount }
    }
    val grandSelectedTotal = selectedTripsTotal + selectedReceiptsTotal

    // Determine target employee name for batch settlement
    val targetEmployeeName = if (isAdmin) {
        if (selectedFilter != "All") selectedFilter
        else unpaidExpenses.firstOrNull { it.id in selectedTripIds }?.employeeName
            ?: unpaidReceipts.firstOrNull { it.id in selectedReceiptIds }?.employeeName
            ?: "All Selected Employees"
    } else {
        currentUser.name
    }

    if (showPaymentDialog) {
        ProcessPaymentDialog(
            employeeName = targetEmployeeName,
            tripCount = selectedTripIds.size,
            receiptCount = selectedReceiptIds.size,
            totalAmountPkr = grandSelectedTotal,
            onConfirmPayment = { method, ref, period, notes ->
                viewModel.processPaymentDisbursement(
                    employeeName = targetEmployeeName,
                    selectedTripIds = selectedTripIds.toList(),
                    selectedReceiptIds = selectedReceiptIds.toList(),
                    totalAmount = grandSelectedTotal,
                    paymentMethod = method,
                    referenceNumber = ref,
                    periodDescription = period,
                    notes = notes
                )
                selectedTripIds.clear()
                selectedReceiptIds.clear()
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Banner: Role & Disbursement Authority
        Surface(
            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAdmin) FuelGoldSecondary.copy(alpha = 0.15f) else FuelGreenPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AccountBalance else Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = if (isAdmin) FuelGoldSecondary else FuelGreenPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAdmin) "Finance Expense Settlement" else "My Travel Reimbursements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAdmin) "Weekly & Multi-Day Batch Payments" else "Tracking Status & Paid Vouchers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAdmin) FuelGoldSecondary.copy(alpha = 0.15f) else FuelGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isAdmin) "FINANCE / ADMIN" else "USER VIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAdmin) FuelGoldSecondary else FuelGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // If Admin: Employee Filter Row
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Payee:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )

                        var filterMenuOpen by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable { filterMenuOpen = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedFilter,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = filterMenuOpen,
                                onDismissRequest = { filterMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Company Employees") },
                                    onClick = {
                                        viewModel.setEmployeeFilter("All")
                                        filterMenuOpen = false
                                    }
                                )
                                AppUsers.EMPLOYEES.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text(emp.name) },
                                        onClick = {
                                            viewModel.setEmployeeFilter(emp.name)
                                            filterMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Unpaid Queue vs Paid History
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = FuelGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Unpaid Claims (${unpaidExpenses.size + unpaidReceipts.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Paid Vouchers (${visibleVouchers.size})")
                            }
                        }
                    )
                }
            }
        }

        // Tab Content
        if (selectedTab == 0) {
            // UNPAID CLAIMS TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Batch Selection Bar for Admin
                if (isAdmin && (unpaidExpenses.isNotEmpty() || unpaidReceipts.isNotEmpty())) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select multi-day / weekly items to disburse in one voucher",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )

                                TextButton(
                                    onClick = {
                                        if (selectedTripIds.size == unpaidExpenses.size && selectedReceiptIds.size == unpaidReceipts.size) {
                                            selectedTripIds.clear()
                                            selectedReceiptIds.clear()
                                        } else {
                                            selectedTripIds.clear()
                                            selectedReceiptIds.clear()
                                            selectedTripIds.addAll(unpaidExpenses.map { it.id })
                                            selectedReceiptIds.addAll(unpaidReceipts.map { it.id })
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "Select All",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (selectedTripIds.size == unpaidExpenses.size) "Deselect All" else "Select All",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 1: Travel Expenses
                if (unpaidExpenses.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRIP TRAVEL EXPENSES (${unpaidExpenses.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FuelGreenPrimary
                            )
                            Text(
                                text = "PKR ${"%.2f".format(unpaidExpenses.sumOf { it.calculatedExpensePkr })}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    items(unpaidExpenses, key = { "trip_${it.id}" }) { trip ->
                        TripHistoryCard(
                            expense = trip,
                            selectable = isAdmin,
                            isSelected = trip.id in selectedTripIds,
                            onToggleSelect = {
                                if (trip.id in selectedTripIds) selectedTripIds.remove(trip.id)
                                else selectedTripIds.add(trip.id)
                            },
                            onStatusChange = { newStatus ->
                                viewModel.updateExpenseStatus(trip.id, newStatus)
                            },
                            onDelete = { viewModel.deleteTravelExpense(trip) }
                        )
                    }
                }

                // Section 2: Fuel Receipts
                if (unpaidReceipts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FUEL STATION RECEIPTS (${unpaidReceipts.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FuelGoldSecondary
                            )
                            Text(
                                text = "PKR ${"%.2f".format(unpaidReceipts.sumOf { it.totalAmount })}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    items(unpaidReceipts, key = { "rec_${it.id}" }) { receipt ->
                        ReceiptCard(
                            receipt = receipt,
                            selectable = isAdmin,
                            isSelected = receipt.id in selectedReceiptIds,
                            onToggleSelect = {
                                if (receipt.id in selectedReceiptIds) selectedReceiptIds.remove(receipt.id)
                                else selectedReceiptIds.add(receipt.id)
                            },
                            onDelete = { viewModel.deleteReceipt(receipt) }
                        )
                    }
                }

                if (unpaidExpenses.isEmpty() && unpaidReceipts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FuelGreenPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "All Expenses Settled!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No pending unpaid fuel logs or travel claims found for this view.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Settlement Action Bar for Admin
            if (isAdmin && (selectedTripIds.isNotEmpty() || selectedReceiptIds.isNotEmpty())) {
                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedTripIds.size + selectedReceiptIds.size} Items Selected",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Batch Settlement Amount:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Text(
                                text = "PKR ${"%.2f".format(grandSelectedTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = FuelGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("disburse_selected_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Disburse Multi-Day Payment (PKR ${"%.0f".format(grandSelectedTotal)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        } else {
            // PAID VOUCHERS TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (visibleVouchers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Settled Payment Vouchers Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Disbursed payment vouchers with accounting audit numbers will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(visibleVouchers, key = { it.id }) { voucher ->
                        PaymentVoucherCard(voucher = voucher)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentVoucherCard(
    voucher: PaymentVoucherEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(voucher.paymentDateMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voucher_card_${voucher.batchId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Voucher Batch ID & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FuelGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FuelGreenPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = voucher.batchId,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PKR ${"%.2f".format(voucher.totalAmountPkr)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = FuelGreenPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FuelGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "SETTLED & LOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = FuelGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payee and Details
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Payee Employee:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = voucher.employeeName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Method & Ref #:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${voucher.paymentMethod} (${voucher.referenceNumber})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Settled Items:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${voucher.tripExpenseCount} Trips • ${voucher.receiptCount} Fuel Receipts",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FuelGoldSecondary
                        )
                    }
                }
            }

            if (voucher.notes.isNotBlank() || voucher.periodDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${voucher.periodDescription} • ${voucher.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
