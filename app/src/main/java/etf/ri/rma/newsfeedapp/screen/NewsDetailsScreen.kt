package etf.ri.rma.newsfeedapp.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.dao.ImageDAO
import etf.ri.rma.newsfeedapp.data.NewsDAO
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsDetailsScreen(
    navController: NavController,
    newsId: String
) {
    val news = remember { NewsData.getAllNews().find { it.uuid == newsId } }

    // Fetch similar stories based on UUID
    val similarNews = remember {
        news?.let {
            try {
                NewsDAO.getSimilarStories(it.uuid)
            } catch (e: InvalidUUIDException) {
                emptyList<NewsItem>()
            }
        } ?: emptyList<NewsItem>()
    }

    // Handle invalid news
    if (news == null) {
        Text("Vijest nije pronađena", modifier = Modifier.padding(16.dp))
        return
    }

    // Rest of your screen layout...
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

        // Image and tags for news
        news.imageUrl?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = news.title,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Similar stories section
        Text("Povezane vijesti iz iste kategorije", style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.testTag("news_list")) {
            similarNews.forEachIndexed { index, related ->
                Text(
                    text = related.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable { navController.navigate("details/${related.uuid}") }
                        .testTag("related_news_title_${index + 1}")
                        .padding(vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Button to close details
        Button(
            onClick = {
                navController.navigate("news_feed")
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
    }
}
