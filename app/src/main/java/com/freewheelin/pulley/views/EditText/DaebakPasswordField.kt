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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import kotlinx.android.synthetic.main.view_input_daebak.view.*
import kotlinx.android.synthetic.main.view_input_password.view.*
import kotlinx.android.synthetic.main.view_input_password.view.editText
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

    var showEye: Boolean
        get() {
            val checkEye = findViewById<TextView>(R.id.checkEye)
            return checkEye.visibility == View.VISIBLE
        }
        set(value) {
            val checkEye = findViewById<TextView>(R.id.checkEye)
            if(value) {
                checkEye.visibility = View.VISIBLE
            } else {
                checkEye.visibility = View.GONE
            }
        }

    var isShownError: Boolean
         get() {
             val container = findViewById<LinearLayout>(R.id.errorContainerLl)
             return container.visibility == View.VISIBLE
         }
        set(value) {
            val container = findViewById<LinearLayout>(R.id.errorContainerLl)
            val edit = findViewById<EditText>(R.id.editText)

            if (value) {
                container.visibility = View.VISIBLE
                edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_red_fe7b67)
            } else {
                container.visibility = View.GONE
                if(edit.isFocused)
                    edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
                else
                    edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)
            }
        }

    var isVisbleLabel: Boolean
        get() {
            val label = findViewById<TextView>(R.id.labelTv)
            return label.visibility == View.VISIBLE
        }
        set(value) {
            val label = findViewById<TextView>(R.id.labelTv)
            if(value) {
                label.visibility = View.VISIBLE
            } else {
                label.visibility = View.INVISIBLE
            }
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        val container = findViewById<LinearLayout>(R.id.errorContainerLl)
        val edit = findViewById<EditText>(R.id.editText)
        val label = findViewById<TextView>(R.id.labelTv)

        if(enabled) {
            label.setTextColor(ContextCompat.getColor(context,R.color.black_4c4c4c))

            edit.setTextColor(ContextCompat.getColor(context,R.color.black_4c4c4c))
            edit.setHintTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
            if(edit.isFocused)
                edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
            else
                edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)
        } else {
            label.setTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))

            edit.setTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))
            edit.setHintTextColor(ContextCompat.getColor(context,R.color.grey_e0e0e0))
            edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_e0e0e0)
        }

        edit.isEnabled = enabled
    }


    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_password, this)

        val edit = findViewById<EditText>(R.id.editText)
        val check = findViewById<CheckBox>(R.id.checkEye)

        edit.onFocusChangeListener = this
        edit.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                isShownError = false
                listener?.onFieldValueChanged(this@DaebakPasswordField)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                if(sequence.isEmpty()) {
                    edit.textSize = 16f
                } else {
                    edit.textSize = 18f
                }
            }
        })
        check.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                edit.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                edit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            edit.setSelection(edit.length())
        }
        isShownError = false
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        val edit = findViewById<EditText>(R.id.editText)

        if(hasFocus)
            edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_purple_6d6dff)
        else
            edit.background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_c0c0c0)

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
        val edit = findViewById<EditText>(R.id.editText)

        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakPasswordField)
        this.label = array.getString(R.styleable.DaebakPasswordField_DaebakPasswordField_Label)
        edit.hint = array.getString(R.styleable.DaebakPasswordField_DaebakPasswordField_Hint)
        this.isVisbleLabel = array.getBoolean(R.styleable.DaebakPasswordField_DaebakPasswordField_isVisibleLabel, true)
        this.showEye = array.getBoolean(R.styleable.DaebakPasswordField_showEye, true)
        setMaxLength(array.getInt(R.styleable.DaebakPasswordField_DaebakPasswordField_maxLength, 0))
        array.recycle()
    }

    fun setMaxLength(length:Int) {
        val edit = findViewById<EditText>(R.id.editText)

        if(length > 0)
            edit.filters = arrayOf( InputFilter.LengthFilter(length) )
    }

    fun showErrorMsg(errorMsg: String) {
        this.errorMsg = errorMsg
        this.isShownError = true
        context.vibrate()
    }
}