package com.freewheelin.pulley.legacy.utils

import android.content.Context
import com.facebook.appevents.AppEventsLogger

object FacebookEvent {
    const val VIEW_CONTENTS     = "콘텐츠 조회"
    const val SIGNUP_COMPLETED  = "등록완료"
    const val TUTORIAL_FINISHED = "튜토리얼 완료"
    const val SUBSCRIBE_STARTED = "받아보기"
    const val TRIAL_STARTED     = "체험판 시작"
    const val PAYMENT_STARTED   = "결제 시작"

    var logger: AppEventsLogger? = null

    // facebook
    fun log(context: Context, log:String) {
        if(logger == null) {
            logger = AppEventsLogger.newLogger(context)
        }
        logger?.logEvent(log)
    }
}