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
    suspend fun getTags(imageUrl: String): List<String> {
        // Provjera da li tagove već imamo u cache-u
        tagCache[imageUrl]?.let {
            return it // Ako imamo tagove u cache-u, vratit ćemo ih odmah
        }

        // Ako tagovi nisu u cache-u, pozivamo API
        val tags = try {
            val response = ImageRetrofitInstance.api.getImageTags(imageUrl, "9qfGW6bjGV8oAl5Dkvel4H1LqF3ofl7UyJoxdtyh") // Zamijeni sa stvarnim API ključem
            response.result.tags.map { it.tag }
        } catch (e: Exception) {
            throw InvalidImageURLException("Neispravan URL slike ili greška u API pozivu")
        }

        // Spremamo dobijene tagove u cache
        tagCache[imageUrl] = tags
        return tags
    }
}