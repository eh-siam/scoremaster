package com.example.scoremaster.domain.model

import com.example.scoremaster.domain.dls.MatchFormat

data class Match(
    val id: Long = 0,
    val tournamentId: Long? = null,
    val matchName: String?,
    val team1Id: Long,
    val team2Id: Long,
    val team1Name: String,
    val team2Name: String,
    val numberOfOvers: Int,
    val originalNumberOfOvers: Int = numberOfOvers,
    val matchFormat: MatchFormat = MatchFormat.fromOvers(numberOfOvers),
    val venue: String?,
    val date: String,
    val currentInningsIndex: Int = 1,
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val winnerTeamId: Long? = null,
    val winnerTeamName: String? = null,
    val winningMargin: String? = null,
    val playerOfTheMatchId: Long? = null,
    val playerOfTheMatchName: String? = null,
    val dlsRevisedTarget: Int? = null,
    val dlsRevisedOvers: Int? = null,
    val dlsParScore: Int? = null
) {
    val effectiveOvers: Int
        get() = dlsRevisedOvers ?: numberOfOvers
}
