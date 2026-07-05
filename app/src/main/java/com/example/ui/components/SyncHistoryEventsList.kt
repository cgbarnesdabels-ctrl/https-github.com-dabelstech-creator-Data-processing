package com.example.ui.components

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SyncEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncHistoryEventsList(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Query filters state
    var displayMode by remember { mutableStateOf("Table") } // "Table" or "List"
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All Status") }
    var deviceFilter by remember { mutableStateOf("All Devices") }
    var serviceFilter by remember { mutableStateOf("All Services") }
    var dateRangeFilter by remember { mutableStateOf("All Time") }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var sortOrder by remember { mutableStateOf(true) } // true = Newest First, false = Oldest First

    // Filter computation
    val filteredEvents = remember(events, searchQuery, statusFilter, deviceFilter, serviceFilter, dateRangeFilter, customStartDate, customEndDate, sortOrder) {
        var result = events.filter { event ->
            // 1. Text Search
            val matchesSearch = searchQuery.isBlank() ||
                    event.eventType.contains(searchQuery, ignoreCase = true) ||
                    event.details.contains(searchQuery, ignoreCase = true)

            // 2. Status check
            val matchesStatus = statusFilter == "All Status" ||
                    (statusFilter == "SUCCESS" && event.status == "SUCCESS") ||
                    (statusFilter == "FAILED" && event.status == "FAILED")

            // 3. Device query matching
            val matchesDevice = deviceFilter == "All Devices" ||
                    (deviceFilter == "iOS" && event.eventType.contains("iOS", ignoreCase = true)) ||
                    (deviceFilter == "Android" && (event.eventType.contains("Android", ignoreCase = true) || event.details.contains("Android", ignoreCase = true))) ||
                    (deviceFilter == "Garmin" && event.details.contains("Garmin", ignoreCase = true)) ||
                    (deviceFilter == "Fitbit" && event.details.contains("Fitbit", ignoreCase = true))

            // 4. Service check (Added calendar!)
            val matchesService = serviceFilter == "All Services" ||
                    (serviceFilter == "Google Drive" && (event.eventType.contains("Drive", ignoreCase = true) || event.eventType == "Google Drive")) ||
                    (serviceFilter == "Gmail Sync" && (event.eventType.contains("Gmail", ignoreCase = true) || event.eventType == "Gmail Sync")) ||
                    (serviceFilter == "Chrome Sync" && (event.eventType.contains("Chrome", ignoreCase = true) || event.eventType == "Chrome Sync")) ||
                    (serviceFilter == "Google Calendar" && (event.eventType.contains("Calendar", ignoreCase = true) || event.eventType == "Google Calendar"))

            // 5. Dynamic Date filters
            val matchesDate = when (dateRangeFilter) {
                "Today" -> {
                    val startOfDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    event.timestamp >= startOfDay
                }
                "Last 7 Days" -> {
                    val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                    event.timestamp >= sevenDaysAgo
                }
                "Last 30 Days" -> {
                    val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    event.timestamp >= thirtyDaysAgo
                }
                "Custom" -> {
                    val start = customStartDate ?: 0L
                    val end = customEndDate ?: Long.MAX_VALUE
                    event.timestamp in start..end
                }
                else -> true
            }

            matchesSearch && matchesStatus && matchesDevice && matchesService && matchesDate
        }

        // Apply chronological sort order
        result = if (sortOrder) {
            result.sortedByDescending { it.timestamp }
        } else {
            result.sortedBy { it.timestamp }
        }
        result
    }

    var isExporting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Log Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Text(
                    text = "System audit histories",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export CSV action
                IconButton(
                    onClick = {
                        isExporting = true
                        viewModel.exportEventsToCsv(context, filteredEvents) { error ->
                            isExporting = false
                            if (error != null) {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate800, CircleShape)
                        .testTag("export_csv_btn")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EmeraldPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                // Clear entire SQL db
                IconButton(
                    onClick = {
                        viewModel.clearAllDashboardLogs()
                        Toast.makeText(context, "Audit trails cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate800, CircleShape)
                        .testTag("clear_logs_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = RosePrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter logs by details or event...", color = Slate400, fontSize = 13.sp) },
            prefix = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp).padding(end = 4.dp)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("search_query_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Core filter parameters row
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1: Status Filter
            FilterGroupRow(label = "Status:") {
                listOf("All Status", "SUCCESS", "FAILED").forEach { status ->
                    val isSelected = statusFilter == status
                    FilterBadge(
                        text = status,
                        isSelected = isSelected,
                        testTag = "status_filter_$status",
                        onClick = { statusFilter = status }
                    )
                }
            }

            // Row 2: Device query
            FilterGroupRow(label = "Device:") {
                listOf("All Devices", "iOS", "Android", "Garmin", "Fitbit").forEach { device ->
                    val isSelected = deviceFilter == device
                    val displayName = when (device) {
                        "All Devices" -> "All"
                        else -> device
                    }
                    FilterBadge(
                        text = displayName,
                        isSelected = isSelected,
                        testTag = "device_filter_$device",
                        onClick = { deviceFilter = device }
                    )
                }
            }

            // Row 3: Service Source categories (Added calendar selector)
            FilterGroupRow(label = "Service:") {
                listOf("All Services", "Google Calendar", "Google Drive", "Gmail Sync", "Chrome Sync").forEach { srv ->
                    val isSelected = serviceFilter == srv
                    val displayName = when (srv) {
                        "All Services" -> "All"
                        "Google Calendar" -> "Calendar"
                        "Google Drive" -> "Drive"
                        "Gmail Sync" -> "Gmail"
                        "Chrome Sync" -> "Chrome"
                        else -> srv
                    }
                    FilterBadge(
                        text = displayName,
                        isSelected = isSelected,
                        testTag = "service_filter_$srv",
                        onClick = { serviceFilter = srv }
                    )
                }
            }

            // Row 3: Chronological query timeline
            FilterGroupRow(label = "Date:") {
                listOf("All Time", "Today", "Last 7 Days", "Custom").forEach { df ->
                    val isSelected = dateRangeFilter == df
                    val displayName = when (df) {
                        "All Time" -> "All"
                        "Last 7 Days" -> "7d"
                        else -> df
                    }
                    FilterBadge(
                        text = displayName,
                        isSelected = isSelected,
                        testTag = "date_range_filter_$df",
                        onClick = { dateRangeFilter = df }
                    )
                }
            }

            // Custom Range Expanded UI
            if (dateRangeFilter == "Custom") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startText = customStartDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Start Date"

                    val endText = customEndDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "End Date"

                    Button(
                        onClick = { showDatePickerDialog(context, true) { customStartDate = it } },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("custom_start_date_btn")
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(startText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showDatePickerDialog(context, false) { customEndDate = it } },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("custom_end_date_btn")
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(endText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (customStartDate != null || customEndDate != null) {
                        IconButton(
                            onClick = {
                                customStartDate = null
                                customEndDate = null
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Slate800, CircleShape)
                                .testTag("clear_custom_dates_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Custom Range", tint = RosePrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Row 4: Device Source Filter
            FilterGroupRow(label = "Device:") {
                listOf("All Devices", "iOS", "Android", "Garmin", "Fitbit").forEach { dev ->
                    val isSelected = deviceFilter == dev
                    val name = if (dev == "All Devices") "All" else dev
                    FilterBadge(
                        text = name,
                        isSelected = isSelected,
                        testTag = "device_filter_$dev",
                        onClick = { deviceFilter = dev }
                    )
                }
            }

            // Sorting bar toggle selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { sortOrder = !sortOrder }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (sortOrder) "Showing Newest First" else "Showing Oldest First",
                    style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                )
                Icon(
                    imageVector = if (sortOrder) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Divider(color = Slate800, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Layout displayMode toggle buttons row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Layout Mode:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterBadge(
                        text = "📊 Spreadsheet Table",
                        isSelected = displayMode == "Table",
                        testTag = "layout_table_tab",
                        onClick = { displayMode = "Table" }
                    )
                    FilterBadge(
                        text = "📜 Compact List",
                        isSelected = displayMode == "List",
                        testTag = "layout_list_tab",
                        onClick = { displayMode = "List" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = 280.dp)
        ) {
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No matching audit logs located.", style = MaterialTheme.typography.bodyMedium, color = Slate400)
                    }
                }
            } else {
                if (displayMode == "Table") {
                    SyncHistoryTable(events = filteredEvents)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredEvents, key = { it.id }) { event ->
                            SyncEventItem(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyncHistoryTable(
    events: List<SyncEvent>,
    modifier: Modifier = Modifier
) {
    val SkyPrimary = Color(0xFF38BDF8)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate950)
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .testTag("sync_audit_table_container")
    ) {
        // Table Header row representing spreadsheet grid layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate800)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMESTAMP",
                modifier = Modifier.weight(1.3f).testTag("header_timestamp"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "DEVICE",
                modifier = Modifier.weight(1.3f).testTag("header_device"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "SERVICE TYPE",
                modifier = Modifier.weight(1.6f).testTag("header_service"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "STATUS",
                modifier = Modifier.weight(1.0f).testTag("header_status"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate300,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Horizontal line separator
        Divider(color = Slate800, thickness = 1.dp)

        // Scrollable Table Row Items (replaces traditional cards with grid columns)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(events, key = { it.id }) { event ->
                val dateStr = remember(event.timestamp) {
                    SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(event.timestamp))
                }

                // Dynamically deduce simulated Device Source
                val deviceName = remember(event.details, event.eventType) {
                    when {
                        event.eventType.contains("iOS", ignoreCase = true) -> "iOS Device"
                        event.eventType.contains("Android", ignoreCase = true) || event.details.contains("Android", ignoreCase = true) -> "Android S24"
                        event.details.contains("Garmin", ignoreCase = true) -> "Garmin Fenix"
                        event.details.contains("Fitbit", ignoreCase = true) -> "Fitbit Luxe"
                        event.eventType.contains("Drive", ignoreCase = true) || event.eventType.contains("Calendar", ignoreCase = true) -> "Google Cloud"
                        else -> "Local Host"
                    }
                }

                val deviceIcon = when (deviceName) {
                    "iOS Device" -> Icons.Default.List
                    "Android S24" -> Icons.Default.Info
                    "Garmin Fenix" -> Icons.Default.DateRange
                    "Fitbit Luxe" -> Icons.Default.PlayArrow
                    "Google Cloud" -> Icons.Default.Share
                    else -> Icons.Default.Home
                }

                val isEven = events.indexOf(event) % 2 == 0
                val rowBg = if (isEven) Slate900 else Slate950

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(vertical = 11.dp, horizontal = 12.dp)
                            .testTag("table_row_${event.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cell 1: Timestamp
                        Column(modifier = Modifier.weight(1.3f)) {
                            val parts = dateStr.split(" ")
                            val timePart = parts.getOrNull(0) ?: ""
                            val datePart = parts.getOrNull(1) ?: ""
                            
                            Text(
                                text = timePart,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = datePart,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // Cell 2: Device Source with matching Icon
                        Row(
                            modifier = Modifier.weight(1.3f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = deviceIcon,
                                contentDescription = null,
                                tint = SkyPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = deviceName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate200,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }

                        // Cell 3: Service type
                        Column(modifier = Modifier.weight(1.6f)) {
                            Text(
                                text = event.eventType,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate300,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                            if (event.recordsSynced > 0) {
                                Text(
                                    text = "+${event.recordsSynced} details",
                                    color = EmeraldPrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Cell 4: Status badge cell
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (event.status) {
                                        "SUCCESS" -> EmeraldPrimary.copy(alpha = 0.15f)
                                        "FAILED" -> RosePrimary.copy(alpha = 0.15f)
                                        else -> AmberPrimary.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = event.status,
                                color = when (event.status) {
                                    "SUCCESS" -> EmeraldPrimary
                                    "FAILED" -> RosePrimary
                                    else -> AmberPrimary
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                    Divider(color = Slate800, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun FilterGroupRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(54.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun FilterBadge(
    text: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Slate800)
            .border(1.dp, if (isSelected) EmeraldPrimary.copy(alpha = 0.7f) else Slate800, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = if (isSelected) EmeraldPrimary else Slate400,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun SyncEventItem(event: SyncEvent) {
    val dateStr = remember(event.timestamp) {
        SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(event.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status indicator visual badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when (event.status) {
                        "SUCCESS" -> EmeraldPrimary.copy(alpha = 0.2f)
                        "FAILED" -> RosePrimary.copy(alpha = 0.2f)
                        else -> AmberPrimary.copy(alpha = 0.2f)
                    }
                )
                .padding(6.dp)
        ) {
            Icon(
                imageVector = when (event.status) {
                    "SUCCESS" -> Icons.Default.CheckCircle
                    "FAILED" -> Icons.Default.Clear
                    else -> Icons.Default.Warning
                },
                contentDescription = event.status,
                tint = when (event.status) {
                    "SUCCESS" -> EmeraldPrimary
                    "FAILED" -> RosePrimary
                    else -> AmberPrimary
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    fontSize = 12.sp
                )

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = event.details,
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate300),
                fontSize = 11.sp
            )

            if (event.recordsSynced > 0) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(10.dp))
                    Text(
                        text = "${event.recordsSynced} records replicated successfully",
                        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun showDatePickerDialog(
    context: Context,
    isStart: Boolean,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val resCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                if (isStart) {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                } else {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
            }
            onDateSelected(resCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
