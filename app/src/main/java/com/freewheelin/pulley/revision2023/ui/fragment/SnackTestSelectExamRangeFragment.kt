package com.freewheelin.pulley.revision2023.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.auth.signup.SignupActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentGuestJoinIntroduceBinding
import com.freewheelin.pulley.databinding.FragmentPurchaseGuide1Binding
import com.freewheelin.pulley.databinding.FragmentTestExamRangeBinding
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseGuideActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.JoinInduceForGuestDialog.GuestJoinStep
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog
import com.freewheelin.pulley.revision2023.ui.dialogs.SnackTestRecommendSettingDialog.*
import com.freewheelin.pulley.revision2023.viewmodel.GuestJoinViewModel
import com.freewheelin.pulley.revision2023.viewmodel.PurchaseGuideViewModel
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class SnackTestSelectExamRangeFragment() : Fragment() {
    private lateinit var binding: FragmentTestExamRangeBinding
    private lateinit var test: Test
    var viewModel: RecommendSettingViewModel? = null


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
            cancelBtn.setOnClickListener { viewModel?.exitBtn() }
            saveBtn.setOnClickListener {
                //TODO save api
            }
        }

        arguments?.let {

        }
    }

    private fun setModifyBtnClickListener() {
        binding.apply {
            commonSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등공통과목수정) } }
            optionalSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등선택과목수정) } }
            middleSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.중등과목수정) } }
            over50HighSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등공통과목수정) } }
            over50MiddleSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등공통과목수정) } }

            satOptionalSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등선택과목수정) } }
            myChoiceCommonSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등공통과목수정) } }
            myChoiceOptionalSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.고등선택과목수정) } }
            myChoiceMiddleSubjectModifyBtn.setOnClickListener { viewModel?.setStep?.let { it(ViewType.중등과목수정) } }

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
            val level = test.dailyInfo.testLevel
            val testLevel = Test.TestLevel.ordinalOfNonNull(level)
            val levelRadioBtn = when(testLevel) {
                Test.TestLevel.HIGH -> R.id.highButton
                Test.TestLevel.LIKE_ME -> R.id.middleButton
                Test.TestLevel.EASY -> R.id.lowButton
            }
            levelRg.check(levelRadioBtn)
        }
    }

    companion object {
        val DAILY_TEST_EXTRA = "DAILY_TEST_EXTRA"
        @JvmStatic
        fun newInstance(test: Test) =
            SnackTestSelectExamRangeFragment().apply {
                this.test = test
                arguments = Bundle().apply {

                }
            }
    }
}