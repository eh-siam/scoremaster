package com.example.scoremaster.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.dls.DlsInterruption
import com.example.scoremaster.domain.engine.DlsCalculator

@Composable
fun ReviseOversDialog(
    currentOvers: Int,
    currentTarget: Int?,
    isInnings2: Boolean,
    team1Runs: Int = 0,
    team1OversPlayed: Double = 0.0,
    team1WicketsLost: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (revisedOvers: Int, revisedTarget: Int?, dlsParScore: Int?, interruption: DlsInterruption?, isTerminated: Boolean) -> Unit
) {
    val context = LocalContext.current
    var oversText by remember { mutableStateOf(currentOvers.toString()) }
    var targetText by remember { mutableStateOf(currentTarget?.toString() ?: "") }
    var showConfirmation by remember { mutableStateOf(false) }

    val parsedRevisedOvers = oversText.toIntOrNull()?.coerceIn(1, currentOvers) ?: currentOvers
    val minOversRequired = if (currentOvers > 20) 20 else 5

    val dlsResult = DlsCalculator.calculateDlsTarget(
        team1Runs = team1Runs,
        team1OversPlayed = team1OversPlayed,
        team1WicketsLost = team1WicketsLost,
        totalMatchOvers = currentOvers,
        team2RevisedOvers = parsedRevisedOvers,
        isFirstInningsTerminated = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🌧️ ICC Standard DLS Target",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update match overs due to rain or time limits:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = oversText,
                    onValueChange = { oversText = it },
                    label = { Text("Revised Match Overs *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto DLS Target:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                color = if (dlsResult.isTargetIncreased) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val r1Formatted = String.format(LocalLocale.current.platformLocale, "%.1f", dlsResult.team1ResourcePercent)
                                val r2Formatted = String.format(LocalLocale.current.platformLocale, "%.1f", dlsResult.team2ResourcePercent)
                                Text(
                                    text = "R1: $r1Formatted% | R2: $r2Formatted%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dlsResult.isTargetIncreased) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "${dlsResult.dlsTargetRuns} Runs in $parsedRevisedOvers Overs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = dlsResult.explanationText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { targetText = dlsResult.dlsTargetRuns.toString() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Apply Auto DLS Target (${dlsResult.dlsTargetRuns})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isInnings2) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Final Target Runs (2nd Innings)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isInnings2 && team1OversPlayed < minOversRequired) {
                        Toast.makeText(
                            context,
                            "At least $minOversRequired overs must be played in 1st innings to apply DLS target.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showConfirmation = true
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply DLS Revisions", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showConfirmation) {
        val revisedOvers = oversText.toIntOrNull()?.coerceAtLeast(1) ?: currentOvers
        val finalTarget = targetText.toIntOrNull() ?: dlsResult.dlsTargetRuns
        val targetToDisplay = if (isInnings2) finalTarget else dlsResult.dlsTargetRuns

        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = {
                Text(
                    text = "❓ Confirm DLS Application",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to revise match to $revisedOvers overs with Target $targetToDisplay runs?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val interruption = DlsInterruption(
                            interruptionNumber = 1,
                            scoreAtInterruption = team1Runs,
                            wicketsAtInterruption = team1WicketsLost,
                            oversAtInterruption = team1OversPlayed,
                            scheduledOversBeforeInterruption = currentOvers,
                            revisedOversAfterInterruption = revisedOvers,
                            resourceBeforeInterruption = dlsResult.team1ResourcePercent,
                            resourceLost = (100.0 - dlsResult.team1ResourcePercent).coerceAtLeast(0.0),
                            resourceRemaining = dlsResult.team2ResourcePercent
                        )
                        showConfirmation = false
                        onConfirm(
                            revisedOvers,
                            targetToDisplay,
                            dlsResult.dlsParScore,
                            interruption,
                            true
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Yes, Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
