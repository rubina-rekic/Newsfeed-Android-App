package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top")
    suspend fun searchNews(
        @Query("api_token") apiToken: String,
        @Query("categories") category: String
    ): NewsResponse
}