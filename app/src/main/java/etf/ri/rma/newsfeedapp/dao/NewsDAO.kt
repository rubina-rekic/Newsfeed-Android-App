package etf.ri.rma.newsfeedapp.data
import etf.ri.rma.newsfeedapp.data.api.RetrofitInstance
import etf.ri.rma.newsfeedapp.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections


object NewsDAO {
    // za mapiranje kategorija
    private fun mapiranjeKat(category: String): String {
        return when (category) {
            "Politika" -> "politics"
            "Sport" -> "sports"
            "Nauka/tehnologija" -> "science" //  sta sa techh ???
            "Zdravlje" -> "health"
            else -> "general"
        }
    }

    private const val API_TOKEN = "9qfGW6bjGV8oAl5Dkvel4H1LqF3ofl7UyJoxdtyh"
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


        if (category == "Sve") {
            // Vijesti se vraćaju kao standardne
            return allStoriesList.map { it.copy(isFeatured = false) }.distinctBy { it.uuid }
        }

        val apiCategory = mapiranjeKat(category)
        val lastFetchTime = lastFetchTimeByCategory[apiCategory] ?: 0L


        // filtriramo po kategoriji iz NewsItem-a
        val cachedNewsForCategory = _allStoriesList
            .filter { it.category.equals(category, ignoreCase = true) || it.category.equals(apiCategory, ignoreCase = true) }
            .map { it.copy(isFeatured = false) } // Ensure existing are not featured for this return
            .toMutableList()


        if (currentTime - lastFetchTime > 30 * 1000L) { // 30 seconds passed, call web service
            println("Calling web service for category: $category (API: $apiCategory)")
            try {
                val newsResponse = RetrofitInstance.api.searchNews(API_TOKEN, apiCategory)
                val newStoriesDTO = newsResponse.data
                val newStoriesFromApi = newStoriesDTO.map { it.toNewsItem() }

                lastFetchTimeByCategory[apiCategory] = currentTime // Update fetch time for API category

                val resultListForUI = mutableListOf<NewsItem>()

                // Add new stories from API to the result list and update the cache
                newStoriesFromApi.forEach { newStory ->
                    val existingStory = allStoriesMap[newStory.uuid]
                    if (existingStory != null) {
                        // Story already exists, mark it as featured and add to result list
                        val updatedExisting = existingStory.copy(isFeatured = true)
                        resultListForUI.add(updatedExisting)
                    } else {
                        // New story, add to cache and mark as featured
                        val featuredNewStory = newStory.copy(isFeatured = true)
                        allStoriesMap[featuredNewStory.uuid] = featuredNewStory
                        _allStoriesList.add(0, featuredNewStory) // Add to the beginning of the global list
                        resultListForUI.add(featuredNewStory)
                    }
                }

                // Add remaining cached news for this category that are not already in the result list
                cachedNewsForCategory.forEach { existingCachedNews ->
                    if (resultListForUI.none { it.uuid == existingCachedNews.uuid }) {
                        resultListForUI.add(existingCachedNews.copy(isFeatured = false)) // Add as standard news
                    }
                }

                // Ensure uniqueness and correct order
                return resultListForUI.distinctBy { it.uuid }

            } catch (e: Exception) {
                println("Error fetching news from web service for category $category: ${e.message}")
                // If API call fails, return cached news for the category
                return cachedNewsForCategory.distinctBy { it.uuid }
            }
        }else {

            return cachedNewsForCategory.distinctBy { it.uuid }
        }
    }





    fun getSimilarStories(uuid: String): List<NewsItem> {
        try {
            UUID.fromString(uuid) // Validacija UUID formata
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Invalid UUID format: $uuid")
        }

        val originalStory = allStoriesMap[uuid]
            ?: return emptyList() // ako nema org vijesit vrati praznu listu

        val similarStories = allStoriesList
            .filter { it.category == originalStory.category && it.uuid != originalStory.uuid }
            .shuffled()
            .take(2)

        return similarStories
    }
}