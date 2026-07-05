package com.example.data.database

import androidx.room.*
import com.example.data.model.HealthMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC")
    fun getAllMetrics(): Flow<List<HealthMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: HealthMetric): Long

    @Query("DELETE FROM health_metrics WHERE id = :id")
    suspend fun deleteMetric(id: Int)

    @Query("DELETE FROM health_metrics")
    suspend fun clearAllMetrics()
}
