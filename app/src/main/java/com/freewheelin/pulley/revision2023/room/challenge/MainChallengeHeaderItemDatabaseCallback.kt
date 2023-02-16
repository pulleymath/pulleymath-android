package com.freewheelin.pulley.revision2023.room.challenge

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeStatus
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.room.challenge.MainChallengeHeaderItemDatabase.Companion.INSTANCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainChallengeHeaderItemDatabaseCallback(private val applicationScope: CoroutineScope): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let {
            applicationScope.launch {
                // TODO init database
//                createDummyHeaderItem(database.headerItemDao())
            }
        }
    }
//    private suspend fun createDummyHeaderItem(dao: MainChallengeHeaderItemDao) {
//        val dummyList = mutableListOf(
//            MainChallengeHeaderItem(
//                challengeId = 0,
//                status = ChallengeStatus.ACTIVE,
//                name = "스타트 챌린지",
//                targetServiceType = listOf(
//                    PaidServiceType.BASIC_C,
//                    PaidServiceType.BASIC_P,
//                    PaidServiceType.STANDARD,
//                    PaidServiceType.PREMIUM,
//                    PaidServiceType.PAID_ING
//                ),
//                seq = 0,
//                enabledDuplicate = false
//            ),
//            MainChallengeHeaderItem(
//                challengeId = 1,
//                status = ChallengeStatus.ACTIVE,
//                name = "위클리 챌린지",
//                targetServiceType = listOf(
//                    PaidServiceType.BASIC_C,
//                    PaidServiceType.BASIC_P,
//                    PaidServiceType.STANDARD,
//                    PaidServiceType.PREMIUM,
//                    PaidServiceType.PAID_ING
//                ),
//                seq = 0,
//                enabledDuplicate = false
//            ),
//            MainChallengeHeaderItem(
//                challengeId = 2,
//                status = ChallengeStatus.ACTIVE,
//                name = "수학 (상) 2주완성",
//                targetServiceType = listOf(
//                    PaidServiceType.BASIC_C,
//                    PaidServiceType.BASIC_P,
//                    PaidServiceType.STANDARD,
//                    PaidServiceType.PREMIUM,
//                    PaidServiceType.PAID_ING
//                ),
//                seq = 0,
//                enabledDuplicate = false
//            ),
//            MainChallengeHeaderItem(
//                challengeId = 3,
//                status = ChallengeStatus.ACTIVE,
//                name = "기하 4주완성",
//                targetServiceType = listOf(
//                    PaidServiceType.BASIC_C,
//                    PaidServiceType.BASIC_P,
//                    PaidServiceType.STANDARD,
//                    PaidServiceType.PREMIUM,
//                    PaidServiceType.PAID_ING
//                ),
//                seq = 0,
//                enabledDuplicate = false
//            )
//        )
//        dummyList.forEach { item -> dao.insert(item) }


//    }
}