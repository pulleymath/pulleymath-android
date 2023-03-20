package com.freewheelin.pulley.revision2023.room.cookinginfoitem

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2021.model.CookingInfoItem
import com.freewheelin.pulley.revision2023.utils.converters.*
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        CookingInfoItem::class
    ],
    version = 2,
    exportSchema = false
)

@TypeConverters(
    value = [
        CookingInfoItemTypeConverter::class,
        CookingInfoItemQuizTypeConverter::class,
        CookingInfoItemInfoTypeConverter::class,
        CookingInfoExerciseGroupTypeConverter::class,
        CookingInfoItemVideoTypeConverter::class,
    ]
)
abstract class CookingInfoItemDatabase: RoomDatabase() {

    abstract fun cookingInfoItemDao(): CookingInfoItemDao

    companion object {
        @Volatile
        var INSTANCE: CookingInfoItemDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): CookingInfoItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookingInfoItemDatabase::class.java,
                    "cooking_info_item_database"
                )
                    .addCallback(CookingInfoItemDatabaseCallback(applicationScope))
                    .addMigrations(MIGRATION_1_TO_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private val MIGRATION_1_TO_2: Migration = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.run {
                    execSQL("ALTER TABLE lc_cooking_info_item_table ADD COLUMN studentId TEXT NULLABLE DEFAULT ''")
                }
            }
        }
    }

}