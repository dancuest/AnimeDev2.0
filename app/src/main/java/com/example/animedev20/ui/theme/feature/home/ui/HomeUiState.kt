package com.example.animedev.feature.home.ui

import com.example.animedev.domain.model.HomeContent

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val homeContent: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
