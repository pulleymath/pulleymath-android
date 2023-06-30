package com.freewheelin.pulley.legacy.activities.auth

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
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2023.viewmodel.AppViewModel
import com.freewheelin.pulley.revision2023.viewmodel.InitSettingCompletedViewModel
import com.freewheelin.pulley.legacy.utils.partialFontAndColored

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        val IS_GUEST_USER = "IS_GUEST_USER"
        val IS_HIGH_SCHOOL_USER = "IS_HIGH_SCHOOL_USER"
        fun getIntent(context: Context, isHighSchoolUser: Boolean, isGuestUser: Boolean = false): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java).apply {
                putExtra(IS_GUEST_USER, isGuestUser)
                putExtra(IS_HIGH_SCHOOL_USER, isHighSchoolUser)
            }
        }
    }

    val viewModel: InitSettingCompletedViewModel by viewModels()
    private val binding: ActivityInitSettingCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_setting_complete, null, false)
    }
    var isGuestUser = false
    var isHighSchoolUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isGuestUser = intent.getBooleanExtra(IS_GUEST_USER, false)
        isHighSchoolUser = intent.getBooleanExtra(IS_HIGH_SCHOOL_USER, true)
        viewModel.updateSchoolType(isHighSchoolUser)

        viewModel.getAppSignupMessage()

        viewModel.signupMessage.observe(this) {
            var message = it.message
            var changedMessage: CharSequence = message
            it.highlight?.forEach {
                changedMessage = changedMessage.partialFontAndColored(Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_300), it)
            }
            binding.guideTv.text = changedMessage
        }
        binding.startBtn.setOnClickListener {
            moveToMain()
        }
    }

    private fun moveToMain() {
        val intent = LearningTabActivity.getIntent(this)
        startActivity(intent)

        if (isGuestUser) {
            val userUpdateIntent = Intent(UserManager.EVENT_USER_UPDATE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(userUpdateIntent)
            finish()
        } else {
            finishAffinity()
        }
    }
}
