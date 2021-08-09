package com.freewheelin.pulley.views.TextViews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.view_sortable_label.view.*

interface SortableListener {
    fun onOrderChanged(view: SortableTextView, order: SortableTextView.Order)
}
class SortableTextView: ConstraintLayout, View.OnClickListener {
    enum class Order{
        ascend,
        descend
    }


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { {}
        setTypedArray(attrs)
    }

    var listener: SortableListener? = null

    var order = Order.ascend
        set(value) {
            field = value
            when(value) {
                Order.ascend -> {
                    arrowIv.setImageResource(R.drawable.ic_arrow_sortable_top)
                }
                Order.descend -> {
                    arrowIv.setImageResource(R.drawable.ic_arrow_sortable_bottom)
                }
            }
            isSelected = isSelected
        }
    var label: String?
        get() = labelTv.text.toString()
        set(value) {
            labelTv.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_sortable_label, this)
        this.setOnClickListener(this)
        isSelected = false

    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array= context.obtainStyledAttributes(attrs, R.styleable.SortableTextView)
        label = array.getString(R.styleable.SortableTextView_label)
        array.recycle()
    }

    override fun onClick(view: View?) {
        if(isSelected == false) {
            isSelected = true

        } else {
            order = if (order == Order.descend) {
                Order.ascend
            } else {
                Order.descend
            }
        }

        listener?.onOrderChanged(this, this.order)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        if(selected) {
            val selectedColor = ContextCompat.getColor(context, R.color.purple_6D6DFF)
            labelTv.setTextColor(selectedColor)
            arrowIv.setColorFilter(selectedColor)
        } else {
            val unselectedTextColor = ContextCompat.getColor(context, R.color.grey_9f9f9f)
            val unselectedImageColor = ContextCompat.getColor(context, R.color.grey_e0e0e0)
            labelTv.setTextColor(unselectedTextColor)
            arrowIv.setColorFilter(unselectedImageColor)
        }
    }
}
