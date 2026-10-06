package com.example.scoremaster.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.scoremaster.data.database.dao.BallEventDao
import com.example.scoremaster.data.database.dao.DlsDao
import com.example.scoremaster.data.database.dao.MatchDao
import com.example.scoremaster.data.database.dao.PlayerDao
import com.example.scoremaster.data.database.dao.TournamentDao
import com.example.scoremaster.data.database.entity.BallEventEntity
import com.example.scoremaster.data.database.entity.DlsInterruptionEntity
import com.example.scoremaster.data.database.entity.InningsEntity
import com.example.scoremaster.data.database.entity.MatchEntity
import com.example.scoremaster.data.database.entity.PlayerEntity
import com.example.scoremaster.data.database.entity.TeamEntity
import com.example.scoremaster.data.database.entity.TournamentEntity

@Database(
    entities = [
        TournamentEntity::class,
        TeamEntity::class,
        MatchEntity::class,
        PlayerEntity::class,
        InningsEntity::class,
        BallEventEntity::class,
        DlsInterruptionEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class ScoreMasterDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun playerDao(): PlayerDao
    abstract fun ballEventDao(): BallEventDao
    abstract fun dlsDao(): DlsDao

    companion object {
        @Volatile
        private var INSTANCE: ScoreMasterDatabase? = null

        fun getDatabase(context: Context): ScoreMasterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoreMasterDatabase::class.java,
                    "score_master_db",
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
