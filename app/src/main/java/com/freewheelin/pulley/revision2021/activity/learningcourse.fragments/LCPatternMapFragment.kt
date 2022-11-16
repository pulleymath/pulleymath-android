package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCPatternMapViewModel
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentLearnCoursePatternMapBinding
import com.freewheelin.pulley.databinding.ItemPatternMapCardBinding
import com.freewheelin.pulley.databinding.ItemPatternMapHeaderBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.activity.dialog.LCPatternDetailDialog
import com.freewheelin.pulley.revision2021.model.LCPatternCard
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.utils.AnimUtils
import com.freewheelin.pulley.utils.BoongthEffect
//import com.freewheelin.pulley.revision2021.views.LCPatternDetailDialog
//import com.freewheelin.pulley.revision2021.views.LCPatternEndDialog
import kotlinx.coroutines.*

class LCPatternMapFragment : Fragment() {
    companion object {
        fun newInstance(courseId: Int) : LCPatternMapFragment {
            return LCPatternMapFragment().apply {
                arguments = Bundle().apply {
                    putInt(LearningCourseActivity.COURSE_DETAIL_ID, courseId)
//                    putSerializable(LearningCourseActivity.STUDY_CHAPTER_FLAG, chapter)
                }
            }
        }
    }

    val binding: FragmentLearnCoursePatternMapBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learn_course_pattern_map, null, false)
    }

    private lateinit var viewModel: LCPatternMapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val chapterId = (activity as LearningCourseActivity).viewModel.selectedChapterId ?: -1
        viewModel.fetchPatternMap(chapterId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LCPatternMapViewModel::class.java)
        arguments?.let {
//            val chapter = it.getSerializable(LearningCourseActivity.STUDY_CHAPTER_FLAG) as StudyChapter?

//            viewModel.setChapterName(chapter)
            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner
                viewModel.showProgress.postValue(true)

                patternMapRv.adapter = PatternCardListAdapter()
                patternMapRv.layoutManager =
                    GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false).also {
                        it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return when (position) {
                                    0 -> 3
                                    else -> 1
                                }
                            }
                        }
                    }

                nextStepBtnCl.setOnClickListener {
                    (activity as LearningCourseActivity).setPagerToWrongNoteMap()
                }
            }
        }
    }

    inner class PatternCardListAdapter(): ListAdapter<LCPatternCard, RecyclerView.ViewHolder>(
        DiffCallback<LCPatternCard>()
    ) {
        val typeHeader = 0
        val typeGridItem = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    PatternCardHeaderHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pattern_map_header, parent, false))
                }
                else -> {
                    PatternCardViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_pattern_map_card, parent, false))
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as PatternCardHeaderHolder).bind(getItem(position))
                else -> (holder as PatternCardViewHolder).bind(getItem(position), position)
            }

        }
        override fun getItemViewType(position: Int): Int {
            return when(position) {
                0 -> typeHeader
                else -> typeGridItem
            }
        }
    }

    inner class PatternCardHeaderHolder(private val itemBinding: ItemPatternMapHeaderBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: LCPatternCard) {

        }
    }
    inner class PatternCardViewHolder(private val itemBinding: ItemPatternMapCardBinding): RecyclerView.ViewHolder(itemBinding.root),
        PatternMapItemClickListener {

        fun bind(item: LCPatternCard, position: Int) {
            itemBinding.apply {
                listener = this@PatternCardViewHolder
                this.item = item
                vm = viewModel

                cardNumber = "${position}"
                isFinalCard = position == viewModel.patternCardLastIndex
                isTopPosition = position < 4
                isLeftPosition = position % 3 == 1
                isRightPosition = position % 3 == 0
                showNextStepButton = viewModel.patternCardList.value?.map {
                                                                        it.isCompleteCard
                                                                    }?.reduce { prev, next ->
                                                                        prev || next
                                                                    }

                viewModel.patternCardList.value?.lastIndex?.let { lastIndex ->
                    if (position + 2 < lastIndex) {
                        isBottomPosition = false
                        return@let
                    }

                    when (lastIndex % 3) {
                        0 -> { isBottomPosition = position in (lastIndex - 2 .. lastIndex) }
                        1 -> { isBottomPosition = position == lastIndex }
                        2 -> { isBottomPosition = position in (lastIndex - 1 .. lastIndex) }
                    }
                }

                cardRootCl.setOnTouchListener(BoongthEffect())
            }
        }

        override fun onItemClick(item: LCPatternCard) {
            (activity as LearningCourseActivity).setPagerToPatternId(item.patternId)
        }

    }
    interface PatternMapItemClickListener {
        fun onItemClick(item: LCPatternCard)
    }
}

@BindingAdapter("bind_pattern_map_table")
fun bindPatternMapTableRecyclerView(recyclerView: RecyclerView, item: List<LCPatternCard>?) {
    Log.d("bind_pattern_map_table", "list=$item")
    item?.let { contentList ->
        val adapter = recyclerView.adapter as LCPatternMapFragment.PatternCardListAdapter
        adapter.submitList(contentList)
    }
}