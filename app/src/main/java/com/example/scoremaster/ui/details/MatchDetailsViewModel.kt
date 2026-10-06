package com.example.scoremaster.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.domain.model.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TossDecision { BAT, BOWL }

data class MatchDetailsUiState(
    val match: Match? = null,
    val team1Players: List<String> = List(11) { i -> "Player ${i + 1}" },
    val team2Players: List<String> = List(11) { i -> "Player ${i + 1}" },
    val tossWinnerTeamIndex: Int = 0, // 0 = Team 1, 1 = Team 2
    val tossDecision: TossDecision = TossDecision.BAT,
    val selectedStrikerIndex: Int = 0,
    val selectedNonStrikerIndex: Int = 1,
    val selectedBowlerIndex: Int = 0,
    val isLoading: Boolean = true
) {
    val isTeam1BattingFirst: Boolean
        get() = if (tossWinnerTeamIndex == 0) {
            tossDecision == TossDecision.BAT
        } else {
            tossDecision == TossDecision.BOWL
        }
}

sealed class MatchDetailsEvent {
    data class UpdateTeam1Player(val index: Int, val name: String) : MatchDetailsEvent()
    data class UpdateTeam2Player(val index: Int, val name: String) : MatchDetailsEvent()
    object AddTeam1Player : MatchDetailsEvent()
    object AddTeam2Player : MatchDetailsEvent()
    data class RemoveTeam1Player(val index: Int) : MatchDetailsEvent()
    data class RemoveTeam2Player(val index: Int) : MatchDetailsEvent()
    data class SelectTossWinner(val index: Int) : MatchDetailsEvent()
    data class SelectTossDecision(val decision: TossDecision) : MatchDetailsEvent()
    data class SelectStriker(val index: Int) : MatchDetailsEvent()
    data class SelectNonStriker(val index: Int) : MatchDetailsEvent()
    data class SelectBowler(val index: Int) : MatchDetailsEvent()
    data class SavePlayers(val onSuccess: (() -> Unit)? = null) : MatchDetailsEvent()
    data class StartMatch(val onSuccess: (matchId: Long) -> Unit) : MatchDetailsEvent()
}

