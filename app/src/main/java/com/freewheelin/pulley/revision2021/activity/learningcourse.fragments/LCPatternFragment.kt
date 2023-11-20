package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.PatternQuizFragment
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCPatternViewModel
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentLearningCoursePatternBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.LCPatternScoring
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.utils.observeListOnce
import com.freewheelin.pulley.revision2021.views.LCTouchListener
import kotlinx.coroutines.*


class LCPatternFragment : Fragment(), LCTouchListener {

    companion object {
        val COURSE_DESC = "COURSE_DESC"
        fun newInstance(course: SingleCourseDesc) : LCPatternFragment {
            return LCPatternFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(COURSE_DESC, course)
                }
            }
        }
    }

    val binding: FragmentLearningCoursePatternBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learning_course_pattern, null, false)
    }
    private var tabFragments: MutableList<Fragment> = mutableListOf()
    val viewModel: LCPatternViewModel by viewModels()

    fun updateChildPatternQuiz(quiz: LCPatternQuiz, childIndex: Int) {
        when (childIndex) {
            0 -> return
            1 -> viewModel.patternQuiz1.postValue(quiz)
            2 -> viewModel.patternQuiz2.postValue(quiz)
            3 -> viewModel.patternQuiz3.postValue(quiz)
        }
        binding.apply {
            val child = childFragmentManager.fragments.find { it.tag.equals("f" + pagerWrapper.pager.adapter?.getItemId(childIndex)) }
            (child as PatternQuizFragment?)?.run {
                updateQuiz(quiz)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { it ->
            val course = it.getSerializable(COURSE_DESC) as SingleCourseDesc
            val courseId = course.learningCourseDetailId

            viewModel.initPatternInfo(courseId)
            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner
                pagerWrapper.listener = this@LCPatternFragment
                viewModel.setPatternName(course)
                viewModel.patternQuizList.observe(viewLifecycleOwner) {
                    it.forEachIndexed { index, quiz ->
                        updateChildPatternQuiz(quiz, index)
                    }
                }
                viewModel.selectedQuizIndex.observe(viewLifecycleOwner) { index ->
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        val children = childFragmentManager.fragments.filter { it.tag.equals("f" + pagerWrapper.pager.adapter?.getItemId(index)) }
                        children.forEach {
                            (it as PatternQuizFragment).run {
                                setHintBtn()
                                setTempConceptSolutionViewFlag()
                                resumeFloatingAnswerSheetLocation()
                            }
                        }
                    }
                }

                viewModel.patternQuizList.observeListOnce(this@LCPatternFragment) {
                    val frags = it.mapIndexed { index, quiz ->
                        PatternQuizFragment.newInstance(quiz, index, viewModel.patternQuizList.value?.size)
                    }
                    frags.let { tabFragments = it.toMutableList() }

                    childFragmentManager.fragments.forEach {
                        childFragmentManager.beginTransaction().remove(it).commit()
                    }
                    pagerWrapper.pager.adapter = LCPatternViewPagerAdapter(tabFragments, childFragmentManager, lifecycle)
                    pagerWrapper.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            viewModel.selectedQuizIndex.postValue(position)

                            when (position) {
                                0 -> {
                                    pagerWrapper.setStartIndex()
                                }
                                tabFragments.lastIndex -> {
                                    pagerWrapper.setEndIndex()
                                }
                                else -> {
                                    pagerWrapper.setMiddleIndex()
                                }
                            }
                        }
                    })
                }

                conceptSolutionToggleBtn.setOnClickListener {
                    (activity as LearningCourseActivity).hidePenPanel()
                    val children = childFragmentManager.fragments.filter { it.tag.equals("f" + pagerWrapper.pager.adapter?.getItemId(pagerWrapper.pager.currentItem)) }
                    children.forEach {
                        (it as PatternQuizFragment).toggleDrawer()
                    }
                }

                appendHintBtn.setOnClickListener {
                    viewModel.remainingHintSizeLive.value?.let { hintSize ->
                        viewModel.usePatternQuizHint {
                            val nextHintSize = hintSize - 1
                            viewModel.setHintBtnText(nextHintSize)

                            getChildrenPage().forEach {
                                val quizFrag = (it as PatternQuizFragment)
                                if (quizFrag.hasMoreHint()) {
                                    quizFrag.setNextHint(nextHintSize)
                                }
                            }
                        }
                    }
                }

                resetHintBtnLl.setOnClickListener {
                    viewModel.resetHint()

                    getChildrenPage().forEach {
                        val quizFrag = (it as PatternQuizFragment)
                        quizFrag.viewModel.resetQuizImage()
                    }
                }

                questionBtnLl.setOnClickListener {
                    (activity as LearningCourseActivity).hidePenPanel()

                    getChildrenPage().forEach {
                        val quizFrag = (it as PatternQuizFragment)
                        val patternName = viewModel.patternName.value ?: return@forEach
                        val chapterName = (activity as LearningCourseActivity).viewModel.headerTitle.value ?: return@forEach
                        val courseName = "[${chapterName}] : [${patternName}]"
                        quizFrag.openChannelIoDialog(courseName)
                    }
                }

                pagerWrapper.pagerEnableCallback = {
                    (activity as LearningCourseActivity).setPagerUserInputEnable(it)
                }
                headerCl.setOnClickListener {
                    (activity as LearningCourseActivity).hidePenPanel()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        arguments?.let { it ->
            val course = it.getSerializable(COURSE_DESC) as SingleCourseDesc
            val courseId = course.learningCourseDetailId

            viewModel.fetchPatternInfo(courseId)
        }
        (activity as LearningCourseActivity).run {
            showMainPanPanelIfPenSelected()
            mainPanelTopMarginByCourseType()
        }
    }

    fun getChildrenPage() = childFragmentManager.fragments.filter {
        it.tag.equals(
            "f" + binding.pagerWrapper.pager.adapter?.getItemId(binding.pagerWrapper.pager.currentItem)
        )
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(tabFragments[0].isAdded)
            childFragmentManager.putFragment(outState, "LC1", tabFragments[0])
        if(tabFragments[1].isAdded)
            childFragmentManager.putFragment(outState, "LC2", tabFragments[1])
        if(tabFragments[2].isAdded)
            childFragmentManager.putFragment(outState, "LC3", tabFragments[2])
        if(tabFragments[3].isAdded)
            childFragmentManager.putFragment(outState, "LC4", tabFragments[3])
    }

    fun setHintBtn(flag: Boolean?, size: Int?, hintExist: Boolean) {
        if (flag != null && size != null) {
            viewModel.isHintBtnDisabled.postValue(flag)
            viewModel.setHintBtnText(size)
            viewModel.hintExist.postValue(hintExist)
        }
    }
    fun setHintBtnDisabled(flag: Boolean) {
        viewModel.isHintBtnDisabled.postValue(flag)
    }

    fun setHintBtnText(size: Int) {
        viewModel.setHintBtnText(size)
    }

    fun setPagerToAnotherQuiz(quizId: Int) {
        val position = viewModel.getPagerPositionOnQuizId(quizId)
        if (!viewModel.isLastPagerPosition(position)) {
            val nextPagePosition = position + 1
            viewModel.setViewPagerPosition(binding.pagerWrapper, nextPagePosition)
        }
    }
    fun setPagerToPatternMap() {
        (activity as LearningCourseActivity).setPagerToPatternMap()
    }
    fun setPagerSwipeBlocked(blocked: Boolean) {
        binding.pagerWrapper.isPagerSwipeBlocked = blocked
    }
    fun setFingerDrawMode(value: Boolean) {
        binding.pagerWrapper.fingerDrawMode = value
    }
    fun setPagerUserInputEnabled(enabled: Boolean) {
        binding.pagerWrapper.pager.isUserInputEnabled = enabled
    }
    fun setConceptSolutionToggleBtnText(isOpened: Boolean) {
        binding.conceptSolutionToggleBtn.text = if (isOpened) "개념 | 정답 닫기" else "개념 | 정답 보기"
    }
    fun scoringPatternQuiz(scoring: LCPatternScoring) {
        viewModel.patternQuizList.value?.forEach { quiz ->
            if (quiz.patternQuizId == scoring.patternQuizId) {
                quiz.isCorrect = scoring.isCorrect
                quiz.isFirstTry = true
                viewModel.updatePatternQuizList()
            }
        }
    }
    fun resumeFloatingAnswerSheetLocation() {
        childFragmentManager.fragments.forEach {
            (it as PatternQuizFragment).run {
                resumeFloatingAnswerSheetLocation()
            }
        }
    }

    fun setQuizImageScale(scale: Float) {
        binding.pagerWrapper.scaleFactor = scale
    }
    fun setPatternPagerNextPage() {
        val pagerIndex = binding.pagerWrapper.pager.currentItem
        if (!viewModel.isPagerLastIndex()) {
            binding.pagerWrapper.pager.currentItem = pagerIndex + 1
        }
    }
    fun setPatternPagerPrevPage() {
        val pagerIndex = binding.pagerWrapper.pager.currentItem
        if (!viewModel.isPagerFirstIndex()) {
            binding.pagerWrapper.pager.currentItem = pagerIndex - 1
        }
    }

    inner class LCPatternViewPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
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

    override fun onGestureTouch() {
        (activity as LearningCourseActivity).hidePenPanel()
    }

}