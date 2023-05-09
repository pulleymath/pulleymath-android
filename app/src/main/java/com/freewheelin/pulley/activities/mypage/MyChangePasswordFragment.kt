package com.freewheelin.pulley.activities.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil

import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.SplashActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestChangePassword
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.databinding.FragmentMyResetPasswordBinding
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
//import com.freewheelin.pulley.views.editText.PasswordFieldV2
//import com.freewheelin.pulley.views.editText.PasswordFieldV2Listener
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class MyChangePasswordFragment : MyPageBaseFragment() {

    companion object {
        const val NOT_MATCH_PW = "NOT_MATCH_PW" // 패스워드 불일
    }

    val user
        get() = requireActivity().application.user!!

    lateinit var binding: FragmentMyResetPasswordBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_reset_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        with(binding) {
            currentPassword.isVisbleLabel = false
            newPassword.isVisbleLabel = false
            newPasswordConfirm.isVisbleLabel = false
//            currentPassword.listener = this@MyChangePasswordFragment
//            newPassword.listener = this@MyChangePasswordFragment
//            newPasswordConfirm.listener = this@MyChangePasswordFragment

            changeBtn.setOnClickListener {
                if(changeBtn.isEnableUI()) requestChange()
            }

            currentPassword.inputEt.doAfterTextChanged { text ->
                if(text?.length?:0 >= 6) enableRequestBtn()
            }

            newPassword.inputEt.doAfterTextChanged { text ->
                if(text?.length?:0 >= 6) enableRequestBtn()
            }

            newPasswordConfirm.inputEt.doAfterTextChanged { text ->
                if(text?.length?:0 >= 6) enableRequestBtn()
            }

            changeBtn.toDisableUI()
            backBtn.setOnClickListener { onBackBtnClicked() }
        }
    }

    fun requestChange() {
        with(binding) {
            val current = currentPassword.text
            val new = newPassword.text

            changeBtn.startLoading()
            val request = RequestChangePassword(current, new)
            API_V2.requestChangePassword(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    changeBtn.completeLoading()
                    DaebakToast.show(requireContext(), "비밀번호가 정상적으로 변경되었습니다.", overDialog = true)
                    onBackBtnClicked()
                }, {
                    changeBtn.completeLoading()
                    if (it is HttpException) {
                        val error = Gson().fromJson(it.response()?.errorBody()?.string(), com.freewheelin.pulley.model.ResponseBody::class.java)
                        when (error.error) {
                            NOT_MATCH_PW -> currentPassword.errorMsg = error.message ?: ""
                            else -> {
                                newPassword.errorMsg = error.message ?: "현재 사용 중인 비밀번호가 일치하지 않습니다."
                            }
                        }
                    } else {
                        newPassword.errorMsg = it.localizedMessage
                    }
                })
        }
    }

    fun enableRequestBtn() {
        with(binding) {
            if(checkValidation()) changeBtn.toEnableUI() else changeBtn.toDisableUI()
        }
    }

    fun checkValidation() : Boolean {
        with(binding) {
            val current = currentPassword.text
            val new = newPassword.text
            val confirm = newPasswordConfirm.text

            if(current.isEmpty()) {
                currentPassword.errorMsg = "현재 비밀번호를 입력하세요."
                return false
            }
            else if(!current.isValidPW()) {
                currentPassword.errorMsg = getString(R.string.text_please_input_6_between_15_eng_num_symbol)
                return false
            }
            else if(new.isEmpty()) {
                newPassword.errorMsg = "새 비밀번호를 입력하세요."
                return false
            }
            else if(!new.isValidPW()) {
                newPassword.errorMsg = "비밀번호가 형식에 맞지 않습니다."
                return false
            }
            else if(new != confirm) {
                newPasswordConfirm.errorMsg = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다."
                return false
            }
            else if(current == new) {
                newPassword.errorMsg = "현재 비밀번호와 다른 비밀번호를 입력하세요."
                return false
            }

            return true
        }
    }

    fun showCompleteDialog() {
        DialogUtils.toLoginDialog(requireActivity(), {
            MyApplication.user?.logout {
                requireActivity().finishAffinity()
                requireActivity().startActivity(Intent(requireContext(), SplashActivity::class.java))
            }
        },{
            onBackBtnClicked()
        })
    }
}

