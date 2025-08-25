package com.example.countryfinder.presentation.ui.screens

import android.app.Activity
import android.app.Instrumentation
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.example.countryfinder.TestApp
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CityListActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<CityListActivity>()

    private val tags = CityListActivity.CityListTags

    // Must reset the repository in order to reset the states for other tests
    @Before
    fun assertApplicationIsTestApp() {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as TestApp
        app.resetState()
    }

    @Test
    fun list_shows_seeded_cities() {
        // Top bar renders
        composeRule.onNodeWithText("CountryFinder").assertExists()

        // Rows from FakeCityRepository shows
        composeRule.onNodeWithTag(tags.row(1)).assertExists()
        composeRule.onNodeWithTag(tags.row(2)).assertExists()
        composeRule.onNodeWithTag(tags.row(3)).assertExists()
        composeRule.onNodeWithTag(tags.row(4)).assertExists()
    }

    @Test
    fun toggle_favorite_changes_icon_state() {
        val cityFavorite = composeRule.onNodeWithTag(tags.fav(2), useUnmergedTree = true)
        cityFavorite.assertIsOff()
        cityFavorite.performClick()
        cityFavorite.assertIsOn()
    }

    @Test
    fun only_favorites_shows_only_marked_cities() {
        composeRule.onNodeWithTag(tags.fav(2), useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag(tags.fav(4), useUnmergedTree = true).performClick()

        // Enable filter
        composeRule.onNodeWithTag(tags.ONLY_FAV_SWITCH).performClick()

        // Only rows 2 and 4 should remain
        composeRule.onNodeWithTag(tags.row(1)).assertDoesNotExist()
        composeRule.onNodeWithTag(tags.row(3)).assertDoesNotExist()
        composeRule.onNodeWithTag(tags.row(2)).assertExists()
        composeRule.onNodeWithTag(tags.row(4)).assertExists()
    }

    @Test
    fun search_filters_results_case_insensitive() {
        // Type "bo" -> should match "Bogota"
        composeRule.onNodeWithTag(tags.SEARCH).performTextClearance()
        composeRule.onNodeWithTag(tags.SEARCH).performTextInput("bo")

        // Wait until list stabilizes, the 200ms debounce
        composeRule.waitUntil(timeoutMillis = 2_000) {
            // Condition: "Bogota, CO" is present and "Berlin, DE" is gone
            val bogota = composeRule.onAllNodes(hasTestTag(tags.row(2))).fetchSemanticsNodes().isNotEmpty()
            val berlinGone = composeRule.onAllNodes(hasTestTag(tags.row(3))).fetchSemanticsNodes().isEmpty()
            bogota && berlinGone
        }

        composeRule.onNodeWithTag(tags.row(2)).assertExists()
        composeRule.onNodeWithTag(tags.row(3)).assertDoesNotExist()
    }

    @Test
    fun info_button_starts_detail_activity_intent() {
        Intents.init()
        try {
            // We dont need to open destiny Activity, so we just intercept the intent
            Intents.intending(IntentMatchers.hasComponent(CityDetailActivity::class.java.name))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            composeRule.onNodeWithTag(tags.info(1), useUnmergedTree = true).performClick()

            // Verification: we try to open CityDetailActivity
            Intents.intended(IntentMatchers.hasComponent(CityDetailActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }


    @Test
    fun map_button_sends_geo_intent() {
        Intents.init()
        try {
            composeRule.onNodeWithTag(tags.map(1)).performClick()
            Intents.intended(allOf(
                IntentMatchers.hasAction(android.content.Intent.ACTION_VIEW),
                IntentMatchers.hasDataString(org.hamcrest.Matchers.startsWith("geo:"))
            ))
        } finally {
            Intents.release()
        }
    }
}
