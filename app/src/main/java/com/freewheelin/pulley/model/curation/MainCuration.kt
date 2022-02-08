package com.freewheelin.pulley.model.curation

import android.content.Context
import android.net.Uri
import android.util.Log
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.partialFont
import java.net.URI

class MainCuration {
    companion object {
        const val TEST: String = "test"
        const val BOOK = "book"
        const val MOCK = "mo"
        const val NOTE = "note"
        const val MY_STUDY = "mystudy"
        const val wifiErrorMsg = "앗! 풀리수학과 접속이 끊겼어요!\n" +
                "Wifi 연결이 필요해요!"
        const val errorAnalysisDefaultMsg = "풀리수학과 공부하러 오셨군요!\n" +
                "오늘은 어떤 문제를 풀어볼까요?! :)"

        const val errorGuideDefaultMsg = "풀리수학 200% 활용방법의 첫 단계!"
        const val errorDoSomethingDefaultText = "유형학습 바로가기"
    }


    var dayCuration: String = ""
    var mainCuration: String = ""

    var tip: String = ""
    var tipURI: String = ""
    var tipURITextDisplayed: String = ""


    fun getTipText(context: Context): CharSequence {
        return ("TIP : $tip").partialFont(Theme.extraBold(context), "TIP : ")
    }

    fun getBtnDisplayText(): String {
        return "$tipURITextDisplayed >"
    }

    fun getTabIndex(): Int {
        val uri = Uri.parse(tipURI)
        return when (uri.pathSegments[0]) {
            TEST -> 1
            BOOK -> 2
            MOCK -> 3
            NOTE -> 4
            MY_STUDY -> 5
            else -> {
                LogUtils.assert(false, "예상치 못한 tipURI 케이스 ${tipURI}")
                0
            }

        }

    }

}