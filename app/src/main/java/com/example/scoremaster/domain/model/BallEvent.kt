package com.example.scoremaster.domain.model

data class BallEvent(
    val id: Long = 0,
    val inningsId: Long,
    val overNumber: Int,
    val ballNumberInOver: Int,
    val isLegalBall: Boolean,
    val bowlerId: Long,
    val strikerId: Long,
    val nonStrikerId: Long,
    val runsOffBat: Int = 0,
    val extraType: ExtraType = ExtraType.NONE,
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val wicketType: WicketType? = null,
    val dismissedPlayerId: Long? = null,
    val fielderId: Long? = null,
    val newBatsmanId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalBallRuns: Int
        get() = runsOffBat + extraRuns

    val isExtra: Boolean
        get() = extraType != ExtraType.NONE

    val shortDisplayText: String
        get() = when {
            isWicket -> "W"
            extraType == ExtraType.WIDE -> if (extraRuns > 1) "Wd+${extraRuns - 1}" else "Wd"
            extraType == ExtraType.NO_BALL -> if (runsOffBat > 0) "Nb+$runsOffBat" else "Nb"
            extraType == ExtraType.BYE -> "B$extraRuns"
            extraType == ExtraType.LEG_BYE -> "Lb$extraRuns"
            else -> runsOffBat.toString()
        }
}
