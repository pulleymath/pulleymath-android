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

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2023.viewmodel.AppViewModel
import com.freewheelin.pulley.revision2023.viewmodel.InitSettingCompletedViewModel
import com.freewheelin.pulley.legacy.utils.partialFontAndColored
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        val IS_GUEST_USER = "IS_GUEST_USER"
        val USER_GRADE = "USER_GRADE"
        fun getIntent(context: Context, grade: Int, isGuestUser: Boolean = false): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java).apply {
                putExtra(IS_GUEST_USER, isGuestUser)
                putExtra(USER_GRADE, grade)
            }
        }
    }

    val viewModel: InitSettingCompletedViewModel by viewModels()
    private val binding: ActivityInitSettingCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_setting_complete, null, false)
    }
    var isGuestUser = false
//    var isHighSchoolUser = false
    var initUserGrade = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isGuestUser = intent.getBooleanExtra(IS_GUEST_USER, false)
        initUserGrade = intent.getIntExtra(USER_GRADE, 0)
        viewModel.updateSchoolType(initUserGrade)

        viewModel.getAppSignupMessage()

        viewModel.signupMessage.observe(this) {
            val message = it.message
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
        viewModel.fetchMainProfile {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            if (isGuestUser) {
                val userUpdateIntent = Intent(UserManager.EVENT_USER_UPDATE)
                userUpdateIntent.putExtra(IS_GUEST_USER, true)
                LocalBroadcastManager.getInstance(this).sendBroadcast(userUpdateIntent)
                finish()
            } else {
                finishAffinity()
            }
        }
    }
}
