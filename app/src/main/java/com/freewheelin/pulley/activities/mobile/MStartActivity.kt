package com.freewheelin.pulley.activities.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.utils.FacebookEvent
import com.freewheelin.pulley.utils.show
import kotlinx.android.synthetic.main.m_activity_start.*

class MStartActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.m_activity_start)
        setTopLabelText()
        initMobile()
    }

    private fun initMobile() {

        studentLottie.playAnimation()

        freeStartBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.모바일무료체험)
            startActivity(intent)

            FacebookEvent.log(this, FacebookEvent.VIEW_CONTENTS)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            freeStartGuideTv.show()
            freeStartBtn?.show()
        }, 1000)
    }

    private fun setTopLabelText() {
        val orgStr = getString(R.string.guide_use_only_tablet)
        val boldStr = getString(R.string.text_tablet_and_pc)
        val startIndex = orgStr.indexOf(boldStr)
        val spannable = SpannableStringBuilder(orgStr)
        spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@MStartActivity, R.color.purple_6D6DFF)),
                startIndex,
                startIndex + boldStr.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        topLabel.text = spannable
    }
}
