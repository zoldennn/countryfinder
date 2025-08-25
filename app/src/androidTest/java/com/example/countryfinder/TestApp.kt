package com.example.countryfinder

import android.app.Application
import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.countryfinder.domain.usecase.ToggleFavoriteCityUseCase

class TestApp : Application(), AppContainer {

    // Keep the concrete fake so we can call test-only APIs like reset()
    val repo: FakeCityRepository by lazy { FakeCityRepository() }

    // AppContainer interface exposure (what production code uses)
    override val cityRepository: CityRepository get() = repo

    override val toggleFavoriteCityUseCase: ToggleFavoriteCityUseCase by lazy {
        ToggleFavoriteCityUseCase(cityRepository)
    }

    override val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase by lazy {
        ObserveFavoriteIdsUseCase(cityRepository)
    }

    fun resetState() {
        repo.reset()
    }
}
