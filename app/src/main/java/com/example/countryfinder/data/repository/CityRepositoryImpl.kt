package com.example.countryfinder.data.repository

import com.example.countryfinder.data.favorites.FavoritesDataStore
import com.example.countryfinder.data.services.CityApiService
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow

class CityRepositoryImpl(
    private val favorites: FavoritesDataStore,
    private val api: CityApiService,
    private val cityCache: Map<Int, City>
) : CityRepository {
    override suspend fun getCities(): List<City> = api.getCities()

    override suspend fun toggleFavorite(cityId: Int): Boolean =
        favorites.toggle(cityId)

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        favorites.observeIds()

    override fun getCitiesByIds(ids: Set<Int>): List<City> =
        ids.mapNotNull { cityCache[it] }
}