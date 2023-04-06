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
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2023.viewmodel.AppViewModel
import com.freewheelin.pulley.revision2023.viewmodel.InitSettingCompletedViewModel
import com.freewheelin.pulley.utils.partialFontAndColored

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java)
        }
    }

    val viewModel: InitSettingCompletedViewModel by viewModels()
    private val binding: ActivityInitSettingCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_setting_complete, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.getAppSignupMessage()

        viewModel.signupMessage.observe(this) {
            binding.guideTv.text = it.message.partialFontAndColored(Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_6D6DFF), it.highlight)
        }
        binding.startBtn.setOnClickListener {
            moveToMain()
        }
    }

    private fun moveToMain() {
        startActivity(Intent(this, LearningTabActivity::class.java))
        finishAffinity()
    }
}
