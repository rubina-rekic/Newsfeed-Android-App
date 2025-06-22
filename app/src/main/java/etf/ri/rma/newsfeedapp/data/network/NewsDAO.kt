// etf.ri.rma.newsfeedapp.data.network/NewsDAO.kt
package etf.ri.rma.newsfeedapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
// Import the specific toNewsItem extension function from your data package
import etf.ri.rma.newsfeedapp.data.toNewsItem // Ensure this imports your single extension
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.SavedNewsDAO
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.model.NewsItemDTO // Make sure NewsItemDTO is imported if used directly here
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class NewsDAO(private val context: Context) {

    companion object {
        private var api: NewsApiService = RetrofitInstance.api
        private const val API_KEY = "V61eDUFrmQScTsKyZtAWu6rfybY5NNXljUtCCCEQ"
        private const val CACHE_DURATION_MINUTES = 5L
        private const val LAST_FETCH_TIME_KEY_PREFIX = "last_fetch_"
        private const val TAG = "NewsDAO"
    }

    private val savedNewsDAO: SavedNewsDAO = NewsDatabase.getDatabase(context).savedNewsDAO()

    fun setApiService(apiService: Any) {
        api = apiService as NewsApiService
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun mapiranjeKat(category: String): String {
        return when (category.lowercase(Locale.ROOT)) {
            "sport", "sports" -> "sports"
            "politika", "politics" -> "politics"
            "nauka", "science", "tehnologija", "tech", "nauka/tehnologija" -> "science"
            "zdravlje", "health" -> "health"
            else -> "general"
        }
    }

    suspend fun getNewsWithTags(category: String): Flow<List<NewsItem>> = withContext(Dispatchers.IO) {
        val apiCategory = mapiranjeKat(category)
        val cacheKey = if (category == "Sve") "all_categories" else apiCategory

        val prefs = context.getSharedPreferences("news_cache_prefs", Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong(LAST_FETCH_TIME_KEY_PREFIX + cacheKey, 0L)
        val now = System.currentTimeMillis()
        val isCacheExpired = (now - lastFetchTime) > TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES)

        Log.d(TAG, "Dohvat vijesti za kategoriju: $category. Cache istekla: $isCacheExpired, Mreža dostupna: ${isNetworkAvailable()}")

        if (isNetworkAvailable() && isCacheExpired) {
            Log.d(TAG, "Mreža dostupna i cache istekla. Dohvaćam s API-ja i osvježavam bazu.")
            try {
                when (category) {
                    "Sve" -> {
                        val allCategoriesApi = listOf("sports", "politics", "science", "health", "general")
                        for (cat in allCategoriesApi) {
                            fetchAndSaveCategoryStories(cat)
                        }
                    }
                    "Nauka/tehnologija" -> {
                        fetchAndSaveCategoryStories(mapiranjeKat("Nauka"))
                        fetchAndSaveCategoryStories(mapiranjeKat("Tehnologija"))
                    }
                    else -> {
                        fetchAndSaveCategoryStories(apiCategory)
                    }
                }
                prefs.edit().putLong(LAST_FETCH_TIME_KEY_PREFIX + cacheKey, now).apply()
                Log.d(TAG, "API dohvat uspješan. Vrijeme keša ažurirano.")
            } catch (e: Exception) {
                Log.e(TAG, "Greška pri dohvaćanju s API-ja za kategoriju $category: ${e.message}", e)
            }
        } else if (!isNetworkAvailable()) {
            Log.d(TAG, "Mreža nije dostupna. Dohvaćam iz baze podataka.")
        } else {
            Log.d(TAG, "Keš je i dalje validan. Preskačem API dohvat.")
        }

        return@withContext if (category == "Sve") {
            savedNewsDAO.getAllNewsWithTagsFlow().map { it.map { newsWithTags -> newsWithTags.toNewsItem() } }
        } else {
            savedNewsDAO.getNewsWithCategoryFlow(apiCategory).map { it.map { newsWithTags -> newsWithTags.toNewsItem() } }
        }
    }

    private suspend fun fetchAndSaveCategoryStories(apiCategory: String): List<NewsItem> {
        val response = api.getTopStoriesByCategory(apiToken = API_KEY, categories = apiCategory)

        if (response.isSuccessful && response.body() != null) {
            // Explicitly pass apiCategory to the extension function
            val newStories = response.body()!!.data.map { it.toNewsItem(apiCategory) }
                .filter { mapiranjeKat(it.category) == apiCategory }
                .take(3)

            newStories.forEach { newsItem ->
                val insertedId = savedNewsDAO.insertNews(newsItem.copy(isFeatured = true))
                Log.d(TAG, "Spremljena vijest s UUID ${newsItem.uuid} u kategoriji $apiCategory, id: $insertedId")
            }
            return newStories
        }
        val errorBody = response.errorBody()?.string()
        Log.e(TAG, "API poziv neuspješan za kategoriju $apiCategory: ${response.code()} - $errorBody")
        return emptyList()
    }

    suspend fun saveNews(news: NewsItem): Boolean = withContext(Dispatchers.IO) {
        val newsId = savedNewsDAO.insertNews(news)
        return@withContext newsId != -1L
    }

    suspend fun addTags(tags: List<String>, newsId: Int): Int = withContext(Dispatchers.IO) {
        savedNewsDAO.addTags(tags, newsId)
    }

    suspend fun getTags(newsId: Int): List<String> = withContext(Dispatchers.IO) {
        return@withContext savedNewsDAO.getTags(newsId)
    }

    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> = withContext(Dispatchers.IO) {
        val tagsToSearch = tags.take(2)
        // No .map { it.toNewsItem() } needed if DAO returns NewsItem directly
        savedNewsDAO.getSimilarNews(tagsToSearch)
    }

    suspend fun getNewsByUuid(uuid: String): NewsItem? = withContext(Dispatchers.IO) {
        savedNewsDAO.getNewsByUuid(uuid)
    }

    suspend fun getSimilarStories(uuid: String): List<NewsItem> = withContext(Dispatchers.IO) {
        try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Nevalidan uuid: $uuid")
        }

        if (isNetworkAvailable()) {
            try {
                val response = api.getSimilarStories(uuid = uuid, apiToken = API_KEY)
                if (response.isSuccessful && response.body() != null) {
                    // Explicitly pass null to the extension function for similar stories
                    val similar = response.body()!!.data.map { it.toNewsItem(null) }.take(2)
                    similar.forEach { newsItem -> saveNews(newsItem) }
                    return@withContext similar
                }
            } catch (e: Exception) {
                Log.e(TAG, "Greška pri dohvaćanju sličnih vijesti s API-ja: ${e.message}", e)
            }
        }

        val newsFromDb = savedNewsDAO.getNewsByUuid(uuid)
            ?: run {
                Log.e(TAG, "Vijest s UUID $uuid nije pronađena u bazi za slične priče.")
                throw InvalidUUIDException("Vijest s UUID $uuid nije pronađena u bazi.")
            }

        val tagsForCurrentNews = savedNewsDAO.getTags(newsFromDb.id)
        // No .map { it.toNewsItem() } needed as savedNewsDAO.getSimilarNews returns List<NewsItem>
        savedNewsDAO.getSimilarNews(tagsForCurrentNews).filter { it.uuid != uuid }
    }
}