package com.example.animedev20.ui.theme.feature.animeinfo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import com.example.animedev20.ui.theme.domain.usecase.GetAnimeDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val animeId: Long,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)

    val uiState: StateFlow<AnimeDetailUiState> =
        _uiState.asStateFlow()

    private val _hasDisliked = MutableStateFlow(false)

    val hasDisliked: StateFlow<Boolean> =
        _hasDisliked.asStateFlow()

    private var latestFavoriteState: Boolean = false

    private var hasTrackedView: Boolean = false

    init {
        refreshFavoritesSnapshot()
        observeFavoriteStatus()
        loadInteractionStatus()
        loadAnimeDetail()
    }

    /**
     * Carga el detalle del anime.
     */
    fun loadAnimeDetail() {
        viewModelScope.launch {
            _uiState.value = AnimeDetailUiState.Loading

            val result = getAnimeDetailUseCase(animeId)

            result.fold(
                onSuccess = { detail ->

                    _uiState.value =
                        AnimeDetailUiState.Success(
                            detail = detail,
                            isFavorite = latestFavoriteState
                        )

                    if (!hasTrackedView) {
                        runCatching {
                            interactionRepository.trackView(
                                detail.anime.id
                            )
                        }.onSuccess {
                            hasTrackedView = true
                        }.onFailure { error ->
                            Log.w(
                                TAG,
                                "No se pudo registrar la vista del anime " +
                                        "${detail.anime.id}",
                                error
                            )
                        }
                    }
                },

                onFailure = { throwable ->

                    _uiState.value =
                        AnimeDetailUiState.Error(
                            throwable.message
                                ?: "No pudimos cargar la información del anime"
                        )
                }
            )
        }
    }

    /**
     * Consulta en backend las interacciones persistidas del usuario
     * con este anime.
     *
     * FAVORITE y DISLIKE son estados independientes.
     */
    private fun loadInteractionStatus() {
        viewModelScope.launch {

            runCatching {
                interactionRepository.getInteractionStatus(
                    animeId
                )
            }.onSuccess { status ->

                _hasDisliked.value =
                    status.hasDisliked

                /*
                 * El estado de favorito continúa siendo gestionado
                 * por FavoritesRepository.
                 *
                 * No modificamos latestFavoriteState aquí porque
                 * FAVORITE y DISLIKE son señales independientes.
                 */

                Log.d(
                    TAG,
                    "Estado de interacción cargado: " +
                            "animeId=${status.animeId}, " +
                            "isFavorite=${status.isFavorite}, " +
                            "hasDisliked=${status.hasDisliked}"
                )
            }.onFailure { error ->

                /*
                 * Si no podemos consultar el estado, no impedimos
                 * cargar el detalle. Simplemente mantenemos el estado
                 * inicial del botón como false.
                 */
                Log.w(
                    TAG,
                    "No se pudo consultar el estado de interacción " +
                            "del anime $animeId",
                    error
                )
            }
        }
    }

    /**
     * Alterna el estado de favorito.
     *
     * IMPORTANTE:
     * esta acción no modifica hasDisliked.
     */
    fun toggleFavorite() {
        val currentState = _uiState.value

        if (currentState is AnimeDetailUiState.Success) {

            viewModelScope.launch {

                runCatching {
                    favoritesRepository.toggleFavorite(
                        currentState.detail.anime
                    )
                }.onFailure { error ->

                    Log.w(
                        TAG,
                        "No se pudo actualizar el favorito del anime " +
                                "${currentState.detail.anime.id}",
                        error
                    )
                }
            }
        }
    }

    /**
     * Registra una señal DISLIKE.
     *
     * El DISLIKE es independiente del estado de favorito.
     * Por lo tanto, marcar un anime como DISLIKE no lo elimina
     * de favoritos.
     */
    fun trackDislike() {

        if (_hasDisliked.value) {
            return
        }

        val currentState = _uiState.value

        if (currentState is AnimeDetailUiState.Success) {

            viewModelScope.launch {

                runCatching {

                    interactionRepository.trackDislike(
                        currentState.detail.anime.id
                    )

                }.onSuccess {

                    _hasDisliked.value = true

                    Log.d(
                        TAG,
                        "DISLIKE registrado para el anime " +
                                "${currentState.detail.anime.id}"
                    )

                }.onFailure { error ->

                    Log.w(
                        TAG,
                        "No se pudo registrar el DISLIKE del anime " +
                                "${currentState.detail.anime.id}",
                        error
                    )
                }
            }
        }
    }

    /**
     * Actualiza la fotografía local del estado de favoritos.
     */
    private fun refreshFavoritesSnapshot() {
        viewModelScope.launch {

            runCatching {
                favoritesRepository.refreshFavorites()
            }.onFailure { error ->

                Log.w(
                    TAG,
                    "No se pudieron actualizar los favoritos",
                    error
                )
            }
        }
    }

    /**
     * Observa los cambios del estado de favorito.
     *
     * Este estado es independiente de hasDisliked.
     */
    private fun observeFavoriteStatus() {
        viewModelScope.launch {

            favoritesRepository
                .isFavorite(animeId)
                .collect { isFavorite ->

                    latestFavoriteState = isFavorite

                    val currentState =
                        _uiState.value

                    if (
                        currentState
                                is AnimeDetailUiState.Success
                    ) {

                        _uiState.value =
                            currentState.copy(
                                isFavorite = isFavorite
                            )
                    }
                }
        }
    }

    companion object {

        private const val TAG =
            "AnimeDetailViewModel"

        fun provideFactory(
            animeId: Long,
            animeRepository: AnimeRepository,
            favoritesRepository: FavoritesRepository,
            interactionRepository: InteractionRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    val useCase =
                        GetAnimeDetailUseCase(
                            animeRepository
                        )

                    return AnimeDetailViewModel(
                        animeId = animeId,
                        getAnimeDetailUseCase = useCase,
                        favoritesRepository = favoritesRepository,
                        interactionRepository = interactionRepository
                    ) as T
                }
            }
    }
}