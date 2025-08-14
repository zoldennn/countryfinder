package com.example.countryfinder.data.favorites

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesDataStoreTest {

    private lateinit var context: Context
    private lateinit var store: FavoritesDataStore

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        store = FavoritesDataStore(context)

        // TODO: Emprolijar esto, hay que limpiar el DataStore antes de cada test para evitar Exception de instancias repetidas y arrastrar el estado
        clearFavorites()
    }

    @After
    fun tearDown() = runTest {
        clearFavorites()
    }

    private suspend fun clearFavorites() {
        val currentFavorites = store.favoritesFlow.first()
        if (currentFavorites.isNotEmpty()) {
            // For every toggled id > remove
            currentFavorites.forEach { idStr ->
                idStr.toLongOrNull()?.let { store.toggle(it) }
            }
            // Check if empty
            assertTrue(store.favoritesFlow.first().isEmpty())
        }
    }

    @Test
    fun initiallyFavoritesIsEmpty() = runTest {
        // With
        val currentFavorites = store.favoritesFlow.first()

        // Assert
        assertTrue(currentFavorites.isEmpty())
    }

    @Test
    fun toggleFavoriteAddsThenRemovesCityId() = runTest {
        // With
        val id = 123L

        // Do
        store.toggle(id)
        var currentFavorites = store.favoritesFlow.first()
        assertTrue(id.toString() in currentFavorites)

        // Assert
        store.toggle(id)
        currentFavorites = store.favoritesFlow.first()
        assertFalse(id.toString() in currentFavorites)
    }

    @Test
    fun toggleFavoriteSupportsMultipleCityIds() = runTest {
        // With
        val ids = listOf(1L, 2L, 3L, 10L, 42L)
        ids.forEach { store.toggle(it) }

        // Do
        val currentFavorites = store.favoritesFlow.first()
        assertEquals(ids.map { it.toString() }.toSet(), currentFavorites)

        // Remove one and check
        store.toggle(10L)
        val afterFavorites = store.favoritesFlow.first()

        // Assert
        assertEquals(ids.filter { it != 10L }.map { it.toString() }.toSet(), afterFavorites)
    }

    @Test
    fun toggleFavoritePersistOnRecreation() = runTest {
        // With
        store.toggle(7L)
        store.toggle(8L)

        // Do
        val newDataStore = FavoritesDataStore(context)
        val currentFavorites = newDataStore.favoritesFlow.first()

        // Assert
        assertTrue("7" in currentFavorites && "8" in currentFavorites)
    }
}