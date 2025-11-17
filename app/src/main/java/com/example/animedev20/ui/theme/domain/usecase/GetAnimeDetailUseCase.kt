package com.example.animedev.domain.usecase

import com.example.animedev.domain.model.AnimeDetail
import com.example.animedev.domain.repository.AnimeRepository

class GetAnimeDetailUseCase(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(animeId: Long): Result<AnimeDetail> =
        runCatching { animeRepository.getAnimeDetail(animeId) }
}