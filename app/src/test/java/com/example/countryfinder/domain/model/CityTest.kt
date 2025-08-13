package com.example.countryfinder.domain.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CityTest {

    private val gson = Gson()

    @Test
    fun `Object creation`() {
        val city = City("PE", "Buenos Aires", 6559994, CityCoordinates(-78.497498, -9.12417))

        assertNotNull(city)
        assertEquals("PE", city.country)
        assertEquals("Buenos Aires", city.name)
        assertEquals(6559994, city.id)
        assertEquals(-78.497498, city.coordinates.lon, 1e-6)
        assertEquals(-9.12417, city.coordinates.lat, 1e-6)
    }

    @Test
    fun `Object creation with empty values`() {
        val cityJson = "".trimIndent()

        val city = gson.fromJson(cityJson, City::class.java)
        assertNull(city)
    }

    @Test
    fun `deserialize City from JSON`() {
        val cityJson = """
            {
              "country":"UA",
              "name":"Hurzuf",
              "_id":707860,
              "coord":{"lon":34.283333,"lat":44.549999}
            }
        """.trimIndent()

        val city = gson.fromJson(cityJson, City::class.java)

        assertNotNull(city)
        assertEquals("UA", city.country)
        assertEquals("Hurzuf", city.name)
        assertEquals(707860L, city.id)
        assertEquals(34.283333, city.coordinates.lon, 1e-6)
        assertEquals(44.549999, city.coordinates.lat, 1e-6)
    }

    @Test
    fun `deserialize a City list`() {
        val cityJson = """
            [
              {"country":"US","name":"Denver","_id":5419384,"coord":{"lon":-104.9847,"lat":39.7392}},
              {"country":"AU","name":"Sydney","_id":2147714,"coord":{"lon":151.20732,"lat":-33.86785}}
            ]
        """.trimIndent()

        val cityList = gson.fromJson(cityJson, Array<City>::class.java).toList()

        assertNotNull(cityList)
        assertEquals(2, cityList.size)
        assertEquals("Denver", cityList[0].name)
        assertEquals("Sydney", cityList[1].name)
    }
}
