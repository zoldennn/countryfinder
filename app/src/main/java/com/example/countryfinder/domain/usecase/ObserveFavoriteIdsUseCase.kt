package com.example.countryfinder.domain.usecase

import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteIdsUseCase(
    private val repo: CityRepository
) {
    operator fun invoke(): Flow<Set<Int>> = repo.observeFavoriteIds()
}