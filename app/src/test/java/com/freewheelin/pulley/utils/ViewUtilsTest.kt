package com.freewheelin.pulley.utils

import android.view.View
import com.freewheelin.pulley.lib.ContextTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewUtilsTest: ContextTest() {

    @Test fun setPaddingLeft() {
        val view = View(context)
        view.setPadding(30,40,50,60)
        view.setPaddingLeft(60)

        assertEquals(60, view.paddingLeft)
        assertEquals(40, view.paddingTop)
        assertEquals(50, view.paddingRight)
        assertEquals(60, view.paddingBottom)
    }

    @Test fun setPaddingRight() {
        val view = View(context)
        view.setPadding(30,40,50,60)
        view.setPaddingRight(60)

        assertEquals(30, view.paddingLeft)
        assertEquals(40, view.paddingTop)
        assertEquals(60, view.paddingRight)
        assertEquals(60, view.paddingBottom)

        view.setPaddingRight(100)
        assertEquals(100, view.paddingRight)
    }

    @Test fun setPaddingBottom() {
        val view = View(context)
        view.setPadding(30,40,50,60)
        view.setPaddingBottom(60)

        assertEquals(30, view.paddingLeft)
        assertEquals(40, view.paddingTop)
        assertEquals(50, view.paddingRight)
        assertEquals(60, view.paddingBottom)

        view.setPaddingBottom(20)
        assertEquals(20, view.paddingBottom)

        view.setPaddingBottom(0)
        assertEquals(0, view.paddingBottom)
    }
}