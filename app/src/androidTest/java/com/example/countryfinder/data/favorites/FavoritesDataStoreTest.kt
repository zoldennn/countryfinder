package com.example.countryfinder.data.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesDataStoreTest {

    private lateinit var context: Context
    private lateinit var store: FavoritesDataStore
    private lateinit var file: File
    private lateinit var testScope: TestScope
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        testScope = TestScope(UnconfinedTestDispatcher())

        file = File.createTempFile("favorites_ds", ".preferences_pb", context.cacheDir).apply {
            delete() // We make sure to start clean
        }

        dataStore = newPreferencesDataStore(file, testScope)
        store = FavoritesDataStore(dataStore)
    }

    // TODO: Emprolijar esto, hay que limpiar el DataStore después de cada test para evitar Exception de instancias repetidas y arrastrar el estado
    @After
    fun tearDown() = runTest {
        file.delete()
    }

    fun newPreferencesDataStore(file: File, scope: CoroutineScope): DataStore<Preferences> {
        // DataStore demands only one instance per file -> every test class uses his own file
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
    }

    @Test
    fun initiallyFavoritesIsEmpty() = runTest {
        // With
        val current = store.observeIds().first()

        // Assert
        assertTrue(current.isEmpty())
    }

    @Test
    fun toggleFavoriteAddsThenRemovesCityId() = runTest {
        // With
        val id = 123
        val added = store.toggle(id)
        assertTrue(added)

        // Do
        var current = store.observeIds().first()
        assertTrue(id in current)

        val removed = store.toggle(id)
        assertFalse(removed)
        current = store.observeIds().first()

        // Assert
        assertFalse(id in current)
    }

    @Test
    fun toggleFavoriteSupportsMultipleCityIds() = runTest {
        // With
        val ids = listOf(1, 2, 3, 10, 42)
        ids.forEach { store.toggle(it) }

        // Do
        val current = store.observeIds().first()
        assertEquals(ids.toSet(), current)
        store.toggle(10) // remove 10
        val after = store.observeIds().first()

        // Assert
        assertEquals(ids.filter { it != 10 }.toSet(), after)
    }

    /**
     * DataStore does not allow two active instances for the same file in the same process
     * this tests asserts "persistance" recreating only the FavoritesDataStore wrapper on the same DataStore
     * What we look for is to observeIds reads what was saved previously
     */
    @Test
    fun toggleFavoritePersistOnRecreation() = runTest {
        // With
        store.toggle(7)
        store.toggle(8)

        // Re-create only wrapper; DataStore is the same
        val newWrapper = FavoritesDataStore(dataStore)
        val current = newWrapper.observeIds().first()

        assertTrue(7 in current && 8 in current)
    }
}
