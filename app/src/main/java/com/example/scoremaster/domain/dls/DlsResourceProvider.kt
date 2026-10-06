package com.example.scoremaster.domain.dls

import kotlin.math.exp

interface DlsResourceProvider {
    fun getResource(
        oversRemaining: Int,
        ballsRemaining: Int,
        wicketsRemaining: Int
    ): Double?
}

class OfficialDlsResourceProvider : DlsResourceProvider {

    companion object {
        // Z0 values for wickets remaining = 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
        private val Z0 = doubleArrayOf(200.0, 180.0, 158.0, 134.0, 108.0, 82.0, 58.0, 38.0, 20.0, 8.0)

        // b values for wickets lost = 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        private val B = doubleArrayOf(0.0353, 0.0388, 0.0431, 0.0484, 0.0555, 0.0652, 0.0792, 0.1010, 0.1410, 0.2220)

        // Reference constant Z(50, 0) = 200.0 * (1 - e^(-0.0353 * 50)) = 165.7642
        private const val Z_REF_50_0 = 165.7642
    }

    override fun getResource(
        oversRemaining: Int,
        ballsRemaining: Int,
        wicketsRemaining: Int
    ): Double? {
        if (wicketsRemaining <= 0) return 0.0
        val wRem = wicketsRemaining.coerceIn(1, 10)
        val index = 10 - wRem // 0 for 10 wickets remaining (0 lost), 9 for 1 wicket remaining (9 lost)

        val u = (oversRemaining.coerceAtLeast(0) + (ballsRemaining.coerceIn(0, 5) / 6.0))
        if (u <= 0.0) return 0.0

        val z0Val = Z0[index]
        val bVal = B[index]

        val zVal = z0Val * (1.0 - exp(-bVal * u))
        val resourcePercentage = (zVal / Z_REF_50_0) * 100.0

        return resourcePercentage.coerceIn(0.0, 100.0)
    }
}
