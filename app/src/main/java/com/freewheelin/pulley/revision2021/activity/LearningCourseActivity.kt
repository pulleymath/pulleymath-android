package com.freewheelin.pulley.revision2021.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.BaseActivity
import com.freewheelin.pulley.legacy.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.databinding.ActivityLearningCourseBinding
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.token
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.*
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.viewmodel.LearningCourseViewModel
import com.freewheelin.pulley.revision2021.views.BalloonCourseRoadView
//import com.freewheelin.pulley.revision2021.views.CookingPencilcase
import com.freewheelin.pulley.revision2023.model.PriorConcept
import com.freewheelin.pulley.revision2023.ui.fragment.PatternMapFragment
import com.freewheelin.pulley.revision2023.ui.fragment.PriorConceptFragment
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.ui.fragment.MainFragment
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.PenColorType
import com.freewheelin.pulley.revision2023.ui.view.PencilPanelListener
import com.freewheelin.pulley.revision2023.utils.listeners.ChatBotClientClickEventListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LearningCourseActivity: BaseActivity(),
    PencilPanelListener {

    companion object {
        val COURSE_DETAIL_ID = "COURSE_DETAIL_ID"
        val STUDY_CHAPTER_FLAG = "STUDY_CHAPTER"
        val STUDY_CHAPTER_NAME = "STUDY_CHAPTER_NAME"
        val SMALL_CHAPTER_INDEX = "SMALL_CHAPTER_INDEX"
//        val SUBJECT_ID = "SUBJECT_ID"
        val CHAPTER_ID = "CHAPTER_ID"
        val CHAPTER_NAME = "CHAPTER_NAME"
        val COOKING_ID = "COOKING_ID"
        val FROM_PRIOR_CONCEPT = "FROM_PRIOR_CONCEPT"

        val WHERE_ARE_YOU_FROM = "WHERE_ARE_YOU_FROM"
        val FROM_MAIN_TAB = 302
        val FROM_CONCEPT_TAB = 303
        val FROM_BASE_CONCEPT = 304

        fun getIntent(context: Context, chapterId: Int, chapterName: String) : Intent {
            return Intent(context, LearningCourseActivity::class.java).apply {
                putExtra(CHAPTER_ID, chapterId)
                putExtra(CHAPTER_NAME, chapterName)
            }
        }

        fun getIntent(context: Context, chapterId: Int, chapterName: String, cookingId: Int) : Intent {
            return Intent(context, LearningCourseActivity::class.java).apply {
                putExtra(CHAPTER_ID, chapterId)
                putExtra(CHAPTER_NAME, chapterName)
                putExtra(COOKING_ID, cookingId)
                putExtra(FROM_PRIOR_CONCEPT, true)
            }
        }

        fun getIntent(context: Context, priorConcept: PriorConcept) : Intent {
            val chapterId = priorConcept.priorConceptChapterId
            val chapterName = priorConcept.name
            val cookingId = priorConcept.priorConceptCookingId
            return Intent(context, LearningCourseActivity::class.java).apply {
                putExtra(CHAPTER_ID, chapterId)
                putExtra(CHAPTER_NAME, chapterName)
                putExtra(COOKING_ID, cookingId)
                putExtra(FROM_PRIOR_CONCEPT, true)
            }
        }
    }

    val binding: ActivityLearningCourseBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_learning_course,null,false)
    }
    val viewModel: LearningCourseViewModel by viewModels()

    private var tabFragments: MutableList<Fragment> = mutableListOf()
    var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    private fun hideSystemUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(binding.root)
        ConceptLearningUsageMonitor.startConceptLearningUsage()
        addBackBtnCallback()

        val selectedChapterId = intent.getIntExtra(CHAPTER_ID, -1)
        val selectedChapterName = intent.getStringExtra(CHAPTER_NAME) ?: ""
        val fromPriorConceptScene = intent.getBooleanExtra(FROM_PRIOR_CONCEPT, false)
        val cookingId = intent.getIntExtra(COOKING_ID, -1)

        binding.apply {
            lifecycleOwner = this@LearningCourseActivity
            vm = viewModel
            isTablet = this@LearningCourseActivity.isTablet
            viewModel.setLessonHeaderTitle(selectedChapterName)
            viewModel.fetchCourseList(selectedChapterId) {
                if (fromPriorConceptScene) goCookingIfPriorConceptCourse(it, cookingId)
            }
            viewModel.isPriorConceptScene.postValue(fromPriorConceptScene)

//            pencilcaseView.listener = this@LearningCourseActivity

            initChatBot()
            backBtn.setOnClickListener {
//                onBackPressed()
                backBtnAction()
            }
            penPanel.isCookingMemo = true
            externalPenBtn.setOnClickListener {
                penPanel.visibleIf(!penPanel.isVisible)
                if (penPanel.isVisible) {
                    penPanel.openPencilPanel()
                    penPanel.setMarginTop(if (isCourseTypePattern()) 80 else 24)
                    externalPenBtn.setImageResource(R.drawable.ic_pencil_fliled_purple)
                } else {
                    penPanel.closePencilPanel()
                    externalPenBtn.setImageResource(R.drawable.ic_pencil)
                }
            }
            // 헤더 ripple 분리하려면 각 버튼마다 따로붙여야함
            headerPriorConceptCl.setOnClickListener { sourceView ->
                hidePenPanel()
                showHeaderNaviView(CourseType.PriorConcept, headerPriorConceptCl)
            }
            headerCookingCl.setOnClickListener { sourceView ->
                hidePenPanel()
                showHeaderNaviView(CourseType.Cooking, headerCookingCl)
            }
            headerPatternCl.setOnClickListener { sourceView ->
                hidePenPanel()
                showHeaderNaviView(CourseType.Pattern, headerPatternCl)
            }
            headerWrongNoteCl.setOnClickListener { sourceView ->
                hidePenPanel()
                viewModel.naviViewDismiss()
                setPagerToWrongNoteMap()
                binding.naviFl.removeAllViews()
            }
            headerCl.setOnClickListener {
                hidePenPanel()
            }

            viewModel.selectedPagerIndex.observe(this@LearningCourseActivity) {
                resumeLCPatternFloatingAnswerSheetLocation()
                val courseType = viewModel.getCourseTypeByPosition(it)

//                pencilcaseView.hasOneMemoPerPage(courseType == CourseType.Pattern)
                // TODO 페이지 타입별로 external onoff
//                pencilcaseView.courseType = viewModel.getCourseTypeByPosition(it) ?: CourseType.PriorConcept
            }

            navPrevBtn.setOnClickListener {
                viewModel.courseContentTable.value?.let {
                    val pagerIndex = binding.pager.currentItem
                    val currCourse = it[pagerIndex]
                    when (currCourse.courseType) {
                        CourseType.Pattern -> {
                            (tabFragments[pagerIndex] as LCPatternFragment).let { frag ->
                                if (frag.viewModel.isPagerFirstIndex()) {
                                    binding.pager.currentItem = binding.pager.currentItem - 1
                                } else {
                                    frag.setPatternPagerPrevPage()
                                }
                            }
                        }
                        else -> {
                            if (pagerIndex == 0) {
                                Toast.makeText(this@LearningCourseActivity, "첫 페이지입니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                binding.pager.currentItem = binding.pager.currentItem - 1
                            }
                        }
                    }

                }
            }
            navNextBtn.setOnClickListener {
                // 현재 아이템의 타입이 패턴이 아닌경우 페이저 +1
                // 패턴인경우 패턴문제가 +1
                // 패턴인데 패턴문제가 라스트인덱스인 경우 페이저 + 1
                viewModel.courseContentTable.value?.let {
                    val pagerIndex = binding.pager.currentItem
                    val currCourse = it[pagerIndex]
                    when (currCourse.courseType) {
                        CourseType.Pattern -> {
                            (tabFragments[pagerIndex] as LCPatternFragment).let { frag ->
                                if (frag.viewModel.isPagerLastIndex()) {
                                    binding.pager.currentItem = binding.pager.currentItem + 1
                                } else {
                                    frag.setPatternPagerNextPage()
                                }
                            }
                        }
                        else -> {
                            if (it.lastIndex == pagerIndex) {
                                Toast.makeText(this@LearningCourseActivity, "마지막 페이지입니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                binding.pager.currentItem = binding.pager.currentItem + 1
                            }
                        }
                    }
                }
            }
            naviFl.setOnClickListener {
                viewModel.naviViewDismiss()
            }

            viewModel.courseContentTable.observeOnce(this@LearningCourseActivity) {
                viewModel.setCurrentCourseType(0)

                val priprConceptMapFrag = PriorConceptFragment.newInstance(selectedChapterName)
                val frags = it.mapIndexedNotNull { index, course ->
                    val courseDetailId = course.learningCourseDetailId
                    when (course.courseType) {
                        CourseType.Cooking -> LCCookingFragment.newInstance(courseDetailId)
                        CourseType.PatternMap -> PatternMapFragment.newInstance(courseDetailId)
                        CourseType.Pattern -> LCPatternFragment.newInstance(course)
                        CourseType.WrongNoteMap -> LCWrongNoteMapFragment.newInstance()
                        else -> null
                    }
                }

                frags.let { tabFragments.addAll(listOf(priprConceptMapFrag) + it) }

                pager.adapter = LCViewPagerAdapter(tabFragments, supportFragmentManager, lifecycle)
                pager.offscreenPageLimit = 1
                pager.setOnTouchListener { view, motionEvent ->
                    true
                }
            }

            onPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.currPagerPosition = position
                    viewModel.selectedPagerIndex.postValue(position)
                    viewModel.setCurrentCourseType(position)

                    setPagerIndexes(position)
                }

                private fun setPagerIndexes(position: Int) {

                    viewModel.apply {
                        val pagerLastIndex = courseContentTable.value?.lastIndex
                        isPagerFirstIndex.postValue(position == 0)
                        isPagerLastIndex.postValue(position == pagerLastIndex)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == 0) {
                        viewModel.getCourseTypeByPosition(viewModel.currPagerPosition)?.let { courseType ->
                            when(courseType) {
                                CourseType.Pattern -> {
                                    binding.pager.isUserInputEnabled = false
                                }
                                else -> { binding.pager.isUserInputEnabled = true }
                            }
                        }
                    }
                    super.onPageScrollStateChanged(state)
                }
            }
            pager.registerOnPageChangeCallback(onPageChangeCallback as ViewPager2.OnPageChangeCallback)
            viewModel.isPriorConceptNaviSelected.observe(this@LearningCourseActivity) {
                setCourseHeaderAnim(priorConceptDropdownIv, it)
            }
            viewModel.isCookingNaviSelected.observe(this@LearningCourseActivity) {
                setCourseHeaderAnim(cookingDropdownIv, it)
            }
            viewModel.isPatternNaviSelected.observe(this@LearningCourseActivity) {
                setCourseHeaderAnim(patternDropdownIv, it)
            }
        }
    }

    fun initChatBot() {
        binding.apply {
            chatBotBtn.setInitPosition()
            chatBotBtn.setOnClickListener {
                if (chatBotBgCl.isVisible) {
                    chatBotBgCl.visibleIf(false)
//                    println("aspasp windowhasmemo ???")
//                    webView.evaluateJavascript("window.hasMemo(true)", null)

                } else {
                    val infoStr = if (viewModel.chatBotInfo == null) "" else viewModel.chatBotInfo.toString()
                    val url = Network.webAppUrl + "/ottway?token=$token&uri=chat-bot?initInfo=${infoStr}"
                    webView.loadUrl(url)
                    chatBotBgCl.visibleIf(true)
                    chatBotCv.visibleIf(true)
                }
            }
            chatBotBtn.removeDragListener()
            rootView.setOnDragListener { view, dragEvent ->
                val dragState = (dragEvent.localState as? View)?.id ?: -1
                if (chatBotBtn.id == dragState) return@setOnDragListener binding.chatBotBtn.addDragListener(dragEvent)

                true
            }
            webView.let {
                val onClose = {
                    runOnUiThread { chatBotBgCl.visibleIf(false) }
                }
                it.autoCloseOnChatBotExit(onClose)
                it.enableConsoleLogging()
                val onMemoExist: () -> Boolean = {
                    viewModel.isMemoSavedImageOrStrokeExist
                }
                val onAnalyzedMemo: () -> String = {
                    val memoBase64 = supportFragmentManager.fragments.map {
                        (it as? LCPatternFragment)?.run {
                            return@map getResumedMemoOnBase64()
                        }
                    }.find { (it ?: "").isNotEmpty() } ?: ""

                    memoBase64
                }
                it.addJavascriptInterface(ChatBotClientClickEventListener (
                    onCloseListener = onClose,
                    errorCloseListener = onClose,
                    analyzedMemoListener = onAnalyzedMemo,
                    isMemoExistListener = onMemoExist
                ), "android")

                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
            }

        }

    }


    private fun goCookingIfPriorConceptCourse(courseContentTable: List<SingleCourseDesc>, cookingId: Int) {

        val targetCookingIndex = courseContentTable.let { list ->
            list.forEachIndexed { index, singleCourseDesc ->
                val isEqualType = singleCourseDesc.courseType == CourseType.Cooking
                val isEqualId = singleCourseDesc.learningCourseDetailId == cookingId
                if (isEqualType && isEqualId) return@let index
            }
            return@let -1
        }

        if (targetCookingIndex != -1) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                binding.pager.currentItem = targetCookingIndex
                viewModel.currentCourseType.postValue(CourseType.Cooking)
            }
        }
    }

    private fun backBtnAction() {
        if (binding.chatBotBgCl.isVisible) {
            binding.chatBotBgCl.visibleIf(false)
            binding.chatBotCv.visibleIf(false)
            binding.chatBotBtn.startLongClickDescAnim()
        } else {
            finishWithResult()
        }
    }
    fun finishWithResult() {
        setResultFromWhere()
        finish()
    }
    private fun setResultFromWhere() {
        val fromWhere = intent.getIntExtra(SolveActivity.WHERE_ARE_YOU_FROM, -1)
        when (fromWhere) {
            FROM_MAIN_TAB -> {
                setResult(MainFragment.SOLVE_RESULT)
            }
            FROM_CONCEPT_TAB -> {
                setResult(FROM_CONCEPT_TAB)
            }
        }
    }
    fun addBackBtnCallback() {
        onBackPressedDispatcher.addCallback(this) {
            backBtnAction()
        }
    }

    fun setCourseHeaderAnim(view: View, flag: Boolean) {
        if (!flag && view.rotation != -60f) return
        val startAngle = if (flag) 0f else -60f
        val endAngle = if (flag) -60f else 0f

        val rotateAnim = ObjectAnimator.ofFloat(view, "rotation", startAngle, endAngle)
        rotateAnim.duration = 300
        rotateAnim.start()

        rotateAnim.doOnEnd {
            view.rotation = if (flag) -60f else 0f
        }
    }

    fun showHeaderNaviView(selectedType: CourseType, sourceView: View) {
        binding.naviFl.removeAllViews()
        viewModel.setNaviFlag(selectedType)
        val courseList = viewModel.getHeaderCourseListOnType(selectedType)
        val currentCourse = viewModel.courseContentTable.value?.get(binding.pager.currentItem)
        val roadView = BalloonCourseRoadView(this)
        roadView.setPeakViewBias(selectedType)
        roadView.setCourseList(courseList, currentCourse) { course ->
            if (viewModel.isPriorConcept(course)) {
                val chapterId = course.targetChapterId ?: -1
                viewModel.createLearningCourseOnStudentId(chapterId) {
                    val name = course.name ?: ""
                    val cookingId = course.targetConceptCookingId ?: -1
                    startActivity(getIntent(this, chapterId, name, cookingId))
                }
            } else {
                binding.pager.currentItem = viewModel.trimPosition(course)
                viewModel.currentCourseType.postValue(selectedType)
            }
            viewModel.naviViewDismiss()
            binding.naviFl.removeAllViews()
        }
        roadView.setOnMapBtnClickListener(selectedType) {
            val position = when (it) {
                CourseType.PriorConceptMap -> 0
                CourseType.PatternMap -> viewModel.getPatternMapPosition()
                else -> 0
            }
            viewModel.naviViewDismiss()
            binding.naviFl.removeAllViews()
            binding.pager.currentItem = position
            viewModel.currentCourseType.postValue(selectedType)
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT

            val outLocation = IntArray(2)
            sourceView.getLocationInWindow(outLocation)
            val sourceX = outLocation[0]
            val roadViewWidth = 368.toPx()
            val sourceViewWidth = sourceView.width
            val startMargin = sourceX - (roadViewWidth / 2) + (sourceViewWidth / 2)
            marginStart = startMargin
        }

        roadView.layoutParams = params

        binding.naviFl.addView(roadView)
        viewModel.showNaviView()
    }

    private fun saveImageAsCache(image: Bitmap): Uri? {
        val imagesFolder = File(this.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "question_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            val provider = "com.freewheelin.fileprovider"
            uri = FileProvider.getUriForFile(this, provider, file)
        } catch (e: IOException) {
            println("saveImageAsCache 실패")
        }
        return uri
    }
    fun getFileImageAsCache(image: Bitmap): File? {
        val imagesFolder = File(this.cacheDir, "images")
        var file: File? = null
        try {
            imagesFolder.mkdirs()
            val tempFile = File(imagesFolder, "question_image.png")
            val stream = FileOutputStream(tempFile)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            file = tempFile
        } catch (e: IOException) {
            println("saveImageAsCache 실패")
        }
        return file
    }

    fun resumeLCPatternFloatingAnswerSheetLocation() {
        supportFragmentManager.fragments.forEach {
            (it as? LCPatternFragment)?.run {
                resumeFloatingAnswerSheetLocation()
            }
        }
    }
    fun setPagerToCookingFirstPage() {
        binding.pager.setCurrentItem(1, true)
        viewModel.currentCourseType.postValue(CourseType.Cooking)
    }
    fun setPagerToCookingId(cookingId: Int) {
        val position = viewModel.getPagerPositionOnCookingId(cookingId)
        binding.pager.setCurrentItem(position, false)
        viewModel.currentCourseType.postValue(CourseType.Cooking)
    }
    fun setPagerToPatternId(patternId: Int) {
        val position = viewModel.getPagerPositionOnPatternId(patternId)
        binding.pager.setCurrentItem(position, false)
        viewModel.currentCourseType.postValue(CourseType.Pattern)
    }
    fun setPagerToCooking() {
        binding.pager.currentItem = 1
        viewModel.currentCourseType.postValue(CourseType.Cooking)
    }

    fun setPatternFragRemainingHintSize(size: Int) {
        tabFragments.forEach {
            (it as? LCPatternFragment)?.let { frag ->
                frag.setHintBtnText(size)
            }
        }
    }
    fun setPagerToPatternMap() {
        val position = viewModel.getPatternMapPosition()
        binding.pager.setCurrentItem(position, false)
        viewModel.currentCourseType.postValue(CourseType.Pattern)
    }
    fun setPagerToWrongNoteMap() {
        val position = viewModel.getWrongNoteMapPosition()
        binding.pager.setCurrentItem(position, true)
        viewModel.currentCourseType.postValue(CourseType.WrongNoteMap)
    }
    fun setPagerUserInputEnable(enabled: Boolean) {
        binding.pager.isUserInputEnabled = enabled
    }
    fun savePencilcaseType(type: DrawType?) {
        viewModel.pencilDrawType = type
    }
    fun getPencilcaseType(): DrawType? {
        return viewModel.pencilDrawType
    }
    fun savePencilcaseColor(color: PenColorType) {
        viewModel.pencilColorType = color
    }
    fun getPencilcaseColor(): PenColorType? {
        return viewModel.pencilColorType
    }

    fun savePencilcaseThicknesss(thickness: Float) {
        viewModel.pencilThickness = thickness
    }
    fun getPencilcaseThickness(): Float? {
        return viewModel.pencilThickness
    }

    fun saveFingerDrawMode(value: Boolean) {
        viewModel.fingerDrawModeMode = value
    }
    fun getFingerDrawMode(): Boolean {
        return viewModel.fingerDrawModeMode
    }
    fun hideKeyboard (view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE ) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun mainPanelTopMarginByCourseType() {
        binding.penPanel.setMarginTop(if (isCourseTypePattern()) 80 else 24)

    }
    fun showMainPanPanelIfPenSelected() {
        if (binding.penPanel.isVisible) {
            binding.penPanel.mainPanelLl.visibleIf(true)
        }
    }
    fun hideMainPenPanel() {
        binding.penPanel.mainPanelLl.visibleIf(false)
    }
    fun hidePenPanel() {
        binding.penPanel.figurePanelCl.visibleIf(false)
        binding.penPanel.penOptionPanelCl.visibleIf(false)
        binding.penPanel.eraserPanelCl.visibleIf(false)
    }
    fun isPagerLastPage(): Boolean {
        val pagerIndex = binding.pager.currentItem
        val tabLastIndex = tabFragments.lastIndex
        return tabLastIndex == pagerIndex
    }
    fun isCourseTypePattern(): Boolean {
        return viewModel.getCourseTypeByPosition(viewModel.currPagerPosition) == CourseType.Pattern
    }

    fun setUndoCount(count: Int) {
        if (binding.penPanel.memoViews.size == 0) return
        val undoCount = binding.penPanel.memoViews.map {
            it.undoCount
        }.reduce { acc, next -> acc + next }

        binding.penPanel.undoCount = undoCount
    }
    fun setRedoCount(count: Int) {
        if (binding.penPanel.memoViews.size == 0) return
        val redoCount = binding.penPanel.memoViews.map {
            it.redoCount
        }.reduce { acc, next -> acc + next }

        binding.penPanel.redoCount = redoCount
    }
    fun setMemoImageAndPathUndoCountChanged(undoCount: Int) {
        viewModel.isMemoSavedImageOrStrokeExist = undoCount > 0
    }
    override fun onDestroy() {
        binding.pager.unregisterOnPageChangeCallback(onPageChangeCallback as ViewPager2.OnPageChangeCallback)
        super.onDestroy()
    }

    inner class LCViewPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

    override fun onResume() {
        super.onResume()
        ConceptLearningUsageMonitor.startConceptLearning(viewModel.selectedChapterId)
    }

    override fun onPause() {
        super.onPause()
        ConceptLearningUsageMonitor.pauseConceptLearning()
    }

    override fun onDrawTypeChanged(type: DrawType?) {
        binding.naviFl.removeAllViews()
    }

//    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {}
//
//    override fun onThicknessSelected(thickness: Float) {}

//    override fun onModeChanged(isFixedMode: Boolean) {}
    override fun onFingerDrawModeChanged(value: Boolean) {
        saveFingerDrawMode(value)
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}
