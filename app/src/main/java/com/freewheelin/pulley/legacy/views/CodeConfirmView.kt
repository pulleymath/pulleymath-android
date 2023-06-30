package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewCodeConfirmBinding
import java.util.*
import kotlin.concurrent.timerTask

class CodeConfirmView : LinearLayout {

    enum class Status { Sucess, Fail }

    var codeInterface:CodeConfirmInterface? = null

    var requestText = ""
    var confirmCode = ""
    var phoneAuthType: String = "ALIMTALK"

    /**
     * requestCode : Api에 [코드] 요청 후 콜백
     * requestConfirm : Api에 [코드 인증] 요청 후 콜백
     * confirmSuccess : 결과 성공시만 호출, 에러시는 내부 처리
     */
    interface CodeConfirmInterface {
        fun requestCode(text:String, type: String, callback:(status:Status, msg:String?)->Unit)
        fun requestConfirm(requestText:String, confirmCode:String, callback:(status:Status, msg:String?)->Unit)
        fun confirmSuccess()
    }
    var binding: ViewCodeConfirmBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_code_confirm, this, true)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setAttributes(attrs)
        initUI()
    }

    private fun setAttributes(attrs: AttributeSet) {
        val attrList = context.obtainStyledAttributes(attrs, R.styleable.CodeConfirmView)
        for(i in 0 until attrList.indexCount) {
            val attr = attrList.getIndex(i)
            when(attr) {
                R.styleable.CodeConfirmView_title -> attrList.getString(attr)?.let { setTitle(it) }
                R.styleable.CodeConfirmView_hint -> attrList.getString(attr)?.let { setHint(it) }
                R.styleable.CodeConfirmView_max_length -> attrList.getInt(attr, 50)?.let { setMaxLength(it) }
                R.styleable.CodeConfirmView_request_button -> attrList.getString(attr)?.let { setRequestButtonText(it) }
                R.styleable.CodeConfirmView_confirm_button -> attrList.getString(attr)?.let { setConfirmButtonText(it) }
                R.styleable.CodeConfirmView_input_type -> attrList.getInt(attr, 0)?.let { setInputType(it) }
            }
        }
    }

    fun setTitle(title:String) {
        binding.textTitle.text = title
    }

    fun setHint(hint:String) {
        binding.editValue.hint = hint
    }

    fun setText(text:String) {
        binding.editValue.setText(text)
    }

    fun setMaxLength(length:Int) {
        binding.editValue.filters = arrayOf( InputFilter.LengthFilter(length) )
    }

    fun setRequestButtonText(text:String) {
        binding.btnRequestCode.text = text
    }

    fun setConfirmButtonText(text:String) {
        binding.btnCodeConfirm.text = text
    }

    fun setInputType(type:Int) {
        when(type) {
            1 -> binding.editValue.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            2 -> {
                binding.editValue.inputType = InputType.TYPE_CLASS_PHONE
                binding.phoneAuthTypeCl.visibility = View.VISIBLE
            }
            else -> binding.editValue.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    fun getText() = binding.editValue.text.toString()

    private fun initUI() {
        binding.btnRequestCode.setOnClickListener {
            requestCode()
        }
        // 인증 요청
        binding.btnCodeConfirm.setOnClickListener {
            if(binding.btnCodeConfirm.isEnabled) requestConfirm()
        }

        binding.editCodeConfirm.doAfterTextChanged { text ->
            binding.btnCodeConfirm.isEnabled = text?.length?:0 == 4
        }
        binding.phoneMessageSwitch.setOnCheckedChangeListener { cb, flag ->
            phoneAuthType = if (flag) "SMS" else "ALIMTALK"
        }
        initReqeust()
    }

    private fun initReqeust() {
        // 요청 버튼 활성화
        binding.btnRequestCode.isEnabled = true
        // 요청값 에러
        binding.containerValueError.visibility = View.GONE
        // 코드 인증 요청 레이아웃
        binding.containerCodeConfirm.visibility = View.GONE
        // 코드 인증값 에러
        binding.containerCodeConfirmError.visibility = View.GONE
        // 컨펌 버튼 비활성화
        binding.btnCodeConfirm.isEnabled = false

        binding.phoneMessageSwitch.isEnabled = true
    }

    fun requestCode() {
        if(codeInterface == null) {
            Log.d(javaClass.simpleName, "EXCEPTION : CodeConfirmInterface 인터페이스가 연결되지 않았습니다")
            Toast.makeText(context, "CodeConfirmInterface 인터페이스가 연결되지 않았습니다", Toast.LENGTH_LONG).show()
            return
        }
        binding.btnRequestCode.setLoading(true)
        // 구현체로 코드요청 후 콜백처리
        requestText = binding.editValue.text.toString().trim()
        binding.editValue.setText(requestText)
        codeInterface?.requestCode(requestText, phoneAuthType) { status, msg ->

            binding.btnRequestCode.setLoading(false)
            requestResult(status, msg)
        }
    }

    private fun requestResult(status:Status, msg: String?) {
        when(status) {
            Status.Sucess -> {
                binding.containerValueError.visibility = View.GONE
                startCodeConfirm()
            }
            Status.Fail -> {
                requestFailed(msg)
            }
        }
    }

    // 코드 컨펌 시작
    private fun startCodeConfirm() {
        with(binding) {
            btnRequestCode.isEnabled = false
            containerCodeConfirm.visibility = View.VISIBLE
            containerCodeConfirmError.visibility = View.GONE
            editCodeConfirm.requestFocus()
            phoneMessageSwitch.isEnabled = false

            startTimer()
        }
    }

    private fun requestFailed(msg:String?) {
        binding.containerValueError.visibility = View.VISIBLE
        binding.textValueError.text = msg
    }

    fun requestConfirm() {
        // 코드 인증도 구현체로 요청 후 콜백 처리
        confirmCode = binding.editCodeConfirm.text.toString()
        binding.editCodeConfirm.setText(confirmCode)

        binding.btnCodeConfirm.setLoading(true)
        codeInterface?.requestConfirm(requestText, confirmCode) { status, msg ->
            binding.btnCodeConfirm.setLoading(false)

            when(status) {
                Status.Sucess -> {
                    codeInterface?.confirmSuccess()
                    setFinish()
                }
                Status.Fail -> {
                    setConfirmErrorMsg(msg)
                }
            }
        }
    }

    private fun setExpiredError(msg:String) {
        setConfirmErrorMsg(msg)
        setFinish()
    }

    private fun setConfirmErrorMsg(msg:String?) {
        binding.containerCodeConfirmError.visibility = View.VISIBLE
        binding.textCodeConfirmError.text = msg?:"인증 오류가 발생하였습니다"
    }

    private fun setFinish() {
        binding.btnRequestCode.text = "인증번호 재발송"
        deinitTimer()
        initReqeust()
    }

    /*
        Timer Control
     */
    var remainSec = 180
    var timer: Timer? = null

    private fun startTimer() {
        timer = Timer()
        val task = timerTask {
            tick()
        }
        timer?.schedule(task, 1000, 1000)
        binding.timerTv.visibility = View.VISIBLE
    }

    private fun deinitTimer() {
        remainSec = 180
        timer?.cancel()
        timer = null
        val min = (remainSec) / 60
        val sec = remainSec % 60

        binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
    }

    fun tick() {
        post {
            try {
                if (remainSec > 0) {
                    remainSec = remainSec - 1
                    val min = (remainSec) / 60
                    val sec = remainSec % 60
                    binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
                } else {
                    setExpiredError("인증번호 유효기간이 만료되었습니다. 인증번호를 재발송해주세요.")
                    DaebakToast.show(context, "인증번호 유효기간이 만료되었습니다. 인증번호를 재발송해주세요.", overDialog = true)
                }
            }catch (e:Exception) {
                setExpiredError("인증처리중 알 수 없는 오류가 발생하였습니다.")
            }
        }
    }
}