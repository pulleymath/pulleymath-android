package com.freewheelin.pulley.revision2021.activity.fragments

//import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.bases.isTablet
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject.SubjectIndicator
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.utils.*

class ConceptCourseFragment : LearningTabFragment() {
    companion object {
        val RESULT_OK = 301
        val CHALLENGE_TUTORIAL_FINISH = 302
        fun newInstance() = ConceptCourseFragment()
    }

    lateinit var binding: FragmentConceptCourseBinding

    val viewModel: ConceptCourseViewModel by viewModels()
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
        initActivityResult()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_concept_course, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner


            studyRv.adapter = ChapterAdapter()
            studyRv.setOnClickListener {
                viewModel.showProgress.postValue(!viewModel.showProgress.value!!)
            }
        }
        viewModel.apply {
            showProgress.postValue(true)
            onHeaderSubjectBtnClick(SubjectIndicator.MathSang.rawValue)
            selectedSubjectId.observe(viewLifecycleOwner) { subjectId ->
                if (subjectId > -1) { fetch(subjectId) }
            }

            showMobileHeader.postValue(requireContext().isTablet.not())
            showTabletHeader.postValue(requireContext().isTablet)

            joinedChallengeList.observe(viewLifecycleOwner) {
                println("asoaso joinedChallengeList: ${it.size}")
            }
        }
    }

    private fun sendTutorialEventLog(seq: Int) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "개념학습유도", "${seq}")
    }

    override fun initUI() {
//        viewModel.onHeaderSubjectBtnClick(SubjectIndicator.MathSang.rawValue)
        setHeaderSubject()

        if ((activity as LearningTabActivity).isFromTutorial) {
            (activity as LearningTabActivity).isFromTutorial = false
        }
    }
    fun moveSubjectId(id : Int) { // SubjectIndicator
        viewModel.selectedSubjectId.postValue(id)
    }

    fun fetch () {
        viewModel.selectedSubjectId.value?.let {
            if (it != -1) { viewModel.fetch(it) }
        }
    }
    fun setHeaderSubject() {
        viewModel.checkHeaderSelectedActionOfRelatedChallenge()
    }

    inner class ChapterAdapter(): ListAdapter<StudyChapter, RecyclerView.ViewHolder>(DiffCallback<StudyChapter>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ChapterViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_study_chapter, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ChapterViewHolder).bind(getItem(position), position)
        }
    }

    inner class ChapterViewHolder(private val itemBinding: ItemStudyChapterBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: StudyChapter, position: Int) {
            itemBinding.apply {
                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner
                chapterCl.visibility = View.VISIBLE

                val adapter = ConceptCourseSmallAdapter(viewModel, getResult)
                adapter.submitList(item.children)
                smallChapterRv.adapter = adapter
                smallChapterRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                smallChapterRv.setHasFixedSize(true)
                largeChapterTitleTv.setMarginTop(if (position == 0) 48 else 32)
                val isLastItem = position == viewModel.chapterList.value?.size?.minus(1)
                footerCl.visibility = if (isLastItem) View.VISIBLE else View.GONE
                largeChapterBorder.visibility = if (isLastItem) View.VISIBLE else View.GONE
            }
        }
    }

    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> ConceptLearningUsageMonitor.finishConceptLearning()
                CHALLENGE_TUTORIAL_FINISH -> {
                    viewModel.completedTutorial { startChallenge ->
                        // TODO 챌린지 완료 후
                        viewModel.updateChallenge(startChallenge)
                        fetch()
                        val turnOnCompletedDialog = {
                            val completedDialog = ChallengeCompletedDialog(startChallenge) {
                                ChallengeManager.getMainTabMoveIntent(it).let {
                                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
                                }
                            }
                            childFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog") }
                        }

                        val finishGuideDialog = ChallengeGuideManager
                            .getFinishGuideFromMission1(nextEvent = turnOnCompletedDialog)
                        childFragmentManager.let { finishGuideDialog.show(it, "finishGuideDialog") }
                    }
                }
            }
        }
    }

    interface ChapterItemClickListener {
        fun onItemClick(sc: StudyChapter, partIndex: Int)
    }
}
