package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.layout.*
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

@Composable
fun NewsFeedScreen(navController: NavController?=null) {
    var showFilterScreen by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Sve") }
    var dateRange by remember { mutableStateOf<Pair<String?, String?>>(null to null) }
    var unwantedWordsByCategory by remember { mutableStateOf(mapOf<String, List<String>>()) }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == "Sve") {
            listState.animateScrollToItem(0)
        }
    }
    val allNews = NewsData.getAllNews()
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    val currentUnwantedWords = unwantedWordsByCategory[selectedCategory].orEmpty()

    val filteredNews = allNews.filter { news ->
        val newsDate = try {
            dateFormatter.parse(news.publishedDate)
        } catch (e: Exception) {
            null
        }
        val startDate = dateRange.first?.let { dateFormatter.parse(it) }
        val endDate = dateRange.second?.let { dateFormatter.parse(it) }

        val isInCategory = selectedCategory == "Sve" || news.category.equals(selectedCategory, ignoreCase = true)
        val isInDateRange = if (startDate != null && endDate != null && newsDate != null) {
            compareDates(newsDate, startDate) >= 0 && compareDates(newsDate, endDate) <= 0
        } else true

        val passesUnwantedWordFilter = if (selectedCategory != "Sve") {
            currentUnwantedWords.none { word -> news.title.contains(word, ignoreCase = true) }
        } else true

        isInCategory && isInDateRange && passesUnwantedWordFilter
    }

    if (showFilterScreen) {
        FilterScreen(
            selectedCategory = selectedCategory,
            dateRange = dateRange,
            unwantedWordsByCategory = unwantedWordsByCategory,
            onApplyFilters = { selectedCat, range, wordsMap ->
                selectedCategory = selectedCat
                dateRange = range
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
                onNewsClick = { newsId -> navController?.navigate("details/$newsId") }

            )
        }
    }
}


fun compareDates(date1: Date, date2: Date): Int {
    return date1.compareTo(date2)
}
