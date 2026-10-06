package com.example.scoremaster.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.engine.InningsCalculationResult
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchResultUiState(
    val match: Match? = null,
    val inningsList: List<Innings> = emptyList(),
    val innings1Calc: InningsCalculationResult? = null,
    val innings2Calc: InningsCalculationResult? = null,
    val allPlayers: List<Player> = emptyList(),
    val selectedPlayerOfTheMatch: Player? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MatchResultViewModel(
    val matchId: Long,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val matchFlow = matchRepository.getMatchByIdFlow(matchId)
    private val inningsListFlow = matchRepository.getInningsForMatch(matchId)

    val uiState: StateFlow<MatchResultUiState> = combine(
        matchFlow,
        inningsListFlow
    ) { match, inningsList ->
        match to inningsList
    }.flatMapLatest { (match, inningsList) ->
        if (match == null) {
            flowOf(MatchResultUiState())
        } else {
            val p1List = matchRepository.getPlayersListForTeamInMatch(matchId, match.team1Id)
            val p2List = matchRepository.getPlayersListForTeamInMatch(matchId, match.team2Id)
            val allP = p1List + p2List

            val inn1 = inningsList.filter { it.inningsNumber == 1 }.maxByOrNull { it.legalBallsBowled } ?: inningsList.find { it.inningsNumber == 1 }
            val inn2 = inningsList.filter { it.inningsNumber == 2 }.maxByOrNull { it.legalBallsBowled } ?: inningsList.find { it.inningsNumber == 2 }

            val ballEvents1Flow = inn1?.let { matchRepository.getBallEventsForInnings(it.id) } ?: flowOf(emptyList())
            val ballEvents2Flow = inn2?.let { matchRepository.getBallEventsForInnings(it.id) } ?: flowOf(emptyList())

            combine(ballEvents1Flow, ballEvents2Flow) { bEvents1, bEvents2 ->
                val calc1 = inn1?.let {
                    CricketScoringEngine.calculateInningsState(bEvents1, allP, it.currentStrikerId, it.currentNonStrikerId, it.currentBowlerId)
                }

                val calc2 = inn2?.let {
                    CricketScoringEngine.calculateInningsState(bEvents2, allP, it.currentStrikerId, it.currentNonStrikerId, it.currentBowlerId)
                }

                val pom = allP.find { it.id == match.playerOfTheMatchId }

                MatchResultUiState(
                    match = match,
                    inningsList = inningsList,
                    innings1Calc = calc1,
                    innings2Calc = calc2,
                    allPlayers = allP,
                    selectedPlayerOfTheMatch = pom
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatchResultUiState()
    )

    fun selectPlayerOfTheMatch(player: Player) {
        viewModelScope.launch {
            val currentMatch = uiState.value.match ?: return@launch
            matchRepository.completeMatch(
                matchId = matchId,
                winnerTeamId = currentMatch.winnerTeamId,
                winnerTeamName = currentMatch.winnerTeamName,
                winningMargin = currentMatch.winningMargin ?: "Completed",
                playerOfTheMatchId = player.id,
                playerOfTheMatchName = player.name
            )
        }
    }
}
