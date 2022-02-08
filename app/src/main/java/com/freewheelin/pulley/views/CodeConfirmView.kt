package com.freewheelin.pulley.views

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
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.view_code_confirm.view.*
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

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_code_confirm, this)
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
        textTitle.text = title
    }

    fun setHint(hint:String) {
        editValue.hint = hint
    }

    fun setText(text:String) {
        editValue.setText(text)
    }

    fun setMaxLength(length:Int) {
        editValue.filters = arrayOf( InputFilter.LengthFilter(length) )
    }

    fun setRequestButtonText(text:String) {
        btnRequestCode.text = text
    }

    fun setConfirmButtonText(text:String) {
        btnCodeConfirm.text = text
    }

    fun setInputType(type:Int) {
        when(type) {
            1 -> editValue.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            2 -> {
                editValue.inputType = InputType.TYPE_CLASS_PHONE
                phoneAuthTypeCl.visibility = View.VISIBLE
            }
            else -> editValue.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    fun getText() =  editValue.text.toString()

    private fun initUI() {
        btnRequestCode.setOnClickListener {
            requestCode()
        }
        // 인증 요청
        btnCodeConfirm.setOnClickListener {
            if(btnCodeConfirm.isEnableUI()) requestConfirm()
        }

        editCodeConfirm.doAfterTextChanged { text ->
            if(text?.length?:0 == 4) btnCodeConfirm.toEnableUI() else btnCodeConfirm.toDisableUI()
        }
        phoneMessageSwitch.setOnCheckedChangeListener { cb, flag ->
            phoneAuthType = if (flag) "SMS" else "ALIMTALK"
        }
        initReqeust()
    }

    private fun initReqeust() {
        // 요청 버튼 활성화
        btnRequestCode.toEnableUI()
        // 요청값 에러
        containerValueError.visibility = View.GONE
        // 코드 인증 요청 레이아웃
        containerCodeConfirm.visibility = View.GONE
        // 코드 인증값 에러
        containerCodeConfirmError.visibility = View.GONE
        // 컨펌 버튼 비활성화
        btnCodeConfirm.toDisableUI()

        phoneMessageSwitch.isEnabled = true
    }

    fun requestCode() {
        if(codeInterface == null) {
            Log.d(javaClass.simpleName, "EXCEPTION : CodeConfirmInterface 인터페이스가 연결되지 않았습니다")
            Toast.makeText(context, "CodeConfirmInterface 인터페이스가 연결되지 않았습니다", Toast.LENGTH_LONG).show()
            return
        }
        btnRequestCode.startLoding()
        // 구현체로 코드요청 후 콜백처리
        requestText = editValue.text.toString().trim()
        editValue.setText(requestText)
        codeInterface?.requestCode(requestText, phoneAuthType) { status, msg ->

            btnRequestCode.completeLoading()
            requestResult(status, msg)
        }
    }

    private fun requestResult(status:Status, msg: String?) {
        when(status) {
            Status.Sucess -> {
                containerValueError.visibility = View.GONE
                startCodeConfirm()
            }
            Status.Fail -> {
                requestFailed(msg)
            }
        }
    }

    // 코드 컨펌 시작
    private fun startCodeConfirm() {
        btnRequestCode.toDisableUI()
        containerCodeConfirm.visibility = View.VISIBLE
        containerCodeConfirmError.visibility = View.GONE
        editCodeConfirm.requestFocus()
        phoneMessageSwitch.isEnabled = false

        startTimer()
    }

    private fun requestFailed(msg:String?) {
        containerValueError.visibility = View.VISIBLE
        textValueError.text = msg
    }

    fun requestConfirm() {
        // 코드 인증도 구현체로 요청 후 콜백 처리
        confirmCode = editCodeConfirm.text.toString()
        editCodeConfirm.setText(confirmCode)

        btnCodeConfirm.startLoding()
        codeInterface?.requestConfirm(requestText, confirmCode) { status, msg ->
            btnCodeConfirm.completeLoading()

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
        containerCodeConfirmError.visibility = View.VISIBLE
        textCodeConfirmError.text = msg?:"인증 오류가 발생하였습니다"
    }

    private fun setFinish() {
        btnRequestCode.text = "인증번호 재발송"
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
        timerTv.visibility = View.VISIBLE
    }

    private fun deinitTimer() {
        remainSec = 180
        timer?.cancel()
        timer = null
        val min = (remainSec) / 60
        val sec = remainSec % 60

        timerTv.text = "${min}:${String.format("%02d", sec)}"
    }

    fun tick() {
        post {
            try {
                if (remainSec > 0) {
                    remainSec = remainSec - 1
                    val min = (remainSec) / 60
                    val sec = remainSec % 60
                    timerTv.text = "${min}:${String.format("%02d", sec)}"
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