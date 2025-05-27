package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.R

@Composable
fun StandardNewsCard(item: NewsItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        val backgroundColor = when (item.category) {
            "Sport" -> Color(0xFFADD8E6)
            "Nauka/tehnologija" -> Color(0xFF90EE90)
            "Politika" -> Color(0xFFE6E6FA)
            else -> MaterialTheme.colorScheme.surface
        }
        Card(
            modifier = Modifier
                .background(color = backgroundColor)
                .padding(8.dp)
                .fillMaxWidth()
                .testTag("standard_news_card"),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .background(color = backgroundColor)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Load image dynamically using Coil
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = "News Image",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(end = 16.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("news_title_${item.uuid}")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Izvor: ${item.source} | ${item.publishedDate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
