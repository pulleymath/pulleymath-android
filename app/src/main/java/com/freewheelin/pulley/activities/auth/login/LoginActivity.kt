package com.freewheelin.pulley.activities.auth.login

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.SplashActivity
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
import com.freewheelin.pulley.core.API_V3
import com.freewheelin.pulley.databinding.ActivityLoginBinding
import com.freewheelin.pulley.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.viewmodel.LoginActViewModel
import com.freewheelin.pulley.views.editText.*
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
    val viewModel: LoginActViewModel by viewModels()

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

            dummyLoginBtn.apply {
                visibility = if (BuildConfig.FLAVOR == "beta") {
                    View.VISIBLE
                } else { View.GONE }

                setOnClickListener {
                    pwField.text = "test1234"
                    if (loginBtn.isEnableUI()) this@LoginActivity.onLoginBtnClicked()
                }
            }
            liveBtn.apply {
                visibility = if (BuildConfig.FLAVOR == "beta") {
                    View.VISIBLE
                } else { View.GONE }

                setOnClickListener {
                    changeServerApi(true)
                    Toast.makeText(this@LoginActivity, "Live Api!", Toast.LENGTH_SHORT).show();
                }
            }
            stagingBtn.apply {
                visibility = if (BuildConfig.FLAVOR == "beta") {
                    View.VISIBLE
                } else { View.GONE }

                setOnClickListener {
                    changeServerApi(false)
                    Toast.makeText(this@LoginActivity, "Staging Api!", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
    fun changeServerApi(isLive: Boolean) {
        val api = if (isLive) Network.Server.live.toString() else Network.Server.staging.toString()
        Preferences.onServerAPI.set(api)
//        val intent = Intent(this@LoginActivity, SplashActivity::class.java)
//        val mPendingIntentId = 123456
//        val mPendingIntent = PendingIntent.getActivity(this@LoginActivity, mPendingIntentId, intent, PendingIntent.FLAG_IMMUTABLE)
//        val mgr = this@LoginActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
//        System.exit(0)
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
            if (emailField.text.isEmpty() || pwField.text.isEmpty()) {
                return false
            }
            return emailField.text.isValidEmail() && pwField.text.isValidPW()
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
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
                        handleResponse(res)
                    }, { error ->

                        hideProgress()
                        requested = false
//                        DialogUtils.v2LoginErrDialog(this@LoginActivity)
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
                WRONG_LOGINID -> {
                    emailField.showErrorMsg(errMsg ?: "")
                    pwField.isShownError = false
                }
                WRONG_LOGINPW, NOT_MATCH_PW -> {
                    emailField.isShownError = false
                    pwField.showErrorMsg(errMsg ?: "")
                }
                NOT_FOUND_DATA -> {
                    pwField.isShownError = false
                    emailField.showErrorMsg(getString(R.string.text_this_email_is_not_registered))
                }
                LOGINID_INVALID -> {
                    pwField.isShownError = false
                    emailField.showErrorMsg(errMsg ?: "")
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
    fun putFcmToken() {
        if(MyApplication.token?.isNotEmpty() == true) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token?.isNotEmpty() == true) {
                    disposables += API_APP.putToken(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { _ ->
                            Log.d(javaClass.simpleName, "토큰이 등록되었습니다.")
                        }
                }
            })
        }
    }

    fun onFindIdPwTvClicked() {
        startActivity(FindEmailAndPwActivity::class.java)
    }

    fun handleResponse(res: ResponseBody<SignInAppToken>) {
        Log.d(javaClass.simpleName, "template=${res.error}")

        if (res.error == null) {
            when (res.data?.isValidPhone) {
                false -> {
                    ConfirmPhoneDialog(this, successCB = {
                        goLearningTab()
                    }, failCB = { clearToken() }).show()
                }
                else -> {
                    goLearningTab()
                }
            }
        } else {
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

    private fun clearToken() {
        MyApplication.token = ""
        MyApplication.user?.token = ""
        MyApplication.user?.commit("LoginActivity")
    }

    private fun goLearningTab() {
        viewModel.fetchUser {
            MyApplication.user = it
            MyApplication.token = it.token
            commitUser()

            putFcmToken()

            startActivity(Intent(this, LearningTabActivity::class.java))
            finishAffinity()
        }
//        disposables += API_V3.getUserObservable()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .timeout(3, TimeUnit.SECONDS)
//            .subscribe({ res ->
//                res.data.let {
//                    MyApplication.user = it
//                    MyApplication.token = it.token
//                    commitUser()
//
//                    putFcmToken()
//
//                    startActivity(Intent(this, LearningTabActivity::class.java))
//                    finishAffinity()
//                }
//            }, { error ->
//                responseFailed(this, Throwable(error.message))
//            })
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
