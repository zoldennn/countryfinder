package com.example.countryfinder

import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.countryfinder.domain.usecase.ToggleFavoriteCityUseCase

interface AppContainer {
    val cityRepository: CityRepository
    val toggleFavoriteCityUseCase: ToggleFavoriteCityUseCase
    val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase
}