package com.freewheelin.pulley.legacy.utils

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.challenge.OnceAppearInfoByStudentId
import com.google.gson.Gson

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
    var onServerAPI = APPreference(Network.Server.live.toString())
    var onLoggingEvent = APPreference(false)
    var onSuccessToast = APPreference(false)
//    var testBaseURL = APPreference("https://api-staging.pulleymath.com")
    var testBaseURL = APPreference("https://api-dev.pulleymath.com")
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
    val tooltipShowingCntAddSimilarOfStartChallenge = APPreference(0)
    val tooltipShowingCntChangeSimilar = APPreference(0)
    val tooltipShowingCntAdditionalStudyInAnalysis = APPreference(0)
    val tooltipShowingCntAdditionalStudyInWrongNote = APPreference(0)
    val tooltipShowingCntMail = APPreference(0)
    val tooltipShowingCntAnalysisMain = APPreference(0)
    val tooltipShowingCntRecommendPlan = APPreference(0)
    val tooltipShowingCntMiddleOpening = APPreference(0)
    val galleryClickCnt = APPreference(0)
    val univGalleryClickCnt = APPreference(0)

    val answerXPosition = APPreference(-1f)
    val answerYPosition = APPreference(-1f)

    val isAvailableRushDialog = APPreference(true)

    val targetDate = APPreference(0L)
    val targetDateTitle = APPreference("")
    val targetID = APPreference(-1)

    val forceUpdateDialogCount = APPreference(0)

    var channelTalkCurrChatId = APPreference("")
    var studentIdWhenIssuingChatId = APPreference("")
    var channelTalkUserId = APPreference("")

    var isConceptLearningTutorialPassed = APPreference(false)
    var floatingAnswerSheetLastLocation = APPreference("")
    val _startChallengeAlreadyAppeared = APPreference("")
    var startChallengeAlreadyAppeared: OnceAppearInfoByStudentId
        get() {
            val infoStr = _startChallengeAlreadyAppeared.get()
            return Gson().fromJson(infoStr, OnceAppearInfoByStudentId::class.java) ?: OnceAppearInfoByStudentId(listOf())
        }
        set (value) {
            val scInfoStr = Gson().toJson(value)
            _startChallengeAlreadyAppeared.set(scInfoStr)
        }

    val _guestWelcomeMessageAppeared = APPreference("")
    var guestWelcomeMessageAppeared: OnceAppearInfoByStudentId
        get() {
            val infoStr = _guestWelcomeMessageAppeared.get()
            return Gson().fromJson(infoStr, OnceAppearInfoByStudentId::class.java) ?: OnceAppearInfoByStudentId(listOf())
        }
        set (value) {
            val scInfoStr = Gson().toJson(value)
            _guestWelcomeMessageAppeared.set(scInfoStr)
        }
    val createdUUID = APPreference("")
    var signedEmail = APPreference("")
    var schoolType = APPreference("")

    val _checkPlanMakeBtnClicked = APPreference("")
    var checkPlanMakeBtnClicked: OnceAppearInfoByStudentId
        get() {
            val infoStr = _checkPlanMakeBtnClicked.get()
            return Gson().fromJson(infoStr, OnceAppearInfoByStudentId::class.java) ?: OnceAppearInfoByStudentId(listOf())
        }
        set (value) {
            val scInfoStr = Gson().toJson(value)
            _checkPlanMakeBtnClicked.set(scInfoStr)
        }

}


