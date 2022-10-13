package com.freewheelin.pulley.revision2021.cookingmemo.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PulleyCookingMemo::class], version = 1, exportSchema = false)
abstract class DatabaseHelper : RoomDatabase() {
    abstract fun pulleyCookingWritingDao(): PulleyCookingMemoDao

    companion object {
        private var instance: DatabaseHelper? = null
        fun get(context: Context) : DatabaseHelper {
            if(instance == null) {
                instance = Room.databaseBuilder(context, DatabaseHelper::class.java, "pulley_cooking_memo").build()
            }
            return instance!!
        }
    }
}