package etf.ri.rma.newsfeedapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class NewsItem(
    val uuid: String,                     // jedinstveni identifikator
    val title: String,                    // naslov vijesti
    val snippet: String,                  // kratak opis vijesti
    val imageUrl: String?,                // URL slike (može biti null)
    val category: String,                 // prva kategorija sa web servisa
    var isFeatured: Boolean,              // istaknutada ne mozda
    val source: String,                   // izvor vijesti
    val publishedDate: String,            // datum objavljivanja
    val imageTags: ArrayList<String> = arrayListOf()
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