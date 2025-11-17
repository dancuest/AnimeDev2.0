package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.delay

object FakeUserRepositoryImpl : UserRepository {

    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private var cachedProfile: UserProfile = FakeDataSource.defaultUserProfile

    override suspend fun getPreferredGenres(): List<Genre> {
        delay(500)
        return cachedSettings.preferredGenres
    }

    override suspend fun getUserProfile(): UserProfile {
        delay(400)
        return cachedProfile
    }

    override suspend fun getUserSettings(): UserSettings {
        delay(400)
        return cachedSettings
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        delay(400)
        cachedSettings = settings
        cachedProfile = cachedProfile.copy(
            favoriteGenres = settings.preferredGenres,
            preferredDuration = settings.preferredDuration
        )
        return cachedSettings
    }

    override suspend fun updateAccountInfo(
        name: String,
        email: String,
        nickname: String
    ): UserProfile {
        delay(400)
        cachedProfile = cachedProfile.copy(name = name, email = email, nickname = nickname)
        return cachedProfile
    }
}