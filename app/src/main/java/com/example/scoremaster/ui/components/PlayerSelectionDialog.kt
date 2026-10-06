package com.example.scoremaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scoremaster.domain.model.Player

@Composable
fun PlayerSelectionDialog(
    title: String,
    players: List<Player>,
    selectedPlayerId: Long?,
    onDismiss: () -> Unit,
    onPlayerSelected: (Player) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf(players.find { it.id == selectedPlayerId } ?: players.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Player:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedPlayer?.name ?: "Select Player")
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        players.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.name) },
                                onClick = {
                                    selectedPlayer = player
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlayer?.let { onPlayerSelected(it) }
                },
                enabled = selectedPlayer != null
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
