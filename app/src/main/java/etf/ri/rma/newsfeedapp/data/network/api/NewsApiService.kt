package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top")
    suspend fun searchNews(
        @Query("categories") category: String,
        @Query("api_token") apiToken: String
    ): NewsResponse

    @GET("news/similar")
    suspend fun getSimilarStories(
        @Query("uuid") uuid: String,
        @Query("api_token") apiToken: String
    ): NewsResponse
}