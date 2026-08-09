package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityWhaleSpaceLoginBinding
import com.freewheelin.pulley.legacy.activities.StartActivity
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.usesWebApp
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.viewmodel.WhaleSpaceLoginViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID


class WhaleSpaceLoginActivity : AppCompatActivity() {

    val binding: ActivityWhaleSpaceLoginBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_whale_space_login, null, false)
    }
    val viewModel: WhaleSpaceLoginViewModel by viewModels()


    val endpoint = "https://auth.worksmobile.com/oauth2/v2.0/authorize"
    val clientId = "_R0zU6c0PFjN_mm658N9"
    val redirectUri = "${Network.homePageUrl}/signin/complete/whalespace/android"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            loadingLottie.playAnimation()
            reTryBtn.setOnClickListener {
                val intent = Intent(this@WhaleSpaceLoginActivity, StartActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val autoAction = intent.getBooleanExtra("AUTO_ACTION", false)
        intent?.let { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.removeFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.removeFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            intent.data?.let {
                if (it.path != null && it.path?.startsWith("/complete/whalespace") == true) {
                    handleAuthCallback(it)
                    return
                }
            }
        }
        if (autoAction) {
            authorize()
        }
    }

    private fun handleAuthCallback(uri: Uri) {
        uri.getQueryParameter("code")?.let { code ->
            viewModel.sendCode(code) {
                handleResponse(it)
            }
        }
    }

    fun handleResponse(res: ResponseBody<SignInAppToken>) {
        if (res.error == null) {
            when (res.data?.isValidPhone) {
                false -> {
                    ConfirmPhoneDialog(this, successCB = {
                        goLearningTab()
                    }, failCB = { clearToken() }).show()
                }
                else -> {
                    goLearningTab()
                }
            }
        } else {
            clearToken()
            errorHandle(res)
        }
    }
    private fun goLearningTab() {
        viewModel.fetchUser {
            MyApplication.user = it
            MyApplication.token = it.token
            MyApplication.user?.commit("WhaleSpaceLoginActivity")

            putFcmToken()
            viewModel.sendLoginLog(it, it.accountEmail)

            val userUpdateIntent = Intent(UserManager.EVENT_USER_UPDATE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(userUpdateIntent)
            viewModel.fetchMainProfile {
                // 교육청(AIEP)·웨일스페이스 사용자는 네이티브 Main 대신 WebView로 webapp을 사용한다
                val intent = if (it.signInChannel.usesWebApp) {
                    AiepWebViewActivity.webAppIntent(this)
                } else {
                    Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                finishAffinity()
            }
        }
    }
    fun putFcmToken() {
        if(MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                val token = task.result
                if (token?.isNotEmpty() == true) {
                    viewModel.putFcmToken(token)
                }
            })
        }
    }
    fun authorize() {
        val state = UUID.randomUUID().toString()
        val preferences = this.getSharedPreferences("OAUTH_STORAGE", Context.MODE_PRIVATE)
        preferences.edit()
            .putString("OAUTH_STATE", state)
            .apply()

        val uri = Uri.parse(endpoint)
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", "openid")
            .appendQueryParameter("scope", "email")
            .appendQueryParameter("scope", "profile")
            .appendQueryParameter("scope", "user.whalespace.read")
            .appendQueryParameter("state", state)
            .build()

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        customTabsIntent.launchUrl(this, uri)
    }
    private fun clearToken() {
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user?.commit("WhaleSpaceLoginActivity")
    }

    private fun errorHandle(res: ResponseBody<SignInAppToken>) {
        val error = res.error
        val errMsg = res.message
        binding.apply {
            when (error) {
                "UNAUTHORIZED" -> {
                    DialogUtils.confirmV2(
                        this@WhaleSpaceLoginActivity,
                        title = "인증실패",
                        contents = "문제가 지속될 경우\n카카오톡(@풀리는수학)으로 문의 바랍니다.",
                        isOneBtn = true,
                        successCb = {
                            finish()
                        }
                    )
                }
                else -> {
                    DialogUtils.showServerErr(this@WhaleSpaceLoginActivity)
                }
            }
        }
    }

}