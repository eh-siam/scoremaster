package com.example.scoremaster.domain.model

data class Tournament(
    val id: Long = 0,
    val name: String,
    val numberOfTeams: Int,
    val numberOfOvers: Int,
    val venue: String?,
    val matchDate: String,
    val teams: List<Team> = emptyList()
)
