package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.ServerCommunicator
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.Buttons.PrimaryButton
import com.freewheelin.pulley.views.EditText.DaebakInputField
import com.freewheelin.pulley.views.EditText.DaebakInputFieldListener
//import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.dialog_my_signup_info_setting.*
import kotlinx.android.synthetic.main.fragment_phone_setting.*
import kotlinx.android.synthetic.main.fragment_phone_setting.codeConfirmBtn
import kotlinx.android.synthetic.main.fragment_phone_setting.codeConfirmIv
import kotlinx.android.synthetic.main.fragment_phone_setting.codeTv
import kotlinx.android.synthetic.main.fragment_phone_setting.confirmCompleteCl
import kotlinx.android.synthetic.main.fragment_phone_setting.modifyBtn
import kotlinx.android.synthetic.main.fragment_phone_setting.phoneTv
import kotlinx.android.synthetic.main.fragment_phone_setting.requestCodeBtn
import kotlinx.android.synthetic.main.fragment_phone_setting.timerTv
import kotlinx.android.synthetic.main.view_input_daebak.view.*
import java.util.*
import kotlin.concurrent.timerTask


class PhoneSettingFragment : MyPageBaseFragment(), DaebakInputFieldListener {

    var remainSec = 180
    var timer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        modifyBtn.toDisableUI()
        phoneTv.goneLabel()
        codeTv.goneLabel()
        codeTv.visibility = View.INVISIBLE
        codeConfirmBtn.visibility = View.INVISIBLE
        timerTv.visibility = View.INVISIBLE

        phoneTv.editText.imeOptions = EditorInfo.IME_ACTION_DONE
        phoneTv.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        phoneTv.listener = this
        codeTv.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        confirmCompleteCl.visibility = View.GONE
        modifyBtn.setOnClickListener {
            onModifyBtnClicked(modifyBtn)
        }

        requestCodeBtn.setOnClickListener {
            onRequestCodeBtnClicked()
        }

        codeConfirmBtn.setOnClickListener {
            onCodeConfirmBtnClicked()
        }

        rootView.setOnClickListener {
            context?.hideKeyboard(it)
        }
    }

    fun onModifyBtnClicked(view: PrimaryButton) {
        if(view.isEnableUI()) {
            LogUtils.logEvent(context!!, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "휴대폰")
            val phone = phoneTv.text
            user!!.updateCellphone(context!!, phone) {
                deinitTimer()
                CompleteDialog(context!!, "변경이 완료되었습니다.", "이제 수정된 휴대폰번호로\n풀리수학을 사용하세요 :)").showFor(2000)
                listener?.onModifyCompleted()
            }
        } else {
            phoneTv.showErrorMsg("휴대폰 번호 인증을 완료해주세요.")
        }
    }

    fun onCodeConfirmBtnClicked() {
        val code = codeTv.text
        val phone = phoneTv.text

        if(phone.isValidPhoneNum() == false || code.isEmpty() || remainSec == 0) {
            if(phone.isEmpty()) {
                phoneTv.showErrorMsg("휴대폰 번호를 입력해주세요.")
            } else if(phone.isValidPhoneNum() == false) {
                phoneTv.showErrorMsg("전화번호 형식을 확인해주세요.")
            }

            if(code.isEmpty()) {
                codeTv.showErrorMsg("휴대폰으로 받은 코드를 입력해주세요.")
            }

            if(remainSec == 0)
                codeTv.showErrorMsg("시간이 만료되었습니다.")

            return
        }

        ServerCommunicator.authCode(context!!, user!!, phone, code,
                successCB = {
                    deinitTimer()
                    codeConfirmIv.show()
                    timerTv.hide()
                    modifyBtn.toEnableUI()
                    codeConfirmBtn.visibility = View.INVISIBLE
                    confirmCompleteCl.show(200)
                    requestCodeBtn.toDisableUI()
                },
                failCB = { code, error ->
                    when(error) {
                        "TIME_END" -> codeTv.showErrorMsg("시간이 만료되었습니다.")
                        "INCORRECT_AUTH_NUMBER" -> codeTv.showErrorMsg("인증번호가 잘못되었습니다.")
                        "NOT_EXIST" -> phoneTv.showErrorMsg("휴대폰 번호를 확인해주세요.")
                    }
                }
        )
    }

    fun onRequestCodeBtnClicked() {
        codeConfirmIv.hideIfNeed()
        codeTv.isShownError = false
        phoneTv.isShownError = false
        deinitTimer()
        val phone = phoneTv.text
        if(phone.isEmpty()) {
            phoneTv.showErrorMsg("휴대폰 번호를 입력해주세요.")
            return
        }
        if(phone.isValidPhoneNum() == false) {
            phoneTv.showErrorMsg("전화번호 형식을 확인해주세요.")
            return
        }
        ServerCommunicator.authPhone(context!!, user!!, phone,
                successCB = {
                    if(codeTv.visibility == View.INVISIBLE) codeTv.show()
                    if(codeConfirmBtn.visibility == View.INVISIBLE) codeConfirmBtn.show()
                    if(timerTv.visibility == View.INVISIBLE) timerTv.show()
                    runTimer()
                },
                failCB = { code, error ->

                }
        )
    }

    override fun onFieldFocusChanged(view: DaebakInputField, hasFocus: Boolean) {}

    override fun onFieldValueChanged(view: DaebakInputField) {
        if (view == phoneTv) {
            if(codeTv.visibility == View.VISIBLE)
                codeConfirmBtn.visibility = View.VISIBLE
            confirmCompleteCl.visibility = View.INVISIBLE
            requestCodeBtn.toEnableUI()
            modifyBtn.toDisableUI()
            codeConfirmIv.visibility = View.INVISIBLE
        }
    }
    private fun runTimer() {
        timer = Timer()
        val task = timerTask {
            tick()
        }
        timer?.schedule(task, 1000, 1000)
    }

    private fun deinitTimer() {
        remainSec = 180
        timer?.cancel()
        timer = null
    }

    fun tick() {
        activity?.runOnUiThread {
            if (remainSec > 0)
                remainSec = remainSec - 1
            val min = (remainSec) / 60
            val sec = remainSec % 60

            timerTv.text = "${min}:${String.format("%02d", sec)}"
        }
    }

}
