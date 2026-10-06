package com.example.scoremaster.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.navigation.ScoreMasterBottomBar
import com.example.scoremaster.ui.components.OverBarChartComponent
import com.example.scoremaster.ui.components.calculateOverSummaries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavHostController,
    onNewMatchClick: () -> Unit,
    onTournamentClick: () -> Unit = onNewMatchClick,
    onMatchClick: (Match) -> Unit
) {
    val allMatches by viewModel.allMatches.collectAsState()
    val matchInningsMap by viewModel.matchInningsMap.collectAsState()
    val filteredMatches by viewModel.filteredMatches.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val haptic = LocalHapticFeedback.current

    val totalCount = allMatches.size
    val liveCount = allMatches.count { it.status == MatchStatus.IN_PROGRESS }
    val completedCount = allMatches.count { it.status == MatchStatus.COMPLETED }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier.padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsCricket,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy((2).dp)) {
                            Text(
                                text = "ScoreMaster",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "CRICKET DASHBOARD",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showMenu = true
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Match", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    onNewMatchClick()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            ScoreMasterBottomBar(navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // 1. HERO BANNER CARD ("READY TO SCORE?") WITH 3D FLOATING TILT
            item {
                val infinite3dTransition = rememberInfiniteTransition(label = "3dHeroCardTransition")

                val rotationY by infinite3dTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rotationY"
                )

                val rotationX by infinite3dTransition.animateFloat(
                    initialValue = 3f,
                    targetValue = -3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rotationX"
                )

                val floatY by infinite3dTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "floatY"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.rotationX = rotationX
                            this.rotationY = rotationY
                            this.translationY = floatY
                            this.cameraDistance = 16f * density
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Animated3dCricketIcon()

                            Text(
                                text = "READY TO SCORE?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "Start a new cricket match and keep every ball recorded in real-time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ ICC DLS Ready",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📊 Real-Time Scorecard",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. DASHBOARD STATS SUMMARY COUNTER BAR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatCounterItem(
                            count = totalCount.toString(),
                            label = "Matches"
                        )

                        Surface(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        ) {}

                        StatCounterItem(
                            count = liveCount.toString(),
                            label = "Live",
                            isLive = liveCount > 0
                        )

                        Surface(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        ) {}

                        StatCounterItem(
                            count = completedCount.toString(),
                            label = "Completed"
                        )
                    }
                }
            }

            // 3. QUICK ACTION BUTTONS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNewMatchClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("New Match", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onTournamentClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Tournament", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. SEARCH BAR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search team, venue or match...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // 5. MATCH CATEGORY FILTER CHIPS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MatchFilterTab.entries) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onFilterSelected(filter)
                            },
                            label = {
                                Text(
                                    text = filter.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // 6. CATEGORIZED MATCH FEED SECTIONS
            val liveMatches = filteredMatches.filter { it.status == MatchStatus.IN_PROGRESS }
            val upcomingMatches = filteredMatches.filter { it.status == MatchStatus.NOT_STARTED }
            val recentMatches = filteredMatches.filter { it.status == MatchStatus.COMPLETED }

            if (filteredMatches.isEmpty()) {
                item {
                    EmptyMatchStateCard(
                        filterName = selectedFilter.displayName,
                        searchQuery = searchQuery
                    )
                }
            } else {
                // Section 1: Live Now
                if (selectedFilter == MatchFilterTab.ALL || selectedFilter == MatchFilterTab.LIVE) {
                    if (liveMatches.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "LIVE NOW",
                                textColor = MaterialTheme.colorScheme.error,
                                showPulsingDot = true
                            )
                        }
                        items(liveMatches) { match ->
                            DashboardLiveMatchCard(
                                match = match,
                                inningsList = matchInningsMap[match.id] ?: emptyList(),
                                onClick = { onMatchClick(match) }
                            )
                        }
                    }
                }

                // Section 2: Upcoming
                if (selectedFilter == MatchFilterTab.ALL || selectedFilter == MatchFilterTab.UPCOMING) {
                    if (upcomingMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "📅 UPCOMING", textColor = MaterialTheme.colorScheme.primary)
                        }
                        items(upcomingMatches) { match ->
                            DashboardUpcomingMatchCard(
                                match = match,
                                onClick = { onMatchClick(match) }
                            )
                        }
                    }
                }

                // Section 3: Recent / Completed
                if (selectedFilter == MatchFilterTab.ALL || selectedFilter == MatchFilterTab.COMPLETED) {
                    if (recentMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "RECENT MATCHES", textColor = MaterialTheme.colorScheme.onSurface)
                        }
                        items(recentMatches) { match ->
                            DashboardCompletedMatchCard(
                                match = match,
                                inningsList = matchInningsMap[match.id] ?: emptyList(),
                                onClick = { onMatchClick(match) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showPulsingDot: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        if (showPulsingDot) {
            PulsingLiveDot(size = 8.dp)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
fun StatCounterItem(
    count: String,
    label: String,
    isLive: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isLive) {
                PulsingLiveDot(size = 6.dp)
            }
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DashboardLiveMatchCard(
    match: Match,
    inningsList: List<Innings>,
    onClick: () -> Unit
) {
    val inn1 = inningsList.find { it.inningsNumber == 1 }
    val inn2 = inningsList.find { it.inningsNumber == 2 }
    val currentInnings = when (match.currentInningsIndex) {
        2 -> inn2 ?: inn1
        else -> inn1
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = "${match.team1Name} vs ${match.team2Name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PulsingLiveDot(size = 6.dp)
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Scores Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val inn1Batting = inn1?.battingTeamName ?: match.team1Name
                    val inn1Score = if (inn1 != null) "${inn1.totalRuns}/${inn1.totalWickets}" else "0/0"
                    Text(
                        text = "$inn1Batting: $inn1Score",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (inn2 != null) {
                        Text(
                            text = "${inn2.battingTeamName}: ${inn2.totalRuns}/${inn2.totalWickets}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val currentOvers = currentInnings?.oversFormatted ?: "0.0"
                Text(
                    text = "$currentOvers / ${match.effectiveOvers} Ov",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.venue ?: "Live Match",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "View →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DashboardUpcomingMatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${match.team1Name} vs ${match.team2Name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${match.date} • ${match.venue ?: "${match.numberOfOvers} Overs"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "View →",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DashboardCompletedMatchCard(
    match: Match,
    inningsList: List<Innings>,
    onClick: () -> Unit
) {
    val inn1 = inningsList.find { it.inningsNumber == 1 }
    val inn2 = inningsList.find { it.inningsNumber == 2 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Team 1 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inn1?.battingTeamName ?: match.team1Name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val inn1Overs = inn1?.oversFormatted.orEmpty()
                Text(
                    text = "${inn1?.totalRuns ?: 0}/${inn1?.totalWickets ?: 0}${if (inn1Overs.isNotBlank()) " ($inn1Overs ov)" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Team 2 Row
            if (inn2 != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inn2.battingTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val inn2Overs = inn2.oversFormatted
                    Text(
                        text = "${inn2.totalRuns}/${inn2.totalWickets}${if (inn2Overs.isNotBlank()) " ($inn2Overs ov)" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Result Margin & View Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!match.winningMargin.isNull_or_blank()) {
                    val marginStr = if (match.dlsRevisedTarget != null || match.dlsRevisedOvers != null) {
                        "${match.winningMargin} (DLS)"
                    } else {
                        match.winningMargin.orEmpty()
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = marginStr,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "View Scorecard",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun PulsingLiveDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liveDotPulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(color = color, shape = CircleShape)
    )
}

@Composable
fun Animated3dCricketIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "inner3dIcon")

    // 3D Rotation on Y-axis (360 degrees spin)
    val iconRotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotationY"
    )

    // Pulse scale effect
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .graphicsLayer {
                    this.rotationY = iconRotationY
                    this.scaleX = iconPulse
                    this.scaleY = iconPulse
                    this.cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsCricket,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun EmptyMatchStateCard(
    filterName: String,
    searchQuery: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsCricket,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = if (searchQuery.isNotBlank()) "No Results Found" else "No $filterName Matches Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "No matches found matching '$searchQuery'. Try searching for a different team or venue."
                } else {
                    "There are no $filterName matches recorded right now."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
