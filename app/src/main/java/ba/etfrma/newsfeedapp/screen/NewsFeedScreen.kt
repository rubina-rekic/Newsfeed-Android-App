package ba.etfrma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ba.etfrma.newsfeedapp.data.NewsData
import ba.etfrma.newsfeedapp.model.NewsItem

@Composable
fun NewsFeedScreen() {
    var selectedCategory by remember { mutableStateOf("All") }
    val allNews = NewsData.getAllNews()
    val filteredNews = if (selectedCategory == "All") {
        allNews
    } else {
        allNews.filter { it.category == selectedCategory }
    }

    val listState = rememberLazyListState()


    LaunchedEffect(selectedCategory) {
        listState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FilterSection(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
        NewsList(newsItems = filteredNews, selectedCategory = selectedCategory, listState = listState)
    }
}
