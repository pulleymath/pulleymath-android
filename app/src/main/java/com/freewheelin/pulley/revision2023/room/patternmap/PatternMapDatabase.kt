package com.freewheelin.pulley.revision2023.room.patternmap

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2023.utils.converters.PatternMapProgressTypeConverter
import com.freewheelin.pulley.revision2023.model.LCPatternMap
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        LCPatternMap::class
    ],
    version = 2,
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
                    .addMigrations(MIGRATION_1_TO_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private val MIGRATION_1_TO_2: Migration = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.run {
                    execSQL("ALTER TABLE lc_pattern_map_table ADD COLUMN studentId TEXT NULLABLE DEFAULT ''")
                }
            }
        }
    }

}