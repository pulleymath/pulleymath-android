package com.freewheelin.pulley.activities.auth

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.databinding.ActivityStudyReportBinding
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudyReportActivity : BaseActivity() {
    var isFromInit: Boolean = false
    companion object {
        const val ARG_FROM_INIT = "ARG_FROM_INIT"

        fun getIntent(context: Context, fromInit: Boolean): Intent {
            val intent = Intent(context, StudyReportActivity::class.java)
            intent.putExtra(ARG_FROM_INIT, fromInit)
            return intent
        }
    }
    private val binding: ActivityStudyReportBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_study_report, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            mailContainerCl.visibility = View.GONE
            mailGuideText.extensionTouchArea(12.toPx())
            isFromInit = intent.getBooleanExtra(ARG_FROM_INIT, false)
            bottomActionButton.visibility = View.GONE
            if(isFromInit) {
                Handler(Looper.getMainLooper()).postDelayed({
                    bottomActionButton.show()
                },5000)
            } else {
                headerView.visibility = View.VISIBLE
            }

            backBtn.setOnClickListener {
                onBackPressed()
            }

            API_V2.getReportUrl(user!!.studentID).enqueue(object: Callback<Map<String, String>>{
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    responseFailed(this@StudyReportActivity, t)
                }

                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    val result = response.body()
                    val reportURL = result?.get("reportURL")
                    if(reportURL != null) {
                        Picasso. get()
                            .load(reportURL + "1.5x.jpg")
                            .resize(DisplayUtils.getScreenWidth(this@StudyReportActivity), 0)
                            .into(reportIv, object: com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    mailContainerCl.show()
                                }

                                override fun onError(e: Exception?) {}
                            })
                    } else {
                        responseError(this@StudyReportActivity, response)
                    }
                }
            })
            bottomActionButton.setOnClickListener {
                LogUtils.logEvent(this@StudyReportActivity, user!!, PulleyEvent.INIT_TEST, "스낵보고서", "공부시작")
                if(!user!!.hasPulleyPlus)
                    FacebookEvent.log(this@StudyReportActivity, FacebookEvent.TUTORIAL_FINISHED)
                moveToMain()
            }

            mailGuideText.setOnClickListener { onMailBtnClicked() }
            mailBtn.setOnClickListener { onMailBtnClicked() }
            if(user!!.hasPulleyPlus)
                bottomActionButton.text = "스낵테스트 종료하기"
            else
                bottomActionButton.text = "스낵테스트 종료하기"
        }
    }

    override fun onBackPressed() {
        if(isFromInit) {
            LogUtils.logEvent(this, user, PulleyEvent.DIALOG, "이탈방지", "가지마팝업", "초기보고서")
            DialogUtils.showReluctanceDialog(this, leftBtnCB = {
                finish()
            })
        } else {
            super.onBackPressed()
        }
    }
    fun onMailBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.INIT_TEST, "보고서", "메일보내기")
        API_V2.postReportMail(user!!.studentID).enqueue(object: Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                DaebakToast.show(this@StudyReportActivity, "메일 보내기에 실패했습니다. 문제가 지속되면 고객센터에 문의해주세요.")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    DaebakToast.show(this@StudyReportActivity, "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
                } else {
                    DaebakToast.show(this@StudyReportActivity, "메일 보내기에 실패했습니다. 문제가 지속되면 고객센터에 문의해주세요.")
                }
            }

        })
    }

    fun moveToMain() {
        val intent = LearningTabActivity.getIntent(this, )
        startActivity(intent)
    }

    fun showHeaderView() {
        binding.apply {
            if(headerView.translationY == 0f) return

            val showAnimator = ValueAnimator.ofFloat(headerView.translationY, 0f)
            showAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                headerView.translationY = value
            }
            showAnimator.duration = 200
            showAnimator.start()
        }
    }

    fun hideHeaderView() {
        binding.apply {
            if(headerView.translationY == headerView.height * -1f) return

            val hideAnimator = ValueAnimator.ofFloat(headerView.translationY, headerView.height * -1f)
            hideAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                headerView.translationY = value
            }
            hideAnimator.duration = 200
            hideAnimator.start()
        }
    }
}
