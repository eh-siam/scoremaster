package com.example.scoremaster.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scoremaster.data.database.entity.DlsInterruptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DlsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterruption(interruption: DlsInterruptionEntity): Long

    @Query("SELECT * FROM dls_interruptions WHERE matchId = :matchId ORDER BY interruptionNumber ASC")
    fun getInterruptionsForMatch(matchId: Long): Flow<List<DlsInterruptionEntity>>

    @Query("SELECT * FROM dls_interruptions WHERE matchId = :matchId ORDER BY interruptionNumber ASC")
    suspend fun getInterruptionsListForMatch(matchId: Long): List<DlsInterruptionEntity>

    @Query("DELETE FROM dls_interruptions WHERE matchId = :matchId")
    suspend fun clearInterruptionsForMatch(matchId: Long)
}
