package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.InteractionStatusResponse
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository

object NoOpInteractionRepositoryImpl : InteractionRepository {

    override suspend fun trackView(animeId: Long) = Unit

    override suspend fun trackFavorite(animeId: Long) = Unit

    override suspend fun trackUnfavorite(animeId: Long) = Unit

    override suspend fun trackDislike(animeId: Long) = Unit

    override suspend fun trackTriviaScore(
        animeId: Long,
        score: Int,
        totalQuestions: Int
    ) = Unit

    override suspend fun getInteractionStatus(
        animeId: Long
    ): InteractionStatusResponse {
        return InteractionStatusResponse(
            animeId = animeId,
            isFavorite = false,
            hasDisliked = false
        )
    }
}