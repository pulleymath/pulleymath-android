package com.freewheelin.pulley.revision2023.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.findEmailAndPw.FindEmailAndPwActivity
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.databinding.FragmentGuestLoginBinding
import com.freewheelin.pulley.legacy.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.revision2023.viewmodel.GuestJoinViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.editText.*
import com.freewheelin.pulley.revision2023.ui.activity.WhaleSpaceLoginActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuestLoginFragment : Fragment(),
    InputFieldV2Listener, InputFieldV2EnterListener, PasswordFieldV2Listener,
    PasswordFieldV2EnterListener {
    private lateinit var binding: FragmentGuestLoginBinding
    var viewModel: GuestJoinViewModel? = null
    private lateinit var getResult: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // appTheme에서 MaterialComponent인 TextInputLayout 을 사용할 수 있으나
        // 하위 fragment에서는 apptheme가 적용되어 있지 않기 때문에 activity context를 통해 테마 및 layout inflater를 가져온 뒤
        // inflate 해줘야한다.
        val themeWrapper = ContextThemeWrapper(requireActivity(), R.style.DialogTheme)
        val layoutInflater = requireActivity().layoutInflater.cloneInContext(themeWrapper)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_guest_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            isBetaApp = BuildConfig.FLAVOR == "beta"
            viewModel?.loginBtnEnabled?.postValue(false)

            emailField.listener = this@GuestLoginFragment
            emailField.enterListener = this@GuestLoginFragment
            pwField.listener = this@GuestLoginFragment
            pwField.enterListener = this@GuestLoginFragment

            listOf(emailField.editText, pwField.inputEt).forEach {
                it.doAfterTextChanged {
                    val enabled = isValid()
                    viewModel?.loginBtnEnabled?.postValue(enabled)
                }
            }

            backBtn.setOnClickListener {
                viewModel?.removeStep?.let { it(this@GuestLoginFragment) }
            }
            loginBtn.setOnClickListener {
                if (isValid()) onLoginBtnClicked()
            }
            whaleLoginBtn.setOnClickListener {

                val intent = Intent(requireContext(), WhaleSpaceLoginActivity::class.java)
                intent.putExtra("AUTO_ACTION", true)
                startActivity(intent)
                viewModel?.exitBtn()
            }

            dummyLoginBtn.setOnClickListener {
                pwField.text = "vmfl515!dnlf"
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    if (viewModel?.loginBtnEnabled?.value == true) onLoginBtnClicked()
                }
            }
            findIdPwTv.setOnClickListener {
                viewModel?.onExitClickCallback?.invoke()
                val intent = FindEmailAndPwActivity.getIntent(requireContext(), true)
                startActivity(intent)
            }
            updateGradeCl.setOnTouchListener { view, motionEvent ->
                val imm: InputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
                true
            }

        }
        arguments?.let {

        }
    }
    private fun onLoginBtnClicked() {
        binding.apply {
            val email = emailField.text.trim()
            val pw = pwField.text


            if (email.isEmpty() || pw.isEmpty()) {
                if (email.isEmpty()) emailField.showErrorMsg("이메일을 입력해주세요.")
                if (pw.isEmpty()) pwField.showErrorMsg("비밀번호를 입력해주세요.")
                return
            }

            emailField.isShownError = false
            pwField.isShownError = false

            viewModel?.let {
                if (!it.isOnceRequested) { // 요청이 동시에 날라가는 케이스 방지
                    it.isOnceRequested = true
                    it.setLoadingProgress(true)
                    it.getAppToken(email, pw, handleResponse, errorHandle)
                }
            }

        }
    }
    private fun clearToken() {
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user?.commit("GuestLoginFragment")
    }
    private fun renewMainUser() {
        viewModel?.exitBtn()
//        val profileModifyIntent = Intent(UserManager.EVENT_USER_MODIFYING)
//        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(profileModifyIntent)
        val userUpdateIntent = Intent(UserManager.EVENT_USER_UPDATE)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(userUpdateIntent)
    }
    val handleResponse: ((ResponseBody<SignInAppToken>) -> Unit) = { res ->
        Log.d(javaClass.simpleName, "template=${res.error}")

        if (res.error == null) {
            when (res.data?.isValidPhone) {
                false -> {
                    ConfirmPhoneDialog(requireActivity(), successCB = {
                        renewMainUser()
                    }, failCB = {
//                        clearToken()
                    }).show()
                }
                else -> {
                    renewMainUser()
                }
            }
        } else {
            clearToken()
            binding.apply {
                errorHandle(res)
            }
        }
    }
    val errorHandle: ((ResponseBody<SignInAppToken>) -> Unit) = { res ->
        val error = res.error
        val errMsg = res.message
        binding.apply {
            when (error) {
                LoginActivity.WRONG_LOGINID -> {
                    emailField.showErrorMsg(errMsg ?: "")
                    pwField.isShownError = false
                }
                LoginActivity.WRONG_LOGINPW, LoginActivity.NOT_MATCH_PW -> {
                    emailField.isShownError = false
                    pwField.showErrorMsg(errMsg ?: "")
                }
                LoginActivity.NOT_FOUND_DATA -> {
                    pwField.isShownError = false
                    emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                }
                LoginActivity.LOGINID_INVALID -> {
                    pwField.isShownError = false
                    emailField.showErrorMsg(errMsg ?: "")
                }
                LoginActivity.LOCK_ACCOUNT -> {
                    DialogUtils.lockAccountDialog(requireContext()) {
                        openResetPassword()
                    }
                }
                else -> {
                    DialogUtils.showServerErr(requireContext())
                }
            }
        }
    }

    private fun openResetPassword() {
//        Intent(this, FindEmailAndPwActivity::class.java).run {
//            this.putExtra(FindEmailAndPwActivity.PAGE, 1) // 1이 비밀번호 재설정
//            startActivity(this)
//        }
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            GuestLoginFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onFieldFocusChanged(view: InputFieldV2, hasFocus: Boolean) {
        if(!hasFocus) {
            if(view.text.isEmpty())
                binding.emailField.showErrorMsg("이메일을 입력해주세요.")
            else {
                API_V2.existId(view.text).enqueue(object: Callback<Template<String?>> {
                    override fun onFailure(call: Call<Template<String?>>, t: Throwable) {
                        responseFailed(requireContext(), t)
                    }

                    override fun onResponse(call: Call<Template<String?>>, response: Response<Template<String?>>) {
                        val httpCode = response.code()
                        val statusCode = response.body()?.data?:""
                        handleCheckIDResponse(httpCode, statusCode)
                    }
                })
            }
        }
    }
    fun handleCheckIDResponse(httpCode: Int, statusCode:String?) {
        when(httpCode) {
            200 -> {
                when(statusCode) {
                    LoginActivity.AVAILABLE -> binding.emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                    LoginActivity.LOGINID_INVALID -> binding.emailField.showErrorMsg("이메일 형식을 확인해주세요.")
                    LoginActivity.ALREADY_WITHDRAW, LoginActivity.LOGINID_EXIST -> binding.emailField.isShownError = false
                }

            }
            else -> {
                DialogUtils.showServerErr(requireContext())
            }
        }
    }
    override fun onEnter(view: View) {
        if (view.id == R.id.pwField) { // 이메일에서 엔터 쳤을때만 동작
            onLoginBtnClicked()
        }
    }

    override fun onFieldFocusChanged(view: PasswordFieldV2, hasFocus: Boolean) {

    }
    fun isValid() : Boolean {
        binding.apply {
            val email = emailField.text.trim()
            val pw = pwField.text.trim()
            if (emailField.text.isEmpty() || pwField.text.isEmpty()) {
                return false
            }
            return email.isValidEmail() && pw.isValidPW()
        }
    }
}