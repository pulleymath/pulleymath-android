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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import com.freewheelin.pulley.databinding.ViewInputPasswordV2Binding
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.utils.debounce
import com.freewheelin.pulley.utils.pxToSp
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
            return binding.errorTv.text.toString()
        }
        set(value) {
            binding.errorTv.text = value
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
            binding.inputLayout.error = if (value) " " else null
        }
    var inputEt: TextInputEditText
        get() {
            return binding.inputEt
        }


    override fun setEnabled(enabled: Boolean) {
        val edit = findViewById<TextInputEditText>(R.id.inputEt)
        edit.isEnabled = enabled
    }
    var binding: ViewInputPasswordV2Binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_input_password_v2, this, true)

    lateinit var viewLifecycleOwner: LifecycleOwner
    lateinit var afterTextChangedDebounce: (Unit?) -> Unit
    fun setAfterTextDebounce(owner: LifecycleOwner) {
        afterTextChangedDebounce = debounce(800L, owner.lifecycleScope) {
            isShownError = false
            listener?.onFieldValueChanged(this@PasswordFieldV2)
        }
        inputEt.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                afterTextChangedDebounce(null)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
//                if(sequence.isEmpty()) {
//                    inputEt.textSize = resources.getDimension(R.dimen.sp16).pxToSp()
//                } else {
//                    inputEt.textSize = resources.getDimension(R.dimen.sp16).pxToSp()
//                }
            }
        })
    }
    init {
        binding.lifecycleOwner = binding.root.findViewTreeLifecycleOwner()

        inputEt = binding.inputEt
        inputEt.onFocusChangeListener = this

        binding.checkEye.setOnCheckedChangeListener { buttonView, isChecked ->
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
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    enterListener?.onEnter(this)
                    true
                }
            }
            false
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PasswordFieldV2)
        binding.inputLayout.hint = array.getString(R.styleable.PasswordFieldV2_PasswordFieldV2_Hint)
        binding.inputEt.nextFocusDownId = array.getInt(R.styleable.InputFieldV2_InputFieldV2_nextFocusDown, -1);
        val showErrorDrawable = array.getBoolean(R.styleable.PasswordFieldV2_PasswordFieldV2_ShowErrorIcon, false)
        if (!showErrorDrawable) {
            binding.inputLayout.errorIconDrawable = null
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
        println()
        this.errorMsg = errorMsg
        this.isShownError = true
        context.vibrate()
    }
}