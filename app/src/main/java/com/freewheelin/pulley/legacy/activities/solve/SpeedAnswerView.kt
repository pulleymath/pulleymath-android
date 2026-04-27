package com.freewheelin.pulley.legacy.activities.solve

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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.is10InchUI
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.databinding.ItemSpeedyScoringBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.ProblemErrorStatus
import com.freewheelin.pulley.legacy.model.ProblemType
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.*

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

    var recyclerView: RecyclerView
    var answeredCntTv: TextView
    var markingBtn: ConstraintLayout
    var submitBtn: Button
    var emptyFilterContainer: ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_speed_answer, this)
        recyclerView = findViewById(R.id.recyclerView)
        answeredCntTv = findViewById(R.id.answeredCntTv)
        markingBtn = findViewById(R.id.markingBtn)
        submitBtn = findViewById(R.id.submitBtn)
        emptyFilterContainer = findViewById(R.id.emptyFilterContainer)

        setBackgroundColor(ContextCompat.getColor(context, R.color.gray_200))
    }

    fun set(content: Content) {
        this.content = content
    }

    fun updateAll() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    inner class  SpeedAnswerAdapter: RecyclerView.Adapter<SpeedAnswerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeedAnswerHolder {
            val holderBinding: ItemSpeedyScoringBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_speedy_scoring, parent, false)
            return SpeedAnswerHolder(holderBinding)
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
        submitBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_gray_400_round_20)
        markingBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_gray_400_round_20)
    }

    fun enableMarking(cnt: Int) {
        answeredCntTv.text = cnt.toString()
        answeredCntTv.visibility = View.VISIBLE
        markingBtn.isEnabled = true
        markingBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_300_round_20)
        submitBtn.isEnabled = true
        submitBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_300_round_20)
    }

    fun showMarkingBtn() {
        markingBtn.visibility = View.VISIBLE
        submitBtn.visibility = View.INVISIBLE
    }

    fun showSubmitBtn() {
        markingBtn.visibility = View.INVISIBLE
        submitBtn.visibility = View.VISIBLE
    }

    fun scrollTo(problem: Problem?, from:String) {
        val index = getProblems().indexOf(problem)

        Log.d(javaClass.simpleName, "scrollTo(from:$from) ===> index = $index")

        if(index > 0)
            recyclerView.scrollToPosition(index)
        else
            recyclerView.scrollToPosition(0)
    }

    fun changedFocus(focusIndex: Int, prevIndex: Int) {
        val prevHolder = recyclerView.findViewHolderForAdapterPosition(prevIndex) as? SpeedAnswerHolder
        prevHolder?.keypadWindow?.dismiss()

        val focusedHolder = recyclerView.findViewHolderForAdapterPosition(focusIndex) as? SpeedAnswerHolder ?: return
        if (focusedHolder.holderBinding.shortAnswerEt.visibility == View.VISIBLE) {
            focusedHolder.holderBinding.shortAnswerEt.requestFocus()
        } else {
            focusedHolder.itemView.run {
                performClick()
                requestFocus()
                clearFocus()
            }
        }
    }

}
class SpeedAnswerHolder(val holderBinding: ItemSpeedyScoringBinding): RecyclerView.ViewHolder(holderBinding.root), PlusMinusKeypadListener, AnswerSelectionListener {
    lateinit var problem: Problem
    var delegate: AnswerDelegate? = null
    var speedAnswerDelegate: SpeedAnswerDelegate? = null
    var isShowAnswer: Boolean = false
    val resultIv = holderBinding.resultIv
    val numberTv = holderBinding.numberTv
    val infoContainer = holderBinding.infoContainer
    val selectionAnswerView = holderBinding.selectionAnswerView
    val shortAnswerWrapperCl = holderBinding.shortAnswerWrapperCl
    val shortAnswerView = holderBinding.shortAnswerEt
    val shortAnswerPrefixTv = holderBinding.shortAnswerPrefixTv
    val shortAnswerSuffixTv = holderBinding.shortAnswerSuffixTv


    val answerTv = holderBinding.answerTv
    val infoTv = holderBinding.infoTv
    val errorTv = holderBinding.errorTv
    val viewContext: Context
        get() = holderBinding.root.context

