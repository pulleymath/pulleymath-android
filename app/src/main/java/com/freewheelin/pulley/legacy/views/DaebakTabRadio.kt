package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.legacy.utils.toPx

interface DabakTabRadioListener {
    fun onTabSelected(radio: DaebakTabRadio, index: Int)
}

class DaebakTabRadio: LinearLayout, View.OnClickListener {
    var labels: List<String>? = null
        set(value) {
            field = value
            configure(value)
            if(value != null && value.size > 0)
                selectedIndex = 0
        }

    var tabSize: Int = 144.toPx()

    var tabs: List<TabButton>? = null
    var selectedIndex = 0
        set(value) {
            field = value
            tabs?.forEach { it.isSelected = false }
            tabs?.getOrNull(value)?.isSelected = true
        }

    var textSize = resources.getDimension(R.dimen.sp16)
        set(value) {
            field = value
            tabs?.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_PX, value) }
        }

    var radius = 5f
        set(value) {
            field = value
            if(value == 2f)
                this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round_2)
            else
                this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round)
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var selectedTextColor: Int = ContextCompat.getColor(context, R.color.white)
    var unSelectedTextColor: Int = ContextCompat.getColor(context, R.color.gray_800)
    var selectedBackgrounColor: Int = ContextCompat.getColor(context, R.color.gray_800)
    var unselectedBackgroundColor: Int = Color.TRANSPARENT
    var defaultTypeface = Theme.bold(context)
    var selectedTypeface = Theme.bold(context)

    var listener: DabakTabRadioListener? = null

    init {
        this.orientation = HORIZONTAL
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_800_round)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array= context.obtainStyledAttributes(attrs, R.styleable.DaebakTabRadio)
        tabSize = array.getDimensionPixelSize(R.styleable.DaebakTabRadio_tabWidth, tabSize)
        array.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val clipPath = Path()
        clipPath.addRoundRect(RectF(canvas.clipBounds), radius.toPx(), radius.toPx(), Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }

    private fun configure(labels: List<String>?) {
        this.removeAllViews()
        val tabs: ArrayList<TabButton> = ArrayList()
        labels?: return

        for(i in 0 until labels.size) {
            val button = makeTabView(labels[i], i)
            tabs.add(button)
            addView(button)

            button.layoutParams.height = LayoutParams.MATCH_PARENT
            if(tabSize > 0) {
                button.layoutParams.width = tabSize
            } else {
                val padding = resources.getDimension(R.dimen.sp16)
                button.setPadding(padding.toInt(), 0, padding.toInt(), 0)
                button.layoutParams.width = LayoutParams.WRAP_CONTENT
            }

            if(i < labels.size - 1)
                addBorderView()
        }
        this.tabs = tabs
    }

    private fun makeTabView(text: String, index: Int): TabButton {
        val button = TabButton(context, text, index)
        button.setOnClickListener(this)
        return button
    }

    private fun addBorderView() {
        val view = View(context)
        addView(view)
        view.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        view.layoutParams.width = 1.toPx()
        view.background = ContextCompat.getDrawable(context, R.color.gray_800)
    }


    override fun onClick(view: View) {
        val tabButton = view as TabButton
        selectedIndex = tabs!!.indexOf(tabButton)
        listener?.onTabSelected(this, tabButton.index)
    }

    inner class TabButton(context:Context, text: String, val index: Int): androidx.appcompat.widget.AppCompatButton(context) {
        init {
            background = null
            this.text = text

            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp16))
            setTextColor(unSelectedTextColor)
        }

        override fun setSelected(selected: Boolean) {
            super.setSelected(selected)

            if(selected) {
                setTypeface(selectedTypeface)
                setTextColor(selectedTextColor)
                setBackgroundColor(selectedBackgrounColor)
            } else {
                setTypeface(defaultTypeface)
                setTextColor(unSelectedTextColor)
                setBackgroundColor(unselectedBackgroundColor)
            }
        }

    }
}