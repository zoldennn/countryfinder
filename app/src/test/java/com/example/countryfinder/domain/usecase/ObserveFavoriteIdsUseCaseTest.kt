package com.example.countryfinder.domain.usecase

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.collections.emptySet
import kotlin.collections.listOf

class ObserveFavoriteIdsUseCaseTest {

    @Test
    fun `invoke emits updates`() = runTest {
        val repo = FakeCityRepositoryForObserveUC()
        val useCase = ObserveFavoriteIdsUseCase(repo)

        val received: MutableList<Set<Int>> = mutableListOf()

        // Start collecting
        val job = launch { useCase().collect { received.add(it) } }

        // Let the collector receive the initial emission from StateFlow
        advanceUntilIdle()

        // Initial emission
        assertEquals(listOf(emptySet<Int>()), received)

        repo.toggleFavorite(1)
        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>(), setOf(1)), received)

        repo.toggleFavorite(2)
        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>(), setOf(1), setOf(1, 2)), received)

        repo.toggleFavorite(1)
        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>(), setOf(1), setOf(1, 2), setOf(2)), received)

        job.cancel()
    }
}

// TODO: Llevar estos fakes a un sharedTest, son bastante parecidos entre clases
private class FakeCityRepositoryForObserveUC : CityRepository {
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
}