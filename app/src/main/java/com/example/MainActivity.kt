package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.dialogs.GoogleDriveSyncDialog
import com.example.ui.dialogs.UserRoleSwitchDialog
import com.example.ui.screens.AdminVehiclesScreen
import com.example.ui.screens.DisbursementScreen
import com.example.ui.screens.FuelReceiptsScreen
import com.example.ui.screens.TravelCalculatorScreen
import com.example.ui.screens.TripHistoryScreen
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenContainer
import com.example.ui.theme.FuelGreenOnContainer
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.theme.FuelRecordTheme
import com.example.ui.theme.PsoGreen
import com.example.ui.viewmodel.FuelRecordViewModel
import kotlinx.coroutines.flow.collectLatest

enum class AppNavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CALCULATOR("Calculator", Icons.Filled.Calculate, Icons.Outlined.Calculate, "tab_calculator"),
    RECEIPTS("Receipts", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_receipts"),
    TRIPS("Trip Logs", Icons.Filled.History, Icons.Outlined.History, "tab_trips"),
    SETTLEMENT("Finance", Icons.Filled.Payment, Icons.Outlined.Payment, "tab_settlement"),
    ADMIN("Vehicles", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, "tab_admin")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelRecordTheme {
                FuelRecordApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelRecordApp(viewModel: FuelRecordViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var showGoogleDriveSyncDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val psoRates by viewModel.psoRates.collectAsStateWithLifecycle()
    val visibleReceipts by viewModel.visibleReceipts.collectAsStateWithLifecycle()
    val visibleTrips by viewModel.visibleTravelExpenses.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

    val petrolRate = psoRates.firstOrNull { it.fuelType.equals("Petrol", ignoreCase = true) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showRoleSwitchDialog) {
        UserRoleSwitchDialog(
            currentUser = currentUser,
            onSelectUser = { selectedProfile ->
                viewModel.switchUser(selectedProfile)
                showRoleSwitchDialog = false
            },
            onDismiss = { showRoleSwitchDialog = false }
        )
    }

    if (showGoogleDriveSyncDialog) {
        GoogleDriveSyncDialog(
            viewModel = viewModel,
            onDismiss = { showGoogleDriveSyncDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FuelGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FuelRecord PK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                navigationIcon = {
                    // Profile Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isAdmin) FuelGoldSecondary.copy(alpha = 0.15f) else FuelGreenPrimary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { showRoleSwitchDialog = true }
                            .testTag("role_switcher_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = "Role",
                                tint = if (isAdmin) FuelGoldSecondary else FuelGreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser.name.split(" ").firstOrNull() ?: "User",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdmin) FuelGoldSecondary else FuelGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch",
                                tint = if (isAdmin) FuelGoldSecondary else FuelGreenPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Google Drive Sync Button
                    androidx.compose.material3.IconButton(
                        onClick = { showGoogleDriveSyncDialog = true },
                        modifier = Modifier.testTag("gdrive_sync_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync with Google Drive",
                            tint = FuelGreenPrimary
                        )
                    }

                    if (petrolRate != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FuelGreenContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PSO: Rs ${"%.1f".format(petrolRate.effectiveRatePkr)}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FuelGreenOnContainer
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppNavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            when (tab) {
                                AppNavigationTab.RECEIPTS -> {
                                    if (visibleReceipts.isNotEmpty()) {
                                        BadgedBox(
                                            badge = { Badge { Text("${visibleReceipts.size}") } }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                }
                                AppNavigationTab.TRIPS -> {
                                    if (visibleTrips.isNotEmpty()) {
                                        BadgedBox(
                                            badge = { Badge { Text("${visibleTrips.size}") } }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            }
                        },
                        label = { Text(tab.title, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FuelGreenPrimary,
                            selectedTextColor = FuelGreenPrimary,
                            indicatorColor = FuelGreenContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(innerPadding),
            label = "tabContent"
        ) { tabIndex ->
            when (AppNavigationTab.entries[tabIndex]) {
                AppNavigationTab.CALCULATOR -> TravelCalculatorScreen(viewModel = viewModel)
                AppNavigationTab.RECEIPTS -> FuelReceiptsScreen(viewModel = viewModel)
                AppNavigationTab.TRIPS -> TripHistoryScreen(viewModel = viewModel)
                AppNavigationTab.SETTLEMENT -> DisbursementScreen(viewModel = viewModel)
                AppNavigationTab.ADMIN -> AdminVehiclesScreen(viewModel = viewModel)
            }
        }
    }
}

