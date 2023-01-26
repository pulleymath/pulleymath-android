package com.freewheelin.pulley.revision2023.room.pattern

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.room.priorconcept.PriorConceptDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PatternDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        PriorConceptDatabase.INSTANCE?.let { database ->
            applicationScope.launch {
                // TODO init database
            }
        }
    }
}