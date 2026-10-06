package com.example.scoremaster.ui.scoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.domain.dls.DlsInterruption
import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.engine.InningsCalculationResult
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.Player
import com.example.scoremaster.domain.model.WicketType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LiveScoringUiState(
    val match: Match? = null,
    val innings: Innings? = null,
    val battingPlayers: List<Player> = emptyList(),
    val bowlingPlayers: List<Player> = emptyList(),
    val ballEvents: List<BallEvent> = emptyList(),
    val calculationResult: InningsCalculationResult? = null,
    val isMatchFinished: Boolean = false,
    val isSoundEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class LiveScoringViewModel(
    val matchId: Long,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val soundEffectsManager = SoundEffectsManager()
    private val _isSoundEnabled = MutableStateFlow(true)
    private val _currentInningsIndex = MutableStateFlow(1)

    fun toggleSound() {
        val enabled = soundEffectsManager.toggleSound()
        _isSoundEnabled.value = enabled
    }

    private val currentInningsFlow = combine(
        matchRepository.getInningsForMatch(matchId),
        _currentInningsIndex
    ) { inningsList, inningsIdx ->
        inningsList.find { it.inningsNumber == inningsIdx } ?: inningsList.firstOrNull()
    }

    private val ballEventsFlow = currentInningsFlow.flatMapLatest { innings ->
        if (innings != null) {
            matchRepository.getBallEventsForInnings(innings.id)
        } else {
            flowOf(emptyList())
        }
    }

    private val battingPlayersFlow = currentInningsFlow.flatMapLatest { innings ->
        if (innings != null) {
            matchRepository.getPlayersForTeamInMatch(matchId, innings.battingTeamId)
        } else {
            flowOf(emptyList())
        }
    }

    private val bowlingPlayersFlow = currentInningsFlow.flatMapLatest { innings ->
        if (innings != null) {
            matchRepository.getPlayersForTeamInMatch(matchId, innings.bowlingTeamId)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<LiveScoringUiState> = combine(
        matchRepository.getMatchByIdFlow(matchId),
        currentInningsFlow,
        ballEventsFlow,
        battingPlayersFlow,
        bowlingPlayersFlow
    ) { match, currentInnings, bEventsList, bPlayers, bwPlayers ->
        val isSound = _isSoundEnabled.value
        if (match != null) {
            _currentInningsIndex.value = match.currentInningsIndex
        }

        if (currentInnings == null || match == null) {
            LiveScoringUiState(match = match, isSoundEnabled = isSound)
        } else {
            val calcResult = CricketScoringEngine.calculateInningsState(
                ballEvents = bEventsList,
                allPlayers = bPlayers + bwPlayers,
                initialStrikerId = currentInnings.currentStrikerId,
                initialNonStrikerId = currentInnings.currentNonStrikerId,
                initialBowlerId = currentInnings.currentBowlerId
            )

            val updatedInnings = currentInnings.copy(
                totalRuns = calcResult.totalRuns,
                totalWickets = calcResult.totalWickets,
                legalBallsBowled = calcResult.legalBallsBowled,
                currentStrikerId = calcResult.currentStrikerId ?: currentInnings.currentStrikerId,
                currentNonStrikerId = calcResult.currentNonStrikerId ?: currentInnings.currentNonStrikerId,
                currentBowlerId = calcResult.currentBowlerId ?: currentInnings.currentBowlerId
            )

            LiveScoringUiState(
                match = match,
                innings = updatedInnings,
                battingPlayers = bPlayers,
                bowlingPlayers = bwPlayers,
                ballEvents = bEventsList,
                calculationResult = calcResult,
                isSoundEnabled = isSound
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LiveScoringUiState()
    )

    fun onRunScored(runs: Int) {
        val state = uiState.value
        val innings = state.innings ?: return
        val calc = state.calculationResult ?: return

        val strikerId = calc.currentStrikerId ?: return
        val nonStrikerId = calc.currentNonStrikerId ?: return
        val bowlerId = calc.currentBowlerId ?: return

        val legalBalls = calc.legalBallsBowled
        val overNumber = legalBalls / 6
        val ballInOver = (legalBalls % 6) + 1

        val ballEvent = BallEvent(
            inningsId = innings.id,
            overNumber = overNumber,
            ballNumberInOver = ballInOver,
            isLegalBall = true,
            bowlerId = bowlerId,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            runsOffBat = runs,
            extraType = ExtraType.NONE,
            extraRuns = 0
        )

        viewModelScope.launch {
            soundEffectsManager.playRunSound(runs)
            matchRepository.recordBallEvent(ballEvent)
            checkInningsAndMatchStatus()
        }
    }

    fun onExtraDelivered(extraType: ExtraType, runsOffBat: Int, extraRuns: Int) {
        val state = uiState.value
        val innings = state.innings ?: return
        val calc = state.calculationResult ?: return

        val strikerId = calc.currentStrikerId ?: return
        val nonStrikerId = calc.currentNonStrikerId ?: return
        val bowlerId = calc.currentBowlerId ?: return

        val legalBalls = calc.legalBallsBowled
        val overNumber = legalBalls / 6
        val ballInOver = (legalBalls % 6) + 1
        val isLegal = extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL

        val ballEvent = BallEvent(
            inningsId = innings.id,
            overNumber = overNumber,
            ballNumberInOver = if (isLegal) ballInOver else 0,
            isLegalBall = isLegal,
            bowlerId = bowlerId,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            runsOffBat = runsOffBat,
            extraType = extraType,
            extraRuns = extraRuns
        )

        viewModelScope.launch {
            matchRepository.recordBallEvent(ballEvent)
            checkInningsAndMatchStatus()
        }
    }

    fun onWicketFallen(wicketType: WicketType, dismissedPlayerId: Long, fielderId: Long?, newBatsmanId: Long?) {
        val state = uiState.value
        val innings = state.innings ?: return
        val calc = state.calculationResult ?: return

        val strikerId = calc.currentStrikerId ?: return
        val nonStrikerId = calc.currentNonStrikerId ?: return
        val bowlerId = calc.currentBowlerId ?: return

        val legalBalls = calc.legalBallsBowled
        val overNumber = legalBalls / 6
        val ballInOver = (legalBalls % 6) + 1

        val ballEvent = BallEvent(
            inningsId = innings.id,
            overNumber = overNumber,
            ballNumberInOver = ballInOver,
            isLegalBall = true,
            bowlerId = bowlerId,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            runsOffBat = 0,
            extraType = ExtraType.NONE,
            extraRuns = 0,
            isWicket = true,
            wicketType = wicketType,
            dismissedPlayerId = dismissedPlayerId,
            fielderId = fielderId,
            newBatsmanId = newBatsmanId
        )

        viewModelScope.launch {
            soundEffectsManager.playWicketSound()
            matchRepository.recordBallEvent(ballEvent)

            // If a new batsman is selected, update the striker/non-striker assignment
            if (newBatsmanId != null) {
                val updatedInnings = innings.copy(
                    currentStrikerId = if (dismissedPlayerId == strikerId) newBatsmanId else strikerId,
                    currentNonStrikerId = if (dismissedPlayerId == nonStrikerId) newBatsmanId else nonStrikerId
                )
                matchRepository.updateInnings(updatedInnings)
            }

            checkInningsAndMatchStatus()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEffectsManager.release()
    }

    fun onUndoLastBall() {
        val innings = uiState.value.innings ?: return
        viewModelScope.launch {
            matchRepository.undoLastBall(innings.id)
        }
    }

    fun swapStrike() {
        val state = uiState.value
        val innings = state.innings ?: return
        val calc = state.calculationResult ?: return

        val sId = calc.currentStrikerId ?: return
        val nsId = calc.currentNonStrikerId ?: return

        viewModelScope.launch {
            matchRepository.updateInnings(
                innings.copy(
                    currentStrikerId = nsId,
                    currentNonStrikerId = sId
                )
            )
        }
    }

    fun selectNewBowler(bowlerId: Long) {
        val innings = uiState.value.innings ?: return
        viewModelScope.launch {
            matchRepository.updateInnings(innings.copy(currentBowlerId = bowlerId))
        }
    }

    fun updateMatchOversAndTarget(
        revisedOvers: Int,
        revisedTarget: Int?,
        dlsParScore: Int? = null,
        interruption: DlsInterruption? = null,
        isTerminated: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        val state = uiState.value
        val match = state.match ?: return
        val innings = state.innings ?: return
        val calc = state.calculationResult

        viewModelScope.launch {
            val targetToApply = revisedTarget ?: match.dlsRevisedTarget
            val parScoreToApply = dlsParScore ?: targetToApply?.let { (it - 1).coerceAtLeast(0) } ?: 0

            if (targetToApply != null) {
                matchRepository.applyDlsTarget(
                    matchId = match.id,
                    dlsRevisedOvers = revisedOvers,
                    dlsRevisedTarget = targetToApply,
                    dlsParScore = parScoreToApply
                )
            } else {
                matchRepository.updateMatch(
                    match.copy(
                        numberOfOvers = revisedOvers,
                        dlsRevisedOvers = revisedOvers
                    )
                )
            }

            if (interruption != null) {
                matchRepository.saveDlsInterruption(match.id, interruption)
            }

            if (innings.inningsNumber == 1 && (isTerminated || (calc?.legalBallsBowled ?: 0) >= revisedOvers * 6)) {
                matchRepository.updateInnings(innings.copy(isCompleted = true))
            } else if (innings.inningsNumber == 2 && targetToApply != null) {
                matchRepository.updateInnings(innings.copy(targetRuns = targetToApply))
            } else if (innings.inningsNumber == 1) {
                val secondInnings = matchRepository.getInningsByNumber(match.id, 2)
                if (secondInnings != null && targetToApply != null) {
                    matchRepository.updateInnings(secondInnings.copy(targetRuns = targetToApply))
                }
            }

            checkInningsAndMatchStatus()
            onComplete?.invoke()
        }
    }

    fun startSecondInnings(
        openingStrikerId: Long,
        openingNonStrikerId: Long,
        startingBowlerId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            matchRepository.startSecondInnings(
                matchId = matchId,
                openingStrikerId = openingStrikerId,
                openingNonStrikerId = openingNonStrikerId,
                startingBowlerId = startingBowlerId
            )
            _currentInningsIndex.value = 2
            onSuccess()
        }
    }

    private suspend fun checkInningsAndMatchStatus() {
        val state = uiState.value
        val match = state.match ?: return
        val innings = state.innings ?: return
        val calc = state.calculationResult ?: return

        val totalMatchBalls = match.effectiveOvers * 6
        val isAllOut = calc.totalWickets >= 10
        val isOversCompleted = calc.legalBallsBowled >= totalMatchBalls

        if (match.currentInningsIndex == 1) {
            if (isAllOut || isOversCompleted) {
                matchRepository.updateInnings(innings.copy(isCompleted = true))
            }
        } else if (match.currentInningsIndex == 2) {
            val target = innings.targetRuns ?: (0 + 1)
            val isTargetReached = calc.totalRuns >= target

            if (isTargetReached || isAllOut || isOversCompleted) {
                matchRepository.updateInnings(innings.copy(isCompleted = true))

                // Determine Winner
                val (winnerId, winnerName, margin) = if (isTargetReached) {
                    val wicketsLeft = 10 - calc.totalWickets
                    Triple(innings.battingTeamId, innings.battingTeamName, "${innings.battingTeamName} won by $wicketsLeft wickets")
                } else if (calc.totalRuns == target - 1) {
                    Triple(null, null, "Match Tied")
                } else {
                    val runsMargin = (target - 1) - calc.totalRuns
                    Triple(innings.bowlingTeamId, innings.bowlingTeamName, "${innings.bowlingTeamName} won by $runsMargin runs")
                }

                matchRepository.completeMatch(
                    matchId = matchId,
                    winnerTeamId = winnerId,
                    winnerTeamName = winnerName,
                    winningMargin = margin,
                    playerOfTheMatchId = null,
                    playerOfTheMatchName = null
                )
            }
        }
    }
}
