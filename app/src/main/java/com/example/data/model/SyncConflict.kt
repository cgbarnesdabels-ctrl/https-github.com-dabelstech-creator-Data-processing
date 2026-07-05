package com.example.data.model

data class SyncConflict(
    val id: String,
    val source: String,       // e.g. "Google Drive Repo", "Gmail Ledger Stream", "Google Calendar Sync"
    val timestamp: Long,
    val localDataSummary: String,
    val cloudDataSummary: String,
    val description: String
)
