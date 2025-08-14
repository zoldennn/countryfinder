package com.example.countryfinder.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("favorites")

/**
 * Class that handles saving favorites cities in DataStore
 */
class FavoritesDataStore(private val context: Context) {
    private val KEY_IDS = stringSetPreferencesKey("fav_ids")

    val favoritesFlow = context.dataStore.data.map { prefs ->
        prefs[KEY_IDS] ?: emptySet()
    }

    suspend fun toggle(id: Long) {
        context.dataStore.edit { prefs ->
            val set = prefs[KEY_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!set.add(id.toString())) set.remove(id.toString())
            prefs[KEY_IDS] = set
        }
    }
}
