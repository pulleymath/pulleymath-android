package com.freewheelin.pulley.revision2023.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.visibleOrInvisibleIf

class SchoolSpinnerAdapter (context: Context, @LayoutRes private val resId: Int, private val menuList: List<String>)
    : ArrayAdapter<String>(context, resId, menuList) {
    override fun getCount(): Int {
        return menuList.size
    }

    var selectedPosition = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_school_spinner_selected, parent, false)
        if (position < 0 || position >= menuList.size) {
            return view!!
        }
        selectedPosition = position
        val title: TextView? = view?.findViewById(R.id.title)
        title?.text = menuList[position]
        val taillessArrow: ImageView? = view?.findViewById(R.id.arrowIv)

        when (position) {
            0 -> {
                title?.setTextColor(ContextCompat.getColor(context,R.color.purple_300))
                taillessArrow?.setColorFilter(ContextCompat.getColor(context, R.color.purple_300))
            }
            else -> {
                title?.setTextColor(ContextCompat.getColor(context,R.color.white))
                taillessArrow?.setColorFilter(ContextCompat.getColor(context, R.color.white))
            }
        }

        return view!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_school_spinner_dropdown, parent, false)
        if (position < 0 || position >= menuList.size) {
            return view!!
        }

        val content: TextView? = view?.findViewById(R.id.content)
        content?.text = menuList[position]

        val checkIv: ImageView? = view?.findViewById(R.id.checkIv)

        when (position == selectedPosition) {
            true -> {
                content?.setTextColor(ContextCompat.getColor(context,R.color.purple_300))
                checkIv?.visibleOrInvisibleIf(true)
            }
            else -> {
                content?.setTextColor(ContextCompat.getColor(context,R.color.gray_800))
                checkIv?.visibleOrInvisibleIf(false)
            }
        }

        return view!!
    }

    override fun getItem(position: Int): String? {
        return menuList.getOrNull(position)
    }
}