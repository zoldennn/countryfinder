package com.example.countryfinder.data.favorites

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class that handles saving favorites cities in DataStore
 */
class FavoritesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY = stringSetPreferencesKey("favorite_ids")

    fun observeIds(): Flow<Set<Int>> =
        dataStore.data.map { prefs ->
            prefs[KEY]?.mapNotNull { it.toIntOrNull() }?.toSet().orEmpty()
        }

    suspend fun toggle(id: Int): Boolean {
        var added = false
        dataStore.edit { prefs ->
            val current = prefs[KEY]?.toMutableSet() ?: mutableSetOf()
            val str = id.toString()
            added = if (current.contains(str)) { current.remove(str); false } else { current.add(str); true }
            prefs[KEY] = current
        }
        return added
    }
}
