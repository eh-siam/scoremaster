package com.example.scoremaster.domain.engine

import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.domain.model.PointsTableEntry
import com.example.scoremaster.domain.model.Team

object PointsTableCalculator {

    fun calculatePointsTable(
        teams: List<Team>,
        matches: List<Match>,
        allInnings: List<Innings>
    ): List<PointsTableEntry> {
        // Distinct teams by name to ensure no duplicate team rows appear in points table
        val uniqueTeams = teams.distinctBy { it.name.trim().lowercase() }

        val entryMap = uniqueTeams.associate { team ->
            team.id to PointsTableData(
                teamId = team.id,
                teamName = team.name
            )
        }.toMutableMap()

        val completedMatches = matches.filter { it.status == MatchStatus.COMPLETED }

        completedMatches.forEach { match ->
            val team1Data = entryMap[match.team1Id] ?: entryMap.values.find { it.teamName.equals(match.team1Name, ignoreCase = true) }
            val team2Data = entryMap[match.team2Id] ?: entryMap.values.find { it.teamName.equals(match.team2Name, ignoreCase = true) }

            if (team1Data != null && team2Data != null) {
                team1Data.played++
                team2Data.played++

                val isTeam1Winner = match.winnerTeamId == match.team1Id ||
                    (match.winnerTeamName != null && match.winnerTeamName.equals(match.team1Name, ignoreCase = true))
                val isTeam2Winner = match.winnerTeamId == match.team2Id ||
                    (match.winnerTeamName != null && match.winnerTeamName.equals(match.team2Name, ignoreCase = true))

                if (isTeam1Winner) {
                    team1Data.won++
                    team1Data.points += 2
                    team2Data.lost++
                } else if (isTeam2Winner) {
                    team2Data.won++
                    team2Data.points += 2
                    team1Data.lost++
                } else {
                    // Tie or No Result
                    if (match.winningMargin?.contains("Tied", ignoreCase = true) == true) {
                        team1Data.tied++
                        team1Data.points += 1
                        team2Data.tied++
                        team2Data.points += 1
                    } else {
                        team1Data.noResult++
                        team1Data.points += 1
                        team2Data.noResult++
                        team2Data.points += 1
                    }
                }

                // Gather Innings data for Net Run Rate (NRR)
                val matchInnings = allInnings.filter { it.matchId == match.id }
                matchInnings.forEach { innings ->
                    val battingTeam = entryMap[innings.battingTeamId] ?: entryMap.values.find { it.teamName.equals(innings.battingTeamName, ignoreCase = true) }
                    val bowlingTeam = entryMap[innings.bowlingTeamId] ?: entryMap.values.find { it.teamName.equals(innings.bowlingTeamName, ignoreCase = true) }

                    if (battingTeam != null && bowlingTeam != null) {
                        battingTeam.runsScored += innings.totalRuns
                        battingTeam.ballsFaced += innings.legalBallsBowled

                        bowlingTeam.runsConceded += innings.totalRuns
                        bowlingTeam.ballsBowled += innings.legalBallsBowled
                    }
                }
            }
        }

        return entryMap.values.map { data ->
            val oversFaced = (data.ballsFaced / 6) + (data.ballsFaced % 6) / 6.0f
            val oversBowled = (data.ballsBowled / 6) + (data.ballsBowled % 6) / 6.0f

            val runRateFor = if (oversFaced > 0f) data.runsScored / oversFaced else 0f
            val runRateAgainst = if (oversBowled > 0f) data.runsConceded / oversBowled else 0f
            val nrr = runRateFor - runRateAgainst

            PointsTableEntry(
                teamId = data.teamId,
                teamName = data.teamName,
                played = data.played,
                won = data.won,
                lost = data.lost,
                tied = data.tied,
                noResult = data.noResult,
                points = data.points,
                netRunRate = nrr
            )
        }.sortedWith(
            compareByDescending<PointsTableEntry> { it.points }
                .thenByDescending { it.netRunRate }
                .thenByDescending { it.won }
        )
    }

    private data class PointsTableData(
        val teamId: Long,
        val teamName: String,
        var played: Int = 0,
        var won: Int = 0,
        var lost: Int = 0,
        var tied: Int = 0,
        var noResult: Int = 0,
        var points: Int = 0,
        var runsScored: Int = 0,
        var ballsFaced: Int = 0,
        var runsConceded: Int = 0,
        var ballsBowled: Int = 0
    )
}
