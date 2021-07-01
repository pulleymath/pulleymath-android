package com.freewheelin.pulley.activities.mypage

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.model.User
import kotlinx.android.synthetic.main.dialog_my_study_info_setting.*

interface MyPageSettingDialogListener {
    fun onModifyCompleted(user: User)
}

open class MyPageSettingBaseDialog(context: Context, open val user: User, listener: MyPageSettingDialogListener): Dialog(context) {
    var listener: MyPageSettingDialogListener? = null

    init {
        this.listener = listener
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    override fun setContentView(resId: Int) {
        super.setContentView(resId)
        xBtn.setOnClickListener {
            dismiss()
        }
    }

    fun showCompleteDialog() {
        CompleteDialog(context!!, "수정 완료!\n업데이트되었습니다.", "해당 수정 내역은 추천 문항에 반영됩니다.").showFor()
    }
}
