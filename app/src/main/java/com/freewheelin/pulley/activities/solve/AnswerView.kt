package com.freewheelin.pulley.activities.solve

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*


interface AnswerDelegate {
    fun backPressed()
    fun getActivity(): Activity
    fun onAnswerChanged(view: View, answer: String?, problem: Problem? = null)
    fun onEnter()
    fun next()
    fun prev()
}

class AnswerView : ConstraintLayout,
        PlusMinusKeypadListener,
        AnswerSelectionListener {
    var delegate: AnswerDelegate? = null

    companion object {
        var x: Float? = null
        var y: Float? = null
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var keyPad: PopupWindow? = null
    var isShowSubmit = false

    var shortAnswerView: EditText
    var dragIv: ImageView
    var selectionAnswerView: AnswerSelectionView
    var focusContainer: LinearLayout
    var resultIv: ImageView

    var answeredCntTv: TextView
    var markingBtn: ConstraintLayout
    var submitBtn: Button
    var challengeStampIv: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_answer, this)

        shortAnswerView = findViewById(R.id.shortAnswerView)
        dragIv = findViewById(R.id.dragIv)
        selectionAnswerView = findViewById(R.id.selectionAnswerView)
        focusContainer = findViewById(R.id.focusContainer)
        resultIv = findViewById(R.id.resultIv)

        answeredCntTv = findViewById(R.id.answeredCntTv)
        markingBtn = findViewById(R.id.markingBtn)
        submitBtn = findViewById(R.id.submitBtn)
        challengeStampIv = findViewById(R.id.challengeStampIv)

        val paddingStartEnd = resources.getDimension(R.dimen.dp16).toInt()
        setPadding(paddingStartEnd, 0, paddingStartEnd, 0)
        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_32)

        shortAnswerView.removeKeyboard()
        shortAnswerView.setTextSize( resources.getDimension(R.dimen.sp24), resources.getDimension(R.dimen.sp14))

        shortAnswerView.setOnKeyListener { v, keyCode, event ->

            Log.d(javaClass.simpleName, "AnswerView -> $event")

            if (event?.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        if (shortAnswerView?.text?.isNotBlank() == true) delegate?.onEnter()
                    }
                    KeyEvent.KEYCODE_DEL -> deleteBtnClicked()
                    KeyEvent.KEYCODE_MINUS -> enterMinusBtnClicked()
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
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        delegate?.prev()
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        delegate?.next()
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        delegate?.backPressed()
                    }
                }
            }
            true
        }

        shortAnswerView.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                if (keyPad == null)
                    keyPad = PlusMinusKeypadWindow(context, object : PlusMinusKeypadWindowListener {
                        override var keypadListener: PlusMinusKeypadListener = this@AnswerView
                        override fun onKeyboardDismiss() {
                            shortAnswerView.clearFocus()
                        }
                    })

                val height = if(context.isTablet) minOf(
                                ((DisplayUtils.getScreenHeight(context) - this.height) * 0.5f).toInt() - 32.toPx(),
                                resources.getDimensionPixelSize(R.dimen.omrActivity_keypad_height))
                            else ((DisplayUtils.getScreenHeight(context) - this.height) * 0.8f).toInt() - 32.toPx()
                val width = height * 232 / 296

                val position = getTargetAbsolutePosition(false)
                var x = position.first.toInt()
                var y = position.second.toInt()
                x = (x + shortAnswerView.x - 16.toPx()).toInt()

                y = if (isAbove())
                    (y + this.height - 8.toPx())
                else
                    (y - height - 24.toPx())

                keyPad?.showAtLocation(this.rootView, Gravity.NO_GRAVITY, x, y)
                keyPad?.contentView?.layoutParams?.width = width
                (keyPad?.contentView?.layoutParams as ViewGroup.MarginLayoutParams).setMargins(16.toPx(), 16.toPx(), 16.toPx(), 16.toPx())
                keyPad?.contentView?.layoutParams?.height = height
