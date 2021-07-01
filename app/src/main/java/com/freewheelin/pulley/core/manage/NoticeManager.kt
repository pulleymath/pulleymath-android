package com.freewheelin.pulley.core.manage

import com.freewheelin.pulley.model.Notice
import com.freewheelin.pulley.utils.DateTimeUtils


object NoticeManager {
    var notices = listOf<Notice>()

    fun isNeedUpdateTag(): Boolean {
        val recentNotice = notices.maxWith(Comparator { a, b ->
            a.dateTime.compareTo(b.dateTime)
        })

        return if(recentNotice == null)
            false
        else
            DateTimeUtils.isNeedUpdateTag(recentNotice.dateTime)

    }
}

