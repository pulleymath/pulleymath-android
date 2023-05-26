package com.freewheelin.pulley.views.charts

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemChartOnebarBinding
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.setPaddingLeft
import com.freewheelin.pulley.utils.setPaddingRight
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.bars.VerticalBar

class OneBarChart(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    class BarData(val title: String, val value: Int)

    private var data: List<BarData> = ArrayList()
    var isHighlightMaxAndMin: Boolean = false
        set(value) {
            field = value
            this.adapter?.notifyDataSetChanged()
        }
    private var max: Int = 0
    private var min: Int = 0

    private val chartWidth: Int
        get() = DisplayUtils.getScreenWidth(context) - 64.toPx()

    init {
        initUI()
    }

    fun initUI() {
        this.setPaddingLeft(70.toPx())
        this.setPaddingRight(70.toPx())
        clipToPadding = false
        adapter = OneBarChartAdapter()
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false).apply {
            stackFromEnd = true
        }
    }

    fun setData(data: List<BarData>) {
        if (data.isEmpty()) {
            this.data = data
            this.adapter?.notifyDataSetChanged()
        } else {
            max = data.filter { it.value != 0 }.maxByOrNull { it.value }?.value ?: -1
            min = data.filter { it.value != 0 }.minByOrNull { it.value }?.value ?: -1
            this.data = data
            this.adapter = OneBarChartAdapter()
        }
    }



    inner class OneBarChartAdapter: RecyclerView.Adapter<OneBarHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneBarHolder {
            val itemBinding: ItemChartOnebarBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_chart_onebar, parent, false)
            return OneBarHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return data.size

        }

        override fun onBindViewHolder(holder: OneBarHolder, position: Int) {
            val width = maxOf((this@OneBarChart.chartWidth - 140.toPx()) / itemCount, 152.toPx())
            holder.itemView.layoutParams.width = width
            val data = data[position]
            holder.set(max, data)
            holder.bar.color = getProperBarColor(data.value)
            holder.textView.setTextColor(getProperTextColor(data.value))
        }

        private fun getProperBarColor(value: Int): Int {
            return if(!isHighlightMaxAndMin)
                ContextCompat.getColor(context, R.color.gray_400)
            else {
                if(max == value && itemCount > 1)
                    ContextCompat.getColor(context, R.color.blue_400)
                else if(min == value && itemCount > 1)
                    ContextCompat.getColor(context, R.color.red_300)
                else
                    ContextCompat.getColor(context, R.color.gray_400)
            }
        }

        private fun getProperTextColor(value: Int): Int {
            return if(!isHighlightMaxAndMin)
                ContextCompat.getColor(context, R.color.gray_800)
            else {
                if(max == value && itemCount != 1)
                    ContextCompat.getColor(context, R.color.blue_400)
                else if(min == value && itemCount != 1)
                    ContextCompat.getColor(context, R.color.red_300)
                else
                    ContextCompat.getColor(context, R.color.gray_800)
            }
        }

    }
}

class OneBarHolder(val itemBinding: ItemChartOnebarBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val bar: VerticalBar = itemBinding.bar
    val textView: TextView = itemBinding.tv

    fun set(max: Int, data: OneBarChart.BarData) {
        val value = data.value.toFloat() / max.toFloat()
        val animator = ValueAnimator.ofFloat(0f, value)
        animator.addUpdateListener {
            val segValue = it.animatedValue as Float
            bar.value = segValue
        }
        animator.duration = 300
        animator.start()

        textView.text = data.title
        bar.label = data.value.toString()
    }

}