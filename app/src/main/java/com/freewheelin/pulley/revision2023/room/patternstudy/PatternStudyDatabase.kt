package com.freewheelin.pulley.revision2023.room.patternstudy
//
//import android.content.Context
//import androidx.room.*
//import com.freewheelin.pulley.legacy.model.contents.Book
//import com.freewheelin.pulley.revision2023.model.PriorConcept
//import com.freewheelin.pulley.revision2023.utils.converters.*
//import kotlinx.coroutines.CoroutineScope
//
//@Database(
//    entities = [
//        Book::class
//    ],
//    version = 1,
//    exportSchema = false
//)
//@TypeConverters(
//    value = [
//        PatternStudyBookPageTypeConverter::class,
//        PatternStudyBookCategoryTypeConverter::class,
//        PatternStudyProblemTypeConverter::class,
//        PatternStudyTempSimilarProblemTypeConverter::class,
//        PatternStudyPieceCategoryTypeConverter::class,
//        PatternStudyPublicDataTypeConverter::class,
//        PatternStudyDateTypeConverter::class,
//    ]
//)
//abstract class PatternStudyDatabase: RoomDatabase() {
//
//    abstract fun patternStudyDao(): PatternStudyDao
//
//    companion object {
//        @Volatile
//        var INSTANCE: PatternStudyDatabase? = null
//
//        fun getDatabase(context: Context, applicationScope: CoroutineScope): PatternStudyDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    PatternStudyDatabase::class.java,
//                    "pattern_study_database"
//                )
//                    .addCallback(PatternStudyDatabaseCallback(applicationScope))
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//}