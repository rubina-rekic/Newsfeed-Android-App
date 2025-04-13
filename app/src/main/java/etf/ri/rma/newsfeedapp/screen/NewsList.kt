package etf.ri.rma.newsfeedapp.screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import etf.ri.rma.newsfeedapp.model.NewsItem
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.platform.testTag

@Composable
fun NewsList(
    newsItems: List<NewsItem>,
    selectedCategory: String,
    listState: LazyListState
) {
    if (newsItems.isEmpty()) {
        MessageCard(message = "Nema dostupnih vijesti u kategoriji \"$selectedCategory\"")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag("news_list"),
            state = listState
        ) {
            items(newsItems, key = { it.id }) { item ->
                if (item.isFeatured) {
                    FeaturedNewsCard(item = item)
                } else {
                    StandardNewsCard(item = item)
                }
            }
        }
    }
}
