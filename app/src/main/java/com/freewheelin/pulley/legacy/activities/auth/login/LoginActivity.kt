package com.freewheelin.pulley.legacy.activities.auth.login

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.findEmailAndPw.FindEmailAndPwActivity
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.hideKeyboard
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.databinding.ActivityLoginBinding
import com.freewheelin.pulley.legacy.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.usesWebApp
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.viewmodel.LoginActViewModel
import com.freewheelin.pulley.legacy.views.editText.*
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.*
import com.freewheelin.pulley.revision2023.ui.activity.AiepWebViewActivity
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.activity.WhaleSpaceLoginActivity
import com.freewheelin.pulley.revision2023.utils.StringUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit


class LoginActivity : BaseActivity(), InputFieldV2Listener, InputFieldV2EnterListener, PasswordFieldV2Listener, PasswordFieldV2EnterListener {

    companion object {

        const val ALREADY_WITHDRAW = "ALREADY_WITHDRAW"
        const val AVAILABLE = "AVAILABLE" // 가능

        const val LOCK_ACCOUNT = "LOCK_ACCOUNT"
        const val LOGINID_EXIST  = "LOGINID_EXIST" // 존재
        const val LOGINID_INVALID = "LOGINID_INVALID" // 이상함
        const val INVALID_AUTH = "INVALID_AUTH" // 이상함

        const val NOT_FOUND_DATA = "NOT_FOUND_DATA"
        const val NOT_MATCH_PW = "NOT_MATCH_PW" // 패스워드 불일

        const val WRONG_LOGINPW = "WRONG_LOGINPW"
        const val WRONG_LOGINID = "WRONG_LOGINID"

        const val socialLoginFinished = 200
        const val USER_TOKEN = "USER_TOKEN"

        fun getIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
    val viewModel: LoginActViewModel by viewModels()

    private val binding: ActivityLoginBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_login, null, false)
    }
    var spyCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱은 기본 landscape이나, 모바일(폰)에서는 로그인 화면을 portrait로 사용한다.
        // 레이아웃 분기(sw600dp)와 동일 기준인 R.bool.isPortrait로 판별.
        requestedOrientation = if (resources.getBoolean(R.bool.isPortrait)) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            onBackBtnClicked()
        }
        binding.run {
            spyCount = 0
            greetingLabel.text = "서비스 이용을 위해 로그인 해주세요 :)"
            val content = SpannableString(findIdPwTv.text)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            loginBtn.setOnClickListener {
                if (loginBtn.isEnabled) this@LoginActivity.onLoginBtnClicked()
            }
            whaleLoginBtn.setOnClickListener {
                val intent = Intent(this@LoginActivity, WhaleSpaceLoginActivity::class.java)
                intent.putExtra("AUTO_ACTION", true)
                startActivity(intent)
            }
            aiepLoginBtn.setOnClickListener {
                Log.d(javaClass.simpleName, "aiepLoginBtn clicked")
                startActivity(AiepWebViewActivity.loginIntent(this@LoginActivity))
            }
            findIdPwTv.setOnClickListener {
                this@LoginActivity.onFindIdPwTvClicked()
            }
            signupTv.setOnClickListener {
                onSignupBtnClicked()
            }
            findIdPwTv.extensionTouchArea(12.toPx())
            emailField.text = Preferences.signedEmail.get()
            emailField.editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailField.listener = this@LoginActivity
            pwField.enterListener = this@LoginActivity

            rootView.setOnTouchListener { view, motionEvent ->
                currentFocus?.let { hideKeyboard(it) }
                false
            }
            loginBtn.isEnabled = false

            setListener()

            dummyLoginBtn.apply {
                visibility = if (BuildConfig.FLAVOR == "beta") {
                    View.VISIBLE
                } else { View.GONE }

                setOnClickListener {
                    pwField.text = "vmfl515!dnlf"
                    if (loginBtn.isEnabled) this@LoginActivity.onLoginBtnClicked()
                }
            }

            val emoji = StringUtils.getEmojiByUnicode(0x1F977)
            liveBtn.apply {
                visibleIf(BuildConfig.FLAVOR == "beta")

                if (Preferences.onServerAPI.get() == Network.Server.live.toString()) {
                    text = "$emoji\nLive"
                }
                setOnClickListener {
                    changeServerApi(true)
                    Toast.makeText(this@LoginActivity, "Live Api!", Toast.LENGTH_SHORT).show();
                }
            }
            stagingBtn.apply {
                visibleIf(BuildConfig.FLAVOR == "beta")

                if (Preferences.onServerAPI.get() == Network.Server.staging.toString()) {
                    text = "$emoji\nStaging"
                }
                setOnClickListener {
                    changeServerApi(false)
                    Toast.makeText(this@LoginActivity, "Staging Api!", Toast.LENGTH_SHORT).show();

                }
            }
            backBtn.setOnClickListener {
                onBackBtnClicked()
            }
            testBtn.apply {
                visibleIf(BuildConfig.FLAVOR == "beta")
                setOnClickListener {
                    val intent = Intent(this@LoginActivity, WhaleSpaceLoginActivity::class.java)
                    startActivity(intent)
                }
            }
            signupGuideTv.setOnClickListener {
                spyCount += 1
                stagingBtn.visibleIf((emailField.text == "staging" && spyCount > 10))
            }
        }
        viewModel.apply {
            errorAction.observe(this@LoginActivity) { type ->
                when(type) {
                    HttpException401 -> {
//                        DialogUtils.confirmV2(this@LoginActivity,
//                        "토큰 인증 에러", "문제가 계속되면 카카오톡(@풀리는수학) 이나 1670-2115 로 문의 바랍니다.")
                        Log.e(javaClass.simpleName, "401 Auth Error Not Handled : ${type}")
                    }
                    NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
        }
    }
    private fun onBackBtnClicked() {
        finish()
    }
    fun changeServerApi(isLive: Boolean) {
        val api = if (isLive) Network.Server.live.toString() else Network.Server.staging.toString()
        Preferences.onServerAPI.set(api)

        val pm = this.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(this.packageName)
        val componentName = launchIntent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
        System.exit(0)
    }

    fun setListener() {
        binding.apply {
            emailField.editText.doAfterTextChanged { _ ->
                loginBtn.isEnabled = isValid()
            }
            pwField.inputEt.doAfterTextChanged {
                loginBtn.isEnabled = isValid()
            }
        }
    }

    fun isValid() : Boolean {
        binding.apply {
            val email = emailField.text.trim()
            val pw = pwField.text.trim()
            if (emailField.text.isEmpty() || pwField.text.isEmpty()) {
                return false
            }
            return email.isValidEmail() && pw.length > 4
        }
    }

    var requested = false

    private fun onLoginBtnClicked() {
        binding.apply {
            val email = emailField.text
            val pw = pwField.text


            if (email.isEmpty() || pw.isEmpty()) {
                if (email.isEmpty()) emailField.showErrorMsg("이메일을 입력해주세요.")
                if (pw.isEmpty()) pwField.showErrorMsg("비밀번호를 입력해주세요.")
                return
            }

            emailField.isShownError = false
            pwField.isShownError = false

            if (!requested) { // 요청이 동시에 날라가는 케이스 방지
                requested = true
                showProgress()

                disposables += API_V3.getAppToken(RequestLogin(email, pw))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(3, TimeUnit.SECONDS)
                    .subscribe({ res ->
                        println("group error = res:${res.error}")
                        res.data?.let {
                            MyApplication.token = it.token
                            Preferences.isAvailableRushDialog.set(true)
                            hideProgress()
                            requested = false
                        }
                        handleResponse(res, email)
                    }, { error ->

                        hideProgress()
                        requested = false
//                        DialogUtils.v2LoginErrDialog(this@LoginActivity)
                        MyApplication.user = null
                        MyApplication.token = null
                        sendLoginFailLog(email)
                        (error as? HttpException)?.response()?.errorBody()?.string()?.let {
                            val listType = object: TypeToken<ResponseBody<SignInAppToken>>(){}.type
                            val response: ResponseBody<SignInAppToken> = Gson().fromJson(it, listType)
                            Log.e(javaClass.simpleName, "group error=${error.localizedMessage} , ${error.message}, ${response.error}, ${response.message}")
                            errorHandle(response)

                        }


                    })
            }
        }
    }


    private fun errorHandle(res: ResponseBody<SignInAppToken>) {
        val error = res.error
        val errMsg = res.message
        binding.apply {
            when (error) {
                WRONG_LOGINID, WRONG_LOGINPW, NOT_MATCH_PW, NOT_FOUND_DATA, LOGINID_INVALID, INVALID_AUTH -> {
                    emailField.showErrorBorder()
                    pwField.showErrorMsg("아이디(이메일) 또는 비밀번호가 잘못되었습니다.")
                }
                LOCK_ACCOUNT -> {
                    DialogUtils.lockAccountDialog(this@LoginActivity) {
                        openResetPassword()
                    }
                }
                else -> {
                    DialogUtils.showServerErr(this@LoginActivity)
                }
            }
        }
    }
    fun putFcmToken() {
        if(MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    disposables += API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({ _ ->
                            Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
                        },{ error ->
                            Log.e(javaClass.simpleName, "loginactivity putfcm token error=${error.localizedMessage}")
                        })
                }
            })
        }
    }

    fun onFindIdPwTvClicked() {
        startActivity(FindEmailAndPwActivity::class.java)
    }

    fun handleResponse(res: ResponseBody<SignInAppToken>, email: String) {
        Log.d(javaClass.simpleName, "template=${res.error}")

        if (res.error == null) {
            when (res.data?.isValidPhone) {
                false -> {
                    ConfirmPhoneDialog(this, successCB = {
                        goLearningTab(email)
                    }, failCB = { clearToken() }).show()
                }
                else -> {
                    goLearningTab(email)
                }
            }
        } else {
            sendLoginFailLog(email)
            clearToken()
            binding.apply {
                errorHandle(res)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    private fun openResetPassword() {
        Intent(this, FindEmailAndPwActivity::class.java).run {
            this.putExtra(FindEmailAndPwActivity.PAGE, 1) // 1이 비밀번호 재설정
            startActivity(this)
        }
    }

    private fun commitUser() {
        MyApplication.user?.commit("LoginActivity")
    }

    private fun sendLoginFailLog(attemptedEmail: String) {
        viewModel.sendLoginLog(null, attemptedEmail)
    }
//    private fun sendLoginLog() {
//        viewModel.sendLoginLog()
//
//    }
    private fun clearToken() {
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user?.commit("LoginActivity")
    }

    private fun goLearningTab(attemptedEmail: String) {
        viewModel.fetchUser {
            MyApplication.user = it
            MyApplication.token = it.token
            commitUser()
            putFcmToken()
            viewModel.fetchMainProfile {
                viewModel.sendLoginLog(it, attemptedEmail)
                // 교육청(AIEP)·웨일스페이스 사용자는 네이티브 Main 대신 WebView로 webapp을 사용한다
                val intent = if (it.signInChannel.usesWebApp) {
                    AiepWebViewActivity.webAppIntent(this)
                } else {
                    Intent(this, MainActivity::class.java)
                }
                startActivity(intent)
                finishAffinity()
            }
        }
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
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            binding.loadingContainer.visibility = View.GONE
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
//                    AVAILABLE -> binding.emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                    LOGINID_INVALID -> binding.emailField.showErrorMsg("이메일 형식을 확인해주세요.")
                    ALREADY_WITHDRAW, LOGINID_EXIST -> {
                        binding.emailField.isShownError = false
                    }
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
