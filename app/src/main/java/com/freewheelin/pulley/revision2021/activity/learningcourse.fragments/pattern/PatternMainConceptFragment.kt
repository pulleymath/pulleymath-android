package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentPatternMainConceptBinding
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern.PatternMainConceptViewModel

class PatternMainConceptFragment() : Fragment() {
    constructor(patternQuiz: LCPatternQuiz, index: Int): this() {
        this.patternQuiz = patternQuiz
        this.index = index
    }
    var patternQuiz: LCPatternQuiz? = null
    var index = -1
    companion object {
        fun newInstance(patternQuiz: LCPatternQuiz, index: Int) = PatternMainConceptFragment(patternQuiz, index)
    }

    val binding: FragmentPatternMainConceptBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_pattern_main_concept, null, false)
    }
    private lateinit var viewModel: PatternMainConceptViewModel


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
        viewModel = ViewModelProvider(this).get(PatternMainConceptViewModel::class.java)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            testTv.text = "$index + 하하하 "

        }

    }
}