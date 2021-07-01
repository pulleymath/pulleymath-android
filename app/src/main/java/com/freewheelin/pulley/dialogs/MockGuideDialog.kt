package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_mock_guide.*

interface MockGuideDialogListener {
    fun onStartBtnClicked(dialog: MockGuideDialog, isOMR: Boolean)
}


class MockGuideDialog: Dialog {

    var listener: MockGuideDialogListener? = null

    constructor(context: Context): super(context) {
        setContentView(R.layout.dialog_mock_guide)

        guideTv.text = "시험지를 프린트하여 풀이하신다면 [실전 OMR 모드]를\n태블릿에서 문제를 보며 풀고 싶다면\n[바로 풀기 모드]를 선택해주세요!"

        xBtn.setOnClickListener {
            dismiss()
        }

        startBtn.setOnClickListener {
            listener?.onStartBtnClicked(this,true)
        }

        startWithoutPrintBtn.setOnClickListener {
            listener?.onStartBtnClicked(this,false)
        }
    }
}