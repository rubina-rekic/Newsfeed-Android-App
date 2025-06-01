package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ImageRetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imagga.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ImagaApiService by lazy {
        retrofit.create(ImagaApiService::class.java)
    }
}