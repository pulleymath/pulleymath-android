package com.freewheelin.pulley.revision2023.room.cooking

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2023.utils.converters.CookingInfoExerciseGroupTypeConverter
import com.freewheelin.pulley.revision2023.utils.converters.CookingInfoItemVideoTypeConverter
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        CookingInfo::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        CookingInfoExerciseGroupTypeConverter::class,
        CookingInfoItemVideoTypeConverter::class,
    ]
)
abstract class CookingInfoDatabase: RoomDatabase() {

    abstract fun cookingInfoDao(): CookingInfoDao

    companion object {
        @Volatile
        var INSTANCE: CookingInfoDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): CookingInfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookingInfoDatabase::class.java,
                    "cooking_info_database"
                )
                    .addCallback(CookingInfoDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}