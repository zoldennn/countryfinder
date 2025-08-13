package com.example.countryfinder.presentation.viewmodel

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import com.example.countryfinder.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
}
