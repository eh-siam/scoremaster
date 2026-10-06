package com.example.scoremaster.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.BallEvent

@Composable
fun BallHistoryRow(
    oversMap: Map<Int, List<BallEvent>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "BALL-BY-BALL HISTORY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (oversMap.isEmpty()) {
                Text(
                    text = "No balls bowled yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                val sortedOvers = oversMap.entries.sortedByDescending { it.key }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sortedOvers.forEach { (overIndex, balls) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Over ${overIndex + 1}:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(end = 10.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(balls) { ball ->
                                    BallBadge(ball = ball)
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
fun BallBadge(ball: BallEvent) {
    val isWicket = ball.isWicket
    val isSix = ball.runsOffBat == 6
    val isFour = ball.runsOffBat == 4
    val isExtra = ball.isExtra

    val backgroundBrush = when {
        isWicket -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
        isSix -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
        isFour -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        isExtra -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        else -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
    }

    val textColor = when {
        isWicket || isSix || isFour || isExtra -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isWicket -> Color(0xFFF87171)
        isSix -> Color(0xFF60A5FA)
        isFour -> Color(0xFF34D399)
        isExtra -> Color(0xFFFBBF24)
        else -> Color(0xFFCBD5E1)
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .shadow(2.dp, CircleShape)
            .background(brush = backgroundBrush, shape = CircleShape)
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball.shortDisplayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}
