package com.example.countryfinder.presentation.ui.screens

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.ExperimentalTestApi
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
import androidx.test.platform.app.InstrumentationRegistry
import com.example.countryfinder.AppContainer
import com.example.countryfinder.TestApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDetailActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cityId = 99L
    private val cityIdInt  = 99  // for repo/UC which use Int
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

    private lateinit var app: TestApp

    @Before
    fun setUp() {
        val appCtx = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        assertTrue("Application is not TestApp", appCtx is TestApp)
        assertTrue("Application is not AppContainer", appCtx is AppContainer)
        app = appCtx as TestApp

        // Must reset the repository in order to reset the states for other tests
        app.resetState()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersTitleAndCoordinates() {
        // Wait until have at least 1 node with expected title, otherwise no assert is made
        composeRule.waitUntilAtLeastOneExists(
            hasText("$cityName, $cityCountry"),
            timeoutMillis = 5_000
        )

        composeRule.onNodeWithText("$cityName, $cityCountry").assertExists()
        composeRule.onNodeWithText("Lat: $cityLat, Lon: $cityLon").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun button_opens_map_with_intent() {
        Intents.init()
        try {
            // We dont need to open destiny Activity, so we just intercept the intent
            Intents.intending(IntentMatchers.hasAction(Intent.ACTION_VIEW))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            composeRule.waitUntilAtLeastOneExists(hasText("Open map"), 5_000)
            composeRule.onNodeWithText("Open map").performClick()

            val expectedUri = Uri.parse("geo:$cityLat,$cityLon?q=$cityLat,$cityLon($cityName)")
            Intents.intended(
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(expectedUri)
                )
            )
        } finally {
            Intents.release()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun buttonInfoOpensWikipedia() {
        Intents.init()
        composeRule.waitUntilAtLeastOneExists(hasText("More info"), 5_000)

        composeRule.onNodeWithText("More info")
            .assertExists()
            .performClick()

        val encoded = Uri.encode(cityName.replace(' ', '_'))
        val expected = Uri.parse("https://en.wikipedia.org/wiki/$encoded")

        Intents.intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(expected)
            )
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun favorite_toggle_updates_repository() = kotlinx.coroutines.test.runTest {
        // First, no favorites
        assertFalse(app.repo.observeFavoriteIds().first().contains(cityIdInt))

        // Click to favorite icon (by contentDescription)
        composeRule.waitUntilAtLeastOneExists(hasContentDescription("Favorite"), 5_000)
        composeRule.onNodeWithContentDescription("Favorite")
            .assertExists()
            .performClick()

        // Wait until fake repo reflects the change
        withTimeout(5_000) {
            app.repo.observeFavoriteIds().first { it.contains(cityIdInt) }
        }

        // Assert
        val current = app.repo.observeFavoriteIds().first()
        assertTrue(current.contains(cityIdInt))
    }
}
