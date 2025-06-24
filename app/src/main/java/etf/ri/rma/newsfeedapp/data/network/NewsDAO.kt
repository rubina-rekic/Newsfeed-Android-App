package etf.ri.rma.newsfeedapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.data.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.SavedNewsDAO
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class NewsDAO(private val context: Context) {

    companion object {
        private var api: NewsApiService = RetrofitInstance.api
        private const val API_KEY = "V61eDUFrmQScTsKyZtAWu6rfybY5NNXljUtCCCEQ"
        private const val CACHE_DURATION_SECONDS = 30L //30 sekundi
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
        val isCacheExpired = (now - lastFetchTime) > TimeUnit.SECONDS.toMillis(CACHE_DURATION_SECONDS) // Koristi SECONDS

        Log.d(TAG, "Dohvat vijesti za kategoriju: $category. Cache istekla: $isCacheExpired, Mreža dostupna: ${isNetworkAvailable()}")

        val newFeaturedUuidsThisFetch = mutableListOf<String>()

        if (isNetworkAvailable() && isCacheExpired) {
            Log.d(TAG, "Mreža dostupna i cache istekla. Dohvaćam s API-ja i osvježavam bazu.")
            try {

                when (category) {
                    "Sve" -> {
                        val allCategoriesApi = listOf("sports", "politics", "science", "health", "general")
                        for (cat in allCategoriesApi) {
                            newFeaturedUuidsThisFetch.addAll(fetchAndSaveCategoryStories(cat))
                        }
                    }
                    "Nauka/tehnologija" -> {
                        newFeaturedUuidsThisFetch.addAll(fetchAndSaveCategoryStories(mapiranjeKat("Nauka")))
                        newFeaturedUuidsThisFetch.addAll(fetchAndSaveCategoryStories(mapiranjeKat("Tehnologija")))
                    }
                    else -> {
                        newFeaturedUuidsThisFetch.addAll(fetchAndSaveCategoryStories(apiCategory))
                    }
                }
                prefs.edit().putLong(LAST_FETCH_TIME_KEY_PREFIX + cacheKey, now).apply()
                Log.d(TAG, "API dohvat uspješan. Vrijeme keša ažurirano. Featured UUIDs: $newFeaturedUuidsThisFetch")
            } catch (e: Exception) {
                Log.e(TAG, "Greška pri dohvaćanju s API-ja za kategoriju $category: ${e.message}", e)
            }
        } else if (!isNetworkAvailable()) {
            Log.d(TAG, "Mreža nije dostupna. Dohvaćam iz baze podataka.")
        } else {
            Log.d(TAG, "Keš je i dalje validan. Preskačem API dohvat.")
            // Ako je keš validan, ne treba raditi ništa posebno, samo će vratiti postojeće vijesti.
            // Sorter na UI strani će se pobrinuti za featured status na osnovu isFeatured polja iz baze.
        }

        // Dohvaćamo vijesti iz baze i mapiramo ih.
        // Ovdje vršimo i sortiranje.
        val newsFlow = if (category == "Sve") {
            savedNewsDAO.getAllNewsWithTagsFlow()
        } else {
            savedNewsDAO.getNewsWithCategoryFlow(apiCategory)
        }

        return@withContext newsFlow.map { list ->
            list.map { newsWithTags -> newsWithTags.toNewsItem() }
                .sortedWith(compareByDescending<NewsItem> { newsItem ->
                    // Primarno sortiraj po 'isFeatured' statusu
                    newsItem.isFeatured
                }.thenByDescending { newsItem ->
                    // Zatim po datumu
                    try {
                        LocalDate.parse(newsItem.publishedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    } catch (e: Exception) {
                        Log.e(TAG, "Greška pri parsiranju datuma '${newsItem.publishedDate}': ${e.message}", e)
                        LocalDate.MIN // U slučaju greške, postavi na minimalni datum
                    }
                })
        }
    }


    private suspend fun fetchAndSaveCategoryStories(apiCategory: String): List<String> { // Vraća listu UUID-ova novih featured vijesti
        val response = api.getTopStoriesByCategory(apiToken = API_KEY, categories = apiCategory)
        val featuredUuidsForThisFetch = mutableListOf<String>()

        if (response.isSuccessful && response.body() != null) {
            val newStoriesFromApi = response.body()!!.data.map { it.toNewsItem(apiCategory) }
                .filter { mapiranjeKat(it.category) == apiCategory }
                .take(3) // Uvijek uzimamo samo 3 najnovije za featured

            // Resetuj isFeatured status za sve vijesti koje pripadaju OVOJ kategoriji
            val currentCategoryNews = savedNewsDAO.getNewsWithCategory(apiCategory)
            currentCategoryNews.forEach { newsItem ->
                if (!newStoriesFromApi.any { it.uuid == newsItem.uuid }) { // Ako vijest nije u novim 3, postavi je na false
                    savedNewsDAO.updateNewsIsFeatured(newsItem.uuid, false)
                }
            }


            newStoriesFromApi.forEach { newsItem ->
                val existingNews = savedNewsDAO.getNewsByUuid(newsItem.uuid)
                if (existingNews == null) {
                    val insertedId = savedNewsDAO.insertNews(newsItem.copy(isFeatured = true))
                    if (insertedId != -1L) {
                        Log.d(TAG, "Nova featured vijest s UUID ${newsItem.uuid} u kategoriji $apiCategory, id: $insertedId")
                        featuredUuidsForThisFetch.add(newsItem.uuid)
                    }
                } else {
                    savedNewsDAO.updateNewsIsFeatured(newsItem.uuid, true)
                    Log.d(TAG, "Postojeća vijest s UUID ${newsItem.uuid} ažurirana na featured.")
                    featuredUuidsForThisFetch.add(newsItem.uuid)
                }
            }
            return featuredUuidsForThisFetch
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
        savedNewsDAO.getSimilarNews(tagsForCurrentNews).filter { it.uuid != uuid }
    }
}