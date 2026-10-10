package com.example.wordcounter

import com.example.wordcounter.model.CalculationHistoryItem
import com.example.wordcounter.model.TextMetrics
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TextMetrics and CalculationHistoryItem.
 * Since TextMetrics has zero Android dependencies, these tests run purely on the JVM.
 */
class TextMetricsTest {

    @Test
    fun analyze_emptyInput_returnsAllZeros() {
        val result = TextMetrics.analyze("")
        assertEquals(0, result.sentenceCount)
        assertEquals(0, result.wordCount)
        assertEquals(0, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_blankInput_returnsAllZeros() {
        val result = TextMetrics.analyze("   \t  \n  ")
        assertEquals(0, result.sentenceCount)
        assertEquals(0, result.wordCount)
        assertEquals(0, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_goodMorning_matchesSpecificationExample() {
        // As specified in project requirements: "Good morning" => 1, 2, 0, 0
        val result = TextMetrics.analyze("Good morning")
        assertEquals(1, result.sentenceCount)
        assertEquals(2, result.wordCount)
        assertEquals(0, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_withPunctuation_returnsAccurateCounts() {
        val result = TextMetrics.analyze("Good morning!")
        assertEquals(1, result.sentenceCount)
        assertEquals(2, result.wordCount)
        assertEquals(1, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_textWithNumbersAndPunctuation() {
        // Words: "In", "there", "are", "items" (4)
        // Numbers: "2026", "42" (2)
        // Punctuation: ",", "." (2)
        // Sentences: 1
        val result = TextMetrics.analyze("In 2026, there are 42 items.")
        assertEquals(1, result.sentenceCount)
        assertEquals(4, result.wordCount)
        assertEquals(2, result.punctuationCount)
        assertEquals(2, result.numberCount)
    }

    @Test
    fun analyze_multipleSentencesWithVariousDelimiters() {
        val result = TextMetrics.analyze("First sentence. Second sentence! Third sentence? Fourth sentence")
        assertEquals(4, result.sentenceCount)
        assertEquals(8, result.wordCount)
        assertEquals(3, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_nonEnglishLetters_countAsWholeWords() {
        // Lithuanian letters must not split words
        val result = TextMetrics.analyze("Labas rytas, ačiū!")
        assertEquals(1, result.sentenceCount)
        assertEquals(3, result.wordCount)
        assertEquals(2, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_apostropheAndHyphen_keepOneWord() {
        assertEquals(3, TextMetrics.analyze("I don't know.").wordCount)
        assertEquals(2, TextMetrics.analyze("well-known fact").wordCount)
    }

    @Test
    fun analyze_decimalNumbers_doNotSplitSentencesOrNumbers() {
        val result = TextMetrics.analyze("Pi is 3.14. It is 1,000 times better.")
        assertEquals(2, result.sentenceCount)
        assertEquals(6, result.wordCount)
        assertEquals(2, result.punctuationCount)
        assertEquals(2, result.numberCount)
    }

    @Test
    fun toFormattedString_formatsCorrectly() {
        val metrics = TextMetrics.analyze("Good morning")
        val item = CalculationHistoryItem(
            inputText = "Good morning",
            metrics = metrics
        )

        val formatted = item.toFormattedString()
        assertEquals("\"Good morning\" => 1, 2, 0, 0", formatted)
    }
}
