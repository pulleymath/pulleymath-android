package com.freewheelin.pulley.views.EditText

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import kotlinx.android.synthetic.main.view_input_daebak.view.*
import kotlinx.android.synthetic.main.view_input_password.view.*
import kotlinx.android.synthetic.main.view_input_password.view.editText
import kotlinx.android.synthetic.main.view_input_password.view.errorContainerLl
import kotlinx.android.synthetic.main.view_input_password.view.errorTv
import kotlinx.android.synthetic.main.view_input_password.view.labelTv

interface DaebakPasswordFieldListener {
    fun onFieldFocusChanged(view: DaebakPasswordField, hasFocus: Boolean)
    fun onFieldValueChanged(view: DaebakPasswordField) {}
}

interface DaebakPasswordEnterListener {
    fun onEnter(view:View)
}

class DaebakPasswordField: LinearLayout, View.OnFocusChangeListener {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var listener: DaebakPasswordFieldListener? = null
    var enterListener: DaebakPasswordEnterListener? = null

    var label: String?
        get() {
            return labelTv.text.toString()
        }
        set(value) {
            labelTv.text = value
        }

    var text: String
        get() {
            return editText.text.toString()
        }
        set(value) {
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
             return errorContainerLl.visibility == View.VISIBLE
         }
        set(value) {
            if (value) {
                errorContainerLl.visibility = View.VISIBLE
                editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_red_fe7b67)
            } else {
                errorContainerLl.visibility = View.GONE
                if(editText.isFocused)
                    editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
                else
                    editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)
            }
        }

    var isVisbleLabel: Boolean
        get() {
            return labelTv.visibility == View.VISIBLE
        }
        set(value) {
            if(value) {
                labelTv.visibility = View.VISIBLE
            } else {
                labelTv.visibility = View.INVISIBLE
            }
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if(enabled) {
            labelTv.setTextColor(ContextCompat.getColor(context,R.color.black_4c4c4c))

            editText.setTextColor(ContextCompat.getColor(context,R.color.black_4c4c4c))
            editText.setHintTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
            if(editText.isFocused)
                editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
            else
                editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)
        } else {
            labelTv.setTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))

            editText.setTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))
            editText.setHintTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))
            editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_e0e0e0)
        }

        editText.isEnabled = enabled
    }


    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_password, this)
        this.editText.onFocusChangeListener = this
        this.editText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                isShownError = false
                listener?.onFieldValueChanged(this@DaebakPasswordField)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                if(sequence.isEmpty()) {
                    editText.textSize = 16f
                } else {
                    editText.textSize = 18f
                }
            }
        })
        checkEye.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            editText.setSelection(editText.length())
        }
        isShownError = false
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if(hasFocus)
            editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
        else
            editText.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)

        listener?.onFieldFocusChanged(this, hasFocus)

        editText.setOnKeyListener { _, keyCode, event ->
            if(event.keyCode == KeyEvent.KEYCODE_ENTER) {
                enterListener?.onEnter(this)
                true
            }
            false
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakPasswordField)
        this.label = array.getString(R.styleable.DaebakPasswordField_DaebakPasswordField_Label)
        editText.hint = array.getString(R.styleable.DaebakPasswordField_DaebakPasswordField_Hint)
        this.isVisbleLabel = array.getBoolean(R.styleable.DaebakPasswordField_DaebakPasswordField_isVisibleLabel, true)
        setMaxLength(array.getInt(R.styleable.DaebakPasswordField_DaebakPasswordField_maxLength, 0))
        array.recycle()
    }

    fun setMaxLength(length:Int) {
        if(length > 0)
            editText.filters = arrayOf( InputFilter.LengthFilter(length) )
    }

    fun showErrorMsg(errorMsg: String) {
        this.errorMsg = errorMsg
        this.isShownError = true
        context.vibrate()
    }
}