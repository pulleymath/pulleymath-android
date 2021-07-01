package com.freewheelin.pulley.lib

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.facebook.soloader.SoLoader
import com.freewheelin.pulley.bases.MyApplication
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
open class ContextTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            SoLoader.setInTestMode()
            MyApplication.isTest = true
        }
    }

    var context: Context = ApplicationProvider.getApplicationContext<Context>()


    @Test
    fun test() {}
}