    val keypadWindow by lazy {
        PlusMinusKeypadWindow(viewContext, object : PlusMinusKeypadWindowListener {
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
            setTextIsSelectable(true)
            showSoftInputOnFocus = false
        }

        shortAnswerView.setOnFocusChangeListener { view, isFocused ->
            if(isFocused && ignoreFocus.not()) {
                speedAnswerDelegate?.onSpeedNumberClicked(problem)
                if (keypadWindow.isShowing.not()) {
//                    val height = minOf(
//                            ((DisplayUtils.getScrenHeight(context) - context.resources.getDimension(R.dimen.dp64)) * 0.5f).toInt() - 32.toPx(),
//                            context.resources.getDimensionPixelSize(R.dimen.omrActivity_keypad_height)
//                    )

                    val height = if(viewContext.isTablet) ((DisplayUtils.getScreenHeight(viewContext) - viewContext.resources.getDimension(R.dimen.dp64)) * 0.5f).toInt() - 32.toPx()
                                else ((DisplayUtils.getScreenHeight(viewContext) - viewContext.resources.getDimension(R.dimen.dp64)) * 0.8f).toInt() - 32.toPx()

                    val width = height * 232 / 296
                    var x = holderBinding.root.getTargetAbsolutePosition().first.toInt()
                    val y: Int = DisplayUtils.getScreenHeight(viewContext) - height - 16.toPx()
                    x = x - 32.toPx() - width

                    keypadWindow.showAtLocation(view.rootView, Gravity.NO_GRAVITY, x, y)
                    keypadWindow.contentView.layoutParams.width = width
                    (keypadWindow.contentView.layoutParams as ViewGroup.MarginLayoutParams).setMargins(16.toPx(), 16.toPx(), 16.toPx(), 16.toPx())
                    keypadWindow.contentView.layoutParams.height = height
                }
            }
            if (ignoreFocus) ignoreFocus = false
        }

        shortAnswerView.setOnKeyListener { view, i, keyEvent ->
            Log.d(javaClass.simpleName, "speed key =====> $keyEvent")
            if (keyEvent?.action == KeyEvent.ACTION_UP) {
                when (keyEvent?.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> prev()
                    KeyEvent.KEYCODE_DPAD_RIGHT -> next()
                    KeyEvent.KEYCODE_DEL -> del()
                    KeyEvent.KEYCODE_TAB -> next()
                    KeyEvent.KEYCODE_0 -> enterNumberBtnClicked("0")
                    KeyEvent.KEYCODE_1 -> enterNumberBtnClicked("1")
                    KeyEvent.KEYCODE_2 -> enterNumberBtnClicked("2")
                    KeyEvent.KEYCODE_3 -> enterNumberBtnClicked("3")
                    KeyEvent.KEYCODE_4 -> enterNumberBtnClicked("4")
                    KeyEvent.KEYCODE_5 -> enterNumberBtnClicked("5")
                    KeyEvent.KEYCODE_6 -> enterNumberBtnClicked("6")
                    KeyEvent.KEYCODE_7 -> enterNumberBtnClicked("7")
                    KeyEvent.KEYCODE_8 -> enterNumberBtnClicked("8")
                    KeyEvent.KEYCODE_9 -> enterNumberBtnClicked("9")
//                    KeyEvent.KEYCODE_NUMPAD_0,KeyEvent.KEYCODE_NUMPAD_1,KeyEvent.KEYCODE_NUMPAD_2,KeyEvent.KEYCODE_NUMPAD_3,KeyEvent.KEYCODE_NUMPAD_4
//                        ,KeyEvent.KEYCODE_NUMPAD_5,KeyEvent.KEYCODE_NUMPAD_6,KeyEvent.KEYCODE_NUMPAD_7,KeyEvent.KEYCODE_NUMPAD_8,KeyEvent.KEYCODE_NUMPAD_9
//                        ,KeyEvent.KEYCODE_0,KeyEvent.KEYCODE_1,KeyEvent.KEYCODE_2,KeyEvent.KEYCODE_3,KeyEvent.KEYCODE_4
//                        ,KeyEvent.KEYCODE_5,KeyEvent.KEYCODE_6,KeyEvent.KEYCODE_7,KeyEvent.KEYCODE_8,KeyEvent.KEYCODE_9 -> enterNumberBtnClicked()
                    KeyEvent.KEYCODE_MINUS -> toggleSpecificSign('-')
                    KeyEvent.KEYCODE_PLUS,
                    KeyEvent.KEYCODE_NUMPAD_ADD -> toggleSpecificSign('+')
                    KeyEvent.KEYCODE_DPAD_DOWN -> next()
                    KeyEvent.KEYCODE_DPAD_UP -> prev()
                }
            }
            true
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
//                    ignoreFocus = true
//                    shortAnswerView.requestFocus()
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        selectionAnswerView.listener = this
    }

    fun enterNumberBtnClicked() {
        delegate?.onAnswerChanged(holderBinding.root, shortAnswerView.text.toString())
    }

    fun next() {
        delegate?.next()
    }

    fun prev() {
        delegate?.prev()
    }

    fun del() {
        val answer = shortAnswerView.text.toString()
        if(answer.isNotEmpty()) {
            val deleted = answer.substring(0, answer.length - 1)
            shortAnswerView.setText(deleted)
            shortAnswerView.setSelection(deleted.length)
        }
        delegate?.onAnswerChanged(holderBinding.root, shortAnswerView.text.toString())
    }

    fun set(problem: Problem, isShowAnswer: Boolean) {
        this.problem = problem
        this.isShowAnswer = isShowAnswer
        resultIv.setImageDrawable(getResultDrawable())
        // 8인치 체크해서
        // 4글자 이상인데 + 가 있으면 +를 기준으로 개행
        if(viewContext.is10InchUI) {
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
            Result.correct -> ContextCompat.getDrawable(viewContext, R.drawable.ic_correct_new)
            Result.incorrect -> ContextCompat.getDrawable(viewContext, R.drawable.ic_incorrect_new)
            else -> null
        }
    }

    private fun disableRow() {
        shortAnswerView.visibility = View.INVISIBLE
        selectionAnswerView.visibility = View.INVISIBLE
        answerTv.visibility = View.GONE
        infoTv.visibility = View.GONE
        errorTv.visibility = View.VISIBLE
        numberTv.setTextColor(ContextCompat.getColor(viewContext, R.color.gray_500))
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
                numberTv.setTextColor(ContextCompat.getColor(viewContext, R.color.gray_800))
                answerTv.text = "정답 : ${getAnswerStr(problem.answerData)}"
                if(problem.getResultByScoring() == Result.incorrect) {
                    infoTv.text = if(problem.userAnswer != null) {
                        "(내 입력 : ${getAnswerStr(problem.userAnswer!!)})"
                    } else {
                        "(정답 미입력)"
                    }
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
        else {
            val prefix = problem.unitPrefix ?: ""
            val suffix = problem.unitSuffix ?: ""
            return "${prefix} ${answer}${suffix}".trim()
        }
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
        shortAnswerWrapperCl.visibility = View.INVISIBLE
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
        shortAnswerWrapperCl.visibility = View.INVISIBLE
        selectionAnswerView.visibility = View.INVISIBLE
        if(getInfoContainerVisibility() == View.VISIBLE) {
            shortAnswerWrapperCl.visibility = View.INVISIBLE
            shortAnswerView.visibility = View.INVISIBLE
        } else {
            shortAnswerWrapperCl.visibility = View.VISIBLE
            shortAnswerView.visibility = View.VISIBLE
        }

        shortAnswerView.isEnabled = true
        if (problem.userAnswer != null) {
            shortAnswerView.setText(problem.userAnswer.toString())
        } else {
            shortAnswerView.text = null
        }

        shortAnswerPrefixTv.visibleIf(!problem.unitPrefix.isNullOrEmpty())
        shortAnswerSuffixTv.visibleIf(!problem.unitSuffix.isNullOrEmpty())
        shortAnswerPrefixTv.text = problem.unitPrefix
        shortAnswerSuffixTv.text = problem.unitSuffix
    }

    fun enterNumberBtnClicked(text:String) {
        if(shortAnswerView.length() == shortAnswerView.selectionStart)
            shortAnswerView.append(text)
        else
            shortAnswerView.text.insert(shortAnswerView.selectionStart, text)
        delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString())
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
        delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString())
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

    // 키패드 ± 버튼: 없음 → '-' → '+' → 없음 순환
    fun enterMinusBtnClicked() {
        val next: Char? = when (shortAnswerView.text.firstOrNull()) {
            '-' -> '+'
            '+' -> null
            else -> '-'
        }
        applySignPrefix(next)
        delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString())
    }

    // 외부 키보드 +/- 키: 같은 부호면 제거, 다른 부호면 교체, 없으면 삽입
    private fun toggleSpecificSign(sign: Char) {
        val first = shortAnswerView.text.firstOrNull()
        applySignPrefix(if (first == sign) null else sign)
        delegate?.onAnswerChanged(itemView, shortAnswerView.text.toString())
    }

    // 맨 앞 부호를 sign('+'/'-')으로 설정. null이면 부호 제거. 길이 9 한도 유지.
    private fun applySignPrefix(sign: Char?) {
        val text = shortAnswerView.text
        val first = text.firstOrNull()
        val hadSign = first == '-' || first == '+'
        when {
            sign == null && hadSign -> text.delete(0, 1)
            sign == null -> Unit
            hadSign -> text.replace(0, 1, sign.toString())
            else -> {
                if (text.length == 9) text.delete(8, 9)
                text.insert(0, sign.toString())
            }
        }
    }

    override fun onPlusMinusBtnClicked(button: ImageButton) {
        enterMinusBtnClicked()
    }

    override fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?) {
        delegate?.onAnswerChanged(itemView, answerStr, problem)
    }
}