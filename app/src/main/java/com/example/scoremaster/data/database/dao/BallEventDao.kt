package com.example.scoremaster.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scoremaster.data.database.entity.BallEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBallEvent(ballEvent: BallEventEntity): Long

    @Query("SELECT * FROM ball_events WHERE inningsId = :inningsId ORDER BY id ASC")
    fun getBallEventsForInnings(inningsId: Long): Flow<List<BallEventEntity>>

    @Query("SELECT * FROM ball_events WHERE inningsId = :inningsId ORDER BY id ASC")
    suspend fun getBallEventsListForInnings(inningsId: Long): List<BallEventEntity>

    @Query("SELECT * FROM ball_events WHERE inningsId = :inningsId ORDER BY id DESC LIMIT 1")
    suspend fun getLastBallEvent(inningsId: Long): BallEventEntity?

    @Query("DELETE FROM ball_events WHERE id = :ballEventId")
    suspend fun deleteBallEvent(ballEventId: Long)
}
