package com.freewheelin.pulley.revision2023.room.studymemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.freewheelin.pulley.revision2023.model.StudyMemo

//
@Database(
    entities = [
        StudyMemo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyMemoDatabase: RoomDatabase() {

    abstract fun studyMemoDao(): StudyMemoDao

    companion object {
        @Volatile
        var INSTANCE: StudyMemoDatabase? = null

        fun getDatabase(context: Context): StudyMemoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyMemoDatabase::class.java,
                    "study_memo"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}