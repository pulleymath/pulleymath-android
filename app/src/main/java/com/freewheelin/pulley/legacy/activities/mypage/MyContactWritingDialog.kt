package com.freewheelin.pulley.legacy.activities.mypage

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.hideKeyboard
import com.freewheelin.pulley.legacy.bases.vibrate
import com.freewheelin.pulley.legacy.core.API.postInquiry
import com.freewheelin.pulley.legacy.core.API_V1
import com.freewheelin.pulley.databinding.DialogMyContactWritingBinding
import com.freewheelin.pulley.legacy.dialogs.CompleteDialog
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyContactWritingDialog(context: Context, val user: UserV4): Dialog(context), View.OnFocusChangeListener {
    val cagetoryItems = listOf("건의사항", "오류 신고", "기능 문의", "회원정보 문의")

    private val binding: DialogMyContactWritingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_my_contact_writing, null, false)
    }

    init {
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initUI()
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if(hasFocus)
            binding.contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_purple_300)
        else {
            binding.contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_500)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        currentFocus?.let { context.hideKeyboard(it) }
        return super.onTouchEvent(event)
    }

    private fun initUI() {
        with(binding) {
            scrollView.setOnTouchListener { view, motionEvent ->
                currentFocus?.let { context.hideKeyboard(it) }
                false
            }

            contentsEt.onFocusChangeListener = this@MyContactWritingDialog
            contentsEt.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                    if(sequence.isEmpty()) {
                        contentsEt.textSize = 16f
                    } else {
                        contentsEt.textSize = 18f
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            categorySpinner.items = cagetoryItems
            xBtn.setOnClickListener {
                dismiss()
            }

            completeBtn.setOnClickListener {
                onCompleteBtnClicked()
            }
        }
    }

    private fun onCompleteBtnClicked() {
        with(binding) {
            if(!isCompletable()) {
                context.vibrate()
                if(categorySpinner.position == null)
                    categoryErrorLl.visibility = View.VISIBLE
                else
                    categoryErrorLl.visibility = View.GONE

                if(titleDif.editText.text.trim().isEmpty())
                    titleDif.showErrorMsg("제목을 입력해주세요.")
                else
                    titleDif.isShownError = false

                if(contentsEt.text.trim().isEmpty()) {
                    contentsErrorLl.visibility = View.VISIBLE
                    contentsErrorTv.text = "내용을 입력해주세요."
                    contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_red_300)
                } else if (contentsEt.text.trim().length < 10) {
                    contentsErrorLl.visibility = View.VISIBLE
                    contentsErrorTv.text = "내용을 10자 이상 입력해주세요."
                    contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_red_300)
                } else {
                    contentsErrorLl.visibility = View.GONE
                    if(contentsEt.isFocused)
                        contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_purple_300)
                    else
                        contentsEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_500)

                }
            } else {
                val title = titleDif.editText.text.trim().toString()
                val contents = contentsEt.text.trim().toString()
                val category = cagetoryItems[categorySpinner.position!!]
                requestInquiry(category = category, title = title, content = contents)
            }
        }
    }

    private fun requestInquiry(category: String, title: String, content: String) {
        API_V1.postInquiry(category, title, content, user.studentID ).enqueue(object: Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    dismiss()
                    CompleteDialog(context, "${user.fullName}님의 문의가\n정상적으로 접수되었습니다.", "최대한 빠른 시일 내로 답변드리겠습니다 :)").showFor()
                } else {
                    responseError(context, response)
                }

            }
        })
    }



    private fun isCompletable(): Boolean {
        return binding.categorySpinner.position != null &&
            binding.contentsEt.text.trim().length >= 10 && !binding.titleDif.editText.text.trim().isEmpty()
    }
}