package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.network.ImageRetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsDetailsScreen(
    navController: NavController,
    newsId: String
) {
    val news = remember { NewsData.getAllNews().find { it.uuid == newsId } }

    if (news == null) {
        Text("Vijest nije pronađena", modifier = Modifier.padding(16.dp))
        return
    }

    val newsDAO = remember { NewsDAO() }
    val imagaDAO = remember {
        ImagaDAO().apply {
            setApiService(ImageRetrofitInstance.api)
        }
    }

    val similarNews = remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    val imageTags = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(newsId) {
        try {
            similarNews.value = newsDAO.getSimilarStories(news.uuid)
        } catch (e: InvalidUUIDException) {
            similarNews.value = emptyList()
        }
    }

    LaunchedEffect(news.imageUrl) {
        news.imageUrl?.let { url ->
            try {
                imageTags.value = imagaDAO.getTags(url)
            } catch (_: InvalidImageURLException) {
                imageTags.value = listOf("Nepoznato")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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

        news.imageUrl?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = news.title,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            if (imageTags.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tagovi slike:", style = MaterialTheme.typography.titleSmall)
                Text(
                    imageTags.value.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("details_image_tags")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Povezane vijesti iz iste kategorije", style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.testTag("news_list")) {
            similarNews.value.forEachIndexed { index, related ->
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

        Button(
            onClick = { navController.navigate("news_feed") },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
    }
}
