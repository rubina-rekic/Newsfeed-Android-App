package etf.ri.rma.newsfeedapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ImageRetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imagga.com/")  // Base URL za Imagga API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ImageApi by lazy {
        retrofit.create(ImageApi::class.java)
    }
}
