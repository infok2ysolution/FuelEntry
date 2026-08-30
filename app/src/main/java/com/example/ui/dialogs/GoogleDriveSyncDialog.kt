package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.FuelGoldSecondary
import com.example.ui.theme.FuelGreenContainer
import com.example.ui.theme.FuelGreenOnContainer
import com.example.ui.theme.FuelGreenPrimary
import com.example.ui.viewmodel.FuelRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveSyncDialog(
    viewModel: FuelRecordViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isSyncing by viewModel.isSyncingSheet.collectAsStateWithLifecycle()
    val syncResult by viewModel.sheetSyncResult.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var sheetLinkInput by remember { mutableStateOf("") }
    var pasteDataInput by remember { mutableStateOf("") }
    var copyNotice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isSyncing) {
                viewModel.clearSyncResult()
                onDismiss()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = FuelGreenContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Google Drive Sync",
                            tint = FuelGreenOnContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Google Drive & Sheets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Folder: vehicle dashboard",
                        style = MaterialTheme.typography.labelSmall,
                        color = FuelGreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab Selection
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Sheet URL", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Paste Data", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Export CSV", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> {
                        // URL / Sheet ID Tab
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = FuelGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open your Google Sheet inside 'vehicle dashboard' folder in Google Drive. Ensure link sharing is enabled ('Anyone with link can view') and paste the URL or Sheet ID below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = sheetLinkInput,
                            onValueChange = { sheetLinkInput = it },
                            label = { Text("Google Sheet Link or ID") },
                            placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FuelGreenPrimary,
                                focusedLabelColor = FuelGreenPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sheet_url_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.syncWithGoogleDriveSheet(
                                    sheetUrlOrId = sheetLinkInput,
                                    defaultEmployee = currentUser.name
                                )
                            },
                            enabled = sheetLinkInput.isNotBlank() && !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sync_sheet_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing Google Sheet...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync & Import All Data")
                            }
                        }
                    }

                    1 -> {
                        // Direct Paste CSV Tab
                        Text(
                            text = "Copy cells directly from Google Sheets (Trips, Receipts, or Vehicles) and paste here:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pasteDataInput,
                            onValueChange = { pasteDataInput = it },
                            label = { Text("Pasted Spreadsheet / CSV Content") },
                            placeholder = { Text("Date\tEmployee\tFrom\tTo\tDistance\tTotal PKR...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 180.dp)
                                .testTag("paste_sheet_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FuelGreenPrimary,
                                focusedLabelColor = FuelGreenPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.importRawSheetData(
                                    rawCsvOrTsv = pasteDataInput,
                                    defaultEmployee = currentUser.name
                                )
                            },
                            enabled = pasteDataInput.isNotBlank() && !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = FuelGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("import_pasted_data_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importing Data...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Data into App")
                            }
                        }
                    }

                    2 -> {
                        // Export CSV Tab
                        val exportData = remember { viewModel.generateGoogleSheetExportData() }

                        Text(
                            text = "Export all current logs in standard Google Sheets format:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 150.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = exportData,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Vehicle Dashboard Export", exportData)
                                clipboard.setPrimaryClip(clip)
                                copyNotice = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copyNotice) "Copied to Clipboard!" else "Copy CSV for Google Drive")
                        }
                    }
                }

                // Sync Result Card
                if (syncResult != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    val result = syncResult!!
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (result.success) FuelGreenPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (result.success) FuelGreenPrimary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (result.success) "Sync Completed" else "Sync Issue",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (result.success) FuelGreenPrimary else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = result.message,
                                    fontSize = 11.5.sp,
                                    color = if (result.success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.clearSyncResult()
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        }
    )
}
