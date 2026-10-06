package com.example.scoremaster.data.repository

import com.example.scoremaster.data.database.dao.BallEventDao
import com.example.scoremaster.data.database.dao.DlsDao
import com.example.scoremaster.data.database.dao.MatchDao
import com.example.scoremaster.data.database.dao.PlayerDao
import com.example.scoremaster.data.database.entity.BallEventEntity
import com.example.scoremaster.data.database.entity.DlsInterruptionEntity
import com.example.scoremaster.data.database.entity.InningsEntity
import com.example.scoremaster.data.database.entity.MatchEntity
import com.example.scoremaster.data.database.entity.PlayerEntity
import com.example.scoremaster.data.database.entity.TeamEntity
import com.example.scoremaster.domain.dls.DlsInterruption
import com.example.scoremaster.domain.dls.MatchFormat
import com.example.scoremaster.domain.engine.CricketScoringEngine
import com.example.scoremaster.domain.model.BallEvent
import com.example.scoremaster.domain.model.ExtraType
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.data.database.dao.TournamentDao
import com.example.scoremaster.domain.model.MatchStatus
import com.example.scoremaster.domain.model.Player
import com.example.scoremaster.domain.model.WicketType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MatchRepositoryImpl(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    private val ballEventDao: BallEventDao,
    private val dlsDao: DlsDao,
    private val tournamentDao: TournamentDao? = null
) : MatchRepository {

    override fun getAllMatches(): Flow<List<Match>> {
        return matchDao.getAllMatches().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMatchByIdFlow(matchId: Long): Flow<Match?> {
        return matchDao.getMatchByIdFlow(matchId).map { it?.toDomain() }
    }

    override suspend fun getMatchById(matchId: Long): Match? {
        return matchDao.getMatchById(matchId)?.toDomain()
    }

    override suspend fun createMatch(
        matchName: String?,
        team1Name: String,
        team2Name: String,
        numberOfOvers: Int,
        venue: String?,
        date: String,
        tournamentId: Long?
    ): Long {
        val team1Id = if (tournamentId != null && tournamentDao != null) {
            val existing = tournamentDao.getTeamsListForTournament(tournamentId).find { it.name.equals(team1Name, ignoreCase = true) }
            existing?.id ?: matchDao.insertTeam(TeamEntity(tournamentId = tournamentId, name = team1Name))
        } else {
            matchDao.insertTeam(TeamEntity(tournamentId = tournamentId, name = team1Name))
        }

        val team2Id = if (tournamentId != null && tournamentDao != null) {
            val existing = tournamentDao.getTeamsListForTournament(tournamentId).find { it.name.equals(team2Name, ignoreCase = true) }
            existing?.id ?: matchDao.insertTeam(TeamEntity(tournamentId = tournamentId, name = team2Name))
        } else {
            matchDao.insertTeam(TeamEntity(tournamentId = tournamentId, name = team2Name))
        }

        val matchEntity = MatchEntity(
            tournamentId = tournamentId,
            matchName = matchName,
            team1Id = team1Id,
            team2Id = team2Id,
            team1Name = team1Name,
            team2Name = team2Name,
            numberOfOvers = numberOfOvers,
            venue = venue,
            date = date,
            currentInningsIndex = 1,
            status = MatchStatus.NOT_STARTED.name
        )
        return matchDao.insertMatch(matchEntity)
    }

    override suspend fun setupMatchPlayers(
        matchId: Long,
        team1Id: Long,
        team1Players: List<String>,
        team2Id: Long,
        team2Players: List<String>
    ) {
        playerDao.deletePlayersForMatch(matchId)
        val team1Entities = team1Players.map { PlayerEntity(matchId = matchId, teamId = team1Id, name = it) }
        val team2Entities = team2Players.map { PlayerEntity(matchId = matchId, teamId = team2Id, name = it) }

        playerDao.insertPlayers(team1Entities)
        playerDao.insertPlayers(team2Entities)
    }

    override fun getPlayersForTeamInMatch(matchId: Long, teamId: Long): Flow<List<Player>> {
        return playerDao.getPlayersForTeamInMatch(matchId, teamId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPlayersListForTeamInMatch(matchId: Long, teamId: Long): List<Player> {
        return playerDao.getPlayersListForTeamInMatch(matchId, teamId).map { it.toDomain() }
    }

    override suspend fun startMatch(
        matchId: Long,
        battingTeamId: Long,
        bowlingTeamId: Long,
        openingStrikerId: Long,
        openingNonStrikerId: Long,
        startingBowlerId: Long
    ): Long {
        val match = matchDao.getMatchById(matchId) ?: return -1

        val battingTeamName = if (battingTeamId == match.team1Id) match.team1Name else match.team2Name
        val bowlingTeamName = if (bowlingTeamId == match.team1Id) match.team1Name else match.team2Name

        val battingPlayers = playerDao.getPlayersListForTeamInMatch(matchId, battingTeamId)
        val bowlingPlayers = playerDao.getPlayersListForTeamInMatch(matchId, bowlingTeamId)

        val strikerId = openingStrikerId.takeIf { id -> id != 0L && battingPlayers.any { it.id == id } }
            ?: battingPlayers.getOrNull(0)?.id
        val nonStrikerId = openingNonStrikerId.takeIf { id -> id != 0L && battingPlayers.any { it.id == id } }
            ?: battingPlayers.getOrNull(1)?.id
        val bowlerId = startingBowlerId.takeIf { id -> id != 0L && bowlingPlayers.any { it.id == id } }
            ?: bowlingPlayers.getOrNull(0)?.id

        val existingInnings = matchDao.getInningsByNumber(matchId, 1)

        val inningsEntity = InningsEntity(
            id = existingInnings?.id ?: 0L,
            matchId = matchId,
            battingTeamId = battingTeamId,
            battingTeamName = battingTeamName,
            bowlingTeamId = bowlingTeamId,
            bowlingTeamName = bowlingTeamName,
            inningsNumber = 1,
            totalRuns = existingInnings?.totalRuns ?: 0,
            totalWickets = existingInnings?.totalWickets ?: 0,
            legalBallsBowled = existingInnings?.legalBallsBowled ?: 0,
            isCompleted = false,
            currentStrikerId = strikerId,
            currentNonStrikerId = nonStrikerId,
            currentBowlerId = bowlerId
        )
        val inningsId = if (existingInnings != null) {
            matchDao.updateInnings(inningsEntity)
            existingInnings.id
        } else {
            matchDao.insertInnings(inningsEntity)
        }

        matchDao.updateMatch(
            match.copy(
                status = MatchStatus.IN_PROGRESS.name,
                currentInningsIndex = 1
            )
        )
        return inningsId
    }

    override fun getInningsForMatch(matchId: Long): Flow<List<Innings>> {
        return matchDao.getInningsForMatch(matchId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getInningsByNumber(matchId: Long, inningsNumber: Int): Innings? {
        return matchDao.getInningsByNumber(matchId, inningsNumber)?.toDomain()
    }

    override suspend fun updateInnings(innings: Innings) {
        matchDao.updateInnings(innings.toEntity())
    }

    override suspend fun recordBallEvent(ballEvent: BallEvent) {
        ballEventDao.insertBallEvent(ballEvent.toEntity())
        recalculateAndSaveInnings(ballEvent.inningsId)
    }

    override suspend fun undoLastBall(inningsId: Long) {
        val lastBall = ballEventDao.getLastBallEvent(inningsId)
        if (lastBall != null) {
            ballEventDao.deleteBallEvent(lastBall.id)
            recalculateAndSaveInnings(inningsId)
        }
    }

    override fun getBallEventsForInnings(inningsId: Long): Flow<List<BallEvent>> {
        return ballEventDao.getBallEventsForInnings(inningsId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun startSecondInnings(
        matchId: Long,
        openingStrikerId: Long,
        openingNonStrikerId: Long,
        startingBowlerId: Long
    ): Long {
        val match = matchDao.getMatchById(matchId) ?: return -1
        val firstInnings = matchDao.getInningsByNumber(matchId, 1) ?: return -1

        // Mark 1st innings complete
        matchDao.updateInnings(firstInnings.copy(isCompleted = true))

        val target = match.dlsRevisedTarget ?: (firstInnings.totalRuns + 1)

        val secondBattingTeamId = firstInnings.bowlingTeamId
        val secondBattingTeamName = firstInnings.bowlingTeamName
        val secondBowlingTeamId = firstInnings.battingTeamId
        val secondBowlingTeamName = firstInnings.battingTeamName

        val battingPlayers = playerDao.getPlayersListForTeamInMatch(matchId, secondBattingTeamId)
        val bowlingPlayers = playerDao.getPlayersListForTeamInMatch(matchId, secondBowlingTeamId)

        val strikerId = openingStrikerId.takeIf { id -> id != 0L && battingPlayers.any { it.id == id } }
            ?: battingPlayers.getOrNull(0)?.id
        val nonStrikerId = openingNonStrikerId.takeIf { id -> id != 0L && battingPlayers.any { it.id == id } }
            ?: battingPlayers.getOrNull(1)?.id
        val bowlerId = startingBowlerId.takeIf { id -> id != 0L && bowlingPlayers.any { it.id == id } }
            ?: bowlingPlayers.getOrNull(0)?.id

        val existingSecondInnings = matchDao.getInningsByNumber(matchId, 2)

        val secondInningsEntity = InningsEntity(
            id = existingSecondInnings?.id ?: 0L,
            matchId = matchId,
            battingTeamId = secondBattingTeamId,
            battingTeamName = secondBattingTeamName,
            bowlingTeamId = secondBowlingTeamId,
            bowlingTeamName = secondBowlingTeamName,
            inningsNumber = 2,
            totalRuns = existingSecondInnings?.totalRuns ?: 0,
            totalWickets = existingSecondInnings?.totalWickets ?: 0,
            legalBallsBowled = existingSecondInnings?.legalBallsBowled ?: 0,
            targetRuns = target,
            isCompleted = false,
            currentStrikerId = strikerId,
            currentNonStrikerId = nonStrikerId,
            currentBowlerId = bowlerId
        )

        val inningsId = if (existingSecondInnings != null) {
            matchDao.updateInnings(secondInningsEntity)
            existingSecondInnings.id
        } else {
            matchDao.insertInnings(secondInningsEntity)
        }
        matchDao.updateMatch(match.copy(currentInningsIndex = 2))
        return inningsId
    }

    override suspend fun completeMatch(
        matchId: Long,
        winnerTeamId: Long?,
        winnerTeamName: String?,
        winningMargin: String,
        playerOfTheMatchId: Long?,
        playerOfTheMatchName: String?
    ) {
        val match = matchDao.getMatchById(matchId) ?: return
        matchDao.updateMatch(
            match.copy(
                status = MatchStatus.COMPLETED.name,
                winnerTeamId = winnerTeamId,
                winnerTeamName = winnerTeamName,
                winningMargin = winningMargin,
                playerOfTheMatchId = playerOfTheMatchId,
                playerOfTheMatchName = playerOfTheMatchName
            )
        )
    }

    private suspend fun recalculateAndSaveInnings(inningsId: Long) {
        val inningsEntity = matchDao.getInningsById(inningsId) ?: return
        val match = matchDao.getMatchById(inningsEntity.matchId) ?: return

        val ballEventsList = ballEventDao.getBallEventsListForInnings(inningsId).map { it.toDomain() }
        val battingPlayers = playerDao.getPlayersListForTeamInMatch(match.id, inningsEntity.battingTeamId).map { it.toDomain() }
        val bowlingPlayers = playerDao.getPlayersListForTeamInMatch(match.id, inningsEntity.bowlingTeamId).map { it.toDomain() }
        val allPlayers = battingPlayers + bowlingPlayers

        val result = CricketScoringEngine.calculateInningsState(
            ballEvents = ballEventsList,
            allPlayers = allPlayers,
            initialStrikerId = inningsEntity.currentStrikerId,
            initialNonStrikerId = inningsEntity.currentNonStrikerId,
            initialBowlerId = inningsEntity.currentBowlerId
        )

        val updatedInnings = inningsEntity.copy(
            totalRuns = result.totalRuns,
            totalWickets = result.totalWickets,
            legalBallsBowled = result.legalBallsBowled,
            currentStrikerId = result.currentStrikerId ?: inningsEntity.currentStrikerId,
            currentNonStrikerId = result.currentNonStrikerId ?: inningsEntity.currentNonStrikerId,
            currentBowlerId = result.currentBowlerId ?: inningsEntity.currentBowlerId
        )

        matchDao.updateInnings(updatedInnings)
    }

    override suspend fun updateMatch(match: Match) {
        matchDao.updateMatch(match.toEntity())
    }

    override fun getDlsInterruptions(matchId: Long): Flow<List<DlsInterruption>> {
        return dlsDao.getInterruptionsForMatch(matchId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveDlsInterruption(matchId: Long, interruption: DlsInterruption) {
        dlsDao.insertInterruption(interruption.toEntity(matchId))
    }

    override suspend fun applyDlsTarget(
        matchId: Long,
        dlsRevisedOvers: Int,
        dlsRevisedTarget: Int,
        dlsParScore: Int
    ) {
        val match = matchDao.getMatchById(matchId) ?: return
        matchDao.updateMatch(
            match.copy(
                numberOfOvers = dlsRevisedOvers,
                dlsRevisedTarget = dlsRevisedTarget,
                dlsRevisedOvers = dlsRevisedOvers,
                dlsParScore = dlsParScore
            )
        )
        val secondInnings = matchDao.getInningsByNumber(matchId, 2)
        if (secondInnings != null) {
            matchDao.updateInnings(
                secondInnings.copy(
                    targetRuns = dlsRevisedTarget
                )
            )
        }
    }

    // Converters
    private fun Match.toEntity() = MatchEntity(
        id = id,
        tournamentId = tournamentId,
        matchName = matchName,
        team1Id = team1Id,
        team2Id = team2Id,
        team1Name = team1Name,
        team2Name = team2Name,
        numberOfOvers = numberOfOvers,
        originalNumberOfOvers = originalNumberOfOvers,
        matchFormatName = matchFormat.name,
        venue = venue,
        date = date,
        currentInningsIndex = currentInningsIndex,
        status = status.name,
        winnerTeamId = winnerTeamId,
        winnerTeamName = winnerTeamName,
        winningMargin = winningMargin,
        playerOfTheMatchId = playerOfTheMatchId,
        playerOfTheMatchName = playerOfTheMatchName,
        dlsRevisedTarget = dlsRevisedTarget,
        dlsRevisedOvers = dlsRevisedOvers,
        dlsParScore = dlsParScore
    )

    private fun MatchEntity.toDomain() = Match(
        id = id,
        tournamentId = tournamentId,
        matchName = matchName,
        team1Id = team1Id,
        team2Id = team2Id,
        team1Name = team1Name,
        team2Name = team2Name,
        numberOfOvers = numberOfOvers,
        originalNumberOfOvers = originalNumberOfOvers,
        matchFormat = runCatching { MatchFormat.valueOf(matchFormatName) }.getOrDefault(MatchFormat.fromOvers(numberOfOvers)),
        venue = venue,
        date = date,
        currentInningsIndex = currentInningsIndex,
        status = runCatching { MatchStatus.valueOf(status) }.getOrDefault(MatchStatus.NOT_STARTED),
        winnerTeamId = winnerTeamId,
        winnerTeamName = winnerTeamName,
        winningMargin = winningMargin,
        playerOfTheMatchId = playerOfTheMatchId,
        playerOfTheMatchName = playerOfTheMatchName,
        dlsRevisedTarget = dlsRevisedTarget,
        dlsRevisedOvers = dlsRevisedOvers,
        dlsParScore = dlsParScore
    )

    private fun DlsInterruptionEntity.toDomain() = DlsInterruption(
        interruptionNumber = interruptionNumber,
        scoreAtInterruption = scoreAtInterruption,
        wicketsAtInterruption = wicketsAtInterruption,
        oversAtInterruption = oversAtInterruption,
        scheduledOversBeforeInterruption = scheduledOversBeforeInterruption,
        revisedOversAfterInterruption = revisedOversAfterInterruption,
        resourceBeforeInterruption = resourceBeforeInterruption,
        resourceLost = resourceLost,
        resourceRemaining = resourceRemaining
    )

    private fun DlsInterruption.toEntity(matchId: Long) = DlsInterruptionEntity(
        matchId = matchId,
        interruptionNumber = interruptionNumber,
        scoreAtInterruption = scoreAtInterruption,
        wicketsAtInterruption = wicketsAtInterruption,
        oversAtInterruption = oversAtInterruption,
        scheduledOversBeforeInterruption = scheduledOversBeforeInterruption,
        revisedOversAfterInterruption = revisedOversAfterInterruption,
        resourceBeforeInterruption = resourceBeforeInterruption,
        resourceLost = resourceLost,
        resourceRemaining = resourceRemaining
    )

    private fun PlayerEntity.toDomain() = Player(
        id = id,
        matchId = matchId,
        teamId = teamId,
        name = name
    )

    private fun InningsEntity.toDomain() = Innings(
        id = id,
        matchId = matchId,
        battingTeamId = battingTeamId,
        battingTeamName = battingTeamName,
        bowlingTeamId = bowlingTeamId,
        bowlingTeamName = bowlingTeamName,
        inningsNumber = inningsNumber,
        totalRuns = totalRuns,
        totalWickets = totalWickets,
        legalBallsBowled = legalBallsBowled,
        targetRuns = targetRuns,
        isCompleted = isCompleted,
        currentStrikerId = currentStrikerId,
        currentNonStrikerId = currentNonStrikerId,
        currentBowlerId = currentBowlerId
    )

    private fun Innings.toEntity() = InningsEntity(
        id = id,
        matchId = matchId,
        battingTeamId = battingTeamId,
        battingTeamName = battingTeamName,
        bowlingTeamId = bowlingTeamId,
        bowlingTeamName = bowlingTeamName,
        inningsNumber = inningsNumber,
        totalRuns = totalRuns,
        totalWickets = totalWickets,
        legalBallsBowled = legalBallsBowled,
        targetRuns = targetRuns,
        isCompleted = isCompleted,
        currentStrikerId = currentStrikerId,
        currentNonStrikerId = currentNonStrikerId,
        currentBowlerId = currentBowlerId
    )

    private fun BallEventEntity.toDomain() = BallEvent(
        id = id,
        inningsId = inningsId,
        overNumber = overNumber,
        ballNumberInOver = ballNumberInOver,
        isLegalBall = isLegalBall,
        bowlerId = bowlerId,
        strikerId = strikerId,
        nonStrikerId = nonStrikerId,
        runsOffBat = runsOffBat,
        extraType = runCatching { ExtraType.valueOf(extraType) }.getOrDefault(ExtraType.NONE),
        extraRuns = extraRuns,
        isWicket = isWicket,
        wicketType = wicketType?.let { runCatching { WicketType.valueOf(it) }.getOrNull() },
        dismissedPlayerId = dismissedPlayerId,
        fielderId = fielderId,
        newBatsmanId = newBatsmanId,
        timestamp = timestamp
    )

    private fun BallEvent.toEntity() = BallEventEntity(
        id = id,
        inningsId = inningsId,
        overNumber = overNumber,
        ballNumberInOver = ballNumberInOver,
        isLegalBall = isLegalBall,
        bowlerId = bowlerId,
        strikerId = strikerId,
        nonStrikerId = nonStrikerId,
        runsOffBat = runsOffBat,
        extraType = extraType.name,
        extraRuns = extraRuns,
        isWicket = isWicket,
        wicketType = wicketType?.name,
        dismissedPlayerId = dismissedPlayerId,
        fielderId = fielderId,
        newBatsmanId = newBatsmanId,
        timestamp = timestamp
    )
}
