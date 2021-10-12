package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.model.contents.MockExamSummary
import com.freewheelin.pulley.model.contents.SubjectSummary
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import kotlinx.android.synthetic.main.dialog_daebak.*
import kotlinx.android.synthetic.main.dialog_mock_exam_guide.*


interface MockExamGuideDialogListener {
    fun onSolveWithPrint(mockExam: MockExam, makeNew: Boolean)
    fun onSolveWithoutPrint(mockExam: MockExam, makeNew: Boolean)
}

class MockExamGuideDialog(
        context: Context,
        val mockExam: MockExam,
        val isSolved: Boolean,
        val listener: MockExamGuideDialogListener
): Dialog(context), EmailInputDialogListener {

    var mockSummary:MockExamSummary? = null

    val option1 = CommercialSubject.PROBABILITY_AND_STATISTICS// 확통
    val option2 = CommercialSubject.CALCULUS // 미적
    val option3 = CommercialSubject.GEOMETRY // 기하

    val checkBoxes : Array<CheckBox> by lazy { arrayOf(checkSelectOne, checkSelectTwo, checkSelectThree) }

    val selectedOptions:MutableList<SubjectSummary> = mutableListOf()

    init {
        super.setContentView(R.layout.dialog_mock_exam_guide)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        progress(true)
        MockExamManager.getMockSummary(context, mockExam.mockID, user!!) {
            progress(false)
            mockSummary = it
            mockSummary?.commonSubjectSummary?.forEach { it.isSelected = true }
            resetSelectedOptions()
            setUi()
        }
    }

    private fun resetSelectedOptions() {
        selectedOptions.clear()
        for(option in mockSummary?.optionalSubjectSummary?: arrayOf()) {
            selectedOptions.add(option.copy())
        }
    }

    fun setUi() {
        setTitleUi()
        setSubjectsUi()
        setResolveUi()
        setSelectSubjectUi()
        setDisableOptionalUi()
        checkScrollIsShown()
        setListener()
    }

    fun setListener() {
        val dp24 = context.resources.getDimension(R.dimen.dp24)
        xBtn.extensionTouchArea(dp24.toInt())
        xBtn.setOnClickListener {
            dismiss()
        }

        checkRestart.setOnCheckedChangeListener { buttonView, isChecked ->
            resolveGuideLabel.visibility = if(isChecked) View.VISIBLE else View.GONE

            Log.d("모의고사", "다시풀기=$isChecked")

            if(!isChecked) {
                resetSelectedOptions()
                setSubjectsUi()
                setSelectSubjectUi()
            }

            setDisableOptionalUi()
        }

        solveOnTabletBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "바로풀기모드")
            if(isRestart()) { showConfirmDialog { openSolveActivity() } } else openSolveActivity()
        }

        sendEmailBtn.setOnClickListener {
            LogUtils.logEvent(context!!, user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "메일")
            sendEmailPDF()
        }

        solveOnOMR.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "OMR모드")
            if(isRestart()) { showConfirmDialog { openOMRActivity() } } else openOMRActivity()
        }

        radioMethod.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioOnTablet -> setTabletModeUi()
                R.id.radioOnPrint -> setPrintModeUi()
            }
        }

        if(mockSummary?.year?:0 > 2020) {
            selectRadioContainer.setOnCheckedChangeListener { radioGroup, checkedId ->
                mockSummary?.let { mock ->
                    when (checkedId) {
                        R.id.radioSelectOne -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == option1.name }
                        R.id.radioSelectTwo -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == option2.name }
                        R.id.radioSelectThree -> selectedOptions.forEach { it.isSelected = it.subjectCodeType == option3.name }
                    }
                }
                setSubjectsUi()
                setRemainButton(isRestart())
            }
        } else {
            checkSelectOne.setOnCheckedChangeListener(optionalCheckedChangedListener)
            checkSelectTwo.setOnCheckedChangeListener(optionalCheckedChangedListener)
            checkSelectThree.setOnCheckedChangeListener(optionalCheckedChangedListener)
        }
    }

    val optionalCheckedChangedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        mockSummary?.let { mock ->
            when (buttonView.id) {
                R.id.checkSelectOne -> selectedOptions.firstOrNull()?.isSelected = isChecked
                R.id.checkSelectTwo -> selectedOptions.getOrNull(1)?.isSelected = isChecked
                R.id.checkSelectThree -> selectedOptions.getOrNull(2)?.isSelected = isChecked
            }
        }
        setSubjectsUi()
        setRemainButton(isRestart())
    }

    var isScrollShown = false
    fun checkScrollIsShown() {
        val observer = scrollView.viewTreeObserver
        observer.addOnGlobalLayoutListener {
            val viewHeight = scrollView.measuredHeight
            val child = scrollView.getChildAt(0)
            val contentHeight = child.height
            if(viewHeight - contentHeight < 0) {
                isScrollShown = true
            }
            bottomContainerShadow.visibility = if(isScrollShown) View.VISIBLE else View.INVISIBLE
        }

        scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            val child = scrollView.getChildAt(0)
            val diff = child.bottom - (scrollView.height + scrollY)
            Log.d("다이얼로그","scroll=$isScrollShown,child bottom=${child.bottom}, scroll height=${scrollView.height}, scrollY=${scrollY}, diff=$diff")
            if(isScrollShown) {
                Handler(Looper.getMainLooper()).post {
                    if (diff == 0)
                        bottomContainerShadow.visibility = View.INVISIBLE
                    else
                        bottomContainerShadow.visibility = View.VISIBLE

                    // Toast.makeText(context,"다이얼로그 diff=$diff, visibility=${bottomContainerShadow.visibility}",Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun setTitleUi() {
        textMonth.text = "${mockSummary?.year}년 ${mockSummary?.month}월"
        val gua = "${mockSummary?.type?.getStr()}"
        textTitle.text = "고${mockSummary?.grade} ${mockSummary?.examType} [$gua]"
        updateTag.visibility = if(mockSummary?.updated == true) View.VISIBLE else View.GONE
    }

    fun setSubjectsUi() {
        mockSummary?.let { mock ->
            subjectContainer.removeAllViews()
            val combinedSubjects = (mock.commonSubjectSummary + selectedOptions).toMutableList()
            // 교육과정 외
            combinedSubjects.firstOrNull { it.subjectCodeType == "NONE"}?.let { none->
                combinedSubjects.remove(none)
                combinedSubjects.add(combinedSubjects.size, none)
            }

            if(combinedSubjects.isNotEmpty()) {
                var cnt = 0
                for (subject in combinedSubjects) {
                    cnt++
                    var space = " "
                    if (subject.count < 10) space = "   "
                    if(!subject.isSelected) continue // 선택된 것만 반영

                    val text = "${subject.title}${space}${subject.count}문항"
                    val textView = LayoutInflater.from(context).inflate(R.layout.item_mockexam_dialog_subject_textview, subjectContainer, false) as TextView
                    textView.text = text
                    subjectContainer.addView(textView)

                    if (cnt > 5) break
                }
            }
        }
    }

    fun setResolveUi() {
        if(mockSummary!!.isIng) {
            reSolveContainer.visibility = View.VISIBLE
        } else {
            reSolveContainer.visibility = View.GONE
        }
    }

    fun setSelectSubjectUi() {
        // 선택과목이 있는 상태에서
        mockSummary?.let {

            Log.d("모의고사", "selectedOptions=${selectedOptions.size}")

            if(selectedOptions.isNotEmpty()) {
                selectSubjectContainer.visibility = View.VISIBLE

                if(it.year <= 2020) { // 체크박스 : 시험 출제기준이 2020년 까지
                    selectCheckContainer.visibility = View.VISIBLE
                    selectRadioContainer.visibility = View.GONE
                } else {
                    selectCheckContainer.visibility = View.GONE
                    selectRadioContainer.visibility = View.VISIBLE
                }

                for((idx, subject) in selectedOptions.withIndex()) {
//                    val text = "${subject.title} ${subject.count}문항"
                    if(idx >= 3) break
                    val subTitle = "  ${subject.count}문항"
                    var text = "${subject.title}" + subTitle

                    checkBoxes[idx].text = text.partialFontAndColoredWithSize(Theme.bold(context), ContextCompat.getColor(context, R.color.grey_c0c0c0), 0.9f, subject.title.length, text.length)
                    checkBoxes[idx].visibility = View.VISIBLE
                    if(subject.isSelected) {
                        // 체크박스 체크 처리
                        checkBoxes[idx].isChecked = true
                        // 라디오 버튼 선택처리 - 라디오일 경우는 셋 중에 하나만 선택됨
                        when(subject.subjectCodeType) {
                            option1.name -> radioSelectOne.isChecked = true
                            option2.name -> radioSelectTwo.isChecked = true
                            option3.name -> radioSelectThree.isChecked = true
                        }
                    }else{
                        checkBoxes[idx].isChecked = false
                    }
                }
            } else {
                selectSubjectContainer.visibility = View.GONE
                selectCheckContainer.visibility = View.GONE
                selectRadioContainer.visibility = View.GONE
            }
        }
    }

    fun setTabletModeUi() {
//        sendEmailBtn.visibility = View.GONE
//        solveOnOMR.visibility = View.GONE
//        solveOnTabletBtn.visibility = View.VISIBLE

        printButtonsContainer.visibility = View.GONE
        solveOnTabletBtn.visibility = View.VISIBLE
    }

    fun setPrintModeUi() {

        printButtonsContainer.visibility = View.VISIBLE
        solveOnTabletBtn.visibility = View.GONE


        // 핸드폰 ui에서는 지원안함
        if(context.isTablet)
            solveOnOMR.visibility = View.VISIBLE
        else
            solveOnOMR.visibility = View.GONE
    }

    fun setDisableOptionalUi() {
        if(mockSummary?.isIng == true) {
            for (checkBox in checkBoxes) {
                checkBox.isEnabled = isRestart()
            }
            radioSelectOne.isEnabled = isRestart()
            radioSelectTwo.isEnabled = isRestart()
            radioSelectThree.isEnabled = isRestart()
        }
        setRemainButton(isRestart())
    }

    fun setRemainButton(remain:Boolean) {
        if(mockSummary?.isIng == true && !remain) {
            val remainCount = mockSummary?.let{it.getTotalNumber(selectedOptions) - it.markedNumber}
            val text = "$remainCount 문항 "
            solveOnTabletBtn.text = "$text 이어 풀기".partialFontAndColored(Theme.extraBold(context), ContextCompat.getColor(context, R.color.purple_ACACFF), 0, text.length)
        } else {
            val text = "총 ${mockSummary?.getTotalNumber(selectedOptions)}문항 "
            solveOnTabletBtn.text = "$text 풀기".partialFontAndColored(Theme.extraBold(context), ContextCompat.getColor(context, R.color.purple_ACACFF), 0, text.length)
        }
    }

    fun showConfirmDialog(cb: (() -> Unit)) {
        DialogUtils.DaebakDialog(context!!).apply {
            title = "기존 풀이내역은 사라집니다"
            contents = "다시 풀기 시\n기존 풀이내역은 복구할 수 없습니다.\n정말 새로 푸실 건가요?"
            type = DialogType.alert
            leftBtn.text = "취소"
            rightBtn.text = "새로 풀게요"
            rightBtn.setOnClickListener {
                cb()
                dismiss()
            }
        }.show()
    }

    fun isRestart() = checkRestart.isChecked

    private fun sendEmailPDF(){
        setOptionToMockExam()
        val dialog = EmailInputDialog(context!!, listOf(mockExam), user!!, this)
        dialog.show()
    }

    private fun openOMRActivity() {
        dismiss()
        setOptionToMockExam()
        listener.onSolveWithPrint(mockExam, isRestart())
    }

    private fun openSolveActivity() {
        dismiss()
        setOptionToMockExam()
        listener.onSolveWithoutPrint(mockExam, isRestart())
    }

    fun setOptionToMockExam() {

        val optionResult = mutableListOf<CommercialSubject>()

        mockExam.isRestart = isRestart()

        if(selectCheckContainer.visibility == View.VISIBLE) {
            for((idx, subject) in selectedOptions.withIndex()) {
                if(idx >= 3) break
                if(checkBoxes[idx].isChecked) optionResult.add(CommercialSubject.valueOf(subject.subjectCodeType))
            }
        } else if(selectRadioContainer.visibility == View.VISIBLE) {
            when(selectRadioContainer.checkedRadioButtonId) {
                R.id.radioSelectOne -> optionResult.add(option1)
                R.id.radioSelectTwo -> optionResult.add(option2)
                R.id.radioSelectThree -> optionResult.add(option3)
            }
        }
        mockExam.selectOptional = optionResult
    }

    private fun progress(run: Boolean) {
        progressContainer.visibility = if(run) View.VISIBLE else View.GONE
    }

    override fun onSentEmail() {
        LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "모의고사", "메일보내기")
        DaebakToast.show(context, "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.", overDialog = true)
        val intent = Intent(MockExamManager.EVENT_MOCK_EXAM_CLEAR)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}