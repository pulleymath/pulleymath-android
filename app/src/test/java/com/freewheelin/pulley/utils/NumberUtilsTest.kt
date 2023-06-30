package com.freewheelin.pulley.legacy.utils

import org.junit.Test

class NumberUtilsTest {
    @Test
    fun `should not crash when call rand`() {
        /**
         *  정상적인 사용
         */
        NumberUtils.rand(0 ,30)

        /**
         * from 과 to가 같은경우
         */
        NumberUtils.rand(30,30)

        /**
         * from이 더 클경우
         */
        NumberUtils.rand(30,0)
    }
}
