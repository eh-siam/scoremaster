package com.example.scoremaster.ui.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.ui.components.BattingScorecardTable
import com.example.scoremaster.ui.components.BowlingScorecardTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultScreen(
    viewModel: MatchResultViewModel,
    onHomeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val match = uiState.match
    val calc1 = uiState.innings1Calc
    val calc2 = uiState.innings2Calc

    val inn1 = uiState.inningsList.find { it.inningsNumber == 1 }
    val inn2 = uiState.inningsList.find { it.inningsNumber == 2 }

    var expandedPomMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Result & Summary", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (match == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading match summary...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Winner Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MATCH WINNER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        val winnerText = if (match.dlsRevisedTarget != null || match.dlsRevisedOvers != null) {
                            "${match.winningMargin ?: "Match Completed"} (DLS)"
                        } else {
                            match.winningMargin ?: "Match Completed"
                        }

                        Text(
                            text = winnerText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${match.team1Name} vs ${match.team2Name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Match Score Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "MATCH SCORE SUMMARY",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (inn1 != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${inn1.battingTeamName} (1st Innings)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val inn1Runs = calc1?.totalRuns ?: inn1.totalRuns
                                val inn1Wickets = calc1?.totalWickets ?: inn1.totalWickets
                                val inn1Overs = calc1?.legalBallsBowled?.let { "${it / 6}.${it % 6}" } ?: inn1.oversFormatted
                                Text(
                                    text = "$inn1Runs / $inn1Wickets ($inn1Overs ov)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (inn2 != null) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${inn2.battingTeamName} (2nd Innings)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val inn2Runs = calc2?.totalRuns ?: inn2.totalRuns
                                val inn2Wickets = calc2?.totalWickets ?: inn2.totalWickets
                                val inn2Overs = calc2?.legalBallsBowled?.let { "${it / 6}.${it % 6}" } ?: inn2.oversFormatted
                                Text(
                                    text = "$inn2Runs / $inn2Wickets ($inn2Overs ov)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Player of the Match Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Player of the Match",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            val selectedName = uiState.selectedPlayerOfTheMatch?.name
                                ?: match.playerOfTheMatchName
                                ?: "Select Player of the Match"

                            OutlinedButton(
                                onClick = { expandedPomMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedName, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = expandedPomMenu,
                                onDismissRequest = { expandedPomMenu = false }
                            ) {
                                uiState.allPlayers.forEach { player ->
                                    DropdownMenuItem(
                                        text = { Text(player.name) },
                                        onClick = {
                                            viewModel.selectPlayerOfTheMatch(player)
                                            expandedPomMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 1st Innings Scorecard Section
                if (calc1 != null) {
                    Text(
                        text = "1ST INNINGS SCORECARD - ${inn1?.battingTeamName ?: "Team 1"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    BattingScorecardTable(battingStatsList = calc1.battingScorecard)
                    BowlingScorecardTable(bowlingStatsList = calc1.bowlingScorecard)
                }

                // 2nd Innings Scorecard Section
                if (calc2 != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "2ND INNINGS SCORECARD - ${inn2?.battingTeamName ?: "Team 2"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    BattingScorecardTable(battingStatsList = calc2.battingScorecard)
                    BowlingScorecardTable(bowlingStatsList = calc2.bowlingScorecard)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Back to Home Button
                Button(
                    onClick = onHomeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home", modifier = Modifier.padding(end = 8.dp))
                    Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
