package com.freewheelin.pulley.legacy.views.v2

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx

class SpinnerV2: ConstraintLayout {

    private lateinit var spinner:Spinner
    private var hintable = false

    var position = -1
        get() { return field}
        set(value) {
            field = value
            spinner.setSelection(value)
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_spinner_v2, this)
        initUI()
    }

    private fun initUI() {
        spinner = findViewById(R.id.spinner)
        spinner.setPadding(1.toPx(),1.toPx(), 1.toPx(),1.toPx())
    }

    fun set(data:List<String>, hint:String?=null,callback:(position:Int)->Unit) {

        val dataList = if(hint != null) {
            hintable = true
            listOf(hint) + data
        } else data
        val adapter = ArrayAdapter<String>(context, R.layout.item_hintable_spinner, dataList)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                this@SpinnerV2.position = position
                Log.d("스피너", "position=$position, selectedPosition=${this@SpinnerV2.position}")
                callback(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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