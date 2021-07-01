package com.freewheelin.pulley.activities.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged

import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.fragment_my_app_setting.*
import com.freewheelin.pulley.activities.SplashActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestChangePassword
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_reset_password.*
import kotlinx.android.synthetic.main.view_input_password.view.*
import okhttp3.ResponseBody
import retrofit2.HttpException


class MyChangePasswordFragment : MyPageBaseFragment() {

    companion object {
        const val NOT_MATCH_PW = "NOT_MATCH_PW" // 패스워드 불일
    }

    val user
        get() = requireActivity().application.user!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_reset_password, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        currentPassword.isVisbleLabel = false
        newPassword.isVisbleLabel = false
        newPasswordConfirm.isVisbleLabel = false

        changeBtn.setOnClickListener {
            if(changeBtn.isEnableUI()) requestChange()
        }

        currentPassword.editText.doAfterTextChanged { text ->
            if(text?.length?:0 >= 6) enableRequestBtn()
        }

        newPassword.editText.doAfterTextChanged { text ->
            if(text?.length?:0 >= 6) enableRequestBtn()
        }

        newPasswordConfirm.editText.doAfterTextChanged { text ->
            if(text?.length?:0 >= 6) enableRequestBtn()
        }

        changeBtn.toDisableUI()
    }

    fun requestChange() {

        val current = currentPassword.text
        val new = newPassword.text

        changeBtn.startLoding()
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

    fun enableRequestBtn() {
        if(checkValidation()) changeBtn.toEnableUI() else changeBtn.toDisableUI()
    }

    fun checkValidation() : Boolean {

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

