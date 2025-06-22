package etf.ri.rma.newsfeedapp.data

import com.google.gson.annotations.SerializedName
import etf.ri.rma.newsfeedapp.model.NewsItemDTO
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

//treba li ovo uopste ili ce trebti?
/*data class NewsItemDTO(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("snippet")
    val snippet: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("source")
    val source: String,
    @SerializedName("published_at")
    val publishedDate: String
)*/

fun NewsItemDTO.toNewsItem(categoryOverride: String? = null): NewsItem {
    return NewsItem(
        uuid = uuid,
        title = title,
        snippet = snippet,
        imageUrl = image_url,
        // Use categoryOverride if provided, otherwise fallback to DTO's first category, then "general"
        category = categoryOverride ?: categories?.firstOrNull()?.lowercase() ?: "general",
        isFeatured = false, // Default to false
        source = source,
        publishedDate = convertDateFormat(published_at),
        imageTags = arrayListOf() // Default empty list
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