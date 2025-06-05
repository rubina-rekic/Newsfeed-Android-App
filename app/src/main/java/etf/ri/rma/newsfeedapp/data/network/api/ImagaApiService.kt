package etf.ri.rma.newsfeedapp.data.network.api
import etf.ri.rma.newsfeedapp.model.ImaggaTagResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ImagaApiService {
    @GET("v2/tags")
    suspend fun getTags(
        @Query("image_url") imageUrl: String
    ): Response<ImaggaTagResult>
}
