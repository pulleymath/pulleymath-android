package com.freewheelin.pulley.activities.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.utils.partialFontAndColored
import kotlinx.android.synthetic.main.activity_init_setting_complete.*

class InitSettingCompleteActivity : AppCompatActivity() {
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InitSettingCompleteActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_setting_complete)

        startBtn.setOnClickListener {
            val intent = InitTestActivity.getIntent(this)
            startActivity(intent)
            finish()
        }
        guideTv.text = "기본정보가 제출되었습니다!\n이제 ${user!!.fullName}님의 공부 스타일을 진단해드릴게요 :)"
                .partialFontAndColored( Theme.extraBold(this), ContextCompat.getColor(this, R.color.purple_6D6DFF), "${user!!.fullName}님의 공부 스타일")
    }
}
