package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewPlusMinusEnterKeypadBinding
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.utils.visibleIf

interface PlusMinusEnterKeypadListener {
    fun onEnterBtnClicked(button: Button, answer: String)
}

class CookingCustomNumberKeyboard : ConstraintLayout, View.OnClickListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    val binding: ViewPlusMinusEnterKeypadBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_plus_minus_enter_keypad, this, true)


//    var listener: WeakReference<PlusMinusEnterKeypadListener>? = null
    var listener: PlusMinusEnterKeypadListener? = null
    val initialValue = "정답 입력"

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_100_round_ripple)
        isClickable = true

        binding.apply {
            mobileRootCl.visibleIf(context.isMobile)
            tabletRootCl.visibleIf(!context.isMobile)

            if (context.isMobile) {
                enterMobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number0MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number1MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number2MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number3MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number4MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number5MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number6MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number7MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number8MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number9MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number0MobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                deleteMobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                plusMinusMobileBtn.setOnClickListener(this@CookingCustomNumberKeyboard)

            } else {

                enterBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number0Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number1Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number2Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number3Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number4Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number5Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number6Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number7Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number8Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number9Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                number0Btn.setOnClickListener(this@CookingCustomNumberKeyboard)
                deleteBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
                plusMinusBtn.setOnClickListener(this@CookingCustomNumberKeyboard)
            }
        }
    }

    fun releaseKeyboard(listener: PlusMinusEnterKeypadListener?) {
        this.listener = listener
        binding.valueTv.text = initialValue
        binding.valueMobileTv.text = initialValue
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                number0Btn -> { numberBtnOnClickEvent(v as Button, "0") }
                number1Btn -> { numberBtnOnClickEvent(v as Button, "1") }
                number2Btn -> { numberBtnOnClickEvent(v as Button, "2") }
                number3Btn -> { numberBtnOnClickEvent(v as Button, "3") }
                number4Btn -> { numberBtnOnClickEvent(v as Button, "4") }
                number5Btn -> { numberBtnOnClickEvent(v as Button, "5") }
                number6Btn -> { numberBtnOnClickEvent(v as Button, "6") }
                number7Btn -> { numberBtnOnClickEvent(v as Button, "7") }
                number8Btn -> { numberBtnOnClickEvent(v as Button, "8") }
                number9Btn -> { numberBtnOnClickEvent(v as Button, "9") }
                deleteBtn -> { deleteBtnClickEvent() }
                plusMinusBtn -> { plusMinusClickEvent() }
                enterBtn -> { enterClickEvent(v as Button) }

                number0MobileBtn -> { numberBtnOnClickEvent(v as Button, "0") }
                number1MobileBtn -> { numberBtnOnClickEvent(v as Button, "1") }
                number2MobileBtn -> { numberBtnOnClickEvent(v as Button, "2") }
                number3MobileBtn -> { numberBtnOnClickEvent(v as Button, "3") }
                number4MobileBtn -> { numberBtnOnClickEvent(v as Button, "4") }
                number5MobileBtn -> { numberBtnOnClickEvent(v as Button, "5") }
                number6MobileBtn -> { numberBtnOnClickEvent(v as Button, "6") }
                number7MobileBtn -> { numberBtnOnClickEvent(v as Button, "7") }
                number8MobileBtn -> { numberBtnOnClickEvent(v as Button, "8") }
                number9MobileBtn -> { numberBtnOnClickEvent(v as Button, "9") }
                deleteMobileBtn -> { deleteBtnClickEvent() }
                plusMinusMobileBtn -> { plusMinusClickEvent() }
                enterMobileBtn -> { enterClickEvent(v as Button) }
                else -> { }
            }
        }
    }
    fun enterClickEvent(button: Button) {
        val enteredValue = if (context.isMobile) {
            binding.valueMobileTv.text.toString()
        } else {
            binding.valueTv.text.toString()
        }
        val isNotInitialValue = enteredValue != initialValue
        val isNot_OnlyMinus = enteredValue != "-"
        val isNotMinusZero = enteredValue != "-0"
        if (isNotInitialValue && isNot_OnlyMinus && isNotMinusZero) {
//            listener?.get()?.onEnterBtnClicked(button, enteredValue)
            listener?.onEnterBtnClicked(button, enteredValue)
        }
    }
    fun plusMinusClickEvent() {
        binding.apply {
            val prevValue = if (context.isMobile) {
                valueMobileTv.text.toString()
            } else {
                valueTv.text.toString()
            }

            val valueWillBe = if (prevValue == initialValue) {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                "-"
            } else if (prevValue == "-") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                " "
            } else if (prevValue == "0") {
                "0"
            } else if (prevValue.first().toString() == "-") {
                prevValue.drop(1)
            } else {
                "-$prevValue"
            }

            valueTv.text = valueWillBe
            valueMobileTv.text = valueWillBe
        }
    }
    fun deleteBtnClickEvent() {
        binding.apply {
            val prevValue = if (context.isMobile) {
                valueMobileTv.text.toString()
            } else {
                valueTv.text.toString()
            }
            val valueWillBe = if (prevValue == "0") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                initialValue
            } else if (prevValue == "-") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                initialValue
            } else if (prevValue == initialValue) {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                initialValue
            } else if (prevValue.length == 1) {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                initialValue
            } else {
                prevValue.dropLast(1)
            }
            valueTv.text = valueWillBe
            valueMobileTv.text = valueWillBe
        }
    }

    fun numberBtnOnClickEvent(button: Button, text: String) {
        binding.apply {
            val prevValue = if (context.isMobile) {
                valueMobileTv.text.toString()
            } else {
                valueTv.text.toString()
            }
            val valueWillBe = if (prevValue == "0") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                text
            } else if (prevValue == "-0") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                "-$text"
            } else if (prevValue == "-" && text == "0") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common_disabled_20)
                prevValue
            } else if (prevValue == initialValue) {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                text
            } else if (prevValue == " ") {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                text
            } else {
                enterBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                enterMobileBtn.background = ContextCompat.getDrawable(context, R.drawable.bg_btn_round_common)
                prevValue + text
            }
            valueTv.text = valueWillBe
            valueMobileTv.text = valueWillBe
        }
    }
}