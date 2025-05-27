package etf.ri.rma.newsfeedapp.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import etf.ri.rma.newsfeedapp.model.NewsItem
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun NewsList(
    newsItems: List<NewsItem>,
    selectedCategory: String,
    listState: LazyListState,
    onNewsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (newsItems.isEmpty()) {
        MessageCard(message = "Nema dostupnih vijesti u kategoriji \"$selectedCategory\"")
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
                .testTag("news_list"),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(newsItems, key = { it.uuid }) { item ->
                if (item.isFeatured) {
                    FeaturedNewsCard(item = item, onClick = { onNewsClick(item.uuid) })
                } else {
                    StandardNewsCard(item = item, onClick = { onNewsClick(item.uuid) })
                }
            }
        }
    }
}

