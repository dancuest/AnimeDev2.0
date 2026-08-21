package com.example.animedev20.ui.theme.feature.home.ui

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.HomeContent

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val homeContent: HomeContent,
        val selectedGenreId: String? = null,
        val searchQuery: String = "",
        val searchResults: List<Anime> = emptyList(),
        val isSearching: Boolean = false,
        val searchErrorMessage: String? = null
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}