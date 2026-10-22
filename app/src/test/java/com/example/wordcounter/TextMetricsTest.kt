package com.example.wordcounter

import com.example.wordcounter.model.CalculationHistoryItem
import com.example.wordcounter.model.TextMetrics
import com.example.wordcounter.model.TextMetricsResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TextMetrics], [TextMetricsResult], and [CalculationHistoryItem].
 * Since these model classes have zero Android dependencies, these tests run purely on the JVM.
 *
 * Covers 100% of classes, functions, and branches in the model package.
 */
class TextMetricsTest {

    // =========================================================================
    // Existing Tests (Preserved Exactly)
    // =========================================================================

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

    // =========================================================================
    // Extended Tests for TextMetrics
    // =========================================================================

    @Test
    fun textMetrics_defaultConstructor_instantiatesSuccessfully() {
        // Instantiates TextMetrics to achieve 100% class and function coverage on TextMetrics()
        val instance = TextMetrics()
        assertNotNull(instance)
    }

    @Test
    fun analyze_lithuanianLetters_coversAllDiacriticsUpperAndLower() {
        // Testing all Lithuanian diacritic letters: ą, č, ę, ė, į, š, ų, ū, ž (both lower & upper)
        val text = "Ąžuolas Česnakas Ęžys Ėriukas Įvairus Šviesa Ūkas Ųla Žiedas."
        val result = TextMetrics.analyze(text)
        assertEquals(1, result.sentenceCount)
        assertEquals(9, result.wordCount)
        assertEquals(1, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_curlyApostropheAndMultiHyphens_handledAsSingleWord() {
        // Testing Unicode curly apostrophe (’), hyphen (-), and multi-part words
        val text = "Don’t tell the state-of-the-art model o'clock."
        val result = TextMetrics.analyze(text)
        assertEquals(1, result.sentenceCount)
        assertEquals(6, result.wordCount) // Don’t, tell, the, state-of-the-art, model, o'clock
        assertEquals(5, result.punctuationCount) // 3 hyphens in state-of-the-art + 1 apostrophe in o'clock + 1 period
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_variousNumberFormats_handledAccurately() {
        // Numbers with multiple commas/dots (1,000,000 and 3.14159)
        val text = "Items: 1,000,000 in total, cost is 3.14159 each."
        val result = TextMetrics.analyze(text)
        assertEquals(1, result.sentenceCount)
        assertEquals(6, result.wordCount) // Items, in, total, cost, is, each
        assertEquals(2, result.numberCount) // 1,000,000 and 3.14159
        // Punctuation: colon ':', comma ',', period '.' (3 punctuation marks; inner separators in numbers skipped)
        assertEquals(3, result.punctuationCount)
    }

    @Test
    fun analyze_allPunctuationMarks_countedCorrectly() {
        // PUNCTUATION_REGEX includes [.,!?:;'"()\-–—]
        val text = "Hello: world; 'quoted' \"double\" (parentheses) - en–dash em—dash! Really?"
        val result = TextMetrics.analyze(text)
        assertEquals(2, result.sentenceCount) // Delimited by ! and ?
        // Punctuation marks: : ; ' ' " " ( ) - – — ! ?
        assertEquals(13, result.punctuationCount)
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_multipleRepeatedDelimiters_countedAccurately() {
        // E.g. "Really??? Wow!!!"
        val text = "Really??? Wow!!!"
        val result = TextMetrics.analyze(text)
        assertEquals(2, result.sentenceCount)
        assertEquals(2, result.wordCount)
        assertEquals(6, result.punctuationCount) // 3 '?' + 3 '!'
        assertEquals(0, result.numberCount)
    }

    @Test
    fun analyze_onlyPunctuationAndNumbers_noWords() {
        val text = "123, 456.78! (999)"
        val result = TextMetrics.analyze(text)
        assertEquals(2, result.sentenceCount)
        assertEquals(0, result.wordCount)
        assertEquals(3, result.numberCount) // 123, 456.78, 999
        // Punctuation: ',' after 123 (not followed by digit), '!' after 456.78, '(', ')'
        assertEquals(4, result.punctuationCount)
    }

    // =========================================================================
    // Extended Tests for TextMetricsResult (Data Class Coverage)
    // =========================================================================

    @Test
    fun textMetricsResult_propertiesAndDestructuring_workCorrectly() {
        val result = TextMetricsResult(
            sentenceCount = 3,
            wordCount = 15,
            punctuationCount = 5,
            numberCount = 2
        )

        assertEquals(3, result.sentenceCount)
        assertEquals(15, result.wordCount)
        assertEquals(5, result.punctuationCount)
        assertEquals(2, result.numberCount)

        // Destructuring (component1 .. component4)
        val (sentences, words, punctuation, numbers) = result
        assertEquals(3, sentences)
        assertEquals(15, words)
        assertEquals(5, punctuation)
        assertEquals(2, numbers)
    }

    @Test
    fun textMetricsResult_copyAndEqualsAndHashCode_workCorrectly() {
        val original = TextMetricsResult(1, 2, 3, 4)
        val identical = original.copy()
        val modifiedSentence = original.copy(sentenceCount = 10)
        val modifiedWord = original.copy(wordCount = 20)
        val modifiedPunctuation = original.copy(punctuationCount = 30)
        val modifiedNumber = original.copy(numberCount = 40)

        // Equals & HashCode
        assertEquals(original, identical)
        assertEquals(original.hashCode(), identical.hashCode())

        assertNotEquals(original, modifiedSentence)
        assertNotEquals(original, modifiedWord)
        assertNotEquals(original, modifiedPunctuation)
        assertNotEquals(original, modifiedNumber)

        assertFalse(original.equals(null))
        assertFalse(original.equals("some text"))

        // toString
        assertTrue(original.toString().contains("sentenceCount=1"))
        assertTrue(original.toString().contains("wordCount=2"))
        assertTrue(original.toString().contains("punctuationCount=3"))
        assertTrue(original.toString().contains("numberCount=4"))
    }

    // =========================================================================
    // Extended Tests for CalculationHistoryItem (Data Class Coverage)
    // =========================================================================

    @Test
    fun calculationHistoryItem_propertiesAndDestructuring_workCorrectly() {
        val metrics = TextMetricsResult(1, 4, 2, 1)
        val item = CalculationHistoryItem(
            inputText = "Test 123.",
            metrics = metrics
        )

        assertEquals("Test 123.", item.inputText)
        assertEquals(metrics, item.metrics)

        // Destructuring (component1, component2)
        val (text, itemMetrics) = item
        assertEquals("Test 123.", text)
        assertEquals(metrics, itemMetrics)
    }

    @Test
    fun calculationHistoryItem_toFormattedString_formatsWithComplexMetrics() {
        val item = CalculationHistoryItem(
            inputText = "Special test!",
            metrics = TextMetricsResult(sentenceCount = 5, wordCount = 10, punctuationCount = 3, numberCount = 2)
        )
        assertEquals("\"Special test!\" => 5, 10, 3, 2", item.toFormattedString())
    }

    @Test
    fun calculationHistoryItem_copyAndEqualsAndHashCode_workCorrectly() {
        val metrics1 = TextMetricsResult(1, 2, 0, 0)
        val metrics2 = TextMetricsResult(2, 4, 1, 1)

        val item1 = CalculationHistoryItem("Alpha", metrics1)
        val item1Identical = item1.copy()
        val itemWithDiffText = item1.copy(inputText = "Beta")
        val itemWithDiffMetrics = item1.copy(metrics = metrics2)

        assertEquals(item1, item1Identical)
        assertEquals(item1.hashCode(), item1Identical.hashCode())

        assertNotEquals(item1, itemWithDiffText)
        assertNotEquals(item1, itemWithDiffMetrics)

        assertFalse(item1.equals(null))
        assertFalse(item1.equals(metrics1))

        assertTrue(item1.toString().contains("Alpha"))
        assertTrue(item1.toString().contains("sentenceCount=1"))
    }
}
