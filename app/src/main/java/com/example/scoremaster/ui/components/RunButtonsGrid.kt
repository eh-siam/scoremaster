package com.example.scoremaster.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RunButtonsGrid(
    onRunClick: (Int) -> Unit,
    onWideClick: () -> Unit,
    onNoBallClick: () -> Unit,
    onByeClick: () -> Unit,
    onLegByeClick: () -> Unit,
    onWicketClick: () -> Unit,
    onUndoClick: () -> Unit,
    onSwapStrikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Runs Row: 0, 1, 2, 3, 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val runButtons = listOf(0, 1, 2, 3, 4, 5, 6)
            runButtons.forEach { run ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.90f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
                    label = "scale"
                )

                val isBoundary = run == 4 || run == 6
                val containerColor = if (isBoundary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                val contentColor = if (isBoundary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

                Button(
                    onClick = { onRunClick(run) },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = run.toString(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Extras Row: WIDE, NO BALL, BYE, LEG BYE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val extras = listOf(
                "WIDE" to onWideClick,
                "NO BALL" to onNoBallClick,
                "BYE" to onByeClick,
                "L. BYE" to onLegByeClick
            )
            extras.forEach { (label, onClick) ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
                    label = "scale"
                )

                OutlinedButton(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                ) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // Action Row: WICKET, SWAP STRIKE, UNDO LAST BALL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val wicketInteraction = remember { MutableInteractionSource() }
            val wicketPressed by wicketInteraction.collectIsPressedAsState()
            val wicketScale by animateFloatAsState(
                targetValue = if (wicketPressed) 0.90f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "wicketScale"
            )

            Button(
                onClick = onWicketClick,
                interactionSource = wicketInteraction,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .scale(wicketScale),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("WICKET", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            val swapInteraction = remember { MutableInteractionSource() }
            val swapPressed by swapInteraction.collectIsPressedAsState()
            val swapScale by animateFloatAsState(
                targetValue = if (swapPressed) 0.92f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "swapScale"
            )

            OutlinedButton(
                onClick = onSwapStrikeClick,
                interactionSource = swapInteraction,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .scale(swapScale),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
            ) {
                Text("Swap Strike", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            val undoInteraction = remember { MutableInteractionSource() }
            val undoPressed by undoInteraction.collectIsPressedAsState()
            val undoScale by animateFloatAsState(
                targetValue = if (undoPressed) 0.92f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "undoScale"
            )

            OutlinedButton(
                onClick = onUndoClick,
                interactionSource = undoInteraction,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .scale(undoScale),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
            ) {
                Text("Undo Ball", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
