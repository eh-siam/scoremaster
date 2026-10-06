package com.example.scoremaster.domain.dls

enum class MatchFormat(
    val displayName: String,
    val maxOvers: Int,
    val minOversForDls: Int,
    val gParAverageScore: Double
) {
    ODI("ODI (50 Overs)", 50, 20, 245.0),
    T20("T20 (20 Overs)", 20, 5, 150.0);

    companion object {
        fun fromOvers(overs: Int): MatchFormat {
            return if (overs > 20) ODI else T20
        }
    }
}
