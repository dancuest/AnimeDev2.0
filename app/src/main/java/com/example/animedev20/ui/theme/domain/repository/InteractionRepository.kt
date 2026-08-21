package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.data.remote.InteractionStatusResponse

interface InteractionRepository {

    suspend fun trackView(animeId: Long)

    suspend fun trackFavorite(animeId: Long)

    suspend fun trackUnfavorite(animeId: Long)

    suspend fun trackDislike(animeId: Long)

    suspend fun trackTriviaScore(
        animeId: Long,
        score: Int,
        totalQuestions: Int
    )

    suspend fun getInteractionStatus(
        animeId: Long
    ): InteractionStatusResponse
}