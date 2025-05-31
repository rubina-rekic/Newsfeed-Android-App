package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.toNewsItem
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NewsDAO {
    private var apiService: NewsApiService = RetrofitInstance.api

    fun setApiService(service: NewsApiService) {
        apiService = service
    }
    // za mapiranje kategorija
    private fun mapiranjeKat(category: String): String {
        return when (category) {
            "Politika", "politics" -> "politics"
            "Sport", "sports" -> "sports"
            "Nauka/tehnologija", "science", "technology" -> "science"
            "Zdravlje", "health" -> "health"
            else -> "general"
        }
    }


    private  val API_TOKEN = "9qfGW6bjGV8oAl5Dkvel4H1LqF3ofl7UyJoxdtyh"
    private val allStoriesMap: ConcurrentHashMap<String, NewsItem> = ConcurrentHashMap()
    private val _allStoriesList: MutableList<NewsItem> = Collections.synchronizedList(mutableListOf())
    private val allStoriesList: List<NewsItem> get() = _allStoriesList.toList()


    private val lastFetchTimeByCategory: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    init {
        if (_allStoriesList.isEmpty()) {
            val initial = NewsData.getAllNews()
            initial.forEach {
                allStoriesMap[it.uuid] = it
                _allStoriesList.add(it.copy(isFeatured = false))
            }
        }
    }

    fun getAllStories(): List<NewsItem> {

        return allStoriesList.map { it.copy(isFeatured = false) }.toList()
    }


    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> {
        val currentTime = System.currentTimeMillis()
        val apiCategory = mapiranjeKat(category)
        val lastFetchTime = lastFetchTimeByCategory[apiCategory] ?: 0L

        val cachedNewsForCategory = _allStoriesList
            .filter { mapiranjeKat(it.category) == apiCategory }
            .distinctBy { it.uuid }

        // If less than 30 seconds have passed, return cached news for the category
        if (currentTime - lastFetchTime < 30 * 1000L) {
            return cachedNewsForCategory.map { it.copy(isFeatured = false) }
        }

        return try {
            val newsResponse = apiService.searchNews(apiCategory, API_TOKEN)
            val newStoriesDTO = newsResponse.data
            val newFetched = newStoriesDTO.map { it.toNewsItem() }.take(3)

            // Add new stories to the "Sve" category and mark them as featured
            val newFeatured = newFetched.map { new ->
                val existing = allStoriesMap[new.uuid]
                if (existing != null) {
                    existing.copy(isFeatured = true)
                } else {
                    val fresh = new.copy(isFeatured = true)
                    allStoriesMap[fresh.uuid] = fresh
                    _allStoriesList.add(0, fresh)
                    fresh
                }
            }

            // Add new stories to the "Sve" category
            newFeatured.forEach { fresh ->
                if (mapiranjeKat(fresh.category) != "general") {
                    val generalCategoryNews = allStoriesMap[fresh.uuid]
                    if (generalCategoryNews == null) {
                        _allStoriesList.add(fresh.copy(category = "general"))
                    }
                }
            }

            // All previously fetched news for the category as standard
            val previousStandard = cachedNewsForCategory
                .filter { it.uuid !in newFeatured.map { nf -> nf.uuid } }
                .map { it.copy(isFeatured = false) }

            // Update the last fetch time
            lastFetchTimeByCategory[apiCategory] = currentTime

            return newFeatured + previousStandard
        } catch (e: Exception) {
            println("Error fetching news: ${e.message}")
            // If the API call fails, return cached news
            cachedNewsForCategory.map { it.copy(isFeatured = false) }
        }
    }

    suspend fun getSimilarStories(uuid: String): List<NewsItem> {
        try {
            UUID.fromString(uuid) // Validate UUID format
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Invalid UUID format: $uuid")
        }

        try {
            // Make API call to fetch similar stories
            val response = apiService.getSimilarStories(uuid, API_TOKEN)
            val similarStoriesDTO = response.data
            return similarStoriesDTO.map { it.toNewsItem() }
        } catch (e: Exception) {
            println("Error fetching similar stories for UUID $uuid: ${e.message}")
            return emptyList() // Return an empty list if the API call fails
        }
    }
}