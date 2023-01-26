package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPriorConceptBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.ui.adapter.PriorConceptAdapter
import com.freewheelin.pulley.revision2023.utils.listeners.PriorConceptClickListener
import com.freewheelin.pulley.revision2023.viewmodel.PriorConceptViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PriorConceptFragment : Fragment() {
    private lateinit var binding: FragmentPriorConceptBinding
    private val viewModel: PriorConceptViewModel by viewModels()

    private val priorConceptAdapter = PriorConceptAdapter {
        val chapterId = it.priorConceptChapterId
        viewModel.createLearningCourseOnStudentId(chapterId) {
            val subjectId = (activity as LearningCourseActivity).viewModel.selectedSubjectId ?: -1
            startActivity(LearningCourseActivity.getIntent(requireContext(), subjectId, it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_prior_concept, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val chapterName = it.getString(CHAPTER_NAME) ?: ""
            val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId ?: -1

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                vm = viewModel
                priorConceptRv.apply {
                    this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = priorConceptAdapter
                }

                viewModel.priorConceptAdapter = priorConceptAdapter
                viewModel.initAdapterItem(chapterId)

                priorConceptBottomNextBtnWrapperLl.setOnClickListener {
                    (activity as LearningCourseActivity).setPagerToCookingFirstPage()
                }
            }

            viewModel.apply {
                priorConcepts.observe(viewLifecycleOwner) {
                    priorConceptAdapter.submitList(it)
                    if (it.isEmpty() && viewModel.afterFetch) { (activity as? LearningCourseActivity)?.setPagerToCookingFirstPage() }
                }
            }

        }
    }

    companion object {
        val CHAPTER_NAME = "CHAPTER_NAME"

        @JvmStatic
        fun newInstance(chapterName: String) =
            PriorConceptFragment().apply {
                arguments = Bundle().apply {
                    putString(CHAPTER_NAME, chapterName)
                }
            }
    }
}