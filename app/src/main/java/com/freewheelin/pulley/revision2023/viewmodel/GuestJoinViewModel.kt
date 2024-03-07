package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog.GuestJoinStep
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class GuestJoinViewModel(application: Application): BaseAndroidViewModel(application) {
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)


    lateinit var setStep: (GuestJoinStep) -> Unit
    lateinit var replaceStep: (GuestJoinStep) -> Unit
    lateinit var removeStep: (Fragment) -> Unit

    lateinit var onExitClickCallback: (() -> Unit)
    val loginBtnEnabled = MutableLiveData<Boolean>()

    var isOnceRequested = false

    fun exitBtn() {
        onExitClickCallback()
    }

    fun getAppToken(email: String, pw: String, cb: (ResponseBody<SignInAppToken>) -> Unit, errorHandle: (ResponseBody<SignInAppToken>) -> Unit) {
        compositeDisposable += API_V3.getAppToken(RequestLogin(email, pw))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                println("group error = res:${res.error}")
                res.data?.let {
                    MyApplication.token = it.token
                    _isLoading.postValue(false)
                    isOnceRequested = false
                    sendLoginLog(null, email, true)
                }
                cb(res)
            }, { error ->

                _isLoading.postValue(false)
                sendLoginLog(null, email, false)
                isOnceRequested = false
//                        DialogUtils.v2LoginErrDialog(this@LoginActivity)
                (error as? HttpException)?.response()?.errorBody()?.string()?.let {
                    val listType = object: TypeToken<ResponseBody<SignInAppToken>>(){}.type
                    val response: ResponseBody<SignInAppToken> = Gson().fromJson(it, listType)
                    Log.e(javaClass.simpleName, "group error=${error.localizedMessage} , ${error.message}, ${response.error}, ${response.message}")
                    errorHandle(response)

                }


            })
    }
    fun setLoadingProgress(show: Boolean) {
        _isLoading.postValue(show)
    }
    fun sendLoginLog(user: UserV4?, attemptedEmail: String, loginSuccess: Boolean) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            postLog(user, attemptedEmail, loginSuccess)
        }
    }
    suspend fun postLog(user: UserV4?, attemptedEmail: String, loginSuccess: Boolean): V2LogUserResponse {
        return legacyV2Repository.postLoginLog(
            studentID = user?.studentID,
            email = attemptedEmail,
            itemName = if (loginSuccess) "성공" else "실패",
        )
    }
}