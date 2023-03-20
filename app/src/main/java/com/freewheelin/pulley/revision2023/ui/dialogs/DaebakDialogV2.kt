package com.freewheelin.pulley.revision2023.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogDaebakV2Binding

class DaebakDialogV2(context: Context, val successCallback: () -> Unit = {}, val cancelCallback: () -> Unit = {}) : Dialog(context) {
    val binding: DialogDaebakV2Binding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_daebak_v2, null, false)
    }

    var title: String?
        get() = binding.titleTv.text.toString()
        set(value) {
            binding.titleTv.text = value
        }

    var contents: String?
        get() = binding.contentTv.text.toString()
        set(value) {
            binding.contentTv.text = value
        }
    var isConfirmBtnRed: Boolean = false
        set(value) {
            if (value) {
                binding.rightBtn.setBackgroundResource(R.drawable.bg_red_fe7b67_round_ripple)
                binding.leftBtn.setTextColor(context.getColor(R.color.gray_800))

            } else {
                binding.rightBtn.setBackgroundResource(R.drawable.bg_purple_6d6dff_round)
                binding.leftBtn.setTextColor(context.getColor(R.color.purple_300))
            }
            field = value
        }

    var isOneBtn: Boolean = false
        set(value) {
            if (value) {
                binding.leftBtn.visibility = View.GONE
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F, context.resources.displayMetrics).toInt()
                val params = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, height, 10f)
                binding.rightBtn.layoutParams = params
            } else {
                binding.leftBtn.visibility = View.VISIBLE
            }
            field = value
        }

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        binding.leftBtn.setOnClickListener {
            cancelCallback()
            dismiss()
        }
        binding.rightBtn.setOnClickListener {
            dismiss()
            successCallback()
        }
    }

    fun show(context: Context) {
        if(context is Activity && !context.isFinishing) super.show()
    }
}