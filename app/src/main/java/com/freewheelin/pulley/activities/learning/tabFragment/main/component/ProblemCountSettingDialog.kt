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
import kotlinx.android.synthetic.main.button_plus_minus.*
import kotlinx.android.synthetic.main.dialog_problem_count_setting.*

interface ProblemCountSettingDialogListener {
    fun onModifyCompleted(cnt: Int)
}

class ProblemCountSettingDialog(context: Context, count: Int): Dialog(context) {
    val cnt get() = plusMinusBtn.cnt
    val maxCnt = 30
    val minCnt = 5
    val skipCnt = 5
    var listener: ProblemCountSettingDialogListener? = null
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_problem_count_setting)
        plusMinusBtn.cnt = count
        plusBtn.setOnClickListener {
            if(cnt < maxCnt)
                plusMinusBtn.cnt += skipCnt
        }
        minusBtn.setOnClickListener {
            if(cnt > minCnt)
                plusMinusBtn.cnt -= skipCnt
        }
        plusBtn.setOnLongClickListener(null)
        minusBtn.setOnLongClickListener(null)
        plusBtn.setOnTouchListener(null)
        minusBtn.setOnTouchListener(null)
        modifyBtn.setOnClickListener {
            onModifyBtnClicked()
        }
    }

    private fun onModifyBtnClicked() {
        LogUtils.logEvent(context!!, user, PulleyEvent.BUTTON_CLICK, "메인", "목표설정")
        UserManager.setUserGoalCount(context, user!!, cnt) {
            dismiss()
            val intent = Intent(UserManager.EVENT_USER_MODIFYING)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            listener?.onModifyCompleted(cnt)
        }
    }
}