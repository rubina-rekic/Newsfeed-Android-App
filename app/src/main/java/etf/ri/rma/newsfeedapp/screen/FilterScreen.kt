package etf.ri.rma.newsfeedapp.screen

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items


import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import etf.ri.rma.newsfeedapp.model.NewsItem


fun String.toDate(format: String = "dd-MM-yyyy"): Date? {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun FilterScreen(
    selectedCategory: String,
    dateRange: Pair<String?, String?>,
    unwantedWordsByCategory: Map<String, List<String>>,
    onApplyFilters: (String, Pair<String?, String?>, Map<String, List<String>>) -> Unit,
    onBackPressed: () -> Unit
) {
    var currentCategory by remember { mutableStateOf(selectedCategory) }
    var currentDateRange by remember { mutableStateOf(dateRange) }
    var currentUnwantedWordsMap by remember { mutableStateOf(unwantedWordsByCategory.toMutableMap()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var newUnwantedWord by remember { mutableStateOf("") }

    val currentWords = currentUnwantedWordsMap[currentCategory].orEmpty()

    BackHandler { onBackPressed() }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { startDate, endDate ->
                currentDateRange = startDate to endDate
                showDatePicker = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kategorije", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Sve", "Politika", "Sport", "Nauka/tehnologija", "Zdravlje")) { category ->
                FilterChip(
                    selected = currentCategory == category,
                    onClick = { currentCategory = category },
                    label = { Text(category) },
                    modifier = Modifier.testTag("filter_chip_${category.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Opseg datuma", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (currentDateRange.first != null && currentDateRange.second != null)
                    "${currentDateRange.first};${currentDateRange.second}"
                else "Nije odabran opseg",
                modifier = Modifier.testTag("filter_daterange_display")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("filter_daterange_button")
            ) {
                Text("Odaberi opseg")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Nepoželjne riječi", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newUnwantedWord,
                onValueChange = { newUnwantedWord = it },
                placeholder = { Text("Unesite riječ") },
                modifier = Modifier.weight(1f).testTag("filter_unwanted_input"),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newUnwantedWord.isNotBlank() && !currentWords.any { it.equals(newUnwantedWord, ignoreCase = true) }) {
                        val updated = currentWords + newUnwantedWord
                        currentUnwantedWordsMap[currentCategory] = updated
                        newUnwantedWord = ""
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newUnwantedWord.isNotBlank() && !currentWords.any { it.equals(newUnwantedWord, ignoreCase = true) }) {
                        val updated = currentWords + newUnwantedWord
                        currentUnwantedWordsMap[currentCategory] = updated
                        newUnwantedWord = ""
                    }
                },
                modifier = Modifier.testTag("filter_unwanted_add_button")
            ) {
                Text("Dodaj")
            }
        }

        LazyColumn(modifier = Modifier.testTag("filter_unwanted_list")) {
            items(currentWords.size) { index ->
                Text(currentWords[index])
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onApplyFilters(currentCategory, currentDateRange, currentUnwantedWordsMap)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("filter_apply_button")
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Primijeni filtere")
        }
    }
}


fun filterNewsByDate(newsList: List<NewsItem>, startDate: String?, endDate: String?): List<NewsItem> {
    val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val start = startDate?.toDate() ?: Date(0)
    val end = endDate?.toDate() ?: Date()

    return newsList.filter {
        val newsDate = it.publishedDate.toDate()
        newsDate != null && newsDate in start..end
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String, String) -> Unit
) {
    val datePickerState = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val startDate = datePickerState.selectedStartDateMillis?.let {
                    formatter.format(Date(it))
                }
                val endDate = datePickerState.selectedEndDateMillis?.let {
                    formatter.format(Date(it))
                }
                if (startDate != null && endDate != null) {
                    onDateSelected(startDate, endDate)
                }
            }) {
                Text("Potvrdi")
            }
        }
    ) {
        DateRangePicker(state = datePickerState)
    }
}
