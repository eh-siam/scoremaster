package com.example.scoremaster.domain.model

data class Team(
    val id: Long = 0,
    val tournamentId: Long? = null,
    val matchId: Long? = null,
    val name: String
)
