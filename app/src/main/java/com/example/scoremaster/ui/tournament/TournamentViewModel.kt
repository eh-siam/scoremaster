package com.example.scoremaster.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.data.repository.TournamentRepository
import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.engine.InningsCalculationResult
import com.example.scoremaster.domain.engine.TournamentLeaderboardCalculator
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.PointsTableEntry
import com.example.scoremaster.domain.model.Tournament
import com.example.scoremaster.domain.model.TournamentLeaderboard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TournamentUiState(
    val tournament: Tournament? = null,
    val matches: List<Match> = emptyList(),
    val pointsTable: List<PointsTableEntry> = emptyList(),
    val leaderboard: TournamentLeaderboard = TournamentLeaderboard()
)

class TournamentViewModel(
    val tournamentId: Long,
    private val tournamentRepository: TournamentRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    val uiState: StateFlow<TournamentUiState> = combine(
        tournamentRepository.getTournamentByIdFlow(tournamentId),
        matchRepository.getAllMatches(),
        tournamentRepository.getPointsTableForTournament(tournamentId)
    ) { tournament, allMatches, pointsTable ->
        val tournamentMatches = allMatches.filter { it.tournamentId == tournamentId }

        val calcResults = mutableListOf<InningsCalculationResult>()
        tournamentMatches.forEach { match ->
            val p1List = matchRepository.getPlayersListForTeamInMatch(match.id, match.team1Id)
            val p2List = matchRepository.getPlayersListForTeamInMatch(match.id, match.team2Id)
            val allP = p1List + p2List

            val inn1 = matchRepository.getInningsByNumber(match.id, 1)
            val inn2 = matchRepository.getInningsByNumber(match.id, 2)

            if (inn1 != null) {
                val bEvents1 = matchRepository.getBallEventsForInnings(inn1.id).first()
                calcResults.add(
                    CricketScoringEngine.calculateInningsState(
                        ballEvents = bEvents1,
                        allPlayers = allP,
                        initialStrikerId = inn1.currentStrikerId,
                        initialNonStrikerId = inn1.currentNonStrikerId,
                        initialBowlerId = inn1.currentBowlerId
                    )
                )
            }

            if (inn2 != null) {
                val bEvents2 = matchRepository.getBallEventsForInnings(inn2.id).first()
                calcResults.add(
                    CricketScoringEngine.calculateInningsState(
                        ballEvents = bEvents2,
                        allPlayers = allP,
                        initialStrikerId = inn2.currentStrikerId,
                        initialNonStrikerId = inn2.currentNonStrikerId,
                        initialBowlerId = inn2.currentBowlerId
                    )
                )
            }
        }

        val leaderboard = TournamentLeaderboardCalculator.calculateLeaderboard(calcResults)

        TournamentUiState(
            tournament = tournament,
            matches = tournamentMatches,
            pointsTable = pointsTable,
            leaderboard = leaderboard
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TournamentUiState()
    )

    fun generateAutoFixtures(onSuccess: () -> Unit) {
        val t = uiState.value.tournament ?: return
        val teams = t.teams
        if (teams.size < 2) return

        viewModelScope.launch {
            for (i in teams.indices) {
                for (j in i + 1 until teams.size) {
                    val team1 = teams[i]
                    val team2 = teams[j]

                    matchRepository.createMatch(
                        matchName = "${t.name}: ${team1.name} vs ${team2.name}",
                        team1Name = team1.name,
                        team2Name = team2.name,
                        numberOfOvers = t.numberOfOvers,
                        venue = t.venue,
                        date = t.matchDate,
                        tournamentId = tournamentId
                    )
                }
            }
            onSuccess()
        }
    }

    fun createMatchForTournament(
        team1Name: String,
        team2Name: String,
        onSuccess: (matchId: Long) -> Unit
    ) {
        val t = uiState.value.tournament ?: return
        viewModelScope.launch {
            val matchId = matchRepository.createMatch(
                matchName = "${t.name}: $team1Name vs $team2Name",
                team1Name = team1Name,
                team2Name = team2Name,
                numberOfOvers = t.numberOfOvers,
                venue = t.venue,
                date = t.matchDate,
                tournamentId = tournamentId
            )
            onSuccess(matchId)
        }
    }
}
