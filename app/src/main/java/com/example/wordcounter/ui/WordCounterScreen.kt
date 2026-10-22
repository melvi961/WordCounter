package com.example.wordcounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordcounter.R
import com.example.wordcounter.model.CalculationHistoryItem
import com.example.wordcounter.model.TextMetricsResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordCounterScreen(
    modifier: Modifier = Modifier,
    viewModel: WordCounterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sampleSentences = stringArrayResource(id = R.array.sample_sentences)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(id = R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag(TestTags.MAIN_LIST),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacing at top
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Input Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.INPUT_TEXT_FIELD),
                            label = { Text(text = stringResource(id = R.string.input_label)) },
                            placeholder = { Text(text = stringResource(id = R.string.input_placeholder)) },
                            isError = uiState.isInputError,
                            supportingText = {
                                if (uiState.isInputError) {
                                    Text(
                                        text = stringResource(id = R.string.error_empty_input),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.testTag(TestTags.ERROR_TEXT)
                                    )
                                }
                            },
                            minLines = 3,
                            maxLines = 6
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.onCalculateClicked() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(TestTags.BTN_CALCULATE)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(id = R.string.btn_calculate))
                            }

                            OutlinedButton(
                                onClick = { viewModel.onGenerateTestSentence(sampleSentences) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(TestTags.BTN_GENERATE_TEST)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(id = R.string.btn_generate_test),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Metrics Results Section
            if (uiState.currentMetrics != null) {
                item {
                    Text(
                        text = stringResource(id = R.string.section_results),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    MetricsGrid(metrics = uiState.currentMetrics!!)
                }
            }

            // 3. History Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.section_history),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.history_format_legend),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.HISTORY_EMPTY),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.empty_history),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.history) { historyItem ->
                    HistoryCard(item = historyItem)
                }
            }

            // Bottom padding spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * 2x2 grid displaying the four required metrics:
 * Sentences, Words, Punctuation Marks, and Numbers.
 */
@Composable
private fun MetricsGrid(metrics: TextMetricsResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.METRICS_GRID),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = stringResource(id = R.string.label_sentences),
                value = metrics.sentenceCount,
                icon = Icons.Default.FormatListNumbered,
                modifier = Modifier.weight(1f),
                cardTag = TestTags.METRIC_CARD_SENTENCES,
                valueTag = TestTags.METRIC_VALUE_SENTENCES
            )
            MetricCard(
                title = stringResource(id = R.string.label_words),
                value = metrics.wordCount,
                icon = Icons.Default.TextFields,
                modifier = Modifier.weight(1f),
                cardTag = TestTags.METRIC_CARD_WORDS,
                valueTag = TestTags.METRIC_VALUE_WORDS
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = stringResource(id = R.string.label_punctuation),
                value = metrics.punctuationCount,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.weight(1f),
                cardTag = TestTags.METRIC_CARD_PUNCTUATION,
                valueTag = TestTags.METRIC_VALUE_PUNCTUATION
            )
            MetricCard(
                title = stringResource(id = R.string.label_numbers),
                value = metrics.numberCount,
                icon = Icons.Default.Numbers,
                modifier = Modifier.weight(1f),
                cardTag = TestTags.METRIC_CARD_NUMBERS,
                valueTag = TestTags.METRIC_VALUE_NUMBERS
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    cardTag: String? = null,
    valueTag: String? = null
) {
    Card(
        modifier = modifier
            .then(if (cardTag != null) Modifier.testTag(cardTag) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = if (valueTag != null) Modifier.testTag(valueTag) else Modifier
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Strictly read-only card rendering a historical calculation entry.
 * Formatted like: "Good morning" => 1, 2, 0, 0
 */
@Composable
private fun HistoryCard(item: CalculationHistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.HISTORY_ITEM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Required specification format: "Text" => 1, 2, 0, 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp)
            ) {
                Text(
                    text = item.toFormattedString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(TestTags.HISTORY_ITEM_TEXT)
                )
            }
        }
    }
}
