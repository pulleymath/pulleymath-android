package com.freewheelin.pulley.revision2021.activity.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.solve.ProblemGestureListener
import com.freewheelin.pulley.legacy.activities.solve.SolveGestures
import com.freewheelin.pulley.legacy.bases.DensityLevel
import com.freewheelin.pulley.legacy.bases.densityLevel
import com.freewheelin.pulley.databinding.FragmentAssessmentSolveConceptBinding
import com.freewheelin.pulley.revision2021.activity.AssessmentSolveActivity
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveConceptViewModel
import com.freewheelin.pulley.legacy.utils.toPx

class AssessmentSolveConceptFragment : Fragment(), ProblemGestureListener {

    val binding: FragmentAssessmentSolveConceptBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_assessment_solve_concept, null, false)
    }
    private val viewModel = AssessmentSolveConceptViewModel.instance
    var solutionGesture: SolveGestures? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val parentActivity = activity as AssessmentSolveActivity
        val imageWidth = when(parentActivity.densityLevel) {
            DensityLevel.Low -> parentActivity.screenWidth / 2
            DensityLevel.High -> (parentActivity.screenWidth / 2.7).toInt()
            else -> (500.toPx()).toInt()
        }

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            solutionGesture = SolveGestures(requireContext(), solutionIv, solutionMemoView, problemInfoContainer)
            solutionGesture?.listener = this@AssessmentSolveConceptFragment
            conceptContainer.setOnTouchListener(solutionGesture)
            gestureInit()

            solutionIv.maxWidth = imageWidth
            solutionMemoView.layoutParams.width = parentActivity.screenWidth
        }
    }

    fun setTouchListener(isRelease: Boolean) {
        binding.apply {
            val listener = if (isRelease) null else solutionGesture
            conceptContainer.setOnTouchListener(listener)
        }
    }
    fun setConceptContainerBlock(willBlock: Boolean) {
        binding.apply {
            conceptContainer.isBlock = willBlock
        }
    }
    fun gestureInit() {
        solutionGesture?.init()
    }
    companion object {
        fun newInstance(): AssessmentSolveConceptFragment {
            return AssessmentSolveConceptFragment()
        }
    }

    override fun onLeftSwipe() {}

    override fun onRightSwipe() {}
    override fun onGestureTouch() {
        println("AssessmentSolveConceptFragment onGestureTouch!")
    }
}