package etf.ri.rma.newsfeedapp.data
import etf.ri.rma.newsfeedapp.model.NewsItemDTO
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun NewsItemDTO.toNewsItem(categoryOverride: String? = null): NewsItem {
    return NewsItem(
        uuid = uuid,
        title = title,
        snippet = snippet,
        imageUrl = image_url,
        category = categoryOverride ?: categories?.firstOrNull()?.lowercase() ?: "general",
        isFeatured = false,
        source = source,
        publishedDate = convertDateFormat(published_at),
        imageTags = arrayListOf()
    )
}


private fun convertDateFormat(isoDateString: String): String {
    return try {
        val parsedDateTime = OffsetDateTime.parse(isoDateString)
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } catch (e: Exception) {
        isoDateString
    }
}