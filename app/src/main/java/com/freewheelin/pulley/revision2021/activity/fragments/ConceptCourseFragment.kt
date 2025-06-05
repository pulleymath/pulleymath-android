package com.freewheelin.pulley.revision2021.activity.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentConceptCourseBinding
import com.freewheelin.pulley.databinding.ItemStudyChapterBinding
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.legacy.core.manage.UserManager.RE_CONFIGURE_UI
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.legacy.utils.hide
import com.freewheelin.pulley.legacy.utils.setMarginTop
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.activity.LCTutorialActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity.Companion.FROM_CONCEPT_TAB
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.ui.adapter.ConceptCourseSmallAdapter
import com.freewheelin.pulley.revision2021.utils.observeThrottle
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.GuestException
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.HttpException403
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType.NONE
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeManager
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.dialogs.ChallengeCompletedDialog
import com.freewheelin.pulley.revision2023.ui.fragment.MainTabFragment
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import com.freewheelin.pulley.revision2023.utils.ChallengeGuideManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConceptCourseFragment : MainTabFragment() {
    companion object {
        val RESULT_OK = 301
        val CHALLENGE_TUTORIAL_FINISH = 302
        fun newInstance() = ConceptCourseFragment()
    }

    lateinit var binding: FragmentConceptCourseBinding
    private var isViewCreated = false
    val viewModel: ConceptCourseViewModel by viewModels()
    lateinit var challengeReceiver: BroadcastReceiver
    lateinit var reconfigureReceiver: BroadcastReceiver

    override var type: MainTab = MainTab.개념

    override fun onResume() {
        super.onResume()
        viewModel.selectedLcSubject.value?.let { moveSubjectId(it) }
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isViewCreated = true
        challengeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val challengeCourseId = it.getIntExtra(ChallengeManager.COURSE_ID, -1)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_concept_course, container, false)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(challengeReceiver, IntentFilter(ChallengeManager.CONCEPT_STUDY_MOVE_EVENT))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(reconfigureReceiver, IntentFilter(RE_CONFIGURE_UI))
        return binding.root
    }
    var isShowMainTab = true
    var isSubjectHeaderChangedWhenMainTabIsNotShow = false
    override fun resetHeaderControlParams() {
        isShowMainTab = true
        isSubjectHeaderChangedWhenMainTabIsNotShow = false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            studyRv.adapter = ChapterAdapter()
            if (requireContext().isMobile) {
                studyRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val scrollY = studyRv.computeVerticalScrollOffset()
                        if (scrollY > 180 && isSubjectHeaderChangedWhenMainTabIsNotShow) {
                            isSubjectHeaderChangedWhenMainTabIsNotShow = false
                        }
                        if (isSubjectHeaderChangedWhenMainTabIsNotShow) return

                        if (scrollY > 180 && isShowMainTab) {
                            isShowMainTab = false
                            (activity as? MainActivity)?.showTabHeader(false)
                        } else if (scrollY < 10 && !isShowMainTab) {
                            isShowMainTab = true
                            (activity as? MainActivity)?.showTabHeader(true)
                        }
                    }
                })
            }
        }
        viewModel.apply {
//            initHeaderSubject()
            selectedLcSubject.observe(viewLifecycleOwner) {
                showLoading(true)
                if (!isShowMainTab) {
                    binding.studyRv.scrollToPosition(0)
                    isSubjectHeaderChangedWhenMainTabIsNotShow = true
                }
            }
            selectedLcSubject.observeThrottle(viewLifecycleOwner) { subject ->
                if (subject.subjectId > -1) {
                    fetch(subject.subjectId)
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (isAdded && user != null) {
                            try {
                                LogUtils.logEvent(requireContext(), user!!, PulleyEvent.MENU_CLICK, "개념", subject.name)
                            } catch (e: IllegalStateException) {
                                Firebase.crashlytics.log("Error in LogUtils.logEvent even after isAdded check: ${e.message}")
                                Firebase.crashlytics.recordException(e)
                            }
                        } else {
                            Firebase.crashlytics.log("Fragment not added or user is null when trying to log event. Subject: ${subject.name}")
                        }
                    }

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
                    viewModel.fetchAvailableSubjects() {
                    }
                }
            }
            lcSubjects.observe(viewLifecycleOwner) { subjects ->

                binding.headerTabContainer2.let { container ->
                    container.removeAllViews()

                    val bookmarkButton = ImageButton(requireContext()).apply {
                        val tutorialLCSubject = LCSubject().apply {
                            subjectId = 0
                            name = "튜토리얼"
                            unitcode = "0"
                        }
                        layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.dp80),
                            resources.getDimensionPixelSize(R.dimen.dp52)
                        ).apply {
                            marginEnd = resources.getDimensionPixelSize(R.dimen.dp16)
                        }
                        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.dp30)
                        setPadding(horizontalPadding, paddingTop, horizontalPadding, paddingBottom)

                        setImageResource(R.drawable.ic_course_star)
                        setBackgroundResource(R.drawable.bg_white_round_28_ripple_gray200)
                        setColorFilter(ContextCompat.getColor(context, R.color.gray_500))
                        setOnClickListener {
                            // 북마크 버튼 클릭 이벤트 처리
                            resetAllButtonStates(container)
                            setBackgroundResource(R.drawable.bg_purple_300_round_28_ripple)
                            setColorFilter(ContextCompat.getColor(context, R.color.white))
                            viewModel.onHeaderSubjectBtnClick2(tutorialLCSubject)

                        }
                    }
                    container.addView(bookmarkButton)

                    subjects.forEachIndexed { index, subject ->
                        val tabButton = Button(requireContext()).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                resources.getDimensionPixelSize(R.dimen.dp52)
                            ).apply {
                                marginEnd = if (index == subjects.size - 1) 0
                                else resources.getDimensionPixelSize(R.dimen.dp16)
                            }
                            text = subject.name
                            setBackgroundResource(R.drawable.bg_white_round_28_ripple_gray200)
                            val horizontalPadding = resources.getDimensionPixelSize(R.dimen.dp30)
                            setPadding(horizontalPadding, paddingTop, horizontalPadding, paddingBottom)

                            setTextColor(
                                ContextCompat.getColorStateList(
                                    context,
                                    R.color.gray_600
                                )
                            )
                            setOnClickListener {
                                // 각 과목 버튼 클릭 이벤트 처리
                                resetAllButtonStates(container)
                                setBackgroundResource(R.drawable.bg_purple_300_round_28_ripple)
                                setTextColor(
                                    ContextCompat.getColorStateList(
                                        context,
                                        R.color.white
                                    )
                                )
                                viewModel.onHeaderSubjectBtnClick2(subject)
                            }
                        }

                        container.addView(tabButton)
                    }

                }
                binding.headerTabContainer2.children.forEachIndexed { index, view ->
                    if (index == 1) view.performClick()
                }
                selectedLcSubject.postValue(subjects.first())
            }
            selectedLcSubject.observe(viewLifecycleOwner) {
                binding.headerTabContainer2.children
            }
            errorAction.observe(viewLifecycleOwner) { type ->
                when(type) {
                    HttpException403, GuestException -> showGuestJoinInduceDialog()
                    NONE -> {}
                    else -> { Log.e(javaClass.simpleName, "Error Not Handled : ${type}")}
                }
            }
        }
    }
    private fun resetAllButtonStates(container: ViewGroup) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            when (child) {
                is Button -> {
                    child.setBackgroundResource(R.drawable.bg_white_round_28_ripple_gray200)
                    child.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.gray_600))
                }
                is ImageButton -> {
                    child.setBackgroundResource(R.drawable.bg_white_round_28_ripple_gray200)
                    child.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray_500))
                }
            }
        }
    }

    private fun showGuestJoinInduceDialog() {
        LogUtils.logEvent(requireContext(), user!!, PulleyEvent.INDUCE, "개념", "가입유도")
        (activity as? MainActivity)?.showGuestJoinInduceDialog {
            viewModel.errorStatusReset()
        }
    }
    private fun sendTutorialEventLog(seq: Int) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "튜토리얼", "개념학습유도", "${seq}")
    }

    fun moveTutorial() {
        if (isViewCreated) {
            val tutorialLCSubject = LCSubject().apply {
                subjectId = 0
                name = "튜토리얼"
                unitcode = "0"
            }
            viewModel.selectedLcSubject.postValue(tutorialLCSubject)
        }
    }
    fun moveSubjectId(subject : LCSubject) {
        if (isViewCreated) {
            viewModel.selectedLcSubject.postValue(subject)
        }
    }

    fun fetch (subjectId: Int? = null) {
        if (subjectId != null) {
            viewModel.fetch(subjectId)
            return
        }
        viewModel.selectedLcSubject.value?.let {
            if (it.subjectId != -1) { viewModel.fetch(it.subjectId) }
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
                val titleTvTopMargin = if(position == 0 && requireContext().isMobile) 24 else 32
                largeChapterTitleTv.setMarginTop(titleTvTopMargin)
                val isLastItem = position == viewModel.chapterList.value?.size?.minus(1)
                footerCl.visibleIf(isLastItem)
                largeChapterBorder.visibleIf(isLastItem)
            }
        }
    }
    val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            FROM_CONCEPT_TAB -> {
                val reFetchReceiverIntent = Intent(RE_CONFIGURE_UI)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(reFetchReceiverIntent)
                ConceptLearningUsageMonitor.finishConceptLearning()
            }
            CHALLENGE_TUTORIAL_FINISH -> {
                viewModel.completedTutorial { startChallenge ->
                    // TODO 챌린지 완료 후
                    viewModel.updateChallenge(startChallenge)
                    val turnOnCompletedDialog = {
                        val moveEvent: (ChallengeCourse?) -> Unit = { it ->
                            ChallengeManager.getMainTabMoveIntent(it).let {
                                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(it)
                            }
                        }

                        val completedDialog = ChallengeCompletedDialog.newInstance(
                            challenge = startChallenge,
                            completedCourseId = ChallengeManager.CourseName.스타트챌린지_개념.id,
                            isDelayedShowNextBtn = true
                        )
                        completedDialog.moveEvent = moveEvent
                        childFragmentManager.let { completedDialog.show(it, "ChallengeCompletedDialog1") }
                    }

                    val finishGuideDialog = ChallengeGuideManager
                        .getFinishGuideFromMission1(nextEvent = turnOnCompletedDialog)
                    childFragmentManager.let { finishGuideDialog.show(it, "finishGuideDialog") }
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
