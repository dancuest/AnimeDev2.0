package com.example.animedev.domain.model.Trivias

import com.example.animedev.domain.model.Anime

data class TriviaSummary(
    val anime: Anime,
    val lastScore: Int?,
    val totalQuestions: Int,
    val lastDifficulty: TriviaDifficulty?,
    val bestScore: Int
)