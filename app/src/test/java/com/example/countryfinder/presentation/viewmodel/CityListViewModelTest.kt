package com.example.countryfinder.presentation.viewmodel

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import com.example.countryfinder.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private fun citiesSeed() = listOf(
        City("US", "Alabama", 1, CityCoordinates(lon = 0.0, lat = 0.0)),
        City("US", "Albuquerque", 2, CityCoordinates(lon = 0.0, lat = 0.0)),
        City("US", "Anaheim", 3, CityCoordinates(lon = 0.0, lat = 0.0)),
        City("US", "Arizona", 4, CityCoordinates(lon = 0.0, lat = 0.0)),
        City("AU", "Sydney", 5, CityCoordinates(lon = 0.0, lat = 0.0)),
    )

    private fun createViewModelWithSeedCities(favsFlow: MutableSharedFlow<Set<String>>? = null): CityListViewModel {
        coEvery { repository.getCities() } returns citiesSeed()
        val viewModel = CityListViewModel(repository, ioDispatcher = testDispatcher)
        if (favsFlow != null) viewModel.attachFavorites(favsFlow)
        return viewModel
    }

    private fun names(vm: CityListViewModel) = vm.cities.value.map { it.name }

    @Test
    fun `Initial ViewModel load groups by asc city and asc country`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()

        // Do
        advanceUntilIdle()

        // Assert
        val cityNames = viewModel.cities.value.map { "${it.name},${it.country}" }
        assertEquals(
            listOf("Alabama,US", "Albuquerque,US", "Anaheim,US", "Arizona,US", "Sydney,AU"),
            cityNames
        )
        assertNull(viewModel.error.value)
        assertFalse(viewModel.loading.value)
        coVerify(exactly = 1) { repository.getCities() }
    }

    @Test
    fun `prefix A includes Alabama, Albuquerque, Anaheim, Arizona`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()

        // Do
        advanceUntilIdle()
        viewModel.onQueryChange("A")
        advanceTimeBy(200) // Debounce for recompute
        advanceUntilIdle()

        // Assert
        val result = viewModel.cities.value.map { it.name }
        assertEquals(listOf("Alabama", "Albuquerque", "Anaheim", "Arizona"), result)
    }

    @Test
    fun `prefix AL includes Alabama and Albuquerque`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()
        advanceUntilIdle()

        // Do
        viewModel.onQueryChange("Al")
        advanceTimeBy(200)
        advanceUntilIdle()

        // Assert
        val result = viewModel.cities.value.map { it.name }
        assertEquals(listOf("Alabama", "Albuquerque"), result)
    }

    @Test
    fun `prefix ALB returns only Albuquerque`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()
        advanceUntilIdle()

        // Do
        viewModel.onQueryChange("Alb")
        advanceTimeBy(200)
        advanceUntilIdle()

        // Assert
        val result = viewModel.cities.value.map { it.name }
        assertEquals(listOf("Albuquerque"), result)
    }

    @Test
    fun `case-insensitive S returns only Sydney`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()
        advanceUntilIdle()

        // Do
        viewModel.onQueryChange("s") // Minus
        advanceTimeBy(200)
        advanceUntilIdle()

        // Assert
        val result = viewModel.cities.value.map { it.name }
        assertEquals(listOf("Sydney"), result)
    }

    @Test
    fun `empty prefix returns full city list`() = runTest {
        // With
        val viewModel = createViewModelWithSeedCities()
        advanceUntilIdle()

        // Do
        viewModel.onQueryChange("") // vacío
        advanceTimeBy(200)
        advanceUntilIdle()

        // Assert
        val result = viewModel.cities.value.map { it.name }
        assertEquals(listOf("Alabama", "Albuquerque", "Anaheim", "Arizona", "Sydney"), result)
    }

    @Test
    fun `viewModel init loads cities OK and updates states`() = runTest {
        // With
        coEvery { repository.getCities() } returns mockCities()
        val viewModel = CityListViewModel(repository, ioDispatcher = testDispatcher)

        // Do
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.loading.value)
        assertEquals(2, viewModel.cities.value.size)
        assertNull(viewModel.error.value)
        coVerify(exactly = 1) { repository.getCities() }
    }

    @Test
    fun `viewModel init handles error and leaves empty list`() = runTest {
        // With
        coEvery { repository.getCities() } throws RuntimeException("exception")
        val viewModel = CityListViewModel(repository, ioDispatcher = testDispatcher)

        // Do
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.loading.value)
        assertTrue(viewModel.cities.value.isEmpty())
        assertEquals("exception", viewModel.error.value)
        coVerify(exactly = 1) { repository.getCities() }
    }

    @Test
    fun `viewModel init updates new data`() = runTest {
        // With
        coEvery { repository.getCities() } returnsMany listOf(
            mockCities(),
            listOf(City("AR", "Buenos Aires", 3, CityCoordinates(-58.3816, -34.6037)))
        )
        val viewModel = CityListViewModel(repository, ioDispatcher = testDispatcher)

        // Do
        advanceUntilIdle()
        assertEquals(2, viewModel.cities.value.size)
        viewModel.load()
        advanceUntilIdle()

        // Assert
        assertEquals(1, viewModel.cities.value.size)
        assertEquals("Buenos Aires", viewModel.cities.value.first().name)
        assertNull(viewModel.error.value)
        coVerify(exactly = 2) { repository.getCities() }
    }

    @Test
    fun `ony favorites toggles filter by ids`() = runTest {
        // With
        val favsFlow = MutableSharedFlow<Set<String>>(replay = 0, extraBufferCapacity = 1)
        val viewModel = createViewModelWithSeedCities(favsFlow)
        advanceUntilIdle() // finish load()

        // Emit favorites (ids as String)
        favsFlow.tryEmit(setOf("2", "5")) // Albuquerque, Sydney
        advanceUntilIdle()

        // Do
        viewModel.toggleOnlyFavorites()
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("Albuquerque", "Sydney"), names(viewModel))
    }

    @Test
    fun `changing favorites recomputes list when only favorites is toggle`() = runTest {
        // With
        val favsFlow = MutableSharedFlow<Set<String>>(replay = 0, extraBufferCapacity = 2)
        val viewModel = createViewModelWithSeedCities(favsFlow)
        advanceUntilIdle()

        // Do
        viewModel.toggleOnlyFavorites()
        advanceUntilIdle()
        favsFlow.tryEmit(setOf("2", "5"))
        advanceUntilIdle()
        assertEquals(listOf("Albuquerque", "Sydney"), names(viewModel))

        // Assert
        favsFlow.tryEmit(setOf("1"))
        advanceUntilIdle()
        assertEquals(listOf("Alabama"), names(viewModel))
    }

    @Test
    fun `prefix and only favorites combines filter`() = runTest {
        // With
        val favsFlow = MutableSharedFlow<Set<String>>(replay = 0, extraBufferCapacity = 1)
        val vm = createViewModelWithSeedCities(favsFlow)
        advanceUntilIdle()

        // Do
        favsFlow.tryEmit(setOf("1", "3", "5"))
        advanceUntilIdle()
        vm.onQueryChange("A")
        advanceTimeBy(200)
        advanceUntilIdle()

        // Toggle favorites, remains Alabama(1) y Anaheim(3)
        vm.toggleOnlyFavorites()
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("Alabama", "Anaheim"), names(vm))
    }
}
