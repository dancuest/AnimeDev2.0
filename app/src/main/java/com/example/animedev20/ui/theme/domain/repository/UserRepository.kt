package com.example.animedev.domain.repository

import com.example.animedev.domain.model.Genre

interface UserRepository {
    suspend fun getPreferredGenres(): List<Genre>
}
