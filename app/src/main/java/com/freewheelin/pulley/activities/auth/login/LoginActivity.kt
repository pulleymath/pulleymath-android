package com.freewheelin.pulley.activities.auth.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.InitSettingActivity
import com.freewheelin.pulley.activities.auth.findEmailAndPw.FindEmailAndPwActivity
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.core.API_APP
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.databinding.ActivityLoginBinding
import com.freewheelin.pulley.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.dialogs.DeviceManagerDialog
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.editText.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.pulleymath.android.pdf.PdfViewerActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class LoginActivity : BaseActivity(), InputFieldV2Listener, InputFieldV2EnterListener, PasswordFieldV2Listener, PasswordFieldV2EnterListener, LifecycleObserver {

    companion object {

        const val ALREADY_WITHDRAW = "ALREADY_WITHDRAW"
        const val AVAILABLE = "AVAILABLE" // 가능

        const val LOCK_ACCOUNT = "LOCK_ACCOUNT"
        const val LOGINID_EXIST  = "LOGINID_EXIST" // 존재
        const val LOGINID_INVALID = "LOGINID_INVALID" // 이상함

        const val NOT_FOUND_DATA = "NOT_FOUND_DATA"
        const val NOT_MATCH_PW = "NOT_MATCH_PW" // 패스워드 불일

        const val WRONG_LOGINPW = "WRONG_LOGINPW"
        const val WRONG_LOGINID = "WRONG_LOGINID"

        fun getIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    private val binding: ActivityLoginBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_login, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.run {
            greetingLabel.text = "안녕하세요.\n풀리수학에 오신 것을 환영합니다 :)"
            val content = SpannableString(findIdPwTv.text)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            loginBtn.setOnClickListener {
                if (loginBtn.isEnableUI()) this@LoginActivity.onLoginBtnClicked()
            }
            findIdPwTv.setOnClickListener {
                this@LoginActivity.onFindIdPwTvClicked()
            }
            signupTv.setOnClickListener {
                onSignupBtnClicked()
            }
            findIdPwTv.extensionTouchArea(12.toPx())
            emailField.text = user?.email ?: ""
            emailField.editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailField.listener = this@LoginActivity
            pwField.enterListener = this@LoginActivity

            rootView.setOnTouchListener { view, motionEvent ->
                currentFocus?.let { hideKeyboard(it) }
                false
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@LoginActivity)
            loginBtn.toDisableUI()

            setListener()
        }
    }

    fun setListener() {
        binding.apply {
            emailField.editText.doAfterTextChanged { text ->
                if (isValid()) loginBtn.toEnableUI() else loginBtn.toDisableUI()
            }
            pwField.inputEt.doAfterTextChanged {
                if (isValid()) loginBtn.toEnableUI() else loginBtn.toDisableUI()
            }
        }
    }

    fun isValid() : Boolean{
        binding.apply {
            if (emailField.text.isEmpty() == true || pwField.text.isEmpty() == true) {
                return false
            }
            return emailField.text.isValidEmail() == true && pwField.text.isValidPW() == true
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
    }

    var requested = false

    fun onLoginBtnClicked() {
        binding.apply {
            val email = emailField.text
            val pw = pwField.text


            if (email.isEmpty() || pw.isEmpty()) {
                if (email.isEmpty()) emailField.showErrorMsg("이메일을 입력해주세요.")
                if (pw.isEmpty()) pwField.showErrorMsg("비밀번호를 입력해주세요.")
                return
            }

            // 서버에서 체크, 로컬에서는 I1213 형식의 아이디를 사용해야 되기 때문에 valid 체크 할 수 없음
//        if(!email.isValidEmail() ) {
//            emailDet.showErrorMsg("이메일 형식을 확인해주세요.")
//            return
//        }

            emailField.isShownError = false
            pwField.isShownError = false

            if (!requested) { // 요청이 동시에 날라가는 케이스 방지
                requested = true
                showProgress()


                    API_V2.loginApp(RequestLogin(email, pw))
                        .enqueue(object : Callback<Template<User?>> {
                            override fun onFailure(call: Call<Template<User?>>, t: Throwable) {
                                try {
                                    responseFailed(this@LoginActivity, t)
                                    hideProgress()
                                    requested = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    hideProgress()
                                    requested = false
                                    DialogUtils.v2LoginErrDialog(this@LoginActivity)
                                }
                            }

                            override fun onResponse(
                                call: Call<Template<User?>>,
                                response: Response<Template<User?>>
                            ) {
                                try {
                                    Preferences.isAvailableRushDialog.set(true)
                                    val user = response.body()?.data
                                    user?.connectToCrashlytics()
                                    handleResponse(response, user)
                                    hideProgress()
                                    requested = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    hideProgress()
                                    requested = false
                                    DialogUtils.v2LoginErrDialog(this@LoginActivity)
                                }
                            }
                        })





            }
        }
    }

    fun putFcmToken(user: User?) {
        if(user?.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ _ ->
                            Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
                        }, { })
                }
            })
        }
    }

    fun onFindIdPwTvClicked() {
        startActivity(FindEmailAndPwActivity::class.java)
    }

    fun handleResponse(response: Response<Template<User?>>, user: User?) {
        var template = response.body()

        Log.d(javaClass.simpleName, "template=$template")

        when(response.code()) {
            200 -> {
                if(MyApplication.user == null) MyApplication.user = user
                else MyApplication.user!!.update(user)

                Log.d("로그인", "after login : user=$user")

                when {
                    user?.isValidPhone == false -> {
                        ConfirmPhoneDialog(this, successCB = {
                            commitUser()
                            goLearningTab()
                        }, failCB = { clearToken() }).show()
                    }
                    user?.isExceedDevice  == true -> { // 기기 초과 > 삭제팝업
                        // commit 은 기기 삭제후에
                        DialogUtils.confirmExceedDevice(this) {
                            DeviceManagerDialog(this, successCB = {
                                commitUser()
                                goLearningTab()
                            }, failCB = { clearToken() }).show()
                        }
                    }
                    user?.initSettingCompleted == false -> {
                        commitUser()
                        goInitSetting()
                    }
                    else -> {
                        commitUser()
                        goLearningTab()
                    }
                }
            }
            else -> {
                clearToken()

                val errorTemplate = response.errorBody()?.let { errorBody ->
                    Gson().fromJson(errorBody.string(), ResponseBody::class.java)
                }

                Log.d(javaClass.simpleName, "errorTemplate=$errorTemplate")
                binding.apply {
                    when (errorTemplate?.error) {
                        WRONG_LOGINID -> {
                            emailField.showErrorMsg(errorTemplate.message ?: "")
                            pwField.isShownError = false
                        }
                        WRONG_LOGINPW, NOT_MATCH_PW -> {
                            emailField.isShownError = false
                            pwField.showErrorMsg(errorTemplate.message ?: "")
                        }
                        NOT_FOUND_DATA -> {
                            pwField.isShownError = false
                            emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                        }
                        LOGINID_INVALID -> {
                            pwField.isShownError = false
                            emailField.showErrorMsg(errorTemplate.message ?: "")
                        }
                        LOCK_ACCOUNT -> {
                            DialogUtils.lockAccountDialog(this@LoginActivity) {
                                openResetPassword()
                            }.show()
                        }
                        else -> {
                            DialogUtils.showServerErr(this@LoginActivity)
                        }
                    }
                }
            }
        }
    }

    private fun openResetPassword() {
        Intent(this, FindEmailAndPwActivity::class.java).run {
            this.putExtra(FindEmailAndPwActivity.PAGE, 1) // 1이 비밀번호 재설정
            startActivity(this)
            finish()
        }
    }

    private fun commitUser() {
        MyApplication.user?.commit("LoginActivity")
    }

    private fun clearToken() {
        MyApplication.user?.token = ""
        MyApplication.user?.commit("LoginActivity")
    }

    private fun goLearningTab() {

//        putFcmToken(user)

        startActivity(Intent(this, LearningTabActivity::class.java))
        finishAffinity()
    }

    private fun goInitSetting() {
        startActivity(InitSettingActivity.getIntent(this))
        finishAffinity()
    }

    fun onSignupBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.INIT_SETTING, "로그인", "회원가입링크")

        val intent = SignupActivity.getIntent(this)
        startActivity(intent)
    }

    private fun showProgress() {
        binding.loadingContainer.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                binding.loadingContainer.visibility = View.GONE
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
                        responseFailed(this@LoginActivity, t)
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
                    AVAILABLE -> binding.emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                    LOGINID_INVALID -> binding.emailField.showErrorMsg("이메일 형식을 확인해주세요.")
                    ALREADY_WITHDRAW, LOGINID_EXIST -> binding.emailField.isShownError = false
                }

            }
            else -> {
                DialogUtils.showServerErr(this)
            }
        }
    }

    override fun onEnter(view: View) {
        if(view.id == R.id.pwField) // 이메일에서 엔터 쳤을때만 동작
            onLoginBtnClicked()
    }

    override fun onFieldValueChanged(view: InputFieldV2) {

    }

    override fun onFieldFocusChanged(view: PasswordFieldV2, hasFocus: Boolean) {

    }
}
