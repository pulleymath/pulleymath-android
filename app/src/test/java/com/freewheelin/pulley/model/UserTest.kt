package com.freewheelin.pulley.model

import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.lib.ContextTest
import com.freewheelin.pulley.utils.DateTimeUtils
import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.assertEquals

class UserTest: ContextTest() {

    @Test
    fun `json Object 변환이 잘되어야한다`() {
        val gson = Gson()
        val user = gson.fromJson<User> ("""
          {
      "firstName": "기성",
      "lastName": "권",
    "agreeMarketing": false,
    "agreeAppPush": false,
    "schoolLocation": "서울특별시",
    "cellPhone": "01031245615",
    "studentID": "I3",
    "email": "sori@mathflat.com",
    "schoolName": "프리윌린고",
    "majorType": "A",
    "initMoGrade": 0,
    "currentCorrectRate": 30,
    "responseCode": "SUCCESS",
    "recommendLevel": 0,
    "recommendChapter": 0,
    "recommendStudyPoint": 0,
    "initStudied": "3110,3111,3112,3120,3210,3211,3212,3220,3221,3222,3312,3322,3332",
    "serviceName": "서비스 서비스스",
    "startDate": "2019-06-26T15:00:00.000+0000",
    "endDate": "2019-12-26T15:00:00.000+0000",
    "initSettingCompleted": false
  }
        """.trimIndent(), User::class.java)

        assertEquals("기성", user.firstName)
        assertEquals("권", user.lastName)
//        assertEquals("권기성", user.name)
        assertEquals("01031245615", user.cellPhone)
        assertEquals("sori@mathflat.com", user.email)
        assertEquals("서울특별시",user.schoolLocation)
        assertEquals("프리윌린고",user.schoolName)
        assertEquals(Major.liberal_arts, user.major)
        assertEquals(false, user.initSettingCompleted)
        assertEquals(false, user.agreeAppPush)
        assertEquals(false, user.agreeMarketing)
        assertEquals("서비스 서비스스", user.serviceName)
//        assertEquals("2019.06.27", DateTimeUtils.yyyyMMddFormat.format(user.startDate))
//        assertEquals("2019.12.27", DateTimeUtils.yyyyMMddFormat.format(user.endDate))
    }


    @Test
    fun `rawField에 따른 해당 field 값들이 적절하게 리턴되어야한다`() {
//        val user = User()
//        user.rawInitStudied = "3110"
//        assertEquals(setOf(BigUnit.다항식), user.studiedUnit)
//
//        user.rawInitStudied = "3110,3111,3112"
//        assertEquals(setOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식), user.studiedUnit)
//
//        user.rawInitStudied = "3321,3332,3111"
//        assertEquals(setOf(BigUnit.미분법, BigUnit.공간도형, BigUnit.방정식과_부등식), user.studiedUnit)
//
//        user.rawInitStudied = ""
//        assertEquals(setOf<BigUnit>(), user.studiedUnit)
    }

}