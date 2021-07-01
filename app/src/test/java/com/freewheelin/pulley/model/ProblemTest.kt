package com.freewheelin.pulley.model

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.assertEquals

class ProblemTest {
    @Test
    fun `json Object 변환이 잘 되어야 한다`() {
        val gson = Gson()
        val problem = gson.fromJson<Problem>("""

{
"studyID": 13769,
        "unitCode": 332100024,
        "answerData": "2",
        "userAnswer": "2",
        "problemID": 448247,
        "problemLevel": 1,
        "problemType": "객관식",
        "problemURL": "/math_problems/Mo/MO_201904/h3/201904_Ky_B/1_",
        "unit": "지수함수의 극한(1) : lim{x->0} e^x-1 /x 꼴의 극한",
        "parentProblemID": 0,
        "page": 0,
        "problemNum": 1,
        "category": "ORIGIN",
        "totalTimes": 50,
        "correctTimes": 18,
        "result": 1,
        "clear": false,
        "scrap": true
}

        """.trimIndent(), Problem::class.java)

        assertEquals(448247, problem.id)
        assertEquals(332100024, problem.unitCode)
        assertEquals(ProblemType.single, problem.problemType)
        assertEquals(1, problem.problemNum)
        assertEquals(0.36f, problem.correctRate)
        assertEquals("ORIGIN", problem.rawCategory)
        assertEquals(false, problem.isClear)
        assertEquals(true, problem.isScrap)
    }
}


