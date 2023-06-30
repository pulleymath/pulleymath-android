package com.freewheelin.pulley.legacy.model

import com.google.gson.Gson
import com.freewheelin.pulley.legacy.utils.year
import com.freewheelin.pulley.legacy.utils.month
import com.freewheelin.pulley.legacy.utils.day
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class NoticeTest {

    @Test
    fun `json object 변환이 잘 되야 한다`() {
        val gson = Gson()
        val notice = gson.fromJson<Notice>("""
    {
      "category": "NOTICE",
      "contents": "내용고고고고고고",
      "dateTime": "2014-02-18T08:43:04.251Z",
      "headline": "I1",
      "imageUrl": "string",
      "noticeID": 1,
      "studentID": "I1",
      "subject": "공지사항 고고"
    }
        """.trimIndent(), Notice::class.java)

        assertEquals(1, notice.id)
        assertEquals("공지사항 고고", notice.subject)
        assertEquals("I1", notice.headline)
        assertEquals("내용고고고고고고", notice.contents)
        assertEquals("string", notice.imageUrl)
        assertEquals(2014, notice.dateTime.year())
        assertEquals(2, notice.dateTime.month())
        assertEquals(18, notice.dateTime.day())

    }


    @Test
    fun `updateTag가 뜰지 안뜰지 적절하게 판단해야한다`() {

        val notice = Notice()
        notice.dateTime = Date()

        assertTrue(notice.isNeedUpdateTag())


        val currentDate = Date()

        println(currentDate.time)

    }
}
