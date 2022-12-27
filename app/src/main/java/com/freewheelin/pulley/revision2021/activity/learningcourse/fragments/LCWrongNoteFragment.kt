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
import com.freewheelin.pulley.revision2021.activity.LCWrongNoteActivity
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.PatternConceptFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.PatternSolutionFragment
import com.freewheelin.pulley.revision2021.cookingmemo.PathRedoUndoCountChangeListener
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.utils.debounce
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCWrongNoteFViewModel
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.Preferences
import com.freewheelin.pulley.utils.toPx
import com.google.android.material.tabs.TabLayoutMediator

class LCWrongNoteFragment : Fragment(),
    WrongNoteScrollListener, CookingPencilcaseListener, FloatingAnswerDelegate,
    PathRedoUndoCountChangeListener {

    companion object {
        val NOTECARD = "NOTE_CARD"
        fun newInstance(noteCard: LCWrongNoteMapCard) : LCWrongNoteFragment {
            return LCWrongNoteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(NOTECARD, noteCard)
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

            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                viewModel.noteCard.observeOnce(this@LCWrongNoteFragment) {

//                    val patternId = (parentFragment as LCPatternFragment).viewModel.patternId
                    memoView.removePathRedoUndoCountChangeListener()
                    memoView.setPathRedoUndoCountChangeListener(this@LCWrongNoteFragment)
                    memoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    memoView.setMemoSavedName(it.userQuizSolvingHistoryId, it.refPatternQuizId, "lcwrongnotememo")
                    memoView.clearBitmap()
                    memoView.load()
                }

                viewModel.init(noteCard)
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
        resetMemoView()
        resumePencilcaseView()
    }

    fun hasMoreHint(): Boolean {
        return viewModel.hasMoreHint()
    }
    fun setNextHint(remainingHintSize: Int) {
        viewModel.setNextHint(remainingHintSize)
    }

    fun setHintBtn() {
        val size = viewModel.remainingHintSize.value
        (activity as LCWrongNoteActivity).setHintBtn(size == 0, size)
    }
    private fun resetMemoView() {
        val noteActivity = (activity as LCWrongNoteActivity)
        noteActivity.binding.pencilcaseView.memoViews.clear()
    }
    private fun resumePencilcaseView() {
        val noteActivity = (activity as LCWrongNoteActivity)
        noteActivity.binding.pencilcaseView.listener = this@LCWrongNoteFragment
        binding.memoView.set(noteActivity.binding.pencilcaseView)

        val pencilType = noteActivity.getPencilcaseType()
        val color = noteActivity.getPencilcaseColor()
        val thickn = noteActivity.getPencilcaseThickness()
        val isFixedMode = noteActivity.getPencilcaseMode()

        noteActivity.binding.pencilcaseView.apply {

            if (isFixedMode) {
                editType = pencilType
                if (color != null) {
                    penColor = color
                }
                if (thickn != null) {
                    thickness = thickn
                }
                writeModeSwitch.isChecked = isFixedMode

                val isBlocked = pencilType != null
                binding.leftScrollView.isBlock = isBlocked
                (activity as LCWrongNoteActivity).setPagerSwipeBlocked(isBlocked)
            } else {
                editType = null
            }
            pencilOptionLl.isSelected = false
            pencilOptionLl.visibility = View.GONE
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
                DragEvent.ACTION_DRAG_ENDED -> {
                    var x = dragEvent.x
                    var y = dragEvent.y

                    if (y > rootCl.height) {
                        val answerHeight = floatingAnswerSheet.height
                        val answerWidth = floatingAnswerSheet.width

                        x =
                            dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
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

    override fun onEditTypeChanged(type: CookingPencilcase.EditType?) {
        val isBlocked = type != null
        binding.leftScrollView.isBlock = isBlocked
        binding.memoView.isBlocked = isBlocked

        (activity as LCWrongNoteActivity).setPagerSwipeBlocked(isBlocked)
        (activity as LCWrongNoteActivity).savePencilcaseType(type)
    }

    override fun onThicknessSelected(thickness: CookingPencilcase.Thickness) {
        (activity as LCWrongNoteActivity).savePencilcaseThicknesss(thickness)
    }
    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {
        (activity as LCWrongNoteActivity).savePencilcaseColor(color)
    }

    override fun onModeChanged(isFixedMode: Boolean) {
        (activity as LCWrongNoteActivity).savePencilcaseMode(isFixedMode)
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
    override fun onStop() {
        super.onStop()
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
}