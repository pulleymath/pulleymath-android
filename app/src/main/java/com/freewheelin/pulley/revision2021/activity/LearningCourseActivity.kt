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
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.FileProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.ConceptLearningUsageMonitor
import com.freewheelin.pulley.databinding.ActivityLearningCourseBinding
import com.freewheelin.pulley.revision2021.activity.fragments.ConceptCourseFragment
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.*
import com.freewheelin.pulley.revision2021.channelio.ChannelIOWrapper
import com.freewheelin.pulley.revision2021.channelio.channel.view.custom.BlankFragment
import com.freewheelin.pulley.revision2021.channelio.channel.view.custom.ChatFragment
import com.freewheelin.pulley.revision2021.channelio.channel.view.custom.LoungeFragment
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.LCPriorConceptInfo
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.utils.observeOnce
import com.freewheelin.pulley.revision2021.viewmodel.LearningCourseViewModel
import com.freewheelin.pulley.revision2021.views.BalloonCourseRoadView
import com.freewheelin.pulley.revision2021.views.CookingPencilcase
import com.freewheelin.pulley.revision2021.views.CookingPencilcaseListener
import com.freewheelin.pulley.utils.*
import com.zoyi.channel.plugin.android.model.source.photopicker.PhotoItem
import com.zoyi.channel.plugin.android.open.listener.ChannelPluginListener
import com.zoyi.channel.plugin.android.open.model.PopupData
import io.channel.plugin.android.feature.chat.contract.ChatContract
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.absoluteValue

