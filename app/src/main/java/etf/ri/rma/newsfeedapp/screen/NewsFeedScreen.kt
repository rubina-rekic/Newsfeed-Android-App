package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsFeedScreen(navController: NavController? = null) {
    val filters = navController?.currentBackStackEntry?.savedStateHandle
    var savedCategory by remember { mutableStateOf(filters?.get<String>("filters_category") ?: "Sve") }
    var savedDateFrom by remember { mutableStateOf(filters?.get<String>("filters_dateFrom")) }
    var savedDateTo by remember { mutableStateOf(filters?.get<String>("filters_dateTo")) }
    var savedUnwantedWords by remember { mutableStateOf(filters?.get<List<String>>("filters_unwantedWords") ?: emptyList()) }

    var showFilterScreen by rememberSaveable { mutableStateOf(false) }

    // Čuva sve vijesti po kategorijama
    val allFetchedNews = remember { mutableStateMapOf<String, MutableList<NewsItem>>() }

    // Prikazane vijesti
    val featuredNews = remember { mutableStateListOf<NewsItem>() }
    val standardNews = remember { mutableStateListOf<NewsItem>() }

    // Stanje za učitavanje i greške
    var isLoading by remember { mutableStateOf(false) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun refreshDisplayedNews() {
        val allNews = allFetchedNews[savedCategory] ?: emptyList()

        // Filtriranje po nepoželjnim riječima
        var filtered = allNews.filter { item ->
            savedUnwantedWords.none { word ->
                item.title.contains(word, ignoreCase = true) ||
                        item.snippet.contains(word, ignoreCase = true)
            }
        }

        // Filtriranje po datumu
        if (!savedDateFrom.isNullOrEmpty() && !savedDateTo.isNullOrEmpty()) {
            val from = runCatching { dateFormat.parse(savedDateFrom!!) }.getOrNull()
            val to = runCatching { dateFormat.parse(savedDateTo!!) }.getOrNull()
            if (from != null && to != null) {
                filtered = filtered.filter {
                    runCatching { dateFormat.parse(it.publishedDate) }.getOrNull()
                        ?.let { d -> d in from..to } ?: false
                }
            }
        }

        // Reset prikaza
        featuredNews.clear()
        standardNews.clear()

        val featured = filtered.take(3).map { it.copy(isFeatured = true) }
        val standard = filtered.drop(3).map { it.copy(isFeatured = false) }

        featuredNews.addAll(featured)
        standardNews.addAll(standard)
    }

    LaunchedEffect(savedCategory) {
        isLoading = true
        loadingError = null
        try {
            // Kreiramo instancu NewsDAO
            val newsDAO = NewsDAO()

            // Učitavanje vijesti
            val news = if (savedCategory == "Sve") {
                newsDAO.getAllStories() // Pozivanje metode za sve vijesti
            } else {
                newsDAO.getTopStoriesByCategory(savedCategory) // Pozivanje metode za vijesti po kategoriji
            }

            allFetchedNews[savedCategory] = news.toMutableList()
            refreshDisplayedNews()
        } catch (e: Exception) {
            loadingError = "Greška prilikom učitavanja vijesti: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Ponovno filtriranje ako se promijene filteri (datum ili nepoželjne riječi)
    LaunchedEffect(savedDateFrom, savedDateTo, savedUnwantedWords) {
        refreshDisplayedNews()
    }

    if (showFilterScreen) {
        FilterScreen(
            selectedCategory = savedCategory,
            dateRange = Pair(savedDateFrom, savedDateTo),
            dateRangesByCategory = mapOf(), // Dopuniti ako je potrebno
            unwantedWordsByCategory = mapOf(), // Dopuniti ako je potrebno
            onApplyFilters = { category, dateRanges, unwantedWords ->
                savedCategory = category
                savedDateFrom = dateRanges[category]?.first
                savedDateTo = dateRanges[category]?.second
                savedUnwantedWords = unwantedWords[category] ?: emptyList()

                filters?.set("filters_category", savedCategory)
                filters?.set("filters_dateFrom", savedDateFrom)
                filters?.set("filters_dateTo", savedDateTo)
                filters?.set("filters_unwantedWords", savedUnwantedWords)

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
            Text("NewsFeedApp", modifier = Modifier.testTag("news_header"))

            FilterSection(
                selectedCategory = savedCategory,
                onCategorySelected = { cat ->
                    savedCategory = cat
                    filters?.set("filters_category", cat)
                },
                onMoreFiltersClicked = { showFilterScreen = true }
            )

            Spacer(Modifier.height(16.dp))

            // Prikazujemo grešku ako je učitavanje vijesti neuspešno
            if (loadingError != null) {
                MessageCard(loadingError!!)
            } else if (isLoading) {
                // Prikazujemo indikator učitavanja
                MessageCard("Učitavanje vijesti...")
            } else if (featuredNews.isEmpty() && standardNews.isEmpty()) {
                MessageCard("Nema pronađenih vijesti u kategoriji \"$savedCategory\"")
            } else {
                NewsList(
                    newsItems = featuredNews + standardNews,
                    listState = rememberLazyListState(),
                    selectedCategory = savedCategory,
                    onNewsClick = { id: String -> navController?.navigate("details/$id") }
                )
            }
        }
    }
}

