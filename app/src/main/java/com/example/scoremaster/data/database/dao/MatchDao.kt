package com.example.scoremaster.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scoremaster.data.database.entity.InningsEntity
import com.example.scoremaster.data.database.entity.MatchEntity
import com.example.scoremaster.data.database.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches ORDER BY id DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchByIdFlow(matchId: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY id ASC")
    fun getMatchesByTournament(tournamentId: Long): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Long): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInnings(innings: InningsEntity): Long

    @Update
    suspend fun updateInnings(innings: InningsEntity)

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY inningsNumber ASC")
    fun getInningsForMatch(matchId: Long): Flow<List<InningsEntity>>

    @Query("SELECT * FROM innings WHERE matchId = :matchId AND inningsNumber = :inningsNumber LIMIT 1")
    suspend fun getInningsByNumber(matchId: Long, inningsNumber: Int): InningsEntity?

    @Query("SELECT * FROM innings WHERE id = :inningsId")
    suspend fun getInningsById(inningsId: Long): InningsEntity?
}
