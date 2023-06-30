package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentMainLessonBinding
import com.freewheelin.pulley.revision2023.ui.view.MainTab

class MainLessonFragment : MainTabFragment() {
    companion object {
        fun newInstance() = MainLessonFragment()
    }

    lateinit var binding: FragmentMainLessonBinding

    override var type: MainTab = MainTab.과외

    override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_lesson, container, false)
        return binding.root
    }
}