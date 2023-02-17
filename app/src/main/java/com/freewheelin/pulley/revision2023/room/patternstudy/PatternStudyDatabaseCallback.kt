package com.freewheelin.pulley.revision2023.room.patternstudy

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.room.priorconcept.PriorConceptDatabase.Companion.INSTANCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PatternStudyDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let { database ->
            applicationScope.launch {
                // TODO init database
            }
        }
    }
}