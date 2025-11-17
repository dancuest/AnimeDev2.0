package com.example.animedev.domain.repository

import com.example.animedev.domain.model.Anime
import com.example.animedev.domain.model.AnimeDetail

interface AnimeRepository {
    suspend fun getHeroRecommendation(): Anime
    suspend fun getAnimesByGenre(genreId: String): List<Anime>
    suspend fun getAnimeDetail(animeId: Long): AnimeDetail
}
