package com.freewheelin.pulley.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.bases.isNetworkConnected
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.model.ResponseBody
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

private val DEFAULT_ERROR_HANDLE: (Throwable) -> Unit = {

    Log.e("DEFAULT_ERROR_HANDLE", it.localizedMessage)
    it.printStackTrace()

}

fun responseError(context: Context, cb: (() -> Unit)? =null): (Throwable) -> Unit = {
    Log.e("DEFAULT_ERROR_HANDLE", it.localizedMessage)
    it.printStackTrace()

    if(!context.isNetworkConnected)
        DialogUtils.showNetworkErr(context)
    else
        DialogUtils.showServerErr(context)

    if(cb != null)
        cb()
}

fun responseError(context: Context, response: Response<*>) {
    if(response.code() == 500) {
        LogUtils.assert(false , "response 500 ERROR: response: ${response}")
        DialogUtils.showServerErr(context)

    } else if (response.code() == 404) {
        DialogUtils.v2NotFoundErrDialog(context) {
            LogUtils.assert(false , "response 404 ERROR: response: ${response}")
        }
    }
}

fun responseError(context: Context, response: Response<*>, param: Parameter) {
    Log.e("유사문제", "error code=${response.code()}")

    if(response.code() == 500) {
        LogUtils.assert(false , "response 500 ERROR: response: ${response}\n" +
                "param: ${param}")
        DialogUtils.showServerErr(context)
    } else if(response.code() == 400) {
        DialogUtils.showServerErr(context)
    }
}

fun responseFailed(context: Context, throwable: Throwable, isShown500Error:Boolean = true) {
    Log.e("DEFAULT_ERROR_HANDLE", throwable.localizedMessage as String)
    throwable.printStackTrace()

    if(!context.isNetworkConnected)
        DialogUtils.showNetworkErr(context)
    else if (BuildConfig.FLAVOR == "beta") {
        DialogUtils.showDialog(context, throwable.message.toString(), "베타버전만 출력됨")
    } else {
        DialogUtils.showServerErr(context)
    }
}


fun <T> Observable<T>.subscribeOnIO() : Observable<T> {
    return this.subscribeOn(Schedulers.io())
}

fun <T> Observable<T>.onUI(onNext: (T) -> Unit): Disposable {
    return this.onUI(onNext, DEFAULT_ERROR_HANDLE)
}
fun <T> checkErrorAndReturn(context: Context, res: ResponseBody<T>): T? {
    if (res.error != null) {
        responseFailed(context, Throwable("${res.error} ${res.message}"))
        return null
    }
    return res.data
}

fun <T> Observable<T>.onUI(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {

    return this
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, onError)


}
