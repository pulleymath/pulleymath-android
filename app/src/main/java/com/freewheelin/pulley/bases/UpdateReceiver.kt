package com.freewheelin.pulley.bases

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.freewheelin.pulley.activities.SplashActivity

class UpdateReceiver: BroadcastReceiver() {

    companion object {
        private val TAG = UpdateReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive ${intent?.action ?: "unknown action"}")
        if (intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intent)
        }
    }
}