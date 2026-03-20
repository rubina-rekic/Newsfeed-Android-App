package etf.ri.rma.newsfeedapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.model.Tag

class Converters {
    // Ovo su sada ispravni TypeConverter-i za List<Tag>
    @TypeConverter
    fun fromTagList(tags: List<Tag>?): String? {
        if (tags == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(tags)
    }

    @TypeConverter
    fun toTagList(tagsString: String?): List<Tag>? {
        if (tagsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Tag>>() {}.type
        return gson.fromJson(tagsString, type)
    }}

@Database(entities = [NewsItem::class, Tag::class, NewsTagCrossRef::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}