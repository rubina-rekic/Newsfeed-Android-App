package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface NewsApiService {
    @GET("v1/news/top")
    suspend fun getTopStoriesByCategory(
        @Query("api_token") apiToken: String,
        @Query("categories") categories: String,
        @Query("limit") limit: Int = 3
    ): Response<NewsResponse>

    @GET("v1/news/similar/{uuid}")
    suspend fun getSimilarStories(
        @Path("uuid") uuid: String,
        @Query("api_token") apiToken: String,
        @Query("limit") limit: Int = 2,
    ): Response<NewsResponse>

    @GET("v1/news/top")
    suspend fun getNewsBySource(
        @Query("api_token") apiToken: String,
        @Query("domains") sourceIds: String,
        @Query("limit") limit: Int = 10
    ): NewsResponse
}




