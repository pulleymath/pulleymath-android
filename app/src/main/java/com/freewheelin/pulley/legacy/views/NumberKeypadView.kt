package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx
import java.lang.ref.WeakReference

interface PlusMinusKeypadWindowListener {
    var keypadListener: PlusMinusKeypadListener
    fun onKeyboardDismiss()
}

class PlusMinusKeypadWindow : PopupWindow {
    var context: Context
    var listener: PlusMinusKeypadWindowListener? = null

    constructor(context: Context, listener: PlusMinusKeypadWindowListener) : super(context) {
        this.context = context
        this.listener = listener
        val keypad = PlusMinusKeypadView(context)
        keypad.setPlusMinusListener(listener.keypadListener)
        this.contentView = keypad
        contentView.elevation = 4f.toPx()
        setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))
        isOutsideTouchable = true
        isFocusable = false
    }

    override fun dismiss() {
        super.dismiss()
        listener?.onKeyboardDismiss()
    }
}

interface NumberKeypadListener {
    fun onNumberBtnClicked(button: Button, text: String)
    fun onDeleteBtnClicked(button: ImageButton)
    fun onNextBtnClicked(button: Button) {}
    fun onFinishBtnClicked(button: Button) {}
}

interface PlusMinusKeypadListener {
    fun onNumberBtnClicked(button: Button, text: String)
    fun onDeleteBtnClicked(button: ImageButton)
    fun onPlusMinusBtnClicked(button: ImageButton)
}

class PlusMinusKeypadView : ConstraintLayout {
    var listener: WeakReference<PlusMinusKeypadListener>? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var number0Btn: Button
    var number1Btn: Button
    var number2Btn: Button
    var number3Btn: Button
    var number4Btn: Button
    var number5Btn: Button
    var number6Btn: Button
    var number7Btn: Button
    var number8Btn: Button
    var number9Btn: Button

    var deleteBtn: ImageButton
    var plusMinusBtn: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_plus_minus_keypad, this)
        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_100_round_ripple)
        isClickable = true

        number0Btn = findViewById(R.id.number0Btn)
        number1Btn = findViewById(R.id.number1Btn)
        number2Btn = findViewById(R.id.number2Btn)
        number3Btn = findViewById(R.id.number3Btn)
        number4Btn = findViewById(R.id.number4Btn)
        number5Btn = findViewById(R.id.number5Btn)
        number6Btn = findViewById(R.id.number6Btn)
        number7Btn = findViewById(R.id.number7Btn)
        number8Btn = findViewById(R.id.number8Btn)
        number9Btn = findViewById(R.id.number9Btn)

        deleteBtn = findViewById(R.id.deleteBtn)
        plusMinusBtn = findViewById(R.id.plusMinusBtn)

        number0Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number0Btn, "0")
        }

        number1Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number1Btn, "1")
        }

        number2Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number2Btn, "2")
        }

        number3Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number3Btn, "3")
        }

        number4Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number4Btn, "4")
        }

        number5Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number5Btn, "5")
        }

        number6Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number6Btn, "6")
        }

        number7Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number7Btn, "7")
        }

        number8Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number8Btn, "8")
        }

        number9Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number9Btn, "9")
        }

        deleteBtn.setOnClickListener {
            listener?.get()?.onDeleteBtnClicked(deleteBtn)
        }
        plusMinusBtn.setOnClickListener {
            listener?.get()?.onPlusMinusBtnClicked(plusMinusBtn)
        }
    }

    fun setPlusMinusListener(listener: PlusMinusKeypadListener) {
        this.listener = WeakReference(listener)
    }

    fun setPadding(padding: Int) {
        setPadding(padding, padding, padding, padding)
    }
}

class NumberKeypadView : ConstraintLayout {
    enum class Type {
        NEXT,
        FINISH
    }

    var type: Type = Type.NEXT
        set(value) {
            field = value
            when (type) {
                Type.NEXT -> {
                    nextBtn.visibility = View.VISIBLE
                    finishBtn.visibility = View.GONE
                }
                Type.FINISH -> {
                    nextBtn.visibility = View.GONE
                    finishBtn.visibility = View.VISIBLE
                }
            }

        }
    var listener: WeakReference<NumberKeypadListener>? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var number0Btn: Button
    var number1Btn: Button
    var number2Btn: Button
    var number3Btn: Button
    var number4Btn: Button
    var number5Btn: Button
    var number6Btn: Button
    var number7Btn: Button
    var number8Btn: Button
    var number9Btn: Button

    var deleteBtn: ImageButton
    var nextBtn: Button
    var finishBtn: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.view_number_keypad, this)
        val paddingVal = resources.getDimension(R.dimen.dp32)
        setPadding(paddingVal.toInt())
        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_100_round_ripple)
        isClickable = true

        number0Btn = findViewById(R.id.number0Btn)
        number1Btn = findViewById(R.id.number1Btn)
        number2Btn = findViewById(R.id.number2Btn)
        number3Btn = findViewById(R.id.number3Btn)
        number4Btn = findViewById(R.id.number4Btn)
        number5Btn = findViewById(R.id.number5Btn)
        number6Btn = findViewById(R.id.number6Btn)
        number7Btn = findViewById(R.id.number7Btn)
        number8Btn = findViewById(R.id.number8Btn)
        number9Btn = findViewById(R.id.number9Btn)

        deleteBtn = findViewById(R.id.deleteBtn)
        nextBtn = findViewById(R.id.nextBtn)
        finishBtn = findViewById(R.id.finishBtn)

        number0Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number0Btn, "0")
        }

        number1Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number1Btn, "1")
        }

        number2Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number2Btn, "2")
        }

        number3Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number3Btn, "3")
        }

        number4Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number4Btn, "4")
        }

        number5Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number5Btn, "5")
        }

        number6Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number6Btn, "6")
        }

        number7Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number7Btn, "7")
        }

        number8Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number8Btn, "8")
        }

        number9Btn.setOnClickListener {
            listener?.get()?.onNumberBtnClicked(number9Btn, "9")
        }

        deleteBtn.setOnClickListener {
            listener?.get()?.onDeleteBtnClicked(deleteBtn)
        }

        nextBtn.setOnClickListener {
            listener?.get()?.onNextBtnClicked(nextBtn)
        }

        finishBtn.setOnClickListener {
            listener?.get()?.onFinishBtnClicked(finishBtn)
        }
    }

    fun setNumberKeypadListener(listener: NumberKeypadListener) {
        this.listener = WeakReference(listener)
    }

    fun setPadding(padding: Int) {
        setPadding(padding, padding, padding, padding)
    }
}