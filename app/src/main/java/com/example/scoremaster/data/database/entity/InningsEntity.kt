package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "innings")
data class InningsEntity(
    @PrimaryKey(autoGenerate = true)
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
)
