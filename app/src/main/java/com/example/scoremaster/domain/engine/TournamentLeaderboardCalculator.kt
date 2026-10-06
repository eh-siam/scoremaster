package com.example.scoremaster.domain.engine

import com.example.scoremaster.domain.model.TopBatterEntry
import com.example.scoremaster.domain.model.TopBowlerEntry
import com.example.scoremaster.domain.model.TournamentLeaderboard

object TournamentLeaderboardCalculator {

    fun calculateLeaderboard(
        calculationResults: List<InningsCalculationResult>
    ): TournamentLeaderboard {
        val batterMap = mutableMapOf<Long, MutableBatterStats>()
        val bowlerMap = mutableMapOf<Long, MutableBowlerStats>()

        calculationResults.forEach { calc ->
            calc.battingScorecard.forEach { b ->
                if (b.balls > 0 || b.runs > 0) {
                    val entry = batterMap.getOrPut(b.player.id) {
                        MutableBatterStats(
                            playerId = b.player.id,
                            playerName = b.player.name,
                            teamName = "Team"
                        )
                    }
                    entry.totalRuns += b.runs
                    entry.ballsFaced += b.balls
                    entry.fours += b.fours
                    entry.sixes += b.sixes
                    entry.inningsCount++
                }
            }

            calc.bowlingScorecard.forEach { bw ->
                if (bw.legalBalls > 0) {
                    val entry = bowlerMap.getOrPut(bw.player.id) {
                        MutableBowlerStats(
                            playerId = bw.player.id,
                            playerName = bw.player.name,
                            teamName = "Team"
                        )
                    }
                    entry.totalWickets += bw.wickets
                    entry.legalBalls += bw.legalBalls
                    entry.runsConceded += bw.runsConceded
                    entry.inningsCount++
                }
            }
        }

        val sortedBatters = batterMap.values.map {
            TopBatterEntry(
                playerId = it.playerId,
                playerName = it.playerName,
                teamName = it.teamName,
                totalRuns = it.totalRuns,
                inningsCount = it.inningsCount,
                ballsFaced = it.ballsFaced,
                fours = it.fours,
                sixes = it.sixes
            )
        }.sortedByDescending { it.totalRuns }

        val sortedBowlers = bowlerMap.values.map {
            TopBowlerEntry(
                playerId = it.playerId,
                playerName = it.playerName,
                teamName = it.teamName,
                totalWickets = it.totalWickets,
                inningsCount = it.inningsCount,
                legalBalls = it.legalBalls,
                runsConceded = it.runsConceded
            )
        }.sortedWith(
            compareByDescending<TopBowlerEntry> { it.totalWickets }
                .thenBy { it.economy }
        )

        return TournamentLeaderboard(
            topBatters = sortedBatters,
            topBowlers = sortedBowlers
        )
    }

    private data class MutableBatterStats(
        val playerId: Long,
        val playerName: String,
        val teamName: String,
        var totalRuns: Int = 0,
        var ballsFaced: Int = 0,
        var fours: Int = 0,
        var sixes: Int = 0,
        var inningsCount: Int = 0
    )

    private data class MutableBowlerStats(
        val playerId: Long,
        val playerName: String,
        val teamName: String,
        var totalWickets: Int = 0,
        var legalBalls: Int = 0,
        var runsConceded: Int = 0,
        var inningsCount: Int = 0
    )
}
