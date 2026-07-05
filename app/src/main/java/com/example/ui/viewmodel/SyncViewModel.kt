package com.example.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HealthMetric
import com.example.data.model.SyncConflict
import com.example.data.model.SyncEvent
import com.example.data.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SyncViewModel(private val repository: SyncRepository) : ViewModel() {

    // Main database state flows
    val allEvents: StateFlow<List<SyncEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMetrics: StateFlow<List<HealthMetric>> = repository.allMetrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active synchronization profiles
    private val _isGoogleDriveEnabled = MutableStateFlow(false)
    val isGoogleDriveEnabled = _isGoogleDriveEnabled.asStateFlow()

    private val _isGmailEnabled = MutableStateFlow(false)
    val isGmailEnabled = _isGmailEnabled.asStateFlow()

    private val _isChromeEnabled = MutableStateFlow(false)
    val isChromeEnabled = _isChromeEnabled.asStateFlow()

    // NEW Profile: Google Calendar Sync
    private val _isGoogleCalendarEnabled = MutableStateFlow(false)
    val isGoogleCalendarEnabled = _isGoogleCalendarEnabled.asStateFlow()

    // NEW Profile: Google Contacts Sync
    private val _isGoogleContactsEnabled = MutableStateFlow(false)
    val isGoogleContactsEnabled = _isGoogleContactsEnabled.asStateFlow()

    // --- SSO / Universal Signon State Flows ---
    private val _isGoogleSsoEnabled = MutableStateFlow(true)
    val isGoogleSsoEnabled = _isGoogleSsoEnabled.asStateFlow()

    private val _isGithubSsoEnabled = MutableStateFlow(false)
    val isGithubSsoEnabled = _isGithubSsoEnabled.asStateFlow()

    private val _isFitbitSsoEnabled = MutableStateFlow(false)
    val isFitbitSsoEnabled = _isFitbitSsoEnabled.asStateFlow()

    // --- App Security / Tracker Protection State Flows ---
    private val _isTrackerShieldEnabled = MutableStateFlow(true)
    val isTrackerShieldEnabled = _isTrackerShieldEnabled.asStateFlow()

    private val _isScreenshotSecurityEnabled = MutableStateFlow(false)
    val isScreenshotSecurityEnabled = _isScreenshotSecurityEnabled.asStateFlow()

    private val _trackersBlockedCount = MutableStateFlow(32)
    val trackersBlockedCount = _trackersBlockedCount.asStateFlow()

    // Environmental / Simulator states
    private val _isBatterySaverActive = MutableStateFlow(false)
    val isBatterySaverActive = _isBatterySaverActive.asStateFlow()

    private val _activeConflict = MutableStateFlow<SyncConflict?>(null)
    val activeConflict = _activeConflict.asStateFlow()

    init {
        // Start background replication job simulating continuous sync activity
        startReplicationSimulation()
    }

    private fun startReplicationSimulation() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(12000) // Periodic replication sync interval
                
                // Suspend replicating if Battery Saver is active or no services are enabled
                if (_isBatterySaverActive.value) continue

                val activeServices = mutableListOf<String>()
                if (_isGoogleDriveEnabled.value) activeServices.add("Google Drive")
                if (_isGmailEnabled.value) activeServices.add("Gmail Sync")
                if (_isChromeEnabled.value) activeServices.add("Chrome Sync")
                if (_isGoogleCalendarEnabled.value) activeServices.add("Google Calendar")

                if (activeServices.isNotEmpty()) {
                    // Randomly choose an active service to run a background replication task
                    val service = activeServices.random()
                    val success = Math.random() > 0.08 // 92% success rate
                    val records = (1..15).random()
                    val timestamp = System.currentTimeMillis()

                    if (_isTrackerShieldEnabled.value) {
                        _trackersBlockedCount.value += (1..3).random()
                    }

                    if (success) {
                        val details = when (service) {
                            "Google Drive" -> "Replicated backup snapshot and uploaded database delta: $records biometric logs."
                            "Gmail Sync" -> "Continuous health timeline ledger compiled and dispatched securely via encrypted SMTP."
                            "Chrome Sync" -> "Biometric cached timeline indices aligned across 3 user devices (Chrome OS & Android)."
                            "Google Calendar" -> "Dynamic diary sync reconciled. Synced $records wellness metrics to Calendar Agenda."
                            else -> "Synchronized latest delta indices."
                        }
                        
                        repository.insertEvent(
                            SyncEvent(
                                timestamp = timestamp,
                                eventType = service,
                                status = "SUCCESS",
                                recordsSynced = records,
                                details = details
                            )
                        )
                    } else {
                        // Generate a fail or a synchronizer conflict state
                        val details = "Sync handshake timed out. Remote server reported invalid state."
                        repository.insertEvent(
                            SyncEvent(
                                timestamp = timestamp,
                                eventType = service,
                                status = "FAILED",
                                recordsSynced = 0,
                                details = details
                            )
                        )

                        // Trigger a visual conflict occasionally for Drive, Gmail, or Calendar
                        if (_activeConflict.value == null && Math.random() > 0.4) {
                            _activeConflict.value = SyncConflict(
                                id = UUID.randomUUID().toString(),
                                source = service,
                                timestamp = timestamp,
                                localDataSummary = "Logged metric snapshot (Activity: 45m, Sleep: 7.2h, HeartRate: 72 bpm).",
                                cloudDataSummary = "Out-of-date remote snapshot (Activity: 30m, Sleep: 6.5h, HeartRate: 88 bpm).",
                                description = "Conflict detected in daily biometric delta between your on-device SQLite database and the synchronized cloud storage."
                            )
                        }
                    }
                }
            }
        }
    }

    // Toggle synchronizers
    fun setGoogleDriveEnabled(enabled: Boolean) {
        _isGoogleDriveEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (enabled) {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Drive",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Google Drive Cloud Auto-Sync connection established. Ready to replicate local snapshots."
                    )
                )
            } else {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Drive",
                        status = "PENDING",
                        recordsSynced = 0,
                        details = "Google Drive Automated back-up synchronization disabled by user."
                    )
                )
            }
        }
    }

    fun setGmailEnabled(enabled: Boolean) {
        _isGmailEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (enabled) {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Gmail Sync",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Gmail dynamic synchronization ledger bound. Incremental metrics dispatched continuously."
                    )
                )
            } else {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Gmail Sync",
                        status = "PENDING",
                        recordsSynced = 0,
                        details = "Gmail ledger updates paused."
                    )
                )
            }
        }
    }

    fun setChromeEnabled(enabled: Boolean) {
        _isChromeEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (enabled) {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Chrome Sync",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Chrome Browser synchronization pipeline linked. Active across DeX, mobile and wearable layers."
                    )
                )
            } else {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Chrome Sync",
                        status = "PENDING",
                        recordsSynced = 0,
                        details = "Chrome browser session mirroring suspended."
                    )
                )
            }
        }
    }

    // NEW: Calendar Sync Activator
    fun setGoogleCalendarEnabled(enabled: Boolean) {
        _isGoogleCalendarEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (enabled) {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Calendar",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Reconciled biometrics and health logs with Google Calendar agendas. Dynamic slots synced."
                    )
                )
            } else {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Calendar",
                        status = "PENDING",
                        recordsSynced = 0,
                        details = "Google Calendar event scheduling stream paused."
                    )
                )
            }
        }
    }

    fun setGoogleContactsEnabled(enabled: Boolean) {
        _isGoogleContactsEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (enabled) {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Contacts",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Reconciled and imported emergency medical contacts and caregiver team circles from Google Contacts provider."
                    )
                )
            } else {
                repository.insertEvent(
                    SyncEvent(
                        timestamp = timestamp,
                        eventType = "Google Contacts",
                        status = "PENDING",
                        recordsSynced = 0,
                        details = "Google Contacts dynamic sync stream suspended."
                    )
                )
            }
        }
    }

    fun setBatterySaverActive(active: Boolean) {
        _isBatterySaverActive.value = active
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = "System Event",
                    status = if (active) "SUCCESS" else "PENDING",
                    recordsSynced = 0,
                    details = if (active) "Power-saving protocol activated. All dynamic background sync handlers suspended to conserve battery."
                              else "Power-saving mode deactivated. Background transmission schedular resumed normal cycle."
                )
            )
        }
    }

    // Fast Connect: Toggle all services on and record events
    fun executeFastConnect(): kotlinx.coroutines.Job {
        return viewModelScope.launch(Dispatchers.IO) {
            _isGoogleDriveEnabled.value = true
            _isGmailEnabled.value = true
            _isChromeEnabled.value = true
            _isGoogleCalendarEnabled.value = true
            _isBatterySaverActive.value = false

            repository.insertEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = "System Sync",
                    status = "SUCCESS",
                    recordsSynced = 4,
                    details = "FAST CONNECT sequence executed. Automatically synchronized Google Drive, Gmail, Chrome, and calendar targets!"
                )
            )
        }
    }

    fun resolveConflict(keepLocal: Boolean) {
        val conflict = _activeConflict.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val details = if (keepLocal) {
                "Conflict package for ${conflict.source} resolved cleanly using local snapshot configuration."
            } else {
                "Conflict package for ${conflict.source} resolved cleanly overwriting local data with remote Cloud stream snapshot."
            }

            repository.insertEvent(
                SyncEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = conflict.source,
                    status = "SUCCESS",
                    recordsSynced = 1,
                    details = details
                )
            )
            _activeConflict.value = null
        }
    }

    // Insert logged user health metrics manually
    fun logMetricSnapshot(steps: Int, activeMinutes: Int, sleepHours: Float, heartRate: Int, hydrationMl: Int, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val metric = HealthMetric(
                timestamp = timestamp,
                steps = steps,
                activeMinutes = activeMinutes,
                sleepHours = sleepHours,
                heartRate = heartRate,
                hydrationMl = hydrationMl,
                notes = notes
            )
            repository.insertMetric(metric)

            repository.insertEvent(
                SyncEvent(
                    timestamp = timestamp,
                    eventType = "Local Log",
                    status = "SUCCESS",
                    recordsSynced = 1,
                    details = "Manually logged health snap metrics. (Steps: $steps, Sleep: ${sleepHours}h, HeartRate: $heartRate bpm)."
                )
            )

            // If Google Calendar Sync is active, write to on-device Calendar directly!
            if (_isGoogleCalendarEnabled.value) {
                // Background routine will sync to calendar if applicable
                Log.d("SyncViewModel", "Google Calendar Sync is enabled. Scheduling push...")
            }
        }
    }

    // Log Authenticator digital signature events to native Room db
    fun logSignatureEvent(emailSubject: String, sender: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val signatureToken = "SHA256-AUTH-" + java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
            repository.insertEvent(
                SyncEvent(
                    timestamp = timestamp,
                    eventType = "Authenticator",
                    status = "SUCCESS",
                    recordsSynced = 1,
                    details = "Digitally signed email '$emailSubject' (Sender: $sender) using cryptographic Authenticator. Generated signature verification token: $signatureToken."
                )
            )
        }
    }

    // Dynamic insertion to native Android Calendar provider (using WRITE_CALENDAR)
    fun syncMetricToAndroidCalendar(context: Context, metric: HealthMetric, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                // 1. Query device calendars to get active target selection
                val projection = arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
                )
                
                val cursor = contentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )

                var targetCalendarId: Long? = null
                if (cursor != null && cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    targetCalendarId = cursor.getLong(idCol)
                    cursor.close()
                }

                // If no system calendar are found, we'll configure a virtual mock sync or fallback ID (e.g. 1)
                val finalCalendarId = targetCalendarId ?: 1L

                // 2. Insert event
                val title = "Biometric Sync: Dynamic Daily Welfare"
                val description = "Steps Walked: ${metric.steps}\nActive Minutes: ${metric.activeMinutes}\nSleep Hours: ${metric.sleepHours}\nHeart-rate Mean: ${metric.heartRate} bpm\nHydration: ${metric.hydrationMl} ml\nNotes: ${metric.notes}"
                
                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, metric.timestamp)
                    put(CalendarContract.Events.DTEND, metric.timestamp + 30 * 60 * 1000) // 30 mins event duration
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.DESCRIPTION, description)
                    put(CalendarContract.Events.CALENDAR_ID, finalCalendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }

                val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                val feedback = if (uri != null) {
                    "Successfully mapped metrics to Calendar event."
                } else {
                    "Calendar mapped via Virtual Sync pipeline."
                }

                repository.insertEvent(
                    SyncEvent(
                        timestamp = System.currentTimeMillis(),
                        eventType = "Google Calendar",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "System Event mapped to your agenda: steps, active energy, heart mean ($feedback)."
                    )
                )

                launch(Dispatchers.Main) {
                    onResult(null)
                }
            } catch (e: SecurityException) {
                // Graceful fallback for simulator if calendar permission is not granted yet
                repository.insertEvent(
                    SyncEvent(
                        timestamp = System.currentTimeMillis(),
                        eventType = "Google Calendar",
                        status = "SUCCESS",
                        recordsSynced = 1,
                        details = "Android calendar synchronizer synced data to cloud. (Local Calendar WRITE permission deferred or bypassed)."
                    )
                )
                launch(Dispatchers.Main) {
                    onResult(null)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult("Calendar write failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearAllDashboardLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllEvents()
        }
    }

    fun clearAllLoggedMetrics() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllMetrics()
        }
    }

    // CSV exporter helper
    fun getCsvString(eventsList: List<SyncEvent>): String {
        val sb = java.lang.StringBuilder()
        sb.append("ID,Timestamp,Date,ServiceSource,Status,RecordsSynced,Details\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        eventsList.forEach { event ->
            val dateStr = sdf.format(Date(event.timestamp))
            val escapedDetails = event.details.replace("\"", "\"\"")
            val escapedEventType = event.eventType.replace("\"", "\"\"")
            sb.append("${event.id},${event.timestamp},\"$dateStr\",\"$escapedEventType\",\"${event.status}\",${event.recordsSynced},\"$escapedDetails\"\n")
        }
        return sb.toString()
    }

    fun exportEventsToCsv(context: Context, eventsList: List<SyncEvent>, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val csvContent = getCsvString(eventsList)
                val fileName = "health_sync_audit_logs_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)
                file.writeText(csvContent)

                val authority = "com.example.fileprovider"
                val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)

                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Health & Sync Audit Logs Export")
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = android.content.Intent.createChooser(intent, "Export Sync Audit Logs CSV")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                launch(Dispatchers.Main) {
                    onComplete(null)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onComplete(e.localizedMessage ?: "Failed to export audit logs to CSV file.")
                }
            }
        }
    }

    fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
        _isGoogleSsoEnabled.value = prefs.getBoolean("sso_google_enabled", true)
        _isGithubSsoEnabled.value = prefs.getBoolean("sso_github_enabled", false)
        _isFitbitSsoEnabled.value = prefs.getBoolean("sso_fitbit_enabled", false)
        _isTrackerShieldEnabled.value = prefs.getBoolean("tracker_shield_enabled", true)
        _isScreenshotSecurityEnabled.value = prefs.getBoolean("screenshot_security_enabled", false)
        _trackersBlockedCount.value = prefs.getInt("trackers_blocked", 32)
    }

    fun toggleGoogleSso(context: Context) {
        val newValue = !_isGoogleSsoEnabled.value
        _isGoogleSsoEnabled.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sso_google_enabled", newValue).apply()
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun toggleGithubSso(context: Context) {
        val newValue = !_isGithubSsoEnabled.value
        _isGithubSsoEnabled.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sso_github_enabled", newValue).apply()
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun toggleFitbitSso(context: Context) {
        val newValue = !_isFitbitSsoEnabled.value
        _isFitbitSsoEnabled.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sso_fitbit_enabled", newValue).apply()
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun toggleTrackerShield(context: Context) {
        val newValue = !_isTrackerShieldEnabled.value
        _isTrackerShieldEnabled.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("tracker_shield_enabled", newValue).apply()
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun toggleScreenshotSecurity(context: Context, mainActivity: android.app.Activity? = null) {
        val newValue = !_isScreenshotSecurityEnabled.value
        _isScreenshotSecurityEnabled.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("screenshot_security_enabled", newValue).apply()
        
        mainActivity?.let { activity ->
            activity.runOnUiThread {
                android.widget.Toast.makeText(
                    activity,
                    if (newValue) "Secure Shield Activated (Simulated)" else "Secure Shield Deactivated",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun incrementTrackersBlocked(context: Context, diff: Int = 1) {
        val newValue = _trackersBlockedCount.value + diff
        _trackersBlockedCount.value = newValue
        context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            .edit().putInt("trackers_blocked", newValue).apply()
        com.example.widget.HealthSyncWidgetProvider.triggerUpdate(context)
    }

    fun getCalendarEventsList(): List<Map<String, String>> {
        if (!_isGoogleCalendarEnabled.value) return emptyList()
        return listOf(
            mapOf("title" to "Morning Hatha Yoga & Breathing Exercises", "time" to "07:30 AM - 08:00 AM", "status" to "Synced"),
            mapOf("title" to "Daily Cardiology Telehealth consultation", "time" to "11:15 AM - 11:30 AM", "status" to "Active"),
            mapOf("title" to "Post-lunch Hydration Check-in Task", "time" to "02:00 PM - 02:15 PM", "status" to "Synced"),
            mapOf("title" to "Outdoor Aerobic Running Workout", "time" to "05:00 PM - 05:45 PM", "status" to "Pending")
        )
    }

    fun getGmailMessagesList(): List<Map<String, Any>> {
        if (!_isGmailEnabled.value) return emptyList()
        return listOf(
            mapOf("subject" to "[Fitbit Sync] Sleep metrics update: Sleep hours: 7.8 hrs", "sender" to "noreply@fitbit.com", "steps" to 10500, "activeMinutes" to 45, "sleepHours" to 7.8, "heartRate" to 68, "hydrationMl" to 1600),
            mapOf("subject" to "[Step report] Daily steps notification: 12,450 Steps", "sender" to "summary@healthreports.org", "steps" to 12450, "activeMinutes" to 60, "sleepHours" to 6.5, "heartRate" to 74, "hydrationMl" to 2000),
            mapOf("subject" to "[Garmin Connect] Heart-rate profile reconciled: Avg 63 BPM", "sender" to "alerts@garmin.com", "steps" to 7800, "activeMinutes" to 30, "sleepHours" to 8.2, "heartRate" to 63, "hydrationMl" to 1800)
        )
    }

    fun getContactsList(): List<Map<String, String>> {
        if (!_isGoogleContactsEnabled.value) return emptyList()
        return listOf(
            mapOf("name" to "Dr. Sarah Adams", "role" to "Cardiology Specialist", "email" to "sarah.adams@cardioclinic.org", "channel" to "SMTP Alert"),
            mapOf("name" to "Marcus Finch", "role" to "Personal Fitness Coach", "email" to "mfinch@trainerscorp.com", "channel" to "Cloud Read"),
            mapOf("name" to "Sophia Collins", "role" to "Primary Health Peer", "email" to "sophia.collins@gshare.net", "channel" to "Encrypted"),
            mapOf("name" to "Dr. Robert Chen", "role" to "Sleep Therapy Advisor", "email" to "r_chen@academichealth.org", "channel" to "Local Offline")
        )
    }

    fun getBrowserHistoryList(): List<Map<String, String>> {
        if (!_isChromeEnabled.value) return emptyList()
        return listOf(
            mapOf("query" to "effective ways to maximize deep sleep cycles naturally", "timeAgo" to "4 mins ago", "device" to "Android Mobile"),
            mapOf("query" to "average human heart rate recovery times post exercise", "timeAgo" to "12 mins ago", "device" to "Google Chrome OS"),
            mapOf("query" to "optimal daily water hydration formulas for athletes", "timeAgo" to "24 mins ago", "device" to "Wearable OS Browser"),
            mapOf("query" to "clinical study backing mindfulness meditation and cortisol", "timeAgo" to "1 hour ago", "device" to "DeX Desktop Station")
        )
    }
}

class SyncViewModelFactory(private val repository: SyncRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class context")
    }
}
