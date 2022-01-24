package com.freewheelin.pulley.utils

object Preferences {

    val isNovice: Boolean
        get() = appLaunchCount <= 2
    val _appLaunchCount = APPreference(0)
    var appLaunchCount: Int
        get() = _appLaunchCount.get()
        set(value) {
            _appLaunchCount.set(value)
        }

    var onTestAPI = APPreference(false)
    var onLoggingEvent = APPreference(false)
    var onSuccessToast = APPreference(false)
    var testBaseURL = APPreference("https://api-staging.pulleymath.com")
//    var testBaseURL = APPreference("https://api-dev.pulleymath.com")
    var userDataString =  APPreference("")
    var versionDataString = APPreference("")
    val isSpyMode = APPreference(false)
    val isNeedOnboarding = APPreference(true)
    val lastExpiredShowingDate = APPreference(0L)
    val initTestData = APPreference("")

    var shopUrl = APPreference("https://pulleymath.com")
    var devShopUrl = APPreference("https://dev.pulleymath.com")

    val tooltipShowingCntTakeNoteScroll = APPreference(0)
    val tooltipShowingCntAddSimilar = APPreference(0)
    val tooltipShowingCntChangeSimilar = APPreference(0)
    val tooltipShowingCntAdditionalStudyInAnalysis = APPreference(0)
    val tooltipShowingCntAdditionalStudyInWrongNote = APPreference(0)
    val tooltipShowingCntMail = APPreference(0)
    val tooltipShowingCntAnalysisMain = APPreference(0)
    val tooltipShowingCntRecommendPlan = APPreference(0)
    val galleryClickCnt = APPreference(0)

    val answerXPosition = APPreference(-1f)
    val answerYPosition = APPreference(-1f)

    val isAvailableRushDialog = APPreference(true)

    val targetDate = APPreference(0L)
    val targetDateTitle = APPreference("")
    val targetID = APPreference(-1)

}


