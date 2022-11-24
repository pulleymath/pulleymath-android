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
        viewModel.selectedSubjectId.postValue(SubjectIndicator.MathSang.rawValue)

        if ((activity as LearningTabActivity).isFromTutorial) {
            (activity as LearningTabActivity).isFromTutorial = false
            binding.apply {
                tutorialCl1.showTransition(500, ViewTransition.Instant)
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

                val adapter = SmallChapterAdapter()
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

    inner class SmallChapterAdapter: ListAdapter<StudyChapter, RecyclerView.ViewHolder>(DiffCallback<StudyChapter>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return SmallChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_small_chapter, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SmallChapterViewHolder).bind(getItem(position))
        }
        inner class SmallChapterViewHolder(private val scBinding: ItemSmallChapterBinding): RecyclerView.ViewHolder(scBinding.root) {
            fun bind(item: StudyChapter) {
                scBinding.apply {
                    this.item = item
                    isNextItemExist = item.hasNextItem
                    lifecycleOwner = viewLifecycleOwner

                    smallChapterTitleTv.text = item.name

                    setFirstItemMarginStart(rootCl, item.isFirstSmallItem)
                    setLastItemMarginEnd(rootCl, item.isLastSmallItem)
                    setExerciseTvTextColor(item)
                    setViewMarginEnd(backgroundExerciseProgressBarIv, item)
                    setViewMarginEnd(backgroundPatternProgressBarIv, item)
                    setExerciseProgressBarIv(item)
                    setPatternCorrectProgressBarIv(item)
                    setExerciseSolveTv(item)
                    setExerciseTotalTv(item)
                    setPatternTv(item)
                    setPatternWrongProgressBarIv(item)
                    setBetweenWhiteBar(item)
                    setPatternSolveTv(item)
                    setPatternTotalTv(item)
                    setLastStudyDateTv(item)
                    setLastStudyDateIv(item)
                    setDoneStampIv(item)
                    setRightArrowIv(item)
                    setSmallChapterRootCl(item)

                }
            }
            fun setSmallChapterRootCl(item: StudyChapter) {
                scBinding.smallChapterRootCl.apply {
                    setOnTouchListener(BoongthEffect())
                    setOnClickListener {
                        if (item.sequence == TUTORIAL_SEQUENCE) {
                            startActivity(LCTutorialActivity.getIntent(requireContext(), true))
                            return@setOnClickListener
                        }
                        viewModel.createLearningCourseOnStudentId(item.id) {
                            val chapterId = item.id
                            val name = item.name
                            getResult.launch(LearningCourseActivity.getIntent(requireContext(), chapterId, name))
                        }
                    }
                }
            }
            fun setRightArrowIv(item: StudyChapter) {
                scBinding.rightArrowIv.apply {
                    visibility = if (item.hasNextItem) View.VISIBLE else View.GONE
                }
            }
            fun setDoneStampIv(item: StudyChapter) {
                scBinding.stampIv.apply {
                    visibility = if(item.isChapterDone) View.VISIBLE else View.GONE
                }
            }
            fun setLastStudyDateIv(item: StudyChapter) {
                scBinding.lastStudyDateIv.apply {
                    val color = if (item.isChapterDone) R.color.purple_gray_300_opa_50 else R.color.purple_gray_300
                    setColorFilter(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setLastStudyDateTv(item: StudyChapter) {
                scBinding.lastStudyDateTv.apply {
                    text = item.lastStudiedFormatting
                    val color = when {
                        item.isChapterDone -> R.color.purple_gray_300_opa_50
                        else -> R.color.purple_gray_300
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setPatternTotalTv(item: StudyChapter) {
                scBinding.patternTotalTv.apply {
                    text = item.patternTotalText
                    val color = when {
                        item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                        else -> R.color.purple_gray_300
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setPatternSolveTv(item: StudyChapter) {
                scBinding.patternSolvedTv.apply {
                    text = item.patternSolvedText
                    val color = when {
                        item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                        else -> R.color.purple_250
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }

            fun setBetweenWhiteBar(item: StudyChapter) {
                scBinding.whiteBarView.apply {
                    visibility = if (item.progress?.pattern?.userWrongCount == 0) View.GONE else View.VISIBLE
                }
            }
            fun setPatternWrongProgressBarIv(item: StudyChapter) {
                scBinding.patternWrongProgressBarIv.apply {
                    setLayoutWidth(this, item.patternWrongProgressRate)
                    visibility = if (item.patternWrongProgressRate == 0.0) View.GONE else View.VISIBLE
                }
            }
            fun setPatternTv(item: StudyChapter) {
                scBinding.patternTv.apply {
                    val color = when {
                        item.progress?.pattern?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.pattern?.isDone == true -> R.color.purple_250_opa_50
                        else -> R.color.purple_250
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setExerciseTotalTv(item: StudyChapter) {
                scBinding.exerciseTotalTv.apply {
                    text = item.exerciseTotalText
                    val color = when {
                        item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                        else -> R.color.purple_gray_300
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setExerciseSolveTv(item: StudyChapter) {
                scBinding.exerciseSolvedTv.apply {
                    text = item.exerciseSolvedText
                    val color = when {
                        item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                        else -> R.color.purple_200
                    }
                    setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setExerciseProgressBarIv(item: StudyChapter) {
                scBinding.exerciseProgressBarIv.apply {
                    setLayoutWidth(this, item.exerciseProgressRate)
                    visibility = if (item.exerciseProgressRate == 0.0) View.GONE else View.VISIBLE
                    val src = if (item.progress?.exercise?.isDone == true) R.drawable.bg_progress_purple_200_opa_50 else R.drawable.bg_progress_purple_200
                    setImageResource(src)
                }
            }
            fun setPatternCorrectProgressBarIv(item: StudyChapter) {
                scBinding.patternCorrectProgressBarIv.apply {
                    setLayoutWidth(this, item.patternCorrectProgressRate)
                    visibility = if (item.patternCorrectProgressRate == 0.0) View.GONE else View.VISIBLE
                    val src = if (item.progress?.pattern?.isDone == true) {
                        R.drawable.bg_progress_purple_250_opa_50
                    } else if (item.progress?.pattern?.userWrongCount != 0) {
                        R.drawable.bg_progress_purple_250_left_corner
                    } else {
                        R.drawable.bg_progress_purple_250
                    }
                    setImageResource(src)
                }
            }
            fun setLayoutWidth(view: View, rate: Double) {
                val layoutParams = view.layoutParams
                layoutParams.width = (rate * 170).toInt().dpToPx()
                view.layoutParams = layoutParams
            }
            fun setExerciseTvTextColor(item: StudyChapter) {
                scBinding.apply {
                    val color = when {
                        item.progress?.exercise?.userSolvedCount == 0 -> R.color.purple_gray_300
                        item.progress?.exercise?.isDone == true -> R.color.purple_200_opa_50
                        else -> R.color.purple_200
                    }
                    exerciseTv.setTextColor(ContextCompat.getColor(requireContext(), color))
                }
            }
            fun setViewMarginEnd(view: View, item: StudyChapter) {
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    when (item.progressTextLength) {
                        3 -> { this.marginEnd = 43.toPx() }
                        4 -> { this.marginEnd = 47.toPx() }
                        5 -> { this.marginEnd = 51.toPx() }
                        6 -> { this.marginEnd = 55.toPx() }
                        7 -> { this.marginEnd = 59.toPx() }
                        else -> { this.marginEnd = 59.toPx() }
                    }
                }

            }
            fun setFirstItemMarginStart(view: View, isFirstItem: Boolean) {
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    this.marginStart =  if (isFirstItem) 22.toPx() else 0.toPx()
                }
            }
            fun setLastItemMarginEnd(view: View, isLastItem: Boolean) {
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    this.marginEnd =  if (isLastItem) 22.toPx() else 0.toPx()
                }
            }
        }
    }

    interface ChapterItemClickListener {
        fun onItemClick(sc: StudyChapter, partIndex: Int)
    }
}

@BindingAdapter("bind_study_chapter")
fun bindStudyChapterRecyclerView(recyclerView: RecyclerView, item: List<StudyChapter>?){
    Log.d("bind_study_chapter", "list=$item")
    item?.let { chapterList ->
        val adapter = recyclerView.adapter as? ConceptCourseFragment.ChapterAdapter
        adapter?.submitList(chapterList)
    }
}

@BindingAdapter("bind_small_chapter")
fun bindSmallChapterRv(rv: RecyclerView, item: List<StudyChapter>?) {
    Log.d("bind_small_chapter", "list=$item")
    item?.let { chapterList ->
        val adapter = rv.adapter as? ConceptCourseFragment.SmallChapterAdapter
        adapter?.submitList(chapterList)
    }
}

@BindingAdapter("progress_layout_width")
fun setLayoutWidth(view: View, rate: Double) {
    println("progress_layout_width , view: ${view.id} , rate: ${rate}")
    val layoutParams = view.layoutParams
    layoutParams.width = (rate * 170).toInt().dpToPx()
    view.layoutParams = layoutParams
}