package com.example.countryfinder.domain.usecase

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleFavoriteCityUseCaseTest {

    @Test
    fun `invoke toggles favorite and returns new state`() = runTest {
        val repo = FakeCityRepositoryForUC()
        val useCase = ToggleFavoriteCityUseCase(repo)

        // First toggle → becomes favorite → returns true
        val on = useCase(42)
        assertTrue(on)
        assertTrue(42 in repo.currentFavorites())

        // Second toggle → removed from favorites → returns false
        val off = useCase(42)
        assertFalse(off)
        assertFalse(42 in repo.currentFavorites())
    }
}

// TODO: Llevar estos fakes a un sharedTest, son parecidos entre clases
private class FakeCityRepositoryForUC : CityRepository {
    private val favorites = MutableStateFlow<Set<Int>>(emptySet())

    override suspend fun toggleFavorite(cityId: Int): Boolean {
        val cityFavorites = favorites.value.toMutableSet()
        val added = if (cityFavorites.contains(cityId)) { cityFavorites.remove(cityId); false } else { cityFavorites.add(cityId); true }
        favorites.value = cityFavorites.toSet()
        return added
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> = favorites.asStateFlow()

    override fun getCitiesByIds(ids: Set<Int>): List<City> {
        TODO("Not yet implemented")
    }

    override suspend fun getCities() = throw UnsupportedOperationException()

    // Assertion purposes only
    fun currentFavorites(): Set<Int> = favorites.value
}
