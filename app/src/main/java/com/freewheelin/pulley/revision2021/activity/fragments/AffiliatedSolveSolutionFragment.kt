package com.freewheelin.pulley.revision2021.activity.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.VideoPlayerActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedSolveSolutionViewModel
import com.freewheelin.pulley.utils.IntentUtils
import com.freewheelin.pulley.views.DaebakToast

class AffiliatedSolveSolutionFragment : Fragment() {

    val binding: FragmentAffiliatedSolveSolutionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_affiliated_solve_solution, null, false)
    }
    private val viewModel = AffiliatedSolveSolutionViewModel.instance

    companion object {
        var tabSize: Int = 2

        @JvmStatic
        fun newInstance(): AffiliatedSolveSolutionFragment {
            return AffiliatedSolveSolutionFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        viewModel.fetchMedia()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            solutionRv.adapter = VideoSolutionAdapter(viewModel)
        }
    }


    inner class VideoSolutionAdapter(private val viewModel: AffiliatedSolveSolutionViewModel): ListAdapter<AffiliatedSolution, RecyclerView.ViewHolder>(DiffCallback<AffiliatedSolution>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return VideoSolutionHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_video_solution_all, parent, false))
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as AffiliatedSolveSolutionFragment.VideoSolutionHolder).bind(getItem(position))
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    inner class VideoSolutionHolder(private val binding: ItemVideoSolutionAllBinding): RecyclerView.ViewHolder(binding.root), SolutionItemClickListener {
        fun bind(item: AffiliatedSolution) {
            binding.let {
                it.item = item
                it.vm = viewModel
                it.listener = this@VideoSolutionHolder
                setAllContainerViewGone(it)

                when (item.itemType) {
                    AffiliatedSolution.ItemType.textHeader, AffiliatedSolution.ItemType.videoTextHeader -> {
                        it.textHeaderContainer.visibility = View.VISIBLE
                    }
                    AffiliatedSolution.ItemType.pdfItem -> {
                        it.pdfItemContainer.visibility = View.VISIBLE
                    }
                    AffiliatedSolution.ItemType.videoGroupHeader -> {
                        it.videoGroupContainer.visibility = View.VISIBLE
                    }
                    AffiliatedSolution.ItemType.videoItem -> {
                        it.videoItemContainer.visibility = View.VISIBLE
                    }
                    AffiliatedSolution.ItemType.videoFooter -> {
                        it.videoFooterContainer.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
        }

        private fun setAllContainerViewGone(view: ItemVideoSolutionAllBinding) {
            view.apply {
                textHeaderContainer.visibility = View.GONE
                pdfItemContainer.visibility = View.GONE
                videoGroupContainer.visibility = View.GONE
                videoItemContainer.visibility = View.GONE
                videoFooterContainer.visibility = View.GONE
                emptyContainer.visibility = View.GONE
            }
        }

        override fun onItemClick(item: AffiliatedSolution) {
            when (item.itemType) {
                AffiliatedSolution.ItemType.textHeader, AffiliatedSolution.ItemType.videoTextHeader -> {}
                AffiliatedSolution.ItemType.pdfItem -> {
                    viewModel.makeMediaLog(item)
                    IntentUtils.openWebLink(requireContext(), item.fileurl, requireContext().packageManager)
                }
                AffiliatedSolution.ItemType.videoGroupHeader -> {
                    item.isSelected = !item.isSelected
                    viewModel.solutionFilter(item.id, item.group_no)
                }
                AffiliatedSolution.ItemType.videoItem -> {
                    val intent = VideoPlayerActivity.getIntent(requireContext(), item.fileurl, item)
                    startActivity(intent)
                }
                AffiliatedSolution.ItemType.videoFooter -> { }
                else -> {}
            }
        }
    }

    interface SolutionItemClickListener {
        fun onItemClick(item: AffiliatedSolution)
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}
