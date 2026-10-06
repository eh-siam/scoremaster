package com.example.scoremaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scoremaster.data.database.ScoreMasterDatabase
import com.example.scoremaster.data.repository.MatchRepositoryImpl
import com.example.scoremaster.data.repository.TournamentRepositoryImpl
import com.example.scoremaster.navigation.ScoreMasterBottomBar
import com.example.scoremaster.navigation.ScoreMasterNavHost
import com.example.scoremaster.navigation.Screen
import com.example.scoremaster.ui.theme.ScoreMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ScoreMasterDatabase.getDatabase(applicationContext)
        val matchRepository = MatchRepositoryImpl(
            matchDao = database.matchDao(),
            playerDao = database.playerDao(),
            ballEventDao = database.ballEventDao(),
            dlsDao = database.dlsDao(),
            tournamentDao = database.tournamentDao()
        )
        val tournamentRepository = TournamentRepositoryImpl(
            tournamentDao = database.tournamentDao(),
            matchDao = database.matchDao()
        )

        setContent {
            ScoreMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScoreMasterApp(
                        matchRepository = matchRepository,
                        tournamentRepository = tournamentRepository
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreMasterApp(
    matchRepository: MatchRepositoryImpl,
    tournamentRepository: TournamentRepositoryImpl
) {
    val navController = rememberNavController()

    ScoreMasterNavHost(
        navController = navController,
        matchRepository = matchRepository,
        tournamentRepository = tournamentRepository,
        modifier = Modifier.fillMaxSize()
    )
}
