package com.example.data.repository

import com.example.data.database.HealthMetricDao
import com.example.data.database.SyncEventDao
import com.example.data.model.HealthMetric
import com.example.data.model.SyncEvent
import kotlinx.coroutines.flow.Flow

class SyncRepository(
    private val syncEventDao: SyncEventDao,
    private val healthMetricDao: HealthMetricDao
) {
    val allEvents: Flow<List<SyncEvent>> = syncEventDao.getAllSyncEvents()
    val allMetrics: Flow<List<HealthMetric>> = healthMetricDao.getAllMetrics()

    suspend fun insertEvent(event: SyncEvent) {
        syncEventDao.insertSyncEvent(event)
    }

    suspend fun clearAllEvents() {
        syncEventDao.clearAllSyncEvents()
    }

    suspend fun insertMetric(metric: HealthMetric) {
        healthMetricDao.insertMetric(metric)
    }

    suspend fun deleteMetricById(id: Int) {
        healthMetricDao.deleteMetric(id)
    }

    suspend fun clearAllMetrics() {
        healthMetricDao.clearAllMetrics()
    }
}
