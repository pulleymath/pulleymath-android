package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogChannelIoQuestionBinding

class ChannelIoQuestionDialog(context: Context, val bm: Bitmap, private val callback: (String, String) -> Unit
): DialogFragment() {

    private val binding: DialogChannelIoQuestionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_channel_io_question, null, false)
    }

    override fun onStart() {
        super.onStart()
//        dialog?.window?.let {
//            val params = it.attributes
//            params.dimAmount = 0.2f
//            it.attributes = params.
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    var selectedRadioMsg = ""
    var additionalMsg = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@ChannelIoQuestionDialog

            selectedRadioMsg = ""
            additionalMsg = ""
            screenShotIv.setImageBitmap(bm)
            setSubmitBtn(false)

            describeEt.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(msg: CharSequence?, start: Int, end: Int, count: Int) {
                    val isEnabled = count != 0
                    setSubmitBtn(isEnabled)

                    additionalMsg = msg.toString()
                }
            })

            detailRg.setOnCheckedChangeListener { radioGroup, id ->
                selectedRadioMsg = when(id) {
                    R.id.detail1 -> "일부 해설이 이해가 안 가요."
                    R.id.detail2 -> "문제 및 해설 전체가 이해가 안 가요."
                    R.id.detail3 -> "자세한 해설을 알고 싶어요."
                    R.id.detail4 -> "기타"
                    else -> "일부 해설이 이해가 안 가요."
                }
            }


            submitBtn.setOnClickListener {
                callback(selectedRadioMsg, additionalMsg)
                dismiss()
            }

            xBtn.setOnClickListener {
                dismiss()
            }

        }
    }

    fun setSubmitBtn(isEnabled: Boolean) {
        binding.apply {
            if (isEnabled) {
                submitBtn.isEnabled = true
                submitBtn.setBackgroundResource(R.drawable.bg_btn_round_common)
            } else {
                submitBtn.isEnabled = false
                submitBtn.setBackgroundResource(R.drawable.bg_purple_300_disable_round)
            }
        }
    }

}