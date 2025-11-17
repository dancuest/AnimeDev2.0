package com.example.animedev20.ui.theme.domain.model

/** Representa un episodio individual dentro de un anime. */
data class Episode(
                   val number: Int,
                   val title: String,
                   val durationMinutes: Int,
                   val synopsis: String
)

/**
 * Modelo compuesto con toda la información necesaria para mostrar la ficha del anime
 * junto a metadatos ampliados y la lista de episodios.
 */
data class AnimeDetail(
                       val anime: Anime,
                       val culturalNotes: List<String> = emptyList(),
                       val episodes: List<Episode> = emptyList()
)