package com.freewheelin.pulley.activities.auth.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.InitSettingCompleteActivity
import com.freewheelin.pulley.activities.auth.findEmailAndPw.FindEmailAndPwActivity
import com.freewheelin.pulley.activities.auth.login.LoginActivity
import com.freewheelin.pulley.assets.Major
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.hideKeyboard
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.core.API_V3
import com.freewheelin.pulley.databinding.ActivitySignupBinding
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.viewmodel.SignUpActViewModel
import com.freewheelin.pulley.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    }

    private fun initUI() {
        binding.rootView.setOnTouchListener { view, motionEvent ->
            currentFocus?.let { hideKeyboard(it) }
            false
        }
        setViewPager()
    }

    private fun setViewPager() {
        signupFragment = SignupFragment(isGuestUser)
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
        signup.schoolInfo.apply {
            schoolID = school
            regionID = region
            grade = selectedGrade
            initMoGrade = rate
            majorType = if(major < 0) "" else Major.getValue(major)

            // TODO  Signup 이후에 알림톡발송을 위한 API를 쏴야한다 4월13일
            if (isGuestUser) {
                signup.studentId = user?.studentID
                viewModel.requestGuestSignUp(signup) {
                    viewModel.requestSignUpReward {
                        signupSuccess()
                    }
                }
            } else {
                viewModel.requestUserSignUp(signup) {
                    viewModel.requestSignUpReward {
                        signupSuccess()
                    }
                }
            }
        }
    }

    private fun signupSuccess() {
        CoroutineScope(Dispatchers.Main).launch {
            LogUtils.logSignUpEvent(this@SignupActivity, signup.email)
            login(signup.email, signup.password)
        }
    }

    private fun login(email:String, pw:String) {
        API_V3.loginApp(RequestLogin(email, pw)).enqueue(object: Callback<Template<User?>> {
            override fun onFailure(call: Call<Template<User?>>, t: Throwable) {
                responseFailed(this@SignupActivity, t)
            }

            override fun onResponse(call: Call<Template<User?>>, response: Response<Template<User?>>) {
                Preferences.isAvailableRushDialog.set(true)
                val user = response.body()?.data
                user?.connectToCrashlytics()

                when(response.code()) {
                    200 -> loginSuccess(user)
                    else -> loginFailed(response)
                }
                if (isGuestUser) {
                    finish()
                } else {
                    finishAffinity()
                }
            }
        })
    }

    private fun loginSuccess(user:User?) {
        MyApplication.user = user
        MyApplication.token = user?.token
//        if(MyApplication.user == null) MyApplication.user = user
//        else MyApplication.user!!.update(user)
        startActivity(InitSettingCompleteActivity.getIntent(this, isGuestUser))
    }

    private fun loginFailed(response: Response<Template<User?>>) {
        val errorTemplate = response?.errorBody()?.let { errorBody ->
            Gson().fromJson(errorBody.string(), ResponseBody::class.java)
        }
        when(errorTemplate?.error) {
            LoginActivity.NOT_MATCH_PW -> {
                signupFragment.binding.emailDet.isShownError = false
                signupFragment.binding.pwDet.showErrorMsg(errorTemplate.message?:"")
            }
            LoginActivity.LOCK_ACCOUNT, LoginActivity.LOGINID_INVALID -> {
                signupFragment.binding.emailDet.showErrorMsg(errorTemplate.message?:"")
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
}

interface StudentInfoInterface {
    fun goBack()
    fun goStudentInfo()
    fun regist(school:Int?, region:Int?, year:Int, rate:Int, major:Int)
}