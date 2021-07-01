package com.freewheelin.pulley.utils

import org.joda.time.Days
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.*


enum class DateUnit(var unit: Long) {
    Month(86400000 * 30),
    Week(86400000 * 7),
    Day(86400000)
}

class DateTimeUtils {
    companion object {

        fun rand(): Date {
            // NOTE: (hyuntae) generate dateTime from 2018.01.01
            var rand = NumberUtils.rand(1514732400, (Date().time / 1000).toInt()).toLong()
            return Date(rand * 1000)
        }

        private var _yyyMMddFormat: SimpleDateFormat? = null
        val yyyyMMddFormat: SimpleDateFormat
            get() {
                if (_yyyMMddFormat == null)
                    _yyyMMddFormat = SimpleDateFormat("yyyy.MM.dd")

                return _yyyMMddFormat!!
            }


        private var _mMddFormat: SimpleDateFormat? = null
        val mMddFormat: SimpleDateFormat
            get() {
                if (_mMddFormat == null)
                    _mMddFormat = SimpleDateFormat("MM월 dd일")

                return _mMddFormat!!
            }

        private var _mMDashddFormat: SimpleDateFormat? = null
        val mMDashddFormat: SimpleDateFormat
            get() {
                if (_mMDashddFormat == null)
                    _mMDashddFormat = SimpleDateFormat("MM-dd")

                return _mMDashddFormat!!
            }

        private var _hMFormat: SimpleDateFormat? = null
        val hMFormat: SimpleDateFormat
            get() {
                if (_hMFormat == null)
                    _hMFormat = SimpleDateFormat("hh:mm")

                return _hMFormat!!
            }

        private var _YYMMdd: SimpleDateFormat? = null
        val YYMMdd: SimpleDateFormat
            get() {
                if(_YYMMdd == null)
                    _YYMMdd = SimpleDateFormat("YYMMdd")
                return _YYMMdd!!
            }

        val yyyy_MM_dd: SimpleDateFormat by lazy {
            SimpleDateFormat("yyyy-MM-dd")
        }

        val yyyyMMdd: SimpleDateFormat by lazy {
            SimpleDateFormat("yyyyMMdd")
        }

        fun getDate(date: Date? = Date(), dateUnit: DateUnit, diff: Int): Date {
            var calendar = Calendar.getInstance()
            calendar.time = date

            val type: Int
            when (dateUnit) {
                DateUnit.Month -> type = Calendar.MONTH
                DateUnit.Week -> type = Calendar.WEEK_OF_YEAR
                DateUnit.Day -> type = Calendar.DAY_OF_YEAR
            }
            calendar.add(type, diff)

            return calendar.time
        }

        fun isSameDate(date1: Date, date2: Date): Boolean {
            var calendar = Calendar.getInstance()
            calendar.time = date1
            var date1Year = calendar.get(Calendar.YEAR)
            var date1Month = calendar.get(Calendar.MONTH)
            var date1Day = calendar.get(Calendar.DATE)


            calendar.time = date2
            var date2Year = calendar.get(Calendar.YEAR)
            var date2Month = calendar.get(Calendar.MONTH)
            var date2Day = calendar.get(Calendar.DATE)

            return (date1Year == date2Year
                    && date1Month == date2Month
                    && date1Day == date2Day)

        }

        fun getBeforeDateStr(today: Date = Date(), date: Date): String {

            if (isSameDate(today, date))
                return "오늘"
            else if (isSameDate(getDate(today, DateUnit.Day, -1), date)) {
                return "어제"
            } else {
                for (i in 2..7) {
                    if (isSameDate(getDate(today, DateUnit.Day, -i), date)) {
                        return "${i}일 전"
                    }
                }

                return mMDashddFormat.format(date)
            }
        }


        fun getPeriod(from: LocalDate, to: LocalDate): Int {
            return Days.daysBetween(from,to).days + 1
        }

        fun getDayDifferences(from: Date, to: Date): Int {
            return Days.daysBetween(LocalDate(from), LocalDate(to)).days
        }

        fun isNeedNewTag(date: Date): Boolean {
            val now = Date()
            return getDayDifferences(date, now) < 3
        }

        fun isNeedUpdateTag(date: Date): Boolean {
            val now = Date()
            return getDayDifferences(date, now) < 7
        }

        fun getHourMinSpentTimeStr(spentTime: Int): String {
            val hour = spentTime / 3600
            val min = (spentTime - (hour * 3600)) / 60

            return String.format("%02d", hour) + ":" + String.format("%02d", min)
        }
    }
}


fun Date.year(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.YEAR)
}

fun Date.month(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.MONTH) + 1
}

fun Date.day(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun Date.dayOfWeek(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_WEEK)
}

fun Date.isToday(): Boolean {
    return DateTimeUtils.isSameDate(this, Date())
}
