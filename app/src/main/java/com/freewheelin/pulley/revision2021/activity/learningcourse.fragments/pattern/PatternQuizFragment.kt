package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCPatternFragment
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.pattern.PatternQuizViewModel
import com.freewheelin.pulley.revision2021.cookingmemo.PathRedoUndoCountChangeListener
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.databinding.FragmentPatternQuizBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.dialog.ChannelIoQuestionDialog
import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO
import com.freewheelin.pulley.revision2021.cookingmemo.storage.DatabaseHelper
import com.freewheelin.pulley.revision2021.model.LCPatternConcept
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.utils.*
import kotlinx.coroutines.*
import java.io.File

class PatternQuizFragment() : Fragment(),
    FloatingAnswerDelegate,
//    ProblemGestureListener,
    PatternScrollListener,
    CookingPencilcaseListener,
    PathRedoUndoCountChangeListener {

    val screenWidth by lazy { DisplayUtils.getScreenWidth(requireContext()) }
    val screenHeight by lazy { DisplayUtils.getScreenHeight(requireContext()) }

//    var problemGesture: ProblemGestures? = null

    companion object {
        val PARAM_QUIZ = "QUIZ"
        val PARAM_INDEX = "INDEX"
        val PARAM_SIZE = "SIZE"

        fun newInstance(patternQuiz: LCPatternQuiz, index: Int, size: Int?): PatternQuizFragment {
            return PatternQuizFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PARAM_QUIZ, patternQuiz)
                    putInt(PARAM_INDEX, index)
                    size?.let { putInt(PARAM_SIZE, it) }
                }
            }
        }
    }

    val binding: FragmentPatternQuizBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_pattern_quiz, null, false)
    }
    lateinit var viewModel: PatternQuizViewModel

    private var tabFragments: MutableList<Fragment> = mutableListOf()
    lateinit var db: DatabaseHelper

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
        viewModel = ViewModelProvider(this).get(PatternQuizViewModel::class.java)
        db = DatabaseHelper.get(requireContext())

        arguments?.let {
            val quiz = it.getSerializable(PARAM_QUIZ) as LCPatternQuiz
            val currQuizIndex = it.getInt(PARAM_INDEX)
            val quizSize = it.getInt(PARAM_SIZE, 0)
            viewModel.initQuiz(quiz, currQuizIndex, quizSize)

            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                viewModel.apply {
                    remainingHintSize.observe(viewLifecycleOwner) { size ->
                        (parentFragment as LCPatternFragment).setHintBtnDisabled(size == 0)
                    }

                    showConceptSolutionView.observe(viewLifecycleOwner) {
                        // ager 이동시 부모뷰에 텍스트 전달을 위해 observe 사용
                        (parentFragment as LCPatternFragment).setConceptSolutionToggleBtnText(it)
                    }

                    patternQuiz.observeOnce(this@PatternQuizFragment) {

                        val patternId = (parentFragment as LCPatternFragment).viewModel.patternId
                        memoView.removePathRedoUndoCountChangeListener()
                        memoView.setPathRedoUndoCountChangeListener(this@PatternQuizFragment)
                        memoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        memoView.setPatternMemoId(patternId, it.patternQuizId)
                        memoView.clearBitmap()
                        memoView.load()
                    }
                }


                pager.isUserInputEnabled = false

                leftScrollView.listener = this@PatternQuizFragment
                memoView.layoutParams.width = screenWidth

                if (viewModel.currQuizIndex == 0) {
                    viewModel.isQuizMainConcept.postValue(true)
                    tabFragments =
                        mutableListOf(PatternSolutionFragment.newInstance(quiz))
                    pager.adapter =
                        ConceptSolutionPagerAdapter(tabFragments, childFragmentManager, lifecycle)

                    val tabTitles = listOf<String>("정답 및 해설")
                    TabLayoutMediator(tabLayout, pager) { tab, position ->
                        tab.text = tabTitles[position]
                    }.attach()

                    setConceptDrawer(true)

                    //

                    addBaseConcept(quiz)
                    addRelatedConcepts(quiz)
//                    addQuizMainSolution(quiz)





                } else {
                    viewModel.isQuizMainConcept.postValue(false)
                    tabFragments = mutableListOf(
                        PatternConceptFragment.newInstance(quiz),
                        PatternSolutionFragment.newInstance(quiz)
                    )
                    pager.adapter =
                        ConceptSolutionPagerAdapter(tabFragments, childFragmentManager, lifecycle)

                    val tabTitles = listOf<String>("개념", "정답 및 해설")
                    TabLayoutMediator(tabLayout, pager) { tab, position ->
                        tab.text = tabTitles[position]
                    }.attach()
                    setConceptDrawer(false)
                }

                rootCl.setOnDragListener { view, dragEvent ->
                    // floating answer sheet 컨트롤
                    onFloatingAnswerSheetDragListener(view, dragEvent)
                }
                lifecycle.addObserver(floatingAnswerSheet)


                floatingAnswerSheet.doOnAttach { view ->
                    val sheet = view as FloatingAnswerSheet
                    viewModel.patternQuiz.observeOnce(this@PatternQuizFragment) {
                        sheet.configureUI(it)
                    }
                }

                floatingAnswerSheet.delegate = this@PatternQuizFragment
                floatingAnswerSheet.binding.scoringBtn.setOnClickListener {
                    if (viewModel.preventScoringBtnDoubleClickFlag) return@setOnClickListener
                    viewModel.apply {
                        preventScoringBtnDoubleClickFlag = true
                        quizScoring {
                            viewModel.preventScoringBtnDoubleClickFlag = false
                            (parentFragment as LCPatternFragment).scoringPatternQuiz(it)
                        }
                    }
                }

                if (quiz.isCorrect != null) {
                    controlDisabledFloatingAnswer(quiz, floatingAnswerSheet)
                }
                viewModel.currentAnswerOfSingle.observe(viewLifecycleOwner) {
                    // 값이 임력되었을떄 입력이 가능하게
                    floatingAnswerSheet.binding.scoringBtn.isEnabled = when (it) {
                        "" -> false
                        else -> true
                    }
                }
            }
        }
    }
    fun openChannelIoDialog (courseName: String) {
        binding.apply {

            val screenShotBitmap = leftScrollRootCl.getBitmap(leftScrollRootCl.width, leftScrollRootCl.height)

            val dialog = ChannelIoQuestionDialog(requireContext(), screenShotBitmap) { radioMsg, additinalMsg ->
                val message = "${courseName}\n\n${radioMsg}\n\n${additinalMsg}"
                (activity as? LearningCourseActivity)?.let { lcActivity ->
                    lcActivity.getFileImageAsCache(screenShotBitmap)?.let {

                        val chatId = Preferences.channelTalkCurrChatId.get()
                        val studentIdWhenIssuingChatId = Preferences.studentIdWhenIssuingChatId.get()
                        val currentStudentId = user?.studentID ?: ""

                        if (studentIdWhenIssuingChatId == currentStudentId && chatId.isNotEmpty()) {
                            postImageMessage(it, message, false)
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    PChannelIO.openChat(activity, null, "")
                                }
                                delay(1500)
                                postImageMessage(it, message, true)
                            }
                        }
                    }
                }
            }
            childFragmentManager.let { dialog.show(it, "ChannelIoQuestionDialog") }


        }
    }

    fun addBaseConcept(quiz: LCPatternQuiz) {
        binding.apply {
            quiz.concepts.filter {
                it.conceptTypeEnum == LCPatternConcept.ConceptType.base
            }?.forEach {
                val iv = ImageView(context)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, 16.toPx(), 0, 0)

                iv.layoutParams = layoutParams
                iv.id = View.generateViewId()
                iv.setImageUrlGlide(it.conceptImageUrl)
                conceptScrollRootLl.addView(iv)
            }
        }
    }

    fun addRelatedConcepts(quiz: LCPatternQuiz) {
        binding.apply {
            val relatedConcepts = quiz.concepts.filter {
                it.conceptTypeEnum == LCPatternConcept.ConceptType.related
            }

            if (relatedConcepts.isNotEmpty() == true) {
                addReleatedTextView()
            }

            relatedConcepts.forEach {
                val iv = ImageView(context)
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, 16.toPx(), 0, 0)

                iv.layoutParams = layoutParams
                iv.id = View.generateViewId()
                iv.setImageUrlGlide(it.conceptImageUrl)
                conceptScrollRootLl.addView(iv)
            }
        }
    }
    fun addReleatedTextView() {
        binding.apply {
            val tv = TextView(context).apply {
                val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.setMargins(0, 36.toPx(), 0, 0)
                layoutParams = lp
                id = View.generateViewId()
                setTextAppearance(R.style.mo_h2)
                text = "연관 개념"
            }
            conceptScrollRootLl.addView(tv)
        }
    }
    fun addQuizMainSolution(quiz: LCPatternQuiz) {
        binding.apply {
            val iv = ImageView(context)
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16.toPx(), 0, 0)

            iv.layoutParams = layoutParams
            iv.id = View.generateViewId()
            val url = quiz.solutionImageUrl
            iv.setImageUrlGlide(url)
            solutionScrollRootLl.addView(iv)
        }
    }
    fun postImageMessage(file: File, msg: String, isChatOpened: Boolean) {
        (activity as? LearningCourseActivity)?.apply {
            viewModel.uploadImageCaptureFile(file) {
                it?.let { uploadRes ->
                    viewModel.currChannelIOImage = uploadRes
                    val chatId = Preferences.channelTalkCurrChatId.get()

                    CoroutineScope(Dispatchers.IO).launch {
                        if (!isChatOpened) {
                            withContext(Dispatchers.Main) {
                                PChannelIO.openChat(activity, chatId, null)
                            }
                        }
                        delay(1000)
                        viewModel.postChannelIoCapturedImageMessage(uploadRes) {
                            viewModel.postChannelIoTextMessage(msg) {
                            }
                        }
                    }
                }
            }
        }
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

                    var x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
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
                    (activity as LearningCourseActivity).resumeLCPatternFloatingAnswerSheetLocation()
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    floatingAnswerSheet.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        floatingAnswerSheet.cancelDragAndDrop()
                    }
                }
                else -> {}
            }
        }
        return true
    }
    fun controlDisabledFloatingAnswer(quiz: LCPatternQuiz, sheet: FloatingAnswerSheet) {
        sheet.binding.selectionAnswerView.setAnswerByRawString(quiz.userAnswer)
        quiz.userAnswer?.let { sheet.binding.shortAnswerView.setText(it, TextView.BufferType.EDITABLE) }
    }

    override fun onResume() {
        super.onResume()
//        setHintBtn()
        resetMemoView()
        resumePencilcaseView()

//        setTempConceptSolutionViewFlag()
    }

    fun setHintBtn() {
        val size = viewModel.remainingHintSize.value
        (parentFragment as LCPatternFragment).setHintBtn(size == 0, size)
    }

    private fun resetMemoView() {
        (activity as? LearningCourseActivity)?.apply {
            binding.pencilcaseView.memoViews.clear()
        }
    }
    private fun resumePencilcaseView() {
        (activity as? LearningCourseActivity)?.let { lcActivity ->
            lcActivity.binding.pencilcaseView.listener = this@PatternQuizFragment
            binding.memoView.set(lcActivity.binding.pencilcaseView)

            val pencilType = lcActivity.getPencilcaseType()
            val color = lcActivity.getPencilcaseColor()
            val thickn = lcActivity.getPencilcaseThickness()
            val isFixedMode = lcActivity.getPencilcaseMode()

            lcActivity.binding.pencilcaseView.apply {

                if (isFixedMode) {
                    editType = pencilType
                    if (color != null) { penColor = color }
                    if (thickn != null) { thickness = thickn }
                    writeModeSwitch.isChecked = isFixedMode

                    val isBlocked = pencilType != null
                    binding.leftScrollView.isBlock = isBlocked
                    (parentFragment as? LCPatternFragment)?.setPagerSwipeBlocked(isBlocked)
                } else {
                    editType = null
                }
                pencilOptionLl.isSelected = false
                pencilOptionLl.visibility = View.GONE
            }
        }
    }
    fun setTempConceptSolutionViewFlag() {
        viewModel.showConceptSolutionView.postValue(viewModel.tempConceptSolutionViewFlag)
    }
    fun resumeFloatingAnswerSheetLocation() {
        if (viewModel.currQuizIndex != 0) {
            binding.floatingAnswerSheet.setInitPosition()
        }
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
    fun hasMoreHint(): Boolean {
        return viewModel.hasMoreHint()
    }
    fun setNextHint(remainingHintSize: Int) {
        viewModel.setNextHint(remainingHintSize)
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

    override fun onAnswerChanged(view: View, answer: String?) {
        viewModel.setCurrentAnswer(answer)
    }

    override fun onShortAnswerChanged(answer: String?) {
        viewModel.setCurrentAnswer(answer)
    }

    override fun onEditTypeChanged(type: CookingPencilcase.EditType?) {
        val isBlocked = type != null
        binding.leftScrollView.isBlock = isBlocked
        binding.memoView.isBlocked = isBlocked

        (activity as? LearningCourseActivity)?.savePencilcaseType(type)
        (parentFragment as? LCPatternFragment)?.setPagerSwipeBlocked(isBlocked)
    }

    override fun onThicknessSelected(thickness: CookingPencilcase.Thickness) {
        (activity as? LearningCourseActivity)?.savePencilcaseThicknesss(thickness)
    }
    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {
        (activity as? LearningCourseActivity)?.savePencilcaseColor(color)
    }

    override fun onModeChanged(isFixedMode: Boolean) {
        (activity as? LearningCourseActivity)?.savePencilcaseMode(isFixedMode)
    }

    override fun onScaleFactor(scale: Float) {
        (parentFragment as LCPatternFragment).setQuizImageScale(scale)
    }
    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onUndoCountChanged(count: Int) {
        (activity as? LearningCourseActivity)?.setUndoCount(count)
    }

    override fun onRedoCountChanged(count: Int) {
        (activity as? LearningCourseActivity)?.setRedoCount(count)
    }
}

class AnswerShadowBuilder(v: View): View.DragShadowBuilder(v) {
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val x = 40.toPx()/2 + view.resources.getDimension(R.dimen.dp16).toInt()
        val width: Int = view.width
        val height: Int = view.height
        size.set(width, height)
        touch.set(x, view.height / 2)
    }
}
