package com.example.scoremaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.domain.model.ExtraType

@Composable
fun ExtrasDialog(
    extraType: ExtraType,
    onDismiss: () -> Unit,
    onConfirm: (runsOffBat: Int, extraRuns: Int) -> Unit
) {
    var selectedAdditionalRuns by remember { mutableIntStateOf(0) }

    val titleText = when (extraType) {
        ExtraType.WIDE -> "Wide Ball"
        ExtraType.NO_BALL -> "No Ball"
        ExtraType.BYE -> "Bye Runs"
        ExtraType.LEG_BYE -> "Leg Bye Runs"
        ExtraType.NONE -> "Extras"
    }

    val runOptions = when (extraType) {
        ExtraType.WIDE -> listOf(
            0 to "Wide Only\n(1 Extra)",
            1 to "+1 Run\n(2 Extras)",
            2 to "+2 Runs\n(3 Extras)",
            3 to "+3 Runs\n(4 Extras)",
            4 to "+4 Boundary\n(5 Extras)"
        )
        ExtraType.NO_BALL -> listOf(
            0 to "0 Runs\n(1 Extra)",
            1 to "1 Run\noff Bat",
            2 to "2 Runs\noff Bat",
            3 to "3 Runs\noff Bat",
            4 to "Boundary 4\noff Bat",
            6 to "Six 6\noff Bat"
        )
        ExtraType.BYE, ExtraType.LEG_BYE -> listOf(
            1 to "1 Run",
            2 to "2 Runs",
            3 to "3 Runs",
            4 to "4 Boundary"
        )
        ExtraType.NONE -> emptyList()
    }

    val summaryText = when (extraType) {
        ExtraType.WIDE -> if (selectedAdditionalRuns == 0) "1 Wide Ball (1 Extra Run)" else "1 Wide + $selectedAdditionalRuns Additional Runs = ${1 + selectedAdditionalRuns} Total Extras"
        ExtraType.NO_BALL -> {
            val batText = if (selectedAdditionalRuns == 1) "1 Run" else "$selectedAdditionalRuns Runs"
            if (selectedAdditionalRuns == 0) "1 No Ball (1 Extra Run)" else "1 No Ball + $batText off Bat = ${1 + selectedAdditionalRuns} Total Runs"
        }
        ExtraType.BYE -> "$selectedAdditionalRuns Bye Runs"
        ExtraType.LEG_BYE -> "$selectedAdditionalRuns Leg Bye Runs"
        ExtraType.NONE -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select runs for this delivery:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                runOptions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (runs, label) ->
                            val isSelected = selectedAdditionalRuns == runs
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                            Button(
                                onClick = { selectedAdditionalRuns = runs },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = containerColor,
                                    contentColor = contentColor
                                )
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (summaryText.isNotBlank()) {
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (extraType) {
                        ExtraType.WIDE -> {
                            onConfirm(0, 1 + selectedAdditionalRuns)
                        }
                        ExtraType.NO_BALL -> {
                            onConfirm(selectedAdditionalRuns, 1)
                        }
                        ExtraType.BYE, ExtraType.LEG_BYE -> {
                            val byeRuns = if (selectedAdditionalRuns == 0) 1 else selectedAdditionalRuns
                            onConfirm(0, byeRuns)
                        }
                        ExtraType.NONE -> onConfirm(0, 0)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
