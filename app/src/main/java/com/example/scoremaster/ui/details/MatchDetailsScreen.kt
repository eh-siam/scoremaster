package com.example.scoremaster.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.MatchStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    viewModel: MatchDetailsViewModel,
    onBackClick: () -> Unit,
    onStartMatchClick: (matchId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var expandedStrikerMenu by remember { mutableStateOf(false) }
    var expandedNonStrikerMenu by remember { mutableStateOf(false) }
    var expandedBowlerMenu by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details & Squads", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        val currentMatch = uiState.match
        if (uiState.isLoading || currentMatch == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading match details...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Match Overview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentMatch.matchName ?: "Cricket Match",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "${currentMatch.team1Name} vs ${currentMatch.team2Name}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DetailRow("Match", "${currentMatch.team1Name} vs ${currentMatch.team2Name}")
                        DetailRow("Overs", "${currentMatch.effectiveOvers} Overs${if (currentMatch.dlsRevisedOvers != null) " (DLS Revised)" else ""}")
                        if (!currentMatch.venue.isNull_or_blank()) {
                            DetailRow("Venue", currentMatch.venue.orEmpty())
                        }
                        DetailRow("Date", currentMatch.date)

                        val statusText = when (currentMatch.status) {
                            MatchStatus.IN_PROGRESS -> "🔴 Live / In Progress"
                            MatchStatus.NOT_STARTED -> "📅 Upcoming / Not Started"
                            MatchStatus.COMPLETED -> "🏆 Completed"
                        }
                        DetailRow("Status", statusText)
                    }
                }

                val isNotStarted = currentMatch.status == MatchStatus.NOT_STARTED

                // Navigation Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    if (isNotStarted) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Lineup", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("${currentMatch.team1Name} (${uiState.team1Players.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("${currentMatch.team2Name} (${uiState.team2Players.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    } else {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("${currentMatch.team1Name} Squad (${uiState.team1Players.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("${currentMatch.team2Name} Squad (${uiState.team2Players.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isNotStarted) {
                        when (selectedTab) {
                            0 -> {
                                val isT1BattingFirst = uiState.isTeam1BattingFirst

                                val battingTeamName = if (isT1BattingFirst) currentMatch.team1Name else currentMatch.team2Name
                                val bowlingTeamName = if (isT1BattingFirst) currentMatch.team2Name else currentMatch.team1Name

                                val battingSquad = if (isT1BattingFirst) uiState.team1Players else uiState.team2Players
                                val bowlingSquad = if (isT1BattingFirst) uiState.team2Players else uiState.team1Players

                                val tossWinnerName = if (uiState.tossWinnerTeamIndex == 0) currentMatch.team1Name else currentMatch.team2Name

                                // Toss Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Toss Information",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text("Who won the toss?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.SelectTossWinner(0)) },
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (uiState.tossWinnerTeamIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    contentColor = if (uiState.tossWinnerTeamIndex == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text(currentMatch.team1Name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.SelectTossWinner(1)) },
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (uiState.tossWinnerTeamIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    contentColor = if (uiState.tossWinnerTeamIndex == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text(currentMatch.team2Name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        Text("Toss Decision:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.SelectTossDecision(TossDecision.BAT)) },
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (uiState.tossDecision == TossDecision.BAT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    contentColor = if (uiState.tossDecision == TossDecision.BAT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text("Opted to Bat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            Button(
                                                onClick = { viewModel.onEvent(MatchDetailsEvent.SelectTossDecision(TossDecision.BOWL)) },
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (uiState.tossDecision == TossDecision.BOWL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    contentColor = if (uiState.tossDecision == TossDecision.BOWL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Text("Opted to Bowl", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        Text(
                                            text = "$tossWinnerName won the toss and elected to ${if (uiState.tossDecision == TossDecision.BAT) "bat" else "field"} first.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Lineup Position Selectors
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "1st Innings Opening Lineup ($battingTeamName Batting)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // Striker Selector
                                        Text("Opening Striker ($battingTeamName):", fontWeight = FontWeight.SemiBold)
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            val currentStrikerName = battingSquad.getOrNull(uiState.selectedStrikerIndex) ?: "Select Striker"
                                            OutlinedButton(
                                                onClick = { expandedStrikerMenu = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(currentStrikerName, fontWeight = FontWeight.Bold)
                                            }
                                            DropdownMenu(
                                                expanded = expandedStrikerMenu,
                                                onDismissRequest = { expandedStrikerMenu = false }
                                            ) {
                                                battingSquad.forEachIndexed { idx, pName ->
                                                    DropdownMenuItem(
                                                        text = { Text("${idx + 1}. $pName") },
                                                        onClick = {
                                                            viewModel.onEvent(MatchDetailsEvent.SelectStriker(idx))
                                                            expandedStrikerMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Non-Striker Selector
                                        Text("Opening Non-Striker ($battingTeamName):", fontWeight = FontWeight.SemiBold)
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            val currentNonStrikerName = battingSquad.getOrNull(uiState.selectedNonStrikerIndex) ?: "Select Non-Striker"
                                            OutlinedButton(
                                                onClick = { expandedNonStrikerMenu = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(currentNonStrikerName, fontWeight = FontWeight.Bold)
                                            }
                                            DropdownMenu(
                                                expanded = expandedNonStrikerMenu,
                                                onDismissRequest = { expandedNonStrikerMenu = false }
                                            ) {
                                                battingSquad.forEachIndexed { idx, pName ->
                                                    DropdownMenuItem(
                                                        text = { Text("${idx + 1}. $pName") },
                                                        onClick = {
                                                            viewModel.onEvent(MatchDetailsEvent.SelectNonStriker(idx))
                                                            expandedNonStrikerMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Bowler Selector
                                        Text("Opening Bowler ($bowlingTeamName):", fontWeight = FontWeight.SemiBold)
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            val currentBowlerName = bowlingSquad.getOrNull(uiState.selectedBowlerIndex) ?: "Select Bowler"
                                            OutlinedButton(
                                                onClick = { expandedBowlerMenu = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(currentBowlerName, fontWeight = FontWeight.Bold)
                                            }
                                            DropdownMenu(
                                                expanded = expandedBowlerMenu,
                                                onDismissRequest = { expandedBowlerMenu = false }
                                            ) {
                                                bowlingSquad.forEachIndexed { idx, pName ->
                                                    DropdownMenuItem(
                                                        text = { Text("${idx + 1}. $pName") },
                                                        onClick = {
                                                            viewModel.onEvent(MatchDetailsEvent.SelectBowler(idx))
                                                            expandedBowlerMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                SquadOverviewHeaderCard(
                                    teamName = currentMatch.team1Name,
                                    squadList = uiState.team1Players
                                )

                                SquadEditorSection(
                                    teamName = currentMatch.team1Name,
                                    squadList = uiState.team1Players,
                                    onUpdatePlayer = { idx, name -> viewModel.onEvent(MatchDetailsEvent.UpdateTeam1Player(idx, name)) },
                                    onRemovePlayer = { idx -> viewModel.onEvent(MatchDetailsEvent.RemoveTeam1Player(idx)) },
                                    onAddPlayer = { viewModel.onEvent(MatchDetailsEvent.AddTeam1Player) },
                                    onSavePlayers = { viewModel.onEvent(MatchDetailsEvent.SavePlayers()) }
                                )
                            }

                            2 -> {
                                SquadOverviewHeaderCard(
                                    teamName = currentMatch.team2Name,
                                    squadList = uiState.team2Players
                                )

                                SquadEditorSection(
                                    teamName = currentMatch.team2Name,
                                    squadList = uiState.team2Players,
                                    onUpdatePlayer = { idx, name -> viewModel.onEvent(MatchDetailsEvent.UpdateTeam2Player(idx, name)) },
                                    onRemovePlayer = { idx -> viewModel.onEvent(MatchDetailsEvent.RemoveTeam2Player(idx)) },
                                    onAddPlayer = { viewModel.onEvent(MatchDetailsEvent.AddTeam2Player) },
                                    onSavePlayers = { viewModel.onEvent(MatchDetailsEvent.SavePlayers()) }
                                )
                            }
                        }
                    } else {
                        when (selectedTab) {
                            0 -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "${currentMatch.team1Name} Squad Players",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        uiState.team1Players.forEachIndexed { idx, pName ->
                                            OutlinedTextField(
                                                value = pName,
                                                onValueChange = { viewModel.onEvent(MatchDetailsEvent.UpdateTeam1Player(idx, it)) },
                                                label = { Text("Player ${idx + 1}") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                trailingIcon = {
                                                    IconButton(onClick = { viewModel.onEvent(MatchDetailsEvent.RemoveTeam1Player(idx)) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Remove Player")
                                                    }
                                                }
                                            )
                                        }

                                        TextButton(
                                            onClick = { viewModel.onEvent(MatchDetailsEvent.AddTeam1Player) },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Text("Add Player")
                                        }

                                        Button(
                                            onClick = { viewModel.onEvent(MatchDetailsEvent.SavePlayers()) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Save Squad Changes")
                                        }
                                    }
                                }
                            }

                            1 -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "${currentMatch.team2Name} Squad Players",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        uiState.team2Players.forEachIndexed { idx, pName ->
                                            OutlinedTextField(
                                                value = pName,
                                                onValueChange = { viewModel.onEvent(MatchDetailsEvent.UpdateTeam2Player(idx, it)) },
                                                label = { Text("Player ${idx + 1}") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                trailingIcon = {
                                                    IconButton(onClick = { viewModel.onEvent(MatchDetailsEvent.RemoveTeam2Player(idx)) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Remove Player")
                                                    }
                                                }
                                            )
                                        }

                                        TextButton(
                                            onClick = { viewModel.onEvent(MatchDetailsEvent.AddTeam2Player) },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Text("Add Player")
                                        }

                                        Button(
                                            onClick = { viewModel.onEvent(MatchDetailsEvent.SavePlayers()) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Save Squad Changes")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Button
                Box(modifier = Modifier.padding(16.dp)) {
                    if (isNotStarted) {
                        Button(
                            onClick = {
                                viewModel.onEvent(MatchDetailsEvent.StartMatch { matchId ->
                                    onStartMatchClick(matchId)
                                })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Match & Live Scoring", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.onEvent(MatchDetailsEvent.SavePlayers {
                                    onStartMatchClick(currentMatch.id)
                                })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Continue Match", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun SquadOverviewHeaderCard(
    teamName: String,
    squadList: List<String>,
    modifier: Modifier = Modifier
) {
    val squadSize = squadList.size
    val playingXICount = squadSize.coerceAtMost(11)
    val isReady = squadSize >= 11

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$teamName Squad ($squadSize)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isReady) "✓ Ready" else "⚠️ Need 11",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "$squadSize players added in team squad",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playing XI",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$playingXICount / 11 selected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SquadEditorSection(
    teamName: String,
    squadList: List<String>,
    onUpdatePlayer: (index: Int, name: String) -> Unit,
    onRemovePlayer: (index: Int) -> Unit,
    onAddPlayer: () -> Unit,
    onSavePlayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playingXI = squadList.take(11)
    val bench = if (squadList.size > 11) squadList.drop(11) else emptyList()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Playing XI (1 to 11)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playing XI (${playingXI.size} / 11 Players)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ Main Playing XI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            playingXI.forEachIndexed { idx, pName ->
                val labelText = when (idx) {
                    0 -> "#1 • Opening Striker"
                    1 -> "#2 • Opening Non-Striker"
                    10 -> "#11 • Bowler / Tailender"
                    else -> "#${idx + 1} • Playing XI"
                }

                OutlinedTextField(
                    value = pName,
                    onValueChange = { onUpdatePlayer(idx, it) },
                    label = { Text(labelText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { onRemovePlayer(idx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Player", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }

            // Section 2: Substitutes / Bench (12+)
            if (bench.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Substitutes / Bench (${bench.size} Players)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Bench / Reserves",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                bench.forEachIndexed { bIdx, pName ->
                    val actualIndex = 11 + bIdx
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { onUpdatePlayer(actualIndex, it) },
                        label = { Text("#${actualIndex + 1} • Substitute") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onRemovePlayer(actualIndex) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Substitute", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                }
            }

            TextButton(
                onClick = onAddPlayer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Squad Player")
            }

            Button(
                onClick = onSavePlayers,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Squad Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
