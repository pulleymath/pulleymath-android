package com.freewheelin.pulley.revision2023.room.challenge

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.utils.converters.challenge.ChallengeStatusTypeConverter
import com.freewheelin.pulley.revision2023.utils.converters.challenge.PaidServiceListTypeConverter
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        MainChallengeHeaderItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    value = [
        ChallengeStatusTypeConverter::class,
        PaidServiceListTypeConverter::class
    ]
)
abstract class MainChallengeHeaderItemDatabase: RoomDatabase() {

    abstract fun headerItemDao(): MainChallengeHeaderItemDao

    companion object {
        @Volatile
        var INSTANCE: MainChallengeHeaderItemDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): MainChallengeHeaderItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainChallengeHeaderItemDatabase::class.java,
                    "main_challenge_header_item_database"
                )
                    .addCallback(MainChallengeHeaderItemDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}