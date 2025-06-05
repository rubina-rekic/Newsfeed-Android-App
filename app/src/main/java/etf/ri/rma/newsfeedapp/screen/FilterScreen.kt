package etf.ri.rma.newsfeedapp.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import java.time.LocalDate

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*

import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.lifecycle.viewmodel.compose.viewModel
import etf.ri.rma.newsfeedapp.screen.Filter.ParametriF


@SuppressLint("ContextCastToActivity")
@Composable
fun FilterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val viewModel: Filter = viewModel(LocalContext.current as ComponentActivity)
    val currFilters by viewModel.filters

    var showDateRangeDialog by remember { mutableStateOf(false) }

    var dateRangePicked by remember {
        mutableStateOf<Pair<LocalDate?, LocalDate?>?>(
            currFilters.dateRange?.let { (startStr, endStr) ->
                try {
                    LocalDate.parse(startStr, DateTimeFormatter.ofPattern("dd-MM-yyyy")) to
                            LocalDate.parse(endStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                } catch (e: Exception) {
                    println("Neocekivan izuzetak: ${e.message}")
                    null
                }
            }
        )
    }

    val validCategoriesList = listOf(
        "Sve", "Politika", "Sport", "Nauka", "Zdravlje", "Tehnologija", "Zdravlje"
    )

    var selectedCategory: String? by remember { mutableStateOf(currFilters.category) }
    var nezeljeneRijeci by remember { mutableStateOf(currFilters.nezeljeneRijeci.toMutableList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { testTag = "filter_screen" }
            .padding(16.dp)
    ) {
        Text("Kategorije", style = MaterialTheme.typography.titleMedium)
        categoryFilterChips(
            categories = validCategoriesList,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Opseg datuma", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val displayText = dateRangePicked?.let { (start, end) ->
                if (start != null && end != null)
                    "${start.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))};${end.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}"
                else "Nije odabran opseg"
            } ?: "Nije odabran opseg"

            Text(
                text = displayText,
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "filter_daterange_display" }
            )

            Button(
                onClick = { showDateRangeDialog = true },
                modifier = Modifier.testTag("filter_daterange_button")
            ) {
                Text("Odaberi opseg")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Nepoželjne riječi", style = MaterialTheme.typography.titleMedium)
        var unwantedInput by remember { mutableStateOf("") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = unwantedInput,
                onValueChange = { unwantedInput = it },
                label = { Text("Unesite rijec: ") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_unwanted_input")
            )
            Button(
                onClick = {
                    val word = unwantedInput.trim()
                    if (word.isNotBlank() && nezeljeneRijeci.none { it.equals(word, ignoreCase = true) }) {
                        nezeljeneRijeci = (nezeljeneRijeci + word).toMutableList()
                        unwantedInput = ""
                    }
                },
                modifier = Modifier.testTag("filter_unwanted_add_button")
            ) {
                Text("Dodaj")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 150.dp)
                .testTag("filter_unwanted_list")
        ) {
            items(nezeljeneRijeci) { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Nazad"
            OutlinedButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_back_button") //  test tag
            ) {
                Text("Nazad")
            }

            // "Primijeni filtere" button
            Button(
                onClick = {
                    val formattedRange = dateRangePicked?.let { (start, end) ->
                        if (start != null && end != null) {
                            start.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) to
                                    end.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        } else null
                    }

                    viewModel.update(
                        ParametriF(
                            category = selectedCategory,
                            dateRange = formattedRange,
                            nezeljeneRijeci = nezeljeneRijeci
                        )
                    )

                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .testTag("filter_apply_button")
                    .weight(1f) // Gives it equal weight with the back button
            ) {
                Text("Primijeni filtere")
            }
        }
    }

    if (showDateRangeDialog) {
        DateRangePickerDialog(
            onDismissRequest = { showDateRangeDialog = false },
            onConfirm = { start, end ->
                dateRangePicked = start to end
                showDateRangeDialog = false
            }
        )
    }
}

@Composable
fun categoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category ||
                    (category == "Sve" && selectedCategory == null)
            val testTag = when (category) {
                "Politika" -> "filter_chip_pol"
                "Sport" -> "filter_chip_spo"
                "Nauka" -> "filter_chip_sci"
                "Zdravlje" -> "filter_chip_hea"
                "Tehnologija" -> "filter_chip_tech"
                else -> "filter_chip_all"
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (category == "Sve") null else category
                    onCategorySelected(newSelection)
                },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit
) {
    val datePickerState = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                val startDateMillis = datePickerState.selectedStartDateMillis
                val endDateMillis = datePickerState.selectedEndDateMillis

                val startDate = startDateMillis?.let {
                    LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24))
                }
                val endDate = endDateMillis?.let {
                    LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24))
                }

                onConfirm(startDate, endDate)
            }) {
                Text("Potvrdi")
            }
        }
    ) {
        DateRangePicker(state = datePickerState)
    }
}