package com.example.scoremaster.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun ScoreMasterBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show BottomBar ONLY on primary top-level bottom nav destinations
    val shouldShowBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    if (shouldShowBottomBar) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val isSelected = when (screen) {
                    is Screen.MatchSetup -> currentRoute?.startsWith("match_setup") == true
                    else -> currentRoute == screen.route
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        val targetRoute = if (screen is Screen.MatchSetup) Screen.MatchSetup.createRoute() else screen.route
                        if (!isSelected) {
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = (screen !is Screen.Home)
                            }
                        }
                    },
                    icon = {
                        screen.icon?.let { iconVector ->
                            Icon(
                                imageVector = iconVector,
                                contentDescription = screen.title
                            )
                        }
                    },
                    label = {
                        screen.title?.let { titleText ->
                            Text(
                                text = titleText,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
