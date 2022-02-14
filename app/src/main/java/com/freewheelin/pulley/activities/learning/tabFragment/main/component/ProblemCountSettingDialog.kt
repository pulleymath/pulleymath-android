package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.freewheelin.pulley.views.PlusMinusButton

interface ProblemCountSettingDialogListener {
    fun onModifyCompleted(cnt: Int)
}

class ProblemCountSettingDialog(context: Context, count: Int): Dialog(context) {

    lateinit var plusMinusBtn: PlusMinusButton
    var modifyBtn: PrimaryButton

    val maxCnt = 30
    val minCnt = 5
    val skipCnt = 5
    var listener: ProblemCountSettingDialogListener? = null

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_problem_count_setting)

        plusMinusBtn = findViewById(R.id.plusMinusBtn)
        modifyBtn = findViewById(R.id.modifyBtn)

        plusMinusBtn.cnt = count

        plusMinusBtn.plusBtn.setOnClickListener {
            if(plusMinusBtn.cnt < maxCnt)
                plusMinusBtn.cnt += skipCnt
        }

        plusMinusBtn.minusBtn.setOnClickListener {
            if(plusMinusBtn.cnt > minCnt)
                plusMinusBtn.cnt -= skipCnt
        }

        plusMinusBtn.plusBtn.setOnLongClickListener(null)
        plusMinusBtn.minusBtn.setOnLongClickListener(null)
        plusMinusBtn.plusBtn.setOnTouchListener(null)
        plusMinusBtn.minusBtn.setOnTouchListener(null)

        modifyBtn.setOnClickListener {
            onModifyBtnClicked()
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "메인", "목표설정")
        UserManager.setUserGoalCount(context, user!!, plusMinusBtn.cnt) {
            dismiss()
            val intent = Intent(UserManager.EVENT_USER_MODIFYING)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            listener?.onModifyCompleted(plusMinusBtn.cnt)
        }
    }
}