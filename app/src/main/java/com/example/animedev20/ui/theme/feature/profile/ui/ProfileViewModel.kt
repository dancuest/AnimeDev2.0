package com.example.animedev20.ui.theme.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
                       private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { userRepository.getUserProfile() }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            recentAnimes = FakeDataSource.recentAnimeHistory,
                            triviaStats = FakeDataSource.defaultTriviaStats,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No fue posible cargar el perfil"
                        )
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(FakeUserRepositoryImpl()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}