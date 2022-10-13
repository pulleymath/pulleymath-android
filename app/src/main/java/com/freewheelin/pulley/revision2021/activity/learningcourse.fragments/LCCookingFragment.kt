package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.FacebookSdk.getApplicationContext
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentCookingQuizBinding
import com.freewheelin.pulley.databinding.FragmentLearningCourseCookingBinding
import com.freewheelin.pulley.databinding.ItemCookingItemBinding
import com.freewheelin.pulley.databinding.ItemCookingQuizDetailBinding
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.activity.dialog.CookingQuizAnswerSelectDialog
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import com.freewheelin.pulley.revision2021.model.CookingExercise
import com.freewheelin.pulley.revision2021.model.CookingInfoItem
import com.freewheelin.pulley.revision2021.model.CookingQuiz
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCCookingViewModel
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx
import kotlinx.coroutines.*
import kotlin.math.abs


class LCCookingFragment() : Fragment(),
    CookingPencilcaseListener {
    companion object {
        fun newInstance(courseId: Int) : LCCookingFragment {
            return LCCookingFragment().apply {
                arguments = Bundle().apply {
                    putInt(LearningCourseActivity.COURSE_DETAIL_ID, courseId)
                }
            }
        }
    }

    val binding: FragmentLearningCourseCookingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learning_course_cooking, null, false)
    }

    private lateinit var viewModel: LCCookingViewModel
    val screenWidth by lazy { DisplayUtils.getScreenWidth(requireContext()) }

    var isInitFragment = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isInitFragment = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LCCookingViewModel::class.java)
        arguments?.let {
            val courseId = it.getInt(LearningCourseActivity.COURSE_DETAIL_ID)

            binding.apply {
                vm = viewModel
                lifecycleOwner = viewLifecycleOwner

                viewModel.apply {
                    cookingInfo.observe(viewLifecycleOwner) {

                        val chapterId = it.chapterId
                        val cookingId = it.conceptCookingId
                        cookingMemoView.setCookingMemoId(chapterId, cookingId)
                        cookingMemoView.clearBitmap()
                        cookingMemoView.load()
                    }
                }

                viewModel.fetchCookingGroceries(courseId)
                rightRv.adapter = CookingAdapter()

                leftScrollRootCl.setOnTouchListener { view, motionEvent -> false }
                cookingMemoView.layoutParams.width = screenWidth


            }
        }

    }

    fun setHintImageToCooking(imageUrl: String) {
        viewModel.cookingImageUrl.postValue(imageUrl)
    }
    inner class CookingAdapter(): ListAdapter<CookingInfoItem, RecyclerView.ViewHolder>(DiffCallback<CookingInfoItem>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return CookingItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_cooking_item, parent, false))
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CookingItemHolder).bind(getItem(position), position)
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }
    }
    interface CookingItemClickListener {
        fun onItemClick(item: CookingInfoItem)
    }

    val quizMemoViewList: MutableList<CookingMemoView> = mutableListOf()
    var cookingItemBinding: ItemCookingItemBinding? = null
    inner class CookingItemHolder(private val itemBinding: ItemCookingItemBinding): RecyclerView.ViewHolder(itemBinding.root),
        CookingItemClickListener {
        lateinit var itemType: CookingInfoItem.ItemType
        lateinit var item: CookingInfoItem
        var quizItemWidth = screenWidth * 0.55 - 96.toPx()

        fun bind(item: CookingInfoItem, position: Int) {
            this.item = item

            itemBinding.apply {
                this.item = item
                itemType = item.type
                vm = viewModel
                listener = this@CookingItemHolder
                setAllContainerViewGone(this)
                setQuizWidth(itemBinding)

                rightRvItemRoot.setOnClickListener {
                    (activity as LearningCourseActivity).hidePencilcasePanel()
                }

                when (item.type) {
                    CookingInfoItem.ItemType.Video -> {
                        videoContainer.visibility = View.VISIBLE

                        webView.setOnTouchListener { view, motionEvent ->
                            (activity as LearningCourseActivity).hidePencilcasePanel()
                            false
                        }

                        addVideo(item)

                    }
                    CookingInfoItem.ItemType.Exercise -> {
                        cookingItemBinding = itemBinding
                        exerciseContainer.visibility = View.VISIBLE
                        item.exerciseList?.let {
                            addExerciseBtn(it)
                            exerciseLastIndex = it.lastIndex
                            cookingHorizontalSv.setTabIndexStatus(it.lastIndex, 0)
                        }

                        viewModel.apply {
                            cookingInfo.observe(viewLifecycleOwner) {

                                val chapterId = it.chapterId
                                val cookingId = it.conceptCookingId
                                quizMemoViewList.clear()
                                listOfNotNull(
                                    viewModel.cookingExercise0.value,
                                    viewModel.cookingExercise1.value,
                                    viewModel.cookingExercise2.value,
                                    viewModel.cookingExercise3.value,
                                    viewModel.cookingExercise4.value,
                                ).forEachIndexed { index, cookingExercise ->
                                    getQuizBindingOnIndex(index).apply {
                                        cookingMemoView.setMemoSavedName(chapterId, cookingId, "cooking_quiz_${index}")
                                        cookingMemoView.clearBitmap()
                                        cookingMemoView.load()
                                        quizMemoViewList.add(cookingMemoView)
                                    }
                                }

                            }
                        }


                        listOfNotNull(
                            viewModel.cookingExercise0.value,
                            viewModel.cookingExercise1.value,
                            viewModel.cookingExercise2.value,
                            viewModel.cookingExercise3.value,
                            viewModel.cookingExercise4.value,
                        ).forEachIndexed { index, cookingExercise ->
                            val quizBinding = getQuizBindingOnIndex(index)
                            setOnQuizView(quizBinding, cookingExercise)
                        }
                    }
                    else -> {}
                }
            }
        }
        private fun addExerciseBtn(list: List<CookingExercise>) {
            val tabTitles = list.mapIndexed { index, cookingExercise -> "예제${index+1}\n${cookingExercise.name}" }
            tabTitles.forEachIndexed { index, title ->
                val exerciseBtn = CookingExerciseHeaderBtn(requireContext()).apply {
                    initUI(index, tabTitles.size)
                }

                exerciseBtn.setOnClickListener {
                    (activity as LearningCourseActivity).hidePencilcasePanel()

                    itemBinding.quizTabHeader.children.forEach {
                        (it as? CookingExerciseHeaderBtn)?.setStateCommon()
                    }

                    val btn = it as CookingExerciseHeaderBtn
                    btn.setStateSelected()
                    setHorizontalSvScrollAndTabSelect(btn.index)
                }
                itemBinding.quizTabHeader.addView(exerciseBtn)

                if (index != tabTitles.lastIndex) {
                    val arrowView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(8.toPx(), 8.toPx()).apply {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        setImageResource(R.drawable.ic_filled_arrow_right)
                    }
                    itemBinding.quizTabHeader.addView(arrowView)
                }
            }
        }

        fun setQuizWidth(itemBinding: ItemCookingItemBinding) {
            // itemWidth의 screenWidth 퍼센테이지는 1 - (leftScrollView의 width_percent)

            itemBinding.apply {
                cookingQuiz0.root.layoutParams.width = quizItemWidth.toInt()
                cookingQuiz1.root.layoutParams.width = quizItemWidth.toInt()
                cookingQuiz2.root.layoutParams.width = quizItemWidth.toInt()
                cookingQuiz3.root.layoutParams.width = quizItemWidth.toInt()
                cookingQuiz4.root.layoutParams.width = quizItemWidth.toInt()
            }
        }
        private fun getQuizBindingOnIndex(index: Int): FragmentCookingQuizBinding {
            return when (index) {
                0 -> itemBinding.cookingQuiz0
                1 -> itemBinding.cookingQuiz1
                2 -> itemBinding.cookingQuiz2
                3 -> itemBinding.cookingQuiz3
                4 -> itemBinding.cookingQuiz4
                else -> itemBinding.cookingQuiz0
            }
        }
        private fun setOnQuizView (quizBinding: FragmentCookingQuizBinding, excs: CookingExercise) {
            listOfNotNull(
                excs.cookingQuiz0,
                excs.cookingQuiz1,
                excs.cookingQuiz2,
                excs.cookingQuiz3,
                excs.cookingQuiz4,
            ).forEachIndexed { index, quiz ->
                val detailBinding = getDetailBindingOnIndex(quizBinding, index)
                setOnDetailView(detailBinding, quiz)
            }
        }
        private fun getDetailBindingOnIndex(quizBinding: FragmentCookingQuizBinding, index: Int): ItemCookingQuizDetailBinding {
            return when (index) {
                0 -> quizBinding.quizDetail0
                1 -> quizBinding.quizDetail1
                2 -> quizBinding.quizDetail2
                3 -> quizBinding.quizDetail3
                4 -> quizBinding.quizDetail4
                else -> quizBinding.quizDetail0
            }
        }
        private fun setOnDetailView (itemBinding: ItemCookingQuizDetailBinding, quiz: CookingQuiz) {
            itemBinding.let {
                it.hintBtn.setOnClickListener { view ->
                    val exercise = viewModel.findQuizExercise(quiz)
                    viewModel.useHint(quiz.exerciseQuizId) {
                        exercise?.exerciseQuizzes?.forEach { it.isHintUsed.set(false) }
                        quiz.isHintUsed.set(true)
                        setHintImageToCooking(quiz.hintImageUrl)
                    }
                    // 힌트 후 스크롤 무브 테스트했던 코드임
//                    CoroutineScope(Dispatchers.IO).launch {
//                        delay(500)
//                        withContext(Dispatchers.Main) {
//                            binding.leftScrollView.scrollTo(0, 300)
//                        }
//                    }
                }
                if (quiz.userAnswer == null) {
                    // 풀지 않은 문제
                    it.quizSingleAnswer.setOnClickListener { view ->
                        quiz.afterTryAnswered.set(true)
                        onSingleAnswerClick(quiz, view)
                    }

                    it.quizAnswerEt.setOnKeyListener { v, keyCode, event ->
                        onAnswerEditTextChange(v, keyCode, event, it, quiz)
                    }
                } else {
                    // 이미 푼 문제
                    it.quizAnswerEt.setText(quiz.userAnswer, TextView.BufferType.EDITABLE)
                }
            }
        }
        private fun onSingleAnswerClick (quiz: CookingQuiz, sourceView: View) {
            if (quiz.isAnswerEntered.get()) {
                return
            }

            val selectionImages = quiz.answerOptions.map { it.imageUrl }

            val dialog = CookingQuizAnswerSelectDialog(requireContext(), sourceView, selectionImages) { selectedContent ->
                val userAnswer = "${selectedContent.seq}"
                viewModel.scoringCookingQuiz(quiz, userAnswer) {
                    quiz.selectedQuizAnswerImageUrl.set(selectedContent.imageUrl)
                    quiz.isAnswerEntered.set(true)

                }
            }

            childFragmentManager.let { dialog.show(it, "CookingQuizAnswerSelectDialog") }
            childFragmentManager.executePendingTransactions()

            dialog.dialog?.setOnDismissListener {
                // dim click dismiss 일때
                quiz.afterTryAnswered.set(false)

            }
        }

        fun onAnswerEditTextChange (
            v: View,
            keyCode: Int,
            event: KeyEvent,
            itemQuizBinding: ItemCookingQuizDetailBinding,
            quiz: CookingQuiz
        ): Boolean {
            if (event.action == KeyEvent.ACTION_UP) {
                val result = when (event.keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {

                        val userAnswer = itemQuizBinding.quizAnswerEt.text.toString()
                        if (userAnswer.isEmpty()) {
                            (activity as LearningCourseActivity).hideKeyboard(v)
                            true
                        }
                        else {
                            viewModel.scoringCookingQuiz(quiz, userAnswer) {
                                (activity as LearningCourseActivity).hideKeyboard(v)
                                itemQuizBinding.quizAnswerEt.isEnabled = false
                                quiz.isAnswerEntered.set(true)
                            }

                            true
                        }
                    }
                    else -> { false }
                }
                return result
            }
            return false
        }

        fun addVideo(item: CookingInfoItem) {
            itemBinding.apply {

                val marginHorizontal = 64.toPx()
                val vWidth = (((screenWidth * 0.55) - marginHorizontal) / 16 * 9).toInt()

                val lp = webView.layoutParams
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = vWidth
                webView.layoutParams = lp

                webView.webViewClient = object: WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        (activity as LearningCourseActivity).hidePencilcasePanel()
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        // 페이지 로드 후 javascript단에서 함수를 생성해서 document를 컨트롤하는 자동재생 로직임
//                        webView.loadUrl("javascript:(function() { document.getElementsByClassName('ytp-large-play-button ytp-button')[0].click(); })()");

                        binding.rightViewProgress.visibility = View.GONE
                    }
                }
                webView.webChromeClient = CookingChromeClient()
                webView.settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                }
                val videoUrl = makeYoutubeUrl(item)
                webView.loadUrl(videoUrl)
            }
        }
        private fun makeYoutubeUrl(item: CookingInfoItem): String {
            val videoUUID = item.video.uuid
            val startTimeQuery = if(item.video.startTime == null) "" else "&start=${item.video.startTime}"
            val endTimeQuery = if(item.video.endTime == null) "" else "&end=${item.video.endTime}"
            return "https://www.youtube.com/embed/${videoUUID}?${startTimeQuery}${endTimeQuery}"
        }

        private fun setAllContainerViewGone(view: ItemCookingItemBinding) {
            view.apply {
                videoContainer.visibility = View.GONE
                exerciseContainer.visibility = View.GONE
            }
        }

        override fun onItemClick(item: CookingInfoItem) {
            when (item.type) {
                CookingInfoItem.ItemType.Video -> {}
                CookingInfoItem.ItemType.Exercise -> {}
            }
        }

        var prevTabPosition = 0
        var exerciseLastIndex = 0
        var prevXPosition: Double = 0.0

        fun setHorizontalSvScrollAndTabSelect(targetPosition: Int) {
            prevTabPosition = targetPosition
            val positionScrollX = quizItemWidth * targetPosition
            itemBinding.cookingHorizontalSv.setTabIndexStatus(exerciseLastIndex, targetPosition)

            CoroutineScope(Dispatchers.IO).launch {
                delay(100)
                withContext(Dispatchers.Main) {
                    itemBinding.cookingHorizontalSv.smoothScrollTo(positionScrollX.toInt(), 0)
                    prevXPosition = positionScrollX
                }
            }
        }
        fun getNewTabPosition(): Int {
            itemBinding.cookingHorizontalSv.apply {
                val isPageOverThreshold = abs(prevXPosition - scrollNewX) > 150
                val changeAmount = prevXPosition - scrollNewX
                val isRightSwipe = changeAmount < 0
                return when {
                    !isPageOverThreshold -> prevTabPosition
                    isRightSwipe -> prevTabPosition + 1
                    else -> prevTabPosition - 1
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resetMemoView()
        resumePencilcaseView()
    }

    private fun resetMemoView() {
        val lcActivity = (activity as LearningCourseActivity)
        lcActivity.binding.pencilcaseView.memoViews.clear()
    }

    private fun resumePencilcaseView() {
        val lcActivity = (activity as LearningCourseActivity)
        lcActivity.binding.pencilcaseView.listener = this@LCCookingFragment
        binding.cookingMemoView.set(lcActivity.binding.pencilcaseView)
        quizMemoViewList.forEach { it.set(lcActivity.binding.pencilcaseView) }

        val pencilType = lcActivity.getPencilcaseType()
        val color = lcActivity.getPencilcaseColor()
        val thickn = lcActivity.getPencilcaseThickness()
        val isFixedMode = lcActivity.getPencilcaseMode()

        lcActivity.binding.pencilcaseView.apply {

            if (isFixedMode) {
                editType = pencilType
                color?.let { penColor = it }
                thickn?.let { thickness = it }

                writeModeSwitch.isChecked = isFixedMode

                val isBlocked = pencilType != null
                binding.leftScrollView.isBlock = isBlocked
            } else {
                editType = null
            }
            pencilOptionLl.isSelected = false
            pencilOptionLl.visibility = View.GONE
            clearAllBtn.isSelected = false
            clearAllBtn.visibility = View.GONE
        }
    }


    override fun onEditTypeChanged(type: CookingPencilcase.EditType?) {
        val isBlocked = type != null
        binding.leftScrollView.isBlock = isBlocked
        binding.rightRv.isBlocked = isBlocked

        binding.cookingMemoView.isBlocked = isBlocked
        quizMemoViewList.forEach { it.isBlocked = isBlocked }

//        (parentFragment as LCPatternFragment).setPagerSwipeBlocked(isBlocked)
        (activity as LearningCourseActivity).savePencilcaseType(type)
    }

    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {
        (activity as LearningCourseActivity).savePencilcaseColor(color)
    }

    override fun onThicknessSelected(thickness: CookingPencilcase.Thickness) {
        (activity as LearningCourseActivity).savePencilcaseThicknesss(thickness)
    }

    override fun onModeChanged(isFixedMode: Boolean) {
        (activity as LearningCourseActivity).savePencilcaseMode(isFixedMode)
    }

    inner class CookingChromeClient: WebChromeClient() {
        // https://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview/56186877#56186877

        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0

        init {

        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(getApplicationContext().resources, 2130837573)
        }

        override fun onHideCustomView() {
            val activity = (activity as LearningCourseActivity)
            (activity.window.decorView as FrameLayout).removeView(
                mCustomView
            )
            mCustomView = null
            activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            activity.requestedOrientation = mOriginalOrientation
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: CustomViewCallback?
        ) {
            val activity = (activity as LearningCourseActivity)

            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility = activity.window.decorView.systemUiVisibility
            mOriginalOrientation = activity.requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            (activity.window.decorView as FrameLayout).addView(
                mCustomView,
                ViewGroup.LayoutParams(-1, -1)
            )
            activity.window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}

@BindingAdapter("bind_cooking_list")
fun bindCookingRecyclerView(recyclerView: RecyclerView, item: List<CookingInfoItem>?) {
    println("bind_cooking_list, size=${item?.size}")
    item?.let { itemList ->
        val adapter = recyclerView.adapter as LCCookingFragment.CookingAdapter
        adapter.submitList(itemList)
    }
}