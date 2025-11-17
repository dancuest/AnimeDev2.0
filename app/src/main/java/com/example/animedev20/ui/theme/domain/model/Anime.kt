package com.example.animedev20.ui.theme.domain.model

// Representa el estado de emisión de un anime.
enum class EmissionStatus {
    ON_AIR, FINISHED, ON_BREAK
}

// Representa la duración típica de los episodios de un anime.
enum class DurationType {
    SHORT, MEDIUM, LONG
}

/**
 * Modelo de dominio principal para un Anime.
 * Refleja la estructura de la tabla `animes` de la base de datos.
 */
data class Anime(
                 val id: Long,
                 val externalApiId: String, // Mapea a id_externo_api
                 val title: String,         // Mapea a titulo
                 val originalTitle: String?,
                 val synopsis: String,      // Mapea a sinopsis_es
                 val coverImageUrl: String, // Mapea a imagen_portada
                 val totalEpisodes: Int?,
                 val durationType: DurationType,
                 val emissionStatus: EmissionStatus,
                 val releaseYear: Int?,
                 val genres: List<Genre> = emptyList() // Se poblará a través de la tabla de unión
)