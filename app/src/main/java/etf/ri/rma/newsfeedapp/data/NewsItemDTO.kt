package etf.ri.rma.newsfeedapp.data

import com.google.gson.annotations.SerializedName
import etf.ri.rma.newsfeedapp.model.NewsItem

data class NewsItemDTO(
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
)

fun NewsItemDTO.toNewsItem(): NewsItem {
    return NewsItem(
        uuid = this.uuid,
        title = this.title,
        snippet = this.snippet,
        imageUrl = this.imageUrl,
        category = this.categories?.firstOrNull() ?: "Uncategorized",
        isFeatured = false,
        source = this.source,
        publishedDate = formatPublishedDate(this.publishedDate),
        imageTags = arrayListOf()
    )
}

fun formatPublishedDate(publishedAt:String?): String{
    return publishedAt?.let{
        val parts=it.split("T")[0].split("-")
        if(parts.size==3){
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else { "Unknown date"
        }
    }?: "Unknown date"
}