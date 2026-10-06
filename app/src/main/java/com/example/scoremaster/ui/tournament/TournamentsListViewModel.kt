package com.example.scoremaster.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.TournamentRepository
import com.example.scoremaster.domain.model.PointsTableEntry
import com.example.scoremaster.domain.model.Tournament
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TournamentsListUiState(
    val tournaments: List<Tournament> = emptyList(),
    val tournamentPointsTableMap: Map<Long, List<PointsTableEntry>> = emptyMap(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentsListViewModel(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val tournamentsFlow = tournamentRepository.getAllTournaments()

    val uiState: StateFlow<TournamentsListUiState> = tournamentsFlow

        .flatMapLatest { tournaments ->
            if (tournaments.isEmpty()) {
                flowOf(TournamentsListUiState(isLoading = false))
            } else {
                val tableFlows = tournaments.map { tournament ->
                    tournamentRepository.getPointsTableForTournament(tournament.id).map { table ->
                        tournament.id to table
                    }
                }

                combine(tableFlows) { pairs ->
                    val map = pairs.toMap()
                    TournamentsListUiState(
                        tournaments = tournaments,
                        tournamentPointsTableMap = map,
                        isLoading = false
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TournamentsListUiState()
        )
}
