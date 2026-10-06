package com.example.scoremaster.domain.engine

import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.BattingStats
import com.example.scoremaster.domain.model.BowlingStats
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.Player
import com.example.scoremaster.domain.model.WicketType

data class ExtrasBreakdown(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0
) {
    val total: Int
        get() = wides + noBalls + byes + legByes
}

data class InningsCalculationResult(
    val totalRuns: Int,
    val totalWickets: Int,
    val legalBallsBowled: Int,
    val battingScorecard: List<BattingStats>,
    val bowlingScorecard: List<BowlingStats>,
    val extrasBreakdown: ExtrasBreakdown,
    val oversMap: Map<Int, List<BallEvent>>,
    val currentStrikerId: Long?,
    val currentNonStrikerId: Long?,
    val currentBowlerId: Long?
)

object CricketScoringEngine {

    fun calculateInningsState(
        ballEvents: List<BallEvent>,
        allPlayers: List<Player>,
        initialStrikerId: Long?,
        initialNonStrikerId: Long?,
        initialBowlerId: Long?
    ): InningsCalculationResult {
        var totalRuns = 0
        var totalWickets = 0
        var legalBalls = 0

        var wides = 0
        var noBalls = 0
        var byes = 0
        var legByes = 0

        val battingMap = mutableMapOf<Long, BattingStats>()
        val bowlingMap = mutableMapOf<Long, BowlingStats>()

        // Initialize players
        allPlayers.forEach { player ->
            battingMap[player.id] = BattingStats(player = player)
            bowlingMap[player.id] = BowlingStats(player = player)
        }

        var strikerId = initialStrikerId.takeIf { id -> id != null && id != 0L && allPlayers.any { it.id == id } }
            ?: allPlayers.getOrNull(0)?.id
        var nonStrikerId = initialNonStrikerId.takeIf { id -> id != null && id != 0L && allPlayers.any { it.id == id } }
            ?: allPlayers.getOrNull(1)?.id
        var bowlerId = initialBowlerId.takeIf { id -> id != null && id != 0L && allPlayers.any { it.id == id } }
            ?: allPlayers.getOrNull(2)?.id ?: allPlayers.getOrNull(0)?.id

        val oversMap = mutableMapOf<Int, MutableList<BallEvent>>()

        // Track per-over bowler runs for maidens
        val overBowlerRuns = mutableMapOf<Int, Int>()
        val overLegalBalls = mutableMapOf<Int, Int>()

        ballEvents.forEach { ball ->
            val overIndex = ball.overNumber
            oversMap.getOrPut(overIndex) { mutableListOf() }.add(ball)

            bowlerId = ball.bowlerId

            val runsOffBat = ball.runsOffBat
            val extraRuns = ball.extraRuns

            // 1. Score Accounting
            when (ball.extraType) {
                ExtraType.NONE -> {
                    totalRuns += runsOffBat
                }
                ExtraType.WIDE -> {
                    wides += extraRuns
                    totalRuns += extraRuns
                }
                ExtraType.NO_BALL -> {
                    noBalls += 1
                    totalRuns += (1 + runsOffBat + extraRuns)
                }
                ExtraType.BYE -> {
                    byes += extraRuns
                    totalRuns += extraRuns
                }
                ExtraType.LEG_BYE -> {
                    legByes += extraRuns
                    totalRuns += extraRuns
                }
            }

            // 2. Legal ball check
            val isLegal = ball.extraType != ExtraType.WIDE && ball.extraType != ExtraType.NO_BALL
            if (isLegal) {
                legalBalls++
                overLegalBalls[overIndex] = (overLegalBalls[overIndex] ?: 0) + 1
            }

            // 3. Batting Stats Update for Striker
            val currentStrikerStats = battingMap[ball.strikerId] ?: BattingStats(
                player = allPlayers.find { it.id == ball.strikerId } ?: Player(ball.strikerId, 0, 0, "Unknown")
            )

            var newRuns = currentStrikerStats.runs
            var newBalls = currentStrikerStats.balls
            var newFours = currentStrikerStats.fours
            var newSixes = currentStrikerStats.sixes

            if (ball.extraType != ExtraType.WIDE) {
                newBalls += 1 // Wide does not count as ball faced
            }

            if (ball.extraType == ExtraType.NONE || ball.extraType == ExtraType.NO_BALL) {
                newRuns += runsOffBat
                if (runsOffBat == 4) newFours++
                if (runsOffBat == 6) newSixes++
            }

            battingMap[ball.strikerId] = currentStrikerStats.copy(
                runs = newRuns,
                balls = newBalls,
                fours = newFours,
                sixes = newSixes
            )

            // 4. Bowling Stats Update
            val currentBowlerStats = bowlingMap[ball.bowlerId] ?: BowlingStats(
                player = allPlayers.find { it.id == ball.bowlerId } ?: Player(ball.bowlerId, 0, 0, "Bowler")
            )

            var bowlerLegalBalls = currentBowlerStats.legalBalls
            var bowlerRunsConceded = currentBowlerStats.runsConceded
            var bowlerWickets = currentBowlerStats.wickets

            if (isLegal) {
                bowlerLegalBalls += 1
            }

            val bowlerRunsForThisBall = when (ball.extraType) {
                ExtraType.NONE -> runsOffBat
                ExtraType.WIDE -> extraRuns
                ExtraType.NO_BALL -> 1 + runsOffBat
                ExtraType.BYE, ExtraType.LEG_BYE -> 0 // Byes/Leg byes do NOT count as bowler runs
            }

            bowlerRunsConceded += bowlerRunsForThisBall
            overBowlerRuns[overIndex] = (overBowlerRuns[overIndex] ?: 0) + bowlerRunsForThisBall

            // 5. Wickets
            if (ball.isWicket) {
                val isRetiredHurt = ball.wicketType == WicketType.RETIRED_HURT
                if (!isRetiredHurt) {
                    totalWickets++
                }
                val dismissedId = ball.dismissedPlayerId ?: ball.strikerId
                val dismissedPlayerStats = battingMap[dismissedId]
                if (dismissedPlayerStats != null) {
                    val fielderName = ball.fielderId?.let { fId -> allPlayers.find { it.id == fId }?.name }
                    val dismissalText = when (ball.wicketType) {
                        WicketType.BOWLED -> "b ${currentBowlerStats.player.name}"
                        WicketType.CAUGHT -> if (fielderName != null) "c $fielderName b ${currentBowlerStats.player.name}" else "c fielder b ${currentBowlerStats.player.name}"
                        WicketType.LBW -> "lbw b ${currentBowlerStats.player.name}"
                        WicketType.RUN_OUT -> if (fielderName != null) "run out ($fielderName)" else "run out"
                        WicketType.STUMPED -> if (fielderName != null) "st $fielderName b ${currentBowlerStats.player.name}" else "st keeper b ${currentBowlerStats.player.name}"
                        WicketType.HIT_WICKET -> "hit wicket b ${currentBowlerStats.player.name}"
                        WicketType.RETIRED_HURT -> "retired hurt"
                        WicketType.RETIRED_OUT, WicketType.RETIRED -> "retired out"
                        null -> "out"
                    }
                    battingMap[dismissedId] = dismissedPlayerStats.copy(
                        isOut = !isRetiredHurt,
                        dismissalInfo = dismissalText
                    )
                }

                // Credit wicket to bowler if bowler dismissal (not run out / retired)
                val isBowlerWicket = ball.wicketType != WicketType.RUN_OUT &&
                    ball.wicketType != WicketType.RETIRED &&
                    ball.wicketType != WicketType.RETIRED_HURT &&
                    ball.wicketType != WicketType.RETIRED_OUT
                if (isBowlerWicket) {
                    bowlerWickets += 1
                }

                // Replace dismissed player with new batsman if provided
                if (ball.newBatsmanId != null) {
                    if (dismissedId == strikerId) {
                        strikerId = ball.newBatsmanId
                    } else if (dismissedId == nonStrikerId) {
                        nonStrikerId = ball.newBatsmanId
                    }
                }
            }

            bowlingMap[ball.bowlerId] = currentBowlerStats.copy(
                legalBalls = bowlerLegalBalls,
                runsConceded = bowlerRunsConceded,
                wickets = bowlerWickets
            )

            // 6. Strike rotation
            // Rotate strike if odd runs physically run / scored
            val totalPhysicalRuns = when (ball.extraType) {
                ExtraType.NONE -> runsOffBat
                ExtraType.WIDE -> extraRuns - 1 // 1 wide + (extraRuns - 1) run taken
                ExtraType.NO_BALL -> runsOffBat + extraRuns
                ExtraType.BYE, ExtraType.LEG_BYE -> extraRuns
            }

            val shouldSwapStrike = (totalPhysicalRuns % 2 != 0)
            if (shouldSwapStrike && strikerId != null && nonStrikerId != null) {
                val temp = strikerId
                strikerId = nonStrikerId
                nonStrikerId = temp
            }

            // End of over check
            val ballsInCurrentOver = overLegalBalls[overIndex] ?: 0
            if (isLegal && ballsInCurrentOver == 6) {
                // Swap strike at end of over
                if (strikerId != null && nonStrikerId != null) {
                    val temp = strikerId
                    strikerId = nonStrikerId
                    nonStrikerId = temp
                }
            }
        }

        // Calculate maidens
        overLegalBalls.forEach { (overIdx, legalCount) ->
            if (legalCount == 6) {
                val bowlerRunsInOver = overBowlerRuns[overIdx] ?: 0
                if (bowlerRunsInOver == 0) {
                    // Find bowler for this over
                    val overBalls = oversMap[overIdx]
                    val bowlerForOver = overBalls?.firstOrNull()?.bowlerId
                    if (bowlerForOver != null) {
                        val bStats = bowlingMap[bowlerForOver]
                        if (bStats != null) {
                            bowlingMap[bowlerForOver] = bStats.copy(maidens = bStats.maidens + 1)
                        }
                    }
                }
            }
        }

        val battingList = battingMap.values.filter { it.balls > 0 || it.isOut || it.player.id == strikerId || it.player.id == nonStrikerId }
        val bowlingList = bowlingMap.values.filter { it.legalBalls > 0 || it.player.id == bowlerId }

        return InningsCalculationResult(
            totalRuns = totalRuns,
            totalWickets = totalWickets,
            legalBallsBowled = legalBalls,
            battingScorecard = battingList,
            bowlingScorecard = bowlingList,
            extrasBreakdown = ExtrasBreakdown(
                wides = wides,
                noBalls = noBalls,
                byes = byes,
                legByes = legByes
            ),
            oversMap = oversMap,
            currentStrikerId = strikerId,
            currentNonStrikerId = nonStrikerId,
            currentBowlerId = bowlerId
        )
    }
}