class MatchDetailsViewModel(
    private val matchId: Long,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState: StateFlow<MatchDetailsUiState> = _uiState

    init {
        loadMatchDetails()
    }

    private fun loadMatchDetails() {
        viewModelScope.launch {
            matchRepository.getMatchByIdFlow(matchId).collect { currentMatch ->
                if (currentMatch != null) {
                    val p1List = matchRepository.getPlayersListForTeamInMatch(matchId, currentMatch.team1Id)
                    val p2List = matchRepository.getPlayersListForTeamInMatch(matchId, currentMatch.team2Id)

                    _uiState.update { state ->
                        state.copy(
                            match = currentMatch,
                            team1Players = if (p1List.isNotEmpty()) p1List.map { it.name } else state.team1Players,
                            team2Players = if (p2List.isNotEmpty()) p2List.map { it.name } else state.team2Players,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onEvent(event: MatchDetailsEvent) {
        when (event) {
            is MatchDetailsEvent.UpdateTeam1Player -> {
                _uiState.update { state ->
                    val current = state.team1Players.toMutableList()
                    if (event.index in current.indices) {
                        current[event.index] = event.name
                    }
                    state.copy(team1Players = current)
                }
            }
            is MatchDetailsEvent.UpdateTeam2Player -> {
                _uiState.update { state ->
                    val current = state.team2Players.toMutableList()
                    if (event.index in current.indices) {
                        current[event.index] = event.name
                    }
                    state.copy(team2Players = current)
                }
            }
            is MatchDetailsEvent.AddTeam1Player -> {
                _uiState.update { state ->
                    val current = state.team1Players.toMutableList()
                    current.add("Player ${current.size + 1}")
                    state.copy(team1Players = current)
                }
            }
            is MatchDetailsEvent.AddTeam2Player -> {
                _uiState.update { state ->
                    val current = state.team2Players.toMutableList()
                    current.add("Player ${current.size + 1}")
                    state.copy(team2Players = current)
                }
            }
            is MatchDetailsEvent.RemoveTeam1Player -> {
                _uiState.update { state ->
                    val current = state.team1Players.toMutableList()
                    if (event.index in current.indices) {
                        current.removeAt(event.index)
                    }
                    state.copy(team1Players = current)
                }
            }
            is MatchDetailsEvent.RemoveTeam2Player -> {
                _uiState.update { state ->
                    val current = state.team2Players.toMutableList()
                    if (event.index in current.indices) {
                        current.removeAt(event.index)
                    }
                    state.copy(team2Players = current)
                }
            }
            is MatchDetailsEvent.SelectTossWinner -> {
                _uiState.update { it.copy(tossWinnerTeamIndex = event.index) }
            }
            is MatchDetailsEvent.SelectTossDecision -> {
                _uiState.update { it.copy(tossDecision = event.decision) }
            }
            is MatchDetailsEvent.SelectStriker -> {
                _uiState.update { it.copy(selectedStrikerIndex = event.index) }
            }
            is MatchDetailsEvent.SelectNonStriker -> {
                _uiState.update { it.copy(selectedNonStrikerIndex = event.index) }
            }
            is MatchDetailsEvent.SelectBowler -> {
                _uiState.update { it.copy(selectedBowlerIndex = event.index) }
            }
            is MatchDetailsEvent.SavePlayers -> savePlayers(event.onSuccess)
            is MatchDetailsEvent.StartMatch -> startMatch(event.onSuccess)
        }
    }

    private fun savePlayers(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val state = uiState.value
            val currentMatch = state.match ?: return@launch

            val t1List = state.team1Players.mapIndexed { idx, name ->
                name.ifBlank { "Player ${idx + 1}" }
            }
            val t2List = state.team2Players.mapIndexed { idx, name ->
                name.ifBlank { "Player ${idx + 1}" }
            }

            matchRepository.setupMatchPlayers(
                matchId = matchId,
                team1Id = currentMatch.team1Id,
                team1Players = t1List,
                team2Id = currentMatch.team2Id,
                team2Players = t2List
            )

            onSuccess?.invoke()
        }
    }

    private fun startMatch(onSuccess: (matchId: Long) -> Unit) {
        viewModelScope.launch {
            val state = uiState.value
            val currentMatch = state.match ?: return@launch

            // Save players first
            val t1List = state.team1Players.mapIndexed { idx, name ->
                name.ifBlank { "Player ${idx + 1}" }
            }
            val t2List = state.team2Players.mapIndexed { idx, name ->
                name.ifBlank { "Player ${idx + 1}" }
            }

            matchRepository.setupMatchPlayers(
                matchId = matchId,
                team1Id = currentMatch.team1Id,
                team1Players = t1List,
                team2Id = currentMatch.team2Id,
                team2Players = t2List
            )

            val p1Entities = matchRepository.getPlayersListForTeamInMatch(matchId, currentMatch.team1Id)
            val p2Entities = matchRepository.getPlayersListForTeamInMatch(matchId, currentMatch.team2Id)

            val isT1BattingFirst = state.isTeam1BattingFirst

            val battingTeamId = if (isT1BattingFirst) currentMatch.team1Id else currentMatch.team2Id
            val bowlingTeamId = if (isT1BattingFirst) currentMatch.team2Id else currentMatch.team1Id

            val battingEntities = if (isT1BattingFirst) p1Entities else p2Entities
            val bowlingEntities = if (isT1BattingFirst) p2Entities else p1Entities

            val sIdx = state.selectedStrikerIndex.coerceIn(battingEntities.indices)
            val nsIdx = state.selectedNonStrikerIndex.coerceIn(battingEntities.indices)
            val bIdx = state.selectedBowlerIndex.coerceIn(bowlingEntities.indices)

            val striker = battingEntities.getOrNull(sIdx) ?: battingEntities.getOrNull(0)
            val nonStriker = battingEntities.getOrNull(nsIdx) ?: battingEntities.getOrNull(1)
            val bowler = bowlingEntities.getOrNull(bIdx) ?: bowlingEntities.getOrNull(0)

            if (striker != null && nonStriker != null && bowler != null) {
                matchRepository.startMatch(
                    matchId = matchId,
                    battingTeamId = battingTeamId,
                    bowlingTeamId = bowlingTeamId,
                    openingStrikerId = striker.id,
                    openingNonStrikerId = nonStriker.id,
                    startingBowlerId = bowler.id
                )
                onSuccess(matchId)
            }
        }
    }
}
