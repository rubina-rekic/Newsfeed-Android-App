package etf.ri.rma.newsfeedapp.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.NewsDAO
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun NewsFeedScreen(navController: NavController? = null) {
    val filters = navController?.currentBackStackEntry?.savedStateHandle
    var savedCategory by remember { mutableStateOf(filters?.get<String>("filters_category") ?: "Sve") }
    var savedDateFrom by remember { mutableStateOf(filters?.get<String>("filters_dateFrom")) }
    var savedDateTo by remember { mutableStateOf(filters?.get<String>("filters_dateTo")) }
    var savedUnwantedWords by remember { mutableStateOf(filters?.get<List<String>>("filters_unwantedWords") ?: emptyList()) }


    var showFilterScreen by rememberSaveable { mutableStateOf(false) }

    val displayedNews = remember { mutableStateListOf<NewsItem>() }
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


    LaunchedEffect(savedCategory, savedDateFrom, savedDateTo, savedUnwantedWords) {
        coroutineScope.launch {
            val newsSourceList = if (savedCategory == "Sve") {
                NewsDAO.getAllStories()
            } else {
                NewsDAO.getTopStoriesByCategory(savedCategory)
            }

            // Filter news
            var filteredResult = newsSourceList

            if (savedUnwantedWords.isNotEmpty()) {
                filteredResult = filteredResult.filter { item ->
                    savedUnwantedWords.none { word ->
                        item.title.contains(word, ignoreCase = true) ||
                                item.snippet.contains(word, ignoreCase = true)
                    }
                }
            }

            if (savedDateFrom != null && savedDateTo != null) {
                val from = runCatching { dateFormat.parse(savedDateFrom!!) }.getOrNull()
                val to = runCatching { dateFormat.parse(savedDateTo!!) }.getOrNull()
                if (from != null && to != null) {
                    filteredResult = filteredResult.filter {
                        runCatching { dateFormat.parse(it.publishedDate) }.getOrNull()
                            ?.let { d -> d in from..to } ?: false
                    }
                }
            }

            displayedNews.clear()
            displayedNews.addAll(filteredResult)
        }
    }

    // Show FilterScreen if needed
    if (showFilterScreen) {
        FilterScreen(
            selectedCategory = savedCategory,
            dateRange = Pair(savedDateFrom, savedDateTo),
            dateRangesByCategory = mapOf(), // Provide actual data
            unwantedWordsByCategory = mapOf(), // Provide actual data
            onApplyFilters = { category, dateRanges, unwantedWords ->
                savedCategory = category
                savedDateFrom = dateRanges[category]?.first
                savedDateTo = dateRanges[category]?.second
                savedUnwantedWords = unwantedWords[category] ?: emptyList()

                filters?.set("filters_category", savedCategory)
                filters?.set("filters_dateFrom", savedDateFrom)
                filters?.set("filters_dateTo", savedDateTo)
                filters?.set("filters_unwantedWords", savedUnwantedWords)

                showFilterScreen = false // Close the filter screen
            },
            onBackPressed = { showFilterScreen = false } // Handle back press
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("NewsFeedApp", modifier = Modifier.testTag("news_header"))

            FilterSection(
                selectedCategory = savedCategory,
                onCategorySelected = { cat ->
                    savedCategory = cat
                    filters?.set("filters_category", cat)
                },
                onMoreFiltersClicked = { showFilterScreen = true } // Activate filters
            )
            Spacer(Modifier.height(16.dp))

            if (displayedNews.isEmpty()) {
                MessageCard("Nema pronađenih vijesti u kategoriji \"$savedCategory\"")
            } else {
                NewsList(
                    newsItems = displayedNews,
                    listState = rememberLazyListState(),
                    selectedCategory = savedCategory,
                    onNewsClick = { id: String -> navController?.navigate("details/$id") }
                )
            }
        }
    }
}