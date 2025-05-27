package etf.ri.rma.newsfeedapp.data.api


import etf.ri.rma.newsfeedapp.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top")
    suspend fun searchNews(
        @Query("api_token") apiToken: String,
        @Query("categories") category: String
    ):  NewsResponse
}
