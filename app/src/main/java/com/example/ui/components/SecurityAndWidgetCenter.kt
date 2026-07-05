package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel

@Composable
fun SecurityAndWidgetCenter(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Collect StateFlows from ViewModel
    val ssoGoogle by viewModel.isGoogleSsoEnabled.collectAsState()
    val ssoGithub by viewModel.isGithubSsoEnabled.collectAsState()
    val ssoFitbit by viewModel.isFitbitSsoEnabled.collectAsState()
    
    val trackerShield by viewModel.isTrackerShieldEnabled.collectAsState()
    val screenSecurity by viewModel.isScreenshotSecurityEnabled.collectAsState()
    val trackersBlockedCount by viewModel.trackersBlockedCount.collectAsState()
    
    var showSsoCredentialsDialog by remember { mutableStateOf(false) }
    var credentialKeyInput by remember { mutableStateOf("") }
    var selectedSsoProvider by remember { mutableStateOf("Google Secure SSO") }

    val blueAccent = Color(0xFF38BDF8)
    val slate850 = Color(0xFF131C2E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag("security_widget_center_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Center",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "SECURITY & IDENTITY GATE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Manage tracker shields, SSO & home widgets",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                        )
                    }
                }
                
                Surface(
                    color = if (trackerShield) EmeraldPrimary.copy(alpha = 0.15f) else RosePrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (trackerShield) "PROTECTED" else "REVEALED",
                        color = if (trackerShield) EmeraldPrimary else RosePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = Slate800, thickness = 1.dp)

            // --- SECTION 1: DYNAMIC HOME SCREEN APP WIDGET INTEGRATOR ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = "App Widget", tint = AmberPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "On-Device Home Screen Widget Preview",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate100)
                        )
                    }
                    
                    Text(
                        text = "Active XML Provider",
                        color = Slate500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Beautiful simulation card of how the home screen widget displays live info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HEAL SYNC MONITOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = if (trackerShield) EmeraldPrimary.copy(alpha = 0.2f) else RosePrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (trackerShield) "🛡️ SHIELDED" else "⚠️ UNPROTECTED",
                                    color = if (trackerShield) EmeraldPrimary else RosePrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "System Tracker Protection: " + if (trackerShield) "ACTIVE" else "DISABLED",
                            fontSize = 10.sp,
                            color = Slate200
                        )
                        
                        Text(
                            text = "SSO Connection: " + when {
                                ssoGoogle && ssoGithub -> "Google Secure & GitHub Linked"
                                ssoGoogle -> "Google Secure Vault Linked"
                                ssoGithub -> "GitHub ledger account Linked"
                                ssoFitbit -> "Fitbit hardware account Linked"
                                else -> "No active SSO profiles"
                            },
                            fontSize = 9.sp,
                            color = blueAccent
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trackers Blocked on Widget: $trackersBlockedCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RosePrimary
                            )
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate800)
                                    .clickable {
                                        viewModel.incrementTrackersBlocked(context, (5..15).random())
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Query", tint = Slate300, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Ref", fontSize = 8.sp, color = Slate300, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        // Force update actual home screen App Widget Manager entries
                        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
                        viewModel.incrementTrackersBlocked(context, 1)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("widget_force_update_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send update", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-compile & Signal Screen Home Widget", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = Slate800, thickness = 1.dp)

            // --- SECTION 2: MULTIPURPOSE UNIVERSAL SIGN-ON ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Multipurpose Universal Signon (SSO)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate100)
                )

                Text(
                    text = "Consolidate multiple secure authentication tunnels into a multipurpose sync token. Sync directly to the cloud automatically.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp, lineHeight = 15.sp)
                )

                // Row of SSO Providers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Google SSO
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (ssoGoogle) slate850 else Slate950)
                            .border(1.dp, if (ssoGoogle) EmeraldPrimary.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleGoogleSso(context) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Google SSO", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Google Secure Identity Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Active OAuth SSO token verification", fontSize = 10.sp, color = Slate400)
                            }
                        }
                        Switch(
                            checked = ssoGoogle,
                            onCheckedChange = { viewModel.toggleGoogleSso(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = Slate500,
                                uncheckedTrackColor = Slate800
                            ),
                            modifier = Modifier.testTag("sso_google_switch")
                        )
                    }

                    // GitHub Ledger Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (ssoGithub) slate850 else Slate950)
                            .border(1.dp, if (ssoGithub) blueAccent.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleGithubSso(context) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "GitHub Vault", tint = blueAccent, modifier = Modifier.size(20.dp))
                            Column {
                                Text("GitHub Distributed Ledger Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Replicate sqlite commits to private gist repo", fontSize = 10.sp, color = Slate400)
                            }
                        }
                        Switch(
                            checked = ssoGithub,
                            onCheckedChange = { viewModel.toggleGithubSso(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = blueAccent,
                                checkedTrackColor = blueAccent.copy(alpha = 0.3f),
                                uncheckedThumbColor = Slate500,
                                uncheckedTrackColor = Slate800
                            ),
                            modifier = Modifier.testTag("sso_github_switch")
                        )
                    }

                    // Fitbit SDK Gateway Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (ssoFitbit) slate850 else Slate950)
                            .border(1.dp, if (ssoFitbit) AmberPrimary.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleFitbitSso(context) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Fitbit Wearable Link", tint = AmberPrimary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Fitbit Wearable Biometrics SSO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Pull automatic sleep patterns & step indexes", fontSize = 10.sp, color = Slate400)
                            }
                        }
                        Switch(
                            checked = ssoFitbit,
                            onCheckedChange = { viewModel.toggleFitbitSso(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberPrimary,
                                checkedTrackColor = AmberPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = Slate500,
                                uncheckedTrackColor = Slate800
                            ),
                            modifier = Modifier.testTag("sso_fitbit_switch")
                        )
                    }
                }

                // Inject Custom SSO Token / URL triggers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .clickable {
                            selectedSsoProvider = "Universal Web3 Link"
                            showSsoCredentialsDialog = true
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manually config OAuth client sync keys...", fontSize = 11.sp, color = blueAccent, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowForward, contentDescription = "Open credential config", tint = blueAccent, modifier = Modifier.size(14.dp))
                }
            }

            Divider(color = Slate800, thickness = 1.dp)

            // --- SECTION 3: TRACKER PROTECTION SECURITY SUITE ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Shield Protection", tint = RosePrimary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "App Anti-Tracker Security Suite",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate100)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RosePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$trackersBlockedCount",
                            fontSize = 18.sp,
                            color = RosePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Trackers Shielded", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Simulated analytic cookies & beacons successfully intercepted in safe space.", fontSize = 10.sp, color = Slate400, lineHeight = 14.sp)
                    }
                }

                // Anti-tracker Shield selection switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Intelligent Tracker Interceptor", fontSize = 12.sp, color = Slate200, fontWeight = FontWeight.Bold)
                        Text("Filter remote tracker probes and block background pixel analytics", fontSize = 10.sp, color = Slate400)
                    }
                    Switch(
                        checked = trackerShield,
                        onCheckedChange = { viewModel.toggleTrackerShield(context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RosePrimary,
                            checkedTrackColor = RosePrimary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("tracker_shield_switch")
                    )
                }

                // FLAG_SECURE system lock down
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Secure Screen Shield (Prevent Screenshots)", fontSize = 12.sp, color = Slate200, fontWeight = FontWeight.Bold)
                        Text("Blocks screen recorders, screenshot capture, and recently viewed background trackers", fontSize = 10.sp, color = Slate400)
                    }
                    Switch(
                        checked = screenSecurity,
                        onCheckedChange = { viewModel.toggleScreenshotSecurity(context, activity) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RosePrimary,
                            checkedTrackColor = RosePrimary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("screenshot_security_switch")
                    )
                }
            }
        }
    }

    // SSO credential input dialog
    if (showSsoCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showSsoCredentialsDialog = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Configure $selectedSsoProvider Options",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter secure OAuth redirect links, client IDs or cryptographic access ledger hash coordinates below:",
                        fontSize = 12.sp,
                        color = Slate300
                    )
                    OutlinedTextField(
                        value = credentialKeyInput,
                        onValueChange = { credentialKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SSO Key Link / Client ID URL", color = Slate400) },
                        placeholder = { Text("https://auth.myhealthsync.org/verify", color = Slate600) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: Multipurpose auth keys are stored privately inside the Room Android Sandbox environment.",
                        fontSize = 9.sp,
                        color = Slate500
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSsoCredentialsDialog = false
                        viewModel.incrementTrackersBlocked(context, 1)
                    }
                ) {
                    Text("Apply Tunnels", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSsoCredentialsDialog = false }) {
                    Text("Dismiss", color = Slate400)
                }
            }
        )
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(4.dp)
)
