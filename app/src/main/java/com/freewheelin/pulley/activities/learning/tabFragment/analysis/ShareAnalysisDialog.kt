package com.freewheelin.pulley.activities.learning.tabFragment.analysis

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.editText.PulleyTextFieldListener
import kotlinx.android.synthetic.main.dialog_analysis_share.*

interface ShareAnalysisDialogListener {
    fun onDownloadClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap)
    fun onShareBtnClicked(dialog: ShareAnalysisDialog, bitmap: Bitmap)
}
class ShareAnalysisDialog: Dialog {
    var listener: ShareAnalysisDialogListener? = null
    val text = listOf(
            "이루기 위해서 미루지 말자",
            "오늘보다 더 나은 내일",
            "계속 갈망하라. 언제나 우직하게!",
            "시간은 절대 나를 기다려 주지 않는다",
            "공부만큼은 노력을 배신하지 않는다",
            "남과 비교하지 말고 전과 비교하라"
    )
    constructor(context: Context, dailySummary: DailyStudy): super(context) {
        setContentView(R.layout.dialog_analysis_share)
        setCanceledOnTouchOutside(false)
        initUI()
        shareContents.setUpUI(dailySummary)
        val guide = text[NumberUtils.rand(0,6)]
        memoTv.setText(guide)

    }
    fun initUI() {
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
            listener?.onDownloadClicked(this, shareContents.getBitmap(1080, 1080))
        }
        shareBtn.setOnClickListener {
            LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "데일리서머리", "공유하기버튼", getEventValue())
            listener?.onShareBtnClicked(this, shareContents.getBitmap(1080, 1080))
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val existCurrentFocus = currentFocus ?: return super.onTouchEvent(event)
        context.hideKeyboard(existCurrentFocus)

        return super.onTouchEvent(event)
    }

    fun getEventValue(): String {
        val showingPercentageOnOff = if(percentageSwitch.isChecked) "on" else "off"
        val showingDateOnOff = if(todaySwitch.isChecked) "on" else "off"
        val memoValue = if(memoTv.text?.isNotEmpty() == true) "메모" else ""

        var eventValue = "백분위 ${showingPercentageOnOff}, 날짜 ${showingDateOnOff}"

        if(memoValue.isNotEmpty())
            eventValue += ", ${memoValue}"

        return eventValue
    }
}