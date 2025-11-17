package com.example.animedev.feature.animeinfo.ui

import com.example.animedev.domain.model.AnimeDetail

sealed class AnimeDetailUiState {
    object Loading : AnimeDetailUiState()
    data class Success(val detail: AnimeDetail, val isFavorite: Boolean) : AnimeDetailUiState()
    data class Error(val message: String) : AnimeDetailUiState()
}