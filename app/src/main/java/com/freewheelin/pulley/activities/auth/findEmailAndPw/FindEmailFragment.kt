package com.freewheelin.pulley.activities.auth.findEmailAndPw

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import kotlinx.android.synthetic.main.fragment_find_email.*
import kotlinx.android.synthetic.main.view_input_daebak.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindEmailFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(): FindEmailFragment {
            val fragment = FindEmailFragment()
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputGuideLabel.text = "가입하신 이메일을 찾아드릴게요!\n아래 내용을 입력해주세요."
        findEmailBtn.setOnClickListener {
            onFindEmailBtnClicked()
        }

        loginBtn.setOnClickListener {
            onLoginBtnClicked()
        }
        nameDet.editText.hint= ""
        phoneDet.editText.filters = arrayOf( InputFilter.LengthFilter(15) )
        phoneDet.editText.hint = "- 없이 입력해주세요."
        phoneDet.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        phoneDet.editText.filters = arrayOf( InputFilter.LengthFilter(11) )

        nameDet.editText.toKoreanKeyboard()
        resultGuideLabel.text = "가입하신 이메일 주소는\n다음과 같습니다 :)"

        clipBtn.setOnClickListener {

            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("이메일", "${emailTv.text}")
            clipboard.setPrimaryClip(clip)

            DaebakToast.show(requireContext(), "이메일 주소를 복사했습니다.")
        }

        signupBtn.paintFlags = signupBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        signupBtn.setOnClickListener {
            val intent = SignupActivity.getIntent(requireContext())
            startActivity(intent)
            activity?.finish()
        }

        setListener()
        findEmailBtn.toDisableUI()
    }

    fun setListener() {
        nameDet.editText.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                val name = nameDet.text
                if(name.isEmpty())
                    nameDet.showErrorMsg("이름을 입력해주세요.")
                else if(name.length < 2)
                    nameDet.showErrorMsg("이름을 2글자 이상 입력하세요.")
                else if(name.isValidName() == false)
                    nameDet.showErrorMsg("올바른 형식이 아닙니다.")
                else
                    nameDet.isShownError = false
            }
        }

        nameDet.editText.doAfterTextChanged { editable ->
            if(checkIsValidInput()) findEmailBtn.toEnableUI() else findEmailBtn.toDisableUI()
        }

        phoneDet.editText.doAfterTextChanged { editable ->
            if(checkIsValidInput()) findEmailBtn.toEnableUI() else findEmailBtn.toDisableUI()
        }
    }

    fun onFindEmailBtnClicked() {
        val name = nameDet.text
        val phone = phoneDet.text

        if(checkIsValidInput() == false) {
            if(name.isEmpty())
                nameDet.showErrorMsg("이름을 입력해주세요.")
            else if(name.length < 2)
                nameDet.showErrorMsg("이름을 2글자 이상 입력하세요.")
            else if(name.isValidName() == false)
                nameDet.showErrorMsg("올바른 형식이 아닙니다.")
            else
                nameDet.isShownError = false

            if(phone.isEmpty())
                phoneDet.showErrorMsg("휴대폰번호를 입력해주세요.")
            else if(phone.isValidPhoneNum() == false)
                phoneDet.showErrorMsg("휴대폰번호 형식을 확인해주세요.")
            else
                phoneDet.isShownError = false

            return
        }

        API_V2.findEmail(name, phone).enqueue(object: Callback<Template<String>>{
            override fun onFailure(call: Call<Template<String>>, t: Throwable) {
                responseFailed(requireContext(), t)
            }

            override fun onResponse(call: Call<Template<String>>, response: Response<Template<String>>) {
                val code = response.code()
                when(code) {
                    200 -> {
                        notFoundMsg.visibility = View.GONE
                        val email = response.body()?.data
                        emailTv.text = email
                        inputContainerCl.hide {
                            resultContainerCl.show()
                        }
                    }
                    else -> {
                        notFoundMsg.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun checkIsValidInput(): Boolean {
        val name = nameDet.text
        val phone = phoneDet.text

        return (name.isValidName() && phone.isValidPhoneNum())
    }
    fun onLoginBtnClicked() {
        activity?.finish()
    }
}
