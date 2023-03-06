package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.utils.partialFontAndColored

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java)
        }
    }
    private val binding: ActivityInitSettingCompleteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_init_setting_complete, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            moveToMain()

//            if (isTablet) {
//                moveToLearningCourseTutorial()
//            } else {
//                moveToMain()
//            }
        }
        binding.guideTv.text = "회원가입이 완료되었습니다!\n이제 풀리수학과 공부를 시작해볼까요?"
                .partialFontAndColored( Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_6D6DFF), "풀리수학")
    }

    private fun moveToLearningCourseTutorial() {
        LCTutorialActivity.getIntent(this).let {
            startActivity(it)
            finishAffinity()
        }
    }

    fun moveToMain() {
        startActivity(Intent(this, LearningTabActivity::class.java))
        finishAffinity()
    }
}
