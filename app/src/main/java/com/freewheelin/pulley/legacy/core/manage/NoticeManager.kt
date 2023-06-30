package com.freewheelin.pulley.legacy.core.manage

import com.freewheelin.pulley.legacy.model.Notice
import com.freewheelin.pulley.legacy.utils.DateTimeUtils


object NoticeManager {
    var notices = listOf<Notice>()

    fun isNeedUpdateTag(): Boolean {
        val recentNotice = notices.maxWithOrNull(Comparator { a, b ->
            a.dateTime.compareTo(b.dateTime)
        })

        return if(recentNotice == null)
            false
        else
            DateTimeUtils.isNeedUpdateTag(recentNotice.dateTime)

    }
}

