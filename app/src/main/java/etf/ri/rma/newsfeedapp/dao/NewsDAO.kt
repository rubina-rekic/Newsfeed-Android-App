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

    // Cache za vrijeme posljednjeg dohvaćanja po API kategoriji
    private val lastFetchTimeByCategory: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    init {
        // Popuni sa početnim vijestima iz NewsData samo jednom pri inicijalizaciji
        if (_allStoriesList.isEmpty()) {
            val initial = NewsData.getAllNews()
            initial.forEach {
                allStoriesMap[it.uuid] = it
                _allStoriesList.add(it)
            }
        }
    }




    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> {
        val currentTime = System.currentTimeMillis()


        if (category == "Sve") {
            // Vijesti se vraćaju kao standardne (ne-featured) za ovu globalnu listu
            return allStoriesList.map { it.copy(isFeatured = false) }.distinctBy { it.uuid }
        }

        val apiCategory = mapiranjeKat(category)
        val lastFetchTime = lastFetchTimeByCategory[apiCategory] ?: 0L

        // Dohvati postojeće vijesti iz keša za ovu SPECIFIČNU kategoriju
        // (bilo da je kategorija iz originalnog NewsData ili iz API poziva, mi ih filtriramo po kategoriji iz NewsItem-a)
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
            println("Vraćam keširane vijesti za kategoriju: $category (unutar 30 sekundi)")
            // Ako nije prošlo 30 sekundi, vrati sve keširane vijesti za tu kategoriju
            return cachedNewsForCategory.distinctBy { it.uuid }
        }
    }

    /**
     * Vraća listu svih vijesti koje su dohvaćene sa web servisa tokom trenutnog korištenja aplikacije.
     * Ova metoda ne poziva direktno web servis.
     *
     * @return Lista svih dohvaćenih vijesti.
     */
    fun getAllStories(): List<NewsItem> {
        // Vraća sve vijesti iz keša kao standardne (ne-featured)
        return allStoriesList.map { it.copy(isFeatured = false) }.toList()
    }

    /**
     * Vraća 2 najsličnije vijesti sa proslijeđenim UUID-em iz iste kategorije.
     * Ukoliko uuid nije u ispravnom formatu baca izuzetak InvalidUUIDException.
     *
     * @param uuid UUID vijesti za koju tražimo slične.
     * @return Lista 2 najsličnije vijesti.
     * @throws InvalidUUIDException Ako UUID nije u ispravnom formatu.
     */
    fun getSimilarStories(uuid: String): List<NewsItem> {
        try {
            UUID.fromString(uuid) // Validacija UUID formata
        } catch (e: IllegalArgumentException) {
            throw InvalidUUIDException("Invalid UUID format: $uuid")
        }

        val originalStory = allStoriesMap[uuid]
            ?: return emptyList() // Ako originalna vijest nije pronađena, vrati praznu listu

        val similarStories = allStoriesList
            .filter { it.category == originalStory.category && it.uuid != originalStory.uuid }
            .shuffled() // Simulacija sličnosti, uzima nasumično 2 iz iste kategorije
            .take(2)

        return similarStories
    }
}