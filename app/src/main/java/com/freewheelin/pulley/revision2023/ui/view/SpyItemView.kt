package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2023.ui.dialogs.SpyItem

class SpyItemView: LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    private var title: TextView
    private var switch: Switch

    init {
        LayoutInflater.from(context).inflate(R.layout.view_spy_item, this)
        title = findViewById(R.id.titleTv)
        switch = findViewById(R.id.onOffSwitch)
    }

    fun setTitle(value: String) {
        title.text = value
    }
    fun setSwitch(item: SpyItem, initChecked: Boolean, listener: (SpyItem, Boolean) -> Unit) {
        switch.visibleIf(item.isSwitch)
        if (item.isSwitch) {
            switch.isChecked = initChecked
            switch.setOnCheckedChangeListener { compoundButton, b ->
                listener(item, b)
            }
        }
    }
    fun setOnClickListener(item: SpyItem, listener: (SpyItem) -> Unit) {
        setOnClickListener {
            listener(item)
        }
    }
}