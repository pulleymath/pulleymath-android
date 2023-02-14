package com.freewheelin.pulley.revision2023.room.challenge

//
//@Database(
//    entities = [
//        MainChallengeDetailItem::class
//    ],
//    version = 1,
//    exportSchema = false
//)
//@TypeConverters(
//    value = [
//        ChallengeStatusTypeConverter::class,
//        ChallengeFormatTypeConverter::class,
//        ChallengeRewardTypeConverter::class,
//        ChallengeCourseListTypeConverter::class,
//    ]
//)
//abstract class MainChallengeDetailDatabase: RoomDatabase() {
//
//    abstract fun dao(): MainChallengeDetailDao
//
//    companion object {
//        @Volatile
//        var INSTANCE: MainChallengeDetailDatabase? = null
//
//        fun getDatabase(context: Context, applicationScope: CoroutineScope): MainChallengeDetailDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    MainChallengeDetailDatabase::class.java,
//                    "main_challenge_detail_item_database"
//                )
//                    .addCallback(MainChallengeDetailDatabaseCallback(applicationScope))
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}