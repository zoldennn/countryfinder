package com.example.countryfinder

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCityRepository : CityRepository {
    private val favs = MutableStateFlow<Set<Int>>(emptySet())

    // Seed data so the Activity shows a list immediately
    private val seed = listOf(
        City("AR", "Buenos Aires", 1L, CityCoordinates(0.0, 0.0)),
        City("CO", "Bogota",       2L, CityCoordinates(0.0, 0.0)),
        City("DE", "Berlin",       3L, CityCoordinates(0.0, 0.0)),
        City("ES", "Barcelona",    4L, CityCoordinates(0.0, 0.0)),
    )

    override suspend fun getCities(): List<City> = seed

    override suspend fun toggleFavorite(cityId: Int): Boolean {
        val s = favs.value.toMutableSet()
        val added = if (s.contains(cityId)) { s.remove(cityId); false } else { s.add(cityId); true }
        favs.value = s.toSet()
        return added
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> = favs.asStateFlow()

    override fun getCitiesByIds(ids: Set<Int>): List<City> =
        ids.mapNotNull { id -> seed.firstOrNull { it.id == id.toLong() } }

    fun reset() {
        favs.value = emptySet()
    }
}

