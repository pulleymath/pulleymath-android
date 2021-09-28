package com.freewheelin.pulley.activities.solve

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemErrorStatus
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Content
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import kotlinx.android.synthetic.main.item_problem_gallery.view.numberTv
import kotlinx.android.synthetic.main.item_problem_gallery.view.resultIv
import kotlinx.android.synthetic.main.item_speedy_scoring.view.*
import kotlinx.android.synthetic.main.item_speedy_scoring.view.selectionAnswerView
import kotlinx.android.synthetic.main.item_speedy_scoring.view.shortAnswerView
import kotlinx.android.synthetic.main.view_answer.view.*
import kotlinx.android.synthetic.main.view_number_keypad.view.*
import kotlinx.android.synthetic.main.view_speed_answer.view.*
import kotlinx.android.synthetic.main.view_speed_answer.view.answeredCntTv
import kotlinx.android.synthetic.main.view_speed_answer.view.markingBtn
import kotlinx.android.synthetic.main.view_speed_answer.view.submitBtn

interface SpeedAnswerDelegate {
    val isShowAnswer: Boolean
    fun onSpeedNumberClicked(problem: Problem)
}

class SpeedAnswerView: ConstraintLayout {

    var delegate: SpeedAnswerDelegate? = null
    set(value) {
        field = value
        recyclerView.adapter = SpeedAnswerAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
    var content: Content? = null
    var answerDelegate: AnswerDelegate? = null
    var isFilter: Boolean = false
        set(value) {
            field = value
            recyclerView.adapter?.notifyDataSetChanged()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_speed_answer, this)
        setBackgroundColor(ContextCompat.getColor(context, R.color.grey_f2f2f2))
    }

    fun set(content: Content) {
        this.content = content
    }

    fun updateAll() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    inner class  SpeedAnswerAdapter: RecyclerView.Adapter<SpeedAnswerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedAnswerHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_speedy_scoring, null, false)
            return SpeedAnswerHolder(view)
        }

        override fun getItemCount(): Int {
            return getProblems().size
        }

        override fun onBindViewHolder(holder: SpeedAnswerHolder, position: Int) {
            val problem = getProblems()[position]
            holder.delegate = answerDelegate
            holder.set(problem, delegate!!.isShowAnswer)
            holder.speedAnswerDelegate = delegate
        }
    }

    fun getProblems(): List<Problem> {
        if(isFilter) {
            return content?.problems?.filter { it.getResultByScoring() == Result.incorrect } ?: emptyList()
        } else {
            return content?.problems ?: emptyList()
        }
    }
    fun update(problem: Problem) {
        val index = getProblems().indexOf(problem)
        recyclerView.adapter?.notifyItemChanged(index, "ANSWER")
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        if(visibility == View.VISIBLE) {
            Handler(Looper.getMainLooper()).postDelayed({
                recyclerView.showIfNeed(300)
            }, 500)
        } else {
            recyclerView.visibility = View.INVISIBLE
        }
    }

    fun disableMarking() {
        answeredCntTv.visibility = View.GONE
        markingBtn.isEnabled = false
        submitBtn.isEnabled = false
        submitBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_e0e0e0_round_20)
        markingBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_e0e0e0_round_20)
    }

    fun enableMarking(cnt: Int) {
        answeredCntTv.text = cnt.toString()
        answeredCntTv.visibility = View.VISIBLE
        markingBtn.isEnabled = true
        markingBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_6d6dff_round_20)
        submitBtn.isEnabled = true
        submitBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_6d6dff_round_20)
    }

    fun showMarkingBtn() {
        markingBtn.visibility = View.VISIBLE
        submitBtn.visibility = View.INVISIBLE
    }

    fun showSubmitBtn() {
        markingBtn.visibility = View.INVISIBLE
        submitBtn.visibility = View.VISIBLE
    }

    fun scrollTo(problem: Problem?) {
        val index = getProblems().indexOf(problem)

        if(index >= 0)
            recyclerView.scrollToPosition(index)
//            recyclerView.smoothScrollToPosition(index)
    }

    fun changedFocus(focusIndex: Int, prevIndex: Int) {
        val prevHolder = recyclerView.findViewHolderForAdapterPosition(prevIndex) as? SpeedAnswerHolder
        prevHolder?.keypadWindow?.dismiss()

        val itemView = recyclerView.findViewHolderForAdapterPosition(focusIndex)?.itemView ?: return
        if (itemView.shortAnswerView.visibility == View.VISIBLE) {
            itemView.shortAnswerView.requestFocus()
        } else {
            itemView.run {
                performClick()
                requestFocus()
                clearFocus()
            }
        }
    }

}
class SpeedAnswerHolder(val view: View): RecyclerView.ViewHolder(view), PlusMinusKeypadListener, AnswerSelectionListener {
    lateinit var problem: Problem
    var delegate: AnswerDelegate? = null
    var speedAnswerDelegate: SpeedAnswerDelegate? = null
    var isShowAnswer: Boolean = false
    val resultIv = view.resultIv
    val numberTv = view.numberTv
    val infoContainer = view.infoContainer
    val selectionAnswerView = view.selectionAnswerView
    val shortAnswerView = view.shortAnswerView


