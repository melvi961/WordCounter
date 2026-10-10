package com.example.wordcounter.ui

import androidx.lifecycle.ViewModel
import com.example.wordcounter.model.CalculationHistoryItem
import com.example.wordcounter.model.TextMetrics
import com.example.wordcounter.model.TextMetricsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * UI State for the Word Counter screen.
 */
data class WordCounterUiState(
    val inputText: String = "",
    val isInputError: Boolean = false,
    val currentMetrics: TextMetricsResult? = null,
    val history: List<CalculationHistoryItem> = emptyList()
)

/**
 * ViewModel responsible for managing Word Counter state across UI configuration changes.
 */
class WordCounterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WordCounterUiState())
    val uiState: StateFlow<WordCounterUiState> = _uiState.asStateFlow()

    /**
     * Updates the user's input text and clears the validation error if text is entered.
     */
    fun onInputTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            inputText = newText,
            isInputError = false
        )
    }

    /**
     * Triggered when user taps "Calculate".
     * Validates input:
     * - If empty: shows inline error on the field and aborts.
     * - If valid: calculates metrics using TextMetrics and appends to history.
     */
    fun onCalculateClicked() {
        val currentText = _uiState.value.inputText
        if (currentText.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isInputError = true
            )
            return
        }

        // Calculate metrics using reusable, dependency-free TextMetrics class
        val result = TextMetrics.analyze(currentText)

        // Create new history record
        val historyRecord = CalculationHistoryItem(
            inputText = currentText,
            metrics = result
        )

        // Prepend new history record so latest is at the top
        val updatedHistory = listOf(historyRecord) + _uiState.value.history

        _uiState.value = _uiState.value.copy(
            isInputError = false,
            currentMetrics = result,
            history = updatedHistory
        )
    }

    /**
     * Generates and fills a random sample sentence into the input field.
     */
    fun onGenerateTestSentence(sampleSentences: Array<String>) {
        if (sampleSentences.isEmpty()) return

        val randomIndex = Random.nextInt(sampleSentences.size)
        val selectedSentence = sampleSentences[randomIndex]

        _uiState.value = _uiState.value.copy(
            inputText = selectedSentence,
            isInputError = false
        )
    }
}
