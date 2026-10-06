package com.example.scoremaster.domain.model

data class PointsTableEntry(
    val teamId: Long,
    val teamName: String,
    val played: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val noResult: Int = 0,
    val points: Int = 0,
    val netRunRate: Float = 0.0f
)
