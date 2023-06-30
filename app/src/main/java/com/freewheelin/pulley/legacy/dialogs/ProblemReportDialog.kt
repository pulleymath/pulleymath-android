package com.freewheelin.pulley.legacy.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.hideKeyboard
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API_V2
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.databinding.DialogProblemReportBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.utils.responseError
import com.freewheelin.pulley.legacy.utils.responseFailed
import com.freewheelin.pulley.legacy.utils.show
import com.freewheelin.pulley.legacy.utils.toKoreanKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class ProblemReportType {
    PROBLEM,
    ANSWER,
    SOLUTION,
    PICTURE;

    enum class ProblemReportDetail {
        PROBLEM_TYPING_ERROR,
        PROBLEM_DIFFERENT_CHAPTER,
        PROBLEM_DUPLICATE,

        ANSWER_TYPING_ERROR,
        ANSWER_NOT_EXIST,
        ANSWER_CANNOT_INPUT,

        SOLUTION_INCORRECT,
        SOLUTION_TYPING_ERROR,

        PICTURE_CANNOT_SEE,
        PICTURE_TYPING_ERROR;

        val text: String
            get() {
                return when(this) {
                    PROBLEM_TYPING_ERROR -> "오타가 있어요."
                    PROBLEM_DIFFERENT_CHAPTER -> "다른 과목/단원 문제인 것 같아요."
                    PROBLEM_DUPLICATE -> "같은 학습 내에 중복문제가 있어요."

                    ANSWER_TYPING_ERROR -> "정답이 잘못 표기된 것 같아요."
                    ANSWER_NOT_EXIST -> "(객관식) 선택지에 정답이 없어요."
                    ANSWER_CANNOT_INPUT -> "정답이 입력할 수 없는 값으로 나와요."

                    SOLUTION_INCORRECT -> "해설이 이상해요."
                    SOLUTION_TYPING_ERROR -> "오타가 있어요."

                    PICTURE_CANNOT_SEE -> "그림(도표)가 잘 안 보여요."
                    PICTURE_TYPING_ERROR -> "그림(도표) 내용에 오타가 있어요."
                }
            }

        val placeHolder: CharSequence
            get() {
                return when(this) {
                    PROBLEM_TYPING_ERROR -> "삼각형이 산각형으로 적혀있어요."
                    PROBLEM_DIFFERENT_CHAPTER -> "미적분인데 확통 문제로 나와요."
                    PROBLEM_DUPLICATE -> "지금 푸는 유형학습의 5번, 9번 문제가 동일해요."

                    ANSWER_TYPING_ERROR -> "2번이 정답인데 5번이라고 표기되었어요."
                    ANSWER_NOT_EXIST -> "정답이 10인데, 선택지에 없어요."
                    ANSWER_CANNOT_INPUT -> "정답이 분수/소수/음수로 나와요."

                    SOLUTION_INCORRECT -> "2x인데 4x라고 적혀있어요."
                    SOLUTION_TYPING_ERROR -> "삼각형이 산각형으로 적혀있어요."

                    PICTURE_CANNOT_SEE -> "그래프에 표시된 좌표가 잘 안 보여요."
                    PICTURE_TYPING_ERROR -> "f(x)=y인데 f(x)=x로 적혀있어요."
                }
            }
    }

    val details: List<ProblemReportDetail>
        get() {
            return when(this) {
                PROBLEM -> listOf(ProblemReportDetail.PROBLEM_TYPING_ERROR, ProblemReportDetail.PROBLEM_DIFFERENT_CHAPTER, ProblemReportDetail.PROBLEM_DUPLICATE)
                ANSWER -> listOf(ProblemReportDetail.ANSWER_TYPING_ERROR, ProblemReportDetail.ANSWER_NOT_EXIST, ProblemReportDetail.ANSWER_CANNOT_INPUT)
                SOLUTION -> listOf(ProblemReportDetail.SOLUTION_INCORRECT, ProblemReportDetail.SOLUTION_TYPING_ERROR)
                PICTURE -> listOf(ProblemReportDetail.PICTURE_CANNOT_SEE, ProblemReportDetail.PICTURE_TYPING_ERROR)
            }
        }
}


