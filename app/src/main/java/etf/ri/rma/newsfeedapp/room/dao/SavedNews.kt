package etf.ri.rma.newsfeedapp.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.room.entities.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.room.entities.NewsWithTags
import etf.ri.rma.newsfeedapp.room.entities.Tag
import kotlinx.coroutines.flow.Flow // VAŽNO: Dodaj ovaj import

@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Promijenjeno na REPLACE
    suspend fun insertNews(news: NewsItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsTagCrossRef(crossRef: NewsTagCrossRef)

    @Query("SELECT * FROM news WHERE uuid = :uuid LIMIT 1")
    suspend fun getNewsByUuid(uuid: String): NewsItem?

    // Nova metoda: Provjeri je li vijest s određenim UUID-om označena kao featured
    @Query("SELECT * FROM news WHERE uuid = :uuid AND isFeatured = 1 LIMIT 1")
    suspend fun getFeaturedNewsByUuid(uuid: String): NewsItem?


    // --- Flow varijante za automatsko ažuriranje UI-ja ---

    @Transaction
    @Query("SELECT * FROM news")
    fun getAllNewsWithTagsFlow(): Flow<List<NewsWithTags>> // Vraća Flow

    @Transaction
    @Query("SELECT * FROM news WHERE category = :category")
    fun getNewsWithCategoryFlow(category: String): Flow<List<NewsWithTags>> // Vraća Flow

    // --- Postojeće metode (suspend) ---

    @Transaction
    @Query("SELECT * FROM news")
    suspend fun getAllNewsWithTags(): List<NewsWithTags>

    @Transaction
    @Query("SELECT * FROM news WHERE category = :category")
    suspend fun getNewsWithCategory(category: String): List<NewsWithTags>

    @Query("SELECT id FROM tags WHERE value = :tagValue LIMIT 1")
    suspend fun getTagIdByValue(tagValue: String): Int?

    @Query("SELECT T.value FROM tags AS T INNER JOIN NewsTags AS NT ON T.id = NT.tagId WHERE NT.newsId = :newsId")
    suspend fun getTagsForNews(newsId: Int): List<String>

    @Transaction
    @Query("SELECT N.* FROM news AS N INNER JOIN NewsTags AS NT ON N.id = NT.newsId INNER JOIN tags AS T ON NT.tagId = T.id WHERE T.value IN (:tags) GROUP BY N.id ORDER BY N.publishedDate DESC")
    suspend fun getNewsByTags(tags: List<String>): List<NewsWithTags>


    // Ovu metodu sam ostavio ovdje, ali ako ti pravi probleme, premjesti je u NewsDAO (ili Repository)
    // zato što je to kompleksnija poslovna logika.
    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int {
        var newTagsAddedCount = 0
        for (tagValue in tags) {
            var tagId: Int? = getTagIdByValue(tagValue) // Koristi internu DAO metodu
            if (tagId == null) {
                val newTag = Tag(value = tagValue)
                val insertedTagId = insertTag(newTag).toInt()
                if (insertedTagId != -1) {
                    tagId = insertedTagId
                    newTagsAddedCount++
                }
            }
            if (tagId != null && tagId != -1) {
                insertNewsTagCrossRef(NewsTagCrossRef(newsId = newsId, tagId = tagId))
            }
        }
        return newTagsAddedCount
    }
}