class LearningCourseActivity: BaseActivity(), LifecycleObserver, ChannelPluginListener,
    CookingPencilcaseListener {

    companion object {
        val COURSE_DETAIL_ID = "COURSE_DETAIL_ID"
        val STUDY_CHAPTER_FLAG = "STUDY_CHAPTER"
        val STUDY_CHAPTER_NAME = "STUDY_CHAPTER_NAME"
        val SMALL_CHAPTER_INDEX = "SMALL_CHAPTER_INDEX"
        val CHAPTER_ID = "CHAPTER_ID"
        val CHAPTER_NAME = "CHAPTER_NAME"
        val COOKING_ID = "COOKING_ID"
        val IS_PRIOR_CONCEPT = "IS_PRIOR_CONCEPT"

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
                putExtra(IS_PRIOR_CONCEPT, true)
            }
        }
        fun getIntent(context: Context, priorConcept: LCPriorConceptInfo) : Intent {
            val chapterId = priorConcept.priorConceptChapterId
            val chapterName = priorConcept.name
            val cookingId = priorConcept.priorConceptCookingId
            return Intent(context, LearningCourseActivity::class.java).apply {
                putExtra(CHAPTER_ID, chapterId)
                putExtra(CHAPTER_NAME, chapterName)
                putExtra(COOKING_ID, cookingId)
                putExtra(IS_PRIOR_CONCEPT, true)
            }
        }
    }

    val binding: ActivityLearningCourseBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_learning_course,null,false)
    }
    val viewModel: LearningCourseViewModel by viewModels()

    private var tabFragments: MutableList<Fragment> = mutableListOf()
    var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    var presenter: ChatContract.Presenter? = null

    private fun hideSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, binding.root).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }

        // https://cloudylab.blogspot.com/2015/02/android-full-screen.html

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(binding.root)
        ConceptLearningUsageMonitor.startConceptLearningUsage()

        ChannelIOWrapper.initialize(application, this)

        val selectedChapterId = intent.getIntExtra(CHAPTER_ID, -1)
        val selectedChapterName = intent.getStringExtra(CHAPTER_NAME) ?: ""
        val isPriorConceptScene = intent.getBooleanExtra(IS_PRIOR_CONCEPT, false)
        val cookingId = intent.getIntExtra(COOKING_ID, -1)

        binding.apply {
            lifecycleOwner = this@LearningCourseActivity
            vm = viewModel
            viewModel.setLessonHeaderTitle(selectedChapterName)
            viewModel.fetchCourseList(selectedChapterId) {
                if (isPriorConceptScene) goCookingIfPriorConceptCourse(it, cookingId)
            }
            viewModel.isPriorConceptScene.postValue(isPriorConceptScene)

            pencilcaseView.listener = this@LearningCourseActivity

            backBtn.setOnClickListener {
                onBackPressed()
            }
            // 헤더 ripple 분리하려면 각 버튼마다 따로붙여야함
            headerPriorConceptCl.setOnClickListener { sourceView ->
                hidePencilcasePanel()
                showHeaderNaviView(CourseType.PriorConcept, headerPriorConceptCl)
            }
            headerCookingCl.setOnClickListener { sourceView ->
                hidePencilcasePanel()
                showHeaderNaviView(CourseType.Cooking, headerCookingCl)
            }
            headerPatternCl.setOnClickListener { sourceView ->
                hidePencilcasePanel()
                showHeaderNaviView(CourseType.Pattern, headerPatternCl)
            }
            headerWrongNoteCl.setOnClickListener { sourceView ->
                hidePencilcasePanel()
                viewModel.naviViewDismiss()
                setPagerToWrongNoteMap()
                binding.naviFl.removeAllViews()
            }
            headerCl.setOnClickListener {
                hidePencilcasePanel()
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
                                    viewModel.currentCourseType.postValue(currCourse.courseType)
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
                                viewModel.currentCourseType.postValue(currCourse.courseType)
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
                                    viewModel.currentCourseType.postValue(currCourse.courseType)
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
                                viewModel.currentCourseType.postValue(currCourse.courseType)
                            }
                        }
                    }
                }
            }
            naviFl.setOnClickListener {
                viewModel.naviViewDismiss()
            }
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.currPagerPosition = position
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
            })

            viewModel.courseContentTable.observeOnce(this@LearningCourseActivity) {
                viewModel.setCurrentCourseType(0)
//                val chapter = viewModel.selectedChapter

                val priprConceptMapFrag = LCPriorConceptFragment.newInstance(selectedChapterName)
                val frags = it.mapIndexedNotNull { index, course ->
                    val courseDetailId = course.learningCourseDetailId
                    when (course.courseType) {
//                        CourseType.review -> LessonReviewFragment.newInstance(selectedChapter.name)
                        CourseType.Cooking -> LCCookingFragment.newInstance(courseDetailId)
                        CourseType.PatternMap -> LCPatternMapFragment.newInstance(courseDetailId)
                        CourseType.Pattern -> LCPatternFragment.newInstance(course)
                        CourseType.WrongNoteMap -> LCWrongNoteMapFragment.newInstance()
                        else -> null
                    }
                }

                frags.let { tabFragments.addAll(listOf(priprConceptMapFrag) + it) }

                pager.adapter = LCViewPagerAdapter(tabFragments, supportFragmentManager, lifecycle)

            }

            onPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {

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
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    binding.pager.currentItem = targetCookingIndex
                    viewModel.currentCourseType.postValue(CourseType.Cooking)

                }
            }
        }
    }

    override fun onBackPressed() {
        if (isChannelIoForeground) {
            beginBlackChannelIoFrame()
        } else {
            setResult(ConceptCourseFragment.RESULT_OK, intent)
            super.onBackPressed()
        }
    }

    var isChannelIoForeground = false
    fun beginLoungeFragment() {
        viewModel.showChannelIoFrame.postValue(true)
        isChannelIoForeground = true
        val fm = supportFragmentManager
        val fragmentA = LoungeFragment.showMessenger()
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.channelIoFrame, fragmentA).commitAllowingStateLoss()
    }

    fun beginChatFragment(chatId: String?, message: String?) {
        viewModel.showChannelIoFrame.postValue(true)
        isChannelIoForeground = true
        val fm = supportFragmentManager
        val fragmentB = ChatFragment.newInstance(chatId, message)
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.channelIoFrame, fragmentB).commitAllowingStateLoss()
    }

    fun beginBlackChannelIoFrame() {
        viewModel.showChannelIoFrame.postValue(false)
        isChannelIoForeground = false
        val fm = supportFragmentManager
        val fragmentC = BlankFragment()
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.channelIoFrame, fragmentC).commitAllowingStateLoss()
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
    fun savePencilcaseType(type: CookingPencilcase.EditType?) {
        viewModel.pencilcaseType = type
    }
    fun getPencilcaseType(): CookingPencilcase.EditType? {
        return viewModel.pencilcaseType
    }
    fun savePencilcaseColor(color: CookingPencilcase.PenColor) {
        viewModel.pencilcaseColor = color
    }
    fun getPencilcaseColor(): CookingPencilcase.PenColor? {
        return viewModel.pencilcaseColor
    }

    fun savePencilcaseThicknesss(thickness: CookingPencilcase.Thickness) {
        viewModel.pencilcaseThickness = thickness
    }
    fun getPencilcaseThickness(): CookingPencilcase.Thickness? {
        return viewModel.pencilcaseThickness
    }

    fun savePencilcaseMode(isFixedMode: Boolean) {
        viewModel.pencilcaseModeFixed = isFixedMode
    }
    fun getPencilcaseMode(): Boolean {
        return viewModel.pencilcaseModeFixed
    }

    fun hideKeyboard (view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE ) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun hidePencilcasePanel() {
        binding.pencilcaseView.pencilOptionLl.isSelected = false
        binding.pencilcaseView.pencilOptionLl.visibility = View.GONE
        binding.pencilcaseView.clearAllBtn.isSelected = false
        binding.pencilcaseView.clearAllBtn.visibility = View.GONE
    }
    fun isPagerLastPage(): Boolean {
        val pagerIndex = binding.pager.currentItem
        val tabLastIndex = tabFragments.lastIndex
        return tabLastIndex == pagerIndex
    }
    fun isCourseTypePattern(): Boolean {
        return viewModel.getCourseTypeByPosition(viewModel.currPagerPosition) == CourseType.Pattern
    }

    override fun onDestroy() {
        binding.pager.unregisterOnPageChangeCallback(onPageChangeCallback as ViewPager2.OnPageChangeCallback)
//        ChannelIO.shutdown()
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

    fun resetChatId(chatId: String?) {
        chatId?.let {
            val prevChatId = Preferences.channelTalkCurrChatId.get()
            if (it == prevChatId) {
                Preferences.channelTalkCurrChatId.set("")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            902 -> {
                if (resultCode == 12) {
                    this.presenter?.uploadFiles(data!!.getParcelableArrayListExtra<PhotoItem>("PHOTO_INTENT_KEY"))
                }
            }
            else -> {}
        }
    }
    override fun onShowMessenger() {
        println("channelIO, onShowMessenger")
    }

    override fun onHideMessenger() {
        println("channelIO, onHideMessenger")

    }

    override fun onChatCreated(chatId: String?) {
        println("channelIO, onChatCreated chatId : ${chatId}")
        Preferences.channelTalkCurrChatId.set(chatId ?: "")
        val studentId = user?.studentID ?: ""
        Preferences.studentIdWhenIssuingChatId.set(studentId)
    }

    override fun onBadgeChanged(count: Int) {
        println("channelIO, onBadgeChanged")
    }

    override fun onFollowUpChanged(data: MutableMap<String, String>?) {
        println("channelIO, onFollowUpChanged ")
        data?.forEach {
            println("channelIO, onFollowUpChanged data : ${it.key} : ${it.value}")
        }
    }

    override fun onUrlClicked(url: String?): Boolean {
        println("channelIO, onUrlClicked , url ${url}")
        return true
    }

    override fun onPushNotificationClicked(chatId: String?): Boolean {
        println("channelIO, onPushNotificationClicked")
        return true
    }

    override fun onPopupDataReceived(popupData: PopupData?) {
        println("channelIO, channelIO, onPopupDataReceived")
    }

    override fun onResume() {
        super.onResume()
        ConceptLearningUsageMonitor.startConceptLearning(viewModel.selectedChapterId)
    }

    override fun onPause() {
        super.onPause()
        ConceptLearningUsageMonitor.pauseConceptLearning()
    }

    override fun onEditTypeChanged(type: CookingPencilcase.EditType?) {
        binding.naviFl.removeAllViews()
    }

    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {}

    override fun onThicknessSelected(thickness: CookingPencilcase.Thickness) {}

    override fun onModeChanged(isFixedMode: Boolean) {}
}

@BindingAdapter("layout_margin_top_dimen")
fun setLayoutMarginTop(view: View, dimen: Float) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.topMargin = dimen.toInt()
        println("layout_margin_top_dimen, dimen :${dimen.toInt()}")
    }
}
@BindingAdapter("layout_margin_start_dimen")
fun setLayoutMarginBottom(view: View, dimen: Float) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.marginStart = dimen.toInt()
    }
}

@BindingAdapter("layout_margin_end_dimen")
fun setLayoutMarginEnd(view: View, dimen: Float) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        this.marginEnd = dimen.toInt()
    }
}

@BindingAdapter("imageview_tint")
fun ImageView.setImageTint(@ColorInt color: Int?) {
    color?.let {
        setColorFilter(it)
    }
}

@BindingAdapter("imagebtn_tint")
fun ImageButton.setImageTint(@ColorInt color: Int?) {
    color?.let {
        setColorFilter(it)
    }
}

@BindingAdapter("layout_margin_end_dimen_on_text_length")
fun setLayoutMarginEndOnTextLength(view: View, length: Int) {
    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        when (length) {
            3 -> { this.marginEnd = 43.toPx() }
            4 -> { this.marginEnd = 47.toPx() }
            5 -> { this.marginEnd = 51.toPx() }
            6 -> { this.marginEnd = 55.toPx() }
            7 -> { this.marginEnd = 59.toPx() }
            else -> { this.marginEnd = 59.toPx() }
        }
    }
}