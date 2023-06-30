package com.freewheelin.pulley.legacy.activities.learning.tabFragment.analysis

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.hideKeyboard
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.databinding.DialogAnalysisShareBinding
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.editText.PulleyTextFieldListener

interface ShareAnalysisDialogListener {
    fun onDownloadClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap)
    fun onShareBtnClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap)
}
class ShareAnalysisDialog(context: Context, dailySummary: DailyStudy) : Dialog(context) {
    val binding: DialogAnalysisShareBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_analysis_share, null, false)
    }
    var listener: ShareAnalysisDialogListener? = null
    val text = listOf(
        "이루기 위해서 미루지 말자",
        "오늘보다 더 나은 내일",
        "계속 갈망하라. 언제나 우직하게!",
        "시간은 절대 나를 기다려 주지 않는다",
        "공부만큼은 노력을 배신하지 않는다",
        "남과 비교하지 말고 전과 비교하라"
    )

    init {
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        initUI()
        binding.apply {
            shareContents.setUpUI(dailySummary)
            val guide = text[NumberUtils.rand(0, 6)]
            memoTv.setText(guide)
        }
    }
    fun initUI() {
        binding.apply {
            xBtn.extensionTouchArea(8.toPx())
            xBtn.setOnClickListener {
                dismiss()
            }

            percentageSwitch.setOnCheckedChangeListener { button, isCheceked ->
                shareContents.isPercentVisible = isCheceked
            }
            todaySwitch.setOnCheckedChangeListener { button, isChecked ->
                shareContents.isDateVisible = isChecked
            }


            memoTv.listener = object: PulleyTextFieldListener {
                override fun onTextChanged(text: String) {
                    shareContents.guide = text
                }
            }

            downloadBtn.setOnClickListener {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "다운로드버튼", getEventValue())
                listener?.onDownloadClicked(this@ShareAnalysisDialog, shareContents.getBitmap(1080, 1080))
            }
            shareBtn.setOnClickListener {
                LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "공유하기버튼", getEventValue())
                listener?.onShareBtnClicked(this@ShareAnalysisDialog, shareContents.getBitmap(1080, 1080))
            }


        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val existCurrentFocus = currentFocus ?: return super.onTouchEvent(event)
        context.hideKeyboard(existCurrentFocus)

        return super.onTouchEvent(event)
    }

    fun getEventValue(): String {
        binding.apply {
            val showingPercentageOnOff = if (percentageSwitch.isChecked) "on" else "off"
            val showingDateOnOff = if (todaySwitch.isChecked) "on" else "off"
            val memoValue = if (memoTv.text?.isNotEmpty() == true) "메모" else ""

            var eventValue = "백분위 ${showingPercentageOnOff}, 날짜 ${showingDateOnOff}"

            if (memoValue.isNotEmpty())
                eventValue += ", ${memoValue}"

            return eventValue
        }
    }
}