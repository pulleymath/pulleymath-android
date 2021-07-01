package com.freewheelin.pulley.activities.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.item_mypage_header.view.*

class HeaderHolder(val view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): HeaderHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mypage_header, parent, false)
            return HeaderHolder(view)
        }
    }
    val headerTitleTv = view.headerTitleTv

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