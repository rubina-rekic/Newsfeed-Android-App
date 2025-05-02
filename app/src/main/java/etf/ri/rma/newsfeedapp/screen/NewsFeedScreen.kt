package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.NewsData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.compareTo
import kotlin.text.category
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag


@Composable
fun NewsFeedScreen(navController: NavController? = null) {
    var showFilterScreen by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("Sve") }
    var dateRangesByCategory by rememberSaveable { mutableStateOf(mapOf<String, Pair<String?, String?>>()) }
    var unwantedWordsByCategory by rememberSaveable { mutableStateOf(mapOf<String, List<String>>()) }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == "Sve") {
            listState.animateScrollToItem(0)
        }
    }
    val allNews = NewsData.getAllNews()
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    val currentUnwantedWords = unwantedWordsByCategory[selectedCategory].orEmpty()
    val currentDateRange = dateRangesByCategory[selectedCategory] ?: (null to null)

    val filteredNews = allNews.filter { news ->
        val isInCategory = selectedCategory == "Sve" || news.category.equals(selectedCategory, ignoreCase = true)

        if (!isInCategory) return@filter false

        val newsDate = try {
            dateFormatter.parse(news.publishedDate)
        } catch (e: Exception) {
            null
        }

        val startDate = currentDateRange.first?.let { dateFormatter.parse(it) }
        val endDate = currentDateRange.second?.let { dateFormatter.parse(it) }

        val isInDateRange = if (
            (selectedCategory == "Sve" || news.category.equals(selectedCategory, ignoreCase = true)) &&
            startDate != null && endDate != null && newsDate != null
        ) {
            compareDates(newsDate, startDate) >= 0 && compareDates(newsDate, endDate) <= 0
        } else true

        val passesUnwantedWordFilter = currentUnwantedWords.none { word ->
            news.title.contains(word, ignoreCase = true)
        }

        isInDateRange && passesUnwantedWordFilter
    }

    if (showFilterScreen) {
        FilterScreen(
            selectedCategory = selectedCategory,
            dateRange = currentDateRange,
            dateRangesByCategory = dateRangesByCategory,
            unwantedWordsByCategory = unwantedWordsByCategory,
            onApplyFilters = { selectedCat, rangesMap, wordsMap ->
                selectedCategory = selectedCat
                dateRangesByCategory = rangesMap
                unwantedWordsByCategory = wordsMap
                showFilterScreen = false
            },
            onBackPressed = { showFilterScreen = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FilterSection(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onMoreFiltersClicked = { showFilterScreen = true }
            )
            NewsList(
                newsItems = filteredNews,
                selectedCategory = selectedCategory,
                listState = listState,
                onNewsClick = { newsId -> navController?.navigate("details/$newsId") },
                modifier = Modifier.testTag("news_list")
            )
        }
    }
}


fun compareDates(date1: Date, date2: Date): Int {
    return date1.compareTo(date2)
}
