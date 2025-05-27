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
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun FeaturedNewsCard(item: NewsItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(8.dp)
                .fillMaxWidth()
                .testTag("featured_news_card"),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Load image dynamically using Coil
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = "Featured News Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("featured_news_title_${item.uuid}")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Izvor: ${item.source} | ${item.publishedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}