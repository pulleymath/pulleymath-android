package com.freewheelin.pulley.activities.mobile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.utils.FacebookEvent
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.utils.show
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.buttons.PrimaryButton
import java.lang.Exception

class MStartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.m_activity_start)
        setTopLabelText()
        initMobile()
    }

    private fun initMobile() {
        findViewById<LottieAnimationView>(R.id.studentLottie).playAnimation()

        findViewById<PrimaryButton>(R.id.freeStartBtn).setOnClickListener {
            FacebookEvent.log(this, FacebookEvent.VIEW_CONTENTS)
            IntentUtils.openWebLink(this, URL.모바일무료체험, this.packageManager)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<TextView>(R.id.freeStartGuideTv).show()
            findViewById<PrimaryButton>(R.id.freeStartBtn).show()
        }, 1000)
    }

    private fun setTopLabelText() {
        val orgStr = getString(R.string.guide_use_only_tablet)
        val boldStr = getString(R.string.text_tablet_and_pc)
        val startIndex = orgStr.indexOf(boldStr)
        val spannable = SpannableStringBuilder(orgStr)
        spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@MStartActivity, R.color.purple_300)),
                startIndex,
                startIndex + boldStr.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        findViewById<TextView>(R.id.topLabel).text = spannable
    }
}
