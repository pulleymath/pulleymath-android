package com.freewheelin.pulley.revision2023.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogMockExamOptionalSubjectSelectBinding
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.dialogs.EmailInputDialog
import com.freewheelin.pulley.legacy.dialogs.EmailInputDialogListener
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.model.contents.MockExamSummary
import com.freewheelin.pulley.legacy.utils.DialogType
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.partialFontAndColored
import com.freewheelin.pulley.legacy.utils.partialFontAndColoredWithSize
import com.freewheelin.pulley.revision2023.viewmodel.MockExamDialogViewModel

class MockExamOptionalSubjectSelectDialog(): DialogFragment() {
    private val viewModel: MockExamDialogViewModel by viewModels()
    var goSolveCb: (MockExam) -> Unit = {}
    var goOMRCb: (MockExam) -> Unit = {}

    val subjectPAS = CommercialSubject.PROBABILITY_AND_STATISTICS// 확통
    val subjectCalculus = CommercialSubject.CALCULUS // 미적
    val subjectGeometry = CommercialSubject.GEOMETRY // 기하

    companion object {
        const val MOCK_ID = "MOCK_ID"
        const val ASSIGN_ID = "ASSIGN_ID"
        fun newInstance(mockId: Int, assignId: Int?): MockExamOptionalSubjectSelectDialog {
            val args = Bundle().apply {
                putInt(MOCK_ID, mockId)
                putInt(ASSIGN_ID, assignId ?: -999)
            }
            val instance = MockExamOptionalSubjectSelectDialog()
            instance.arguments = args
            return instance
        }
    }

