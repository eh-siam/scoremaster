package com.example.scoremaster.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scoremaster.data.repository.MatchRepository
import com.example.scoremaster.domain.model.Innings
import com.example.scoremaster.domain.model.Match
import com.example.scoremaster.domain.model.MatchStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class MatchFilterTab(val displayName: String) {
    ALL("All"),
    LIVE("Live"),
    COMPLETED("Completed"),
    UPCOMING("Upcoming")
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val matchRepository: MatchRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(MatchFilterTab.ALL)

    val allMatches: StateFlow<List<Match>> = matchRepository.getAllMatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val liveMatch: StateFlow<Match?> = allMatches
        .map { list -> list.find { it.status == MatchStatus.IN_PROGRESS } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val liveInnings: StateFlow<Innings?> = liveMatch
        .flatMapLatest { match ->
            if (match != null) {
                matchRepository.getInningsForMatch(match.id).map { list ->
                    list.find { it.inningsNumber == match.currentInningsIndex } ?: list.firstOrNull()
                }
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val matchInningsMap: StateFlow<Map<Long, List<Innings>>> = allMatches
        .flatMapLatest { matches ->
            if (matches.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(matches.map { match ->
                    matchRepository.getInningsForMatch(match.id).map { inningsList ->
                        match.id to inningsList
                    }
                }) { pairs ->
                    pairs.toMap()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val filteredMatches: StateFlow<List<Match>> = combine(
        allMatches,
        searchQuery,
        selectedFilter
    ) { matches, query, filter ->
        matches.filter { match ->
            val matchesFilter = when (filter) {
                MatchFilterTab.ALL -> true
                MatchFilterTab.LIVE -> match.status == MatchStatus.IN_PROGRESS
                MatchFilterTab.COMPLETED -> match.status == MatchStatus.COMPLETED
                MatchFilterTab.UPCOMING -> match.status == MatchStatus.NOT_STARTED
            }

            val matchesSearch = query.isBlank() ||
                match.team1Name.contains(query, ignoreCase = true) ||
                match.team2Name.contains(query, ignoreCase = true) ||
                (match.matchName?.contains(query, ignoreCase = true) == true) ||
                (match.venue?.contains(query, ignoreCase = true) == true)

            matchesFilter && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onFilterSelected(filter: MatchFilterTab) {
        selectedFilter.value = filter
    }
}
