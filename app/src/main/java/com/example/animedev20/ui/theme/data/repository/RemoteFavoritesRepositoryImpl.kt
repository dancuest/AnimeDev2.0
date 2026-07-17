package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RemoteFavoritesRepositoryImpl(
    private val animeApi: AnimeApi,
    scope: CoroutineScope
) : FavoritesRepository {

    companion object {
        private const val TAG = "RemoteFavoritesRepo"


        private const val FAVORITE_REQUEST_DELAY_MS = 450L
    }

    private val favoriteAnimes = MutableStateFlow<List<Anime>>(emptyList())
    private val refreshMutex = Mutex()

    override val favorites: Flow<List<Anime>> = favoriteAnimes.asStateFlow()

    init {
        scope.launch {
            refreshFavorites()
        }
    }

    override suspend fun refreshFavorites() {
        refreshMutex.withLock {
            runCatching {
                val favoriteIds = animeApi
                    .getMyFavoriteIds()
                    .data
                    .distinct()

                val restoredFavorites = mutableListOf<Anime>()


                favoriteIds.forEachIndexed { index, animeId ->
                    val anime = runCatching {
                        animeApi.getById(
                            id = animeId,
                            translate = false
                        ).data
                    }.onFailure { error ->
                        Log.w(
                            TAG,
                            "No se pudo restaurar el favorito id=$animeId",
                            error
                        )
                    }.getOrNull()

                    if (anime != null) {
                        restoredFavorites += anime
                    }

                    if (index < favoriteIds.lastIndex) {
                        delay(FAVORITE_REQUEST_DELAY_MS)
                    }
                }

                favoriteAnimes.value = restoredFavorites
            }.onFailure { error ->
                Log.w(
                    TAG,
                    "No se pudieron restaurar los favoritos remotos",
                    error
                )

            }
        }
    }

    override suspend fun addFavorite(anime: Anime) {
        val current = favoriteAnimes.value

        if (current.any { it.id == anime.id }) {
            return
        }

        favoriteAnimes.value = current + anime
    }

    override suspend fun removeFavorite(animeId: Long) {
        val current = favoriteAnimes.value
        favoriteAnimes.value = current.filterNot { it.id == animeId }
    }

    override suspend fun toggleFavorite(anime: Anime) {
        if (isFavorite(anime.id).first()) {
            removeFavorite(anime.id)
        } else {
            addFavorite(anime)
        }
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> = favoriteAnimes
        .map { list -> list.any { it.id == animeId } }
        .distinctUntilChanged()
}