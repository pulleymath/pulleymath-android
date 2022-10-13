package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPatternSolutionBinding
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern.PatternSolutionViewModel
import com.freewheelin.pulley.utils.setImageUrlGlide
import com.freewheelin.pulley.utils.toPx
import kotlinx.coroutines.*

class PatternSolutionFragment() : Fragment() {

    companion object {
        fun newInstance(patternQuiz: LCPatternQuiz): PatternSolutionFragment {
            return PatternSolutionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PatternQuizFragment.PARAM_QUIZ, patternQuiz)
                }
            }
        }
    }

    val binding: FragmentPatternSolutionBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_pattern_solution, null, false)
    }
    private lateinit var viewModel: PatternSolutionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PatternSolutionViewModel::class.java)

        arguments?.let {
            val quiz = it.getSerializable(PatternQuizFragment.PARAM_QUIZ) as LCPatternQuiz
            viewModel.initSolution(quiz)
        }

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.solutionUrl.observeOnce(this@PatternSolutionFragment) {
                val iv = ImageView(context)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, 16.toPx(), 0, 0)

                iv.layoutParams = layoutParams
                iv.id = View.generateViewId()
                iv.setImageUrlGlide(it)
                scrollRootLl.addView(iv)

            }
        }
    }

}