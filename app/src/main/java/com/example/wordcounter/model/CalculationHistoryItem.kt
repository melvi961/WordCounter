package com.example.wordcounter.model

/**
 * Represents a single calculation record in the history list.
 *
 * @param inputText The exact input text calculated.
 * @param metrics The calculated sentence, word, punctuation, and number counts.
 */
data class CalculationHistoryItem(
    val inputText: String,
    val metrics: TextMetricsResult
) {
    /**
     * Formats the calculation entry according to the required specification:
     * "Good morning" => 1, 2, 0, 0
     * (Text => Sentences, Words, Punctuation, Numbers)
     */
    fun toFormattedString(): String {
        return "\"$inputText\" => ${metrics.sentenceCount}, ${metrics.wordCount}, ${metrics.punctuationCount}, ${metrics.numberCount}"
    }
}
