package com.freewheelin.pulley.activities.learning.tabFragment

import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.mockExam.MyMockHolder
import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.model.contents.PersonalData
import org.junit.Assert.*
import org.junit.Before
import org.robolectric.annotation.Config
import org.junit.Test
@Config(qualifiers = "sw600dp")
class MockTestFragmentTest : ContextTest() {

    lateinit var lastHolder: MyMockHolder

    @Before
    fun prepare() {
        lastHolder = MyMockHolder(
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_my_mock_list, null, false)
        )
    }

    @Test
    fun `set 함수에 따라 적절한 UI가 세팅되어야한다`() {
        var mockTest = MockExam()
        mockTest.subject = "고3 이과"
        mockTest.majorType = 2
        mockTest.personalData = PersonalData().apply {
            percent = 85
            markingState = "COMPLETED"
            score = 35
            rating = 3
        }

        lastHolder.set(mockTest)
        assertEquals("가형", lastHolder.binding.typeTv.text)
        assertEquals("35점", lastHolder.binding.scoreTv.text)
        assertEquals("85%", lastHolder.binding.percentageTv.text)
        assertEquals("3", lastHolder.binding.ratingTv.text)
        assertEquals(View.VISIBLE, lastHolder.binding.ratingTv.visibility)
        assertEquals(View.INVISIBLE, lastHolder.binding.ratingIv.visibility)

        mockTest.personalData?.apply {
            percent = 99
            rating = 1
        }

        lastHolder.set(mockTest)
        assertEquals(View.INVISIBLE, lastHolder.binding.ratingTv.visibility)
        assertEquals(View.VISIBLE, lastHolder.binding.ratingIv.visibility)

        mockTest = MockExam()
        mockTest.personalData = PersonalData().apply {
            markingState = "YET"
        }

        lastHolder.set(mockTest)
        assertEquals("-", lastHolder.binding.scoreTv.text)
        assertEquals("-", lastHolder.binding.percentageTv.text)
        assertEquals("-", lastHolder.binding.ratingTv.text)
        assertEquals(View.VISIBLE, lastHolder.binding.ratingTv.visibility)
        assertEquals(View.INVISIBLE, lastHolder.binding.ratingIv.visibility)
    }
}