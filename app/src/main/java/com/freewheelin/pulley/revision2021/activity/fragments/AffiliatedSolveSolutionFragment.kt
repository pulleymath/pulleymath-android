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
import com.freewheelin.pulley.revision2021.activity.PdfListActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.AffiliatedOpenProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestAnswer2
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.Pdf
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedSolveSolutionViewModel
import com.freewheelin.pulley.revision2021.viewmodel.PdfViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AffiliatedSolveSolutionFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    val binding: FragmentAffiliatedSolveSolutionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_affiliated_solve_solution, null, false)
    }
    private val viewModel = AffiliatedSolveSolutionViewModel.instance

    companion object {
        @JvmStatic
        fun newInstance(): AffiliatedSolveSolutionFragment {
            return AffiliatedSolveSolutionFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

            val adapter = VideoLectureAdapter(viewModel)
            videoRv.adapter = adapter
            println("tpehf, viewmodel workbook_id :${viewModel.currentProblem.value?.workbook_id}")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchUnivTestGroup {

        }
    }

    inner class VideoLectureAdapter(private val viewModel: AffiliatedSolveSolutionViewModel): ListAdapter<AffiliatedTestAnswer2, RecyclerView.ViewHolder>(DiffCallback<AffiliatedTestAnswer2>()) {
        private val typeHeader = 0
        private val typeItem = 1
//        lateinit var headerBinding: HeaderAffiliatedSolutionVideoBinding

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    VideoHeaderHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.header_affiliated_solution_video, parent, false))
                }
                else -> VideoItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_affiliated_solution_video, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as AffiliatedSolveSolutionFragment.VideoHeaderHolder).bind(getItem(position))
                else -> (holder as AffiliatedSolveSolutionFragment.VideoItemHolder).bind(getItem(position))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(position % 2) {
                0 -> typeHeader
                else -> typeItem
            }
        }
    }

    inner class VideoHeaderHolder(private val binding: HeaderAffiliatedSolutionVideoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AffiliatedTestAnswer2) {

        }
    }
    inner class VideoItemHolder(private val binding: ItemAffiliatedSolutionVideoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AffiliatedTestAnswer2) {

        }
    }

}