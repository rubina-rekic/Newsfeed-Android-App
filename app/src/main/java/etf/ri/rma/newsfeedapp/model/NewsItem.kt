package etf.ri.rma.newsfeedapp.model
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
)

