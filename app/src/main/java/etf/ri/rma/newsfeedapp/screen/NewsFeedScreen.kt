package etf.ri.rma.newsfeedapp.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

 // Make sure this is present and correct

import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.screen.Filter.ParametriF
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("ContextCastToActivity")
@Composable
fun NewsFeedScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    // This should be remembered to ensure the same instance of NewsDAO is used
    val newsDAO = remember { NewsDAO(applicationContext) }

    val viewModel: Filter = viewModel(context as ComponentActivity)
    val filters by viewModel.filters

    // --- CORRECT WAY TO COLLECT FLOW FROM A SUSPEND FUNCTION ---
    // Instead of calling the suspend function directly within remember,
    // we use LaunchedEffect to trigger the suspend function and then
    // collect the resulting Flow.

    // State to hold the collected news items
    val newsWithTags = remember { mutableStateListOf<NewsItem>() }
    // State to track loading
    var isLoading by remember { mutableStateOf(true) }

    // Use LaunchedEffect to launch a coroutine when filters.category changes
    // This coroutine will call the suspend function and then collect from the Flow
    LaunchedEffect(filters.category) {
        Log.d("NewsFeedScreen", "LaunchedEffect triggered for category: ${filters.category}")
        isLoading = true // Start loading when category changes

        // Call the suspend function to get the Flow
        val currentCategory = filters.category ?: "Sve"
        newsDAO.getNewsWithTags(currentCategory)
            .collect { fetchedList ->
                // This 'collect' block will be executed whenever the Flow emits new data
                newsWithTags.clear()
                newsWithTags.addAll(fetchedList)
                isLoading = false // Stop loading once data is received
                Log.d("NewsFeedScreen", "Collected ${fetchedList.size} news items for category: $currentCategory")
            }
    }


    // --- The rest of your UI logic remains the same ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { testTag = "news_feed_screen" }
    ) {
        FilterSection(
            selectedCategory = filters.category ?: "Sve",
            onCategorySelected = { selectedCategory ->
                viewModel.update(
                    ParametriF(
                        category = selectedCategory,
                        dateRange = filters.dateRange,
                        nezeljeneRijeci = filters.nezeljeneRijeci
                    )
                )
            },
            onMoreFiltersClicked = {
                navController.navigate("filters")
            }
        )

        val filteredNewsList = newsWithTags.filter { newsItem ->
            val categoryMatches = if (filters.category == null || filters.category == "Sve") {
                true
            } else {
                val filterCategory = filters.category!!.lowercase(Locale.ROOT)
                val newsItemCategory = newsItem.category.lowercase(Locale.ROOT)

                when (filterCategory) {
                    "nauka/tehnologija" -> {
                        newsDAO.mapiranjeKat(newsItemCategory) == newsDAO.mapiranjeKat("science") ||
                                newsDAO.mapiranjeKat(newsItemCategory) == newsDAO.mapiranjeKat("tech")
                    }
                    else -> newsDAO.mapiranjeKat(newsItemCategory) == newsDAO.mapiranjeKat(filterCategory)
                }
            }

            val dateMatches = filters.dateRange?.let { (startStr, endStr) ->
                try {
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val start = LocalDate.parse(startStr, formatter)
                    val end = LocalDate.parse(endStr, formatter)
                    val newsDate = LocalDate.parse(newsItem.publishedDate, formatter)
                    !newsDate.isBefore(start) && !newsDate.isAfter(end)
                } catch (e: Exception) {
                    Log.e("NewsFeedScreen", "Greška pri parsiranju datuma: ${e.message}", e)
                    false
                }
            } ?: true

            val unwantedWordsMatch = filters.nezeljeneRijeci.none { unwantedWord ->
                newsItem.snippet.contains(unwantedWord, ignoreCase = true) ||
                        newsItem.title.contains(unwantedWord, ignoreCase = true)
            }

            categoryMatches && dateMatches && unwantedWordsMatch
        }

        val sortedAndFilteredNewsList = if (filters.category == "Sve") {
            filteredNewsList.asReversed()
        } else {
            filteredNewsList
                .sortedWith(compareByDescending<NewsItem> { it.isFeatured }
                    .thenByDescending {
                        try {
                            LocalDate.parse(it.publishedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        } catch (e: Exception) {
                            LocalDate.MIN
                        }
                    })
        }

        if (isLoading && sortedAndFilteredNewsList.isEmpty()) { // Show loading only if loading AND list is empty
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (sortedAndFilteredNewsList.isEmpty() && !isLoading) { // Show "No news" if not loading AND list is empty
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nema pronađenih vijesti.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            NewsList(
                newsList = sortedAndFilteredNewsList,
                category = filters.category ?: "Sve",
                navController = navController
            )
        }
    }
}