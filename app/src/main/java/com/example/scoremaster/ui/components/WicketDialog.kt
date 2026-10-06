package com.example.scoremaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scoremaster.domain.model.Player
import com.example.scoremaster.domain.model.WicketType

@Composable
fun WicketDialog(
    striker: Player?,
    nonStriker: Player?,
    fielders: List<Player> = emptyList(),
    availableNewBatsmen: List<Player>,
    onDismiss: () -> Unit,
    onConfirmWicket: (
        wicketType: WicketType,
        dismissedPlayerId: Long,
        fielderId: Long?,
        newBatsmanId: Long?
    ) -> Unit
) {
    var selectedWicketType by remember { mutableStateOf(WicketType.BOWLED) }
    var selectedDismissedPlayerId by remember { mutableStateOf(striker?.id ?: 0L) }
    var selectedFielderId by remember { mutableStateOf<Long?>(fielders.firstOrNull()?.id) }
    var selectedNewBatsmanId by remember { mutableStateOf<Long?>(availableNewBatsmen.firstOrNull()?.id) }

    var expandedTypeMenu by remember { mutableStateOf(false) }
    var expandedFielderMenu by remember { mutableStateOf(false) }
    var expandedBatsmanMenu by remember { mutableStateOf(false) }

    val requiresFielder = selectedWicketType == WicketType.CAUGHT ||
        selectedWicketType == WicketType.RUN_OUT ||
        selectedWicketType == WicketType.STUMPED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wicket Fallen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Select Wicket Type Dropdown
                Text(
                    text = "Dismissal Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedTypeMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedWicketType.displayName)
                    }

                    DropdownMenu(
                        expanded = expandedTypeMenu,
                        onDismissRequest = { expandedTypeMenu = false }
                    ) {
                        WicketType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedWicketType = type
                                    expandedTypeMenu = false
                                }
                            )
                        }
                    }
                }

                // Select Fielder Dropdown (if Caught/Run Out/Stumped)
                if (requiresFielder && fielders.isNotEmpty()) {
                    Text(
                        text = "Select Fielder",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentFielderName = fielders.find { it.id == selectedFielderId }?.name ?: "Select Fielder"
                        OutlinedButton(
                            onClick = { expandedFielderMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(currentFielderName)
                        }

                        DropdownMenu(
                            expanded = expandedFielderMenu,
                            onDismissRequest = { expandedFielderMenu = false }
                        ) {
                            fielders.forEach { player ->
                                DropdownMenuItem(
                                    text = { Text(player.name) },
                                    onClick = {
                                        selectedFielderId = player.id
                                        expandedFielderMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Select who is out (Striker or Non-striker)
                Text(
                    text = "Dismissed Player",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    if (striker != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedDismissedPlayerId == striker.id),
                                    onClick = { selectedDismissedPlayerId = striker.id }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedDismissedPlayerId == striker.id),
                                onClick = { selectedDismissedPlayerId = striker.id }
                            )
                            Text(
                                text = "${striker.name} (Striker)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (nonStriker != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedDismissedPlayerId == nonStriker.id),
                                    onClick = { selectedDismissedPlayerId = nonStriker.id }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedDismissedPlayerId == nonStriker.id),
                                onClick = { selectedDismissedPlayerId = nonStriker.id }
                            )
                            Text(
                                text = "${nonStriker.name} (Non-Striker)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Select New Batsman Dropdown if available
                if (availableNewBatsmen.isNotEmpty()) {
                    Text(
                        text = "Next Batsman",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentNewBatsmanName = availableNewBatsmen.find { it.id == selectedNewBatsmanId }?.name ?: "Select Next Batsman"
                        OutlinedButton(
                            onClick = { expandedBatsmanMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(currentNewBatsmanName)
                        }

                        DropdownMenu(
                            expanded = expandedBatsmanMenu,
                            onDismissRequest = { expandedBatsmanMenu = false }
                        ) {
                            availableNewBatsmen.forEach { player ->
                                DropdownMenuItem(
                                    text = { Text(player.name) },
                                    onClick = {
                                        selectedNewBatsmanId = player.id
                                        expandedBatsmanMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmWicket(
                        selectedWicketType,
                        selectedDismissedPlayerId,
                        if (requiresFielder) selectedFielderId else null,
                        selectedNewBatsmanId
                    )
                }
            ) {
                Text("Confirm Wicket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
