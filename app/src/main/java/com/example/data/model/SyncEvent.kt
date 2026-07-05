package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_events")
data class SyncEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val eventType: String, // e.g. "Google Drive Sync", "Gmail Sync", "Calendar Sync", "Local Log"
    val status: String,    // "SUCCESS", "FAILED", "PENDING"
    val recordsSynced: Int,
    val details: String
)
