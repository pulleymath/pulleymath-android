package com.freewheelin.pulley.revision2023.room.cooking

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.room.cooking.CookingInfoDatabase.Companion.INSTANCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CookingInfoDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let { database ->
            applicationScope.launch {
                // TODO init database
            }
        }
    }
}