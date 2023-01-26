package com.freewheelin.pulley.revision2023.room.cookinginfoitem

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.room.cookinginfoitem.CookingInfoItemDatabase.Companion.INSTANCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CookingInfoItemDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let { database ->
            applicationScope.launch {
                // TODO init database
            }
        }
    }
}