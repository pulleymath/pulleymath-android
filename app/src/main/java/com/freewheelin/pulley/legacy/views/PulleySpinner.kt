package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx

class PulleySpinner : ConstraintLayout {

//    private lateinit var adapter:HintableSpinnerAdapter
    private lateinit var spinner:Spinner
    private lateinit var error:LinearLayout
    private lateinit var errorText:TextView
    var hintable = false

    private var hasHint = false
    var position = -1
        get() { return field}
        set(value) {
            field = value
            spinner.setSelection(value)
        }

    var corner: Int = 0
        set(value) {
            field = value
            when(value) {
                0 -> setSpinnerCornerFlat()
                1 -> setSpinnerCornerRound()
                else -> setSpinnerCornerFlat()
            }
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_hintable_spinner, this)
        initUI()
        setTypedArray(attrs)
    }

    fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PulleySpinner)
        corner = array.getInt(R.styleable.PulleySpinner_corner, 0)
        array.recycle()
    }
    private fun initUI() {
        spinner = findViewById(R.id.spinner)
        spinner.setPadding(1.toPx(),1.toPx(), 1.toPx(),1.toPx())

        error = findViewById(R.id.error)
        errorText = findViewById(R.id.errorText)

        error.visibility = View.GONE
    }

    private fun setSpinnerCornerRound() {
        spinner.setBackgroundResource(R.drawable.bg_white_stroke_gray_500_round)
    }
    private fun setSpinnerCornerFlat() {
        spinner.setBackgroundResource(R.drawable.bg_white_stroke_gray_500)
    }
    fun set(data:List<String>, hint:String?=null, errorMsg:String?=null,callback:(position:Int)->Unit) {
//        adapter = HintableSpinnerAdapter.setSpinner(spinner, data, hint)
        val dataList = if(hint != null) {
            hintable = true
            listOf(hint) + data
        } else data
        val adapter = ArrayAdapter<String>(context, R.layout.item_hintable_spinner, dataList)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                this@PulleySpinner.position = if(hasHint) {
//                    if(position < data.size)
//                        position
//                    else 0
//                } else {
//                    position
//                }
                this@PulleySpinner.position = position
                Log.d("스피너", "position=$position, selectedPosition=${this@PulleySpinner.position}")
                callback(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        errorText.text = errorMsg ?: (hint?:"")
        hasHint = hint != null
    }

    fun initSpinner() {
//        adapter.initSpinner()
        spinner.setSelection(0)
    }

    override fun isSelected(): Boolean {

        return if(hintable) position > 0 else position > -1
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        spinner.isEnabled = enabled
    }
}