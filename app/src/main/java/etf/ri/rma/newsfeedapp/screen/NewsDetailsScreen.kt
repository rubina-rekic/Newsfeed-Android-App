package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.TaggingResult
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.launch

// Funkcija za konverziju datuma
fun String.convertDateFormat(): String {
    return try {
        val parsedDateTime = OffsetDateTime.parse(this)
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: DateTimeParseException) {
        println("DateTimeParseException: ${e.message}")
        this
    } catch (e: Exception) {
        println("General Exception: ${e.message}")
        this
    }
}

@Composable
fun NewsDetailsScreen(
    newsId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    val newsDAO = remember { NewsDAO(applicationContext) }
    val imagaDAO = remember { ImagaDAO(applicationContext) }

    // Stanje za vijest koja se prikazuje. Inicijalno je null.
    var newsItem by remember { mutableStateOf<NewsItem?>(null) }

    // Stanja za tagove slike
    var isLoadingTags by remember { mutableStateOf(false) }
    var imageTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagsErrorMessage by remember { mutableStateOf<String?>(null) }

    // Stanja za slicnnne vijesti
    var isLoadingSimilarStories by remember { mutableStateOf(false) }
    var similarStories by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var similarStoriesErrorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(newsId) {
        val fetchedNews = newsDAO.getNewsByUuid(newsId)

        if (fetchedNews != null) {
            newsItem = fetchedNews

            // Ako je vijest pronađena, dohvati tagove slike
            if (!fetchedNews.imageUrl.isNullOrEmpty()) {
                isLoadingTags = true
                tagsErrorMessage = null // Resetiraj poruku o grešci
                try {
                    when (val result = imagaDAO.getTags(fetchedNews.imageUrl!!, fetchedNews.id)) {
                        is TaggingResult.Success -> {
                            imageTags = result.tags
                        }
                        is TaggingResult.Error -> {
                            tagsErrorMessage = "Greška pri učitavanju tagova: ${result.exception.message}"
                        }
                    }
                } catch (e: Exception) {
                    tagsErrorMessage = "Nepoznata greška pri učitavanju tagova!"
                    println("Error fetching tags: ${e.message}")
                } finally {
                    isLoadingTags = false
                }
            } else {
                imageTags = emptyList()
                tagsErrorMessage = null
            }

            isLoadingSimilarStories = true
            similarStoriesErrorMessage = null
            try {
                val similar = newsDAO.getSimilarStories(newsId)
                similarStories = similar
                // Pohrani slične vijesti u bazu podataka, ako ih nema tam vec
                similar.forEach { newsItem -> newsDAO.saveNews(newsItem) }
            } catch (e: Exception) {
                similarStoriesErrorMessage = "Greška pri traženju sličnih vijesti: ${e.message}"
                println("Error loading similar stories: ${e.message}")
            } finally {
                isLoadingSimilarStories = false
            }

        } else {
            // Ako vijest nije pronađena u bazi podataka
            newsItem = null // Postavi newsItem na null
            similarStoriesErrorMessage = "Vijest nije pronađena."
            tagsErrorMessage = null
        }
    }

    // Prikaz ako vijest nije pronađena (newsItem je null)
    if (newsItem == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Vijest nije pronađena.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Nazad")
            }
        }
        return
    }

    val onBack: () -> Unit = {
        navController.popBackStack()
    }
    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Prikaz slike vijesti, siguran pristup s !! jer je newsItem sigurno non-null do ovdje
            if (!newsItem!!.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = newsItem!!.imageUrl,
                    contentDescription = "News image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Prikaz naslova i sažetka vijesti
            Text(
                text = newsItem!!.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("details_title")
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = newsItem!!.snippet,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("details_snippet")
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Prikaz kategorije, izvora i datuma objave
            Text(
                text = "Kategorija: ${newsItem!!.category}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_category")
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Izvor: ${newsItem!!.source}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_source")
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Datum objave: ${newsItem!!.publishedDate.convertDateFormat()}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_date")
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Prikaz statusa učitavanja ili greške za tagove
            if (isLoadingTags) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Učitavanje tagova...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (tagsErrorMessage != null) {
                Text(
                    text = "Greška pri učitavanju tagova: $tagsErrorMessage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("tags_error_message")
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (imageTags.isNotEmpty()) {
                Text(
                    text = "Tagovi slike:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("image_tags_label")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = imageTags.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("image_tags")
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (!newsItem!!.imageUrl.isNullOrEmpty()) {
                Text(
                    text = "Tagovi slike nisu pronađeni.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("no_image_tags")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Povezane vijesti",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Prikaz statusa učitavanja ili greške za slične vijesti
            if (isLoadingSimilarStories) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Učitavanje sličnih vijesti...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (similarStoriesErrorMessage != null) {
                Text(
                    text = "Greška pri učitavanju sličnih vijesti: $similarStoriesErrorMessage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("similar_news_error_message")
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (similarStories.isEmpty()) {
                Text(
                    text = "Nema sličnih vijesti.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("no_similar_news")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Prikaz liste sličnih vijesti
        items(similarStories.size) { index ->
            val relatedItem = similarStories[index]
            Text(
                text = relatedItem.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Navigacija do detalja slične vijesti
                        navController.navigate("details/${relatedItem.uuid}") {
                            launchSingleTop = true // Sprečava višestruko kreiranje istog ekrana
                        }
                    }
                    .testTag("related_news_title_${index + 1}")
                    .padding(vertical = 8.dp)
            )

            if (index < similarStories.size - 1) {
                HorizontalDivider()
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("details_close_button")
            ) {
                Text("Zatvori detalje")
            }
        }
    }
}