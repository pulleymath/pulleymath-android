package com.freewheelin.pulley.legacy.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.auth.login.LoginActivity
import com.freewheelin.pulley.legacy.activities.auth.signup.SignupActivity

import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.databinding.ActivityStartBinding
import com.freewheelin.pulley.revision2023.viewmodel.StartActViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

class StartActivity : BaseActivity(), LifecycleObserver {
    private val binding: ActivityStartBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_start,null,false)
    }
    val viewModel: StartActViewModel by viewModels()

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StartActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUI()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        addBackPressed()
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
    }

    fun onGuestEnterBtnClicked() {
        binding.progressBar.visibleIf(true)
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "StartActivity", "시작하기", "게스트로그인")

        viewModel.requestGuestSignIn { token ->
            user?.token = token
            MyApplication.token = token
            viewModel.fetchUser {
                MyApplication.user = it
                MyApplication.token = it.token
                viewModel.sendLoginLog(it, it.accountEmail)
                viewModel.fetchMainProfile {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }

    private fun initUI() {
        with(binding) {
            progressBar.visibleIf(false)
            guestText.underline()

            loginBtn.setOnClickListener {
                startActivity(LoginActivity::class.java)
            }
            joinBtn.setOnClickListener {
                startActivity(SignupActivity::class.java)
            }

            guestText.setOnClickListener {
                onGuestEnterBtnClicked()
            }

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 1000
            animator.startDelay = 500
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                (topLabelCoverVisible.layoutParams as? LinearLayout.LayoutParams)?.weight = value
                (topLabelCover.layoutParams as? LinearLayout.LayoutParams)?.weight = 1 - value
                coverLl.requestLayout()
            }
            animator.start()
        }
    }

    fun addBackPressed() {
        onBackPressedDispatcher.addCallback(this) {

            LogUtils.logEvent(this@StartActivity, user, PulleyEvent.DIALOG, "이탈방지", "가지마팝업", "가입화면")
            DialogUtils.showReluctanceDialog(this@StartActivity, leftBtnCB = {
                finish()
            }, rightBtnCB = {
                onGuestEnterBtnClicked()
            })
        }
    }

}
