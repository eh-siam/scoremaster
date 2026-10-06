package com.example.scoremaster.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scoremaster.data.database.entity.TeamEntity
import com.example.scoremaster.data.database.entity.TournamentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: Long): TournamentEntity?

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun getTournamentByIdFlow(tournamentId: Long): Flow<TournamentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>): List<Long>

    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId")
    fun getTeamsForTournament(tournamentId: Long): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId")
    suspend fun getTeamsListForTournament(tournamentId: Long): List<TeamEntity>
}
