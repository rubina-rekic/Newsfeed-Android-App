package etf.ri.rma.newsfeedapp.room.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import etf.ri.rma.newsfeedapp.model.NewsItem



data class NewsWithTags(
    @Embedded val newsItem: NewsItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(NewsTagCrossRef::class, parentColumn = "newsId", entityColumn = "tagId")
    )
    val tags: List<Tag>
) {

    fun toNewsItem(): NewsItem {
        return newsItem.copy(imageTags = ArrayList(tags.map { it.value }))
    }
}