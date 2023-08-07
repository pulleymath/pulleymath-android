package com.freewheelin.pulley.legacy.activities.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemMypageHeaderBinding

class HeaderHolder(val binding: ItemMypageHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): HeaderHolder {
            val binding: ItemMypageHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mypage_header, parent, false)
            return HeaderHolder(binding)
        }
    }
    val headerTitleTv = binding.headerTitleTv

    fun set(category: SettingCategory) {
        this.headerTitleTv.text = category.title
        if (category.title.isEmpty())
            headerTitleTv.visibility = View.GONE
        else
            headerTitleTv.visibility = View.VISIBLE
    }

    fun set(title: String) {
        headerTitleTv.text = title
    }
}