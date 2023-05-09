package com.freewheelin.pulley.activities.mypage

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.main.mypage.*
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityMyRecommendSettingBinding
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2021.model.response.Alarm
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.freewheelin.pulley.revision2023.viewmodel.SnackTestFragViewModel
import com.freewheelin.pulley.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.*

class MyRecommendSettingActivity: AppCompatActivity(), MyPageActionListener {

    val viewModel: SnackTestFragViewModel by viewModels()
    val myPageViewModel: MyMainPageFragViewModel by viewModels()
    private val binding: ActivityMyRecommendSettingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_my_recommend_setting, null, false)
    }
    companion object {
        val TEST_EXTRA = "TEST_EXTRA"
        fun getIntent(context: Context, test: Test): Intent {
            val intent = Intent(context, MyRecommendSettingActivity::class.java).apply {
                putExtra(TEST_EXTRA, test)
            }
            return intent
        }
    }

//    var commonUnits = setOf<BigUnitV3>()
//    var optionalUnits = setOf<BigUnitV3>()
//    var recentUnits = setOf<BigUnitV3>()
//    var excludedUnits = setOf<BigUnitV3>()

    val difficultyButtonIDs: List<Int>
        get() = listOf(R.id.lowButton, R.id.middleButton, R.id.highButton)

    val coverRangeButtonIDs: List<Int>
        get() = listOf(R.id.sameButton, R.id.aheadButton, R.id.tailButton)

    lateinit var commonUnitFragment:Fragment
    lateinit var optionalUnitFragment:Fragment
    lateinit var selectedUnitFragment:Fragment
    lateinit var middleCommonUnitFragment:Fragment

    var test: Test? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        test = intent.getSerializableExtra(TEST_EXTRA) as? Test
        setUpUI()
        viewModel.apply {
            val thisOwner = this@MyRecommendSettingActivity
            schoolType.observe(thisOwner) {
                println("qwoqwo MyRecommend Setting Activity schoolType : ${it}")

            }
            recommendCommonSubjects.observe(thisOwner) { subjects ->
                binding.apply {
                    val subjectNames = getSubjectNames(subjects)

                    sameCommonText.text = subjectNames
                    sameOptionalText.text = subjectNames
                    myChoiceCommonText.text = subjectNames
                    middleSubjectText.text = subjectNames
                    myChoiceMiddleSubjectText.text = subjectNames
                }
            }
            recommendOptionalSubjects.observe(thisOwner) { subjects ->

                binding.apply {
                    val subjectNames = getSubjectNames(subjects)
                    aheadOptionalText.text = subjectNames
                    myChoiceOptionalText.text = subjectNames
                }
            }
        }
    }
    private fun getSubjectNames(list: List<RecommendSubject>): String {
        return list
            .filter {
                it.chapters
                    .map { it.isSelected }
                    .reduce { p1, p2 ->
                        p1 || p2
                    }
            }
            .map { it.subjectName }
            .joinTo(StringBuilder(), ", ").toString()

    }

    fun setUpUI() {
        binding.vm = viewModel
        binding.lifecycleOwner = this
        viewModel.fetchRecommendSubject()

        setScreen()
//        setUnitTempories()
        setFragment()
        setButtonUI()
        setRadioUI()
        setSubjectText()
    }

    fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(this) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
//
//    fun setUnitTempories() {
//        commonUnits   = user!!.studiedUnit
//        optionalUnits = user!!.optionalUnit
//        recentUnits   = user!!.recentUnit
//        excludedUnits = user!!.recentExcludedUnit
//    }

    fun setFragment() {
        commonUnitFragment = StudyCommonUnitSettingFragment()
        optionalUnitFragment = StudyOptionalUnitSettingFragment()
        selectedUnitFragment = StudySelectedUnitSettingFragment()
        middleCommonUnitFragment = StudyMiddleCommonUnitSettingFragment()
    }

    fun setButtonUI() {
        with(binding) {
            sameCommonButton.setOnClickListener { moveTo(commonUnitFragment) }
            sameOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }
            sameMyButton.setOnClickListener { moveTo(selectedUnitFragment) }
            middleSubjectButton.setOnClickListener { moveTo(middleCommonUnitFragment) }
            sameMiddleMyButton.setOnClickListener { moveTo(middleCommonUnitFragment) }

            aheadOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }

            myChoiceCommonButton.setOnClickListener { moveTo(commonUnitFragment) }
            myChoiceOptionalButton.setOnClickListener { moveTo(optionalUnitFragment) }
            myChoiceMiddleSubjectButton.setOnClickListener { moveTo(middleCommonUnitFragment) }

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
            test?.let {
                val level = it.dailyInfo.testLevel
                val testLevel = Test.TestLevel.ordinalOfNonNull(level)
                val levelRadioBtn = when (testLevel) {
                    Test.TestLevel.HIGH -> R.id.highButton
                    Test.TestLevel.LIKE_ME -> R.id.middleButton
                    Test.TestLevel.EASY -> R.id.lowButton
                }
                levelRg.check(levelRadioBtn)

                val range = it.dailyInfo.testRange
                val testRange = Test.TestRange.ordinalOfNonNull(range)
                val radioBtn = when (testRange) {
                    Test.TestRange.RECENT_RANGE -> R.id.sameButton
                    Test.TestRange.ALL_RANGE ->
                        if (schoolType.isMiddle) {
                            R.id.tailButton
                        } else {
                            R.id.aheadButton
                        }
                    Test.TestRange.SUBJECT_BY_GRADE -> R.id.tailButton
                }
                rangeRg.check(radioBtn)
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
                    val isSubjectCodeNotEmpty = test?.dailyInfo?.subjectCode?.isNotEmpty() == true
                    if(isSubjectCodeNotEmpty) {
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
        myPageViewModel.updateRecommends(param) {
            user!!.update(
                recommendLevel = recommendLevel,
                recommendChapter = recommendChapter
            )
            val intent = Intent(TestManager.EVENT_TEST_SETTING)
            LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)

            finish()
        }
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
        viewModel.fetchRecommendSubject()
    }

//    private fun getDurationText(user: User): String {
//        if(user.startDate == null || user.endDate == null) {
////            LogUtils.assert(false, "유저 start 또 enddate가 존재하지 않음 " +
////                    "studentID: ${user.studentID}" +
////                    "mebership: ${user.memberExperiencedType}" +
////                    "startDate: ${user.startDate}" +
////                    "endDate: ${user.endDate}")
//            return ""
//        } else {
//            val now = Date()
//            val startDate: Date = if(now > user.startDate) now else user.startDate!!
//            val endDate = user.endDate!!
//
//            if(startDate < endDate) {
//                return "${DateTimeUtils.yyyyMMddFormat.format(user.startDate)}" +
//                        " - " +
//                        "${DateTimeUtils.yyyyMMddFormat.format(user.endDate)}"
//            } else {
//                return "${DateTimeUtils.yyyyMMddFormat.format(user.startDate)}" +
//                        " - " +
//                        "${DateTimeUtils.yyyyMMddFormat.format(user.endDate)}"
//            }
//        }
//    }

    override fun onModifyCompleted() {
        setUpUI()
    }
}