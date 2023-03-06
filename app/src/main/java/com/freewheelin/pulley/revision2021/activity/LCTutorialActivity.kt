package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.SnackReportActivity
import com.freewheelin.pulley.bases.MyApplication
import com.freewheelin.pulley.core.manage.AppUsageMonitor
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityLcTutorialBinding
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.viewmodel.LCTutorialViewModel
import com.freewheelin.pulley.utils.*

class LCTutorialActivity : AppCompatActivity() {
    companion object {
        val FROM_MAIN_ACTIVITY = "FROM_MAIN"
        fun getIntent(context: Context, isFromMainActivity: Boolean = false): Intent {
            return Intent(context, LCTutorialActivity::class.java).apply {
//                putExtra(FROM_MAIN_ACTIVITY, isFromMainActivity)
            }
        }
        fun getIntentAddFlags(context: Context, isFromMainActivity: Boolean = false): Intent {
            return Intent(context, LCTutorialActivity::class.java).apply {
//                putExtra(FROM_MAIN_ACTIVITY, isFromMainActivity)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    val binding: ActivityLcTutorialBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_lc_tutorial,null,false)
    }
    val viewModel: LCTutorialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(binding.root)


        Preferences.isConceptLearningTutorialPassed.set(true)

        binding.apply {
            vm = viewModel
            lifecycleOwner = this@LCTutorialActivity

            viewModel.createLearningCourseOnStudentId {}

            evenWrapperCl.setOnClickListener { nextEvent() }
            evenExitBtn.setOnClickListener {
                val sequence = viewModel.sequence.value
                LogUtils.logEvent(this@LCTutorialActivity,
                    MyApplication.user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "종료다이얼로그", "${sequence}")
                showTutorialEndDialog()
            }
            oddWrapperCl.setOnClickListener { nextEvent() }
            oddExitBtn.setOnClickListener {
                val sequence = viewModel.sequence.value
                LogUtils.logEvent(this@LCTutorialActivity,
                    MyApplication.user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "종료다이얼로그", "${sequence}")
                showTutorialEndDialog()
            }

            viewModel.sequence.observe(this@LCTutorialActivity) { seq ->

                if (viewModel.isSeqOver(seq)) {
                    goToMainActivity(true)
                    return@observe
                }
                LogUtils.logEvent(this@LCTutorialActivity,
                    MyApplication.user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "튜토리얼이미지", "${seq}")
                changeImageWrapperCl(seq)
                viewModel.answerApiCall(seq)


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUsageMonitor.finishAppUsage()
    }

    override fun onResume() {
        super.onResume()
        AppUsageMonitor.startAppUsage()
    }

    private fun changeImageWrapperCl(seq: Int) {
        binding.apply {
            val isSeqOdd = seq % 2 == 1
            val uri = viewModel.tutorialImages.get(seq)
            if (isSeqOdd) {
                Glide.with(this@LCTutorialActivity)
                    .load(uri)
                    .into(oddSeqIv)
                evenWrapperCl.hide(100) {  }
                val transition = viewModel.transitionList.get(seq)
                oddWrapperCl.showTransition(500, transition) {  }
            } else {
                Glide.with(this@LCTutorialActivity)
                    .load(uri)
                    .into(evenSeqIv)
                val transition = viewModel.transitionList.get(seq)
                oddWrapperCl.hide(100) {  }
                evenWrapperCl.showTransition(500, transition) {  }
            }
        }
    }
    private fun hideSystemUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    }
    private fun goToMainActivity(isEnded: Boolean = false) {

        println("asoaso completed go to mainACtivity ")
        if (isEnded) {
            setResult(ConceptCourseFragment.CHALLENGE_TUTORIAL_FINISH, intent)
        }
        finish()
    }

    fun nextEvent() {
        viewModel.sequencePlus1()
    }
    fun prevEvent() {
        viewModel.sequenceMinus1()
    }

    override fun onBackPressed() {
        if (viewModel.sequence.value == 0) {
            showTutorialEndDialog()
        } else {
            prevEvent()
            return
        }

    }

    fun showTutorialEndDialog() {
        DialogUtils.showTutorialEndDialog(this, leftBtnCB = {
            val sequence = viewModel.sequence.value
            LogUtils.logEvent(this@LCTutorialActivity,
                MyApplication.user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "튜토리얼종료", "${sequence}")
            goToMainActivity()
        }, rightBtnCB = {
            LogUtils.logEvent(this@LCTutorialActivity,
                MyApplication.user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "이어보기", "")
        })
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}