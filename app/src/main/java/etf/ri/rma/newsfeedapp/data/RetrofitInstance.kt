package etf.ri.rma.newsfeedapp.data

import android.util.Base64
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    //vijestii
    private const val vijestiURL = "https://api.thenewsapi.com/"
    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(vijestiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    //dohvacanje slikyy
    private const val slikeURL = "https://api.imagga.com/"
    private const val IMAGGA_API_KEY = "acc_44ab43a18796aca"
    private const val IMAGGA_API_SECRET_KEY = "845d9d765783a9fd4d2cf5883cb44827"
    private val imaggaHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()

            val credentials = "$IMAGGA_API_KEY:$IMAGGA_API_SECRET_KEY"
            val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

            val newRequest = originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()

            chain.proceed(newRequest)
        }
        .build()

    val imageApi: ImagaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(slikeURL)
            .client(imaggaHttpClient) // bitnnn
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImagaApiService::class.java)
    }
}