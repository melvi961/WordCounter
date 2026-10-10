# Code Review – Word Counter

Reviewer: Claude (Anthropic), separate AI session from code generation (Antigravity).
Scope: `model/TextMetrics.kt`, `model/CalculationHistoryItem.kt`, `ui/WordCounterViewModel.kt`, `ui/WordCounterScreen.kt`, `res/values/strings.xml`, `TextMetricsTest.kt`.

## Findings (highest priority first)

| # | Priority | Issue | Example |
|---|----------|-------|---------|
| 1 | High | Words are matched with `[A-Za-z]+`, so any non-English letter splits or drops a word, and apostrophes/hyphens split words. | `"ačiū"` → 2 words; `"don't"` → 2 words |
| 2 | High | Decimal points are treated as sentence ends and as number separators. | `"Pi is 3.14."` → 2 sentences, 2 numbers |
| 3 | Medium | Punctuation list is incomplete (`…`, `«»`, `„“`, `[]`, `/`). Could use `\p{P}`. | `"Wait…"` → 0 punctuation |
| 4 | Medium | Abbreviations end a sentence. | `"Dr. Smith came."` → 2 sentences |
| 5 | Medium | Spec example `"Good morning" => 1, 2, 11, 0` does not match the code/test (`… 0, 0`). Needs clarification with the instructor. | — |
| 6 | Low | `uiState.currentMetrics!!` in `WordCounterScreen.kt`; safer to use `?.let { }`. | — |
| 7 | Low | State updates use `_uiState.value = _uiState.value.copy(...)` instead of the atomic `_uiState.update { }`. | — |
| 8 | Low | "Generate test sentence" can return the same sentence twice in a row. | — |
| 9 | Low | Very long inputs are shown in full in the history list (no truncation). | — |

**Good:** metrics logic is in a separate class with no Android dependencies, all UI text is in `strings.xml`, empty input is validated, and unit tests exist.

## Implemented changes (top 2)

**Fix 1 – Unicode words.** `WORD_REGEX` changed to `[\p{L}\p{M}]+(?:['’\-][\p{L}\p{M}]+)*`.

**Fix 2 – Decimal numbers.** A `.` or `,` between two digits no longer ends a sentence or counts as punctuation, and `3.14` / `1,000` count as one number:
- `NUMBER_REGEX` = `\d+(?:[.,]\d+)*`
- `PUNCTUATION_REGEX` = `(?<!\d)[.,]|[.,](?!\d)|[!?:;'"()\-–—]`
- `SENTENCE_DELIMITER_REGEX` = `(?<!\d)[.!?]+|[.!?]+(?!\d)`

Three unit tests were added (`analyze_nonEnglishLetters_countAsWholeWords`, `analyze_apostropheAndHyphen_keepOneWord`, `analyze_decimalNumbers_doNotSplitSentencesOrNumbers`). All existing tests keep their expected values.

| Input | Before | After |
|-------|--------|-------|
| `Labas rytas, ačiū!` | 1, 4, 2, 0 | 1, 3, 2, 0 |
| `I don't know.` | 1, 4, 2, 0 | 1, 3, 2, 0 |
| `Pi is 3.14. It is 1,000 times better.` | 3, 6, 4, 4 | 2, 6, 2, 2 |
