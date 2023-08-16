package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestExamRangeBinding
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast

class SnackTestSelectExamRangeFragment() : Fragment() {
    private lateinit var binding: FragmentTestExamRangeBinding
    private lateinit var test: Test

    lateinit var viewModel: RecommendSettingViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_exam_range, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()
            setRadioGroup()
            setModifyBtnClickListener()
        }
        viewModel.apply {
            userRecommendLiveData.observe(viewLifecycleOwner) {
                val selectedCommonSubjects = viewModel.getUserSelectedCommonSubjects(it)
                val selectedOptionalSubjects = viewModel.getUserSelectedOptionalSubjects(it)
                val recentStudiedSubjects = viewModel.getRecentStudySubjects(it)

                binding.apply {
                    val filteredSubjects = getReducedFilterTextFromSubject(recentStudiedSubjects)
                    sameMyText.text = filteredSubjects
                    isRecentStudiedRangeEmpty.postValue(filteredSubjects == "없음")


                    myChoiceCommonText.text = getReducedFilterTextFromSubject(selectedCommonSubjects)
                    myChoiceMiddleSubjectText.text = getReducedFilterTextFromSubject(selectedCommonSubjects)

                    satOptionalText.text = getReducedFilterTextFromSubject(selectedOptionalSubjects)
                    myChoiceOptionalText.text = getReducedFilterTextFromSubject(selectedOptionalSubjects)
                }
            }
        }

        arguments?.let {

        }
    }

    private fun getReducedFilterTextFromSubject(list: List<SubjectV3>): String {
        val result = list.map { it.filterText }.distinct().reduceOrNull { prev, next -> "$prev ,$next" }
        return result ?: "없음"
    }
    private fun setModifyBtnClickListener() {
        binding.apply {
            commonSubjectModifyBtn.setOnClickListener {
                viewModel.setStep(ViewType.과목제외)
            }
            middleSubjectModifyBtn.setOnClickListener {
                viewModel.setStep(ViewType.과목제외)
            }

            satOptionalSubjectModifyBtn.setOnClickListener { viewModel.setStep(ViewType.고등선택과목수정) }
            myChoiceCommonSubjectModifyBtn.setOnClickListener { viewModel.setStep(ViewType.고등공통과목수정) }
            myChoiceOptionalSubjectModifyBtn.setOnClickListener { viewModel.setStep(ViewType.고등선택과목수정) }
            myChoiceMiddleSubjectModifyBtn.setOnClickListener { viewModel.setStep(ViewType.중등과목수정) }

            cancelBtn.setOnClickListener { cancelConfigure() }
            saveBtn.setOnClickListener { sendConfigure() }
        }
    }
    fun cancelConfigure() {
        val intent = Intent(TestManager.EVENT_TEST_SETTING)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        viewModel.exitBtn()
    }
    fun sendConfigure() {
        viewModel.apply {
            val recommendLevel = selectedLevelIndex
            val recommendChapter = selectedRangeIndex
            val param: Parameter = Parameter(
                "recommendLevel" to recommendLevel,
                "recommendChapter" to recommendChapter
            )

            updateRecommends(param) {
                val intent = Intent(TestManager.EVENT_TEST_SETTING)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                exitBtn()
            }
        }
    }

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        binding.rootView.layoutParams = lp
    }
    private fun setRadioGroup() {
        binding.apply {
            initLevelRadioGroup()
            initRangeRadioGroup()
            showSubView()
        }
    }

    private fun initLevelRadioGroup() {
        binding.apply {
            val level = test.dailyInfo.testLevel
            val testLevel = Test.TestLevel.ordinalOfNonNull(level)
            val levelRadioBtn = when (testLevel) {
                Test.TestLevel.HIGH -> R.id.highButton
                Test.TestLevel.LIKE_ME -> R.id.middleButton
                Test.TestLevel.EASY -> R.id.lowButton
            }
            viewModel.selectedLevelIndex = testLevel.ordinal
            levelRg.check(levelRadioBtn)

            levelRg.setOnCheckedChangeListener { _, checkedId ->
                viewModel.selectedLevelIndex = getLevelIndex(checkedId)
                showSubView()
            }
        }
    }
    private fun getLevelIndex(id: Int): Int {
        return when (id) {
            R.id.lowButton -> 0
            R.id.middleButton -> 1
            R.id.highButton -> 2
            else -> -1
        }
    }
    private fun initRangeRadioGroup() {
        binding.apply {
            val range = test.dailyInfo.testRange
            val testRange = Test.TestRange.ordinalOfNonNull(range)
            val radioBtn = when (testRange) {
                Test.TestRange.RECENT_RANGE -> R.id.recentStudiedButton
                Test.TestRange.ALL_RANGE -> R.id.satAllRangeButton
                Test.TestRange.SUBJECT_BY_GRADE -> R.id.myChoiceBtn
            }
            rangeRg.check(radioBtn)
            viewModel.selectedRangeIndex = testRange.ordinal
            rangeRg.setOnCheckedChangeListener { _, checkedId ->
                viewModel.selectedRangeIndex = getRangeIndex(checkedId)
                showSubView()
            }
        }
    }
    private fun getRangeIndex(id: Int): Int {
        return when (id) {
            R.id.recentStudiedButton -> 0
            R.id.satAllRangeButton -> 1
            R.id.myChoiceBtn -> 2
            else -> -1
        }

    }
    private fun showSubView() {
        binding.apply {
            viewModel.isSelectedRecentStudiedRg.postValue(rangeRg.checkedRadioButtonId == R.id.recentStudiedButton)

            when(rangeRg.checkedRadioButtonId) {
                R.id.recentStudiedButton -> {
                    viewModel.updateTestRangeType(TestRangeType.RecentStudied)
                }
                R.id.satAllRangeButton -> { viewModel.updateTestRangeType(TestRangeType.SAT) }
                R.id.myChoiceBtn -> { viewModel.updateTestRangeType(TestRangeType.MyChoice) }
            }
        }
    }

    enum class TestRangeType {
        RecentStudied,
        SAT,
        MyChoice
    }

    companion object {
        val DAILY_TEST_EXTRA = "DAILY_TEST_EXTRA"
        @JvmStatic
        fun newInstance(viewModel: RecommendSettingViewModel) =
            SnackTestSelectExamRangeFragment().apply {
                this.test = viewModel.test!!
                this.viewModel = viewModel
                arguments = Bundle().apply {

                }
            }
    }
}