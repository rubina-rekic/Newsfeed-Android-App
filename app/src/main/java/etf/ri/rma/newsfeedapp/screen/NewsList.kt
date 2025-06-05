package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController


@Composable
fun NewsList(
    newsList: List<NewsItem>,
    category: String,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("news_list")
    ) {
        if (newsList.isEmpty()) {
            item {
                MessageCard(category)
            }
        } else {
            items(newsList) { news ->
                val navigateToDetails: (String) -> Unit = { articleId ->
                    navController.navigate("details/$articleId")
                }

                if (category == "Sve") {
                   //uvijek standrad newscard za sve kategoriju <33
                    StandardNewsCard(news, onClick = { navigateToDetails(news.uuid) })
                } else {
                    if (news.isFeatured) {
                        FeaturedNewsCard(news, onClick = { navigateToDetails(news.uuid) })
                    } else {
                        StandardNewsCard(news, onClick = { navigateToDetails(news.uuid) })
                    }
                }
            }
        }
    }
}