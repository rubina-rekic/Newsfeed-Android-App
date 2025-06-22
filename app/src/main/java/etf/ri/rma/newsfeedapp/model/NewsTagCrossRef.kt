package etf.ri.rma.newsfeedapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import etf.ri.rma.newsfeedapp.model.Tag

@Entity(
    tableName = "NewsTags",
    primaryKeys = ["newsId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NewsItem::class,
            parentColumns = ["id"],
            childColumns = ["newsId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class NewsTagCrossRef(
    val newsId: Int,
    val tagId: Int
)