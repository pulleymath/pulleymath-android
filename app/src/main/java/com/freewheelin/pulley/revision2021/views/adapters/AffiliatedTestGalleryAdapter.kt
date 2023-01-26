package com.freewheelin.pulley.revision2021.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewAffiliatedTestGalleryItemBinding
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.utils.listener.AffiliatedGalleryViewClickListener
import com.freewheelin.pulley.revision2021.views.viewholder.AffiliatedTestGalleryViewHolder
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.Type

class AffiliatedTestGalleryAdapter(
    private val itemListener: AffiliatedGalleryViewClickListener
): ListAdapter<AffiliatedTestProblem, RecyclerView.ViewHolder>(DiffCallback<AffiliatedTestProblem>()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewAffiliatedTestGalleryItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.view_affiliated_test_gallery_item, parent, false)
        return AffiliatedTestGalleryViewHolder(binding, itemListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val problem = currentList[holder.absoluteAdapterPosition]
        (holder as? AffiliatedTestGalleryViewHolder)?.bind(problem)

    }
    open fun getIndex(problem: AffiliatedTestProblem): Int {
        return currentList.indexOf(problem)
    }

}