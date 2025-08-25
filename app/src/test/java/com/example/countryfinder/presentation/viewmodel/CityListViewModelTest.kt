package com.example.countryfinder.presentation.viewmodel

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import com.example.countryfinder.domain.repository.CityRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CityListViewModelTest {

    private lateinit var repository: CityRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun mockCities() = listOf(
        City("US", "Denver", 1, CityCoordinates(-104.9847, 39.7392)),
        City("AU", "Sydney", 2, CityCoordinates(151.2073, -33.8678))
    )

    // Alphabetical dataset for testing Search query
    private val citiesSeed = listOf(
        createCity(1, "Buenos Aires", "AR"),
        createCity(2, "Bogota", "CO"),
        createCity(3, "Berlin", "DE"),
        createCity(4, "Barcelona", "ES"),
        createCity(5, "Boston", "US")
    )

    fun createCity(id: Long, name: String, country: String) =
        City(country = country, name = name, id = id, coordinates = CityCoordinates(lon = 0.0, lat = 0.0))

    @Test
    fun `load() fills cities and displayedCities with the full ordered list`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        // Collector starts to activate the WhileSubscribed flow
        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        val expected = citiesSeed.sortedWith(compareBy(
            { it.name.lowercase() }, { it.country.lowercase() }
        ))

        assertEquals(expected, viewModel.cities.value)
        assertEquals(expected, latestDisplayed)

        job.cancel()
    }


    @Test
    fun `load populates cities and displayedCities in sorted order`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        val expected = citiesSeed.sortedWith(compareBy(
            { it.name.lowercase() }, { it.country.lowercase() }
        ))

        assertEquals(expected, viewModel.cities.value)
        assertEquals(expected, latestDisplayed)
        assertFalse(viewModel.loading.value)
        assertNull(viewModel.error.value)

        job.cancel()
    }

    @Test
    fun `loading flag toggles around load`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        advanceUntilIdle()
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun `error is set and cities cleared on repository failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(seed = emptyList(), failOnGet = true)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = listOf(createCity(999, "X", "X")) // sentinel
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        assertTrue(viewModel.cities.value.isEmpty())
        assertTrue(latestDisplayed.isEmpty())
        assertNotNull(viewModel.error.value)
        assertFalse(viewModel.loading.value)

        job.cancel()
    }

    @Test
    fun `toggleOnlyFavorites ON shows only favorites by IDs`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        viewModel.onToggleFavorite(2L)
        viewModel.onToggleFavorite(4L)
        advanceUntilIdle()

        viewModel.toggleOnlyFavorites()
        advanceUntilIdle()

        assertEquals(setOf(2L, 4L), latestDisplayed.map { it.id }.toSet())

        job.cancel()
    }

    @Test
    fun `onToggleFavorite toggles and favorites flow updates`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        // Activate the flow
        var latestFavs: Set<Long> = emptySet()
        val job = launch { viewModel.favorites.collect { latestFavs = it } }

        advanceUntilIdle()

        // Toggle ON
        viewModel.onToggleFavorite(1L)
        advanceUntilIdle()
        assertTrue(latestFavs.contains(1L))

        // Toggle OFF
        viewModel.onToggleFavorite(1L)
        advanceUntilIdle()
        assertFalse(latestFavs.contains(1L))

        job.cancel()
    }

    @Test
    fun `onQueryChange filters by name (case-insensitive) respecting onlyFavorites`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        viewModel.onToggleFavorite(4L) // Barcelona
        viewModel.onToggleFavorite(5L) // Boston
        advanceUntilIdle()

        viewModel.toggleOnlyFavorites()
        advanceUntilIdle()

        viewModel.onQueryChange("bo")
        advanceTimeBy(250)
        advanceUntilIdle()

        val names = latestDisplayed.map { it.name }
        assertEquals(listOf("Boston"), names)

        job.cancel()
    }

    @Test
    fun `blank query returns base search list when onlyFavorites OFF`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        assertEquals(viewModel.cities.value, latestDisplayed)

        job.cancel()
    }

    @Test
    fun `favorites filter applies on current search base not the full list`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = FakeCityRepository(citiesSeed)
        val viewModel = CityListViewModel(repository = repo, ioDispatcher = dispatcher)

        var latestDisplayed: List<City> = emptyList()
        val job = launch { viewModel.displayedCities.collect { latestDisplayed = it } }

        advanceUntilIdle()

        // Mark favorites Berlin(3) and Boston(5)
        viewModel.onToggleFavorite(3L)
        viewModel.onToggleFavorite(5L)
        advanceUntilIdle()

        // Narrow base to names containing 'bo' -> Bogota, Boston
        viewModel.onQueryChange("bo")
        advanceTimeBy(250)
        advanceUntilIdle()

        viewModel.toggleOnlyFavorites()
        advanceUntilIdle()

        assertEquals(listOf("Boston"), latestDisplayed.map { it.name })

        job.cancel()
    }
}


class FakeCityRepository(
    private val seed: List<City> = emptyList(),
    private val failOnGet: Boolean = false
) : CityRepository {

    private val favorites = MutableStateFlow<Set<Int>>(emptySet())

    override suspend fun getCities(): List<City> {
        if (failOnGet) error("Network error")
        return seed
    }

    override suspend fun toggleFavorite(cityId: Int): Boolean {
        val current = favorites.value.toMutableSet()
        val added = if (current.contains(cityId)) { current.remove(cityId); false }
        else { current.add(cityId); true }
        favorites.value = current.toSet()
        return added
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> = favorites.asStateFlow()
    override fun getCitiesByIds(ids: Set<Int>): List<City> {
        TODO("Not yet implemented")
    }
}
