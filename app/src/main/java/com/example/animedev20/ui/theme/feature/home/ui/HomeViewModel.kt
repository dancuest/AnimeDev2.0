package com.example.animedev20.ui.theme.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import com.example.animedev20.ui.theme.domain.usecase.GetHomeContentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val animeRepository: AnimeRepository,
    private val homeRefreshBus: HomeRefreshBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        observeRefreshSignals()
        loadHomeContent()
    }

    fun loadHomeContent() {
        val previousSelectedGenreId = (_uiState.value as? HomeUiState.Success)?.selectedGenreId

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            val result = getHomeContentUseCase()

            result.fold(
                onSuccess = { homeContent ->
                    _uiState.value = buildSuccessState(
                        homeContent = homeContent,
                        fallbackSelectedGenreId = previousSelectedGenreId
                    )
                },
                onFailure = { throwable ->
                    if (_uiState.value is HomeUiState.Success) {
                        scheduleRetry()
                    } else {
                        _uiState.value = HomeUiState.Error(
                            throwable.message ?: "Ha ocurrido un error inesperado"
                        )
                    }
                }
            )
        }
    }

    fun toggleGenreFilter(genreId: String) {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        val updatedGenreId = if (currentState.selectedGenreId == genreId) {
            null
        } else {
            genreId
        }

        _uiState.value = currentState.copy(selectedGenreId = updatedGenreId)
    }

    fun onSearchQueryChange(query: String) {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        val normalizedQuery = query.trim()

        searchJob?.cancel()

        if (normalizedQuery.length < 2) {
            _uiState.value = currentState.copy(
                searchQuery = query,
                searchResults = emptyList(),
                isSearching = false,
                searchErrorMessage = null
            )
            return
        }

        _uiState.value = currentState.copy(
            searchQuery = query,
            searchResults = emptyList(),
            isSearching = true,
            searchErrorMessage = null
        )

        searchJob = viewModelScope.launch {
            delay(350L)

            runCatching {
                animeRepository.searchAnime(normalizedQuery)
            }.fold(
                onSuccess = { results ->
                    val latestState = _uiState.value as? HomeUiState.Success ?: return@launch

                    if (latestState.searchQuery.trim() == normalizedQuery) {
                        _uiState.value = latestState.copy(
                            searchResults = results,
                            isSearching = false,
                            searchErrorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    val latestState = _uiState.value as? HomeUiState.Success ?: return@launch

                    if (latestState.searchQuery.trim() == normalizedQuery) {
                        _uiState.value = latestState.copy(
                            searchResults = emptyList(),
                            isSearching = false,
                            searchErrorMessage = throwable.message
                                ?: "No pudimos buscar animes ahora. Inténtalo nuevamente."
                        )
                    }
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()

        val currentState = _uiState.value as? HomeUiState.Success ?: return

        _uiState.value = currentState.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            searchErrorMessage = null
        )
    }

    private fun scheduleRetry() {
        val previousState = _uiState.value as? HomeUiState.Success
        val previousSelectedGenreId = previousState?.selectedGenreId

        viewModelScope.launch {
            delay(4_000L)
            val result = getHomeContentUseCase()

            result.onSuccess { homeContent ->
                _uiState.value = buildSuccessState(
                    homeContent = homeContent,
                    fallbackSelectedGenreId = previousSelectedGenreId
                )
            }
            // Si falla de nuevo, se mantiene el contenido anterior visible.
        }
    }

    private fun buildSuccessState(
        homeContent: HomeContent,
        fallbackSelectedGenreId: String? = null
    ): HomeUiState.Success {
        val currentState = _uiState.value as? HomeUiState.Success

        return HomeUiState.Success(
            homeContent = homeContent,
            selectedGenreId = currentState?.selectedGenreId ?: fallbackSelectedGenreId,
            searchQuery = currentState?.searchQuery.orEmpty(),
            searchResults = currentState?.searchResults.orEmpty(),
            isSearching = currentState?.isSearching ?: false,
            searchErrorMessage = currentState?.searchErrorMessage
        )
    }

    private fun observeRefreshSignals() {
        viewModelScope.launch {
            homeRefreshBus.events.collect {
                loadHomeContent()
            }
        }
    }

    companion object {
        fun provideFactory(
            animeRepository: AnimeRepository,
            userRepository: UserRepository,
            homeRefreshBus: HomeRefreshBus
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    val useCase = GetHomeContentUseCase(animeRepository, userRepository)
                    return HomeViewModel(useCase, animeRepository, homeRefreshBus) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}