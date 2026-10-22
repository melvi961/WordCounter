package com.example.wordcounter

import com.example.wordcounter.model.CalculationHistoryItem
import com.example.wordcounter.model.TextMetricsResult
import com.example.wordcounter.ui.WordCounterUiState
import com.example.wordcounter.ui.WordCounterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [WordCounterViewModel] and [WordCounterUiState].
 * Runs entirely on the JVM without requiring Android devices or emulators.
 *
 * Covers 100% of classes, functions, and branches in [WordCounterViewModel] and [WordCounterUiState].
 */
class WordCounterViewModelTest {

    private lateinit var viewModel: WordCounterViewModel

    @Before
    fun setUp() {
        viewModel = WordCounterViewModel()
    }

    // =========================================================================
    // WordCounterViewModel Initial State
    // =========================================================================

    @Test
    fun initialState_hasDefaultValues() {
        val state = viewModel.uiState.value
        assertEquals("", state.inputText)
        assertFalse(state.isInputError)
        assertNull(state.currentMetrics)
        assertTrue(state.history.isEmpty())
    }

    // =========================================================================
    // onInputTextChanged Tests
    // =========================================================================

    @Test
    fun onInputTextChanged_updatesInputText() {
        viewModel.onInputTextChanged("Hello Compose")
        assertEquals("Hello Compose", viewModel.uiState.value.inputText)
        assertFalse(viewModel.uiState.value.isInputError)
    }

    @Test
    fun onInputTextChanged_clearsInputError_whenErrorWasPreviouslyActive() {
        // Trigger error by calculating empty input
        viewModel.onCalculateClicked()
        assertTrue(viewModel.uiState.value.isInputError)

        // Typing text must clear the error state
        viewModel.onInputTextChanged("Typing something new")
        assertEquals("Typing something new", viewModel.uiState.value.inputText)
        assertFalse(viewModel.uiState.value.isInputError)
    }

    // =========================================================================
    // onCalculateClicked Tests
    // =========================================================================

