package etf.ri.rma.newsfeedapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log // Dodajte za logiranje grešaka
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.data.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.room.dao.SavedNewsDAO
import etf.ri.rma.newsfeedapp.room.entities.NewsDatabase
import etf.ri.rma.newsfeedapp.room.entities.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.room.entities.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow // Potrebno za Flow
import kotlinx.coroutines.flow.map // Potrebno za mapiranje Flow-a
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class NewsDAO(private val context: Context) {

    companion object {
        private var api: NewsApiService = RetrofitInstance.api
        // Koristite svoj pravi API ključ
        private const val API_KEY = "V61eDUFrmQScTsKyZtAWu6rfybY5NNXljUtCCCEQ" // PROVJERITE OVO!
        private const val CACHE_DURATION_MINUTES = 5L // Cache traje 5 minuta
        private const val LAST_FETCH_TIME_KEY_PREFIX = "last_fetch_"
        private const val TAG = "NewsDAO" // Za logiranje
    }

    private val savedNewsDAO: SavedNewsDAO = NewsDatabase.getDatabase(context).savedNewsDao()

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

    /**
     * Glavna metoda za dohvaćanje vijesti. Prvo provjerava mrežu i keš,
     * zatim dohvaća s API-ja ako je potrebno i sprema u bazu,
     * na kraju uvijek vraća Flow<List<NewsItem>> iz baze.
     */
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
                // Ažuriraj vrijeme zadnjeg dohvaćanja samo ako su API pozivi bili uspješni
                prefs.edit().putLong(LAST_FETCH_TIME_KEY_PREFIX + cacheKey, now).apply()
                Log.d(TAG, "API dohvat uspješan. Vrijeme keša ažurirano.")
            } catch (e: Exception) {
                Log.e(TAG, "Greška pri dohvaćanju s API-ja za kategoriju $category: ${e.message}", e)
                // Ako dođe do greške s mrežom, samo ćemo vratiti podatke iz baze (što se događa ispod)
            }
        } else if (!isNetworkAvailable()) {
            Log.d(TAG, "Mreža nije dostupna. Dohvaćam iz baze podataka.")
        } else {
            Log.d(TAG, "Keš je i dalje validan. Preskačem API dohvat.")
        }

        // Uvijek vraćamo Flow iz baze podataka. UI će se automatski ažurirati
        // kada se podaci u bazi promijene (npr. nakon mrežnog osvježavanja).
        return@withContext if (category == "Sve") {
            savedNewsDAO.getAllNewsWithTagsFlow().map { it.map { newsWithTags -> newsWithTags.toNewsItem() } }
        } else {
            savedNewsDAO.getNewsWithCategoryFlow(apiCategory).map { it.map { newsWithTags -> newsWithTags.toNewsItem() } }
        }
    }

    /**
     * Pomoćna metoda koja dohvaća vijesti s API-ja za zadanu kategoriju i sprema ih u bazu.
     */
    private suspend fun fetchAndSaveCategoryStories(apiCategory: String): List<NewsItem> {
        val response = api.getTopStoriesByCategory(apiToken = API_KEY, categories = apiCategory)

        if (response.isSuccessful && response.body() != null) {
            val newStories = response.body()!!.data.map { it.toNewsItem(apiCategory) }
                .filter { mapiranjeKat(it.category) == apiCategory }
                .take(3) // Uzimamo samo 3 vijesti kao 'featured' s API-ja

            newStories.forEach { newsItem ->
                // Prije spremanja, provjeri je li vijest već featured u bazi
                // Ili neka se API-fetched news stavi kao featured, a ostali kao ne-featured.
                // Strategija "REPLACE" u insertNews će zamijeniti postojeću.
                // Ako želite sačuvati featured status iz baze, morali biste ga prvo dohvatiti.
                // Jednostavnije je da API vijesti koje stignu budu featured, ostali ne.
                val insertedId = savedNewsDAO.insertNews(newsItem.copy(isFeatured = true)) // API news su featured
                Log.d(TAG, "Spremljena vijest s UUID ${newsItem.uuid} u kategoriji $apiCategory, id: $insertedId")
            }
            return newStories
        }
        val errorBody = response.errorBody()?.string()
        Log.e(TAG, "API poziv neuspješan za kategoriju $apiCategory: ${response.code()} - $errorBody")
        return emptyList()
    }


    // --- Metode za pojedinačne operacije (ako su i dalje potrebne izvan getNewsWithTags logike) ---

    // Metoda za spremanje jedne vijesti u bazu
    suspend fun saveNews(news: NewsItem): Boolean = withContext(Dispatchers.IO) {
        val newsId = savedNewsDAO.insertNews(news)
        return@withContext newsId != -1L
    }

    suspend fun addTags(tags: List<String>, newsId: Int): Int = withContext(Dispatchers.IO) {
        // Sada poziva metodu iz SavedNewsDAO
        savedNewsDAO.addTags(tags, newsId)
    }

    suspend fun getTags(newsId: Int): List<String> = withContext(Dispatchers.IO) {
        return@withContext savedNewsDAO.getTagsForNews(newsId)
    }

    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> = withContext(Dispatchers.IO) {
        val tagsToSearch = tags.take(2)
        savedNewsDAO.getNewsByTags(tagsToSearch).map { it.toNewsItem() }
    }

    suspend fun getNewsByUuid(uuid: String): NewsItem? = withContext(Dispatchers.IO) {
        savedNewsDAO.getNewsByUuid(uuid)
    }

    // --- Ostale metode koje su bile redundantne ili nepotrebne nakon prelaska na Room Flow ---
    // Uklonjene in-memory cache operacije i direktni getAllStories/addNewsItem
    // jer se sve sada treba oslanjati na Room kao single source of truth.
    // Ako ih nešto drugo koristi, razmislite o refaktoriranju da koriste Room.

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
                    val similar = response.body()!!.data.map { it.toNewsItem() }.take(2)
                    similar.forEach { newsItem -> saveNews(newsItem) } // Spremi u bazu
                    return@withContext similar
                }
            } catch (e: Exception) {
                Log.e(TAG, "Greška pri dohvaćanju sličnih vijesti s API-ja: ${e.message}", e)
                // Ako API poziv ne uspije, nastavi s bazom podataka
            }
        }

        // Fallback: Ako nema mreže ili API poziv nije uspio, dohvati iz baze
        val newsFromDb = savedNewsDAO.getNewsByUuid(uuid)
            ?: run {
                Log.e(TAG, "Vijest s UUID $uuid nije pronađena u bazi za slične priče.")
                throw InvalidUUIDException("Vijest s UUID $uuid nije pronađena u bazi.")
            }

        val tagsForCurrentNews = savedNewsDAO.getTagsForNews(newsFromDb.id)
        savedNewsDAO.getNewsByTags(tagsForCurrentNews).map { it.toNewsItem() }.filter { it.uuid != uuid }
    }
}