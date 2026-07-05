package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun DashboardScreen(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    var isWebViewMode by remember { mutableStateOf(true) }
    var showChat by remember { mutableStateOf(false) }

    if (isWebViewMode) {
        WebDashboardScreen(
            viewModel = viewModel,
            modifier = modifier,
            onBackToCompose = { isWebViewMode = false }
        )
    } else {
        val scrollState = rememberScrollState()
        val SkyPrimary = Color(0xFF38BDF8)
        
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmicBackground)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .testTag("dashboard_root_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App header bar with mode switch trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HEAL SYNC",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Dynamic health replication ledger",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500, letterSpacing = 0.5.sp)
                        )
                    }

                    Button(
                        onClick = { isWebViewMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyPrimary.copy(alpha = 0.15f),
                            contentColor = SkyPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .border(0.5.dp, SkyPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .testTag("switch_to_web_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🖥️ WEB DASHBOARD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 1. Core Cloud Replication Grid (Drive, Gmail, Chrome, and CALENDAR sync controllers)
                SyncDashboard(viewModel = viewModel)

                // 2. Health Stats & Spline Telemetry Curves Panel
                HealthMetricsVisualizer(viewModel = viewModel)

                // 3. Log Biometrics snapshot panel
                DailyHealthMetricsForm(viewModel = viewModel)

                // 4. Compact Logs Query lists table
                SyncHistoryEventsList(viewModel = viewModel)

                // 5. App Security, Tracker Shield & Home Screen Widget Integrations Manager
                SecurityAndWidgetCenter(viewModel = viewModel)

                // Bottom disclaimer
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Health Sync Engine utilizing secure local SQLite databases with calendar-aware scheduling synchronization bounds. Standard sandbox emulator runtime.",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            FloatingActionButton(
                onClick = { showChat = true },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("AI")
            }

            if (showChat) {
                ChatDialog(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                    onDismissRequest = { showChat = false }
                )
            }
        }
    }
}
