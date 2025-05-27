package etf.ri.rma.newsfeedapp.api

import etf.ri.rma.newsfeedapp.data.ImaggaTagResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApi {
    @GET("v2/tags")
    suspend fun getImageTags(
        @Query("image_url") imageUrl: String,
        @Query("api_key") apiKey: String
    ): ImaggaTagResponse
}
