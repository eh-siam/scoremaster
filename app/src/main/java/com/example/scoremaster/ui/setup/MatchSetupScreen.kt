package com.example.scoremaster.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavHostController
import com.example.scoremaster.navigation.ScoreMasterBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: MatchSetupViewModel,
    initialType: String? = null,
    navController: NavHostController? = null,
    onBackClick: () -> Unit,
    onMatchCreated: (matchId: Long) -> Unit,
    onTournamentCreated: (tournamentId: Long) -> Unit
) {
    LaunchedEffect(initialType) {
        val type = when (initialType) {
            "ONE_VS_ONE" -> MatchType.ONE_VS_ONE
            "TRIANGULAR" -> MatchType.TRIANGULAR
            "TOURNAMENT" -> MatchType.TOURNAMENT
            else -> null
        }
        viewModel.setMatchType(type)
    }
    val matchType by viewModel.matchType.collectAsState()
    val matchName by viewModel.matchName.collectAsState()
    val team1Name by viewModel.team1Name.collectAsState()
    val team2Name by viewModel.team2Name.collectAsState()
    val numberOfOvers by viewModel.numberOfOvers.collectAsState()
    val venue by viewModel.venue.collectAsState()
    val matchDate by viewModel.matchDate.collectAsState()

    val team1Players by viewModel.team1Players.collectAsState()
    val team2Players by viewModel.team2Players.collectAsState()

    val triangularName by viewModel.triangularName.collectAsState()
    val triangularTeam1Name by viewModel.triangularTeam1Name.collectAsState()
    val triangularTeam2Name by viewModel.triangularTeam2Name.collectAsState()
    val triangularTeam3Name by viewModel.triangularTeam3Name.collectAsState()

    val tournamentName by viewModel.tournamentName.collectAsState()
    val numberOfTeams by viewModel.numberOfTeams.collectAsState()
    val tournamentTeamNames by viewModel.tournamentTeamNames.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (matchType) {
                            MatchType.ONE_VS_ONE -> "Head-to-Head Match Setup"
                            MatchType.TRIANGULAR -> "Tri-Series Setup"
                            MatchType.TOURNAMENT -> "Tournament Setup"
                            null -> "Create New Match"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (matchType != null) {
                                viewModel.clearMatchType()
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            navController?.let { ScoreMasterBottomBar(navController = it) }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = matchType,
            animationSpec = tween(durationMillis = 300),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { currentType ->
            if (currentType == null) {
                // Initial Selection View - Sleek Animated Stadium Aesthetic
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isVisible = true
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Subtitle Banner
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SELECT MATCH FORMAT",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Format 1: 1 vs 1
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(300)
                        )
                    ) {
                        FormatSelectionCard(
                            title = "Head-to-Head Match",
                            subtitle = "Quick bilateral match between 2 teams with custom squad",
                            badgeText = "⚡ Quick Match",
                            badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                            badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            icon = Icons.Default.Person,
                            onClick = { viewModel.setMatchType(MatchType.ONE_VS_ONE) }
                        )
                    }

                    // Format 2: Tri-Series
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(450)) + slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(450)
                        )
                    ) {
                        FormatSelectionCard(
                            title = "Tri-Nation Series",
                            subtitle = "3 teams round-robin series & standings",
                            badgeText = "🔥 3-Team Series",
                            badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                            badgeTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            icon = Icons.Default.Group,
                            onClick = { viewModel.setMatchType(MatchType.TRIANGULAR) }
                        )
                    }

                    // Format 3: Full Tournament
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(600)
                        )
                    ) {
                        FormatSelectionCard(
                            title = "Cricket Tournament",
                            subtitle = "Multi-team league, points table & fixtures",
                            badgeText = "🏆 League & Fixtures",
                            badgeColor = MaterialTheme.colorScheme.primaryContainer,
                            badgeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            icon = Icons.Default.EmojiEvents,
                            onClick = { viewModel.setMatchType(MatchType.TOURNAMENT) }
                        )
                    }
                }
            } else {
                // Detailed Form View for Selected Type
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearMatchType() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Change Match Format", fontWeight = FontWeight.Bold)
                        }
                    }

                    when (currentType) {
                        MatchType.ONE_VS_ONE -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "🏏 Head-to-Head Match Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B633D)
                                    )

                                    OutlinedTextField(
                                        value = team1Name,
                                        onValueChange = { viewModel.team1Name.value = it },
                                        label = { Text("Team 1 Name *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = team2Name,
                                        onValueChange = { viewModel.team2Name.value = it },
                                        label = { Text("Team 2 Name *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = numberOfOvers,
                                        onValueChange = { viewModel.numberOfOvers.value = it },
                                        label = { Text("Number of Overs *") },
                                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = matchName,
                                        onValueChange = { viewModel.matchName.value = it },
                                        label = { Text("Match Name (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.SportsCricket, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = venue,
                                        onValueChange = { viewModel.venue.value = it },
                                        label = { Text("Venue (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = matchDate,
                                        onValueChange = { viewModel.matchDate.value = it },
                                        label = { Text("Match Date") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            // Team 1 Playing 11 Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "📋 ${team1Name.ifBlank { "Team 1" }} Squad Player Names",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B633D)
                                    )

                                    team1Players.forEachIndexed { index, pName ->
                                        OutlinedTextField(
                                            value = pName,
                                            onValueChange = { viewModel.updateTeam1Player(index, it) },
                                            label = { Text("Player ${index + 1}") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                            trailingIcon = {
                                                if (team1Players.size > 2) {
                                                    IconButton(onClick = { viewModel.removeTeam1Player(index) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = if (index == team1Players.lastIndex) ImeAction.Done else ImeAction.Next
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.addTeam1Player() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("+ Add Player (${team1Players.size} Players)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Team 2 Playing Squad Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "📋 ${team2Name.ifBlank { "Team 2" }} Squad Player Names",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B633D)
                                    )

                                    team2Players.forEachIndexed { index, pName ->
                                        OutlinedTextField(
                                            value = pName,
                                            onValueChange = { viewModel.updateTeam2Player(index, it) },
                                            label = { Text("Player ${index + 1}") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                            trailingIcon = {
                                                if (team2Players.size > 2) {
                                                    IconButton(onClick = { viewModel.removeTeam2Player(index) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = if (index == team2Players.lastIndex) ImeAction.Done else ImeAction.Next
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.addTeam2Player() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("+ Add Player (${team2Players.size} Players)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.createMatch { matchId ->
                                        onMatchCreated(matchId)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Create & Start Match", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }

                        MatchType.TRIANGULAR -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "🔥 Tri-Series Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B633D)
                                    )

                                    OutlinedTextField(
                                        value = triangularName,
                                        onValueChange = { viewModel.triangularName.value = it },
                                        label = { Text("Series Name *") },
                                        leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = triangularTeam1Name,
                                        onValueChange = { viewModel.triangularTeam1Name.value = it },
                                        label = { Text("Team 1 Name *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = triangularTeam2Name,
                                        onValueChange = { viewModel.triangularTeam2Name.value = it },
                                        label = { Text("Team 2 Name *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = triangularTeam3Name,
                                        onValueChange = { viewModel.triangularTeam3Name.value = it },
                                        label = { Text("Team 3 Name *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = numberOfOvers,
                                        onValueChange = { viewModel.numberOfOvers.value = it },
                                        label = { Text("Number of Overs *") },
                                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = venue,
                                        onValueChange = { viewModel.venue.value = it },
                                        label = { Text("Venue (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = matchDate,
                                        onValueChange = { viewModel.matchDate.value = it },
                                        label = { Text("Match Date") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.createTriangularSeries { tournamentId ->
                                        onTournamentCreated(tournamentId)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Create Tri-Series", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }

                        MatchType.TOURNAMENT -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "🏆 Tournament Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0B633D)
                                    )

                                    OutlinedTextField(
                                        value = tournamentName,
                                        onValueChange = { viewModel.tournamentName.value = it },
                                        label = { Text("Tournament Name *") },
                                        leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = numberOfTeams,
                                        onValueChange = { viewModel.updateNumberOfTeams(it) },
                                        label = { Text("Number of Teams (e.g. 4, 6) *") },
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = numberOfOvers,
                                        onValueChange = { viewModel.numberOfOvers.value = it },
                                        label = { Text("Number of Overs *") },
                                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = venue,
                                        onValueChange = { viewModel.venue.value = it },
                                        label = { Text("Venue (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = matchDate,
                                        onValueChange = { viewModel.matchDate.value = it },
                                        label = { Text("Match Date") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Text(
                                        text = "Team Names",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )

                                    tournamentTeamNames.forEachIndexed { index, name ->
                                        OutlinedTextField(
                                            value = name,
                                            onValueChange = { viewModel.updateTournamentTeamName(index, it) },
                                            label = { Text("Team ${index + 1} Name") },
                                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.createTournament { tournamentId ->
                                        onTournamentCreated(tournamentId)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Create Tournament", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormatSelectionCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
