package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.delay

class FakeUserRepositoryImpl : UserRepository {

    override suspend fun getPreferredGenres(): List<Genre> {
        delay(500)
        return FakeDataSource.preferredGenres
    }
}
