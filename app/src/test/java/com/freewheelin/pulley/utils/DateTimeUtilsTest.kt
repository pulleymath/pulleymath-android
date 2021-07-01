package com.freewheelin.pulley.utils

//import org.junit.Test
import junit.framework.Assert.*
import java.util.*
import org.junit.jupiter.api.Test


class DateTimeUtilsTest {

    @Test
    fun `LocalDate는 한국 타임 존 기준으로 나와야한다`() {
        val localDate = org.joda.time.LocalDate(2017,3,3)

        assertEquals(1488466800, localDate.toDateTimeAtStartOfDay().millis / 1000)
    }

    @Test
    fun `LocalDate를 Date타입 오브젝트로 변환해도 한국시간기준으로 변화가 없어야한다`() {
        val localDate = org.joda.time.LocalDate(2017,3,3)

        assertEquals(1488466800, localDate.toDate().time / 1000)
    }

    @Test
    fun testGetDate() {

        val calendar = Calendar.getInstance()
        // GMT + 09:00 기준
        val _2018_01_17 = Date(1516147200000)
        val _3DayAgo_2018_01_17 = DateTimeUtils.getDate(_2018_01_17, DateUnit.Day, -3)
        calendar.time = _3DayAgo_2018_01_17

        assertEquals(2018, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(14, calendar.get(Calendar.DAY_OF_MONTH))

        val _1WeekAgo_2018_01_17 = DateTimeUtils.getDate(_2018_01_17, DateUnit.Week, -1)
        calendar.time = _1WeekAgo_2018_01_17

        assertEquals(2018, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH))


        val _1MonthAgo_2018_01_17 = DateTimeUtils.getDate(_2018_01_17, DateUnit.Month, -1)
        calendar.time = _1MonthAgo_2018_01_17

        assertEquals(2017, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH))


        // GMT + 09:00 기준
        val _2015_03_31 = Date(1427760000000)

        val _1MonthAgo_2018_03_31 = DateTimeUtils.getDate(_2015_03_31, DateUnit.Month, -1)
        calendar.time = _1MonthAgo_2018_03_31

