package com.freewheelin.pulley.legacy.activities.auth.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.InitSettingCompleteActivity
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity
import com.freewheelin.pulley.legacy.assets.Major
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.hideKeyboard
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.core.API_V3
import com.freewheelin.pulley.databinding.ActivitySignupBinding
import com.freewheelin.pulley.legacy.assets.Grade
import com.freewheelin.pulley.legacy.core.API_APP
import com.freewheelin.pulley.legacy.dialogs.ConfirmPhoneDialog
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.UserV4
import com.freewheelin.pulley.revision2023.viewmodel.SignUpActViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.model.SignInAppToken
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit


class SignupActivity : BaseActivity(), StudentInfoInterface {

    companion object {
        val IS_GUEST_USER = "IS_GUEST_USER"
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, SignupActivity::class.java)
            return intent
        }
        fun getIntent(context: Context, isGuestUser: Boolean): Intent {
            return Intent(context, SignupActivity::class.java).apply {
                putExtra(IS_GUEST_USER, isGuestUser)
            }
        }

        var signup = RequestSignup()
    }
    lateinit var signupFragment: SignupFragment
    lateinit var studentInfoFragment: StudentInfoFragment
    val viewModel: SignUpActViewModel by viewModels()

    var isGuestUser = false

    private val binding: ActivitySignupBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_signup, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isGuestUser = intent.getBooleanExtra(IS_GUEST_USER, false)
        initUI()
        initObserve()
    }

    private fun initObserve() {
        viewModel.apply {
            isLoading.observe(this@SignupActivity) { loading ->
                binding.apply {
                    if (loading) {
                        loadingContainer.visibleIf(true)
                        loadingLottie.playAnimation()
                    } else {
                        loadingContainer.hide(300)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.lifecycleOwner = this
        binding.rootView.setOnTouchListener { view, motionEvent ->
            currentFocus?.let { hideKeyboard(it) }
            false
        }
        setViewPager()
    }

    private fun setViewPager() {
        signupFragment = SignupFragment.newInstance(isGuestUser)
        studentInfoFragment = StudentInfoFragment()

        val pagerAdapter = SignupViewPagerAdapter(listOf(signupFragment,studentInfoFragment), this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = pagerAdapter
    }

    override fun goBack() {
        onBackPressed()
    }

    override fun goStudentInfo() {
        binding.viewPager.currentItem = 1
    }

    override fun onBackPressed() {
        if(binding.viewPager.currentItem == 1) {
            binding.viewPager.currentItem = 0
        } else {
            if(!signupFragment.isAllEmpty() || signupFragment.isAvailableToSecondStep()){ // 동의 후 이탈일 경우 팝업 확인
                DialogUtils.confirmDialog(this, "알림", "회원가입을 완료하지 않았습니다. 회원가입을 중단하시겠습니까?", rightBtnCB = {
                    super.onBackPressed()
                })
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun regist(school:Int?, region:Int?, selectedGrade:Int, rate:Int, major:Int) {
        viewModel.setLoading(true)
        signup.schoolInfo.apply {
            schoolID = school
            regionID = region
            grade = selectedGrade
            initMoGrade = rate
            majorType = if(major < 0) "" else Major.getValue(major)

            val isHighSchoolUser = Grade.init(grade).isInitialSchoolTypeHigh
            if (isGuestUser) {
                signup.studentId = user?.studentID
                viewModel.requestGuestSignUp(signup) {
                    signupSuccess(isHighSchoolUser)
                }
            } else {
                viewModel.requestUserSignUp(signup) {
                    signupSuccess(isHighSchoolUser)
                }
            }
        }
    }

    private fun signupSuccess(isHighSchoolUser: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            LogUtils.logSignUpEvent(this@SignupActivity, signup.email)
            login(signup.email, signup.password, isHighSchoolUser)
        }
    }

    private fun fetchUser(isHighSchoolUser: Boolean) {
        viewModel.fetchUser {
            MyApplication.user = it
            MyApplication.token = it.token
            FirebaseCrashlytics.getInstance().setUserId(it.studentID)
            putFcmToken()
            loginSuccess(isHighSchoolUser)
        }
    }
    private fun login(email: String, pw: String, isHighSchoolUser: Boolean) {
        disposables += API_V3.getAppToken(RequestLogin(email, pw))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                viewModel.setLoading(false)
                res.data?.let {
                    MyApplication.token = it.token
                }
                fetchUser(isHighSchoolUser)
            }, { error ->
                viewModel.setLoading(false)

                (error as? HttpException)?.response()?.errorBody()?.string()?.let {
                    val listType = object: TypeToken<ResponseBody<SignInAppToken>>(){}.type
                    val response: ResponseBody<SignInAppToken> = Gson().fromJson(it, listType)
                    Log.e(javaClass.simpleName, "group error=${error.localizedMessage} , ${error.message}, ${response.error}, ${response.message}")
                    loginFailed(response)

                }
            })
//
//        API_V3.loginApp(RequestLogin(email, pw)).enqueue(object: Callback<Template<User?>> {
//            override fun onFailure(call: Call<Template<User?>>, t: Throwable) {
//                viewModel.setLoading(false)
//                responseFailed(this@SignupActivity, t)
//            }
//
//            override fun onResponse(call: Call<Template<User?>>, response: Response<Template<User?>>) {
//                viewModel.setLoading(false)
//                Preferences.isAvailableRushDialog.set(true)
//                val user = response.body()?.data
//                user?.let { FirebaseCrashlytics.getInstance().setUserId(it.studentID) }
//
//                when(response.code()) {
//                    200 -> loginSuccess(user, isHighSchoolUser)
//                    else -> loginFailed(response)
//                }
//            }
//        })
    }
    private fun loginSuccess(isHighSchoolUser: Boolean) {
        viewModel.requestSignUpReward {
            startActivity(InitSettingCompleteActivity.getIntent(this, isHighSchoolUser, isGuestUser))
            if (isGuestUser) {
                finish()
            } else {
                finishAffinity()
            }
        }

    }

    private fun loginFailed(response: ResponseBody<SignInAppToken>) {

        when(response.error) {
            LoginActivity.NOT_MATCH_PW -> {
                signupFragment.binding.emailDet.isShownError = false
                signupFragment.binding.pwDet.showErrorMsg(response.message?:"")
            }
            LoginActivity.LOCK_ACCOUNT, LoginActivity.LOGINID_INVALID -> {
                signupFragment.binding.emailDet.showErrorMsg(response.message?:"")
                signupFragment.binding.pwDet.isShownError = false
            }
            else -> {
                DialogUtils.showServerErr(this)
            }
        }
    }

    class SignupViewPagerAdapter(val fragments:List<Fragment>, activity:FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int) = fragments.get(position)
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
}

interface StudentInfoInterface {
    fun goBack()
    fun goStudentInfo()
    fun regist(school:Int?, region:Int?, year:Int, rate:Int, major:Int)
}