package etf.ri.rma.newsfeedapp.screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsDetailsScreen(
    navController: NavController,
    newsId: String
) {
    // Tražimo vijest prema UUID-u
    val news = remember { NewsData.getAllNews().find { it.uuid == newsId } }

    // Kreiramo instancu NewsDAO
    val newsDAO = NewsDAO()

    // Dohvatimo slične vijesti na osnovu UUID-a
    val similarNews = remember {
        news?.let {
            try {
                newsDAO.getSimilarStories(it.uuid)
            } catch (e: InvalidUUIDException) {
                emptyList<NewsItem>() // Ako je UUID nevažeći, vratimo praznu listu
            }
        } ?: emptyList<NewsItem>() // Ako vijest ne postoji, vraćamo praznu listu
    }

    // Ako vijest nije pronađena, prikazujemo poruku
    if (news == null) {
        Text("Vijest nije pronađena", modifier = Modifier.padding(16.dp))
        return
    }

    // Osnovni izgled ekrana za prikaz vijesti
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Prikazujemo detalje vijesti
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

        // Prikaz slike vijesti, ako postoji
        news.imageUrl?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = news.title,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prikazivanje povezanih vijesti
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

        // Dugme za zatvaranje detalja vijesti
        Button(
            onClick = {
                navController.navigate("news_feed") // Povratak na ekran sa svim vijestima
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag("details_close_button")
        ) {
            Text("Zatvori detalje")
        }
    }
}
