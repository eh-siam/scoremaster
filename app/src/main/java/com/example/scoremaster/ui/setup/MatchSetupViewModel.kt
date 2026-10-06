package com.example.scoremaster.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MatchType {
    ONE_VS_ONE, TRIANGULAR, TOURNAMENT
}

class MatchSetupViewModel(
    private val matchRepository: MatchRepository,
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _matchType = MutableStateFlow<MatchType?>(null)
    val matchType: StateFlow<MatchType?> = _matchType.asStateFlow()

    // 1v1 Fields
    var matchName = MutableStateFlow("")
    var team1Name = MutableStateFlow("Bangladesh")
    var team2Name = MutableStateFlow("India")
    var numberOfOvers = MutableStateFlow("20")
    var venue = MutableStateFlow("Mirpur Stadium")
    var matchDate = MutableStateFlow("2026-09-30")

    var team1Players = MutableStateFlow(List(11) { i -> "Player ${i + 1}" })
    var team2Players = MutableStateFlow(List(11) { i -> "Player ${i + 1}" })

    // Triangular Series Fields
    var triangularName = MutableStateFlow("Tri-Nation Cricket Series")
    var triangularTeam1Name = MutableStateFlow("Bangladesh")
    var triangularTeam2Name = MutableStateFlow("India")
    var triangularTeam3Name = MutableStateFlow("Sri Lanka")

    // Tournament Fields
    var tournamentName = MutableStateFlow("Dhaka Corporate Cup")
    var numberOfTeams = MutableStateFlow("4")
    var tournamentTeamNames = MutableStateFlow(listOf("Team A", "Team B", "Team C", "Team D"))

    fun setMatchType(type: MatchType?) {
        _matchType.value = type
    }

    fun clearMatchType() {
        _matchType.value = null
    }

    fun updateTeam1Player(index: Int, name: String) {
        val current = team1Players.value.toMutableList()
        if (index in current.indices) {
            current[index] = name
            team1Players.value = current
        }
    }

    fun updateTeam2Player(index: Int, name: String) {
        val current = team2Players.value.toMutableList()
        if (index in current.indices) {
            current[index] = name
            team2Players.value = current
        }
    }

    fun updateNumberOfTeams(countStr: String) {
        numberOfTeams.value = countStr
        val count = countStr.toIntOrNull()?.coerceIn(2, 16) ?: 4
        val currentList = tournamentTeamNames.value
        val newList = List(count) { i ->
            if (i < currentList.size && currentList[i].isNotBlank()) currentList[i] else "Team ${i + 1}"
        }
        tournamentTeamNames.value = newList
    }

    fun updateTournamentTeamName(index: Int, name: String) {
        val currentList = tournamentTeamNames.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = name
            tournamentTeamNames.value = currentList
        }
    }

    fun createMatch(onSuccess: (matchId: Long) -> Unit) {
        viewModelScope.launch {
            val overs = numberOfOvers.value.toIntOrNull()?.coerceAtLeast(1) ?: 20
            val t1Name = team1Name.value.ifBlank { "Team 1" }
            val t2Name = team2Name.value.ifBlank { "Team 2" }

            val matchId = matchRepository.createMatch(
                matchName = matchName.value.ifBlank { null },
                team1Name = t1Name,
                team2Name = t2Name,
                numberOfOvers = overs,
                venue = venue.value.ifBlank { null },
                date = matchDate.value.ifBlank { "2026-09-30" }
            )

            val createdMatch = matchRepository.getMatchById(matchId)
            if (createdMatch != null) {
                val t1List = team1Players.value.mapIndexed { idx, name ->
                    name.ifBlank { "Player ${idx + 1}" }
                }
                val t2List = team2Players.value.mapIndexed { idx, name ->
                    name.ifBlank { "Player ${idx + 1}" }
                }
                matchRepository.setupMatchPlayers(
                    matchId = matchId,
                    team1Id = createdMatch.team1Id,
                    team1Players = t1List,
                    team2Id = createdMatch.team2Id,
                    team2Players = t2List
                )
            }

            onSuccess(matchId)
        }
    }

    fun createTriangularSeries(onSuccess: (tournamentId: Long) -> Unit) {
        viewModelScope.launch {
            val overs = numberOfOvers.value.toIntOrNull()?.coerceAtLeast(1) ?: 20
            val teamNamesList = listOf(
                triangularTeam1Name.value.ifBlank { "Team 1" },
                triangularTeam2Name.value.ifBlank { "Team 2" },
                triangularTeam3Name.value.ifBlank { "Team 3" }
            )

            val tournamentId = tournamentRepository.createTournament(
                name = triangularName.value.ifBlank { "Triangular Series" },
                numberOfTeams = 3,
                numberOfOvers = overs,
                venue = venue.value.ifBlank { null },
                matchDate = matchDate.value.ifBlank { "2026-09-30" },
                teamNames = teamNamesList
            )
            onSuccess(tournamentId)
        }
    }

    fun createTournament(onSuccess: (tournamentId: Long) -> Unit) {
        viewModelScope.launch {
            val teamsCount = numberOfTeams.value.toIntOrNull()?.coerceAtLeast(2) ?: 4
            val overs = numberOfOvers.value.toIntOrNull()?.coerceAtLeast(1) ?: 20

            val tournamentId = tournamentRepository.createTournament(
                name = tournamentName.value.ifBlank { "Cricket Tournament" },
                numberOfTeams = teamsCount,
                numberOfOvers = overs,
                venue = venue.value.ifBlank { null },
                matchDate = matchDate.value.ifBlank { "2026-09-30" },
                teamNames = tournamentTeamNames.value
            )
            onSuccess(tournamentId)
        }
    }
}
