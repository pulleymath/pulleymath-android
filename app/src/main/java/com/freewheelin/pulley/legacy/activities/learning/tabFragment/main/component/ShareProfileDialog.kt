package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.component

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageButton
import android.widget.TextView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.utils.extensionTouchArea
import com.freewheelin.pulley.legacy.utils.getBitmap
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.revision2023.ui.view.CommonButton


interface ShareProfileDialogListener {
    fun onDownloadClicked(dialog: ShareProfileDialog, bitmap: Bitmap)
    fun onShareBtnClicked(dialog: ShareProfileDialog, bitmap: Bitmap)
}

class ShareProfileDialog(context: Context, profile: MainProfile) : Dialog(context) {
    var listener: ShareProfileDialogListener? = null

    lateinit var xBtn: ImageButton
    lateinit var shareContents: ProfileShareContentsView
    lateinit var downloadBtn: ImageButton
    lateinit var shareBtn: CommonButton

    init {
        setContentView(R.layout.dialog_share_profile)
        setCanceledOnTouchOutside(false)
        initUI()
        shareContents.setProfileUI(profile)
    }
    fun initUI() {
        xBtn = findViewById(R.id.xBtn)
        shareContents = findViewById(R.id.shareContents)
        downloadBtn = findViewById(R.id.downloadBtn)
        shareBtn = findViewById(R.id.shareBtn)

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