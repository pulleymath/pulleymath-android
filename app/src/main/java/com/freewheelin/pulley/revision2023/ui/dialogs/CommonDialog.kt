package com.freewheelin.pulley.revision2023.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogDaebakV2Binding

class CommonDialog: DialogFragment() {

    val binding: DialogDaebakV2Binding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_daebak_v2, null, false)
    }
    var successCallback: () -> Unit = {}
    var cancelCallback: () -> Unit = {}

    enum class DialogType {
        Common, Alert
    }

    companion object {
        const val DIALOG_TITLE = "DIALOG_TITLE"
        const val DIALOG_CONTENTS = "DIALOG_CONTENTS"
        const val DIALOG_CANCEL_BTN_TEXT = "DIALOG_CANCEL_BTN_TEXT"
        const val DIALOG_SUCCESS_BTN_TEXT = "DIALOG_SUCCESS_BTN_TEXT"
        const val DIALOG_TYPE = "DIALOG_TYPE"
        const val DIALOG_ONE_BUTTON = "DIALOG_ONE_BUTTON"
        fun newInstance(
            title: String = "",
            contents: String = "",
            cancelText: String = "",
            successText: String = "",
            type: DialogType = DialogType.Common,
            isOneBtn: Boolean = false,
        ): CommonDialog {
            return CommonDialog().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TITLE, title)
                    putString(DIALOG_CONTENTS, contents)
                    putString(DIALOG_CANCEL_BTN_TEXT, cancelText)
                    putString(DIALOG_SUCCESS_BTN_TEXT, successText)
                    putSerializable(DIALOG_TYPE, type)
                    putBoolean(DIALOG_ONE_BUTTON, isOneBtn)
                }
            }
        }
    }

    init {

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {
            title = getString(DIALOG_TITLE)
            contents = getString(DIALOG_CONTENTS)
            cancelText = getString(DIALOG_CANCEL_BTN_TEXT)
            successText = getString(DIALOG_SUCCESS_BTN_TEXT)
            type = getSerializable(DIALOG_TYPE) as DialogType
            isOneBtn = getBoolean(DIALOG_ONE_BUTTON)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.leftBtn.setOnClickListener {
            cancelCallback()
            dismiss()
        }
        binding.rightBtn.setOnClickListener {
            dismiss()
            successCallback()
        }
    }

    private var title: String?
        get() = binding.titleTv.text.toString()
        set(value) {
            binding.titleTv.text = value
        }

    private var contents: String?
        get() = binding.contentTv.text.toString()
        set(value) {
            binding.contentTv.text = value
        }

    private var type: DialogType = DialogType.Common
        set(value) {
            when (value) {
                DialogType.Alert -> {
                    binding.rightBtn.setBackgroundResource(R.drawable.bg_red_300_round_ripple)
                    binding.leftBtn.setTextColor(requireContext().getColor(R.color.gray_800))

                }

                DialogType.Common -> {
                    binding.rightBtn.setBackgroundResource(R.drawable.bg_purple_300_round_ripple)
                    binding.leftBtn.setTextColor(requireContext().getColor(R.color.purple_300))
                }
            }
            field = value
        }

    private var isOneBtn: Boolean = false
        set(value) {
            if (value) {
                binding.leftBtn.visibility = View.GONE
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36F, requireContext().resources.displayMetrics).toInt()
                val params = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, height, 10f)
                binding.rightBtn.layoutParams = params
            } else {
                binding.leftBtn.visibility = View.VISIBLE
            }
            field = value
        }

    private var cancelText: CharSequence?
        get() = binding.leftBtn.text
        set(value) {
            binding.leftBtn.text = value
        }
    private var successText: CharSequence?
        get() = binding.rightBtn.text
        set(value) {
            binding.rightBtn.text = value
        }
}