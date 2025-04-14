package etf.ri.rma.newsfeedapp.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.data.NewsData

@Composable
fun NewsFeedScreen() {
    val allNews = NewsData.getAllNews()
    if (allNews.isEmpty()) {

        MessageCard(message = "Nema dostupnih vijesti!")
    } else {

        var selectedCategory by remember { mutableStateOf("Sve") }
        val filteredNews = if (selectedCategory == "Sve") {
            allNews
        } else {
            allNews.filter { it.category == selectedCategory }
        }

        val listState = rememberLazyListState()
        LaunchedEffect(selectedCategory) {
            listState.scrollToItem(0)
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            FilterSection(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
            NewsList(newsItems = filteredNews, selectedCategory = selectedCategory, listState = listState)
        }
    }
}

