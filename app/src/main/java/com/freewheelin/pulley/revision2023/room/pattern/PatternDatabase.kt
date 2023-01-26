package com.freewheelin.pulley.revision2023.room.pattern

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import com.freewheelin.pulley.revision2023.utils.converters.PatternQuizConceptTypeConverter
import com.freewheelin.pulley.revision2023.utils.converters.PatternQuizHintTypeConverter
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        LCPatternQuiz::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        PatternQuizConceptTypeConverter::class,
        PatternQuizHintTypeConverter::class
    ]
)
abstract class PatternDatabase: RoomDatabase() {

    abstract fun patternDao(): PatternDao

    companion object {
        @Volatile
        var INSTANCE: PatternDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): PatternDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatternDatabase::class.java,
                    "lc_pattern_database"
                )
                    .addCallback(PatternDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}