    @Test
    fun onCalculateClicked_withEmptyInput_setsErrorAndDoesNotCalculate() {
        viewModel.onInputTextChanged("")
        viewModel.onCalculateClicked()

        val state = viewModel.uiState.value
        assertTrue(state.isInputError)
        assertNull(state.currentMetrics)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun onCalculateClicked_withWhitespaceOnlyInput_setsErrorAndDoesNotCalculate() {
        viewModel.onInputTextChanged("     \t \n  ")
        viewModel.onCalculateClicked()

        val state = viewModel.uiState.value
        assertTrue(state.isInputError)
        assertNull(state.currentMetrics)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun onCalculateClicked_withValidInput_setsMetricsAndClearsError() {
        // First simulate having an error
        viewModel.onCalculateClicked()
        assertTrue(viewModel.uiState.value.isInputError)

        // Now enter valid text and calculate
        viewModel.onInputTextChanged("Good morning")
        viewModel.onCalculateClicked()

        val state = viewModel.uiState.value
        assertFalse(state.isInputError)
        assertNotNull(state.currentMetrics)
        assertEquals(1, state.currentMetrics?.sentenceCount)
        assertEquals(2, state.currentMetrics?.wordCount)
        assertEquals(0, state.currentMetrics?.punctuationCount)
        assertEquals(0, state.currentMetrics?.numberCount)
    }

    @Test
    fun onCalculateClicked_withValidInput_addsHistoryWithNewestFirst() {
        // Calculation 1
        viewModel.onInputTextChanged("First sentence.")
        viewModel.onCalculateClicked()

        // Calculation 2
        viewModel.onInputTextChanged("Second sentence with 2026.")
        viewModel.onCalculateClicked()

        // Calculation 3
        viewModel.onInputTextChanged("Third sentence!")
        viewModel.onCalculateClicked()

        val state = viewModel.uiState.value
        assertEquals(3, state.history.size)

        // Most recent calculation must be at index 0 (newest first)
        assertEquals("Third sentence!", state.history[0].inputText)
        assertEquals("Second sentence with 2026.", state.history[1].inputText)
        assertEquals("First sentence.", state.history[2].inputText)

        // Verify metrics inside newest history item
        assertEquals(1, state.history[0].metrics.sentenceCount)
        assertEquals(2, state.history[0].metrics.wordCount)
        assertEquals(1, state.history[0].metrics.punctuationCount)
        assertEquals(0, state.history[0].metrics.numberCount)
    }

    // =========================================================================
    // onGenerateTestSentence Tests
    // =========================================================================

    @Test
    fun onGenerateTestSentence_withEmptyArray_doesNothing() {
        // Set an existing state with text and an error
        viewModel.onInputTextChanged("Initial")
        viewModel.onCalculateClicked() // Valid calculation
        viewModel.onInputTextChanged("") // Set empty text
        viewModel.onCalculateClicked() // Trigger isInputError = true

        val stateBefore = viewModel.uiState.value
        assertTrue(stateBefore.isInputError)
        assertEquals("", stateBefore.inputText)

        // Calling with an empty array should abort and leave state untouched
        viewModel.onGenerateTestSentence(emptyArray())

        val stateAfter = viewModel.uiState.value
        assertEquals(stateBefore, stateAfter)
    }

    @Test
    fun onGenerateTestSentence_withSingleItemArray_fillsInputAndClearsError() {
        // Trigger error first
        viewModel.onCalculateClicked()
        assertTrue(viewModel.uiState.value.isInputError)

        val sample = "This is a single sample sentence."
        viewModel.onGenerateTestSentence(arrayOf(sample))

        val state = viewModel.uiState.value
        assertEquals(sample, state.inputText)
        assertFalse(state.isInputError)
    }

    @Test
    fun onGenerateTestSentence_withMultipleItemsArray_selectsAnItemAndClearsError() {
        viewModel.onCalculateClicked()
        assertTrue(viewModel.uiState.value.isInputError)

        val samples = arrayOf("Alpha sentence.", "Beta sentence.", "Gamma sentence.")
        viewModel.onGenerateTestSentence(samples)

        val state = viewModel.uiState.value
        assertTrue(samples.contains(state.inputText))
        assertFalse(state.isInputError)
    }

    // =========================================================================
    // WordCounterUiState Data Class Coverage
    // =========================================================================

    @Test
    fun wordCounterUiState_customPropertiesAndDestructuring_workCorrectly() {
        val sampleMetrics = TextMetricsResult(1, 2, 0, 0)
        val historyItem = CalculationHistoryItem("Test", sampleMetrics)
        val historyList = listOf(historyItem)

        val state = WordCounterUiState(
            inputText = "Sample text",
            isInputError = true,
            currentMetrics = sampleMetrics,
            history = historyList
        )

        assertEquals("Sample text", state.inputText)
        assertTrue(state.isInputError)
        assertEquals(sampleMetrics, state.currentMetrics)
        assertEquals(historyList, state.history)

        // Destructuring (component1..component4)
        val (text, isError, metrics, history) = state
        assertEquals("Sample text", text)
        assertTrue(isError)
        assertEquals(sampleMetrics, metrics)
        assertEquals(historyList, history)
    }

    @Test
    fun wordCounterUiState_copyAndEqualsAndHashCode_workCorrectly() {
        val original = WordCounterUiState(
            inputText = "Hello",
            isInputError = false,
            currentMetrics = TextMetricsResult(1, 1, 0, 0),
            history = emptyList()
        )
        val identical = original.copy()
        val diffText = original.copy(inputText = "Different")
        val diffError = original.copy(isInputError = true)
        val diffMetrics = original.copy(currentMetrics = TextMetricsResult(2, 2, 1, 0))
        val diffHistory = original.copy(
            history = listOf(CalculationHistoryItem("Item", TextMetricsResult(1, 1, 0, 0)))
        )

        // Equals & HashCode
        assertEquals(original, identical)
        assertEquals(original.hashCode(), identical.hashCode())

        assertNotEquals(original, diffText)
        assertNotEquals(original, diffError)
        assertNotEquals(original, diffMetrics)
        assertNotEquals(original, diffHistory)

        assertFalse(original.equals(null))
        assertFalse(original.equals("Not a WordCounterUiState"))

        // toString
        assertTrue(original.toString().contains("Hello"))
        assertTrue(original.toString().contains("isInputError=false"))
    }
}
