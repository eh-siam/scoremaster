package com.example.scoremaster

import android.app.Application
import com.example.scoremaster.data.database.ScoreMasterDatabase
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.data.repository.MatchRepositoryImpl
import com.example.scoremaster.data.repository.TournamentRepository
import com.example.scoremaster.data.repository.TournamentRepositoryImpl

class ScoreMasterApplication : Application() {

    lateinit var database: ScoreMasterDatabase
        private set

    lateinit var matchRepository: MatchRepository
        private set

    lateinit var tournamentRepository: TournamentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = ScoreMasterDatabase.getDatabase(this)
        matchRepository = MatchRepositoryImpl(
            database.matchDao(),
            database.playerDao(),
            database.ballEventDao(),
            database.dlsDao(),
            database.tournamentDao()
        )
        tournamentRepository = TournamentRepositoryImpl(database.tournamentDao(), database.matchDao())
    }
}
