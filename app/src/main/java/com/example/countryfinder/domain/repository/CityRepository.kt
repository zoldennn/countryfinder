package com.example.countryfinder.domain.repository

import com.example.countryfinder.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    suspend fun getCities(): List<City>
    suspend fun toggleFavorite(cityId: Int): Boolean
    fun observeFavoriteIds(): Flow<Set<Int>>
    fun getCitiesByIds(ids: Set<Int>): List<City>
}