package com.freewheelin.pulley.revision2021.activity.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.VideoPlayerActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveSolutionViewModel
import com.freewheelin.pulley.legacy.utils.IntentUtils

class AssessmentSolveSolutionFragment : Fragment() {

    val binding: FragmentAssessmentSolveSolutionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_assessment_solve_solution, null, false)
    }
    private val viewModel = AssessmentSolveSolutionViewModel.instance

    companion object {
        var tabSize: Int = 2

        @JvmStatic
        fun newInstance(): AssessmentSolveSolutionFragment {
            return AssessmentSolveSolutionFragment()
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


    inner class VideoSolutionAdapter(private val viewModel: AssessmentSolveSolutionViewModel): ListAdapter<AssessmentSolution, RecyclerView.ViewHolder>(DiffCallback<AssessmentSolution>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return VideoSolutionHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_video_solution_all, parent, false))
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as AssessmentSolveSolutionFragment.VideoSolutionHolder).bind(getItem(position))
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    inner class VideoSolutionHolder(private val binding: ItemVideoSolutionAllBinding): RecyclerView.ViewHolder(binding.root), SolutionItemClickListener {
        fun bind(item: AssessmentSolution) {
            binding.let {
                it.item = item
                it.vm = viewModel
                it.listener = this@VideoSolutionHolder
                setAllContainerViewGone(it)

                when (item.itemType) {
                    AssessmentSolution.ItemType.textHeader, AssessmentSolution.ItemType.videoTextHeader -> {
                        it.textHeaderContainer.visibility = View.VISIBLE
                    }
                    AssessmentSolution.ItemType.pdfItem -> {
                        it.pdfItemContainer.visibility = View.VISIBLE
                    }
                    AssessmentSolution.ItemType.videoGroupHeader -> {
                        it.videoGroupContainer.visibility = View.VISIBLE
                    }
                    AssessmentSolution.ItemType.videoItem -> {
                        it.videoItemContainer.visibility = View.VISIBLE
                    }
                    AssessmentSolution.ItemType.videoFooter -> {
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

        override fun onItemClick(item: AssessmentSolution) {
            when (item.itemType) {
                AssessmentSolution.ItemType.textHeader, AssessmentSolution.ItemType.videoTextHeader -> {}
                AssessmentSolution.ItemType.pdfItem -> {
                    viewModel.makeMediaLog(item)
                    IntentUtils.openWebLink(requireContext(), item.fileurl, requireContext().packageManager)
                }
                AssessmentSolution.ItemType.videoGroupHeader -> {
                    item.isSelected = !item.isSelected
                    viewModel.solutionFilter(item.id, item.group_no)
                }
                AssessmentSolution.ItemType.videoItem -> {
                    val intent = VideoPlayerActivity.getIntent(requireContext(), item.fileurl, item)
                    startActivity(intent)
                }
                AssessmentSolution.ItemType.videoFooter -> { }
                else -> {}
            }
        }
    }

    interface SolutionItemClickListener {
        fun onItemClick(item: AssessmentSolution)
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}
