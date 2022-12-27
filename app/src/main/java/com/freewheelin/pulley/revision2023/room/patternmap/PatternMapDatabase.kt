package com.freewheelin.pulley.revision2023.room.patternmap

import android.content.Context
import androidx.room.*
import com.freewheelin.pulley.revision2021.utils.converters.PatternMapProgressTypeConverter
import com.freewheelin.pulley.revision2021.utils.converters.PriorConceptTagTypeConverter
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        LCPatternMap::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(PatternMapProgressTypeConverter::class)
abstract class PatternMapDatabase: RoomDatabase() {

    abstract fun patternMapDao(): PatternMapDao

    companion object {
        @Volatile
        var INSTANCE: PatternMapDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): PatternMapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatternMapDatabase::class.java,
                    "lc_pattern_map_database"
                )
                    .addCallback(PatternMapDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}