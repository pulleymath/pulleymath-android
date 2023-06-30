package com.freewheelin.pulley.legacy.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import com.freewheelin.pulley.legacy.utils.*

class FAQTest {

    @Test
    fun `json object 변환이 잘 되야 한다`() {
        val gson = Gson()
        val faq = gson.fromJson<FAQ>("""
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
        """.trimIndent(), FAQ::class.java)

        assertEquals(1, faq.id)
        assertEquals("공지사항 고고", faq.subject)
        assertEquals("I1", faq.headline)
        assertEquals("내용고고고고고고", faq.contents)
        assertEquals("string", faq.imageUrl)
        assertEquals(2014, faq.dateTime.year())
        assertEquals(2, faq.dateTime.month())
        assertEquals(18, faq.dateTime.day())

    }
}