package com.example.scoremaster.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.data.repository.TournamentRepository
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.ui.analytics.AnalyticsScreen
import com.example.scoremaster.ui.analytics.AnalyticsViewModel
import com.example.scoremaster.ui.details.MatchDetailsScreen
import com.example.scoremaster.ui.details.MatchDetailsViewModel
import com.example.scoremaster.ui.home.HomeScreen
import com.example.scoremaster.ui.home.HomeViewModel
import com.example.scoremaster.ui.result.MatchResultScreen
import com.example.scoremaster.ui.result.MatchResultViewModel
import com.example.scoremaster.ui.scoring.LiveScoringScreen
import com.example.scoremaster.ui.scoring.LiveScoringViewModel
import com.example.scoremaster.ui.setup.MatchSetupScreen
import com.example.scoremaster.ui.setup.MatchSetupViewModel
import com.example.scoremaster.ui.splash.SplashScreen
import com.example.scoremaster.ui.tournament.TournamentDashboardScreen
import com.example.scoremaster.ui.tournament.TournamentViewModel
import com.example.scoremaster.ui.tournament.TournamentsListScreen
import com.example.scoremaster.ui.tournament.TournamentsListViewModel

@Composable
fun ScoreMasterNavHost(
    navController: NavHostController,
    matchRepository: MatchRepository,
    tournamentRepository: TournamentRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            val homeViewModel = remember { HomeViewModel(matchRepository) }
            HomeScreen(
                viewModel = homeViewModel,
                navController = navController,
                onNewMatchClick = {
                    navController.navigate(Screen.MatchSetup.createRoute("ONE_VS_ONE"))
                },
                onTournamentClick = {
                    navController.navigate(Screen.MatchSetup.createRoute("TOURNAMENT"))
                },
                onMatchClick = { match ->
                    if (match.status == MatchStatus.COMPLETED) {
                        navController.navigate(Screen.MatchResult.createRoute(match.id))
                    } else if (match.status == MatchStatus.IN_PROGRESS) {
                        navController.navigate(Screen.LiveScoring.createRoute(match.id))
                    } else {
                        navController.navigate(Screen.MatchDetails.createRoute(match.id))
                    }
                }
            )
        }

        // Match Setup Screen
        composable(
            route = Screen.MatchSetup.route,
            arguments = listOf(navArgument("initialType") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val initialType = backStackEntry.arguments?.getString("initialType")
            val setupViewModel = remember(initialType) { MatchSetupViewModel(matchRepository, tournamentRepository) }
            MatchSetupScreen(
                viewModel = setupViewModel,
                initialType = initialType,
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onMatchCreated = { matchId ->
                    navController.navigate(Screen.MatchDetails.createRoute(matchId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onTournamentCreated = { tournamentId ->
                    navController.navigate(Screen.TournamentDashboard.createRoute(tournamentId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Tournaments Screen
        composable(Screen.Tournaments.route) {
            val tournamentsViewModel = remember { TournamentsListViewModel(tournamentRepository) }
            TournamentsListScreen(
                viewModel = tournamentsViewModel,
                navController = navController,
                onNewTournamentClick = {
                    navController.navigate(Screen.MatchSetup.createRoute("TOURNAMENT"))
                },
                onTournamentClick = { tournamentId ->
                    navController.navigate(Screen.TournamentDashboard.createRoute(tournamentId))
                }
            )
        }

        // Analytics Screen
        composable(Screen.Analytics.route) {
            val analyticsViewModel = remember { AnalyticsViewModel(matchRepository) }
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                navController = navController
            )
        }

        // Match Details Screen
        composable(
            route = Screen.MatchDetails.route,
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: return@composable
            val detailsViewModel = remember(matchId) { MatchDetailsViewModel(matchId, matchRepository) }

            MatchDetailsScreen(
                viewModel = detailsViewModel,
                onBackClick = { navController.popBackStack() },
                onStartMatchClick = { id ->
                    navController.navigate(Screen.LiveScoring.createRoute(id)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Live Scoring Screen
        composable(
            route = Screen.LiveScoring.route,
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: return@composable
            val scoringViewModel = remember(matchId) { LiveScoringViewModel(matchId, matchRepository) }

            LiveScoringScreen(
                viewModel = scoringViewModel,
                onBackClick = { navController.popBackStack() },
                onMatchResultClick = { id ->
                    navController.navigate(Screen.MatchResult.createRoute(id)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Match Result Screen
        composable(
            route = Screen.MatchResult.route,
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId") ?: return@composable
            val resultViewModel = remember(matchId) { MatchResultViewModel(matchId, matchRepository) }

            MatchResultScreen(
                viewModel = resultViewModel,
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Tournament Dashboard Screen
        composable(
            route = Screen.TournamentDashboard.route,
            arguments = listOf(navArgument("tournamentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId") ?: return@composable
            val tournamentViewModel = remember(tournamentId) { TournamentViewModel(tournamentId, tournamentRepository, matchRepository) }

            TournamentDashboardScreen(
                viewModel = tournamentViewModel,
                onBackClick = { navController.popBackStack() },
                onMatchClick = { match ->
                    if (match.status == MatchStatus.COMPLETED) {
                        navController.navigate(Screen.MatchResult.createRoute(match.id))
                    } else if (match.status == MatchStatus.IN_PROGRESS) {
                        navController.navigate(Screen.LiveScoring.createRoute(match.id))
                    } else {
                        navController.navigate(Screen.MatchDetails.createRoute(match.id))
                    }
                }
            )
        }
    }
}
