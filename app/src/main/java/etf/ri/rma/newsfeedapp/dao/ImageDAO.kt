package etf.ri.rma.newsfeedapp.dao

import etf.ri.rma.newsfeedapp.api.ImageRetrofitInstance
import etf.ri.rma.newsfeedapp.exception.InvalidImageURLException

object ImageDAO {
    private val tagCache = mutableMapOf<String, List<String>>() // Cache tagova

    suspend fun getTags(imageUrl: String): List<String> {
        // Provjera da li tagove već imamo u cache-u
        tagCache[imageUrl]?.let {
            return it // Ako imamo tagove u cache-u, vratit ćemo ih odmah
        }

        // Ako tagovi nisu u cache-u, pozivamo API
        val tags = try {
            val response = ImageRetrofitInstance.api.getImageTags(imageUrl, "your_api_key") // Zamijeni sa stvarnim API ključem
            response.result.tags.map { it.tag }
        } catch (e: Exception) {
            throw InvalidImageURLException("Neispravan URL slike ili greška u API pozivu")
        }

        // Spremamo dobijene tagove u cache
        tagCache[imageUrl] = tags
        return tags
    }
}
