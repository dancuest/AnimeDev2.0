package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Genre

interface UserRepository {
    suspend fun getPreferredGenres(): List<Genre>
}