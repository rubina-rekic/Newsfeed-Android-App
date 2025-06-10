package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
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

fun String.convertDateFormat(): String {
    return try {
        val parsedDateTime = OffsetDateTime.parse(this)
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: DateTimeParseException) {
        println("${e.message}")
        this
    } catch (e: Exception) {

        println("${e.message}")
        this
    }
}


@Composable
fun NewsDetailsScreen(
    newsId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val newsDAO = remember { NewsDAO() }
    val imagaDAO = remember { ImagaDAO() }

    val newsList = remember { mutableStateListOf<NewsItem>().apply { addAll(newsDAO.getAllStories()) } }
    val newsItem = newsList.find { it.uuid == newsId }


    var isLoadingTags by remember { mutableStateOf(false) }
    var tagovikojeKESIRAM by remember { mutableStateOf<List<String>>(emptyList()) }
    var porukaGreske by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    var similarStories by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var slicneVijesti by remember { mutableStateOf(false) }

    // New state variables for headlines from the same source
    var isLoadingHeadlines by remember { mutableStateOf(false) }
    var headlinesFromSource by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(newsId) {
        if (newsItem != null) {
            scope.launch {
                slicneVijesti = true
                porukaGreske = null
                try {
                    val similar = newsDAO.getSimilarStories(newsId)
                    similarStories = similar
                    similar.forEach { newsDAO.addNewsItem(it) }
                } catch (e: Exception) {
                    println("Error loading similar stories: ${e.message}")
                    porukaGreske = "Greska pri trazenju similar news"
                } finally {
                    slicneVijesti = false
                }
            }

            // for loading tags
            if (!newsItem.imageUrl.isNullOrEmpty()) {
                scope.launch {
                    isLoadingTags = true
                    try {
                        when (val result = imagaDAO.getTags(newsItem.imageUrl)) {
                            is TaggingResult.Success -> {
                                tagovikojeKESIRAM = result.tags
                            }
                            is TaggingResult.Error -> {
                                porukaGreske = (porukaGreske ?: "") + "\nGreška pri učitavanju tagova: ${result.exception.message}"
                            }
                        }
                    } catch (e: Exception) {
                        porukaGreske =  "Nepoznata greska pri ucitavanju tagova!"
                    } finally {
                        isLoadingTags = false
                    }
                }
            }


            if (newsItem.source.isNotEmpty()) {
                scope.launch {
                    isLoadingHeadlines = true
                    try {
                        val headlines = newsDAO.getHeadlinesBySource(newsItem.source)
                        headlinesFromSource = headlines
                    } catch (e: Exception) {


                    } finally {
                        isLoadingHeadlines = false
                    }
                }
            }
        }
    }

    if (newsItem == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Vijest nije pronađena", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }
        return
    }

    val onBack = {
        navController.navigate("home") {
            popUpTo("home") { inclusive = false }
            launchSingleTop = true
        }
    }

    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            if (newsItem.imageUrl != null) {
                AsyncImage(
                    model = newsItem.imageUrl,
                    contentDescription = "News image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("details_title")
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = newsItem.snippet,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("details_snippet")
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kategorija: ${newsItem.category}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_category")
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Izvor: ${newsItem.source}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_source")
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Datum objave: ${newsItem.publishedDate.convertDateFormat()}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("details_date")
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Display tags
            if (isLoadingTags) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ucitavanje tagova...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (tagovikojeKESIRAM.isNotEmpty()) {
                Text(
                    text = "Tagovi slike:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("image_tags_label")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tagovikojeKESIRAM.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("image_tags")
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else if (!newsItem.imageUrl.isNullOrEmpty()) {
                Text(
                    text = "Tagovi slike nisu pronađeni.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("no_image_tags")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Povezane vijesti iz iste kategorije",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (slicneVijesti) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Učitavanje sličnih vijesti...", style = MaterialTheme.typography.bodySmall)
                }
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

        // Display similar stories
        items(similarStories.size) { index ->
            val relatedItem = similarStories[index]
            Text(
                text = relatedItem.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("details/${relatedItem.uuid}") {
                            popUpTo("news_feed") { inclusive = false }
                        }
                    }
                    .testTag("related_news_title_${index + 1}")
                    .padding(vertical = 8.dp)
            )

            if (index < similarStories.size - 1) {
                HorizontalDivider()
            }
        }

        // New section for headlines from the same source
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Naslovi vijesti iz istog izvora",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingHeadlines) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Učitavanje naslova iz izvora...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if (headlinesFromSource.isNotEmpty()) {
                Column {
                    headlinesFromSource.forEachIndexed { index, headline ->
                        Text(
                            text = "- $headline",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("headline_from_source_${index + 1}")
                        )
                    }
                }
            } else {
                Text(
                    text = "Nema naslova iz istog izvora.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("no_headlines_from_source")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("details_close_button")
            ) {
                Text("Zatvori detalje")
            }

            porukaGreske?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Izuzetak/greska: $msg",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}