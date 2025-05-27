package etf.ri.rma.newsfeedapp.model

import com.google.gson.annotations.SerializedName
import etf.ri.rma.newsfeedapp.data.NewsItemDTO

data class NewsResponse(
    @SerializedName("data")
    val data: List<NewsItemDTO>
)