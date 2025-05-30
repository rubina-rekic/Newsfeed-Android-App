package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.network.ImageRetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException

class ImagaDAO {
    private val tagCache = mutableMapOf<String, List<String>>() // Cache tagova
    private var apiService: ImagaApiService? = null // Initialize apiService as null

    // The setApiService method will allow setting the apiService externally
    fun setApiService(apiService: ImagaApiService) {
        this.apiService = apiService
    }
    fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    suspend fun getTags(imageUrl: String): List<String> {
        if (!isValidUrl(imageUrl)) {
            throw InvalidImageURLException("Invalid image URL: $imageUrl")
        }

        tagCache[imageUrl]?.let {
            return it
        }

        val tags = try {
            val response = apiService?.getImageTags(imageUrl, "acc_44ab43a18796aca")
                ?: throw InvalidImageURLException("API service not initialized")
            response.result.tags.map { it.tag.en } // Access the nested `en` field
        } catch (e: Exception) {
            throw InvalidImageURLException("Error during API call: ${e.message}")
        }
        tagCache[imageUrl] = tags
        return tags
    }

}