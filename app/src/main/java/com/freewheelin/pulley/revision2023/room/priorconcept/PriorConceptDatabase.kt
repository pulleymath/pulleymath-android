package com.freewheelin.pulley.revision2023.room.priorconcept

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.utils.converters.PriorConceptTagTypeConverter
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        PriorConcept::class
    ],
    version = 2,
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
                    .addMigrations(MIGRATION_1_TO_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private val MIGRATION_1_TO_2: Migration = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.run {
                    execSQL("ALTER TABLE lc_prior_concept_table ADD COLUMN sequence INTEGER NOT NULL DEFAULT -1")
                }
            }
        }
    }

}