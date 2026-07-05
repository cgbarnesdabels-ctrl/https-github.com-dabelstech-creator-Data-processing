package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel

@Composable
fun SyncDashboard(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val isGoogleDriveEnabled by viewModel.isGoogleDriveEnabled.collectAsStateWithLifecycle()
    val isGmailEnabled by viewModel.isGmailEnabled.collectAsStateWithLifecycle()
    val isChromeEnabled by viewModel.isChromeEnabled.collectAsStateWithLifecycle()
    val isGoogleCalendarEnabled by viewModel.isGoogleCalendarEnabled.collectAsStateWithLifecycle()
    val isGoogleContactsEnabled by viewModel.isGoogleContactsEnabled.collectAsStateWithLifecycle()
    val isBatterySaverActive by viewModel.isBatterySaverActive.collectAsStateWithLifecycle()
    val activeConflict by viewModel.activeConflict.collectAsStateWithLifecycle()

    val anyServiceActive = (isGoogleDriveEnabled || isGmailEnabled || isChromeEnabled || isGoogleCalendarEnabled || isGoogleContactsEnabled) && !isBatterySaverActive

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard System Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cloud replication center",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )
                Text(
                    text = "Manage multi-source database synchronization",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )
            }

            // Quick bypass Fast Connect sequence target button
            Button(
                onClick = { viewModel.executeFastConnect() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("fast_connect_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fast Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Active Status & Loading Animations with Spinning Arcs and pulses
        SystemDaemonStatus(anyServiceActive, isBatterySaverActive)

        // Conflict Resolution Area
        AnimatedVisibility(
            visible = activeConflict != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            activeConflict?.let { conflict ->
                ConflictResolutionCard(
                    conflict = conflict,
                    onResolve = { keepLocal -> viewModel.resolveConflict(keepLocal) }
                )
            }
        }

        // Battery Save Tracker Module
        BatterySaverController(
            isActive = isBatterySaverActive,
            onToggle = { viewModel.setBatterySaverActive(it) }
        )

        Divider(color = Slate700)

        // Synchronizers grid
        Text(
            text = "Active cloud synchronizers",
            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.SemiBold)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Google Calendar Integration card (NEW)
            SyncServiceRow(
                title = "Google Calendar Sync",
                subtitle = "Schedule activity and welfare slot snapshots",
                icon = Icons.Default.DateRange,
                isEnabled = isGoogleCalendarEnabled,
                isSuspended = isBatterySaverActive,
                onToggle = { viewModel.setGoogleCalendarEnabled(it) },
                testTagPrefix = "google_calendar"
            )

            // 2. Google Drive Card
            SyncServiceRow(
                title = "Google Drive Auto-Sync",
                subtitle = "Replicate local core database deltas",
                icon = Icons.Default.Settings,
                isEnabled = isGoogleDriveEnabled,
                isSuspended = isBatterySaverActive,
                onToggle = { viewModel.setGoogleDriveEnabled(it) },
                testTagPrefix = "google_drive"
            )

            // 3. Gmail Sync Card
            SyncServiceRow(
                title = "Gmail Ledger Stream",
                subtitle = "Transmit continuous metric journals",
                icon = Icons.Default.Email,
                isEnabled = isGmailEnabled,
                isSuspended = isBatterySaverActive,
                onToggle = { viewModel.setGmailEnabled(it) },
                testTagPrefix = "gmail_sync"
            )

            // 4. Chrome Sync Card
            SyncServiceRow(
                title = "Chrome Client Sync",
                subtitle = "Mirror sessions across mobile and DeX platforms",
                icon = Icons.Default.Home,
                isEnabled = isChromeEnabled,
                isSuspended = isBatterySaverActive,
                onToggle = { viewModel.setChromeEnabled(it) },
                testTagPrefix = "chrome_sync"
            )

            // 5. Google Contacts Sync Card
            SyncServiceRow(
                title = "Google Contacts Sync",
                subtitle = "Align emergency medical support contacts circle",
                icon = Icons.Default.Person,
                isEnabled = isGoogleContactsEnabled,
                isSuspended = isBatterySaverActive,
                onToggle = { viewModel.setGoogleContactsEnabled(it) },
                testTagPrefix = "google_contacts"
            )
        }
    }
}

