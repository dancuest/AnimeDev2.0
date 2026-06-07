package com.example.animedev20.ui.theme.domain.model

data class Trailer(
    val number: Int,
    val title: String,
    val durationMinutes: Int,
    val description: String,
    val youtubeUrl: String
)

data class RelatedAnime(
    val id: Long,
    val title: String,
    val relationType: String,
    val relationLabel: String,
    val url: String = "",
    val sourceType: String = "anime"
)

data class AnimeDetail(
    val anime: Anime,
    val culturalNotes: List<String> = emptyList(),
    val trailers: List<Trailer> = emptyList(),
    val relatedAnime: List<RelatedAnime> = emptyList()
)