package com.example.scoremaster.domain.model

data class TopBatterEntry(
    val playerId: Long,
    val playerName: String,
    val teamName: String,
    val totalRuns: Int = 0,
    val inningsCount: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0
) {
    val strikeRate: Float
        get() = if (ballsFaced > 0) (totalRuns.toFloat() / ballsFaced) * 100f else 0f
}

data class TopBowlerEntry(
    val playerId: Long,
    val playerName: String,
    val teamName: String,
    val totalWickets: Int = 0,
    val inningsCount: Int = 0,
    val legalBalls: Int = 0,
    val runsConceded: Int = 0
) {
    val oversFormatted: String
        get() {
            val overs = legalBalls / 6
            val balls = legalBalls % 6
            return "$overs.$balls"
        }

    val economy: Float
        get() {
            val overs = (legalBalls / 6) + (legalBalls % 6) / 6f
            return if (overs > 0f) runsConceded / overs else 0f
        }
}

data class TournamentLeaderboard(
    val topBatters: List<TopBatterEntry> = emptyList(),
    val topBowlers: List<TopBowlerEntry> = emptyList()
)
