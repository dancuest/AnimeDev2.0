package com.example.animedev20.ui.theme.feature.profile.ui

import com.example.animedev20.ui.theme.domain.model.UserProfile

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: UserProfile?, val isEditing: Boolean = false) : ProfileUiState()
}
