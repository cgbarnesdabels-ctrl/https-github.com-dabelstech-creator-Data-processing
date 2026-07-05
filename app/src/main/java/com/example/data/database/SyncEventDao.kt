package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SyncEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncEventDao {
    @Query("SELECT * FROM sync_events ORDER BY timestamp DESC")
    fun getAllSyncEvents(): Flow<List<SyncEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncEvent(event: SyncEvent): Long

    @Query("DELETE FROM sync_events")
    suspend fun clearAllSyncEvents()
}
