package com.example.ui.components

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.data.model.HealthMetric
import com.example.data.model.SyncEvent
import com.example.ui.viewmodel.SyncViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebAppBridge(
    private val context: Context,
    private val webView: WebView,
    private val viewModel: SyncViewModel,
    private val scope: CoroutineScope
) {
    private val gson = Gson()

    init {
        // Collect real-time updates from native Viewmodel StateFlows and stream them to standard Javascript callbacks!
        scope.launch(Dispatchers.Main) {
            viewModel.allEvents.collect { events ->
                pushEventsToWeb(events)
            }
        }

        scope.launch(Dispatchers.Main) {
            viewModel.allMetrics.collect { metrics ->
                pushMetricsToWeb(metrics)
            }
        }

        scope.launch(Dispatchers.Main) {
            // Combine all dynamic sync states and emit whenever any value flips
            kotlinx.coroutines.flow.combine(
                viewModel.isGoogleDriveEnabled,
                viewModel.isGmailEnabled,
                viewModel.isChromeEnabled,
                viewModel.isGoogleCalendarEnabled,
                viewModel.isGoogleContactsEnabled,
                viewModel.isGoogleSsoEnabled,
                viewModel.isGithubSsoEnabled,
                viewModel.isFitbitSsoEnabled,
                viewModel.isTrackerShieldEnabled,
                viewModel.isScreenshotSecurityEnabled,
                viewModel.trackersBlockedCount,
                viewModel.activeConflict
            ) { array ->
                createSyncStatesMap()
            }.collect { stateMap ->
                val json = gson.toJson(stateMap)
                evaluateJavascript("if (window.updateSyncStates) { window.updateSyncStates('$json'); }")
            }
        }
    }

    private fun evaluateJavascript(script: String) {
        webView.post {
            webView.evaluateJavascript(script, null)
        }
    }

    private fun pushEventsToWeb(events: List<SyncEvent>) {
        val json = gson.toJson(events)
        // Clean single quotes inside JSON payload to prevent escaping compilation breaks
        val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
        evaluateJavascript("if (window.updateEventsList) { window.updateEventsList('$escapedJson'); }")
    }

    private fun pushMetricsToWeb(metrics: List<HealthMetric>) {
        val json = gson.toJson(metrics)
        val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
        evaluateJavascript("if (window.updateMetrics) { window.updateMetrics('$escapedJson'); }")
    }

    private fun createSyncStatesMap(): Map<String, Any?> {
        return mapOf(
            "isGoogleDriveEnabled" to viewModel.isGoogleDriveEnabled.value,
            "isGmailEnabled" to viewModel.isGmailEnabled.value,
            "isChromeEnabled" to viewModel.isChromeEnabled.value,
            "isGoogleCalendarEnabled" to viewModel.isGoogleCalendarEnabled.value,
            "isGoogleContactsEnabled" to viewModel.isGoogleContactsEnabled.value,
            "isGoogleSsoEnabled" to viewModel.isGoogleSsoEnabled.value,
            "isGithubSsoEnabled" to viewModel.isGithubSsoEnabled.value,
            "isFitbitSsoEnabled" to viewModel.isFitbitSsoEnabled.value,
            "isTrackerShieldEnabled" to viewModel.isTrackerShieldEnabled.value,
            "isScreenshotSecurityEnabled" to viewModel.isScreenshotSecurityEnabled.value,
            "trackersBlockedCount" to viewModel.trackersBlockedCount.value,
            "activeConflict" to viewModel.activeConflict.value
        )
    }

    // WEB APPLICATION BRIDGE INTERACTION API IMPLEMENTATION:

    @JavascriptInterface
    fun requestEventsJson() {
        scope.launch(Dispatchers.Main) {
            pushEventsToWeb(viewModel.allEvents.value)
        }
    }

    @JavascriptInterface
    fun requestMetricsJson() {
        scope.launch(Dispatchers.Main) {
            pushMetricsToWeb(viewModel.allMetrics.value)
        }
    }

    @JavascriptInterface
    fun requestSyncStatesJson() {
        scope.launch(Dispatchers.Main) {
            val json = gson.toJson(createSyncStatesMap())
            evaluateJavascript("if (window.updateSyncStates) { window.updateSyncStates('$json'); }")
        }
    }

    @JavascriptInterface
    fun toggleService(service: String) {
        scope.launch(Dispatchers.Main) {
            when (service) {
                "drive" -> viewModel.setGoogleDriveEnabled(!viewModel.isGoogleDriveEnabled.value)
                "gmail" -> viewModel.setGmailEnabled(!viewModel.isGmailEnabled.value)
                "chrome" -> viewModel.setChromeEnabled(!viewModel.isChromeEnabled.value)
                "calendar" -> viewModel.setGoogleCalendarEnabled(!viewModel.isGoogleCalendarEnabled.value)
                "contacts" -> viewModel.setGoogleContactsEnabled(!viewModel.isGoogleContactsEnabled.value)
            }
        }
    }

    @JavascriptInterface
    fun toggleSso(sso: String) {
        scope.launch(Dispatchers.Main) {
            when (sso) {
                "google" -> viewModel.toggleGoogleSso(context)
                "github" -> viewModel.toggleGithubSso(context)
                "fitbit" -> viewModel.toggleFitbitSso(context)
            }
        }
    }

    @JavascriptInterface
    fun toggleTrackerShield() {
        scope.launch(Dispatchers.Main) {
            viewModel.toggleTrackerShield(context)
        }
    }

    @JavascriptInterface
    fun toggleScreenshotSecurity() {
        scope.launch(Dispatchers.Main) {
            viewModel.toggleScreenshotSecurity(context, null)
        }
    }

    @JavascriptInterface
    fun logMetric(steps: Int, activeMinutes: Int, sleepHours: Float, heartRate: Int, hydrationMl: Int, notes: String) {
        scope.launch(Dispatchers.Main) {
            viewModel.logMetricSnapshot(
                steps = steps,
                activeMinutes = activeMinutes,
                sleepHours = sleepHours,
                heartRate = heartRate,
                hydrationMl = hydrationMl,
                notes = notes
            )
        }
    }

    @JavascriptInterface
    fun resolveConflict(keepLocal: Boolean) {
        scope.launch(Dispatchers.Main) {
            viewModel.resolveConflict(keepLocal)
        }
    }

    @JavascriptInterface
    fun clearAllDashboardLogs() {
        scope.launch(Dispatchers.Main) {
            viewModel.clearAllDashboardLogs()
        }
    }

    @JavascriptInterface
    fun executeFastConnect() {
        scope.launch(Dispatchers.Main) {
            val job = viewModel.executeFastConnect()
            job.join()
            evaluateJavascript("if (window.onFastConnectComplete) { window.onFastConnectComplete(true, 'FAST CONNECT completed successfully! 4 secure services synchronized.'); }")
        }
    }

    @JavascriptInterface
    fun forceWidgetUpdate() {
        scope.launch(Dispatchers.Main) {
            com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
        }
    }

    @JavascriptInterface
    fun triggerNativeNotification(title: String, message: String) {
        scope.launch(Dispatchers.Main) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val channelId = "threshold_alerts"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    var channel = notificationManager.getNotificationChannel(channelId)
                    if (channel == null) {
                        channel = android.app.NotificationChannel(
                            channelId,
                            "Metric Threshold Alerts",
                            android.app.NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Triggers alerts when biometrics drift outside safe boundaries."
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }

                val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun logSignatureEvent(emailSubject: String, sender: String) {
        scope.launch(Dispatchers.Main) {
            viewModel.logSignatureEvent(emailSubject, sender)
        }
    }

    @JavascriptInterface
    fun exportEventsToCsv() {
        scope.launch(Dispatchers.Main) {
            viewModel.exportEventsToCsv(context, viewModel.allEvents.value) { error ->
                if (error != null) {
                    evaluateJavascript("alert('CSV Export failed $error');")
                } else {
                    evaluateJavascript("terminalLog('[✓ BRIDGE] Export CSV trigger processed successfully.');")
                }
            }
        }
    }

    @JavascriptInterface
    fun fetchCalendarEvents() {
        scope.launch(Dispatchers.Main) {
            val list = viewModel.getCalendarEventsList()
            val json = gson.toJson(list)
            val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
            evaluateJavascript("if (window.updateCalendarEvents) { window.updateCalendarEvents('$escapedJson'); }")
        }
    }

    @JavascriptInterface
    fun fetchGmailMessages() {
        scope.launch(Dispatchers.Main) {
            val list = viewModel.getGmailMessagesList()
            val json = gson.toJson(list)
            val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
            evaluateJavascript("if (window.updateGmailMessages) { window.updateGmailMessages('$escapedJson'); }")
        }
    }

    @JavascriptInterface
    fun fetchContacts() {
        scope.launch(Dispatchers.Main) {
            val list = viewModel.getContactsList()
            val json = gson.toJson(list)
            val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
            evaluateJavascript("if (window.updateContactsList) { window.updateContactsList('$escapedJson'); }")
        }
    }

    @JavascriptInterface
    fun fetchBrowserHistory() {
        scope.launch(Dispatchers.Main) {
            val list = viewModel.getBrowserHistoryList()
            val json = gson.toJson(list)
            val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
            evaluateJavascript("if (window.updateBrowserHistory) { window.updateBrowserHistory('$escapedJson'); }")
        }
    }
}
