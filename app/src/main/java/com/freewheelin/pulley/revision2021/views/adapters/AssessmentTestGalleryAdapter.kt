package com.freewheelin.pulley.revision2021.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewAssessmentGalleryItemBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.utils.listener.AssessmentGalleryViewClickListener
import com.freewheelin.pulley.revision2021.views.viewholder.AssessmentGalleryViewHolder

class AssessmentTestGalleryAdapter(
    private val itemListener: AssessmentGalleryViewClickListener
): ListAdapter<AssessmentProblem, RecyclerView.ViewHolder>(DiffCallback<AssessmentProblem>()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewAssessmentGalleryItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.view_assessment_gallery_item, parent, false)
        return AssessmentGalleryViewHolder(binding, itemListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val problem = currentList[holder.absoluteAdapterPosition]
        (holder as? AssessmentGalleryViewHolder)?.bind(problem)

    }
    open fun getIndex(problem: AssessmentProblem): Int {
        return currentList.indexOf(problem)
    }

}