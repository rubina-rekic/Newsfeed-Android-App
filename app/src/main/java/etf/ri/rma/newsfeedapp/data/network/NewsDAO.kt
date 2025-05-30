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
        //  početne vijesti iz NewsData pri inicijalizaciji
        if (_allStoriesList.isEmpty()) {
            val initial = NewsData.getAllNews()
            initial.forEach {
                allStoriesMap[it.uuid] = it
                _allStoriesList.add(it)
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

        // Filter cached news for the category
        val cachedNewsForCategory = _allStoriesList
            .filter { it.category.equals(category, ignoreCase = true) || it.category.equals(apiCategory, ignoreCase = true) }
            .toMutableList()

        if (currentTime - lastFetchTime <= 30 * 1000L) {
            // Return cached news if the method was called within the last 30 seconds
            return cachedNewsForCategory.take(3) // Ensure only 3 items are returned
        }

        try {
            // Fetch new news from the web service
            val newsResponse = apiService.searchNews(apiCategory,API_TOKEN)
            val newStoriesDTO = newsResponse.data
            val newStoriesFromApi = newStoriesDTO.map { it.toNewsItem() }

            // Update last fetch time
            lastFetchTimeByCategory[apiCategory] = currentTime

            // Add 3 new featured news items to the cached list
            val newFeaturedNews = newStoriesFromApi.take(3).map { it.copy(isFeatured = true) }
            newFeaturedNews.forEach { newStory ->
                allStoriesMap[newStory.uuid] = newStory
                _allStoriesList.add(0, newStory) // Add to the beginning of the global list
            }

            // Combine cached news and new featured news
            return (newFeaturedNews + cachedNewsForCategory).distinctBy { it.uuid }.take(3)
        } catch (e: Exception) {
            println("Error fetching news from web service for category $category: ${e.message}")
            // If API call fails, return cached news for the category
            return cachedNewsForCategory.take(3)
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