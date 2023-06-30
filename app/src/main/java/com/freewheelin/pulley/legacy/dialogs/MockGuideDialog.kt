package com.freewheelin.pulley.legacy.dialogs

import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.freewheelin.pulley.R

interface MockGuideDialogListener {
    fun onStartBtnClicked(dialog: MockGuideDialog, isOMR: Boolean)
}


class MockGuideDialog(context: Context) : Dialog(context) {

    var listener: MockGuideDialogListener? = null

    var guideTv: TextView
    var xBtn: ImageButton
    var startBtn: Button
    var startWithoutPrintBtn: Button


    init {
        setContentView(R.layout.dialog_mock_guide)
        guideTv = findViewById(R.id.guideTv)
        xBtn = findViewById(R.id.xBtn)
        startBtn = findViewById(R.id.startBtn)
        startWithoutPrintBtn = findViewById(R.id.startWithoutPrintBtn)

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