package com.example.animedev.domain.model.Trivias

import com.example.animedev.domain.model.Trivias.TriviaDifficulty

data class TriviaQuestion(
    val id: String,
    val animeId: Long,
    val difficulty: TriviaDifficulty,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val feedback: String
)