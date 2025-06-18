package etf.ri.rma.newsfeedapp.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import etf.ri.rma.newsfeedapp.model.NewsItem

@Entity(
    tableName = "NewsTags",
    primaryKeys = ["newsId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NewsItem::class,
            parentColumns = ["id"],
            childColumns = ["newsId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NewsTagCrossRef(
    val newsId: Int,
    val tagId: Int
)