package com.freewheelin.pulley.revision2023.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPatternMapBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2023.ui.adapter.LCPatternMapListAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.PriorConceptAdapter
import com.freewheelin.pulley.revision2023.viewmodel.PatternMapViewModel
import com.freewheelin.pulley.revision2023.viewmodel.PriorConceptViewModel

class PatternMapFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance(courseId: Int) = PatternMapFragment().apply {
            arguments = Bundle().apply {
                putInt(LearningCourseActivity.COURSE_DETAIL_ID, courseId)
            }
        }
    }
    private val patternMapAdapter by lazy {
        LCPatternMapListAdapter(viewModel) {
            (activity as LearningCourseActivity).setPagerToPatternId(it.patternId)
        }
    }

    lateinit var binding: FragmentPatternMapBinding
    private val viewModel: PatternMapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pattern_map, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            viewModel.adapter = patternMapAdapter

            configureRv()
            fetch()

            nextStepBtnCl.setOnClickListener {
                (activity as LearningCourseActivity).setPagerToWrongNoteMap()
            }
        }
        viewModel.apply {
            lcPatternMaps.observe(viewLifecycleOwner) {
                patternMapAdapter.submitList(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId ?: -1
        viewModel.collectAllPatternMaps(chapterId)
    }

    private fun fetch() {
        val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId ?: -1
        viewModel.initAdapterItem(chapterId)
    }
    private fun configureRv() {
        binding.apply {
            patternMapRv.apply {
                adapter = patternMapAdapter
                this.layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false).also {
                    it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (position) {
                                0 -> 3
                                else -> 1
                            }
                        }
                    }
                }
            }
        }
    }

}