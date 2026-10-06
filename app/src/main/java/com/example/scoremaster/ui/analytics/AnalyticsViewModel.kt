package com.example.scoremaster.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.engine.InningsCalculationResult
import com.example.scoremaster.domain.engine.TournamentLeaderboardCalculator
import com.example.scoremaster.domain.model.BattingStats
import com.example.scoremaster.domain.model.BowlingStats
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.TournamentLeaderboard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val matches: List<Match> = emptyList(),
    val leaderboard: TournamentLeaderboard = TournamentLeaderboard(),
    val highestScoreStat: BattingStats? = null,
    val bestBowlingStat: BowlingStats? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val matchesFlow = matchRepository.getAllMatches()

    val uiState: StateFlow<AnalyticsUiState> = matchesFlow
        .flatMapLatest { matches ->
            if (matches.isEmpty()) {
                flowOf(AnalyticsUiState(isLoading = false))
            } else {
                val inningsFlows = matches.map { match ->
                    matchRepository.getInningsForMatch(match.id).flatMapLatest { inningsList ->
                        if (inningsList.isEmpty()) {
                            flowOf(emptyList<InningsCalculationResult>())
                        } else {
                            val calcFlows = inningsList.map { inn ->
                                matchRepository.getBallEventsForInnings(inn.id).map { bEvents ->
                                    val p1List = matchRepository.getPlayersListForTeamInMatch(match.id, match.team1Id)
                                    val p2List = matchRepository.getPlayersListForTeamInMatch(match.id, match.team2Id)
                                    CricketScoringEngine.calculateInningsState(
                                        ballEvents = bEvents,
                                        allPlayers = p1List + p2List,
                                        initialStrikerId = inn.currentStrikerId,
                                        initialNonStrikerId = inn.currentNonStrikerId,
                                        initialBowlerId = inn.currentBowlerId
                                    )
                                }
                            }
                            combine(calcFlows) { it.toList() }
                        }
                    }
                }

                combine(inningsFlows) { resultsArray ->
                    val allCalcs = resultsArray.flatMap { it }
                    val leaderboard = TournamentLeaderboardCalculator.calculateLeaderboard(allCalcs)

                    val allBatting = allCalcs.flatMap { it.battingScorecard }
                    val highestScore = allBatting.maxByOrNull { it.runs }

                    val allBowling = allCalcs.flatMap { it.bowlingScorecard }
                    val bestBowling = allBowling.filter { it.legalBalls > 0 }
                        .maxWithOrNull(
                            compareBy<BowlingStats> { it.wickets }
                                .thenByDescending { -it.runsConceded }
                        )

                    AnalyticsUiState(
                        matches = matches,
                        leaderboard = leaderboard,
                        highestScoreStat = highestScore,
                        bestBowlingStat = bestBowling,
                        isLoading = false
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )
}
