package com.example.scoremaster.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dls_interruptions")
data class DlsInterruptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val interruptionNumber: Int,
    val scoreAtInterruption: Int,
    val wicketsAtInterruption: Int,
    val oversAtInterruption: Double,
    val scheduledOversBeforeInterruption: Int,
    val revisedOversAfterInterruption: Int,
    val resourceBeforeInterruption: Double = 0.0,
    val resourceLost: Double = 0.0,
    val resourceRemaining: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