//                shortAnswerView.requestFocus()
            }
        }

        selectionAnswerView.listener = this
        dragIv.extensionTouchArea(8.toPx())
        dragIv.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = AnswerShadowBuilder(this)
                this.startDrag(data, shadowBuilder, view, 0)
                this.visibility = View.INVISIBLE
                true
            } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                this.visibility = View.VISIBLE
                false
            } else {
                false
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        when(visibility) {
            View.VISIBLE -> {
                shortAnswerView.isFocusableInTouchMode = true
            }
            View.INVISIBLE, View.GONE -> {
                shortAnswerView.isFocusableInTouchMode = false
            }
        }
    }

    fun setInitPosition() {
        if (AnswerView.x != null && AnswerView.y != null) {
            this.x = AnswerView.x!!
            this.y = AnswerView.y!!
        }
    }

    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        AnswerView.x = x
        AnswerView.y = y
    }

    var selectedProblem: Problem? = null
    fun configureUI(problem: Problem, requestFocus: Boolean) {
        selectedProblem = problem
        if (problem.problemType == ProblemType.short) {
            shortAnswerView.visibility = View.VISIBLE
            selectionAnswerView.visibility = View.INVISIBLE
            shortAnswerView.setText(problem.userAnswer)
        } else {
            shortAnswerView.visibility = View.INVISIBLE
            selectionAnswerView.visibility = View.VISIBLE
            selectionAnswerView.setAnswerType(problem.problemType)
            selectionAnswerView.setAnswerByRawString(problem.userAnswer)
        }

        if (requestFocus)
            focusContainer.requestFocus()

        when (problem.getResultByScoring()) {
            Result.yet -> {
                selectionAnswerView.theme = NumberingButton.THEME_BLACK
                shortAnswerView.setBackgroundResource(R.drawable.bg_white_ffffff_stroke_grey_e0e0e0_round_2)
                shortAnswerView.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                shortAnswerView.isEnabled = true
                selectionAnswerView.isEnabled = true
                resultIv.visibility = View.GONE
//                challengeStampIv.visibility = if (selectedBook?.pieceSubCategory == "START") {
//                    View.VISIBLE
//                } else {
//                    View.GONE
//                }
//                if (problem.problemType == ProblemType.short) shortAnswerView.requestFocus()
            }
            else -> {
                selectionAnswerView.theme = NumberingButton.THEME_GREY
                shortAnswerView.setBackgroundResource(R.drawable.bg_grey_e0e0e0_round_2)
                shortAnswerView.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                shortAnswerView.isEnabled = false
                selectionAnswerView.isEnabled = false
                resultIv.visibility = View.VISIBLE
                challengeStampIv.visibility = View.GONE
                if (problem.getResultByScoring() == Result.incorrect) {
                    resultIv.setImageResource(R.drawable.ic_incorrect_new)
                } else {
                    resultIv.setImageResource(R.drawable.ic_correct_new)
                }
            }
        }
    }

    fun getShortAnswerText() : String {
        return shortAnswerView.text.toString() ?: ""
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

    fun requestFocusOnShortAnswer() {
        shortAnswerView.requestFocus()
        val pos = shortAnswerView.text.toString().length
        shortAnswerView.setSelection(pos)
    }

    fun clearFocusOnShortAnswer() {
        shortAnswerView.clearFocus()
        releasePad()
    }

    fun releasePad() {
        keyPad?.dismiss()
    }

    override fun onNumberBtnClicked(button: Button, text: String) {
        enterNumberBtnClicked(text)
    }

    fun enterNumberBtnClickedFromSolve(text: String, type: ProblemType) {
        Log.d("키보드", "enterNumberBtnClickedFromSolve before => answer=${selectionAnswerView.answer}, type=$type")
        when (type) {
            ProblemType.multi -> {
                val current = selectionAnswerView.answer
                if (current.contains(text)) {
                    current.remove(text)
                } else {
                    current.add(text)
                }
                selectionAnswerView.answer = current
            }
            else -> {
                selectionAnswerView.answer = hashSetOf(text)
            }
        }


        val answerText = selectionAnswerView.answer.joinToString(",")
        shortAnswerView.setText(answerText)
        Log.d("키보드", "answerText=${answerText}")
        delegate?.onAnswerChanged(this, answerText)
    }

    fun enterNumberBtnClicked() {
        delegate?.onAnswerChanged(this, shortAnswerView.text.toString())
    }

    fun enterNumberBtnClicked(text: String) {
        if (shortAnswerView.length() == shortAnswerView.selectionStart)
            shortAnswerView.append(text)
        else
            shortAnswerView.text.insert(shortAnswerView.selectionStart, text)
        delegate?.onAnswerChanged(this, shortAnswerView.text.toString())
    }

    override fun onDeleteBtnClicked(button: ImageButton) {
        deleteBtnClicked()
    }

    fun deleteBtnClicked() {
        Log.d("키보드", "selectionStart=${shortAnswerView.selectionStart}")
        if (shortAnswerView.selectionStart != 0)
            shortAnswerView.text.delete(
                    shortAnswerView.selectionStart - 1,
                    shortAnswerView.selectionStart
            )
        delegate?.onAnswerChanged(this, shortAnswerView.text.toString())

        if(shortAnswerView.text.length == 1 && shortAnswerView.text.toString() == "-")
            disableMarking()
    }

    override fun onPlusMinusBtnClicked(button: ImageButton) {
        enterMinusBtnClicked()
    }

    fun enterMinusBtnClicked() {
        if (shortAnswerView.text.firstOrNull() == '-') {
            shortAnswerView.text.delete(0, 1)
        } else {
            val length = shortAnswerView.text.length
            if (length == 9)
                shortAnswerView.text.delete(length - 1, length)
            shortAnswerView.text.insert(0, "-")
        }
        // - 한개만 입력 시 버튼 활성화 막기
        if(shortAnswerView.text.length == 1 && shortAnswerView.text.toString() == "-")
            return

        delegate?.onAnswerChanged(this, shortAnswerView.text.toString())
    }

    override fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?) {
        delegate?.onAnswerChanged(this, answerStr)
    }

    var selectedBook: Book? = null
    fun showChallengeStampIv(selectedBook: Book) {
        challengeStampIv.visibility = when (selectedProblem?.getResultByScoring()) {
            Result.yet -> {
                if (selectedBook.isStartChallengePiece() && selectedProblem?.isSimilarProblem() == false) {
                    Preferences.tooltipShowingCntAddSimilarOfStartChallenge.set(0)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            else -> View.GONE
        }
    }
    fun showChallengeStampIv(selectedBook: Book, isPulleyBooksCourseOfChallengeInProgress: Boolean) {
        println("asoaso showChallengeStampIv : ${isPulleyBooksCourseOfChallengeInProgress}")
        challengeStampIv.visibility = when (selectedProblem?.getResultByScoring()) {
            Result.yet -> {
                if (selectedBook.isStartChallengePiece()
                    && selectedProblem?.isSimilarProblem() == false
                    && isPulleyBooksCourseOfChallengeInProgress) {
                    println("asoaso showChallengeStampIv 2 ")
                    Preferences.tooltipShowingCntAddSimilarOfStartChallenge.set(0)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            else -> View.GONE
        }
    }
    fun showChallengeStampIv(isShow: Boolean) {
        challengeStampIv.visibility = if (isShow) View.VISIBLE else View.GONE
    }
    fun showMarkingBtn() {
        markingBtn.visibility = View.VISIBLE
        submitBtn.visibility = View.INVISIBLE

        isShowSubmit = false
    }

    fun showSubmitBtn() {
        markingBtn.visibility = View.INVISIBLE
        submitBtn.visibility = View.VISIBLE

        isShowSubmit = true
    }

    fun getEnableSubmit(): Boolean {
        return isShowSubmit && submitBtn.isEnabled
    }
}