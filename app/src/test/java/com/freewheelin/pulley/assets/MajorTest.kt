package com.freewheelin.pulley.legacy.assets

import com.freewheelin.pulley.legacy.model.Notice
import com.freewheelin.pulley.legacy.utils.day
import com.freewheelin.pulley.legacy.utils.month
import com.freewheelin.pulley.legacy.utils.year
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test

class MajorTest {

    @Test
    fun `value에 의한 major init이 잘 돼야한다`() {
        Assert.assertEquals(Major.common, Major.init("U"))
        Assert.assertEquals(Major.liberal_arts, Major.init("A"))
        Assert.assertEquals(Major.natural_sciences, Major.init("B"))
        Assert.assertEquals(Major.none, Major.init("Bawefaefafe"))
    }
}