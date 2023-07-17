package com.freewheelin.pulley.revision2023.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityWhaleSpaceLoginBinding
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity.Companion.USER_TOKEN
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity.Companion.socialLoginFinished
import com.freewheelin.pulley.revision2023.viewmodel.WhaleSpaceLoginViewModel
import java.util.UUID


class WhaleSpaceLoginActivity : AppCompatActivity() {

    val binding: ActivityWhaleSpaceLoginBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_whale_space_login, null, false)
    }
    val viewModel: WhaleSpaceLoginViewModel by viewModels()


    val endpoint = "https://auth.whalespace.io/oauth2/v1.1/authorize"
    val clientId = "HGooZch3UpTdhnKgH_5o"
//    val clientSecret = "5mRPQ6WASG"
    val redirectUri = "https://dev.pulleymath.com/redirect"
    var isCustomTabInit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (intent != null) {
            val data = intent.data
            if (data != null && data.path != null) {
                handleAuthCallback(data)
                return
            }
        }
        authorize()
    }

    private fun handleAuthCallback(uri: Uri) {
        uri.getQueryParameter("code")?.let { code ->
            viewModel.sendCode(code) {
                // TODO token setting
                intent.putExtra(USER_TOKEN, "userToken")
                setResult(socialLoginFinished, intent)
            }
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
            .appendQueryParameter("scope", "")
            .appendQueryParameter("state", state)
            .build()

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        customTabsIntent.launchUrl(this, uri)
    }

    override fun onResume() {
        super.onResume()
        if (isCustomTabInit) {
            setResult(socialLoginFinished, intent)
            finish()
        }
        isCustomTabInit = true
    }

}