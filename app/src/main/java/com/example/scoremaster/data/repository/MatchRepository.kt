package com.example.scoremaster.data.repository

import com.example.scoremaster.domain.dls.DlsInterruption
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getAllMatches(): Flow<List<Match>>
    fun getMatchByIdFlow(matchId: Long): Flow<Match?>
    suspend fun getMatchById(matchId: Long): Match?
    suspend fun updateMatch(match: Match)
    suspend fun createMatch(
        matchName: String?,
        team1Name: String,
        team2Name: String,
        numberOfOvers: Int,
        venue: String?,
        date: String,
        tournamentId: Long? = null
    ): Long

    suspend fun setupMatchPlayers(matchId: Long, team1Id: Long, team1Players: List<String>, team2Id: Long, team2Players: List<String>)
    fun getPlayersForTeamInMatch(matchId: Long, teamId: Long): Flow<List<Player>>
    suspend fun getPlayersListForTeamInMatch(matchId: Long, teamId: Long): List<Player>

    suspend fun startMatch(
        matchId: Long,
        battingTeamId: Long,
        bowlingTeamId: Long,
        openingStrikerId: Long,
        openingNonStrikerId: Long,
        startingBowlerId: Long
    ): Long
    fun getInningsForMatch(matchId: Long): Flow<List<Innings>>
    suspend fun getInningsByNumber(matchId: Long, inningsNumber: Int): Innings?
    suspend fun updateInnings(innings: Innings)

    suspend fun recordBallEvent(ballEvent: BallEvent)
    suspend fun undoLastBall(inningsId: Long)
    fun getBallEventsForInnings(inningsId: Long): Flow<List<BallEvent>>

    suspend fun startSecondInnings(matchId: Long, openingStrikerId: Long, openingNonStrikerId: Long, startingBowlerId: Long): Long
    suspend fun completeMatch(matchId: Long, winnerTeamId: Long?, winnerTeamName: String?, winningMargin: String, playerOfTheMatchId: Long?, playerOfTheMatchName: String?)

    fun getDlsInterruptions(matchId: Long): Flow<List<DlsInterruption>>
    suspend fun saveDlsInterruption(matchId: Long, interruption: DlsInterruption)
    suspend fun applyDlsTarget(matchId: Long, dlsRevisedOvers: Int, dlsRevisedTarget: Int, dlsParScore: Int)
}