        assertEquals(2015, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals(28, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRand() {
        for (i in 0 until 200) {
            var rand = DateTimeUtils.rand()
            println(rand)
            assertTrue(rand > Date(1514732400000))
            assertTrue(rand < Date())
        }
    }

    @Test
    fun `date의 year, month, day 함수는 적절한 값을 반환해야한다`() {
        val _2019_01_01_00_00 = Date(1546268400000)
        assertEquals(2019, _2019_01_01_00_00.year())
        assertEquals(1, _2019_01_01_00_00.month())
        assertEquals(1, _2019_01_01_00_00.day())
    }

    @Test
    fun testIsSameDate() {
        val _2018_01_17_00_00 = Date(1516114800000)

        val _2018_01_17_15_30 = Date(1516170600000)

        assertTrue(DateTimeUtils.isSameDate(_2018_01_17_00_00, _2018_01_17_15_30))

        val _2018_01_13_00_00 = Date(1515769200000)

        val _2018_01_13_23_59 = Date(1515855540000)

        assertTrue(DateTimeUtils.isSameDate(_2018_01_13_00_00, _2018_01_13_23_59))

        val _2018_01_18_00_00 = Date(1516201200000)

        val _2018_01_19_00_00 = Date(1516287600000)

        assertFalse(DateTimeUtils.isSameDate(_2018_01_18_00_00, _2018_01_19_00_00))
    }

    @Test
    fun testGetBeforeDate() {
        val _2018_01_17_00_00 = Date(1516114800000)

        val _2018_01_17_15_30 = Date(1516170600000)
        assertEquals("오늘", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00,_2018_01_17_15_30))

        val _2018_01_18_00_00 = Date(1516201200000)
        assertEquals("어제", DateTimeUtils.getBeforeDateStr(_2018_01_18_00_00,_2018_01_17_15_30))

        val _2018_01_15_00_00 = Date(1515942000000)
        assertEquals("2일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_15_00_00))

        val _2018_01_14_00_00 = Date(1515855600000)
        assertEquals("3일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_14_00_00))

        val _2018_01_13_00_00 = Date(1515769200000)
        assertEquals("4일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_13_00_00))

        val _2018_01_12_00_00 = Date(1515682800000)
        assertEquals("5일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_12_00_00))

        val _2018_01_11_00_00 = Date(1515596400000)
        assertEquals("6일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_11_00_00))

        val _2018_01_10_00_00 = Date(1515510000000)
        assertEquals("7일 전", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_10_00_00))

        val _2018_01_09_00_00 = Date(1515423600000)
        assertEquals("01-09", DateTimeUtils.getBeforeDateStr(_2018_01_17_00_00, _2018_01_09_00_00))
    }

    @Test
    fun `should return proper period`() {
        assertEquals(1, DateTimeUtils.getPeriod(
                org.joda.time.LocalDate(2018,1,17),
                org.joda.time.LocalDate(2018,1,17)
        ))

        assertEquals(14, DateTimeUtils.getPeriod(
                org.joda.time.LocalDate(2018,1,17),
                org.joda.time.LocalDate(2018,1,30)
        ))

        assertEquals(365, DateTimeUtils.getPeriod(
                org.joda.time.LocalDate(2017,1,1),
                org.joda.time.LocalDate(2017,12,31)
        ))
    }

    @Test
    fun `getDayDifferences를 부르면 적절히 call되어야한다`() {
        val _2018_01_17_00_00 = Date(1516114800000)
        val _2018_01_17_15_30 = Date(1516170600000)

        assertEquals(0, DateTimeUtils.getDayDifferences(_2018_01_17_00_00,_2018_01_17_15_30))
        assertEquals(0, DateTimeUtils.getDayDifferences(_2018_01_17_15_30,_2018_01_17_00_00))

        val _2018_01_17_23_59 = Date(1516201199000)
        val _2018_01_18_00_00 = Date(1516201200000)

        assertEquals(-1, DateTimeUtils.getDayDifferences(_2018_01_18_00_00,_2018_01_17_23_59))
        assertEquals(1, DateTimeUtils.getDayDifferences(_2018_01_17_23_59,_2018_01_18_00_00))

        val _2017_01_17_00_00 = Date(1484578800000)
        assertEquals(365, DateTimeUtils.getDayDifferences(_2017_01_17_00_00,_2018_01_17_00_00))
        assertEquals(-365, DateTimeUtils.getDayDifferences(_2018_01_17_00_00,_2017_01_17_00_00))
    }

    @Test
    fun `isNeedNewTag 함수가 날짜에 따라 적절히 true false 반환을해야한다`() {
        val now = Date()
        assertTrue(DateTimeUtils.isNeedNewTag(now))

        val aDayAgo = Date(now.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedNewTag(aDayAgo))

        val twoDaysAgo = Date(aDayAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedNewTag(twoDaysAgo))

        val threeDaysAgo = Date(twoDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(threeDaysAgo))

        val fourDaysAgo = Date(threeDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(fourDaysAgo))

        val fiveDaysAgo = Date(fourDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(fiveDaysAgo))

        val sixDaysAgo = Date(fiveDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(sixDaysAgo))

        val sevenDaysAgo = Date(sixDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(sevenDaysAgo))

        val aMonthAgo = Date(now.time - 30 * DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedNewTag(aMonthAgo))
    }

    @Test
    fun `isNeedUpdateTag 함수가 날짜에 따라 적절히 true false 반환을해야한다`() {
        val now = Date()
        assertTrue(DateTimeUtils.isNeedUpdateTag(now))

        val aDayAgo = Date(now.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(aDayAgo))

        val twoDaysAgo = Date(aDayAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(twoDaysAgo))

        val threeDaysAgo = Date(twoDaysAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(threeDaysAgo))

        val fourDaysAgo = Date(threeDaysAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(fourDaysAgo))

        val fiveDaysAgo = Date(fourDaysAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(fiveDaysAgo))

        val sixDaysAgo = Date(fiveDaysAgo.time - DateUnit.Day.unit)
        assertTrue(DateTimeUtils.isNeedUpdateTag(sixDaysAgo))

        val sevenDaysAgo = Date(sixDaysAgo.time - DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedUpdateTag(sevenDaysAgo))

        val aMonthAgo = Date(now.time - 30 * DateUnit.Day.unit)
        assertFalse(DateTimeUtils.isNeedUpdateTag(aMonthAgo))

    }
}