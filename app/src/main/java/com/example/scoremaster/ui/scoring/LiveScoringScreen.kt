package com.example.scoremaster.ui.scoring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.ui.components.BallHistoryRow
import com.example.scoremaster.ui.components.BattingScorecardTable
import com.example.scoremaster.ui.components.BowlingScorecardTable
import com.example.scoremaster.ui.components.CelebrationType
import com.example.scoremaster.ui.components.CricketCelebrationOverlay
import com.example.scoremaster.ui.components.DetailedOverTimelineComponent
import com.example.scoremaster.ui.components.ExtrasDialog
import com.example.scoremaster.ui.components.PlayerSelectionDialog
import com.example.scoremaster.ui.components.RunButtonsGrid
import com.example.scoremaster.ui.components.WicketDialog
import com.example.scoremaster.ui.home.PulsingLiveDot
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoringScreen(
    viewModel: LiveScoringViewModel,
    onBackClick: () -> Unit,
    onMatchResultClick: (matchId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val match = uiState.match
    val innings = uiState.innings
    val calcResult = uiState.calculationResult

    var showWicketDialog by remember { mutableStateOf(false) }
    var showExtrasDialog by remember { mutableStateOf<ExtraType?>(null) }
    var showBowlerDialog by remember { mutableStateOf(false) }
    var showStartSecondInningsDialog by remember { mutableStateOf(false) }
    var showReviseOversDialog by remember { mutableStateOf(false) }

    var activeCelebration by remember { mutableStateOf<CelebrationType?>(null) }
    var celebrationPlayerName by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Scoring, 1: Batting, 2: Bowling, 3: Ball Timeline
    val scrollState = rememberScrollState()

    val legalBalls = calcResult?.legalBallsBowled ?: 0
    var lastCheckedBalls by remember { mutableIntStateOf(legalBalls) }

    androidx.compose.runtime.LaunchedEffect(legalBalls) {
        if (legalBalls > 0 && legalBalls % 6 == 0 && legalBalls > lastCheckedBalls && innings?.isCompleted == false && match?.status != MatchStatus.COMPLETED) {
            showBowlerDialog = true
        }
        lastCheckedBalls = legalBalls
    }

    androidx.compose.runtime.LaunchedEffect(innings?.isCompleted) {
        if (innings?.isCompleted == true && innings.inningsNumber == 1 && match?.status != MatchStatus.COMPLETED) {
            showStartSecondInningsDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = match?.let { "${it.team1Name} vs ${it.team2Name}" } ?: "Live Match",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Text(
                            text = if (uiState.isSoundEnabled) "🔊" else "🔇",
                            fontSize = 18.sp
                        )
                    }
                    if (match != null && innings != null && !innings.isCompleted && match.status != MatchStatus.COMPLETED) {
                        TextButton(onClick = { showReviseOversDialog = true }) {
                            Text("🌧️ Revise", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (match == null || innings == null || calcResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading scoring screen...")
            }
        } else {
            val totalMatchOvers = match.effectiveOvers

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 3D Ultra-Premium Score Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF19C37D)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF071A2B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF071A2B), Color(0xFF0F2B42))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        PulsingLiveDot(size = 6.dp, color = Color(0xFFEF4444))
                                        Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                                    }
                                }

                                Text(
                                    text = "${innings.battingTeamName} (${if (innings.inningsNumber == 1) "1st Innings" else "2nd Innings"})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                color = Color(0xFF19C37D),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${innings.legalBallsBowled / 6}.${innings.legalBallsBowled % 6} / $totalMatchOvers Ov",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Big Score Display
                        Text(
                            text = "${calcResult.totalRuns} / ${calcResult.totalWickets}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF19C37D),
                            letterSpacing = 1.sp
                        )

                        // Run Rates
                        val crr = innings.currentRunRate
                        val crrFormatted = String.format(LocalLocale.current.platformLocale, "%.2f", crr)
                        Text(
                            text = "Current Run Rate (CRR): $crrFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        if (innings.inningsNumber == 2 && innings.targetRuns != null) {
                            val target = innings.targetRuns
                            val needed = innings.runsNeeded(target)
                            val remainingBalls = innings.ballsRemaining(totalMatchOvers)
                            val rrr = innings.requiredRunRate(totalMatchOvers)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF132A3E),
                                border = BorderStroke(1.dp, Color(0xFF19C37D).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isDlsActive = match.dlsRevisedTarget != null || match.dlsRevisedOvers != null
                                    Text(
                                        text = if (isDlsActive) "Target: $target (DLS)" else "Target: $target",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF19C37D)
                                    )
                                    val rrrFormatted = String.format(LocalLocale.current.platformLocale, "%.2f", rrr)
                                    Text(
                                        text = "Need $needed from $remainingBalls b (RRR: $rrrFormatted)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab Row for Navigation
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Scoring", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Batting", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Bowling", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Timeline", fontSize = 12.sp) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Current Batter & Bowler Live Cards
                            val striker = uiState.battingPlayers.find { it.id == calcResult.currentStrikerId }
                                ?: uiState.battingPlayers.getOrNull(0)
                            val nonStriker = uiState.battingPlayers.find { it.id == calcResult.currentNonStrikerId }
                                ?: uiState.battingPlayers.getOrNull(1)
                            val bowler = uiState.bowlingPlayers.find { it.id == calcResult.currentBowlerId }
                                ?: uiState.bowlingPlayers.getOrNull(0)

                            val strikerStats = calcResult.battingScorecard.find { it.player.id == striker?.id }
                            val nonStrikerStats = calcResult.battingScorecard.find { it.player.id == nonStriker?.id }
                            val bowlerStats = calcResult.bowlingScorecard.find { it.player.id == bowler?.id }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "CURRENT BATSMEN & BOWLER",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Striker Block
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = striker?.name ?: "Striker",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                androidx.compose.material3.Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "★ Striker",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            val strikerSr = String.format(LocalLocale.current.platformLocale, "%.1f", strikerStats?.strikeRate ?: 0f)
                                            Text(
                                                text = "4s: ${strikerStats?.fours ?: 0}  •  6s: ${strikerStats?.sixes ?: 0}  •  SR: $strikerSr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = "${strikerStats?.runs ?: 0} (${strikerStats?.balls ?: 0}b)",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Non-Striker Block
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = nonStriker?.name ?: "Non-Striker",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Non-Striker",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            val nonStrikerSr = String.format(LocalLocale.current.platformLocale, "%.1f", nonStrikerStats?.strikeRate ?: 0f)
                                            Text(
                                                text = "SR: $nonStrikerSr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = "${nonStrikerStats?.runs ?: 0} (${nonStrikerStats?.balls ?: 0}b)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                                    // Bowler Block
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = bowler?.name ?: "Bowler",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Bowler",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            val ecoStr = String.format(LocalLocale.current.platformLocale, "%.2f", bowlerStats?.economy ?: 0f)
                                            Text(
                                                text = "${bowlerStats?.oversFormatted ?: "0.0"} Ov  •  ${bowlerStats?.maidens ?: 0} M  •  Eco: $ecoStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${bowlerStats?.runsConceded ?: 0} / ${bowlerStats?.wickets ?: 0} W",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            OutlinedButton(
                                                onClick = { showBowlerDialog = true },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Current Over Ball Ribbon
                            val currentOverIndex = innings.legalBallsBowled / 6
                            val currentOverBalls = calcResult.oversMap[currentOverIndex] ?: emptyList()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "THIS OVER:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (currentOverBalls.isEmpty()) {
                                        Text(
                                            text = "No balls bowled yet in this over",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(currentOverBalls) { ball ->
                                                com.example.scoremaster.ui.components.BallBadge(ball = ball)
                                            }
                                        }
                                    }
                                }
                            }

                            // If Innings / Match Is Completed
                            if (innings.isCompleted || match.status == MatchStatus.COMPLETED) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (match.status == MatchStatus.COMPLETED) "Match Finished!" else "Innings ${innings.inningsNumber} Completed!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )

                                        if (match.status == MatchStatus.COMPLETED) {
                                            Text(
                                                text = match.winningMargin ?: "Match Completed",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )

                                            Button(
                                                onClick = { onMatchResultClick(match.id) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("View Match Result & Summary")
                                            }
                                        } else if (innings.inningsNumber == 1) {
                                            Text(
                                                text = "${innings.battingTeamName} scored ${calcResult.totalRuns}/${calcResult.totalWickets} in ${calcResult.legalBallsBowled / 6}.${calcResult.legalBallsBowled % 6} overs.\nTarget: ${calcResult.totalRuns + 1}",
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            Button(
                                                onClick = { showStartSecondInningsDialog = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Start 2nd Innings")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Live Scoring Run Buttons Grid
                                RunButtonsGrid(
                                    onRunClick = { runs ->
                                        if (runs == 4) {
                                            val striker = uiState.battingPlayers.find { it.id == calcResult.currentStrikerId }
                                            celebrationPlayerName = striker?.name
                                            activeCelebration = CelebrationType.FOUR
                                        } else if (runs == 6) {
                                            val striker = uiState.battingPlayers.find { it.id == calcResult.currentStrikerId }
                                            celebrationPlayerName = striker?.name
                                            activeCelebration = CelebrationType.SIX
                                        }
                                        viewModel.onRunScored(runs)
                                    },
                                    onWideClick = { showExtrasDialog = ExtraType.WIDE },
                                    onNoBallClick = { showExtrasDialog = ExtraType.NO_BALL },
                                    onByeClick = { showExtrasDialog = ExtraType.BYE },
                                    onLegByeClick = { showExtrasDialog = ExtraType.LEG_BYE },
                                    onWicketClick = { showWicketDialog = true },
                                    onUndoClick = { viewModel.onUndoLastBall() },
                                    onSwapStrikeClick = { viewModel.swapStrike() }
                                )
                            }

                            // Ball History Preview
                            BallHistoryRow(oversMap = calcResult.oversMap)
                        }

                        1 -> {
                            // Batting Scorecard
                            BattingScorecardTable(
                                battingStatsList = calcResult.battingScorecard,
                                strikerId = calcResult.currentStrikerId,
                                nonStrikerId = calcResult.currentNonStrikerId
                            )
                        }

                        2 -> {
                            // Bowling Scorecard
                            BowlingScorecardTable(
                                bowlingStatsList = calcResult.bowlingScorecard,
                                currentBowlerId = calcResult.currentBowlerId
                            )
                        }

                        3 -> {
                            // Detailed Over-by-Over Timeline
                            val allP = uiState.battingPlayers + uiState.bowlingPlayers
                            DetailedOverTimelineComponent(
                                oversMap = calcResult.oversMap,
                                allPlayers = allP
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showWicketDialog && calcResult != null) {
            val striker = uiState.battingPlayers.find { it.id == calcResult.currentStrikerId }
            val nonStriker = uiState.battingPlayers.find { it.id == calcResult.currentNonStrikerId }

            val outPlayerIds = calcResult.battingScorecard.filter { it.isOut }.map { it.player.id }.toSet()
            val availableNewBatsmen = uiState.battingPlayers.filter {
                it.id != striker?.id && it.id != nonStriker?.id && !outPlayerIds.contains(it.id)
            }

            WicketDialog(
                striker = striker,
                nonStriker = nonStriker,
                fielders = uiState.bowlingPlayers,
                availableNewBatsmen = availableNewBatsmen,
                onDismiss = { showWicketDialog = false },
                onConfirmWicket = { wType, dismissedId, fielderId, newBatsmanId ->
                    val dismissedPlayer = uiState.battingPlayers.find { it.id == dismissedId }
                    celebrationPlayerName = dismissedPlayer?.name
                    activeCelebration = CelebrationType.WICKET
                    viewModel.onWicketFallen(wType, dismissedId, fielderId, newBatsmanId)
                    showWicketDialog = false
                }
            )
        }

        val currentExtrasType = showExtrasDialog
        if (currentExtrasType != null) {
            ExtrasDialog(
                extraType = currentExtrasType,
                onDismiss = { showExtrasDialog = null },
                onConfirm = { runsOffBat, extraRuns ->
                    viewModel.onExtraDelivered(currentExtrasType, runsOffBat, extraRuns)
                    showExtrasDialog = null
                }
            )
        }

        if (showBowlerDialog) {
            val currentBowlerId = calcResult?.currentBowlerId
            val totalBalls = calcResult?.legalBallsBowled ?: 0
            val overNum = totalBalls / 6
            // Filter out bowler who just bowled previous over (enforce no consecutive overs rule)
            val availableBowlers = uiState.bowlingPlayers.filter { it.id != currentBowlerId }
            val selectablePlayers = if (availableBowlers.isNotEmpty()) availableBowlers else uiState.bowlingPlayers

            PlayerSelectionDialog(
                title = if (totalBalls > 0 && totalBalls % 6 == 0) "🎳 Select Bowler for Over ${overNum + 1}" else "🎳 Select Bowler",
                players = selectablePlayers,
                selectedPlayerId = selectablePlayers.firstOrNull()?.id,
                onDismiss = { showBowlerDialog = false },
                onPlayerSelected = { bowler ->
                    viewModel.selectNewBowler(bowler.id)
                    showBowlerDialog = false
                }
            )
        }

        if (showStartSecondInningsDialog) {
            val p2List = uiState.bowlingPlayers // Team 2 becomes batting team in 2nd innings
            val p1List = uiState.battingPlayers // Team 1 becomes bowling team

            var sId by remember { mutableStateOf(p2List.getOrNull(0)?.id ?: 0L) }
            var nsId by remember { mutableStateOf(p2List.getOrNull(1)?.id ?: 0L) }
            var bId by remember { mutableStateOf(p1List.getOrNull(0)?.id ?: 0L) }

            PlayerSelectionDialog(
                title = "Select Bowler for 2nd Innings",
                players = p1List,
                selectedPlayerId = bId,
                onDismiss = { showStartSecondInningsDialog = false },
                onPlayerSelected = { bowler ->
                    viewModel.startSecondInnings(sId, nsId, bowler.id) {
                        showStartSecondInningsDialog = false
                    }
                }
            )
        }

        if (showReviseOversDialog && match != null && innings != null) {
            com.example.scoremaster.ui.components.ReviseOversDialog(
                currentOvers = match.numberOfOvers,
                currentTarget = innings.targetRuns ?: match.dlsRevisedTarget,
                isInnings2 = innings.inningsNumber == 2,
                team1Runs = calcResult?.totalRuns ?: 0,
                team1OversPlayed = (calcResult?.legalBallsBowled ?: 0) / 6.0,
                team1WicketsLost = calcResult?.totalWickets ?: 0,
                onDismiss = { showReviseOversDialog = false },
                onConfirm = { revisedOvers, revisedTarget, dlsParScore, interruption, isTerminated ->
                    viewModel.updateMatchOversAndTarget(
                        revisedOvers = revisedOvers,
                        revisedTarget = revisedTarget,
                        dlsParScore = dlsParScore,
                        interruption = interruption,
                        isTerminated = isTerminated
                    ) {
                        showReviseOversDialog = false
                        val oversPlayed = (calcResult?.legalBallsBowled ?: 0) / 6.0
                        if (innings.inningsNumber == 1 && (isTerminated || oversPlayed >= revisedOvers)) {
                            showStartSecondInningsDialog = true
                        }
                    }
                }
            )
        }

        // Celebration Overlay
        val celebrationType = activeCelebration
        if (celebrationType != null) {
            CricketCelebrationOverlay(
                type = celebrationType,
                playerName = celebrationPlayerName,
                onDismiss = {
                    activeCelebration = null
                    celebrationPlayerName = null
                }
            )
        }
    }
}

@Composable
fun SurfaceBadge(text: String) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
