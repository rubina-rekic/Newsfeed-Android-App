package etf.ri.rma.newsfeedapp.data.api

import etf.ri.rma.newsfeedapp.data.api.NewsApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thenewsapi.com/v1/news/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NewsApi by lazy {
        retrofit.create(NewsApi::class.java)
    }
}