interface ProblemReportDialogListener {
    fun onReportCompleted(problem: Problem)
}

class ProblemReportDialog(context: Context, val problem: Problem) : Dialog(context), View.OnFocusChangeListener {
    val binding: DialogProblemReportBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_problem_report, null, false)
    }
    var reportType: ProblemReportType = ProblemReportType.PROBLEM
        set(value) {
            field = value
            configureDetail(value)
        }
    var reportDetail: ProblemReportType.ProblemReportDetail = ProblemReportType.ProblemReportDetail.PROBLEM_TYPING_ERROR
        set(value) {
            field = value
            configureUIByDetail(value)
        }
    val detailSeletor
        get() = listOf(binding.detail1, binding.detail2, binding.detail3)

    var listener: ProblemReportDialogListener? = null


    init {
        setContentView(binding.root)
        initUI()
        reportType = ProblemReportType.PROBLEM
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if(hasFocus)
            view.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_purple_300)
        else {
            view.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_500)
        }
    }

    private fun configureDetail(type: ProblemReportType) {
        detailSeletor.forEach { it.visibility = View.INVISIBLE }
        val list = detailSeletor.toMutableList()
        var index = 0
        type.details.forEach {
            val selector = list.removeAt(0)
            selector.text = it.text
            Handler().postDelayed({
                selector.show(150)
            }, index.toLong() * 100)
            index++
        }
        binding.detail1.isChecked = true
        reportDetail = reportType.details[0]
    }

    private fun configureUIByDetail(type: ProblemReportType.ProblemReportDetail) {
        binding.describeEt.hint = type.placeHolder
        binding.describeEt.text = null
    }
    private fun initUI() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setCanceledOnTouchOutside(false)

        with(binding) {
            describeEt.onFocusChangeListener = this@ProblemReportDialog
            describeEt.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    errorContainerLl.visibility = View.GONE
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            })
            describeEt.toKoreanKeyboard()
            xBtn.setOnClickListener {
                dismiss()
            }
            submitBtn.setOnClickListener {
                onSubmitBtnClicked()
            }

            reportTypeRg.setOnCheckedChangeListener { radioGroup, id ->
                when(id) {
                    R.id.problemRadio -> reportType = ProblemReportType.PROBLEM
                    R.id.answerRadio -> reportType = ProblemReportType.ANSWER
                    R.id.solutionRadio -> reportType = ProblemReportType.SOLUTION
                    R.id.paintingRadio -> reportType = ProblemReportType.PICTURE
                }
            }

            detailRg.setOnCheckedChangeListener { radioGroup, id ->
                val index = when(id) {
                    R.id.detail1 -> 0
                    R.id.detail2 -> 1
                    else -> 2
                }

                reportDetail = reportType.details[index]
            }

            rootView.setOnTouchListener { view, motionEvent ->
                currentFocus?.let { context.hideKeyboard(it) }

                false
            }
            scrollView.setOnTouchListener { view, motionEvent ->
                currentFocus?.let { context.hideKeyboard(it) }

                false
            }
        }
    }

    private fun onSubmitBtnClicked() {
        val detailText = binding.describeEt.text.toString()
        if (detailText.isEmpty()) {
            binding.errorContainerLl.visibility = View.VISIBLE
            binding.describeEt.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_red_300)
            return
        }

        val param = Parameter(
                "studentID" to user!!.studentID,
                "problemID" to problem.id,
                "typeTag" to reportDetail,
                "reportType" to reportType,
                "contents" to detailText
        )
        API_V2.reportProblem(param as Parameter).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    dismiss()
                    listener?.onReportCompleted(problem)
                    val dialog = CompleteDialog(context, "접수되었습니다. :)", "신고된 문제는 가려집니다.")
                    dialog.showFor()
                } else {
                    responseError(context, response)
                }
            }
        })
    }
}