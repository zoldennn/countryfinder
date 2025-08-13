package com.example.countryfinder.domain.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CityCoordinatesTest {

    private val gson = Gson()

    @Test
    fun `Object creation`() {
        val cityCoordinates = CityCoordinates(-78.497498, -9.12417)

        assertNotNull(cityCoordinates)
        assertEquals(-78.497498, cityCoordinates.lon, 1e-6)
        assertEquals(-9.12417, cityCoordinates.lat, 1e-6)
    }

    @Test
    fun `Object creation with empty values`() {
        val cityCordinatesJson = "".trimIndent()

        val cityCoordinates = gson.fromJson(cityCordinatesJson, CityCoordinates::class.java)
        assertNull(cityCoordinates)
    }

    @Test
    fun `deserialize City from JSON`() {
        val cityCoordinatesJson = """
            {
            "lon":34.283333,"lat":44.549999
            }
        """.trimIndent()

        val cityCoordinates = gson.fromJson(cityCoordinatesJson, CityCoordinates::class.java)

        assertNotNull(cityCoordinates)
        assertEquals(34.283333, cityCoordinates.lon, 1e-6)
        assertEquals(44.549999, cityCoordinates.lat, 1e-6)
    }
}