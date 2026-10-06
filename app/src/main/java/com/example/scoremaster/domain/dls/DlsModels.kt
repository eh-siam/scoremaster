package com.example.scoremaster.domain.dls

enum class DlsStatus {
    NORMAL,
    RAIN_INTERRUPTION,
    DLS_CALCULATED,
    DLS_APPLIED,
    MATCH_ABANDONED,
    TEAM_1_WINS,
    TEAM_2_WINS,
    TIED,
    NO_RESULT,
    CALCULATION_UNAVAILABLE
}

data class DlsInterruption(
    val interruptionNumber: Int,
    val scoreAtInterruption: Int,
    val wicketsAtInterruption: Int,
    val oversAtInterruption: Double,
    val scheduledOversBeforeInterruption: Int,
    val revisedOversAfterInterruption: Int,
    val resourceBeforeInterruption: Double = 0.0,
    val resourceLost: Double = 0.0,
    val resourceRemaining: Double = 0.0
)

data class DlsInput(
    val matchFormat: MatchFormat,
    val scheduledOvers: Int,
    val firstInningsScore: Int,
    val firstInningsWickets: Int = 0,
    val firstInningsOversBowled: Double = scheduledOvers.toDouble(),
    val secondInningsCurrentScore: Int = 0,
    val secondInningsWicketsLost: Int = 0,
    val secondInningsOversCompleted: Double = 0.0,
    val revisedOvers: Int = scheduledOvers,
    val interruptions: List<DlsInterruption> = emptyList(),
    val isMatchResumed: Boolean = true,
    val isMatchAbandoned: Boolean = false
)

data class DlsResult(
    val revisedTarget: Int? = null,
    val parScore: Int? = null,
    val runsRequired: Int? = null,
    val resourcePercentage: Double? = null,
    val resourceRemaining: Double? = null,
    val revisedOvers: Int,
    val status: DlsStatus,
    val calculationAvailable: Boolean,
    val errorMessage: String? = null
)
