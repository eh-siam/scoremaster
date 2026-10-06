package com.example.scoremaster.domain.model

data class Innings(
    val id: Long = 0,
    val matchId: Long,
    val battingTeamId: Long,
    val battingTeamName: String,
    val bowlingTeamId: Long,
    val bowlingTeamName: String,
    val inningsNumber: Int,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val legalBallsBowled: Int = 0,
    val targetRuns: Int? = null,
    val originalTargetRuns: Int? = targetRuns,
    val isCompleted: Boolean = false,
    val currentStrikerId: Long? = null,
    val currentNonStrikerId: Long? = null,
    val currentBowlerId: Long? = null
) {
    val oversFormatted: String
        get() {
            val overs = legalBallsBowled / 6
            val balls = legalBallsBowled % 6
            return "$overs.$balls"
        }

    val oversDouble: Double
        get() = (legalBallsBowled / 6) + (legalBallsBowled % 6) / 6.0

    val currentRunRate: Float
        get() {
            val overs = (legalBallsBowled / 6) + (legalBallsBowled % 6) / 6f
            return if (overs > 0f) totalRuns / overs else 0f
        }

    fun requiredRunRate(totalMatchOvers: Int): Float {
        val target = targetRuns ?: return 0f
        val runsNeeded = target - totalRuns
        val ballsRemaining = (totalMatchOvers * 6) - legalBallsBowled
        val oversRemaining = ballsRemaining / 6f
        return if (oversRemaining > 0f && runsNeeded > 0) runsNeeded / oversRemaining else 0f
    }

    fun runsNeeded(target: Int): Int = (target - totalRuns).coerceAtLeast(0)

    fun ballsRemaining(totalMatchOvers: Int): Int = ((totalMatchOvers * 6) - legalBallsBowled).coerceAtLeast(0)
}
