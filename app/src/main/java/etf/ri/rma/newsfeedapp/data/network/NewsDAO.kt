package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.data.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class NewsDAO {
    companion object {
        private var api: NewsApiService = RetrofitInstance.api
        private val cachedNews = Collections.synchronizedList(mutableListOf<NewsItem>())
        private val lastFetchTimes = ConcurrentHashMap<String, Long>()

        private val similarStoriesCache = ConcurrentHashMap<String, List<NewsItem>>()
        private const val API_KEY = "WxNJrNPNyhwF0FsYCs56qGpUPFcGX8whmVjkQpVz"
        private const val CACHE_DURATION_SECONDS = 30L

        init {
            // Učitavanje inicijalnih vijesti, nisu označene kao "featured"
            if (NewsData.getAllNews().isNotEmpty()) {
                NewsData.getAllNews().forEach { newsItem ->
                    // Dodajemo vijest samo ako već ne postoji u kešu, da izbjegnemo duplikate
                    if (cachedNews.none { it.uuid == newsItem.uuid }) {
                        cachedNews.add(newsItem.copy(isFeatured = false))
                    }
                }
            }
        }

        //funkc za mapiranje postojecih kategorija, ispravka, testovi
        fun mapiranjeKat(category: String): String {
            return when (category.lowercase(Locale.ROOT)) {
                "sport", "sports" -> "sports"
                "politika", "politics" -> "politics"
                "nauka", "science", "tehnologija", "tech", "nauka/tehnologija" -> "science" // Map both to "science" for API
                "zdravlje", "health" -> "health"
                else -> "general" // sve ostalo
            }
        }
    }

    fun setApiService(apiService: Any) {
        api = apiService as NewsApiService
    }

    suspend fun getNewsWithTags(category: String): List<NewsItem> = withContext(Dispatchers.IO) {
        val fetchedNews = when (category) {
            "Sve" -> getAllStories() //  vraća  sve st je u cachedNews
            "Nauka/tehnologija" -> {
                val scienceNews = getTopStoriesByCategory(mapiranjeKat("Nauka"))
                val techNews = getTopStoriesByCategory(mapiranjeKat("Tehnologija"))
                (scienceNews + techNews).distinctBy { it.uuid }
            }
            else -> {
                val apiCategory = mapiranjeKat(category)
                getTopStoriesByCategory(apiCategory)
            }
        }
        return@withContext fetchedNews
    }

    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> = withContext(Dispatchers.IO) {
        val apiCategory = mapiranjeKat(category)

        val now = System.currentTimeMillis()
        val lastFetchTime = lastFetchTimes[apiCategory] ?: 0
        val timeSinceLastFetch = now - lastFetchTime

        // Return cached if within the cache duration
        if (timeSinceLastFetch < TimeUnit.SECONDS.toMillis(CACHE_DURATION_SECONDS)) {
            // Vraćamo i featured i ne vijesti za ovu kat iz kesa
            // Redoslijed će se sortirati u NewsFeedScreen
            return@withContext cachedNews.filter {
                mapiranjeKat(it.category) == apiCategory
            }
        }

        val response = api.getTopStoriesByCategory(
            apiToken = API_KEY,
            categories = apiCategory
        )

        if (!response.isSuccessful || response.body() == null) {
            return@withContext cachedNews.filter {
                mapiranjeKat(it.category) == apiCategory
            }
        }

        val newStories = response.body()!!.data.map { it.toNewsItem(category) }
            .filter { mapiranjeKat(it.category) == apiCategory } // Ensure category matches after mapping
            .take(3)

        updateCacheWithNewStories(apiCategory, newStories)
        //ubaci nove featured u kes
        lastFetchTimes[apiCategory] = now

        // vratisve vijesti za odgg kategorijyx
        return@withContext cachedNews.filter {
            mapiranjeKat(it.category) == apiCategory
        }
    }

    private fun updateCacheWithNewStories(category: String, newStories: List<NewsItem>) {
        // un-feature postojece news za given category
        val newsToUpdate = cachedNews.filter { mapiranjeKat(it.category) == category }.toMutableList()

        newsToUpdate.forEachIndexed { index, newsItem ->
            newsToUpdate[index] = newsItem.copy(isFeatured = false)
        }

        // dodaj new stories kao featured  u listu
        newStories.forEach { newStory ->
            val existingIndex = newsToUpdate.indexOfFirst { it.uuid == newStory.uuid }
            if (existingIndex != -1) {
                newsToUpdate[existingIndex] = newsToUpdate[existingIndex].copy(isFeatured = true)
            } else {
                newsToUpdate.add(0, newStory.copy(isFeatured = true))
            }
        }

        cachedNews.removeAll { mapiranjeKat(it.category) == category }
        cachedNews.addAll(newsToUpdate)
    }

    fun getAllStories(): List<NewsItem> {
        return cachedNews.toList() //ne znam eto
    }
    fun addNewsItem(newsItem: NewsItem) {
        if (cachedNews.none { it.uuid == newsItem.uuid }) {
            cachedNews.add(newsItem)
        }
    }

    suspend fun getSimilarStories(uuid: String): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Nevalidan uuid: $uuid")
        }

        similarStoriesCache[uuid]?.let { return@withContext it }

        val response = api.getSimilarStories(
            uuid = uuid,
            apiToken = API_KEY
        )

        if (response.isSuccessful && response.body() != null) {
            val similar = response.body()!!.data.map { it.toNewsItem() }.take(2)
            similarStoriesCache[uuid] = similar
            return@withContext similar
        }

        // vrati vijesti iz kesa ako ne
        val current = cachedNews.find { it.uuid == uuid }
            ?: throw InvalidUUIDException("Vijest s UUID $uuid nije nadjena u kesu")

        val similar = cachedNews
            .filter { it.uuid != uuid && mapiranjeKat(it.category) == mapiranjeKat(current.category) }
            .take(2)

        similarStoriesCache[uuid] = similar
        return@withContext similar
    }

}