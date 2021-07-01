package com.freewheelin.pulley.activities.learning.tabFragment

import android.view.LayoutInflater
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MyMockHolder
import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.model.contents.MockExam
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockTestFragmentTest : ContextTest() {

    lateinit var lastHolder: MyMockHolder

    @Before
    fun prepare() {
        val view = LayoutInflater.from(context).inflate(R.layout.item_my_mock_list, null)
        lastHolder = MyMockHolder(view)
    }

    @Test
    fun `set 함수에 따라 적절한 UI가 세팅되어야한다`() {
        var mockTest = MockExam()
        mockTest.subject = "고3 이과"
        mockTest.percent = 85
        mockTest.markingState = "COMPLETED"
        mockTest.score = 35

        lastHolder.set(mockTest)
        assertEquals("가형", lastHolder.typeTv.text)
        assertEquals("35점", lastHolder.scoreTv.text)
        assertEquals("85", lastHolder.percentageTv.text)
        assertEquals("3", lastHolder.ratingTv.text)
        assertEquals(View.VISIBLE, lastHolder.ratingTv.visibility)
        assertEquals(View.GONE, lastHolder.ratingIv.visibility)

        mockTest.score = 35
        mockTest.percent = 99

        lastHolder.set(mockTest)
        assertEquals(View.GONE, lastHolder.ratingTv.visibility)
        assertEquals(View.VISIBLE, lastHolder.ratingIv.visibility)

        mockTest = MockExam()
        mockTest.markingState = "YET"

        lastHolder.set(mockTest)
        assertEquals("-", lastHolder.scoreTv.text)
        assertEquals("-", lastHolder.percentageTv.text)
        assertEquals("-", lastHolder.ratingTv.text)
        assertEquals(View.VISIBLE, lastHolder.ratingTv.visibility)
        assertEquals(View.GONE, lastHolder.ratingIv.visibility)
    }
}