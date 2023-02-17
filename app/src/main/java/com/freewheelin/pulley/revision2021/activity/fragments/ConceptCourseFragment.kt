package com.freewheelin.pulley.revision2021.activity.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.databinding.*
//import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.databinding.FragmentConceptCourseBinding
import com.freewheelin.pulley.databinding.ItemSmallChapterBinding
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.model.StudyChapter.Companion.TUTORIAL_SEQUENCE
import com.freewheelin.pulley.revision2021.model.response.LCSubject.SubjectIndicator
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.utils.*

class ConceptCourseFragment : LearningTabFragment() {
    companion object {
        val RESULT_OK = 301
        fun newInstance() = ConceptCourseFragment()
    }

    lateinit var binding: FragmentConceptCourseBinding

    private lateinit var viewModel: ConceptCourseViewModel
    private lateinit var getResult: ActivityResultLauncher<Intent>

    override var screenName = "개념"

    override fun onResume() {
        super.onResume()
        viewModel.fetchAvailableSubjects()
        fetch()
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_concept_course, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ConceptCourseViewModel::class.java)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.showProgress.postValue(true)

            studyRv.adapter = ChapterAdapter()
            viewModel.selectedSubjectId.postValue(SubjectIndicator.MathSang.rawValue)
            viewModel.selectedSubjectId.observe(viewLifecycleOwner) { subjectId ->
                if (subjectId > -1) { viewModel.fetch(subjectId) }
            }

            tutoral2TitleTv.text = "${user?.fullName} 학생도 바로 학습을 시작해\n소단원 하나만 끝내볼까?"
            tutorialCl1.setOnClickListener {
                sendTutorialEventLog(1)
                tutorialCl1.hide(100) {  }
                tutorialCl2.showTransition(500, ViewTransition.Instant)
            }
            tutorialCl2.setOnClickListener {
                sendTutorialEventLog(2)
                tutorialCl2.hide(300) {  }
            }
            pullingBtn.setOnClickListener {
                sendTutorialEventLog(2)
                tutorialCl2.hide(300) {  }
            }
            studyRv.setOnClickListener {
                viewModel.showProgress.postValue(!viewModel.showProgress.value!!)
            }

            getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    ConceptLearningUsageMonitor.finishConceptLearning()
                }
            }
        }
    }

    private fun sendTutorialEventLog(seq: Int) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "개념학습유도", "${seq}")
    }

    override fun initUI() {
        if (::viewModel.isInitialized) {
            viewModel.selectedSubjectId.postValue(SubjectIndicator.MathSang.rawValue)

            if ((activity as LearningTabActivity).isFromTutorial) {
                (activity as LearningTabActivity).isFromTutorial = false
                binding.apply {
                    tutorialCl1.showTransition(500, ViewTransition.Instant)
                }
            }
        }
    }

    fun fetch () {
        viewModel.selectedSubjectId.value?.let {
            if (it != -1) { viewModel.fetch(it) }
        }
    }

    inner class ChapterAdapter(): ListAdapter<StudyChapter, RecyclerView.ViewHolder>(DiffCallback<StudyChapter>()) {
        private val typeHeader = 0
        private val typeChapter = 1
        private val typeFooter = 2
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    ChapterHeaderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
                }
                typeChapter -> {
                    ChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
                }
                typeFooter -> {
                    ChapterFooterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
                }
                else -> {
                    ChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as ChapterHeaderViewHolder).bind(getItem(position))
                (viewModel.chapterList.value?.size?.minus(1)) -> (holder as ChapterFooterViewHolder).bind(getItem(position))
                else -> (holder as ChapterViewHolder).bind(getItem(position))
            }
        }
        override fun getItemViewType(position: Int): Int {
            return when(position) {
                0 -> typeHeader
                (viewModel.chapterList.value?.size?.minus(1)) -> typeFooter
                else -> typeChapter
            }
        }
    }

    inner class ChapterViewHolder(private val itemBinding: ItemStudyChapterBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: StudyChapter) {
            itemBinding.apply {
                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner
                headerCl.visibility = View.GONE
                footerCl.visibility = View.GONE
                chapterCl.visibility = View.VISIBLE

                val adapter = ConceptCourseSmallAdapter(viewModel, getResult)
                adapter.submitList(item.children)
                smallChapterRv.adapter = adapter
                smallChapterRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                smallChapterRv.setHasFixedSize(true)
            }
        }
    }
    inner class ChapterHeaderViewHolder(private val itemBinding: ItemStudyChapterBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: StudyChapter) {
            itemBinding.apply {
                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner
                headerCl.visibility = View.VISIBLE
                footerCl.visibility = View.GONE
                chapterCl.visibility = View.GONE

            }
        }
    }
    inner class ChapterFooterViewHolder(private val itemBinding: ItemStudyChapterBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: StudyChapter) {
            itemBinding.apply {
                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner
                headerCl.visibility = View.GONE
                footerCl.visibility = View.VISIBLE
                chapterCl.visibility = View.GONE
            }
        }
    }

    interface ChapterItemClickListener {
        fun onItemClick(sc: StudyChapter, partIndex: Int)
    }
}
