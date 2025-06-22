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

    @TypeConverter
    fun fromStringList(list: ArrayList<String>?): String? {
        if (list == null) return null
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStringList(jsonString: String?): ArrayList<String>? {
        if (jsonString == null) return null
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}

// IMPORTANT: Increased version from 1 to 2
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
                    .fallbackToDestructiveMigration() // IMPORTANT: Added this for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}