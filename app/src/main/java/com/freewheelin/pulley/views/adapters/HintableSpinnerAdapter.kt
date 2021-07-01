package com.freewheelin.pulley.views.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx

class HintableSpinnerAdapter : ArrayAdapter<String> {

    var existHint = false
    lateinit var spinner:Spinner

    private constructor(context: Context, list:List<String>, hint:String?=null) : super(context, R.layout.item_hintable_spinner) {
        addAll(list)
        hint?.let {
            add(it)
            existHint = true
        }
    }

    companion object {
        fun setSpinner(spinner: Spinner, list:List<String>, hint:String?=null) : HintableSpinnerAdapter{
            val adapter = HintableSpinnerAdapter(spinner.context, list, hint)
            spinner.adapter = adapter
            spinner.setPadding(1.toPx(),1.toPx(), 1.toPx(),1.toPx())

            adapter.spinner = spinner
            adapter.initSpinner()
            return adapter
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val v = super.getView(position, convertView, parent)
        if(existHint)
            if (position == count) {
                (v.findViewById(android.R.id.text1) as TextView).text = ""
                (v.findViewById(android.R.id.text1) as TextView).hint = getItem(count)
            }

        return v
    }

    override fun getCount(): Int {
        return super.getCount() - 1
    }

    fun initSpinner() {
        spinner.setSelection(count)
    }
}