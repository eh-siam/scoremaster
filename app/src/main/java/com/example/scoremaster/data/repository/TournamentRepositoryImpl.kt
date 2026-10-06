package com.example.scoremaster.data.repository

import com.example.scoremaster.data.database.dao.MatchDao
import com.example.scoremaster.data.database.dao.TournamentDao
import com.example.scoremaster.data.database.entity.TeamEntity
import com.example.scoremaster.data.database.entity.TournamentEntity
import com.example.scoremaster.domain.engine.PointsTableCalculator
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.domain.model.PointsTableEntry
import com.example.scoremaster.domain.model.Team
import com.example.scoremaster.domain.model.Tournament
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TournamentRepositoryImpl(
    private val tournamentDao: TournamentDao,
    private val matchDao: MatchDao
) : TournamentRepository {

    override fun getAllTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getAllTournaments().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTournamentByIdFlow(tournamentId: Long): Flow<Tournament?> {
        return combine(
            tournamentDao.getTournamentByIdFlow(tournamentId),
            tournamentDao.getTeamsForTournament(tournamentId)
        ) { tournament, teams ->
            tournament?.toDomain(teams.map { it.toDomain() })
        }
    }

    override suspend fun createTournament(
        name: String,
        numberOfTeams: Int,
        numberOfOvers: Int,
        venue: String?,
        matchDate: String,
        teamNames: List<String>
    ): Long {
        val tournamentEntity = TournamentEntity(
            name = name,
            numberOfTeams = numberOfTeams,
            numberOfOvers = numberOfOvers,
            venue = venue,
            matchDate = matchDate
        )
        val tournamentId = tournamentDao.insertTournament(tournamentEntity)

        val teamEntities = teamNames.map {
            TeamEntity(tournamentId = tournamentId, name = it)
        }
        tournamentDao.insertTeams(teamEntities)

        return tournamentId
    }

    override fun getTeamsForTournament(tournamentId: Long): Flow<List<Team>> {
        return tournamentDao.getTeamsForTournament(tournamentId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPointsTableForTournament(tournamentId: Long): Flow<List<PointsTableEntry>> {
        return combine(
            tournamentDao.getTeamsForTournament(tournamentId),
            matchDao.getMatchesByTournament(tournamentId)
        ) { teams, matches ->
            val domainTeams = teams.map { it.toDomain() }
            val domainMatches = matches.map { m ->
                Match(
                    id = m.id,
                    tournamentId = m.tournamentId,
                    matchName = m.matchName,
                    team1Id = m.team1Id,
                    team2Id = m.team2Id,
                    team1Name = m.team1Name,
                    team2Name = m.team2Name,
                    numberOfOvers = m.numberOfOvers,
                    venue = m.venue,
                    date = m.date,
                    currentInningsIndex = m.currentInningsIndex,
                    status = runCatching { MatchStatus.valueOf(m.status) }.getOrDefault(MatchStatus.NOT_STARTED),
                    winnerTeamId = m.winnerTeamId,
                    winnerTeamName = m.winnerTeamName,
                    winningMargin = m.winningMargin,
                    playerOfTheMatchId = m.playerOfTheMatchId,
                    playerOfTheMatchName = m.playerOfTheMatchName
                )
            }

            // Simple Points Table calculation from matches
            PointsTableCalculator.calculatePointsTable(
                teams = domainTeams,
                matches = domainMatches,
                allInnings = emptyList() // Uses match winners and margins
            )
        }
    }

    private fun TournamentEntity.toDomain(teams: List<Team> = emptyList()) = Tournament(
        id = id,
        name = name,
        numberOfTeams = numberOfTeams,
        numberOfOvers = numberOfOvers,
        venue = venue,
        matchDate = matchDate,
        teams = teams
    )

    private fun TeamEntity.toDomain() = Team(
        id = id,
        tournamentId = tournamentId,
        matchId = matchId,
        name = name
    )
}