    val answerTv = view.answerTv
    val infoTv = view.infoTv
    val errorTv = view.errorTv
    val context: Context
        get() = view.context

    val keypadWindow by lazy {
        PlusMinusKeypadWindow(context, object : PlusMinusKeypadWindowListener {
            override var keypadListener: PlusMinusKeypadListener = this@SpeedAnswerHolder
            override fun onKeyboardDismiss() {
                shortAnswerView.clearFocus()
            }
        })
    }

    var ignoreFocus = false

    init {
        numberTv.setOnClickListener {
            speedAnswerDelegate?.onSpeedNumberClicked(problem)
        }
        itemView.setOnClickListener {
            speedAnswerDelegate?.onSpeedNumberClicked(problem)
        }

        shortAnswerView.run {
            setHintTextColor(ContextCompat.getColor(view.context, R.color.grey_c0c0c0))
            setTextSize(
                    view.context.resources.getDimension(R.dimen.sp24),
                    view.context.resources.getDimension(R.dimen.sp14)
            )
            setTextIsSelectable(true)
            showSoftInputOnFocus = false
        }

        shortAnswerView.setOnFocusChangeListener { view, isFocused ->
            if(isFocused && ignoreFocus.not()) {
                speedAnswerDelegate?.onSpeedNumberClicked(problem)
                if (keypadWindow.isShowing.not()) {
                    val height = minOf(
                            ((DisplayUtils.getScrenHeight(context) - context.resources.getDimension(R.dimen.dp64)) * 0.5f).toInt() - 32.toPx(),
                            context.resources.getDimensionPixelSize(R.dimen.omrActivity_keypad_height)
                    )

                    val width = height * 232 / 296
                    var x = this.view.getTargetAbsolutePosition().first.toInt()
                    val y: Int = DisplayUtils.getScrenHeight(context) - height - 16.toPx()
                    x = x - 32.toPx() - width

                    keypadWindow.showAtLocation(view.rootView, Gravity.NO_GRAVITY, x, y)
                    keypadWindow.contentView.layoutParams.width = width
                    (keypadWindow.contentView.layoutParams as ViewGroup.MarginLayoutParams).setMargins(16.toPx(), 16.toPx(), 16.toPx(), 16.toPx())
                    keypadWindow.contentView.layoutParams.height = height
                }
            }
            if (ignoreFocus) ignoreFocus = false
        }

        shortAnswerView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            // holder 세팅시에 호출됨 일단 호출되는 메서드 막기
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (problem.getResultByScoring() == Result.yet) {
                    if (p0?.isEmpty() == true && problem.userAnswer?.isEmpty() == true) return
                    if (p0?.toString() == problem.userAnswer) return
                    if (p0?.toString() == "-") return
//                    delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString(), problem)
                    ignoreFocus = true
//                    shortAnswerView.requestFocus()
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        selectionAnswerView.listener = this
    }

    fun set(problem: Problem, isShowAnswer: Boolean) {
        this.problem = problem
        this.isShowAnswer = isShowAnswer
        resultIv.setImageDrawable(getResultDrawable())
        // 8인치 체크해서
        // 4글자 이상인데 + 가 있으면 +를 기준으로 개행
        if(context.is10InchUI) {
            numberTv.text = problem.getNumberText()
        } else {
            var text = problem.getNumberText()
            if(text.length >= 4 && text.contains("+")) {
                text = text.replace("+", "\n+")
            }
            numberTv.text = text
        }


        setInfoUI()
        infoContainer.visibility = getInfoContainerVisibility()
    }

    fun getResultDrawable(): Drawable? {
        return when(problem.getResultByScoring()) {
            Result.correct -> ContextCompat.getDrawable(view.context, R.drawable.ic_correct_new)
            Result.incorrect -> ContextCompat.getDrawable(view.context, R.drawable.ic_incorrect_new)
            else -> null
        }
    }

    private fun disableRow() {
        shortAnswerView.visibility = View.INVISIBLE
        selectionAnswerView.visibility = View.INVISIBLE
        answerTv.visibility = View.GONE
        infoTv.visibility = View.GONE
        errorTv.visibility = View.VISIBLE
        numberTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
    }

    private fun setInfoUI() {
        when(problem.problemErrorStatus) {
            ProblemErrorStatus.REPORT -> {
                disableRow()
                errorTv.text = "신고 처리 중입니다."
            }
            ProblemErrorStatus.ERROR -> {
                disableRow()
                errorTv.text = "삭제된 문제입니다."
            }
            else -> {
                if(problem.problemType == ProblemType.short) {
                    setShortAnswerUI()
                } else {
                    setSelectionAnswerUI()
                }

                errorTv.visibility = View.GONE
                answerTv.visibility = View.VISIBLE
                numberTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                answerTv.text = "정답 : ${getAnswerStr(problem.answerData)}"
                if(problem.getResultByScoring() == Result.incorrect) {
                    infoTv.text = if(problem.userAnswer != null) "(내 입력 : ${getAnswerStr(problem.userAnswer!!)})"
                        else "(정답 미입력)"
                    infoTv.visibility = View.VISIBLE
                } else {
                    infoTv.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun getAnswerStr(answer: String): String {
        return if(problem.problemType == ProblemType.single)
            when(answer) {
                "1" -> "①"
                "2" -> "②"
                "3" -> "③"
                "4" -> "④"
                else -> "⑤"
            }
        else
            return answer
    }

    fun getInfoContainerVisibility(): Int {
        return if(isShowAnswer && problem.getResultByScoring() != Result.yet
                || problem.problemErrorStatus == ProblemErrorStatus.ERROR
                || problem.problemErrorStatus == ProblemErrorStatus.REPORT) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    fun setSelectionAnswerUI() {
        shortAnswerView.visibility = View.INVISIBLE
        if(getInfoContainerVisibility() == View.VISIBLE) {
            selectionAnswerView.visibility = View.INVISIBLE
        } else {
            selectionAnswerView.visibility = View.VISIBLE
        }

        selectionAnswerView.setAnswerByRawString(problem.userAnswer)
        selectionAnswerView.setAnswerType(problem.problemType)
        shortAnswerView.clearFocus()
    }

    fun setShortAnswerUI() {
        selectionAnswerView.visibility = View.INVISIBLE
        if(getInfoContainerVisibility() == View.VISIBLE) {
            shortAnswerView.visibility = View.INVISIBLE
        } else {
            shortAnswerView.visibility = View.VISIBLE
        }

        shortAnswerView.isEnabled = true
        if (problem.userAnswer != null)
            shortAnswerView.setText(problem.userAnswer.toString())
        else
            shortAnswerView.text = null
    }

    fun enterNumberBtnClicked(text:String) {
        if(shortAnswerView.length() == shortAnswerView.selectionStart)
            shortAnswerView.append(text)
        else
            shortAnswerView.text.insert(shortAnswerView.selectionStart, text)

    }

    override fun onNumberBtnClicked(button: Button, text: String) {
        enterNumberBtnClicked(text)
    }

    fun deleteBtnClicked() {
        Log.d("키보드", "speed selectionStart=${shortAnswerView.selectionStart}")
        if(shortAnswerView.selectionStart != 0)
            shortAnswerView.text.delete(
                    shortAnswerView.selectionStart - 1,
                    shortAnswerView.selectionStart
            )

    }

//    fun deleteBtnClicked() {
//        if(shortAnswerView.selectionStart != 0)
//            shortAnswerView.text.delete(
//                    shortAnswerView.selectionStart - 1,
//                    shortAnswerView.selectionStart
//            )
//        delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString())
//    }

    override fun onDeleteBtnClicked(button: ImageButton) {
        deleteBtnClicked()
    }

    fun enterMinusBtnClicked() {
        if(shortAnswerView.text.firstOrNull() == '-') {
            shortAnswerView.text.delete(0, 1)
        } else {
            val length = shortAnswerView.text.length
            if(length == 9)
                shortAnswerView.text.delete(length - 1, length)
            shortAnswerView.text.insert(0, "-")
        }
    }

    override fun onPlusMinusBtnClicked(button: ImageButton) {
        enterMinusBtnClicked()
    }

    override fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?) {
        delegate?.onAnswerChanged(itemView, answerStr, problem)
    }
}