package com.example.scoremaster.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scoremaster.data.database.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("DELETE FROM players WHERE matchId = :matchId")
    suspend fun deletePlayersForMatch(matchId: Long)

    @Query("SELECT * FROM players WHERE matchId = :matchId AND teamId = :teamId")
    fun getPlayersForTeamInMatch(matchId: Long, teamId: Long): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE matchId = :matchId AND teamId = :teamId")
    suspend fun getPlayersListForTeamInMatch(matchId: Long, teamId: Long): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Long): PlayerEntity?
}
