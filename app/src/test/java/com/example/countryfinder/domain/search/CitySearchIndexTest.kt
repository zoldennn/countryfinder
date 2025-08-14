package com.example.countryfinder.domain.search

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CitySearchIndexTest {

    private fun createCity(name: String, country: String, id: Long): City =
        City(country = country, name = name, id = id, coordinates = CityCoordinates(lon = 0.0, lat = 0.0))

    private fun basicCitySeed() = listOf(
        createCity("Alabama", "US", 1),
        createCity("Albuquerque", "US", 2),
        createCity("Anaheim", "US", 3),
        createCity("Arizona", "US", 4),
        createCity("Sydney", "AU", 5),
    )

    @Test
    fun `all returns sorted by city then country (case-insensitive)`() {
        // With
        val input = listOf(
            createCity("sydney", "AU", 5),
            createCity("Anaheim", "US", 3),
            createCity("alabama", "US", 1),
            createCity("Arizona", "US", 4),
            createCity("Albuquerque", "US", 2),
        )

        // Do
        val index = CitySearchIndex(input)
        val names = index.all().map { "${it.name.lowercase(Locale.ROOT)},${it.country.lowercase(Locale.ROOT)}" }

        // Assert
        assertEquals(
            listOf(
                "alabama,us",
                "albuquerque,us",
                "anaheim,us",
                "arizona,us",
                "sydney,au"
            ),
            names
        )
    }

    @Test
    fun `prefix A returns Alabama, Albuquerque, Anaheim, Arizona`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("A").map { it.name }

        // Assert
        assertEquals(listOf("Alabama", "Albuquerque", "Anaheim", "Arizona"), result)
    }

    @Test
    fun `prefix Al returns Alabama and Albuquerque`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("Al").map { it.name }

        // Assert
        assertEquals(listOf("Alabama", "Albuquerque"), result)
    }

    @Test
    fun `prefix Alb returns only Albuquerque`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("Alb").map { it.name }

        // Assert
        assertEquals(listOf("Albuquerque"), result)
    }

    @Test
    fun `case-insensitive prefix s returns Sydney`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("s").map { it.name }

        // Assert
        assertEquals(listOf("Sydney"), result)
    }

    @Test
    fun `empty prefix returns full sorted list`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("").map { it.name }

        // Assert
        assertEquals(listOf("Alabama", "Albuquerque", "Anaheim", "Arizona", "Sydney"), result)
    }

    @Test
    fun `whitespace-only prefix returns full sorted list`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("   ").map { it.name }

        // Assert
        assertEquals(listOf("Alabama", "Albuquerque", "Anaheim", "Arizona", "Sydney"), result)
    }

    @Test
    fun `non-matching prefix returns empty list`() {
        // With
        val index = CitySearchIndex(basicCitySeed())

        // Do
        val result = index.findByPrefix("zzz")

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `same name different country keeps country tiebreaker`() {
        // With
        val input = listOf(
            createCity("Paris", "FR", 1),
            createCity("Paris", "US", 2),
            createCity("Parintins", "BR", 3),
            createCity("Paramaribo", "SR", 4),
        )

        // Do
        val index = CitySearchIndex(input)

        // 'par' should include all 'Paris*' and 'Para*' sorted by city then country
        val result = index.findByPrefix("Par").map { "${it.name},${it.country}" }

        // Assert
        assertEquals(
            listOf(
                "Paramaribo,SR", // 'Paramaribo' < 'Parintins' < 'Paris'
                "Parintins,BR",
                "Paris,FR",
                "Paris,US"
            ),
            result
        )
    }

    @Test
    fun `unicode and accents handled via Locale ROOT`() {
        // With
        val input = listOf(
            createCity("Ávila", "ES", 1),
            createCity("Avila", "IT", 2),
            createCity("äviken", "SE", 3),
            createCity("Avignon", "FR", 4),
        )

        // Do
        val index = CitySearchIndex(input)
        val result = index.findByPrefix("a").map { it.name }

        // Assert
        assertEquals(listOf("Avignon", "Avila"), result)
    }
}
