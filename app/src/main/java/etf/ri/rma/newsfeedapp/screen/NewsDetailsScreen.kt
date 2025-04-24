package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.category


@Composable
fun NewsDetailsScreen(
    navController: NavController,
    newsId: String
) {
    val news = remember { NewsData.getAllNews().find { it.id == newsId } }
    val relatedNews = remember {
        NewsData.getAllNews()
            .filter { it.category == news?.category && it.id != newsId }
            .sortedWith(compareBy(
                {
                    val newsDate = news?.publishedDate?.toDate()?.time ?: 0L
                    val relatedDate = it.publishedDate.toDate()?.time ?: 0L
                    kotlin.math.abs(relatedDate - newsDate)
                },
                { it.title }
            ))
            .take(2)
    }

    if (news == null) {
        Text("Vijest nije pronađena", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // News details
        Text(news.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("details_title"))
        Spacer(modifier = Modifier.height(8.dp))
        Text(news.snippet, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("details_snippet"))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Kategorija: ${news.category}", modifier = Modifier.testTag("details_category"))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Izvor: ${news.source}", modifier = Modifier.testTag("details_source"))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Datum: ${news.publishedDate}", modifier = Modifier.testTag("details_date"))

        Spacer(modifier = Modifier.height(16.dp))

        // rilejted vijesti
        Text("Povezane vijesti iz iste kategorije", style = MaterialTheme.typography.titleMedium)
        relatedNews.forEachIndexed { index, related ->
            Text(
                text = related.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { navController.navigate("details/${related.id}") }
                    .testTag("related_news_title_${index + 1}")
                    .padding(vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = { navController.popBackStack("news_feed", inclusive = false) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
    }
}


