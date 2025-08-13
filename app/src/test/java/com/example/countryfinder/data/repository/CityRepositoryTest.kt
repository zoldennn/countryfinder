package com.example.countryfinder.data.repository

import com.example.countryfinder.data.services.CityApiService
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CityRepositoryTest {

    private val api: CityApiService = mockk()

    @Test
    fun `getCities returns a City list from the API`() = runTest {
        // With
        val cityList = listOf(
            City(country = "US", name = "Denver", id = 5419384L, coordinates = CityCoordinates(-104.9847, 39.7392)),
            City(country = "AU", name = "Sydney", id = 2147714L, coordinates = CityCoordinates(151.20732, -33.86785))
        )
        coEvery { api.getCities() } returns cityList

        val repo = CityRepositoryImpl(api)

        // Do
        val result = repo.getCities()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Denver", result[0].name)
        assertEquals("Sydney", result[1].name)
        coVerify(exactly = 1) { api.getCities() }
    }

    @Test
    fun `getCities returns exception on API failure`() = runTest {
        // With
        coEvery { api.getCities() } throws IOException("HTTP 404")
        val repo = CityRepositoryImpl(api)

        // Do
        try {
            repo.getCities()
            assertTrue("Exception was expected", false)
        } catch (e: IOException) {
            assertEquals("HTTP 404", e.message)
        }

        coVerify(exactly = 1) { api.getCities() }
    }
}
