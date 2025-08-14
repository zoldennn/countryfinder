package com.example.countryfinder.presentation.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.countryfinder.data.favorites.FavoritesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDetailActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val favorites = FavoritesDataStore(context)
    private val cityId = 99L
    private val cityName = "Avila"
    private val cityCountry = "ES"
    private val cityLat = 1.23
    private val cityLon = 4.56

    private fun intentWithExtras() = Intent(context, CityDetailActivity::class.java).apply {
        putExtra("city_name", cityName)
        putExtra("country", cityCountry)
        putExtra("lat", cityLat)
        putExtra("lon", cityLon)
        putExtra("city_id", cityId)
    }

    // Need this order in rules for avoid compose hierarchy error
    @get:Rule(order = 0)
    val composeRule = createEmptyComposeRule()

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule<CityDetailActivity>(intentWithExtras())

    @Before
    fun setUp() {
        runBlocking {
            val currentFavorites = favorites.favoritesFlow.first()
            if (currentFavorites.contains(cityId.toString())) {
                favorites.toggle(cityId)
            }
        }
        Intents.init()
    }

    @After
    fun tearDown() {
        runBlocking {
            val currentFavorites = favorites.favoritesFlow.first()
            if (currentFavorites.contains(cityId.toString())) {
                favorites.toggle(cityId)
            }
        }
        Intents.release()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersTitleAndCoordinates() {
        // Wait until have at least 1 node with expected title, otherwise no assert is made
        composeRule.waitUntilAtLeastOneExists(
            hasText("$cityName, $cityCountry"),
            timeoutMillis = 5_000
        )

        // Assert
        composeRule.onNodeWithText("$cityName, $cityCountry").assertIsDisplayed()
        composeRule.onNodeWithText("Lat: $cityLat, Lon: $cityLon").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun buttonOpensMapWithIntent() {
        // With
        composeRule.waitUntilAtLeastOneExists(hasText("Open map"), 5_000)

        // Do
        composeRule.onNodeWithText("Open map")
            .assertIsDisplayed()
            .performClick()

        // Assert
        val expectedUri = Uri.parse("geo:$cityLat,$cityLon?q=$cityLat,$cityLon($cityName)")
        Intents.intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(expectedUri)
            )
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun buttonInfoOpensWikipedia() {
        // With
        composeRule.waitUntilAtLeastOneExists(hasText("More info"), 5_000)

        // Do
        composeRule.onNodeWithText("More info")
            .assertIsDisplayed()
            .performClick()

        val encoded = Uri.encode(cityName.replace(' ', '_'))
        val expected = Uri.parse("https://en.wikipedia.org/wiki/$encoded")

        // Assert
        Intents.intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(expected)
            )
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun favoriteTogglePersistInDatastore() = runBlocking {
        // First no favorite
        var currentFavorites = favorites.favoritesFlow.first()
        assertFalse(currentFavorites.contains(cityId.toString()))

        // Click on fav icon
        composeRule.waitUntilAtLeastOneExists(hasContentDescription("Favorite"), 5_000)
        composeRule.onNodeWithContentDescription("Favorite")
            .assertIsDisplayed()
            .performClick()

        // Wait for DataStore to store the change
        withTimeout(5_000) {
            favorites.favoritesFlow.first { it.contains(cityId.toString()) }
        }

        // Assert
        currentFavorites = favorites.favoritesFlow.first()
        assertTrue(currentFavorites.contains(cityId.toString()))
    }
}
