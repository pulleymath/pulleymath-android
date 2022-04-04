package com.freewheelin.pulley.activities.learning.tabFragment.main.mypage


import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.mypage.MyPageBaseFragment
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.ServerCommunicator
import com.freewheelin.pulley.databinding.FragmentPhoneSettingBinding
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.PrimaryButton
import com.freewheelin.pulley.views.editText.DaebakInputField
import com.freewheelin.pulley.views.editText.DaebakInputFieldListener
import java.util.*
import kotlin.concurrent.timerTask

class PhoneSettingFragment : MyPageBaseFragment(), DaebakInputFieldListener {

    var remainSec = 180
    var timer: Timer? = null
    lateinit var binding: FragmentPhoneSettingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_phone_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
    }

    fun setUpUI() {
        with(binding) {
            modifyBtn.toDisableUI()
            phoneTv.goneLabel()
            codeTv.goneLabel()
            codeTv.visibility = View.INVISIBLE
            codeConfirmBtn.visibility = View.INVISIBLE
            timerTv.visibility = View.INVISIBLE

            phoneTv.editText.imeOptions = EditorInfo.IME_ACTION_DONE
            phoneTv.editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            phoneTv.listener = this@PhoneSettingFragment
            codeTv.editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    fun onModifyBtnClicked(view: PrimaryButton) {
        if(view.isEnableUI()) {
            LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정하기", "휴대폰")
            val phone = binding.phoneTv.text
            user!!.updateCellphone(requireContext(), phone) {
                deinitTimer()
                CompleteDialog(requireContext(), "변경이 완료되었습니다.", "이제 수정된 휴대폰번호로\n풀리수학을 사용하세요 :)").showFor(2000)
                listener?.onModifyCompleted()
            }
        } else {
            binding.phoneTv.showErrorMsg("휴대폰 번호 인증을 완료해주세요.")
        }
    }

    fun onCodeConfirmBtnClicked() {
        with(binding) {
            val code = codeTv.text
            val phone = phoneTv.text

            if (phone.isValidPhoneNum() == false || code.isEmpty() || remainSec == 0) {
                if (phone.isEmpty()) {
                    phoneTv.showErrorMsg("휴대폰 번호를 입력해주세요.")
                } else if (phone.isValidPhoneNum() == false) {
                    phoneTv.showErrorMsg("전화번호 형식을 확인해주세요.")
                }

                if (code.isEmpty()) {
                    codeTv.showErrorMsg("휴대폰으로 받은 코드를 입력해주세요.")
                }

                if (remainSec == 0)
                    codeTv.showErrorMsg("시간이 만료되었습니다.")

                return
            }

            ServerCommunicator.authCode(requireContext(), user!!, phone, code,
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
                    when (error) {
                        "TIME_END" -> codeTv.showErrorMsg("시간이 만료되었습니다.")
                        "INCORRECT_AUTH_NUMBER" -> codeTv.showErrorMsg("인증번호가 잘못되었습니다.")
                        "NOT_EXIST" -> phoneTv.showErrorMsg("휴대폰 번호를 확인해주세요.")
                    }
                }
            )
        }
    }

    fun onRequestCodeBtnClicked() {
        with(binding) {
            codeConfirmIv.hideIfNeed()
            codeTv.isShownError = false
            phoneTv.isShownError = false
            deinitTimer()
            val phone = phoneTv.text
            if (phone.isEmpty()) {
                phoneTv.showErrorMsg("휴대폰 번호를 입력해주세요.")
                return
            }
            if (phone.isValidPhoneNum() == false) {
                phoneTv.showErrorMsg("전화번호 형식을 확인해주세요.")
                return
            }
            ServerCommunicator.authPhone(requireContext(), user!!, phone,
                successCB = {
                    if (codeTv.visibility == View.INVISIBLE) codeTv.show()
                    if (codeConfirmBtn.visibility == View.INVISIBLE) codeConfirmBtn.show()
                    if (timerTv.visibility == View.INVISIBLE) timerTv.show()
                    runTimer()
                },
                failCB = { code, error ->

                }
            )
        }
    }

    override fun onFieldFocusChanged(view: DaebakInputField, hasFocus: Boolean) {}

    override fun onFieldValueChanged(view: DaebakInputField) {
        with(binding) {
            if (view == phoneTv) {
                if (codeTv.visibility == View.VISIBLE)
                    codeConfirmBtn.visibility = View.VISIBLE
                confirmCompleteCl.visibility = View.INVISIBLE
                requestCodeBtn.toEnableUI()
                modifyBtn.toDisableUI()
                codeConfirmIv.visibility = View.INVISIBLE
            }
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

            binding.timerTv.text = "${min}:${String.format("%02d", sec)}"
        }
    }

}
