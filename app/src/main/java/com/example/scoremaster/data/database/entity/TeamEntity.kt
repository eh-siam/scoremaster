package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tournamentId: Long? = null,
    val matchId: Long? = null,
    val name: String
)
