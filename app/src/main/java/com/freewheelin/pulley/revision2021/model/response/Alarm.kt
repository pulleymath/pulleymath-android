package com.freewheelin.pulley.revision2021.model.response

import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.response.base.BaseAlarmResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class Alarm : BaseDiffItem, Serializable {
    var messageID: Int = 0
    lateinit var studentID: String
    lateinit var messageType: String
    lateinit var title: String
    lateinit var contents: String
    var linkUrl: String? = null
    lateinit var createdAt: String
    var isRead: Boolean = false
    var readAt: String? = null

    val isReadObservable: ObservableBoolean = ObservableBoolean(false)

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA) }

    fun whenDidYouGetMessage(currentTime: String?): String {
        // 리턴은 오늘, 1일 전 - 7일 전, 2020.05.12 이런식이다.
        if (currentTime == null) {
            return ""
        }
        val createdDate = sdf.parse(createdAt)
        val createdDateLong = createdDate.time
        val parsedCurrentServerDate = sdf.parse(currentTime)
        val serverDateLong = parsedCurrentServerDate.time
        val timeDiff: Int = ((serverDateLong - createdDateLong) / 1000).toInt()

        return getDateDiffString(timeDiff)
    }
    private fun getDateDiffString (timeDiff: Int): String {
        val maximumSec = 60
        val maximumMin = 60 * 60
        val maximumHour = 60 * 60 * 24
        val maximumWeek = 60 * 60 * 24 * 7
        return when {
            timeDiff < maximumSec -> {
                "${timeDiff}초 전"
            }
            timeDiff < maximumMin -> {
                val result = timeDiff / maximumSec
                "${result}분 전"
            }
            timeDiff < maximumHour -> {
                val result = timeDiff / maximumMin
                "${result}시간 전"
            }
            timeDiff < maximumWeek -> {
                val result = timeDiff / maximumHour
                "${result}일 전"
            }
            else -> {
                val customSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                val customDate = customSdf.parse(createdAt)
                customDate.toString()
            }
        }
    }

    fun getDetailMessageTime(): String {
        val createdDate = sdf.parse(createdAt)
        val newFormat by lazy { SimpleDateFormat("yyyy-MM-dd a HH:mm", Locale.KOREA) }

        val messageTime = newFormat.format(createdDate)
        return messageTime
    }
    override fun equals(other: Any?): Boolean {
        return messageID == (other as Alarm).messageID
    }
    override fun getId() = "$messageID"
}
class AlarmResponse : BaseAlarmResponse<Alarm>()
class AlarmReadResponse : BaseSingleResponseNode<Alarm>()
