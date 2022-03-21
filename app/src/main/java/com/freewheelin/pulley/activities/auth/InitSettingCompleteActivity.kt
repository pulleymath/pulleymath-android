package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ActivityInitSettingCompleteBinding
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
            val intent = InitTestActivity.getIntent(this)
            startActivity(intent)
            finish()
        }
        binding.guideTv.text = "기본정보가 제출되었습니다!\n이제 ${user!!.fullName}님의 공부 스타일을 진단해드릴게요 :)"
                .partialFontAndColored( Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_6D6DFF), "${user!!.fullName}님의 공부 스타일")
    }
}
