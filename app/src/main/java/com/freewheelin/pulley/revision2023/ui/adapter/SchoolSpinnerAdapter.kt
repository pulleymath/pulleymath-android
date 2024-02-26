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
        selectedPosition = position
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_school_spinner_selected, parent, false)
        val title: TextView? = view?.findViewById(R.id.title)
        title?.text = getItem(position)
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
//        return super.getDropDownView(position, convertView, parent)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_school_spinner_dropdown, parent, false)

        val content: TextView? = view?.findViewById(R.id.content)
        content?.text = getItem(position)

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
// 드롭다운하지 않은 상태의 Spinner 항목의 뷰
//    override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
//        val binding = ItemSpinnerBuyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        binding.tvSpinner.text = menuList[position]
//
//        return binding.root
//    }
//
//    // 드롭다운된 항목들 리스트의 뷰
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val binding = ItemSpinnerBuyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        binding.tvSpinner.text = menuList[position]
//
//        return binding.root
//    }
//
//    override fun getCount() = menuList.size
}