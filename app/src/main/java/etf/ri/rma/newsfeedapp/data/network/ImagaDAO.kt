package etf.ri.rma.newsfeedapp.data.network

import android.content.Context
import android.util.LruCache
import android.util.Patterns
import etf.ri.rma.newsfeedapp.data.RetrofitInstance

import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.ImageTaggingException
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.data.network.exception.NetworkException
import etf.ri.rma.newsfeedapp.room.dao.SavedNewsDAO
import etf.ri.rma.newsfeedapp.room.entities.NewsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class TaggingResult {
    data class Success(val tags: List<String>) : TaggingResult()
    data class Error(val exception: Exception) : TaggingResult()
}

class ImagaDAO(private val context: Context) { // Dodaj Context u konstruktor

    companion object {
        private var api: ImagaApiService = RetrofitInstance.imageApi
        private val imageTagsCache = LruCache<String, List<String>>(100)
    }

    // Inicijaliziraj Room DAO
    private val savedNewsDAO: SavedNewsDAO = NewsDatabase.getDatabase(context).savedNewsDao()

    fun setApiService(apiService: ImagaApiService) {
        api = apiService
    }

    // Dodaj newsId kao parametar jer ti treba za spremanje tagova u bazu
    suspend fun getTags(imageURL: String, newsId: Int): TaggingResult = withContext(Dispatchers.IO) {
        if (!Patterns.WEB_URL.matcher(imageURL).matches()) {
            return@withContext TaggingResult.Error(InvalidImageURLException("Invalid image URL: $imageURL"))
        }

        // 1. Provjeri in-memory keš
        imageTagsCache.get(imageURL)?.let { tags ->
            return@withContext TaggingResult.Success(tags)
        }

        // 2. Provjeri bazu podataka
        try {
            val dbTags = savedNewsDAO.getTagsForNews(newsId)
            if (dbTags.isNotEmpty()) {
                imageTagsCache.put(imageURL, dbTags) // Dodaj u in-memory keš iz baze
                return@withContext TaggingResult.Success(dbTags)
            }
        } catch (e: Exception) {
            // Logiraj grešku, ali nastavi s API pozivom ako čitanje iz baze ne uspije
            e.printStackTrace()
        }

        // 3. Ako nije u kešu ili bazi, dohvati s API-ja
        return@withContext try {
            val response = api.getTags(imageUrl = imageURL)

            if (response.isSuccessful) {
                val tags = response.body()?.result?.tags?.map { it.tag.en } ?: emptyList()
                imageTagsCache.put(imageURL, tags) // Keširaj API rezultat

                // Spremi tagove u bazu podataka NAKON uspješnog API poziva
                if (tags.isNotEmpty()) {
                    savedNewsDAO.addTags(tags, newsId) // Pozovi addTags iz Room DAO-a
                }
                TaggingResult.Success(tags)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Imagga API greska: ${response.code()} - ${errorBody ?: response.message()}"
                TaggingResult.Error(ImageTaggingException(errorMessage))
            }
        } catch (e: IOException) {
            TaggingResult.Error(NetworkException("Greska sa konekcijom pri ucitavanju tagova", e))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = "HTTP greska ${e.code()}: ${errorBody ?: e.message()}"
            TaggingResult.Error(ImageTaggingException(errorMessage, e))
        } catch (e: Exception) {
            TaggingResult.Error(ImageTaggingException("Neka nepoznata greska", e))
        }
    }
}