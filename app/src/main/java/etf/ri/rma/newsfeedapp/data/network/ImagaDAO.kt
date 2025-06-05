package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.network.exception.ImageTaggingException
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import etf.ri.rma.newsfeedapp.data.network.exception.NetworkException
import android.util.Patterns
import android.util.LruCache
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class TaggingResult {
    data class Success(val tags: List<String>) : TaggingResult()
    data class Error(val exception: Exception) : TaggingResult()
}

class ImagaDAO {
    companion object {
        private var api: ImagaApiService = RetrofitInstance.imageApi
        private val imageTagsCache = LruCache<String, List<String>>(100)
    }

    fun setApiService(apiService: ImagaApiService) {
        api = apiService
    }

    suspend fun getTags(imageURL: String): TaggingResult = withContext(Dispatchers.IO) {
        if (!Patterns.WEB_URL.matcher(imageURL).matches()) {
            throw InvalidImageURLException("Invalid image URL: $imageURL")
        }

        imageTagsCache.get(imageURL)?.let { tags ->
            return@withContext TaggingResult.Success(tags)
        }

        try {
            val response = api.getTags(imageUrl = imageURL)

            if (response.isSuccessful) {
                val tags = response.body()?.result?.tags?.map { it.tag.en } ?: emptyList()
                imageTagsCache.put(imageURL, tags) // hahhasha kesiraj tagove
                return@withContext TaggingResult.Success(tags)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Imagga API greska: ${response.code()} - ${errorBody ?: response.message()}"
                return@withContext TaggingResult.Error(ImageTaggingException(errorMessage))
            }
        } catch (e: IOException) {
            return@withContext TaggingResult.Error(NetworkException("Greska sa konekcijom pri ucitavanju tagova", e))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = "HTTP greska ${e.code()}: ${errorBody ?: e.message()}"
            return@withContext TaggingResult.Error(ImageTaggingException(errorMessage, e))
        } catch (e: Exception) {
            return@withContext TaggingResult.Error(ImageTaggingException("Neka nepoznata greska", e))
        }
    }
}