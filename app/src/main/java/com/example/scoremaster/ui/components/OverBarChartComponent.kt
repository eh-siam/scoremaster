package com.example.scoremaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.ExtraType

data class OverSummary(
    val overNumber: Int,
    val runs: Int,
    val wickets: Int,
    val hasBoundary: Boolean = false,
    val teamName: String? = null
)

fun calculateOverSummaries(ballEvents: List<BallEvent>, teamName: String? = null): List<OverSummary> {
    if (ballEvents.isEmpty()) return emptyList()
    val oversMap = ballEvents.groupBy { it.overNumber }
    return oversMap.entries.sortedBy { it.key }.map { (overIdx, balls) ->
        val overRuns = balls.sumOf { ball ->
            when (ball.extraType) {
                ExtraType.NONE -> ball.runsOffBat
                ExtraType.WIDE, ExtraType.BYE, ExtraType.LEG_BYE -> ball.extraRuns
                ExtraType.NO_BALL -> 1 + ball.runsOffBat + ball.extraRuns
            }
        }
        val overWickets = balls.count { it.isWicket }
        val hasBoundary = balls.any { it.runsOffBat == 4 || it.runsOffBat == 6 }
        OverSummary(
            overNumber = overIdx + 1,
            runs = overRuns,
            wickets = overWickets,
            hasBoundary = hasBoundary,
            teamName = teamName
        )
    }
}

@Composable
fun OverBarChartComponent(
    overSummaries: List<OverSummary>,
    teamName: String? = null,
    modifier: Modifier = Modifier
) {
    if (overSummaries.isEmpty()) return

    val maxRunsInOver = (overSummaries.maxOfOrNull { it.runs } ?: 12).coerceAtLeast(12)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071A2B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!teamName.isNull_or_blank()) "📊 MANHATTAN OVER GRAPH ($teamName)" else "📊 MANHATTAN OVER GRAPH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF19C37D),
                    letterSpacing = 0.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendTag(color = Color(0xFF10B981), label = "Boundary")
                    LegendTag(color = Color(0xFFEF4444), label = "Wicket")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                items(overSummaries) { over ->
                    OverBarItem(over = over, maxRuns = maxRunsInOver)
                }
            }
        }
    }
}

@Composable
fun OverBarItem(
    over: OverSummary,
    maxRuns: Int
) {
    val barHeightFraction = (over.runs.toFloat() / maxRuns.toFloat()).coerceIn(0.12f, 1.0f)
    val maxBarHeight = 65.dp
    val barHeight = maxBarHeight * barHeightFraction

    val barBrush = when {
        over.wickets > 0 -> Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
        over.hasBoundary || over.runs >= 10 -> Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        over.runs > 0 -> Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
        else -> Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Wicket Badge or Run Label
        if (over.wickets > 0) {
            Surface(
                color = Color(0xFFEF4444),
                shape = CircleShape
            ) {
                Text(
                    text = "${over.wickets}W",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        } else {
            Text(
                text = over.runs.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (over.runs >= 10) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f)
            )
        }

        // Vertical Bar
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(brush = barBrush)
                .border(
                    width = 1.dp,
                    color = when {
                        over.wickets > 0 -> Color(0xFFF87171)
                        over.hasBoundary -> Color(0xFF34D399)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
        )

        // Over Number Label
        Text(
            text = "Ov${over.overNumber}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LegendTag(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
