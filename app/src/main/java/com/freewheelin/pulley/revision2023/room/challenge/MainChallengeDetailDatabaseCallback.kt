package com.freewheelin.pulley.revision2023.room.challenge

//import androidx.room.RoomDatabase
//import androidx.sqlite.db.SupportSQLiteDatabase
//import com.freewheelin.pulley.revision2023.model.PaidServiceType
//import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
//import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeDetailItem
//import com.freewheelin.pulley.revision2023.room.challenge.MainChallengeDetailDatabase.Companion.INSTANCE
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//
//class MainChallengeDetailDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
//    override fun onCreate(db: SupportSQLiteDatabase) {
//        super.onCreate(db)
//
//        INSTANCE?.let { database ->
//            applicationScope.launch {
//                // TODO init database
////                createDummyHeaderItem(database.dao())
//            }
//        }
//    }
//}