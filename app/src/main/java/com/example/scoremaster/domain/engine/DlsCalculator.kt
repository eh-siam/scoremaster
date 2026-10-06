package com.example.scoremaster.domain.engine

import com.example.scoremaster.domain.dls.MatchFormat
import com.example.scoremaster.domain.dls.OfficialDlsResourceProvider
import kotlin.math.floor

data class DlsCalculationResult(
    val team1ResourcePercent: Double,
    val team2ResourcePercent: Double,
    val dlsTargetRuns: Int,
    val dlsParScore: Int,
    val isTargetIncreased: Boolean,
    val explanationText: String
)

object DlsCalculator {

    private val resourceProvider = OfficialDlsResourceProvider()

    fun getResourcePercentage(oversRemaining: Double, wicketsLost: Int): Double {
        val overs = oversRemaining.toInt()
        val balls = ((oversRemaining - overs) * 6.0).toInt().coerceIn(0, 5)
        val wicketsRem = (10 - wicketsLost).coerceIn(1, 10)
        return resourceProvider.getResource(overs, balls, wicketsRem) ?: 0.0
    }

    fun calculateDlsTarget(
        team1Runs: Int,
        team1OversPlayed: Double,
        team1WicketsLost: Int,
        totalMatchOvers: Int,
        team2RevisedOvers: Int,
        isFirstInningsTerminated: Boolean = false
    ): DlsCalculationResult {
        val fullMatchOvers = totalMatchOvers.coerceAtLeast(1)
        val format = MatchFormat.fromOvers(fullMatchOvers)
        val gAverage = format.gParAverageScore

        val revisedOvers2 = team2RevisedOvers.coerceIn(1, fullMatchOvers)

        // Team 2 resource for revised overs
        val r2Percent = (resourceProvider.getResource(revisedOvers2, 0, 10) ?: 100.0).coerceIn(1.0, 100.0)

        val r1Percent = if (isFirstInningsTerminated && team1OversPlayed > 0.0) {
            // 1st innings was stopped prematurely by rain/interruption
            val r1Full = resourceProvider.getResource(fullMatchOvers, 0, 10) ?: 100.0
            val t1OversPlayedInt = team1OversPlayed.toInt()
            val t1BallsPlayed = ((team1OversPlayed - t1OversPlayedInt) * 6.0).toInt().coerceIn(0, 5)
            val remainingOvers1 = fullMatchOvers - t1OversPlayedInt - (if (t1BallsPlayed > 0) 1 else 0)
            val remainingBalls1 = if (t1BallsPlayed > 0) 6 - t1BallsPlayed else 0

            val r1LostPercent = if (team1OversPlayed < fullMatchOvers) {
                resourceProvider.getResource(remainingOvers1, remainingBalls1, 10 - team1WicketsLost) ?: 0.0
            } else {
                0.0
            }

            (r1Full - r1LostPercent).coerceIn(1.0, 100.0)
        } else {
            // Match overs revised for both teams before/during 1st innings
            (resourceProvider.getResource(revisedOvers2, 0, 10) ?: 100.0).coerceIn(1.0, 100.0)
        }

        val isTargetIncreased = r2Percent > r1Percent
        val targetDouble = if (r2Percent <= r1Percent) {
            if (r1Percent > 0.0 && isFirstInningsTerminated) {
                floor(team1Runs * (r2Percent / r1Percent)) + 1.0
            } else {
                (team1Runs + 1).toDouble()
            }
        } else {
            floor(team1Runs + gAverage * ((r2Percent - r1Percent) / 100.0)) + 1.0
        }

        val dlsTarget = targetDouble.toInt().coerceAtLeast(1)
        val dlsParScore = (dlsTarget - 1).coerceAtLeast(0)

        val r1Str = String.format(java.util.Locale.getDefault(), "%.1f", r1Percent)
        val r2Str = String.format(java.util.Locale.getDefault(), "%.1f", r2Percent)

        val explanation = when {
            isFirstInningsTerminated && team1OversPlayed > 0.0 -> {
                if (isTargetIncreased) {
                    "Team 1 lost overs prematurely (R1: $r1Str%). Target increased to $dlsTarget in $revisedOvers2 overs."
                } else {
                    "Team 1 innings terminated at ${String.format(java.util.Locale.getDefault(), "%.1f", team1OversPlayed)} ov (R1: $r1Str%). Target adjusted to $dlsTarget in $revisedOvers2 overs."
                }
            }
            revisedOvers2 < fullMatchOvers -> {
                "Match revised to $revisedOvers2 overs for both teams (R1: $r1Str% | R2: $r2Str%). Target: 1st Innings Score + 1."
            }
            else -> {
                "Standard match ($revisedOvers2 overs). Target will be 1st Innings Score + 1."
            }
        }

        return DlsCalculationResult(
            team1ResourcePercent = r1Percent,
            team2ResourcePercent = r2Percent,
            dlsTargetRuns = dlsTarget,
            dlsParScore = dlsParScore,
            isTargetIncreased = isTargetIncreased,
            explanationText = explanation
        )
    }
}
