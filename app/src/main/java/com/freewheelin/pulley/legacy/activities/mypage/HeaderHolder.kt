package com.freewheelin.pulley.legacy.activities.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemMypageHeaderBinding
import com.freewheelin.pulley.legacy.utils.setImageURL
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2023.viewmodel.MyMainPageFragViewModel
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath

class HeaderHolder(val binding: ItemMypageHeaderBinding, val viewModel: MyMainPageFragViewModel) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup, viewModel: MyMainPageFragViewModel): HeaderHolder {
            val binding: ItemMypageHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_mypage_header, parent, false)
            return HeaderHolder(binding, viewModel)
        }
    }
    val headerTitleTv = binding.headerTitleTv

    fun set(category: SettingCategory, indexPath: IndexPath) {
        this.headerTitleTv.text = category.title
        if (category.title.isEmpty())
            headerTitleTv.visibility = View.GONE
        else
            headerTitleTv.visibility = View.VISIBLE

        binding.profileLl.visibleIf(category.isPrivate)
        viewModel.mainProfileV4.value?.let { profile ->
            binding.profilePullingIv.setImageURL(profile.profileImageUrl)
        }
        viewModel.user.value?.let { user ->
            binding.studentNameTv.text = user.fullName
            binding.accountEmailTv.text = user.accountEmail
        }
    }

    fun set(title: String) {
        headerTitleTv.text = title
    }
}