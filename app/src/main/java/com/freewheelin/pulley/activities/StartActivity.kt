package com.freewheelin.pulley.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.login.LoginActivity
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.ActivityStartBinding
import com.freewheelin.pulley.utils.*

class StartActivity : BaseActivity(), LifecycleObserver {
    private val binding: ActivityStartBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_start,null,false)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StartActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initTablet()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()
    }

    fun onLoginBtnClicked() {
        startActivity(LoginActivity::class.java)
        finish()
    }

    fun onStartBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.INIT_SETTING, "이닛세팅", "가입시작")
        startActivity(SignupActivity::class.java)
    }

    private fun initTablet() {
        with(binding) {
            loginTv.visibility = View.INVISIBLE
            loginLabel.visibility = View.INVISIBLE
            loginBorder.visibility = View.INVISIBLE

            startBtn.setOnClickListener {
                onStartBtnClicked()
            }

            loginTv.setOnClickListener {
                onLoginBtnClicked()
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
            animator.addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    loginTv.show()
                    loginLabel.show()
                    loginBorder.show()
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

            })
            animator.start()
        }
    }

    override fun onBackPressed() {
        LogUtils.logEvent(this, user, PulleyEvent.DIALOG,"이탈방지", "가지마팝업", "가입화면")
        DialogUtils.showReluctanceDialog(this, leftBtnCB = {
            finish()
        }, rightBtnCB = {
            startActivity(SignupActivity::class.java)
        })
    }
}
