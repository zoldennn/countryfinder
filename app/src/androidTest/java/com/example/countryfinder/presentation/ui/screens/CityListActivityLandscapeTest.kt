package com.example.countryfinder.presentation.ui.screens

import android.app.Activity
import android.app.Instrumentation
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import com.example.countryfinder.TestApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry

// TODO: Ahora que hay varias clases de tests Activity, considerar agregar una BaseTestClass y heredarla
class CityListActivityLandscapeTest {

    @get:Rule(order = 0)
    val intentsRule = IntentsRule()

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<CityListActivity>()

    private val tags = CityListActivity.CityListTags

    @Before
    fun ensureTestAppAndLandscape() {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as TestApp
        app.resetState()

        composeRule.activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @Test
    fun landscape_map_shows_right_pane_after_clicking_map_button() {
        // Scroll to row 1 (in case is not in viewport, this avoid future failures)
        composeRule.onNodeWithTag(tags.LIST)
            .performScrollToNode(hasTestTag(tags.row(1)))

        // Placeholder text in map is visible
        composeRule.onNodeWithTag(tags.MAP_PLACEHOLDER, useUnmergedTree = true).assertExists()

        // PerformClick in "MAP" button, row 1
        composeRule.onNodeWithTag(tags.map(1), useUnmergedTree = true).performClick()

        // Wait until map panel appears
        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodes(hasTestTag(tags.MAP_PANEL), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Placeholder should not be visible anymore
        composeRule.onAllNodes(hasTestTag(tags.MAP_PLACEHOLDER)).assertCountEquals(0)
    }

    @Test
    fun landscape_info_opens_detail_activity_intent() {
        // Row 1 is visible
        composeRule.onNodeWithTag(tags.LIST)
            .performScrollToNode(hasTestTag(tags.row(1)))

        // We dont need to open destiny Activity, so we just intercept the intent
        Intents.intending(IntentMatchers.hasComponent(CityDetailActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        // PerformClick on "INFO" - row1
        composeRule.onNodeWithTag(tags.info(1), useUnmergedTree = true).performClick()

        // Assert
        Intents.intended(IntentMatchers.hasComponent(CityDetailActivity::class.java.name))
    }
}
