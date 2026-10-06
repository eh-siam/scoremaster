package com.example.scoremaster.domain.model

data class BowlingStats(
    val player: Player,
    val legalBalls: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0
) {
    val oversFormatted: String
        get() {
            val overs = legalBalls / 6
            val balls = legalBalls % 6
            return "$overs.$balls"
        }

    val oversDouble: Double
        get() = (legalBalls / 6) + (legalBalls % 6) / 6.0

    val economy: Float
        get() {
            val totalOvers = (legalBalls / 6) + (legalBalls % 6) / 6f
            return if (totalOvers > 0f) runsConceded / totalOvers else 0f
        }
}
