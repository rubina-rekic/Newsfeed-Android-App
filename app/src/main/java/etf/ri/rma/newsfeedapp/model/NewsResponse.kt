package etf.ri.rma.newsfeedapp.model

import kotlinx.serialization.Serializable

data class NewsItemDTO(
    @Serializable
    val uuid: String,
    @Serializable
    val title: String,
    @Serializable
    val description: String,
    @Serializable
    val snippet: String,
    @Serializable
    val url: String,
    @Serializable
    val image_url: String?,
    @Serializable
    val language: String,
    @Serializable
    val published_at: String,
    @Serializable
    val source: String,
    @Serializable
    val categories: List<String>
)

data class NewsResponse(
    val data: List<NewsItemDTO>
)
