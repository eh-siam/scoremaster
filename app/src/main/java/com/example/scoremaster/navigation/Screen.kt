package com.example.scoremaster.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object Splash : Screen("splash")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object MatchSetup : Screen("match_setup?initialType={initialType}", "Create", Icons.Default.SportsCricket) {
        fun createRoute(initialType: String? = null): String =
            if (!initialType.isNull_or_blank()) "match_setup?initialType=$initialType" else "match_setup"
    }
    object Tournaments : Screen("tournaments", "Tournaments", Icons.Default.EmojiEvents)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Leaderboard)

    object MatchDetails : Screen("match_details/{matchId}") {
        fun createRoute(matchId: Long) = "match_details/$matchId"
    }
    object LiveScoring : Screen("live_scoring/{matchId}") {
        fun createRoute(matchId: Long) = "live_scoring/$matchId"
    }
    object MatchResult : Screen("match_result/{matchId}") {
        fun createRoute(matchId: Long) = "match_result/$matchId"
    }
    object TournamentDashboard : Screen("tournament_dashboard/{tournamentId}") {
        fun createRoute(tournamentId: Long) = "tournament_dashboard/$tournamentId"
    }

    companion object {
        val bottomNavItems = listOf(Home, MatchSetup, Tournaments, Analytics)
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
