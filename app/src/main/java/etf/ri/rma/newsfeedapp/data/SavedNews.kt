package etf.ri.rma.newsfeedapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.model.NewsWithTags
import etf.ri.rma.newsfeedapp.model.Tag
import kotlinx.coroutines.flow.Flow // VAŽNO: Dodaj ovaj import

@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Changed back to IGNORE as per test a05 expecting false on duplicate
    suspend fun insertNews(news: NewsItem): Long // Accepts NewsItem directly

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsTagCrossRef(crossRef: NewsTagCrossRef)

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1") // Query table "News"
    suspend fun getNewsByUuid(uuid: String): NewsItem? // Returns NewsItem

    @Query("SELECT * FROM News WHERE uuid = :uuid AND isFeatured = 1 LIMIT 1") // Query table "News"
    suspend fun getFeaturedNewsByUuid(uuid: String): NewsItem? // Returns NewsItem


    // --- Flow varijante za automatsko ažuriranje UI-ja ---

    @Transaction
    @Query("SELECT * FROM News") // Query table "News"
    fun getAllNewsWithTagsFlow(): Flow<List<NewsWithTags>>

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category") // Query table "News"
    fun getNewsWithCategoryFlow(category: String): Flow<List<NewsWithTags>>

    // --- Postojeće metode (suspend) - Adjusted to match TestS4 expectations ---

    @Transaction
    @Query("SELECT * FROM News") // Query table "News"
    // Renamed to allNews to match the test method name
    // Returns List<NewsWithTags> as test expects `it.imageTags`
    suspend fun allNews(): List<NewsWithTags>

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category") // Query table "News"
    // Matches test method name and return type
    suspend fun getNewsWithCategory(category: String): List<NewsWithTags>

    @Query("SELECT id FROM Tags WHERE value = :tagValue LIMIT 1") // Query table "Tags"
    suspend fun getTagIdByValue(tagValue: String): Int?

    @Query("SELECT T.value FROM Tags AS T INNER JOIN NewsTags AS NT ON T.id = NT.tagId WHERE NT.newsId = :newsId") // Query tables "Tags", "NewsTags"
    // Renamed to getTags to match test method name
    suspend fun getTags(newsId: Int): List<String>

    @Query("SELECT N.* FROM News AS N INNER JOIN NewsTags AS NT ON N.id = NT.newsId INNER JOIN Tags AS T ON NT.tagId = T.id WHERE T.value IN (:tags) GROUP BY N.id ORDER BY N.publishedDate DESC")
    // Renamed to getSimilarNews and returns List<NewsItem> as per TestS4.a12/a13 expectations
    suspend fun getSimilarNews(tags: List<String>): List<NewsItem>

    // This method was not in the original test suite, adding to match previous context
    @Query("SELECT value FROM Tags")
    suspend fun getAllTags(): List<String>

    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int {
        var newTagsAddedCount = 0
        for (tagValue in tags) {
            var tagId: Int? = getTagIdByValue(tagValue)
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

    @Transaction
    suspend fun saveNews(news: NewsItem): Boolean { // Accepts NewsItem directly
        val rowId = insertNews(news)
        // If insertNews returns -1L (due to OnConflictStrategy.IGNORE), it means it was a duplicate and not saved
        return rowId != -1L
    }
}