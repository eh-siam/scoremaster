package com.example.scoremaster.data.repository

import com.example.scoremaster.domain.model.PointsTableEntry
import com.example.scoremaster.domain.model.Team
import com.example.scoremaster.domain.model.Tournament
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    fun getAllTournaments(): Flow<List<Tournament>>
    fun getTournamentByIdFlow(tournamentId: Long): Flow<Tournament?>
    suspend fun createTournament(
        name: String,
        numberOfTeams: Int,
        numberOfOvers: Int,
        venue: String?,
        matchDate: String,
        teamNames: List<String>
    ): Long

    fun getTeamsForTournament(tournamentId: Long): Flow<List<Team>>
    fun getPointsTableForTournament(tournamentId: Long): Flow<List<PointsTableEntry>>
}
