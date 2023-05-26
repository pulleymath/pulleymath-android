package com.freewheelin.pulley.revision2021.activity.fragments

//import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import android.content.*
import android.os.Bundle
import android.util.Log
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
import com.freewheelin.pulley.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject.SubjectIndicator
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.*
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.activity.PurchaseInduceWebViewActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.freewheelin.pulley.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConceptCourseFragment : LearningTabFragment() {
    companion object {
        val RESULT_OK = 301
        val CHALLENGE_TUTORIAL_FINISH = 302
        fun newInstance() = ConceptCourseFragment()
    }

    lateinit var binding: FragmentConceptCourseBinding

    val viewModel: ConceptCourseViewModel by viewModels()
    private lateinit var getResult: ActivityResultLauncher<Intent>
    lateinit var challengeReceiver: BroadcastReceiver
    lateinit var reconfigureReceiver: BroadcastReceiver

    override var screenName = "개념"

    override fun onResume() {
        super.onResume()
        fetch()
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        challengeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val challengeCourseId = it.getIntExtra(ChallengeManager.COURSE_ID, -1)
                    println("asoaso CCF challengeReceiver courseId : ${challengeCourseId}")
                    when (challengeCourseId) {
                        ChallengeManager.CourseName.스타트챌린지_개념.id -> {
                            actionOnStartChallenge()
                        }
                        else -> {}
                    }
                }
            }
        }
        reconfigureReceiver = object: BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    viewModel.chapterReset()
                    fetch()
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initActivityResult()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_concept_course, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(challengeReceiver, IntentFilter(ChallengeManager.CONCEPT_STUDY_MOVE_EVENT))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reconfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            studyRv.adapter = ChapterAdapter()
            headerTab.apply {
                setViewModel(viewModel)
                setLifecycleOwner(viewLifecycleOwner)
            }
        }
        viewModel.apply {
            initHeaderSubject()
            selectedSubjectId.observe(viewLifecycleOwner) { subjectId ->
                if (subjectId > -1) {
                    val subject = SubjectIndicator.convertRawToSubject(subjectId)
                    LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "개념", subject.inKorean)
                    fetch(subjectId)
                }
            }

            showMobileHeader.postValue(requireContext().isTablet.not())
            showTabletHeader.postValue(requireContext().isTablet)

            joinedChallengeList.observe(viewLifecycleOwner) {

            }
            isLoading.observe(viewLifecycleOwner) { loading ->
                binding.apply {
                    if (loading) {
                        conceptFragProgressCl.visibleIf(true)
                        loadingLottie.playAnimation()
                    } else {
                        conceptFragProgressCl.hide(300)
                    }
                }
            }
            schoolType.observe(viewLifecycleOwner) {
                CoroutineScope(Dispatchers.IO).launch {
                    binding.headerTab.changeSchoolType(it)
                    viewModel.fetchAvailableSubjects()
                    delay(300)
                    initHeaderSubject()
                }
            }
            errorAction.observe(viewLifecycleOwner) { type ->
                when(type) {
                    HttpException403, GuestException -> showGuestJoinInduceDialog()
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
        }
    }
    private fun showGuestJoinInduceDialog() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.INDUCE, "개념", "가입유도")
        (activity as? LearningTabActivity)?.showGuestJoinInduceDialog {
            viewModel.errorStatusReset()
        }
    }
    private fun sendTutorialEventLog(seq: Int) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "개념학습유도", "${seq}")
    }

    override fun initUI() {
//        viewModel.onHeaderSubjectBtnClick(SubjectIndicator.MathSang.rawValue)
//        setHeaderSubject()

    }
    fun moveSubjectId(id : Int) { // SubjectIndicator
        viewModel.selectedSubjectId.postValue(id)
    }

    fun moveAvailableFirstSubject() {
        viewModel.moveAvailableFirstSubject()
    }
    fun fetch () {
        viewModel.selectedSubjectId.value?.let {
            if (it != -1) { viewModel.fetch(it) }
        }
    }
    fun actionOnStartChallenge() {
        viewModel.checkHeaderSelectedActionOfRelatedChallenge()
        CoroutineScope(Dispatchers.Main).launch {
            delay(700)
            launchTutorialActivity()
        }
    }

    fun launchTutorialActivity () {
        getResult.launch(LCTutorialActivity.getIntent(requireContext()))
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
                footerCl.visibleIf(isLastItem)
                largeChapterBorder.visibleIf(isLastItem)
            }
        }
    }

    private fun initActivityResult() {
        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> ConceptLearningUsageMonitor.finishConceptLearning()
                CHALLENGE_TUTORIAL_FINISH -> {
                    println("asoaso completed [[completedTutorial]] ")
                    viewModel.completedTutorial { startChallenge ->
                        // TODO 챌린지 완료 후
                        viewModel.updateChallenge(startChallenge)
                        fetch()
                        val turnOnCompletedDialog = {
                            val moveEvent: (ChallengeCourse?) -> Unit = { it ->
                                ChallengeManager.getMainTabMoveIntent(it).let {
                                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
                                }
                            }

                            val completedDialog = ChallengeCompletedDialog(startChallenge,
                                ChallengeManager.CourseName.스타트챌린지_개념.id,
                                moveEvent = moveEvent,
                                isDelayedShowNextBtn = true
                            )
                            childFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog1") }
                        }
                        println("asoaso completed [[finishGuideDialog]] ")

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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(challengeReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(reconfigureReceiver)
    }
}
