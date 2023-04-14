package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2023.viewmodel.AppViewModel
import com.freewheelin.pulley.revision2023.viewmodel.InitSettingCompletedViewModel
import com.freewheelin.pulley.utils.partialFontAndColored

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        val IS_GUEST_USER = "IS_GUEST_USER"
        fun getIntent(context: Context, isGuestUser: Boolean = false): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java).apply {
                putExtra(IS_GUEST_USER, isGuestUser)
            }
        }
    }

    val viewModel: InitSettingCompletedViewModel by viewModels()
    private val binding: ActivityInitSettingCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_setting_complete, null, false)
    }
    var isGuestUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isGuestUser = intent.getBooleanExtra(SignupActivity.IS_GUEST_USER, false)

        viewModel.getAppSignupMessage()

        viewModel.signupMessage.observe(this) {
            var message = it.message
            var changedMessage: CharSequence = message
            it.highlight?.forEach {
                changedMessage = changedMessage.partialFontAndColored(Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_6D6DFF), it)
            }
            binding.guideTv.text = changedMessage
        }
        binding.startBtn.setOnClickListener {
            moveToMain()
        }
    }

    private fun moveToMain() {
        startActivity(Intent(this, LearningTabActivity::class.java))
        if (isGuestUser) {
            val userUpdateIntent = Intent(UserManager.EVENT_USER_UPDATE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(userUpdateIntent)
            finish()
        } else {
            finishAffinity()
        }
    }
}