    private val binding: DialogMockExamOptionalSubjectSelectBinding by lazy {
        DataBindingUtil.inflate(
            layoutInflater.cloneInContext(requireContext()),
            R.layout.dialog_mock_exam_optional_subject_select,
            null,
            false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {
            viewModel.mockId = getInt(MOCK_ID)
            viewModel.assignId = getInt(ASSIGN_ID)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.onExitClickCallback = {
                dismiss()
            }
            exitBtn.setOnClickListener {
                dismiss()
            }

            checkRestart.setOnCheckedChangeListener { buttonView, isChecked ->
                resolveGuideLabel.visibility = if(isChecked) View.VISIBLE else View.GONE

                Log.d("모의고사", "다시풀기=$isChecked")

                if(!isChecked) {
//                    resetSelectedOptions()
                    setSubjectsUI(viewModel.mockSummary.value)
                    setSelectSubjectUI(viewModel.mockSummary.value)
                }

                setDisableOptionalUI()
            }

            solveOnTabletBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "바로풀기모드")
                if(checkRestart.isChecked) { showConfirmDialog { openSolveActivity() } } else openSolveActivity()
            }
            radioMethod.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId) {
                    R.id.radioOnTablet -> setTabletModeUi()
                    R.id.radioOnPrint -> setPrintModeUi()
                }
            }

            selectRadioContainer.setOnCheckedChangeListener { group, checkedId ->
                viewModel.mockSummary.value?.let { mockSummary ->
                    val selectedOptions = mockSummary.optionalSubjectSummary.toMutableList()
                    when (checkedId) {
                        R.id.radioSelectOne -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == subjectPAS.name }
                        R.id.radioSelectTwo -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == subjectCalculus.name }
                        R.id.radioSelectThree -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == subjectGeometry.name }
                    }
                    mockSummary.optionalSubjectSummary = selectedOptions.toTypedArray()
                    setSubjectsUI(mockSummary)
                    viewModel.mockSummary.postValue(mockSummary)
                }
            }

            sendEmailBtn.setOnClickListener {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "모의고사", "메일")
                sendEmailPDF()
            }

            solveOnOMR.setOnClickListener {
                LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "모의고사", "OMR모드")
                if(checkRestart.isChecked) { showConfirmDialog { openOmrActivity() } } else openOmrActivity()
            }

            viewModel.fetchMockSummary {
                setSubjectsUI(it)
                setSelectSubjectUI(it)
                setDisableOptionalUI()
            }
        }
    }
    private fun sendEmailPDF() {
        val mockExam = setMockExamToSolve()
        user?.let {
            val dialog = EmailInputDialog(requireContext(), listOf(mockExam), it, object: EmailInputDialogListener{
                override fun onSentEmail() { /* TODO? */ }
            })
            dialog.show()
        }

    }

    private fun openSolveActivity() {
        val mockExam = setMockExamToSolve()
        goSolveCb(mockExam)
        dismiss()
    }
    private fun openOmrActivity() {
        val mockExam = setMockExamToSolve()
        goOMRCb(mockExam)
        dismiss()
    }

    private fun setMockExamToSolve(): MockExam {
        val optionResult = mutableListOf<CommercialSubject>()

        if(binding.selectCheckContainer.visibility == View.VISIBLE) {
            val checkBoxes = listOf(binding.checkSelectOne, binding.checkSelectTwo, binding.checkSelectThree)
            viewModel.mockSummary.value?.optionalSubjectSummary?.forEachIndexed { index, subjectSummary ->
                if (index > 2) return@forEachIndexed
                if (checkBoxes[index].isChecked) optionResult.add(CommercialSubject.valueOf(subjectSummary.subjectCodeType))
            }
        } else if(binding.selectRadioContainer.visibility == View.VISIBLE) {
            when(binding.selectRadioContainer.checkedRadioButtonId) {
                R.id.radioSelectOne -> optionResult.add(subjectPAS)
                R.id.radioSelectTwo -> optionResult.add(subjectCalculus)
                R.id.radioSelectThree -> optionResult.add(subjectGeometry)
            }
        }

        val mockExam = MockExam().also {
            it.mockID = viewModel.mockId!!
            it.assignID = viewModel.assignId
            it.isRestart = binding.checkRestart.isChecked
            it.selectOptional = optionResult
        }

        return mockExam
    }

    fun setSubjectsUI(summary: MockExamSummary?) {
        summary?.let { mockSummary ->
            binding.subjectContainer.removeAllViews()
            mockSummary.commonSubjectSummary.forEach { summary -> summary.isSelected = true }
            val combinedSubjects = (mockSummary.commonSubjectSummary + mockSummary.optionalSubjectSummary).toMutableList()
            combinedSubjects.firstOrNull { it.subjectCodeType == "NONE" }
                ?.let { none ->
                    combinedSubjects.remove(none)
                    combinedSubjects.add(combinedSubjects.size, none)
                }
            combinedSubjects.forEachIndexed { index, subject ->
                var space = " "
                if (subject.count < 10) space = "   "
                if(subject.isSelected) {
                    val textView = LayoutInflater.from(context).inflate(R.layout.item_mockexam_dialog_subject_textview, binding.subjectContainer, false) as TextView
                    textView.text = "${subject.title}${space}${subject.count}문항"
                    binding.subjectContainer.addView(textView)
                    if (index > 4) return@forEachIndexed
                }
            }
        }
    }
    fun setSelectSubjectUI(mockSummary: MockExamSummary?) {
        binding.apply {
            val checkSelectList = listOf(checkSelectOne, checkSelectTwo, checkSelectThree)
            mockSummary?.optionalSubjectSummary?.forEachIndexed { index, subject ->
                if (index > 2) return@forEachIndexed
                val subTitle = "  ${subject.count}문항"
                var text = "${subject.title}" + subTitle
                checkSelectList[index].text = text.partialFontAndColoredWithSize(Theme.bold(requireContext()), ContextCompat.getColor(requireContext(), R.color.gray_500), 0.9f, subject.title.length, text.length)
                checkSelectList[index].visibility = View.VISIBLE
                checkSelectList[index].isChecked = subject.isSelected

                if (subject.isSelected) {
                    when (subject.subjectCodeType) {
                        subjectPAS.name -> radioSelectOne.isChecked = true
                        subjectCalculus.name -> radioSelectTwo.isChecked = true
                        subjectGeometry.name -> radioSelectThree.isChecked = true
                    }
                }
            }
        }

    }

    fun setDisableOptionalUI() {
        val checkBoxes = listOf(binding.checkSelectOne, binding.checkSelectTwo, binding.checkSelectThree)
        val radioSelects = listOf(binding.radioSelectOne, binding.radioSelectTwo, binding.radioSelectThree)
        val isRestart = binding.checkRestart.isChecked
        if(viewModel.mockSummary.value?.isIng == true) {
            checkBoxes.forEach { it.isEnabled = isRestart }
            radioSelects.forEach { it.isEnabled = isRestart }
        }
        setRemainButton(isRestart)
    }
    fun setRemainButton(remain:Boolean) {
        viewModel.mockSummary.value?.let { mockSummary ->
            val selectedOptions = mockSummary.optionalSubjectSummary.toMutableList()
            if(mockSummary.isIng && !remain) {
                val remainCount = mockSummary.let{ it.getTotalNumber(selectedOptions) - it.markedNumber}
                val text = "$remainCount 문항 "
                binding.solveOnTabletBtn.text = "$text 이어 풀기".partialFontAndColored(Theme.extraBold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_200), 0, text.length)
            } else {
                val text = "총 ${mockSummary.getTotalNumber(selectedOptions)}문항 "
                binding.solveOnTabletBtn.text = "$text 풀기".partialFontAndColored(Theme.extraBold(requireContext()), ContextCompat.getColor(requireContext(), R.color.purple_200), 0, text.length)
            }
        }

    }

    fun showConfirmDialog(cb: (() -> Unit)) {
        DialogUtils.DaebakDialog(requireContext()).apply {
            title = "기존 풀이내역은 사라집니다"
            contents = "다시 풀기 시\n기존 풀이내역은 복구할 수 없습니다.\n정말 새로 푸실 건가요?"
            type = DialogType.alert
            binding.leftBtn.text = "취소"
            binding.rightBtn.text = "새로 풀게요"
            binding.rightBtn.setOnClickListener {
                cb()
                dismiss()
            }
        }.show()
    }

    fun setTabletModeUi() {
        with(binding) {
            sendEmailBtn.visibility = View.GONE
            solveOnOMR.visibility = View.GONE
            solveOnTabletBtn.visibility = View.VISIBLE

            printButtonsContainer.visibility = View.GONE
        }
    }

    fun setPrintModeUi() {
        with(binding) {
            solveOnTabletBtn.visibility = View.GONE

            printButtonsContainer.visibility = View.VISIBLE
            sendEmailBtn.visibility = View.VISIBLE
            // 핸드폰 ui에서는 지원안함
            if(requireContext().isTablet)
                solveOnOMR.visibility = View.VISIBLE
            else
                solveOnOMR.visibility = View.GONE
        }

    }

}