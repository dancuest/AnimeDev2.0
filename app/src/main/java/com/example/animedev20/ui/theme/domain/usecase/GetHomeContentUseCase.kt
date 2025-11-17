package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetHomeContentUseCase(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HomeContent> = runCatching {
        coroutineScope {
            // 1. Obtener la recomendación principal (hero)
            val heroAnimeDeferred = async { animeRepository.getHeroRecommendation() }

            // 2. Obtener los géneros preferidos del usuario
            val preferredGenres = userRepository.getPreferredGenres()

            // 3. Para cada género, obtener la lista de animes en paralelo
            val sectionsDeferred = preferredGenres.map { genre ->
                async {
                    val animes = animeRepository.getAnimesByGenre(genre.id)
                    AnimeSection(genre = genre, animes = animes)
                }
            }

            // 4. Esperar a que todas las llamadas finalicen y construir el resultado
            HomeContent(
                heroAnime = heroAnimeDeferred.await(),
                sections = sectionsDeferred.map { it.await() }
            )
        }
    }
}