package com.example.countryfinder.domain.search

import com.example.countryfinder.domain.model.City
import java.util.Locale
import kotlin.math.min

/**
 * This class maintains a search index optimized for filtering cities by prefix in a case-insensitive way
 *
 * It is created once when the full city list is loaded, then reused for every user search
 *
 * The goal is to allow very fast prefix searches using binary search (lower/upper bound)
 * instead of scanning the entire list every time
 */
class CitySearchIndex(cities: List<City>) {

    // The list of cities sorted alphabetically by name (case-insensitive), then by country
    private val sorted: List<City> = cities.sortedWith(compareBy(
        { it.name.lowercase(Locale.ROOT) },
        { it.country.lowercase(Locale.ROOT) }
    ))

    // A list of lowercase city names that matches the sorted list order
    // Used for binary search to find prefix matches quickly
    private val keys: List<String> = sorted.map { it.name.lowercase(Locale.ROOT) }

    /**
     * Returns the full sorted list of cities.
     */
    // TODO: Usar este método en próximos PRs
    fun all(): List<City> = sorted

    /**
     * Finds all cities whose names start with the given prefix (case-insensitive)
     *
     * @param prefix The search text entered by the user
     *               - Leading/trailing spaces are ignored
     *               - If empty, returns the full city list
     *
     * @return A sublist of cities matching the prefix
     */
    fun findByPrefix(prefix: String): List<City> {
        val cityPrefix = prefix.trim().lowercase(Locale.ROOT)
        if (cityPrefix.isEmpty()) return sorted

        // Find the index of the first city name >= prefix
        val start = lowerBound(keys, cityPrefix)
        if (start >= keys.size) return emptyList()

        // '\uFFFF' ensures we include all names starting with 'p'
        val maxKey = cityPrefix + '\uFFFF'

        // Find the index of the first city name > maxKey
        val end = upperBound(keys, maxKey)

        // Extract the range and filter to ensure exact prefix match
        return sorted.subList(start, end).filter {
            it.name.length >= cityPrefix.length &&
                    it.name.substring(0, min(it.name.length, cityPrefix.length)).lowercase(Locale.ROOT) == cityPrefix
        }
    }

    /**
     * Returns the smallest index in 'list' where element >= key
     * Binary search used to find the start of a range
     */
    private fun lowerBound(list: List<String>, key: String): Int {
        var left = 0
        var rightInclusive = list.size
        while (left < rightInclusive) {
            val midIndex = (left + rightInclusive) ushr 1
            if (list[midIndex] < key) left = midIndex + 1 else rightInclusive = midIndex
        }
        return left
    }

    /**
     * Returns the smallest index in 'list' where element > key
     * Binary search used to find the end of a range
     */
    private fun upperBound(list: List<String>, key: String): Int {
        var left = 0
        var rightInclusive = list.size
        while (left < rightInclusive) {
            val midIndex = (left + rightInclusive) ushr 1
            if (list[midIndex] <= key) left = midIndex + 1 else rightInclusive = midIndex
        }
        return left
    }
}