@Composable
fun SystemDaemonStatus(
    anyServiceActive: Boolean,
    isBatterySaverActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_spin")
    
    // Spinning Arc animation
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Breathing pulse alpha animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val containerBg = if (isBatterySaverActive) {
        AmberPrimary.copy(alpha = 0.08f)
    } else if (anyServiceActive) {
        EmeraldPrimary.copy(alpha = 0.06f)
    } else {
        Slate800
    }

    val borderColor = if (isBatterySaverActive) {
        AmberPrimary.copy(alpha = 0.3f)
    } else if (anyServiceActive) {
        EmeraldPrimary.copy(alpha = 0.30f)
    } else {
        Slate700
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Visual status circle indicator
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isBatterySaverActive) {
                // Suspended Power Saving warning
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(AmberPrimary, CircleShape)
                )
            } else if (anyServiceActive) {
                // Spinning active ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = EmeraldPrimary,
                        startAngle = angle,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(EmeraldPrimary, CircleShape)
                )
            } else {
                // Inactive state indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Slate600, CircleShape)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            val statusTitle = when {
                isBatterySaverActive -> "BACKGROUND TIMELINE REPLICATION SUSPENDED"
                anyServiceActive -> "DYNAMIC SYNC DAEMON ACTIVE"
                else -> "ALL REPLICATORS IDLE / DISCONNECTED"
            }
            val statusColor = when {
                isBatterySaverActive -> AmberPrimary
                anyServiceActive -> EmeraldPrimary
                else -> Slate400
            }

            Text(
                text = statusTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 0.5.sp
                )
            )

            val statusDesc = when {
                isBatterySaverActive -> "Power optimal bounds enabled. Auto-uploads paused to preserve battery cycle."
                anyServiceActive -> "Continuous client synchronization connected. Real-time updates pulsing securely."
                else -> "No active endpoints linked. Logged events will accumulate inside local SQLite storage."
            }

            Text(
                text = statusDesc,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (anyServiceActive && !isBatterySaverActive) Slate100.copy(alpha = pulseAlpha) else Slate300
                ),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SyncServiceRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isSuspended: Boolean,
    onToggle: (Boolean) -> Unit,
    testTagPrefix: String
) {
    val showsActive = isEnabled && !isSuspended

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (showsActive) Slate900 else Slate800)
            .border(
                1.dp,
                if (showsActive) EmeraldPrimary.copy(alpha = 0.4f) else Slate700,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (showsActive) EmeraldPrimary.copy(alpha = 0.15f) else Slate700),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (showsActive) EmeraldPrimary else Slate400,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (showsActive) Color.White else Slate300
                )
            )
            val subDesc = if (isEnabled && isSuspended) {
                "[PAUSED] System power saving restriction active."
            } else {
                subtitle
            }
            Text(
                text = subDesc,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isEnabled && isSuspended) AmberPrimary else Slate400
                )
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldPrimary,
                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate700
            ),
            modifier = Modifier
                .testTag("${testTagPrefix}_toggle")
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun BatterySaverController(
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate900)
            .clickable { onToggle(!isActive) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isActive) AmberPrimary else EmeraldPrimary,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = "Battery Optimization Profile",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.White)
                )
                Text(
                    text = if (isActive) "Restricted continuous loops active" else "Uncapped connection bounds",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )
            }
        }

        Switch(
            checked = isActive,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AmberPrimary,
                checkedTrackColor = AmberPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = Slate500,
                uncheckedTrackColor = Slate800
            ),
            modifier = Modifier.testTag("battery_saver_switch")
        )
    }
}

@Composable
fun ConflictResolutionCard(
    conflict: com.example.data.model.SyncConflict,
    onResolve: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmberPrimary.copy(alpha = 0.08f))
            .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Alert", tint = AmberPrimary, modifier = Modifier.size(16.dp))
            Text(
                text = "CONFLICT IN ${conflict.source.uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AmberPrimary)
            )
        }

        Text(
            text = conflict.description,
            style = MaterialTheme.typography.bodyMedium.copy(color = Slate100),
            fontSize = 12.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900)
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("On-Device (Keep Local)", color = EmeraldPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(conflict.localDataSummary, color = Slate300, fontSize = 10.sp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900)
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Remote Cloud (Overwrite)", color = AmberPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(conflict.cloudDataSummary, color = Slate300, fontSize = 10.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onResolve(true) },
                modifier = Modifier.testTag("resolve_keep_local")
            ) {
                Text("Use Local Copy", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = { onResolve(false) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Slate950),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("resolve_keep_cloud")
            ) {
                Text("Overwrite local", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
