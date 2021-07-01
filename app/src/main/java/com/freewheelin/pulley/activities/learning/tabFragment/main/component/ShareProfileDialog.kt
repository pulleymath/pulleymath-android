package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.utils.extensionTouchArea
import com.freewheelin.pulley.utils.getBitmap
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.dialog_share_profile.*


interface ShareProfileDialogListener {
    fun onDownloadClicked(dialog: ShareProfileDialog, bitmap: Bitmap)
    fun onShareBtnClicked(dialog: ShareProfileDialog, bitmap: Bitmap)
}

class ShareProfileDialog: Dialog {
    var listener: ShareProfileDialogListener? = null

    constructor(context: Context, profile: MainProfile): super(context) {
        setContentView(R.layout.dialog_share_profile)
        setCanceledOnTouchOutside(false)
        initUI()
        shareContents.setProfileUI(profile)
    }
    fun initUI() {
        xBtn.extensionTouchArea(8.toPx())
        xBtn.setOnClickListener {
            dismiss()
        }

        downloadBtn.setOnClickListener {
            listener?.onDownloadClicked(this, shareContents.getBitmap(1080, 1080))
        }
        shareBtn.setOnClickListener {
            listener?.onShareBtnClicked(this, shareContents.getBitmap(1080, 1080))
        }
    }
}