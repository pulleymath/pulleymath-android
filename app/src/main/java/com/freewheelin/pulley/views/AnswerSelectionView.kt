package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.utils.extensionTouchArea
import com.freewheelin.pulley.utils.spToPx
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.NumberingButton.Companion.THEME_WHITE

interface AnswerSelectionListener {
    fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?)
}

class AnswerSelectionView: LinearLayout, NumberingButtonListener {
    private val SINGLE = 0
    private val MULTI = 1

    var buttonMargin: Int = 12.toPx()
        set(value) {
            field = value

            setButtonMargin(number1Btn, value)
            setButtonMargin(number2Btn, value)
            setButtonMargin(number3Btn, value)
            setButtonMargin(number4Btn, value)
            setButtonMargin(number5Btn, value)
        }

    var buttonWidth: Int = 28.toPx()
        set(value) {
            field = value
            setWidth(number1Btn, value)
            setWidth(number2Btn, value)
            setWidth(number3Btn, value)
            setWidth(number4Btn, value)
            setWidth(number5Btn, value)
        }
    var buttonHeight: Int = 28.toPx()
        set(value) {
            field = value
            setHeight(number1Btn, value)
            setHeight(number2Btn, value)
            setHeight(number3Btn, value)
            setHeight(number4Btn, value)
            setHeight(number5Btn, value)
        }


    var answer: HashSet<String> = HashSet()
    set(value) {
        field = value

        number1Btn.isSelected = false
        number2Btn.isSelected = false
        number3Btn.isSelected = false
        number4Btn.isSelected = false
        number5Btn.isSelected = false

        for (answer in value) {
            when (answer) {
                "1" -> number1Btn.isSelected = true
                "2" -> number2Btn.isSelected = true
                "3" -> number3Btn.isSelected = true
                "4" -> number4Btn.isSelected = true
                "5" -> number5Btn.isSelected = true
            }
        }
    }
    var answerType: Int = SINGLE

    var listener: AnswerSelectionListener? = null

    var theme = THEME_WHITE
    set(value) {
        field = value

        number1Btn.theme = value
        number2Btn.theme = value
        number3Btn.theme = value
        number4Btn.theme = value
        number5Btn.theme = value
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled == false) {
            number1Btn.isEnabled = false
            number2Btn.isEnabled = false
            number3Btn.isEnabled = false
            number4Btn.isEnabled = false
            number5Btn.isEnabled = false
        } else {
            number1Btn.isEnabled = true
            number2Btn.isEnabled = true
            number3Btn.isEnabled = true
            number4Btn.isEnabled = true
            number5Btn.isEnabled = true
        }
    }


    var textSize: Int = 16.spToPx().toInt()
    set(value) {
        field = value
        val sp = value.toFloat()

        number1Btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp)
        number2Btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp)
        number3Btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp)
        number4Btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp)
        number5Btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var number1Btn: NumberingButton
    var number2Btn: NumberingButton
    var number3Btn: NumberingButton
    var number4Btn: NumberingButton
    var number5Btn: NumberingButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_answer_selection, this)
        gravity = Gravity.CENTER

        number1Btn = findViewById(R.id.number1Btn)
        number2Btn = findViewById(R.id.number2Btn)
        number3Btn = findViewById(R.id.number3Btn)
        number4Btn = findViewById(R.id.number4Btn)
        number5Btn = findViewById(R.id.number5Btn)

        number1Btn.listener = this
        number2Btn.listener = this
        number3Btn.listener = this
        number4Btn.listener = this
        number5Btn.listener = this
    }

    override fun onNumberingButtonClicked(button: NumberingButton, isSelected: Boolean) {
        if (answerType == SINGLE) {
            answer.remove(number1Btn.text)
            answer.remove(number2Btn.text)
            answer.remove(number3Btn.text)
            answer.remove(number4Btn.text)
            answer.remove(number5Btn.text)

            if (button != number1Btn)
                number1Btn.isSelected = false
            if(button != number2Btn)
                number2Btn.isSelected = false
            if(button != number3Btn)
                number3Btn.isSelected = false
            if(button != number4Btn)
                number4Btn.isSelected = false
            if(button != number5Btn)
                number5Btn.isSelected = false

            if(isSelected)
                answer.add(button.text.toString())
            else
                answer.remove(button.text.toString())

        } else {
            if (isSelected) {
                answer.add(button.text.toString())
            } else {
                answer.remove(button.text.toString())
            }
        }

        listener?.onAnswerChanged(this, getAnswerStr())
    }

    private fun getAnswerStr(): String? {
        if(this.answer.isEmpty())
            return null

        val sortedAnswers = this.answer.sortedBy { it }

        return sortedAnswers.joinToString(separator = ",")
    }
    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.AnswerSelectionView)
        buttonWidth = (array.getDimensionPixelSize(R.styleable.AnswerSelectionView_buttonWidth, 28))
        buttonMargin = (array.getDimensionPixelSize(R.styleable.AnswerSelectionView_buttonMargin, 24))
        buttonHeight = (array.getDimensionPixelSize(R.styleable.AnswerSelectionView_buttonHeight, 28))
        textSize = array.getDimensionPixelSize(R.styleable.AnswerSelectionView_textSize, 16.spToPx().toInt())
        answerType = array.getInt(R.styleable.AnswerSelectionView_answerType, SINGLE)
        theme = array.getInt(R.styleable.AnswerSelectionView_selectionTheme, THEME_WHITE)
    }

    private fun setButtonMargin(button: Button, margin: Int) {
        val margin = (margin * 0.5).toInt()
        (button.layoutParams as? LayoutParams)?.apply {
            if(orientation == VERTICAL)
                setMargins(24, margin, 24, margin)

            else
                setMargins(margin, 24, margin, 24)
        }
        button.extensionTouchArea(margin)
        requestLayout()
    }

    private fun setWidth(button: Button, size: Int) {
        (button.layoutParams as? LayoutParams)?.apply {
            width = size
        }
        button.requestLayout()
    }

    private fun setHeight(button: Button, size: Int) {
        (button.layoutParams as? LayoutParams)?.apply {
            height = size
        }
        button.requestLayout()
    }

    fun setAnswerByRawString(answerStr: String?) {
        if(answerStr == null)
            this.answer = HashSet()
        else {
            this.answer= answerStr.split(",").toHashSet()
        }
    }

    fun setAnswerType(type: ProblemType) {
        if(type == ProblemType.single)
            this.answerType = SINGLE
        else
            this.answerType = MULTI
    }
}
