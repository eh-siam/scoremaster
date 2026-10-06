package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ball_events")
data class BallEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inningsId: Long,
    val overNumber: Int, // 0-based over index e.g., 0 for over 1
    val ballNumberInOver: Int, // 1 to 6 for legal balls
    val isLegalBall: Boolean,
    val bowlerId: Long,
    val strikerId: Long,
    val nonStrikerId: Long,
    val runsOffBat: Int = 0,
    val extraType: String = "NONE", // NONE, WIDE, NO_BALL, BYE, LEG_BYE
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val wicketType: String? = null, // BOWLED, CAUGHT, LBW, RUN_OUT, STUMPED, HIT_WICKET, RETIRED
    val dismissedPlayerId: Long? = null,
    val fielderId: Long? = null,
    val newBatsmanId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
