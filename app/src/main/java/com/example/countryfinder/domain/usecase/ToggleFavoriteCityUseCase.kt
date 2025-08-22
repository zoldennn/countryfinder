package com.example.countryfinder.domain.usecase

import com.example.countryfinder.domain.repository.CityRepository

/**
 * Changes favorite state and return the new state (true if favorite)
 */
class ToggleFavoriteCityUseCase(
    private val repo: CityRepository
) {
    suspend operator fun invoke(cityId: Int): Boolean = repo.toggleFavorite(cityId)
}