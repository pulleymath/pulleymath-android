package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.ContentManager
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.utils.extensionTouchArea
import com.freewheelin.pulley.utils.isValidEmail
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.editText.DaebakInputField
import com.freewheelin.pulley.views.editText.DaebakInputFieldListener
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.databinding.DialogEmailInputBinding
import com.freewheelin.pulley.model.contents.MockExam

interface EmailInputDialogListener {
    fun onSentEmail()
    fun onSendEmailBtnClicked() {}
}


class EmailInputDialog(context: Context, contents: List<Content>, user: User, listener: EmailInputDialogListener): Dialog(context), DaebakInputFieldListener {
    val binding: DialogEmailInputBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_email_input, null, false)
    }
    val contents: List<Content> = contents
    val user: User = user

    var listener: EmailInputDialogListener? = null

    init {
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        init()
    }

    private fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        with(binding) {
            emailField.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailField.listener = this@EmailInputDialog
            emailField.text = user.email
            emailField.editText.setSelection(user.email.length)
            xBtn.extensionTouchArea(24.toPx())
            xBtn.setOnClickListener {
                dismiss()
            }
            sendBtn.setOnClickListener {
                onSendBtnClicked()
            }
            this@EmailInputDialog.listener = listener
        }
    }

    override fun onFieldFocusChanged(view: DaebakInputField, hasFocus: Boolean) {}

    override fun onFieldValueChanged(view: DaebakInputField) {
        val emailText = binding.emailField.text
        if(emailText.isNotEmpty())
            binding.sendBtn.toEnableUI()
        else
            binding.sendBtn.toDisableUI()
    }

    private fun onSendBtnClicked() {
        with(binding) {
            if(sendBtn.isEnableUI()) {
                val emailText = emailField.text
                if (!emailText.isValidEmail()) {
                    emailField.showErrorMsg("이메일 형식을 확인해주세요.")
                } else {
                    listener?.onSendEmailBtnClicked()
                    sendBtn.startLoding()
                    val mockChecked = contents[0]
                    if(mockChecked is MockExam) {
                        MockExamManager.sendEmail(context, mockChecked, user, emailText) {
                            listener?.onSentEmail()
                            sendBtn.completeLoading()
                            dismiss()
                        }
                    } else {
                        ContentManager.sendEmail(context, contents, user, emailText) {
                            listener?.onSentEmail()
                            sendBtn.completeLoading()
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun dismiss() {
        try {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {

        }
        super.dismiss()

    }
}
