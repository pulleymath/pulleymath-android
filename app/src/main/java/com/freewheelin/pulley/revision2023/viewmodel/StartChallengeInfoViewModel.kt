package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2023.model.PaidServiceType

class StartChallengeInfoViewModel(application: Application): BaseAndroidViewModel(application) {

    var isChallengeFinished: Boolean = false
    var challengeId: Int = 0

    val couponImageUrl: String
        get() {
//            return if (user?.serviceType?.isPaidUser == true)
//                "https://pulley-common.s3.ap-northeast-2.amazonaws.com/app/images/challenge_paid_user_reward_coupon.png"
//            else
            return "https://pulley-common.s3.ap-northeast-2.amazonaws.com/app/images/challenge_free_user_reward_coupon.png"
        }

    val startBtnText: String
        get() {
            return if (isChallengeFinished) { "쿠폰 사용하기" }
            else { "챌린지 참여하기" }
        }
    val subTitleTvText: String
        get() {
            return if (isChallengeFinished) { "지금 바로 50% 할인 쿠폰을 사용하거나\n마이페이지 > 쿠폰함에서 확인할 수 있어요 :)" }
            else { "본 챌린지를 통해 개념부터 워크북까지\n풀리수학+ 프리미엄을 체험해 볼 수 있어요." }
        }
    val titleTvText: String
        get() {
            return if (isChallengeFinished) { "쿠폰 발급이 완료됐어요!" }
            else if (user?.serviceType != PaidServiceType.NONE) {
                "스타트 챌린지에 참여해 할인 쿠폰을 받으세요!"
            }
            else "스타트 챌린지에 참여해 쿠폰을 받으세요!"
        }
    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }
}