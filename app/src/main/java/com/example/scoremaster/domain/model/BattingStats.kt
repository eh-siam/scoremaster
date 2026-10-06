package com.example.scoremaster.domain.model

data class BattingStats(
    val player: Player,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalInfo: String = "not out"
) {
    val strikeRate: Float
        get() = if (balls > 0) (runs.toFloat() / balls) * 100f else 0f
}
