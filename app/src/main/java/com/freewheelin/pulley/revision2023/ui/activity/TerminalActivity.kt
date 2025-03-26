package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.HttpException401
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.NONE
import com.freewheelin.pulley.revision2023.viewmodel.TerminalViewModel
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.tasks.OnCompleteListener

class TerminalActivity : AppCompatActivity() {
    val viewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terminal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = intent
        if (Intent.ACTION_VIEW.equals(intent.action)) {
            intent.data?.let {
                val token = it.getQueryParameter("token")
                if (token == null) {
                    unauthorizedAccess(401)
                    return
                }
                MyApplication.token = token

                viewModel.fetchUserFromSSCoaching {
                    MyApplication.user = it
                    MyApplication.token = it.token
                    commitUser()
                    putFcmToken()
                    viewModel.fetchMainProfile {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                }

            }

        }
        viewModel.apply {
            errorAction.observe(this@TerminalActivity) { type ->
                when(type) {
                    HttpException401 -> {
                        unauthorizedAccess(4010)
                        Log.e(javaClass.simpleName, "401 Auth Error Not Handled : ${type}")
                    }
                    NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
        }
    }

    private fun unauthorizedAccess(error: Int) {
        DialogUtils.confirmV2(this,
            "확인되지 않은 접근입니다.",
            "문제가 계속되면 카카오톡 @풀리는수학으로 문의주세요. -${error}",
            isOneBtn = true,
            successCb = {
                finishAffinity();
                System.exit(0);
            })
    }

    private fun commitUser() {
        MyApplication.user?.commit("TerminalActivity")
    }
    private fun putFcmToken() {
        if(MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    viewModel.putFirebaseToken(token)
                }
            })
        }
    }
}