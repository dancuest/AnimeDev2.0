package com.example.animedev.domain.model

data class HomeContent(
    val heroAnime: Anime,
    val sections: List<AnimeSection>
)

data class AnimeSection(
    val genre: Genre,
    val animes: List<Anime>
)
