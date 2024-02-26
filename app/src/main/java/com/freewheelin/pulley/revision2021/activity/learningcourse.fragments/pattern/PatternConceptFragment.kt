package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern.PatternConceptViewModel
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPatternConceptBinding
import com.freewheelin.pulley.revision2021.model.LCPatternConcept
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.legacy.utils.setImageUrlGlide
import com.freewheelin.pulley.legacy.utils.toPx

class PatternConceptFragment : Fragment() {

    companion  object {
        val PARAM_QUIZ = "QUIZ"
        fun newInstance(patternQuiz: LCPatternQuiz): PatternConceptFragment {
            return PatternConceptFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PARAM_QUIZ, patternQuiz)
                }
            }
        }
    }

    val binding: FragmentPatternConceptBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_pattern_concept, null, false)
    }
    private lateinit var viewModel: PatternConceptViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PatternConceptViewModel::class.java)

        arguments?.let {
            val quiz = it.getSerializable(PatternQuizFragment.PARAM_QUIZ) as LCPatternQuiz
            viewModel.initQuiz(quiz)

            binding.apply {
                addBaseConcept()
                addReleatedConcepts()
            }

        }

    }
    private fun addBaseConcept() {
        binding.apply {
            val conceptsBase = viewModel.patternQuiz.value?.concepts?.filter { it.conceptTypeEnum == LCPatternConcept.ConceptType.base }

            if (conceptsBase?.isEmpty() == true) {
                val tv = TextView(context).also { tv ->
                    val tvLp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    tvLp.setMargins(0, 24.toPx(), 0, 0)
                    tv.setPadding(0, 50.toPx(), 0, 50.toPx())
                    tv.id = View.generateViewId()
                    tv.text = "해당 문제는 유형별 개념을 제공하지 않아요."
                    tv.setTextAppearance(R.style.c1)
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600))
                    tv.gravity = Gravity.CENTER
                }

                scrollRootLl.addView(tv)
            } else {
                conceptsBase?.forEach {
                    val iv = ImageView(context)
                    val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(0, 16.toPx(), 0, 0)

                    iv.layoutParams = layoutParams
                    iv.id = View.generateViewId()
                    iv.setImageUrlGlide(it.conceptImageUrl)
                    scrollRootLl.addView(iv)
                }
            }
        }
    }

    private fun addReleatedConcepts() {
        binding.apply {
            val relatedConcepts = viewModel.patternQuiz.value?.concepts?.filter {
                it.conceptTypeEnum == LCPatternConcept.ConceptType.related
            }

            addReleatedTextView()

            if (relatedConcepts?.isEmpty() == true) {
                val tv = TextView(context).also { tv ->
                    val tvLp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    tvLp.setMargins(0, 24.toPx(), 0, 0)
                    tv.setPadding(0, 50.toPx(), 0, 50.toPx())
                    tv.id = View.generateViewId()
                    tv.text = "해당 문제는 연관 개념을 제공하지 않아요."
                    tv.setTextAppearance(R.style.c1)
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600))
                    tv.gravity = Gravity.CENTER
                }

                scrollRootLl.addView(tv)
            } else {
                relatedConcepts?.forEach {
                    val iv = ImageView(context)
                    val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(0, 16.toPx(), 0, 0)

                    iv.layoutParams = layoutParams
                    iv.id = View.generateViewId()
                    iv.setImageUrlGlide(it.conceptImageUrl)
                    scrollRootLl.addView(iv)
                }
            }
        }
    }
    fun addReleatedTextView() {
        binding.apply {
            val tv = TextView(context).apply {
                val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(0, 36.toPx(), 0, 0)
                layoutParams = lp
                id = View.generateViewId()
                setTextAppearance(R.style.mo_h2)
                text = "연관 개념"
            }
            scrollRootLl.addView(tv)
        }
    }
}