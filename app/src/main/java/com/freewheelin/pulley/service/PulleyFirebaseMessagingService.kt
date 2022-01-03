package com.freewheelin.pulley.service

import android.util.Log
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.API_APP
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PulleyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        /* TODO : 메시지 왔을 때 처리 */
    }

    override fun onNewToken(token: String) {

        if(user?.token?.isNotEmpty() == true) {
            Log.d(javaClass.simpleName, "new Token=>$token")

            API_APP.putToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    Log.d(javaClass.simpleName, "토큰=$token")
                }, { })
        }
    }
}