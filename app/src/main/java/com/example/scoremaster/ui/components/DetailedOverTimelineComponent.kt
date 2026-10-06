package com.example.scoremaster.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.Player

@Composable
fun DetailedOverTimelineComponent(
    oversMap: Map<Int, List<BallEvent>>,
    allPlayers: List<Player>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (oversMap.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ball history recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val sortedOvers = oversMap.entries.sortedByDescending { it.key }

            sortedOvers.forEach { (overIndex, balls) ->
                val overRuns = balls.sumOf { ball ->
                    when (ball.extraType) {
                        ExtraType.NONE -> ball.runsOffBat
                        ExtraType.WIDE, ExtraType.BYE, ExtraType.LEG_BYE -> ball.extraRuns
                        ExtraType.NO_BALL -> 1 + ball.runsOffBat + ball.extraRuns
                    }
                }
                val overWickets = balls.count { it.isWicket }
                val bowler = balls.firstOrNull()?.let { b -> allPlayers.find { it.id == b.bowlerId } }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Over Header
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
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "OVER ${overIndex + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = bowler?.name ?: "Bowler",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "$overRuns Runs • $overWickets Wkts",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Ball-by-ball list
                        balls.forEach { ball ->
                            val striker = allPlayers.find { it.id == ball.strikerId }
                            val ballBowler = allPlayers.find { it.id == ball.bowlerId }

                            val ballDesc = when {
                                ball.isWicket -> "WICKET! (${ball.wicketType?.name ?: "Out"})"
                                ball.runsOffBat == 4 -> "4 RUNS! (FOUR)"
                                ball.runsOffBat == 6 -> "6 RUNS! (SIX)"
                                ball.extraType == ExtraType.WIDE -> "WIDE (${ball.extraRuns} Runs)"
                                ball.extraType == ExtraType.NO_BALL -> "NO BALL (${1 + ball.runsOffBat + ball.extraRuns} Runs)"
                                ball.extraType == ExtraType.BYE -> "BYE (${ball.extraRuns} Runs)"
                                ball.extraType == ExtraType.LEG_BYE -> "LEG BYE (${ball.extraRuns} Runs)"
                                ball.runsOffBat == 0 -> "Dot ball (No run)"
                                else -> "${ball.runsOffBat} Run${if (ball.runsOffBat > 1) "s" else ""}"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BallBadge(ball = ball)

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ballBowler?.name ?: "Bowler"} to ${striker?.name ?: "Batsman"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = ballDesc,
                                        fontSize = 11.sp,
                                        fontWeight = if (ball.isWicket || ball.runsOffBat >= 4) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            ball.isWicket -> MaterialTheme.colorScheme.error
                                            ball.runsOffBat == 4 || ball.runsOffBat == 6 -> Color(0xFF10B981)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
