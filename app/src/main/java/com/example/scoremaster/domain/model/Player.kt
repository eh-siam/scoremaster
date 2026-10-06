package com.example.scoremaster.domain.model

data class Player(
    val id: Long = 0,
    val matchId: Long,
    val teamId: Long,
    val name: String
)
