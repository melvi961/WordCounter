# Word Counter

A clean, modern Android application built with **Kotlin** and **Jetpack Compose (Material 3)** that analyzes text and counts sentences, words, punctuation marks, and numbers.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [How Metrics are Counted](#how-metrics-are-counted)
  - [Counting Rules & Regular Expressions](#counting-rules--regular-expressions)
  - [Examples](#examples)
- [User Guide (Step-by-Step)](#user-guide-step-by-step)
- [Technical Requirements](#technical-requirements)
- [Building and Running in Android Studio](#building-and-running-in-android-studio)
- [Running Unit Tests](#running-unit-tests)
- [Project Architecture & Structure](#project-architecture--structure)
- [Adding a New Language (Localization)](#adding-a-new-language-localization)

---

## Overview

**Word Counter** is designed to provide immediate, reliable text metrics for any input text. Whether analyzing simple phrases, multi-sentence paragraphs, numbers with decimals, or multi-lingual texts, the app segments and calculates four key text dimensions:

1. **Sentences**
2. **Words**
3. **Punctuation marks**
4. **Numbers**

The core text processing engine is built as an independent, pure Kotlin class (`TextMetrics`) with zero Android framework dependencies, making it portable and easy to test.

---

## Features

- **Calculate**: Instantly evaluates the entered text and displays the results in four color-coded metric cards.
- **Generate Test Sentence**: Picks a random predefined sentence from the app resources (`strings.xml`) and fills it into the input field for quick demonstration and testing.
- **Empty-Input Warning**: Validates text before calculating. If the user taps *Calculate* while the field is empty or contains only whitespace, an inline error alert (*"Please enter text before calculating."*) is displayed, preventing blank calculations.
- **Calculation History (Current Run)**: Keeps an in-memory record of all calculations performed during the current app session. Each entry is formatted strictly according to the project specification:
  ```text
  "Text" => sentences, words, punctuation, numbers
  ```
  New calculations appear at the top of the history list.

---

## How Metrics are Counted

All metric calculations are handled by [`TextMetrics.analyze(text: String)`](app/src/main/java/com/example/wordcounter/model/TextMetrics.kt).

### Counting Rules & Regular Expressions

| Metric | Pattern / Logic | Description |
| :--- | :--- | :--- |
| **Words** | `[\p{L}\p{M}]+(?:['’\-][\p{L}\p{M}]+)*` | Matches sequences of Unicode letters and combining marks (`\p{L}\p{M}`). Supports non-English alphabets (e.g., Lithuanian letters `ą, č, ę, ė, į, š, ų, ū, ž`). Internal apostrophes and hyphens (like `"don't"` or `"well-known"`) keep the word intact as a single word. |
| **Numbers** | `\d+(?:[.,]\d+)*` | Matches digit sequences. Decimal points (`.`) and thousand separators (`,`) between digits are retained as part of a single number (e.g., `3.14` and `1,000` count as **1** number each). |
| **Punctuation** | `(?<!\d)[.,]|[.,](?!\d)|[!?:;'"()\-–—]` | Matches standard punctuation marks (`. , ! ? : ; ' " ( ) - – —`). Periods or commas located between two digits (such as the `.` in `3.14` or `,` in `1,000`) are excluded from the punctuation count. |
| **Sentences** | `(?<!\d)[.!?]+|[.!?]+(?!\d)` | Splits text by sentence terminators (`.`, `!`, `?` or combinations like `...`, `!?`), ignoring blank splits. A period inside a decimal number (`3.14`) does not split a sentence. If the text has no ending punctuation but contains content (e.g., `"Good morning"`), it counts as **1 sentence**. |

> **Note on Blank Input:** If the input is empty or consists only of whitespace, all four counts evaluate to `0`.

---

### Examples

| Input Text | Sentences | Words | Punctuation | Numbers | Formatted History Output | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- | :--- |
| `Good morning` | 1 | 2 | 0 | 0 | `"Good morning" => 1, 2, 0, 0` | Baseline test: no terminal punctuation still counts as 1 sentence. |
| `Good morning!` | 1 | 2 | 1 | 0 | `"Good morning!" => 1, 2, 1, 0` | Exclamation mark adds 1 punctuation. |
| `Labas rytas, ačiū!` | 1 | 3 | 2 | 0 | `"Labas rytas, ačiū!" => 1, 3, 2, 0` | Non-English word (`ačiū`) with Lithuanian letters counted as a complete word. |
| `Pi is 3.14. It is 1,000 times better.` | 2 | 6 | 2 | 2 | `"Pi is 3.14. It is 1,000 times better." => 2, 6, 2, 2` | `3.14` and `1,000` count as 1 number each; internal separators do not split sentences or add punctuation. |
| `In 2026, there are 42 items.` | 1 | 4 | 2 | 2 | `"In 2026, there are 42 items." => 1, 4, 2, 2` | Counts 2 numbers (`2026`, `42`) and 2 punctuation marks (`,`, `.`). |
| `I don't know.` | 1 | 3 | 1 | 0 | `"I don't know." => 1, 3, 1, 0` | Apostrophe inside `don't` does not split the word. |

---

## User Guide (Step-by-Step)

1. **Launch the App**: Open **Word Counter** on your Android device or emulator.
2. **Enter or Generate Text**:
   - Type or paste your desired text into the **Text Input** field.
   - Alternatively, tap the **Generate test sentence** button to randomly populate the field with a sample phrase.
3. **Calculate**:
   - Tap the **Calculate** button.
   - If the field is empty, a red error notice appears: *"Please enter text before calculating."*
   - Once valid text is provided, the error clears immediately.
4. **View Metrics**:
   - Look at the **Analysis Results** section directly below the input card to view the counts:
     - **Sentences** (blue card with list icon)
     - **Words** (blue card with text icon)
     - **Punctuation** (blue card with sparkle icon)
     - **Numbers** (blue card with digits icon)
5. **Check History**:
   - Scroll down to the **Calculation History** section.
   - Every calculation from your current session is saved in order (newest first) in the exact format:
     `"Your input text" => Sentences, Words, Punctuation, Numbers`

---

## Technical Requirements

- **Operating System:** Android 7.0 (Nougat, API Level 24) or higher
- **Target / Compile SDK:** Android 14 (API Level 34)
- **Language:** Kotlin 1.9.24
- **UI Framework:** Jetpack Compose (BOM `2024.06.00`) with Material 3
- **JDK:** Java 17
- **Build Tool:** Gradle 8.7 with Android Gradle Plugin (AGP) 8.4.2

---

## Building and Running in Android Studio

1. **Open Project**:
   - Launch **Android Studio** (Hedgehog, Koala, Ladybug, or newer recommended).
   - Select **File** > **Open...** and browse to the root `WordCounter` folder.
2. **Sync Project**:
   - Allow Android Studio to download dependencies and sync Gradle automatically.
   - If prompted, ensure the Gradle JDK in **Settings / Preferences > Build, Execution, Deployment > Build Tools > Gradle** is configured to **JDK 17**.
3. **Set Up a Device**:
   - Start an Android Emulator with **API 24+** (e.g., Pixel 7 running API 34), or connect a physical Android device with **USB Debugging** enabled.
4. **Run**:
   - Select the `app` configuration in the top toolbar.
   - Click the green **Run** button (`Shift + F10` on Windows/Linux, `Control + R` on macOS).

---

## Running Unit Tests

The text analysis logic resides in a pure Kotlin class with zero Android dependencies, allowing the unit test suite to run rapidly directly on the JVM.

### From Android Studio

- In the Project view, navigate to:
  ```text
  app/src/test/java/com/example/wordcounter/TextMetricsTest.kt
  ```
- Right-click `TextMetricsTest.kt` and select **Run 'TextMetricsTest'**.
- Alternatively, open the file and click the green play icon next to `class TextMetricsTest` in the editor gutter.

### From the Command Line / Terminal

Run the Gradle test task from the root of the project:

- **Windows (PowerShell / Command Prompt):**
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
- **macOS / Linux:**
  ```bash
  ./gradlew testDebugUnitTest
  ```

HTML test reports will be generated at:
```text
app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Project Architecture & Structure

The project follows standard Android architecture guidelines with a unidirectional data flow (UDF) using ViewModel and Kotlin `StateFlow`.

```text
WordCounter/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/wordcounter/
│   │   │   │   ├── MainActivity.kt               # Single Activity host with edge-to-edge support
│   │   │   │   ├── model/
│   │   │   │   │   ├── TextMetrics.kt            # Reusable text metrics calculation engine (Zero Android dependencies)
│   │   │   │   │   └── CalculationHistoryItem.kt # Data model formatting items into "Text" => s, w, p, n
│   │   │   │   └── ui/
│   │   │   │       ├── WordCounterViewModel.kt   # Manages UI state, input validation, and history list
│   │   │   │       ├── WordCounterScreen.kt      # Jetpack Compose UI (Scaffold, Cards, LazyColumn)
│   │   │   │       └── theme/                    # Material 3 Theme, Color, and Type definitions
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml               # All user-facing strings & sample sentence array
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       └── ...
│   │   └── test/java/com/example/wordcounter/
│   │       └── TextMetricsTest.kt                # JVM unit test suite covering metrics, regex, and formatting
│   ├── build.gradle.kts                          # App-level build configuration (minSdk 24, Compose, etc.)
│   └── ...
├── build.gradle.kts                              # Root build script
├── settings.gradle.kts                           # Module and plugin management
├── CODE_REVIEW.md                                # Code review summary and implemented regex fixes
└── README.md                                     # Project documentation
```

### Highlights of Reusable Components

- **`TextMetrics.kt`**:
  - Implemented as a standalone class with pure Kotlin functions (`TextMetrics.analyze(text)` returning `TextMetricsResult`).
  - Contains **no Android SDK imports** (`android.*`).
  - Can be copied and reused as-is in any Kotlin Multiplatform, desktop (Compose Multiplatform), CLI, or backend (Ktor, Spring Boot) application.

---

## Adding a New Language (Localization)

All user-visible text labels, button captions, error messages, and sample sentences are stored in [`strings.xml`](app/src/main/res/values/strings.xml). The app can easily be localized into any language using standard Android resource qualifiers.

### Step-by-Step Instructions

1. **Create the Locale Folder**:
   Under `app/src/main/res/`, create a new folder named `values-<language_code>`.
   - Examples:
     - Lithuanian: `values-lt`
     - Spanish: `values-es`
     - German: `values-de`
     - French: `values-fr`

2. **Create the Localized `strings.xml`**:
   Create a new file `app/src/main/res/values-<language_code>/strings.xml`.

3. **Translate the String Resources**:
   Copy the keys from the default `values/strings.xml` and provide the translated strings. For example, for Lithuanian (`values-lt/strings.xml`):

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="app_name">Žodžių skaičiuoklė</string>
       <string name="app_subtitle">Teksto analizė sakiniams, žodžiams, skyrybos ženklams ir skaičiams</string>

       <!-- Įvestis -->
       <string name="input_label">Teksto įvestis</string>
       <string name="input_placeholder">Įveskite arba įklijuokite tekstą analizei…</string>
       <string name="error_empty_input">Prieš skaičiuodami įveskite tekstą.</string>

       <!-- Veiksmai -->
       <string name="btn_calculate">Skaičiuoti</string>
       <string name="btn_generate_test">Sukurti bandomąjį sakinį</string>

       <!-- Rezultatai -->
       <string name="section_results">Analizės rezultatai</string>
       <string name="label_sentences">Sakiniai</string>
       <string name="label_words">Žodžiai</string>
       <string name="label_punctuation">Skyrybos ženklai</string>
       <string name="label_numbers">Skaičiai</string>

       <!-- Istorija -->
       <string name="section_history">Skaičiavimų istorija</string>
       <string name="history_format_legend">Formatas: \"Tekstas\" => Sakiniai, Žodžiai, Skyrybos ženklai, Skaičiai</string>
       <string name="empty_history">Šiame paleidime skaičiavimų dar nėra. Įveskite tekstą ir spauskite Skaičiuoti.</string>

       <!-- Bandomieji sakiniai -->
       <string-array name="sample_sentences">
           <item>Labas rytas! Kaip šiandien sekasi?</item>
           <item>2026 metais technologijos vystosi greičiau nei bet kada anksčiau.</item>
           <item>Greita ruda lapė peršoka per 10 tingių šunų.</item>
       </string-array>
   </resources>
   ```

4. **Using Android Studio Translations Editor (Alternative GUI Method)**:
   - In Android Studio, open `app/src/main/res/values/strings.xml`.
   - In the top-right corner of the editor, click **Open editor** (Translations Editor).
   - Click the globe icon (**Add Locale**) in the toolbar, select your desired language/region, and fill in the translated values in the table.

5. **Test the New Language**:
   - Change your emulator or device system language in **Settings > System > Languages & input**.
   - Launch **Word Counter**; Android automatically displays the interface in the selected language.
