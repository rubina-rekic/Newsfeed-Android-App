package etf.ri.rma.newsfeedapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "news",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class NewsItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "snippet") var snippet: String,
    @ColumnInfo(name = "imageUrl") var imageUrl: String?,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "isFeatured") var isFeatured: Boolean,
    @ColumnInfo(name = "source") var source: String,
    @ColumnInfo(name = "publishedDate") var publishedDate: String,
    var imageTags: List<Tag> = emptyList()
)