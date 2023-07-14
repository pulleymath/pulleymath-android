package com.freewheelin.pulley.revision2023.model.challenge

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.legacy.utils.DateTimeUtils
import org.joda.time.LocalDateTime
import java.io.Serializable

data class MainChallengeHeaderWrapper(
    val data: List<MainChallengeHeaderItem>,
    val error: String?,
    val message: String?,
    val current_time: String?
)
@Entity(tableName = "main_challenge_header_item_table")
data class MainChallengeHeaderItem(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val status: ChallengeStatus,
    val name: String,
    val seq: Int,
    var isSelected: Boolean = false,
    var studentId: String? = user?.studentID
): Serializable {
//    override fun getId() = "$challengeId"

}

//@Entity(tableName = "main_challenge_detail_item_table")
open class Challenge: Serializable {
    var challengeId: Int = -1
    val userChallengeId: Int? = null
    val userStatus: ChallengeUserStatus = ChallengeUserStatus.YET
    val challengeName: String = ""
    val challengeFormat: ChallengeFormat = ChallengeFormat.ROUTINE
    val totalCourseCount: Int = -1
    val doneCourseCount: Int = -1
    val remainRewardsCount: Int = -1
    val startedAt: String? = null
    val endedAt: String? = null
    val period: String? = null
    val introTitle: String = ""
    val description: String = ""
    val seq: Int = -1
    val reward: ChallengeReward? = null
    val courses: List<ChallengeCourse> = listOf()
    val successfulUserCount: Int? = null

    // ----
    val startChallenge: StartChallenge?
        get() {
            return if (isStartChallenge) { StartChallenge.convertChild(this) } else null
        }
    // -----
    val isStartChallenge: Boolean
        get() {
            return challengeFormat == ChallengeFormat.START
        }

    fun getNextCourse(currentCourseId: Int): ChallengeCourse? {
        return courses
            .filter { it.challengeCourseId > currentCourseId }
            .find { it.status.isInProgress() }
            ?: courses
                .filter { it.challengeCourseId < currentCourseId }
                .find { it.status.isInProgress() }
    }

    fun isStartChallengeAndUserStatusYET(): Boolean {
        return isStartChallenge && userChallengeId == null
   }

    val startBtnText: String
        get() {
            return when (challengeFormat) {
                ChallengeFormat.START -> {
                    when (userStatus) {
                        ChallengeUserStatus.YET -> "챌린지 참여하기"
                        ChallengeUserStatus.ING -> "챌린지 완료하고 쿠폰받기"
                        ChallengeUserStatus.DONE -> {
                            if (remainRewardsCount != 0) {
                                "챌린지 완료하고 쿠폰받기"
                            } else {
                                "쿠폰 발급 완료"
                            }
                        }
                        else -> ""
                    }
                }
                else -> ""
            }
        }

    val showPeriodView: Boolean
        get() {
            return startedAt != null && endedAt != null
        }
    val periodText: String
        get() {
            if (startedAt == null || endedAt == null) return ""
            val startDateTime = LocalDateTime.parse(startedAt)
            val startDateStr = DateTimeUtils.yy_MM_dd.format(startDateTime.toDate())

            val endDateTime = LocalDateTime.parse(endedAt)
            val endDateStr = DateTimeUtils.yy_MM_dd.format(endDateTime.toDate())
            return "$startDateStr ~ $endDateStr"
        }

    var finishEffectAlreadyAppear: Boolean = false
}

data class ChallengeReward (
    val id: Int,
    val challengeId: Int,
    val type: ChallengeRewardType,
    val name: String,
    val rewardImageUrl: String?,
    val rewardValue: String,
    val isDeleted: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
):Serializable {

}

data class ChallengeCourse(
    val userChallengeCourseId: Int,
    val challengeCourseId: Int,
    val courseName: String,
    val status: ChallengeUserStatus,
    val targetValue: Int,
    val doneValue: Int,
    val eventType: String?, // type?
    val eventContent: String?, // type?
    val linkedLocation: String?,
    val baseImgUrl: String?,
    val doneImgUrl: String?,
    val parentDetailItem: Challenge?
): BaseDiffItem, Serializable {
    override fun getId() = "$userChallengeCourseId"

    val stampText: String
        get() {
            val txt = if (challengeCourseId < 5) {
                return courseName
            } else {
                "$courseName $doneValue/$targetValue"
            }
            return txt
        }
    val completedSubTitle: String
        get() {
            return "다음 미션으로 이동해 진행해보세요:)"
        }
}


enum class ChallengeFormat {
    START, ROUTINE, STUDY
}

enum class ChallengeStatus {
    ACTIVE, INACTIVE, END;

    fun isNotAvailable(): Boolean {
        return this == INACTIVE
    }
}
enum class ChallengeUserStatus {
    YET, ING, DONE, FAILED;

    fun isInProgress(): Boolean {
        return this == ING || this == YET
    }
}
enum class ChallengeRewardType {
    COUPON,
}