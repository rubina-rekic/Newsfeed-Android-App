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

    val newsDAO = remember { NewsDAO(applicationContext) }

    val viewModel: Filter = viewModel(context as ComponentActivity)
    val filters by viewModel.filters


    val newsWithTags = remember { mutableStateListOf<NewsItem>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(filters.category) {
        Log.d("NewsFeedScreen", "LaunchedEffect triggered for category: ${filters.category}")
        isLoading = true

        val currentCategory = filters.category ?: "Sve"
        newsDAO.getNewsWithTags(currentCategory)
            .collect { fetchedList ->
                newsWithTags.clear()
                newsWithTags.addAll(fetchedList)
                isLoading = false
                Log.d("NewsFeedScreen", "Collected ${fetchedList.size} news items for category: $currentCategory")
            }
    }
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

        // Sortiranje se sada dešava uglavnom unutar NewsDAO ili Flow-a.
        // Ovdje više ne trebamo složeno sortiranje.
        val finalNewsList = filteredNewsList

        if (isLoading && finalNewsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (finalNewsList.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nema pronađenih vijesti.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            NewsList(
                newsList = finalNewsList,
                category = filters.category ?: "Sve",
                navController = navController
            )
        }
    }
}