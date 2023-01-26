package com.freewheelin.pulley.revision2023.room.priorconcept

import android.content.Context
import androidx.room.*
import com.freewheelin.pulley.revision2023.utils.converters.PriorConceptTagTypeConverter
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        PriorConcept::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(PriorConceptTagTypeConverter::class)
abstract class PriorConceptDatabase: RoomDatabase() {

    abstract fun priorConceptDao(): PriorConceptDao

    companion object {
        @Volatile
        var INSTANCE: PriorConceptDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): PriorConceptDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PriorConceptDatabase::class.java,
                    "prior_concept_database"
                )
                    .addCallback(PriorConceptDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}