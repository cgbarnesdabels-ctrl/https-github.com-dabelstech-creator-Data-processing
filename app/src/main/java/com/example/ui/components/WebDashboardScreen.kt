package com.example.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "CollectionSize")
@Composable
fun WebDashboardScreen(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier,
    onBackToCompose: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val scope = rememberCoroutineScope()

    // Browser navigation and address states
    var displayUrl by remember { mutableStateOf("https://heal-sync.cloud/dashboard") }
    var typedUrl by remember { mutableStateOf("https://heal-sync.cloud/dashboard") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Connect Viewmodel StateFlows
    val trackersBlockedCount by viewModel.trackersBlockedCount.collectAsState()
    val isTrackerShieldEnabled by viewModel.isTrackerShieldEnabled.collectAsState()

    // Helper function to resolve entered text to a valid URL/search
    fun navigateToInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val targetUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
            trimmed
        } else if (trimmed.contains(".") && !trimmed.contains(" ")) {
            "https://$trimmed"
        } else {
            // Search DuckDuckGo by default
            val encodedQuery = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
            "https://duckduckgo.com/?q=$encodedQuery"
        }

        webViewInstance?.loadUrl(targetUrl)
        focusManager.clearFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        // App Browser Top Header Chrome / Shell Frame (Designed style: PREMIUM MOBILE WEB BROWSER)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Address Bar & Instant Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mobile-Style Navigation Address Container
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(Slate950, RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isTrackerShieldEnabled) EmeraldPrimary.copy(alpha = 0.5f) else Slate800,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Secure SSL Lock Indicator
                        val isHttpsSecure = displayUrl.startsWith("https") || displayUrl.startsWith("file")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SSL Status Indicator",
                            tint = if (isHttpsSecure) EmeraldPrimary else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )

                        // Editable Address Input Field
                        TextField(
                            value = typedUrl,
                            onValueChange = { typedUrl = it },
                            placeholder = {
                                Text(
                                    "Search or enter URL...",
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = Slate200,
                                unfocusedTextColor = Slate200,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = EmeraldPrimary
                            ),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = Slate200,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                navigateToInput(typedUrl)
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("browser_address_input")
                        )

                        // Clear Button or Safe Badge
                        if (typedUrl != displayUrl && typedUrl.isNotEmpty()) {
                            IconButton(
                                onClick = { typedUrl = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Address Bar",
                                    tint = Slate400,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else {
                            // SSL Status indicators
                            Text(
                                text = "SSL",
                                fontSize = 8.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .background(EmeraldPrimary.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Go/Navigate Button
                    Button(
                        onClick = { navigateToInput(typedUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = CosmicBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("browser_go_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Quick Speed Dial Bookmarks Ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick link: Local Health Sync Portal
                    AssistChip(
                        onClick = {
                            webViewInstance?.loadUrl("file:///android_asset/web_app/index.html")
                            focusManager.clearFocus()
                        },
                        label = { Text("🏥 Health Dashboard", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = EmeraldPrimary.copy(alpha = 0.12f),
                            labelColor = EmeraldPrimary,
                            leadingIconContentColor = EmeraldPrimary
                        ),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                    )

                    // Quick link: DuckDuckGo Search
                    AssistChip(
                        onClick = {
                            webViewInstance?.loadUrl("https://duckduckgo.com")
                            focusManager.clearFocus()
                        },
                        label = { Text("👁️ DuckDuckGo", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate950,
                            labelColor = Slate300
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    )

                    // Quick link: Google Search
                    AssistChip(
                        onClick = {
                            webViewInstance?.loadUrl("https://google.com")
                            focusManager.clearFocus()
                        },
                        label = { Text("🌐 Google", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate950,
                            labelColor = Slate300
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    )

                    // Quick link: Wikipedia
                    AssistChip(
                        onClick = {
                            webViewInstance?.loadUrl("https://wikipedia.org")
                            focusManager.clearFocus()
                        },
                        label = { Text("📖 Wikipedia", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate950,
                            labelColor = Slate300
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    )

                    // Quick link: GitHub
                    AssistChip(
                        onClick = {
                            webViewInstance?.loadUrl("https://github.com")
                            focusManager.clearFocus()
                        },
                        label = { Text("🐙 GitHub", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate950,
                            labelColor = Slate300
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Integrated Shield & Active Telemetry Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(6.dp))
                        .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isTrackerShieldEnabled) EmeraldPrimary else Color.Gray)
                        )
                        Text(
                            text = if (isTrackerShieldEnabled) "🛡️ TRACKER SHIELD ACTIVE" else "🛡️ SHIELD DEACTIVATED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isTrackerShieldEnabled) EmeraldPrimary else Slate400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "BLOCKED: $trackersBlockedCount",
                            fontSize = 9.sp,
                            color = if (trackersBlockedCount > 0) EmeraldPrimary else Slate400,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Quick Toggle Button to switch Shield Status in browser!
                        Text(
                            text = if (isTrackerShieldEnabled) "TURN OFF" else "TURN ON",
                            fontSize = 8.sp,
                            color = if (isTrackerShieldEnabled) Color.Red else EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable {
                                    viewModel.toggleTrackerShield(context)
                                }
                                .background(Slate900, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Embedded HTML5/JS Web rendering View wrapping WebView with AndroidView
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Slate950),
            border = BorderStroke(1.dp, Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("embedded_html_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                javaScriptCanOpenWindowsAutomatically = true
                                databaseEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let {
                                        val readableUrl = if (it.startsWith("file:///android_asset/web_app/index.html")) {
                                            "https://heal-sync.cloud/dashboard"
                                        } else {
                                            it
                                        }
                                        displayUrl = readableUrl
                                        typedUrl = readableUrl
                                    }
                                    canGoBack = canGoBack()
                                    canGoForward = canGoForward()
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    url?.let {
                                        val readableUrl = if (it.startsWith("file:///android_asset/web_app/index.html")) {
                                            "https://heal-sync.cloud/dashboard"
                                        } else {
                                            it
                                        }
                                        displayUrl = readableUrl
                                        typedUrl = readableUrl
                                    }
                                    canGoBack = canGoBack()
                                    canGoForward = canGoForward()
                                }
                            }
                            webChromeClient = WebChromeClient()

                            // Bind our high-performance Web Bridge to bridge Javascript inputs natively
                            val bridge = WebAppBridge(
                                context = ctx,
                                webView = this,
                                viewModel = viewModel,
                                scope = scope
                            )
                            addJavascriptInterface(bridge, "AndroidBridge")

                            // Load the embedded responsive index.html SPA from assets folder!
                            loadUrl("file:///android_asset/web_app/index.html")
                            webViewInstance = this
                        }
                    },
                    update = {
                        // Update hook triggers the canGoBack / canGoForward updates
                        canGoBack = it.canGoBack()
                        canGoForward = it.canGoForward()
                    }
                )
            }
        }

        // Dedicated Mobile Web Browser Bottom Dashboard Command Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(1.dp, Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Navigation Control: Back
                IconButton(
                    onClick = { webViewInstance?.let { if (it.canGoBack()) it.goBack() } },
                    enabled = canGoBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (canGoBack) Slate950 else Slate950.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Mobile Go Back",
                        tint = if (canGoBack) EmeraldPrimary else Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Navigation Control: Forward
                IconButton(
                    onClick = { webViewInstance?.let { if (it.canGoForward()) it.goForward() } },
                    enabled = canGoForward,
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (canGoForward) Slate950 else Slate950.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        // Standard ArrowForward in Material design
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Mobile Go Forward",
                        tint = if (canGoForward) EmeraldPrimary else Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Core Browser Action: Reload
                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate950, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Page",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Home Navigation Action: Reset webView to Sandbox Dashboard
                IconButton(
                    onClick = {
                        webViewInstance?.loadUrl("file:///android_asset/web_app/index.html")
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Slate950, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Go Home Dashboard",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Switch back to Native Compose screen
                Button(
                    onClick = onBackToCompose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                        contentColor = EmeraldPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .testTag("switch_to_native_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📱 NATIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
