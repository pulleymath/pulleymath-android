package com.freewheelin.pulley.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.revision2021.repository.AlarmRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PulleyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val target = if (message.data.isNotEmpty()) {
            message.data["target_android"] ?: "FIREBASE-PUSH"
        } else {
            "FIREBASE-PUSH"
        }
        //TODO target 관련 분기
        message.notification?.let {
            sendNotification(it.title, it.body);

        }
    }

    override fun onNewToken(token: String) {
        Log.d(javaClass.simpleName, "new Token=>$token")
        if(user?.token?.isNotEmpty() == true) {
            Log.d(javaClass.simpleName, "new Token=>$token")
            if (token.isNotEmpty()) {
                API_APP.putToken(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        Log.d(javaClass.simpleName, "토큰=$token")
                    }, { })
            }
        }
    }
    private fun sendNotification(messageTitle: String?, messageBody: String?) {
        val intent = Intent(this, LearningTabActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_small_icon)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name)
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}