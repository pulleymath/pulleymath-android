package com.freewheelin.pulley.views.editText

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import com.freewheelin.pulley.databinding.ViewInputV2Binding
import com.freewheelin.pulley.utils.pxToSp
import com.freewheelin.pulley.utils.setPaddingTop
import com.freewheelin.pulley.views.ArduousSpinner
import com.freewheelin.pulley.views.ArduousSpinnerListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

interface InputFieldV2Listener {
    fun onFieldFocusChanged(view: InputFieldV2, hasFocus: Boolean)
    fun onFieldValueChanged(view: InputFieldV2) {}
}

interface InputFieldV2EnterListener {
    fun onEnter(view:View)
}

class InputFieldV2: LinearLayout, View.OnFocusChangeListener, ArduousSpinnerListener {

    companion object {
        val DAEBAK_EDITTEXT = 0
        val DAEBAK_SPNNER = 1
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var listener: InputFieldV2Listener? = null
    var enterListener: InputFieldV2EnterListener? = null
    var text: String
        get() {
            return binding.inputEt.text.toString()
        }
        set(value) {
            binding.inputEt.setText(value)
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
            container.visibility = if (value) View.VISIBLE else View.GONE
            inputLayout.error = if (value) " " else null
        }

    var editType = DAEBAK_EDITTEXT
        set(value) {
            field = value

            val edit = findViewById<TextInputLayout>(R.id.inputLayout)
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
        val edit = findViewById<TextInputEditText>(R.id.inputEt)
        val spin = findViewById<ArduousSpinner>(R.id.spinner)

        edit.isEnabled = enabled
        spin.isEnabled = enabled
    }

    var editText: TextInputEditText
//    var editText: EditText
    var inputLayout: TextInputLayout
    var spinner: ArduousSpinner
    val containerCl: ConstraintLayout
    var errorTv: TextView

    var errorContainerLl: LinearLayout
    var binding: ViewInputV2Binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_input_v2, this, true)

    init {
        this.orientation = LinearLayout.VERTICAL

        editText = findViewById(R.id.inputEt)
        inputLayout = findViewById(R.id.inputLayout)
        spinner = findViewById(R.id.spinner)
        containerCl = findViewById(R.id.containerLl)
        errorTv = findViewById(R.id.errorTv)
        errorContainerLl = findViewById(R.id.errorContainerLl)

        editText.onFocusChangeListener = this
        editText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                isShownError = false
                listener?.onFieldValueChanged(this@InputFieldV2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
//                if(sequence.isEmpty()) {
//                    editText.textSize = resources.getDimension(R.dimen.sp16).pxToSp()
//                } else {
//                    editText.textSize = resources.getDimension(R.dimen.sp16).pxToSp()
//                }
            }
        })


        spinner.listener = this
        isShownError = false
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        listener?.onFieldFocusChanged(this, hasFocus)

        editText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    enterListener?.onEnter(this)
                    true
                }
            }
            false
        }
    }

    override fun onListShown() {
        listener?.onFieldFocusChanged(this@InputFieldV2, true)
    }
    override fun onItemClicked(view: ArduousSpinner, position: Int) {
        this.isShownError = false
        listener?.onFieldValueChanged(this@InputFieldV2)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.InputFieldV2)
        inputLayout.hint = array.getString(R.styleable.InputFieldV2_InputFieldV2_Hint)
        editText.nextFocusDownId = array.getInt(R.styleable.InputFieldV2_InputFieldV2_nextFocusDown, -1);

        val showErrorDrawable = array.getBoolean(R.styleable.InputFieldV2_InputFieldV2_ShowErrorIcon, true)
        if (!showErrorDrawable) {
            inputLayout.errorIconDrawable = null
        }


        spinner.defaultStr = inputLayout.hint?.toString()
        this.editType = array.getInt(R.styleable.InputFieldV2_InputFieldV2_editType, DAEBAK_EDITTEXT)
        setMaxLength(array.getInt(R.styleable.InputFieldV2_InputFieldV2_maxLength, 0))
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

        containerCl.setPaddingTop(0)
    }

}