import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.countryfinder.data.favorites.FavoritesDataStore
import com.example.countryfinder.data.repository.CityRepositoryImpl
import com.example.countryfinder.data.services.CityApiService
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CityRepositoryApiTest {

    @Test
    fun `toggleFavorite updates store and observeFavoriteIds emits updates`() = runTest {
        val api: CityApiService = mockk(relaxed = true)

        val file = File.createTempFile("prefs_fav", ".preferences_pb")
        val dataStore = newPreferencesDataStore(file, this)
        val favorites = FavoritesDataStore(dataStore)

        val repo = CityRepositoryImpl(
            favorites = favorites,
            api = api,
            cityCache = emptyMap()
        )

        val received = mutableListOf<Set<Int>>()
        val job = launch { repo.observeFavoriteIds().collect { received.add(it) } }

        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>()), received)

        val on = repo.toggleFavorite(7)
        assertTrue(on)
        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>(), setOf(7)), received)

        val off = repo.toggleFavorite(7)
        assertFalse(off)
        advanceUntilIdle()
        assertEquals(listOf(emptySet<Int>(), setOf(7), emptySet<Int>()), received)

        job.cancel()
        file.delete()
    }
}

/** Creates a Preferences DataStore backed by a temp file (only used in this Test). */
fun newPreferencesDataStore(tempFile: File, scope: CoroutineScope): DataStore<androidx.datastore.preferences.core.Preferences> {
    return PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { tempFile }
    )
}
