package com.example.scoremaster

import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.Player
import com.example.scoremaster.domain.model.WicketType
import org.junit.Assert.assertEquals
import org.junit.Test

class CricketScoringEngineTest {

    private val player1 = Player(id = 1, matchId = 1, teamId = 10, name = "Batsman 1")
    private val player2 = Player(id = 2, matchId = 1, teamId = 10, name = "Batsman 2")
    private val bowler1 = Player(id = 3, matchId = 1, teamId = 20, name = "Bowler 1")
    private val allPlayers = listOf(player1, player2, bowler1)

    @Test
    fun testNormalRunsAndStrikeRotation() {
        val events = listOf(
            BallEvent(id = 1, inningsId = 1, overNumber = 0, ballNumberInOver = 1, isLegalBall = true, bowlerId = 3, strikerId = 1, nonStrikerId = 2, runsOffBat = 1),
            BallEvent(id = 2, inningsId = 1, overNumber = 0, ballNumberInOver = 2, isLegalBall = true, bowlerId = 3, strikerId = 2, nonStrikerId = 1, runsOffBat = 4),
            BallEvent(id = 3, inningsId = 1, overNumber = 0, ballNumberInOver = 3, isLegalBall = true, bowlerId = 3, strikerId = 2, nonStrikerId = 1, runsOffBat = 6)
        )

        val result = CricketScoringEngine.calculateInningsState(
            ballEvents = events,
            allPlayers = allPlayers,
            initialStrikerId = 1,
            initialNonStrikerId = 2,
            initialBowlerId = 3
        )

        assertEquals(11, result.totalRuns)
        assertEquals(0, result.totalWickets)
        assertEquals(3, result.legalBallsBowled)
        assertEquals(2L, result.currentStrikerId) // Striker swapped after 1 run
    }

    @Test
    fun testWideBallDoesNotIncrementLegalBall() {
        val events = listOf(
            BallEvent(id = 1, inningsId = 1, overNumber = 0, ballNumberInOver = 0, isLegalBall = false, bowlerId = 3, strikerId = 1, nonStrikerId = 2, runsOffBat = 0, extraType = ExtraType.WIDE, extraRuns = 1),
            BallEvent(id = 2, inningsId = 1, overNumber = 0, ballNumberInOver = 1, isLegalBall = true, bowlerId = 3, strikerId = 1, nonStrikerId = 2, runsOffBat = 2)
        )

        val result = CricketScoringEngine.calculateInningsState(
            ballEvents = events,
            allPlayers = allPlayers,
            initialStrikerId = 1,
            initialNonStrikerId = 2,
            initialBowlerId = 3
        )

        assertEquals(3, result.totalRuns)
        assertEquals(1, result.legalBallsBowled)
        assertEquals(1, result.extrasBreakdown.wides)
    }

    @Test
    fun testWicketIncrementsWicketCount() {
        val events = listOf(
            BallEvent(
                id = 1, inningsId = 1, overNumber = 0, ballNumberInOver = 1, isLegalBall = true,
                bowlerId = 3, strikerId = 1, nonStrikerId = 2, runsOffBat = 0,
                isWicket = true, wicketType = WicketType.BOWLED, dismissedPlayerId = 1
            )
        )

        val result = CricketScoringEngine.calculateInningsState(
            ballEvents = events,
            allPlayers = allPlayers,
            initialStrikerId = 1,
            initialNonStrikerId = 2,
            initialBowlerId = 3
        )

        assertEquals(0, result.totalRuns)
        assertEquals(1, result.totalWickets)
        assertEquals(1, result.legalBallsBowled)
    }
}
