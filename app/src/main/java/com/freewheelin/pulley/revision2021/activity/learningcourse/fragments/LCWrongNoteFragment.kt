package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentLcWrongNoteBinding
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.PatternConceptFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.PatternSolutionFragment
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.utils.debounce
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCWrongNoteFViewModel
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.memoView.MemoListener
import com.freewheelin.pulley.legacy.views.memoView.PathRedoUndoCountChangeListener
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.PencilPanelListener
import com.google.android.material.tabs.TabLayoutMediator

class LCWrongNoteFragment : Fragment(),
    WrongNoteScrollListener, PencilPanelListener, FloatingAnswerDelegate,
    PathRedoUndoCountChangeListener, MemoListener {

    companion object {
        val NOTECARD = "NOTE_CARD"
        val CHAPTER_ID = "CHAPTER_ID"
        fun newInstance(noteCard: LCWrongNoteMapCard, chapterId: Int) : LCWrongNoteFragment {
            return LCWrongNoteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(NOTECARD, noteCard)
                    putInt(CHAPTER_ID, chapterId)
                }
            }
        }
    }

    val binding: FragmentLcWrongNoteBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_lc_wrong_note, null, false)
    }
    lateinit var viewModel: LCWrongNoteFViewModel
    private var tabFragments: MutableList<Fragment> = mutableListOf()
    val screenWidth by lazy { DisplayUtils.getScreenWidth(requireContext()) }

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LCWrongNoteFViewModel::class.java)
        arguments?.let {
            val noteCard = it.getSerializable(NOTECARD) as LCWrongNoteMapCard
            val chapterId = it.getInt(CHAPTER_ID)

            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                viewModel.noteCard.observeOnce(this@LCWrongNoteFragment) {

//                    val patternId = (parentFragment as LCPatternFragment).viewModel.patternId
                    memoView.memoListener = this@LCWrongNoteFragment
                    memoView.memoCase = StudyMemoCase.CONCEPT_LEARNING_WRONG_PROBLEM
                    memoView.removePathRedoUndoCountChangeListener()
                    memoView.setPathRedoUndoCountChangeListener(this@LCWrongNoteFragment)
                    memoView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    memoView.clearBitmap()
                    val studentId = MyApplication.user?.studentID ?: ""
                    val memoId = "lcwrongnotememo&&${studentId}&&${it.userQuizSolvingHistoryId}&&${it.refPatternQuizId}"
                    memoView.load(memoId) {
                        viewModel.getMemoFromParams(chapterId, it.userQuizSolvingHistoryId, StudyMemoCase.CONCEPT_LEARNING_WRONG_PROBLEM) {
                            println("aspasp 새로운디비 에서 찾으러옴 존재하나? : ${it != null}")
                            memoView.setMemo(it) {
                                viewModel.isMemoDrawAStrokeAtLeastOnceAsQuiz = false
                                viewModel.isAllMemoRemovedOnQuiz = false
                            }
                            viewModel.alreadyHaveMemoOnThisQuiz = it != null
                        }
                    }
                }

                viewModel.init(noteCard, chapterId)
                setConceptDrawer(false)
                memoView.layoutParams.width = screenWidth

                viewModel.showConceptSolutionView.observe(viewLifecycleOwner) {
                    // ager 이동시 부모뷰에 텍스트 전달을 위해 observe 사용
                    (activity as LCWrongNoteActivity).setConceptSolutionToggleBtnText(it)
                }

                tabFragments = mutableListOf(
                    PatternConceptFragment.newInstance(noteCard.toLCPatternQuiz()),
                    PatternSolutionFragment.newInstance(noteCard.toLCPatternQuiz())
                )

                pager.adapter = ConceptSolutionPagerAdapter(tabFragments, childFragmentManager, lifecycle)



                val tabTitles = listOf<String>("개념", "정답 및 해설")
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    tab.text = tabTitles[position]
                }.attach()

                leftScrollView.listener = this@LCWrongNoteFragment
                val quizUserInputDebounce = debounce<Unit>(300L, viewLifecycleOwner.lifecycleScope) {
                    (activity as LCWrongNoteActivity).setPagerUserInputEnabled(true)
                }
                leftScrollView.setUserInputOfPatternQuizEnabled = { enabled ->
                    (activity as LCWrongNoteActivity).setPagerUserInputEnabled(enabled)
                }
                leftScrollView.scrollEndCallback = quizUserInputDebounce


                rootCl.setOnDragListener { view, dragEvent ->
                    // floating answer sheet 컨트롤
                    onFloatingAnswerSheetDragListener(view, dragEvent)
                }
                lifecycle.addObserver(floatingAnswerSheet)


                floatingAnswerSheet.doOnAttach { view ->
                    val sheet = view as FloatingAnswerSheet
                    viewModel.noteCard.observeOnce(this@LCWrongNoteFragment) {
                        sheet.coufigureUIOnNote(it)
                    }
                }

                floatingAnswerSheet.delegate = this@LCWrongNoteFragment
                floatingAnswerSheet.binding.scoringBtn.setOnClickListener {
                    viewModel.quizScoring {
                        (activity as LCWrongNoteActivity).scoringPatternQuiz(it)
                    }
                }

                if (noteCard.isCorrect != null) {
                    controlDisabledFloatingAnswer(noteCard, floatingAnswerSheet)
                }
                viewModel.currentAnswerOfSingle.observe(viewLifecycleOwner) {
                    // 값이 임력되었을떄 입력이 간으하게
                    floatingAnswerSheet.binding.scoringBtn.isEnabled = when (it) {
                        "" -> false
                        else -> true
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as LCWrongNoteActivity).run {
            viewModel.chatBotInfo = this@LCWrongNoteFragment.viewModel.chatbotInfo
        }
        resetMemoView()
        resumePencilcaseView()
    }
    fun saveMemo() {
        binding.memoView.getMemoBase64()?.let {
            viewModel.saveMemo(it, StudyMemoCase.CONCEPT_LEARNING_WRONG_PROBLEM, screenWidth)
        }
    }
    fun hasMoreHint(): Boolean {
        return viewModel.hasMoreHint()
    }
    fun setNextHint(remainingHintSize: Int) {
        viewModel.setNextHint(remainingHintSize)
    }

    fun setHintBtn() {
        val size = viewModel.remainingHintSize.value
        val hintExist = viewModel.hintExist
        (activity as LCWrongNoteActivity).setHintBtn(size == 0, size, hintExist)
    }
    private fun resetMemoView() {
        val noteActivity = (activity as LCWrongNoteActivity)
        noteActivity.binding.penPanel.memoViews.clear()
    }

    private fun resumePencilcaseView() {
        val noteActivity = (activity as LCWrongNoteActivity)
        noteActivity.binding.penPanel.listener = this@LCWrongNoteFragment
        binding.memoView.set(noteActivity.binding.penPanel)

        val pencilType = noteActivity.getPencilcaseType()
        val fingerDrawMode = noteActivity.getFingerDrawMode()

        noteActivity.binding.penPanel.apply {

            if (drawType != null) {
                resetMode()
                fingerDrawModeSwitch.isChecked = fingerDrawMode

                val isBlocked = pencilType != null
                binding.leftScrollView.isBlock = isBlocked
                binding.leftScrollView.fingerDrawMode = fingerDrawMode
                binding.memoView.fingerDrawMode = fingerDrawMode
                (activity as LCWrongNoteActivity).setPagerSwipeBlocked(isBlocked)
                (activity as LCWrongNoteActivity).saveFingerDrawMode(fingerDrawMode)
            }
            noteActivity.hidePenPanel()
//            pencilOptionLl.isSelected = false
//            pencilOptionLl.visibility = View.GONE
        }
    }
    fun setTempConceptSolutionViewFlag() {
        viewModel.showConceptSolutionView.postValue(viewModel.tempConceptSolutionViewFlag)
    }
    fun resumeFloatingAnswerSheetLocation() {
        binding.floatingAnswerSheet.setInitPosition()
    }
    fun toggleDrawer() {
        val value = viewModel.showConceptSolutionView.value?.not()
        viewModel.showConceptSolutionView.postValue(value)
        viewModel.tempConceptSolutionViewFlag = value
    }
    fun setConceptDrawer(showDrawer: Boolean) {
        viewModel.tempConceptSolutionViewFlag = showDrawer
        viewModel.showConceptSolutionView.postValue(showDrawer)
    }

    fun onFloatingAnswerSheetDragListener (view: View, dragEvent: DragEvent): Boolean {
        binding.apply {
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    val x = dragEvent.x
                    val y = dragEvent.y
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val answerHeight = floatingAnswerSheet.height

                    var x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                    var y = dragEvent.y - answerHeight / 2f

                    floatingAnswerSheet.setPosition(x, y)
                    floatingAnswerSheet.visibility = View.VISIBLE
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    var x = dragEvent.x
                    var y = dragEvent.y

                    if (y > rootCl.height) {
                        val answerHeight = floatingAnswerSheet.height
                        val answerWidth = floatingAnswerSheet.width

                        x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                        y = dragEvent.y - answerHeight / 2f

                        // 화면 밖으로 나가면 안으로 넣기
                        if (x > rootCl.width - answerWidth) {
                            x = (rootCl.width - answerWidth).toFloat()
                        } else if (x < (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))) {
                            x = (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                        }

                        if (y > rootCl.height - answerHeight) {
                            y = (rootCl.height - answerHeight).toFloat()
                        } else if (y < -answerHeight) {
                            y = (-answerHeight).toFloat()
                        }
                        floatingAnswerSheet.setPosition(x, y)
                        floatingAnswerSheet.visibility = View.VISIBLE
                    }

                }
                DragEvent.ACTION_DROP -> {

                    floatingAnswerSheet.visibility = View.VISIBLE

                    val answerHeight = floatingAnswerSheet.height
                    val answerWidth = floatingAnswerSheet.width

                    var x =
                        dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                    var y = dragEvent.y - answerHeight / 2f


                    // 화면 밖으로 나가면 안으로 넣기
                    if (x > rootCl.width - answerWidth) {
                        x = (rootCl.width - answerWidth).toFloat()
                    } else if (x < (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))) {
                        x = (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                    }

                    if (y > rootCl.height - answerHeight) {
                        y = (rootCl.height - answerHeight).toFloat()
                    } else if (y < -answerHeight) {
                        y = (-answerHeight).toFloat()
                    }

                    floatingAnswerSheet.setPosition(x, y)
                    Preferences.floatingAnswerSheetLastLocation.set("${x}&&${y}")
                    (activity as LCWrongNoteActivity).resumeLCPatternFloatingAnswerSheetLocation()
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    floatingAnswerSheet.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        floatingAnswerSheet.cancelDragAndDrop()
                    }
                }
                else -> {
//                        println("emform, dragEvent.action : ${dragEvent.action}")
                }
            }
        }
        return true
    }
    fun controlDisabledFloatingAnswer(noteCard: LCWrongNoteMapCard, sheet: FloatingAnswerSheet) {
        sheet.binding.selectionAnswerView.setAnswerByRawString(noteCard.userAnswer)
        noteCard.userAnswer?.let { sheet.binding.shortAnswerView.setText(it, TextView.BufferType.EDITABLE) }
    }

    override fun onScaleFactor(scale: Float) {
        (activity as LCWrongNoteActivity).setQuizImageScale(scale)
    }
    override fun onGestureListener() {
        (activity as LCWrongNoteActivity).hidePenPanel()
    }

    override fun onDrawTypeChanged(type: DrawType?) {
        val isBlocked = type != null
        binding.leftScrollView.isBlock = isBlocked

        (activity as LCWrongNoteActivity).setPagerSwipeBlocked(isBlocked)
        (activity as LCWrongNoteActivity).savePencilcaseType(type)
    }
    // TODO

