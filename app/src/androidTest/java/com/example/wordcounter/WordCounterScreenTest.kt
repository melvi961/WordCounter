package com.example.wordcounter

import android.content.pm.ActivityInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wordcounter.ui.TestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [MainActivity] and [WordCounterScreen].
 *
 * Verifies Compose UI interactions, error handling, text metrics calculations,
 * history list ordering, and device rotation configuration changes.
 */
@RunWith(AndroidJUnit4::class)
class WordCounterScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Scenario 1:
     * Tap Calculate with empty input shows the "Please enter text" error.
     */
    @Test
    fun tapCalculate_withEmptyInput_showsPleaseEnterTextError() {
        val calculateText = targetContext.getString(R.string.btn_calculate)
        val errorText = targetContext.getString(R.string.error_empty_input)
        val emptyHistoryText = targetContext.getString(R.string.empty_history)

        // 1. Initially, no error is displayed and history shows empty placeholder
        composeTestRule.onNodeWithText(errorText).assertDoesNotExist()
        composeTestRule.onNodeWithText(emptyHistoryText).assertIsDisplayed()

        // 2. Tap "Calculate" with an empty input field
        composeTestRule.onNodeWithText(calculateText).performClick()

        // 3. Verify inline error message is shown
        // Found by text from string resources
        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
        // Found by testTag on the unmerged tree (supportingText is inside OutlinedTextField which merges child semantics)
        composeTestRule.onNodeWithTag(TestTags.ERROR_TEXT, useUnmergedTree = true).assertIsDisplayed()

        // 4. Verify no metrics grid or history entry was generated
        composeTestRule.onNodeWithTag(TestTags.METRICS_GRID).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.HISTORY_EMPTY).assertIsDisplayed()
    }

    /**
     * Scenario 2:
     * Type "Good morning!", tap Calculate: results show 1, 2, 1, 0 and a history entry appears.
     */
    @Test
    fun typeGoodMorning_tapCalculate_showsExpectedMetricsAndHistoryEntry() {
        val inputToTest = "Good morning!"
        val calculateText = targetContext.getString(R.string.btn_calculate)
        val sentencesLabel = targetContext.getString(R.string.label_sentences)
        val wordsLabel = targetContext.getString(R.string.label_words)
        val punctuationLabel = targetContext.getString(R.string.label_punctuation)
        val numbersLabel = targetContext.getString(R.string.label_numbers)
        val expectedHistoryEntry = "\"Good morning!\" => 1, 2, 1, 0"

        // 1. Enter "Good morning!" into the text field
        composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
            .performTextInput(inputToTest)

        // 2. Tap Calculate
        composeTestRule.onNodeWithText(calculateText)
            .performClick()

        // 3. Verify error is not displayed (checked on unmerged tree)
        composeTestRule.onNodeWithTag(TestTags.ERROR_TEXT, useUnmergedTree = true).assertDoesNotExist()

        // 4. Verify results show: 1 sentence, 2 words, 1 punctuation, 0 numbers
        // Verification via testTags (using unmerged tree for nested values)
        composeTestRule.onNodeWithTag(TestTags.METRICS_GRID).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.METRIC_VALUE_SENTENCES, useUnmergedTree = true).assertTextEquals("1")
        composeTestRule.onNodeWithTag(TestTags.METRIC_VALUE_WORDS, useUnmergedTree = true).assertTextEquals("2")
        composeTestRule.onNodeWithTag(TestTags.METRIC_VALUE_PUNCTUATION, useUnmergedTree = true).assertTextEquals("1")
        composeTestRule.onNodeWithTag(TestTags.METRIC_VALUE_NUMBERS, useUnmergedTree = true).assertTextEquals("0")

        // Verification via string resource labels and siblings
        composeTestRule.onNode(hasAnySibling(hasText(sentencesLabel)) and hasText("1")).assertIsDisplayed()
        composeTestRule.onNode(hasAnySibling(hasText(wordsLabel)) and hasText("2")).assertIsDisplayed()
        composeTestRule.onNode(hasAnySibling(hasText(punctuationLabel)) and hasText("1")).assertIsDisplayed()
        composeTestRule.onNode(hasAnySibling(hasText(numbersLabel)) and hasText("0")).assertIsDisplayed()

        // 5. Scroll LazyColumn to history item and verify displayed
        val historyMatcher = hasTestTag(TestTags.HISTORY_ITEM_TEXT) and hasText(expectedHistoryEntry)
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(historyMatcher)
        composeTestRule.onNode(historyMatcher, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * Scenario 3:
     * Tap "Generate test sentence": the input field is filled with non-empty text,
     * then Calculate adds it to history.
     */
    @Test
    fun tapGenerateTestSentence_fillsInputWithNonEmptyText_thenCalculateAddsToHistory() {
        val generateBtnText = targetContext.getString(R.string.btn_generate_test)
        val calculateText = targetContext.getString(R.string.btn_calculate)
        val emptyHistoryText = targetContext.getString(R.string.empty_history)

        // Verify initial state: history is empty
        composeTestRule.onNodeWithText(emptyHistoryText).assertIsDisplayed()

        // 1. Tap "Generate test sentence"
        composeTestRule.onNodeWithText(generateBtnText).performClick()

        // 2. Inspect the input field semantics and verify it has been filled with non-empty text
        val inputNode = composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
        val semanticsConfig = inputNode.fetchSemanticsNode().config
        val generatedText = semanticsConfig[SemanticsProperties.EditableText].text

        assertTrue("Generated sentence must not be blank", generatedText.isNotBlank())

        // 3. Tap Calculate
        composeTestRule.onNodeWithText(calculateText).performClick()

        // 4. Verify empty history placeholder is removed
        composeTestRule.onNodeWithText(emptyHistoryText).assertDoesNotExist()

        // 5. Verify history item containing generated text appears
        // Scroll LazyColumn to node to ensure off-screen lazy items are composed
        val historyMatcher = hasTestTag(TestTags.HISTORY_ITEM_TEXT) and hasText(generatedText, substring = true)
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(historyMatcher)
        composeTestRule.onNode(historyMatcher, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * Scenario 4:
     * Two calculations: history shows both, newest first.
     */
    @Test
    fun twoCalculations_historyShowsBoth_newestFirst() {
        val firstInput = "Hello world!"
        val expectedFirstHistory = "\"Hello world!\" => 1, 2, 1, 0"

        val secondInput = "Kotlin 2026 is awesome!"
        val expectedSecondHistory = "\"Kotlin 2026 is awesome!\" => 1, 3, 1, 1"

        val calculateText = targetContext.getString(R.string.btn_calculate)

        // 1. Perform 1st calculation
        composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
            .performTextInput(firstInput)
        composeTestRule.onNodeWithText(calculateText)
            .performClick()

        // Verify first history item is present
        val firstHistoryMatcher = hasTestTag(TestTags.HISTORY_ITEM_TEXT) and hasText(expectedFirstHistory)
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(firstHistoryMatcher)
        composeTestRule.onNode(firstHistoryMatcher, useUnmergedTree = true)
            .assertIsDisplayed()

        // 2. Clear input field and perform 2nd calculation
        composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
            .performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
            .performTextInput(secondInput)
        composeTestRule.onNodeWithText(calculateText)
            .performClick()

        // 3. Verify history shows both calculations
        val historyItems = composeTestRule.onAllNodesWithTag(TestTags.HISTORY_ITEM)
        historyItems.assertCountEquals(2)

        val historyTexts = composeTestRule.onAllNodesWithTag(TestTags.HISTORY_ITEM_TEXT, useUnmergedTree = true)
        historyTexts.assertCountEquals(2)

        // 4. Verify newest first (index 0 is the second calculation, index 1 is the first calculation)
        historyTexts[0].assertTextEquals(expectedSecondHistory)
        historyTexts[1].assertTextEquals(expectedFirstHistory)

        // Also verify both entries exist in the history items
        val secondHistoryMatcher = hasTestTag(TestTags.HISTORY_ITEM_TEXT) and hasText(expectedSecondHistory)
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(secondHistoryMatcher)
        composeTestRule.onNode(secondHistoryMatcher, useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(firstHistoryMatcher)
        composeTestRule.onNode(firstHistoryMatcher, useUnmergedTree = true).assertExists()
    }

    /**
     * Scenario 5:
     * Device hardware: rotate the device (landscape and back) and check that the history is still there.
     */
    @Test
    fun deviceHardware_rotateDevice_landscapeAndBack_preservesHistory() {
        val input = "Good morning!"
        val calculateText = targetContext.getString(R.string.btn_calculate)
        val expectedHistoryEntry = "\"Good morning!\" => 1, 2, 1, 0"
        val historyMatcher = hasTestTag(TestTags.HISTORY_ITEM_TEXT) and hasText(expectedHistoryEntry)

        // 1. Enter text and calculate to generate a history entry
        composeTestRule.onNodeWithTag(TestTags.INPUT_TEXT_FIELD)
            .performTextInput(input)
        composeTestRule.onNodeWithText(calculateText)
            .performClick()

        // Verify history entry is visible in initial portrait mode
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(historyMatcher)
        composeTestRule.onNode(historyMatcher, useUnmergedTree = true)
            .assertIsDisplayed()

        // 2. Rotate device to Landscape safely
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        composeTestRule.waitForIdle()
        // Wait until always-visible top component (the input field) exists after configuration change
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(TestTags.INPUT_TEXT_FIELD)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Scroll LazyColumn to the history item in landscape and verify displayed
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(historyMatcher)
        composeTestRule.onNode(historyMatcher, useUnmergedTree = true)
            .assertIsDisplayed()

        // 4. Rotate device back to Portrait safely
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        composeTestRule.waitForIdle()
        // Wait until always-visible top component (the input field) exists after configuration change
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(TestTags.INPUT_TEXT_FIELD)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 5. Scroll LazyColumn to the history item in portrait and verify displayed
        composeTestRule.onNodeWithTag(TestTags.MAIN_LIST)
            .performScrollToNode(historyMatcher)
        composeTestRule.onNode(historyMatcher, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
