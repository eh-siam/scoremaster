package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tournamentId: Long? = null,
    val matchName: String?,
    val team1Id: Long,
    val team2Id: Long,
    val team1Name: String,
    val team2Name: String,
    val numberOfOvers: Int,
    val originalNumberOfOvers: Int = numberOfOvers,
    val matchFormatName: String = if (numberOfOvers > 20) "ODI" else "T20",
    val venue: String?,
    val date: String,
    val currentInningsIndex: Int = 1,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, COMPLETED
    val winnerTeamId: Long? = null,
    val winnerTeamName: String? = null,
    val winningMargin: String? = null,
    val playerOfTheMatchId: Long? = null,
    val playerOfTheMatchName: String? = null,
    val dlsRevisedTarget: Int? = null,
    val dlsRevisedOvers: Int? = null,
    val dlsParScore: Int? = null
)
