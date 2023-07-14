package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeInduceDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog.GuestJoinStep
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ChallengeInduceViewModel(application: Application): BaseAndroidViewModel(application) {


    lateinit var setStep: (GuestJoinStep) -> Unit
    lateinit var replaceStep: (GuestJoinStep) -> Unit
    lateinit var removeStep: (Fragment) -> Unit

    lateinit var onExitClickCallback: (() -> Unit)
    val title = MutableLiveData<String>()
    val induceType = MutableLiveData<ChallengeInduceDialog.Type>()

    lateinit var type: ChallengeInduceDialog.Type
    var course: ChallengeCourse? = null
    fun exitBtn() {
        onExitClickCallback()
    }
}