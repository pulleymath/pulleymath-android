package com.freewheelin.pulley.views.EditText

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import com.freewheelin.pulley.utils.setPaddingTop
import com.freewheelin.pulley.views.ArduousSpinner
import com.freewheelin.pulley.views.ArduousSpinnerListener

interface DaebakInputFieldListener {
    fun onFieldFocusChanged(view: DaebakInputField, hasFocus: Boolean)
    fun onFieldValueChanged(view: DaebakInputField) {}
}

interface DaebakInputEnterListener {
    fun onEnter(view:View)
}

class DaebakInputField: LinearLayout, View.OnFocusChangeListener, ArduousSpinnerListener {

    companion object {
        val DAEBAK_EDITTEXT = 0
        val DAEBAK_SPNNER = 1
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var listener: DaebakInputFieldListener? = null
    var enterListener: DaebakInputEnterListener? = null

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
                label.visibility = View.GONE
            }
        }

    var editType = DAEBAK_EDITTEXT
        set(value) {
            field = value

            val edit = findViewById<EditText>(R.id.editText)
            val spin = findViewById<ArduousSpinner>(R.id.spinner)

            when(value) {
                DAEBAK_EDITTEXT -> {
                    edit.visibility = View.VISIBLE
                    spin.visibility = View.GONE
                }
                DAEBAK_SPNNER -> {
                    edit.visibility = View.GONE
                    spin.visibility = View.VISIBLE
                }
            }
        }

    override fun setEnabled(enabled: Boolean) {
//        super.setEnabled(enabled)

        val edit = findViewById<EditText>(R.id.editText)
        val label = findViewById<TextView>(R.id.labelTv)
        val spin = findViewById<ArduousSpinner>(R.id.spinner)

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
        spin.isEnabled = enabled
    }

    var editText: EditText
    var labelTv: TextView
    var spinner: ArduousSpinner
    val containerCl: LinearLayout
    var errorTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_daebak, this)
        this.orientation = LinearLayout.VERTICAL

        editText = findViewById(R.id.editText)
        labelTv = findViewById(R.id.labelTv)
        spinner = findViewById(R.id.spinner)
        containerCl = findViewById(R.id.containerLl)
        errorTv = findViewById(R.id.errorTv)

        editText.onFocusChangeListener = this
        editText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                isShownError = false
                listener?.onFieldValueChanged(this@DaebakInputField)
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
        spinner.listener = this
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

    override fun onListShown() {
        listener?.onFieldFocusChanged(this@DaebakInputField, true)
    }
    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        this.isShownError = false
        listener?.onFieldValueChanged(this@DaebakInputField)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakInputField)
        this.label = array.getString(R.styleable.DaebakInputField_DaebakInputField_Label)
        editText.hint = array.getString(R.styleable.DaebakInputField_DaebakInputField_Hint)
        spinner.defaultStr = editText.hint?.toString()
        this.isVisbleLabel = array.getBoolean(R.styleable.DaebakInputField_DaebakInputField_isVisibleLabel, true)
        this.editType = array.getInt(R.styleable.DaebakInputField_DaebakInputField_editType, DAEBAK_EDITTEXT)
        setMaxLength(array.getInt(R.styleable.DaebakInputField_DaebakInputField_maxLength, 0))
        array.recycle()

    }

    fun setMaxLength(length:Int) {
        if(length > 0) {
            editText.filters = arrayOf(InputFilter.LengthFilter(length))
        }
    }

    fun showErrorMsg(errorMsg: String) {
        this.errorMsg = errorMsg
        this.isShownError = true
        context.vibrate()
    }

    fun goneLabel() {

        labelTv.visibility = View.GONE
        containerCl.setPaddingTop(0)
    }

}