package com.freewheelin.pulley.legacy.core.manager

import com.freewheelin.pulley.legacy.core.manage.NoticeManager
import com.freewheelin.pulley.legacy.model.Notice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class NoticeManagerTest {
    @Test
    fun `최신 notice가 있으면 isNeedUpdateTag가 true를 없으면 false를 리턴해야한다`() {
        NoticeManager.notices = listOf(
                Notice().apply { dateTime = Date() },
                Notice().apply { dateTime = Date() },
                Notice().apply { dateTime = Date() }
        )

        assertTrue(NoticeManager.isNeedUpdateTag())


        NoticeManager.notices = listOf(
                Notice().apply { dateTime = Date(1516114800000) },
                Notice().apply { dateTime = Date(1516114800000) },
                Notice().apply { dateTime = Date(1516114800000) }

        )
        assertFalse(NoticeManager.isNeedUpdateTag())
    }

}