package com.example.animedev.data.repository

import com.example.animedev.data.FakeDataSource
import com.example.animedev.domain.model.Genre
import com.example.animedev.domain.repository.UserRepository
import kotlinx.coroutines.delay

class FakeUserRepositoryImpl : UserRepository {

    override suspend fun getPreferredGenres(): List<Genre> {
        delay(500)
        return FakeDataSource.preferredGenres
    }
}
