package com.example.scoremaster.domain.dls

import kotlin.math.floor

class DlsCalculator(
    private val resourceProvider: DlsResourceProvider = OfficialDlsResourceProvider()
) {

    fun calculate(input: DlsInput): DlsResult {
        val format = input.matchFormat
        if (input.scheduledOvers > format.maxOvers || input.scheduledOvers <= 0) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.CALCULATION_UNAVAILABLE,
                calculationAvailable = false,
                errorMessage = "Invalid scheduled overs ${input.scheduledOvers} for ${format.displayName}."
            )
        }

        if (input.firstInningsScore < 0 || input.secondInningsCurrentScore < 0) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.CALCULATION_UNAVAILABLE,
                calculationAvailable = false,
                errorMessage = "Scores cannot be negative."
            )
        }

        if (input.firstInningsWickets !in 0..10 || input.secondInningsWicketsLost !in 0..10) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.CALCULATION_UNAVAILABLE,
                calculationAvailable = false,
                errorMessage = "Wickets lost must be between 0 and 10."
            )
        }

        if (input.revisedOvers > input.scheduledOvers || input.revisedOvers < 0) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.CALCULATION_UNAVAILABLE,
                calculationAvailable = false,
                errorMessage = "Revised overs cannot exceed scheduled overs (${input.scheduledOvers})."
            )
        }

        if (input.isMatchAbandoned) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.MATCH_ABANDONED,
                calculationAvailable = false,
                errorMessage = "Match has been abandoned."
            )
        }

        if (input.revisedOvers < format.minOversForDls) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.NO_RESULT,
                calculationAvailable = false,
                errorMessage = "DLS result cannot be determined yet. Minimum ${format.minOversForDls} overs required for ${format.displayName}."
            )
        }

        val r1Full = resourceProvider.getResource(
            oversRemaining = input.scheduledOvers,
            ballsRemaining = 0,
            wicketsRemaining = 10
        )

        val r2Full = resourceProvider.getResource(
            oversRemaining = input.revisedOvers,
            ballsRemaining = 0,
            wicketsRemaining = 10
        )

        if (r1Full == null || r2Full == null) {
            return DlsResult(
                revisedOvers = input.revisedOvers,
                status = DlsStatus.CALCULATION_UNAVAILABLE,
                calculationAvailable = false,
                errorMessage = "Official DLS calculation data is not configured."
            )
        }

        val r1Losses = 0.0
        var r2Losses = 0.0

        input.interruptions.forEach { interruption ->
            val oversAtInterruptionInt = interruption.oversAtInterruption.toInt()
            val ballsAtInterruptionInt = ((interruption.oversAtInterruption - oversAtInterruptionInt) * 6).toInt()
            val wicketsRemainingAtInterruption = 10 - interruption.wicketsAtInterruption

            val resBefore = resourceProvider.getResource(
                oversRemaining = interruption.scheduledOversBeforeInterruption - oversAtInterruptionInt,
                ballsRemaining = (6 - ballsAtInterruptionInt) % 6,
                wicketsRemaining = wicketsRemainingAtInterruption
            ) ?: 0.0

            val resAfter = resourceProvider.getResource(
                oversRemaining = interruption.revisedOversAfterInterruption - oversAtInterruptionInt,
                ballsRemaining = (6 - ballsAtInterruptionInt) % 6,
                wicketsRemaining = wicketsRemainingAtInterruption
            ) ?: 0.0

            val resLost = (resBefore - resAfter).coerceAtLeast(0.0)
            r2Losses += resLost
        }

        val r1Total = (r1Full - r1Losses).coerceAtLeast(1.0)
        val r2Total = (r2Full - r2Losses).coerceAtLeast(1.0)

        val targetDouble = if (r2Total <= r1Total) {
            floor(input.firstInningsScore * (r2Total / r1Total)) + 1.0
        } else {
            floor(input.firstInningsScore + format.gParAverageScore * ((r2Total - r1Total) / 100.0)) + 1.0
        }

        val revisedTarget = targetDouble.toInt().coerceAtLeast(1)
        val parScore = (revisedTarget - 1).coerceAtLeast(0)
        val runsRequired = (revisedTarget - input.secondInningsCurrentScore).coerceAtLeast(0)

        val resultStatus = when {
            input.secondInningsCurrentScore >= revisedTarget -> DlsStatus.TEAM_2_WINS
            input.secondInningsOversCompleted >= input.revisedOvers || input.secondInningsWicketsLost >= 10 -> {
                if (input.secondInningsCurrentScore == parScore) DlsStatus.TIED
                else if (input.secondInningsCurrentScore > parScore) DlsStatus.TEAM_2_WINS
                else DlsStatus.TEAM_1_WINS
            }
            else -> DlsStatus.DLS_CALCULATED
        }

        return DlsResult(
            revisedTarget = revisedTarget,
            parScore = parScore,
            runsRequired = runsRequired,
            resourcePercentage = r2Total,
            resourceRemaining = r2Total,
            revisedOvers = input.revisedOvers,
            status = resultStatus,
            calculationAvailable = true,
            errorMessage = null
        )
    }
}
