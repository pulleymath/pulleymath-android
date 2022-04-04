package com.freewheelin.pulley.activities.mypage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyCommonUnitSettingFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyInfoSettingFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudyOptionalUnitSettingFragment
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.StudySelectedUnitSettingFragment
import com.freewheelin.pulley.assets.BigUnit
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityMyRecommendSettingBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.*

class MyRecommendSettingActivity: AppCompatActivity(), MyPageActionListener {
    private val binding: ActivityMyRecommendSettingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_my_recommend_setting, null, false)
    }
    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, MyRecommendSettingActivity::class.java)
            return intent
        }
    }

    var commonUnits = setOf<BigUnit>()
    var optionalUnits = setOf<BigUnit>()
    var recentUnits = setOf<BigUnit>()
    var excludedUnits = setOf<BigUnit>()

    val difficultyButtonIDs: List<Int>
        get() = listOf(R.id.lowButton, R.id.middleButton, R.id.highButton)

    val coverRangeButtonIDs: List<Int>
        get() = listOf(R.id.sameButton, R.id.aheadButton, R.id.tailButton)

    lateinit var commonUnitFragment:Fragment
    lateinit var optionalUnitFragment:Fragment
    lateinit var selectedUnitFragment:Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpUI()
    }

    fun setUpUI() {
        setScreen()
        setUnitTempories()
        setFragment()
        setButtonUI()
        setRadioUI()
        setSubjectText()
    }

    fun setScreen() {
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScrenHeight(this) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }

    fun setUnitTempories() {
        commonUnits   = user!!.studiedUnit
        optionalUnits = user!!.optionalUnit
        recentUnits   = user!!.recentUnit
        excludedUnits = user!!.recentExcludedUnit
    }

    fun setFragment() {
        commonUnitFragment = StudyCommonUnitSettingFragment()
        optionalUnitFragment = StudyOptionalUnitSettingFragment()
        selectedUnitFragment = StudySelectedUnitSettingFragment()
    }

    fun setButtonUI() {
        with(binding) {
            sameCommonButton.setOnClickListener { moveTo(commonUnitFragment) }
            sameOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }
            sameMyButton.setOnClickListener { moveTo(selectedUnitFragment) }

            aheadOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }

            myChoiceCommonButton.setOnClickListener { moveTo(commonUnitFragment) }
            myChoiceOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }

            cancelBtn.setOnClickListener {
                cancelConfigure()
            }
            saveBtn.setOnClickListener {
                sendConfigure()
            }
        }
    }

    fun setRadioUI() {
        with(binding) {
            val user = user ?: return

            val recommendLevel = user.recommendLevel
            val recommendChapter = user.recommendChapter

            if(recommendLevel != null && recommendLevel > -1) {
                levelRg.check(difficultyButtonIDs[recommendLevel])
            } else {
                levelRg.check(R.id.lowButton)
            }
            if(recommendChapter != null && recommendChapter > -1) {
                rangeRg.check(coverRangeButtonIDs[recommendChapter])
            } else {
                rangeRg.check(R.id.sameButton)
            }

            levelRg.setOnCheckedChangeListener { group, checkedId ->
                showSubView()
            }
            rangeRg.setOnCheckedChangeListener { group, checkedId ->
                showSubView()
            }

            showSubView()
        }
    }

    fun setSubjectText() {
        with(binding) {
            sameCommonText.text = calcNoneText(user?.getCommonSubjectText())
            sameOptionalText.text = calcNoneText(user?.getOptionalSubjectText())
            sameMyText.text = calcNoneText(user?.getRecentSubjectText())

//        aheadCommonText.text = calcNoneText(user?.getCommonSubjectText())
            aheadOptionalText.text = calcNoneText(user?.getOptionalSubjectText())

            myChoiceCommonText.text = calcNoneText(user?.getCommonSubjectText())
            myChoiceOptionalText.text = calcNoneText(user?.getOptionalSubjectText())

            if(user?.optionalUnit?.isNotEmpty() == true) {
                myChoiceLabel.text = "수정하기에서 대단원 선택이 가능합니다."
            } else {
                myChoiceLabel.text = "선택과목이 없어, 공통과목으로만 5문제를 선별해 출제합니다."
            }
        }
    }

    private fun calcNoneText(text:String?) : String {
        return if(text?.isNotEmpty() == true) text
        else "없음"
    }

    fun showSubView() {
        with(binding) {
            when(rangeRg.checkedRadioButtonId) {
                R.id.aheadButton -> {showRangeContainer(aheadContainer)}
                R.id.tailButton -> {showRangeContainer(myChoiceContainer)}
                else -> {
                    if(user?.recentSubjectCode?.isNotEmpty() == true) {
                        showRangeContainer(sameContainerOver50)
                    } else {
                        showRangeContainer(sameContainerBelow50)
                    }
                }
            }
        }
    }

    fun showRangeContainer(container: View) {
        with(binding) {
            sameContainerBelow50.visibility = View.GONE
            sameContainerOver50.visibility = View.GONE
            aheadContainer.visibility = View.GONE
            myChoiceContainer.visibility = View.GONE

            container.visibility = View.VISIBLE
        }
    }

    fun sendConfigure() {
        val recommendLevel = difficultyButtonIDs.indexOf(binding.levelRg.checkedRadioButtonId)
        val recommendChapter = coverRangeButtonIDs.indexOf(binding.rangeRg.checkedRadioButtonId)

        val param: Parameter = Parameter(
                "recommendLevel" to recommendLevel,
                "recommendChapter" to recommendChapter
        )

        API_V2.setUserDailySetup(user!!.studentID, param).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(baseContext, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    user!!.update(
                            recommendLevel = recommendLevel,
                            recommendChapter = recommendChapter
                    )
//                    dismiss()
//                    showCompleteDialog()
//                    listener?.onModifyCompleted(user)

                    val intent = Intent(TestManager.EVENT_TEST_SETTING)
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)

                    finish()
                }
            }
        })
    }

    fun cancelConfigure() {
        val intent = Intent(TestManager.EVENT_TEST_SETTING)
        LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        finish()
    }

    private fun onRecommendUnitSettingBtnClickked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-공통단원")
        val fragment = StudyCommonUnitSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    private fun onOptionalUnitSettingBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-선택단원")
        val fragment = StudyOptionalUnitSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    private fun onStudyInfoSettingBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "마이페이지", "수정-학년정보")
        val fragment = StudyInfoSettingFragment()
        fragment.listener = this
        moveTo(fragment)
    }

    fun moveTo(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.add(R.id.childContainer, frag)
        tran.commit()
    }

    fun back(frag: Fragment, withAnim: Boolean = true) {
        setSubjectText()

        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.remove(frag)
        tran.commit()
    }

    private fun getDurationText(user: User): String {
        if(user.startDate == null || user.endDate == null) {
//            LogUtils.assert(false, "유저 start 또 enddate가 존재하지 않음 " +
//                    "studentID: ${user.studentID}" +
//                    "mebership: ${user.memberExperiencedType}" +
//                    "startDate: ${user.startDate}" +
//                    "endDate: ${user.endDate}")
            return ""
        } else {
            val now = Date()
            val startDate: Date = if(now > user.startDate) now else user.startDate!!
            val endDate = user.endDate!!

            if(startDate < endDate) {
                return "${DateTimeUtils.yyyyMMddFormat.format(user.startDate)}" +
                        " - " +
                        "${DateTimeUtils.yyyyMMddFormat.format(user.endDate)}"
            } else {
                return "${DateTimeUtils.yyyyMMddFormat.format(user.startDate)}" +
                        " - " +
                        "${DateTimeUtils.yyyyMMddFormat.format(user.endDate)}"
            }
        }
    }

    override fun onModifyCompleted() {
        setUpUI()
    }
}