//    override fun onThicknessSelected(thickness: Float) {
//        (activity as LCWrongNoteActivity).savePencilcaseThicknesss(thickness)
//    }
//    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {
//        (activity as LCWrongNoteActivity).savePencilcaseColor(color)
//    }

    override fun onFingerDrawModeChanged(value: Boolean) {
        (activity as LCWrongNoteActivity).saveFingerDrawMode(value)
        binding.leftScrollView.fingerDrawMode = value
        binding.memoView.fingerDrawMode = value
    }

    override fun onAnswerChanged(view: View, answer: String?) {
        viewModel.setCurrentAnswer(answer)
    }

    override fun onShortAnswerChanged(answer: String?) {
        viewModel.setCurrentAnswer(answer)
    }
    inner class ConceptSolutionPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
    override fun onPause() {
        super.onPause()
        saveMemo()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onUndoCountChanged(count: Int) {
        (activity as? LCWrongNoteActivity)?.setUndoCount(count)
    }

    override fun onRedoCountChanged(count: Int) {
        (activity as? LCWrongNoteActivity)?.setRedoCount(count)
    }

    override fun onDrawAStroke(memoCase: StudyMemoCase) {
        if (memoCase == StudyMemoCase.CONCEPT_LEARNING_WRONG_PROBLEM) {
            viewModel.isMemoDrawAStrokeAtLeastOnceAsQuiz = true
        }
    }

    override fun onRemoveAllMemo() {
        viewModel.isMemoDrawAStrokeAtLeastOnceAsQuiz = false
        viewModel.isAllMemoRemovedOnQuiz = true
    }
}