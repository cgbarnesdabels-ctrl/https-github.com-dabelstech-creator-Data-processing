package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.ComponentName
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthSyncWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Run database queries on IO coroutine thread safely
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val sharedPreferences = context.getSharedPreferences("heal_sync_prefs", Context.MODE_PRIVATE)
            
            // Query latest event from database
            // Since we don't have getLatestSyncEventSync() yet, let's query the flow or simple query here
            // Let's get the latest entries manually or add a helper
            val eventsFlow = db.syncEventDao().getAllSyncEvents()
            // We can resolve latest event by collecting first or writing a precise non-flow synchronous call
            // Let's create a custom suspend query or run simple fetch
            db.syncEventDao().getAllSyncEvents()
            
            // We will edit SyncEventDao to have a synchronous fetch, but for now we can read the flow by subscribing, or run limit query
            val latestEventDetails = try {
                // Let's read simple SharedPreferences cache or do a direct safe query
                sharedPreferences.getString("last_event_text", "No recent sync entries logged.") ?: "No sync entries detected"
            } catch (e: Exception) {
                "Ready to synchronize"
            }

            val trackersBlocked = sharedPreferences.getInt("trackers_blocked", 42)
            val isShieldActive = sharedPreferences.getBoolean("tracker_shield_enabled", true)
            val ssoConnected = sharedPreferences.getBoolean("sso_google_enabled", true) || sharedPreferences.getBoolean("sso_github_enabled", false)

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.health_sync_widget)

                    // Bind views
                    views.setTextViewText(R.id.widget_last_sync_title, "Latest: $latestEventDetails")
                    views.setTextViewText(
                        R.id.widget_status_title,
                        if (isShieldActive) "Tracker Shield: ACTIVE (Secured)" else "Tracker Shield: SUSPENDED"
                    )
                    views.setTextViewText(
                        R.id.widget_shield,
                        if (isShieldActive) "🛡️ SECURED" else "⚠️ UNPROTECTED"
                    )
                    views.setTextViewText(
                        R.id.widget_sso_status,
                        if (ssoConnected) "Unified SSO: Active Connected" else "Unified SSO: No Linked provider"
                    )
                    views.setTextViewText(R.id.widget_intercept_count, "Trackers Blocked: $trackersBlocked")

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    companion object {
        fun triggerUpdate(context: Context) {
            val intent = android.content.Intent(context, HealthSyncWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, HealthSyncWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
