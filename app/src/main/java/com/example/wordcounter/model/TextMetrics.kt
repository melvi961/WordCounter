package com.example.wordcounter.model

/**
 * Data class encapsulating the four calculated metrics for any given text.
 */
data class TextMetricsResult(
    val sentenceCount: Int,
    val wordCount: Int,
    val punctuationCount: Int,
    val numberCount: Int
)

/**
 * Reusable utility class for text analysis.
 *
 * NOTE: This class has ZERO Android dependencies (pure Kotlin),
 * making it 100% portable, reusable, and easily unit-testable.
 */
class TextMetrics {

    companion object {
        // Regex rules:
        // Words: Unicode letters (any language, e.g. Lithuanian "ačiū"),
        //        apostrophes/hyphens inside a word keep it as one word ("don't", "well-known")
        // Numbers: digits, with decimal/thousand separators kept as one number ("3.14", "1,000")
        // Punctuation: [.,!?:;'"()-–—], but a '.' or ',' between two digits is not counted
        // Sentences: delimited by [.!?]+, except a '.' between two digits ("3.14")
        private val WORD_REGEX = Regex("""[\p{L}\p{M}]+(?:['’\-][\p{L}\p{M}]+)*""")
        private val NUMBER_REGEX = Regex("""\d+(?:[.,]\d+)*""")
        private val PUNCTUATION_REGEX = Regex("""(?<!\d)[.,]|[.,](?!\d)|[!?:;'"()\-–—]""")
        private val SENTENCE_DELIMITER_REGEX = Regex("""(?<!\d)[.!?]+|[.!?]+(?!\d)""")

        /**
         * Analyzes [text] and returns a [TextMetricsResult] containing the counts of:
         * - Sentences
         * - Words
         * - Punctuation marks
         * - Numbers
         *
         * Edge cases:
         * - If [text] is empty or whitespace-only, all metrics are 0.
         * - Sentences are segmented using '.', '!', and '?'. If text has no terminal
         *   punctuation but contains content (e.g. "Good morning"), it counts as 1 sentence.
         */
        fun analyze(text: String): TextMetricsResult {
            if (text.isBlank()) {
                return TextMetricsResult(
                    sentenceCount = 0,
                    wordCount = 0,
                    punctuationCount = 0,
                    numberCount = 0
                )
            }

            // Count words (Unicode letters, so non-English words work too)
            val words = WORD_REGEX.findAll(text).count()

            // Count numbers (decimals like 3.14 count as one number)
            val numbers = NUMBER_REGEX.findAll(text).count()

            // Count punctuation marks (decimal separators are skipped)
            val punctuation = PUNCTUATION_REGEX.findAll(text).count()

            // Count sentences by splitting on [.!?]+ (not inside numbers) and ignoring empty segments
            val sentences = text
                .split(SENTENCE_DELIMITER_REGEX)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .size

            return TextMetricsResult(
                sentenceCount = sentences,
                wordCount = words,
                punctuationCount = punctuation,
                numberCount = numbers
            )
        }
    }
}
