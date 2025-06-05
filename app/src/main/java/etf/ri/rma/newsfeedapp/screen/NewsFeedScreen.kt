package etf.ri.rma.newsfeedapp.screen

import android.annotation.SuppressLint
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

import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.screen.Filter.ParametriF
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*fun mapiranjeZaNewsfeeds(mijenjamo: String): String {
    return when (mijenjamo.lowercase(Locale.ROOT)) {
        "sports" -> "Sport"
        "politics" -> "Politika"
        "science" -> "Nauka"
        "health" -> "Zdravlje"
        "tech" -> "Tehnologija"
        "general" -> "Ostalo"
        else -> mijenjamo
    }
}*/

@SuppressLint("ContextCastToActivity")
@Composable
fun NewsFeedScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val newsDAO = NewsDAO()
    var isLoading by remember { mutableStateOf(false) }
    var newsWithTags by remember { mutableStateOf<List<NewsItem>>(emptyList()) }

    val viewModel: Filter = viewModel(LocalContext.current as ComponentActivity)
    val filters by viewModel.filters

    suspend fun fetchNewsForSelectedCategory(selectedFilterCategory: String) {
        isLoading = true
        try {
            newsWithTags = newsDAO.getNewsWithTags(selectedFilterCategory)
        } catch (e: Exception) {
            newsWithTags = emptyList() //isprazni listu u slucaju greske skroz?
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchNewsForSelectedCategory(filters.category ?: "Sve")
    }

    LaunchedEffect(filters.category) {
        if (filters.category != null) {
            fetchNewsForSelectedCategory(filters.category!!)
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
                        NewsDAO.mapiranjeKat(newsItemCategory) == NewsDAO.mapiranjeKat("science") ||
                                NewsDAO.mapiranjeKat(newsItemCategory) == NewsDAO.mapiranjeKat("tech")
                    }
                    else -> NewsDAO.mapiranjeKat(newsItemCategory) == NewsDAO.mapiranjeKat(filterCategory)
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
                    false
                }
            } != false

            val unwantedWordsMatch = filters.nezeljeneRijeci.none { unwantedWord ->
                newsItem.snippet.contains(unwantedWord, ignoreCase = true) ||
                        newsItem.title.contains(unwantedWord, ignoreCase = true)
            }

            categoryMatches && dateMatches && unwantedWordsMatch
        }

        val sortedAndFilteredNewsList = if (filters.category == "Sve") {
            filteredNewsList.asReversed() // mejntejn originalnu "Sve" logiku (most recent prve)
        } else {
            // Sort by isFeatured (true first), onda by publishedDate (most recent first)
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
