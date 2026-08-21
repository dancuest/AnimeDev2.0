package com.example.animedev20.ui.theme.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class InteractionRequest(
    val type: String,
    val animeId: Long,
    val payload: Map<String, Any>? = null
)

data class InteractionStatusResponse(
    val animeId: Long,
    val isFavorite: Boolean,
    val hasDisliked: Boolean
)

interface InteractionsApi {

    @POST("interactions")
    suspend fun postInteraction(
        @Body request: InteractionRequest
    )

    @GET("interactions/status/{animeId}")
    suspend fun getInteractionStatus(
        @Path("animeId") animeId: Long
    ): InteractionStatusResponse
}