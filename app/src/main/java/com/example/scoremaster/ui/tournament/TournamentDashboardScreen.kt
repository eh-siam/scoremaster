package com.example.scoremaster.ui.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.ui.components.LeaderboardComponent
import com.example.scoremaster.ui.components.MatchCard
import com.example.scoremaster.ui.components.PointsTableComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    viewModel: TournamentViewModel,
    onBackClick: () -> Unit,
    onMatchClick: (Match) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tournament = uiState.tournament
    val matches = uiState.matches
    val pointsTable = uiState.pointsTable
    val leaderboard = uiState.leaderboard

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateMatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tournament?.name ?: "Tournament Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 && tournament != null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateMatchDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Match") },
                    text = { Text("Create Match", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (tournament == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading tournament dashboard...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Header Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = tournament.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InfoChip(
                                        icon = Icons.Default.Groups,
                                        text = "${tournament.numberOfTeams} Teams"
                                    )
                                    InfoChip(
                                        icon = Icons.Default.SportsCricket,
                                        text = "${tournament.numberOfOvers} Overs"
                                    )
                                    InfoChip(
                                        icon = Icons.Default.CalendarToday,
                                        text = tournament.matchDate
                                    )
                                }
                            }
                        }
                    }

                    // Tabs: Matches | Points Table | Leaderboard
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Matches (${matches.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Points Table", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Leaderboard", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                    }

                    when (selectedTab) {
                        0 -> {
                            // Matches List
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (matches.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "No matches",
                                                modifier = Modifier.size(56.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "No tournament matches created yet.\nTap below to generate all round-robin fixtures in 1 click!",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge
                                            )

                                            Button(
                                                onClick = {
                                                    viewModel.generateAutoFixtures {
                                                        // Fixtures created!
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Text("⚡ Auto-Generate All Fixtures", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        items(matches) { match ->
                                            MatchCard(
                                                match = match,
                                                onClick = { onMatchClick(match) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Points Table
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                PointsTableComponent(pointsTable = pointsTable)
                            }
                        }

                        2 -> {
                            // Tournament Leaderboard (Orange Cap & Purple Cap)
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(12.dp)
                            ) {
                                LeaderboardComponent(leaderboard = leaderboard)
                            }
                        }
                    }
                }
            }

            // Dialog for creating single match between tournament teams
            if (showCreateMatchDialog && tournament != null && tournament.teams.size >= 2) {
                var selectedTeam1 by remember { mutableStateOf(tournament.teams[0]) }
                var selectedTeam2 by remember { mutableStateOf(tournament.teams[1]) }

                var expandedTeam1Menu by remember { mutableStateOf(false) }
                var expandedTeam2Menu by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showCreateMatchDialog = false },
                    title = { Text("New Tournament Match", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Select Team 1:")
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { expandedTeam1Menu = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(selectedTeam1.name)
                                }
                                DropdownMenu(expanded = expandedTeam1Menu, onDismissRequest = { expandedTeam1Menu = false }) {
                                    tournament.teams.forEach { team ->
                                        DropdownMenuItem(
                                            text = { Text(team.name) },
                                            onClick = {
                                                selectedTeam1 = team
                                                expandedTeam1Menu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Text("Select Team 2:")
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { expandedTeam2Menu = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(selectedTeam2.name)
                                }
                                DropdownMenu(expanded = expandedTeam2Menu, onDismissRequest = { expandedTeam2Menu = false }) {
                                    tournament.teams.filter { it.id != selectedTeam1.id }.forEach { team ->
                                        DropdownMenuItem(
                                            text = { Text(team.name) },
                                            onClick = {
                                                selectedTeam2 = team
                                                expandedTeam2Menu = false
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider()

                            OutlinedButton(
                                onClick = {
                                    viewModel.generateAutoFixtures {
                                        showCreateMatchDialog = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text("⚡ Auto-Generate All Fixtures", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.createMatchForTournament(selectedTeam1.name, selectedTeam2.name) { matchId ->
                                    showCreateMatchDialog = false
                                }
                            },
                            enabled = selectedTeam1.id != selectedTeam2.id
                        ) {
                            Text("Create Single Match")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateMatchDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
