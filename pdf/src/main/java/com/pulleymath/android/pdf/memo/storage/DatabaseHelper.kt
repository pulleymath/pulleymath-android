package com.pulleymath.android.pdf.memo.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PdfMemo::class], version = 1, exportSchema = false)
abstract class DatabaseHelper : RoomDatabase() {
    abstract fun pdfWritingDao(): PdfMemoDao

    companion object {
        private var instance:DatabaseHelper? = null
        fun get(context: Context) : DatabaseHelper {
            if(instance == null) {
                instance = Room.databaseBuilder(context, DatabaseHelper::class.java, "pdf").build()
            }
            return instance!!
        }
    }
}
