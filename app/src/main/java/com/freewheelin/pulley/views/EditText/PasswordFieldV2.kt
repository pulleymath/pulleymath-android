package com.freewheelin.pulley.views.editText

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

interface PasswordFieldV2Listener {
    fun onFieldFocusChanged(view: PasswordFieldV2, hasFocus: Boolean)
    fun onFieldValueChanged(view: PasswordFieldV2) {}
}

interface PasswordFieldV2EnterListener {
    fun onEnter(view:View)
}

class PasswordFieldV2: LinearLayout, View.OnFocusChangeListener {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var listener: PasswordFieldV2Listener? = null
    var enterListener: PasswordFieldV2EnterListener? = null
    var errorTv: TextView
    var inputLayout: TextInputLayout

    var text: String
        get() {
            val editText = findViewById<TextInputEditText>(R.id.inputEt)
            return editText.text.toString()
        }
        set(value) {
            val editText = findViewById<TextInputEditText>(R.id.inputEt)
            editText.setText(value)
        }

    var errorMsg: String
        get() {
            return errorTv.text.toString()
        }
        set(value) {
            errorTv.text = value
            isShownError = true
        }

    var isShownError: Boolean
        get() {
            val container = findViewById<LinearLayout>(R.id.errorContainerLl)
            return container.visibility == View.VISIBLE
        }
        set(value) {
            val container = findViewById<LinearLayout>(R.id.errorContainerLl)
            container.visibility = if (value) View.VISIBLE else View.GONE
            inputLayout.error = if (value) " " else null
        }
    var inputEt: TextInputEditText
        get() {
            return findViewById(R.id.inputEt)
        }


    override fun setEnabled(enabled: Boolean) {
        val edit = findViewById<TextInputEditText>(R.id.inputEt)
        edit.isEnabled = enabled
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_password_v2, this)

        errorTv = findViewById(R.id.errorTv)
        inputEt = findViewById(R.id.inputEt)
        inputLayout = findViewById(R.id.inputLayout)

        val check = findViewById<CheckBox>(R.id.checkEye)

        inputEt.onFocusChangeListener = this
        inputEt.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                isShownError = false
                listener?.onFieldValueChanged(this@PasswordFieldV2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                if(sequence.isEmpty()) {
                    inputEt.textSize = 16f
                } else {
                    inputEt.textSize = 18f
                }
            }
        })
        check.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                inputEt.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                inputEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            inputEt.setSelection(inputEt.length())
        }
        isShownError = false
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        val edit = findViewById<TextInputEditText>(R.id.inputEt)
        listener?.onFieldFocusChanged(this, hasFocus)

        edit.setOnKeyListener { _, keyCode, event ->
            if(event.keyCode == KeyEvent.KEYCODE_ENTER) {
                enterListener?.onEnter(this)
                true
            }
            false
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PasswordFieldV2)
        inputLayout.hint = array.getString(R.styleable.PasswordFieldV2_PasswordFieldV2_Hint)
        val showErrorDrawable = array.getBoolean(R.styleable.PasswordFieldV2_PasswordFieldV2_ShowErrorIcon, false)
        if (!showErrorDrawable) {
            inputLayout.errorIconDrawable = null
        }
        setMaxLength(array.getInt(R.styleable.PasswordFieldV2_PasswordFieldV2_maxLength, 0))
        array.recycle()
    }

    fun setMaxLength(length:Int) {
        val edit = findViewById<TextInputEditText>(R.id.inputEt)

        if(length > 0)
            edit.filters = arrayOf( InputFilter.LengthFilter(length) )
    }

    fun showErrorMsg(errorMsg: String) {
        this.errorMsg = errorMsg
        this.isShownError = true
        context.vibrate()
    }
}