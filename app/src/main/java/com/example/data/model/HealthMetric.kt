package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val steps: Int,
    val activeMinutes: Int,
    val sleepHours: Float,
    val heartRate: Int,
    val hydrationMl: Int,
    val notes: String = ""
)
