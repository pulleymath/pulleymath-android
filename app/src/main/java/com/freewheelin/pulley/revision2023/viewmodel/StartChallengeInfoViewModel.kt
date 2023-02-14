package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.MyApplication.Companion.user

class StartChallengeInfoViewModel(application: Application): BaseAndroidViewModel(application) {

    val showEmptyContainer = MutableLiveData<Boolean>(false)

    val couponImageUrl: String
        get() {
            return if (user?.serviceType?.isPaidUser == true)
                "https://pulley-common.s3.ap-northeast-2.amazonaws.com/app/images/challenge_paid_user_reward_coupon.png"
            else
                "https://pulley-common.s3.ap-northeast-2.amazonaws.com/app/images/challenge_free_user_reward_coupon.png"
        }
    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }
}