package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val numberOfTeams: Int,
    val numberOfOvers: Int,
    val venue: String?,
    val matchDate: String
)
