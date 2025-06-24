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
import kotlinx.coroutines.flow.Flow //treba ovajj import

@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNews(news: NewsItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsTagCrossRef(crossRef: NewsTagCrossRef)

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1")
    suspend fun getNewsByUuid(uuid: String): NewsItem? // vraca NewsItem

    @Query("SELECT * FROM News WHERE uuid = :uuid AND isFeatured = 1 LIMIT 1")
    suspend fun getFeaturedNewsByUuid(uuid: String): NewsItem? // vraca NewsItem



    @Transaction
    @Query("SELECT * FROM News")
    fun getAllNewsWithTagsFlow(): Flow<List<NewsWithTags>>

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    fun getNewsWithCategoryFlow(category: String): Flow<List<NewsWithTags>>


    @Transaction
    @Query("SELECT * FROM News") //  "News"
    suspend fun allNews(): List<NewsWithTags>
    @Query("UPDATE News SET isFeatured = :isFeatured WHERE uuid = :uuid")
    suspend fun updateNewsIsFeatured(uuid: String, isFeatured: Boolean)
    @Query("UPDATE News SET isFeatured = 0")
    suspend fun resetAllFeaturedStatus()

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    suspend fun getNewsWithCategory(category: String): List<NewsWithTags>

    @Query("SELECT id FROM Tags WHERE value = :tagValue LIMIT 1")
    suspend fun getTagIdByValue(tagValue: String): Int?

    @Query("SELECT T.value FROM Tags AS T INNER JOIN NewsTags AS NT ON T.id = NT.tagId WHERE NT.newsId = :newsId") // Query tables "Tags", "NewsTags"
    suspend fun getTags(newsId: Int): List<String>

    @Query("SELECT N.* FROM News AS N INNER JOIN NewsTags AS NT ON N.id = NT.newsId INNER JOIN Tags AS T ON NT.tagId = T.id WHERE T.value IN (:tags) GROUP BY N.id ORDER BY N.publishedDate DESC")
    suspend fun getSimilarNews(tags: List<String>): List<NewsItem>

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
    suspend fun saveNews(news: NewsItem): Boolean { // prima NewsItem
        val rowId = insertNews(news)
        return rowId != -1L
    }
}