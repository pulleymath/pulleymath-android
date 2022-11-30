package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.FacebookSdk.getApplicationContext
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCCookingViewModel
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.util.*

class LCCookingFragment() : Fragment(),
    CookingPencilcaseListener, PlusMinusEnterKeypadListener {
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
    var isRvLoaded = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.getInt(LearningCourseActivity.COURSE_DETAIL_ID)?.let {
            viewModel = ViewModelProvider(this).get(LCCookingViewModel::class.java)
            viewModel.fetchCookingGroceries(it)
        }

        isInitFragment = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            viewModel.apply {
                cookingInfo.observe(viewLifecycleOwner) {
                    val chapterId = it.chapterId
                    val cookingId = it.conceptCookingId
                    cookingMemoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    cookingMemoView.setCookingMemoId(chapterId, cookingId)
                    cookingMemoView.clearBitmap()
                    cookingMemoView.load()
                }
            }

//                viewModel.fetchCookingGroceries(courseId)
//                rightRv.adapter = CookingAdapter()

            leftScrollRootCl.setOnTouchListener { view, motionEvent -> false }
            cookingMemoView.layoutParams.width = leftScrollRootCl.layoutParams.width

            selectionFl.setOnClickListener {
                viewModel.showSelection.postValue(false)
                recoveryQuizSingleAnswer()
            }
            keyboardFl.setOnClickListener {
                viewModel.showNumkeyboard.postValue(false)
                viewModel.selectedShortQuiz.postValue(null)
                viewModel.selectedItemBinding = null
                binding.numberKeyboard.releaseKeyboard(null)
            }

            viewModel.cookingImageUrl.observe(viewLifecycleOwner) {
                CoroutineScope(Dispatchers.IO).launch {
                    val requestCreator = Picasso.get()
                        .load(it)
//                        .load("${it}?time=${Date().time}")

                    val width = requestCreator.get().width
                    val height = requestCreator.get().height
                    withContext(Dispatchers.Main) {

                        requestCreator
                            .resize(if (height > 5000) 3000 else width, 0)
                            .onlyScaleDown()
                            .into(exerciseIv, object: Callback {
                                override fun onSuccess() {
                                    if (!isRvLoaded) {
                                        isRvLoaded = true
                                        rightRv.adapter = CookingAdapter()
                                        viewModel.cookingList.postValue(viewModel.cookingList.value)
                                    }
                                }
                                override fun onError(e: java.lang.Exception?) {
                                    println("picasso downscale load error : ${e}")
                                }
                            })
                    }
                }
            }
        }
    }

    fun setHintImageToCooking(imageUrl: String) {
        viewModel.cookingImageUrl.postValue(imageUrl)
    }

    inner class CookingAdapter(): ListAdapter<CookingInfoItem, RecyclerView.ViewHolder>(DiffCallback<CookingInfoItem>()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return CookingItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_cooking_right_view, parent, false))
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CookingItemHolder).bind(getItem(position), position)
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    val quizMemoViewList: MutableList<CookingMemoView> = mutableListOf()

    var focusedQuizList: MutableList<CookingQuiz> = mutableListOf()
    inner class CookingItemHolder(private val itemBinding: ItemCookingRightViewBinding): RecyclerView.ViewHolder(itemBinding.root), CookingItemClickListener {
        var quizItemWidth = screenWidth * 0.55 - 96.toPx()
        fun bind(item: CookingInfoItem, position: Int) {
            itemBinding.apply {
                this.item = item
                vm = viewModel
                listener = this@CookingItemHolder
                lifecycleOwner = viewLifecycleOwner
                setAllContainerViewGone(this)
                rightRvItemRoot.setOnClickListener {
                    (activity as LearningCourseActivity).hidePencilcasePanel()
                }


                when (item.type) {
                    CookingInfoItem.ItemType.Video -> {
                        videoContainerCl.visibility = View.VISIBLE
                        viewModel.rightViewBinding = itemBinding
                        webView.setOnTouchListener { view, motionEvent ->
                            (activity as LearningCourseActivity).hidePencilcasePanel()
                            false
                        }

                        addVideo(item)
                        item.exerciseList?.let {
                            addExerciseBtn(it)
                        }

                        viewModel.apply {
                            selectedExerciseIndex.observe(viewLifecycleOwner) {
                                val selectedExercise =
                                    cookingList.value?.filter { it.type == CookingInfoItem.ItemType.Exercise }
                                        ?.first()
                                        ?.exerciseList
                                        ?.get(it)
                                viewModel.currentCookingExercise.postValue(selectedExercise)

                            }
                        }
                    }
                    CookingInfoItem.ItemType.Exercise -> {
                        exerciseContainer.visibility = View.VISIBLE

                        quizMemoViewList.clear()

                        viewModel.apply {
                            selectedExerciseIndex.observe(viewLifecycleOwner) { selectedIndex ->
                                item.cookingInfo?.let {
                                    val chapterId = it.chapterId
                                    val cookingId = it.conceptCookingId
                                    quizMemoViewList.clear()

                                    cookingQuizzes.cookingMemoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    cookingQuizzes.cookingMemoView.setMemoSavedName(chapterId,cookingId,"cooking_quiz_${selectedIndex}")
                                    cookingQuizzes.cookingMemoView.clearBitmap()
                                    cookingQuizzes.cookingMemoView.load()
                                    quizMemoViewList.add(cookingQuizzes.cookingMemoView)

                                    val lcActivity = (activity as LearningCourseActivity)
                                    lcActivity.binding.pencilcaseView.listener = this@LCCookingFragment
                                    binding.cookingMemoView.set(lcActivity.binding.pencilcaseView)
                                    quizMemoViewList.forEach { it.set(lcActivity.binding.pencilcaseView) }
                                }

                                cookingList.value?.filter { it.type == CookingInfoItem.ItemType.Exercise }
                                    ?.first()
                                    ?.exerciseList
                                    ?.get(selectedIndex)
                                    ?.let { selectedExercise ->
                                        val quizBinding = cookingQuizzes
                                        setOnQuizView(quizBinding, selectedExercise)
                                    }
                            }
                        }
                    }
                    CookingInfoItem.ItemType.Footer -> {
                        footerContainer.visibility = View.VISIBLE


                    }
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

                    viewModel.selectedExerciseIndex.postValue(index)
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

        private fun setAllContainerViewGone(view: ItemCookingRightViewBinding) {
            view.apply {
                videoContainerCl.visibility = View.GONE
                exerciseContainer.visibility = View.GONE
                footerContainer.visibility = View.GONE
            }
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
                        view?.evaluateJavascript(addMyClickCallBackJs(), null)
                    }

                    fun addMyClickCallBackJs(): String {
                        var js = "javascript:"
                        js += "function clickListener(event){" +
                            "if(event.target.className == null){androidInterface.clickListener(event.target.id)}" +
                            "else{androidInterface.clickListener(event.target.className)}}"
                        js += "document.addEventListener(\"click\",clickListener,true);"
                        return js
                    }
                }
                webView.webChromeClient = CookingChromeClient()
                webView.addJavascriptInterface(JsToAndroid(), "androidInterface")
                webView.settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                }
                val videoUrl = makeYoutubeUrl(item)
                println("YOUTUBE_URL, :${videoUrl}")
                webView.loadUrl(videoUrl)
            }
        }
        inner class JsToAndroid : Any() {
            @JavascriptInterface
            fun clickListener(idOrClass: String) {
                val largeRedPlayButton = "ytp-large-play-button ytp-button ytp-large-play-button-red-bg"
                val thumbnailImage = "ytp-cued-thumbnail-overlay-image"
                if (idOrClass == largeRedPlayButton || idOrClass == thumbnailImage) {
                    val cookingId = viewModel.cookingInfo.value?.conceptCookingId
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "풀리개념학습", "유튜브", "conceptCookingId_$cookingId")
                }
            }
        }
        private fun makeYoutubeUrl(item: CookingInfoItem): String {
            val videoUUID = item.video.uuid
            val startTimeQuery = if(item.video.startTime == null) "" else "&start=${item.video.startTime}"
            val endTimeQuery = if(item.video.endTime == null) "" else "&end=${item.video.endTime}"
            return "https://www.youtube.com/embed/${videoUUID}?${startTimeQuery}${endTimeQuery}"
        }
        private fun setOnQuizView (quizBinding: FragmentCookingQuizBinding, excs: CookingExercise) {
            listOfNotNull(
                excs.cookingQuiz0,
                excs.cookingQuiz1,
                excs.cookingQuiz2,
                excs.cookingQuiz3,
                excs.cookingQuiz4,
                excs.cookingQuiz5,
                excs.cookingQuiz6,
                excs.cookingQuiz7,
                excs.cookingQuiz8,
                excs.cookingQuiz9,
            ).forEachIndexed { index, quiz ->
                val detailBinding = getDetailBindingOnIndex(quizBinding, index)
                setOnDetailView(detailBinding, quiz)
            }
        }
        fun getDetailBindingOnIndex(quizBinding: FragmentCookingQuizBinding, index: Int): ItemCookingQuizDetailBinding {
            return when (index) {
                0 -> quizBinding.quizDetail0
                1 -> quizBinding.quizDetail1
                2 -> quizBinding.quizDetail2
                3 -> quizBinding.quizDetail3
                4 -> quizBinding.quizDetail4
                5 -> quizBinding.quizDetail5
                6 -> quizBinding.quizDetail6
                7 -> quizBinding.quizDetail7
                8 -> quizBinding.quizDetail8
                9 -> quizBinding.quizDetail9
                else -> quizBinding.quizDetail0
            }
        }

        private fun setOnDetailView (detailBinding: ItemCookingQuizDetailBinding, quiz: CookingQuiz) {
            detailBinding.let {
                it.hintBtn.setOnClickListener(null)
                it.hintBtn.setOnClickListener { view ->
                    val exercise = viewModel.currentCookingExercise.value
                    viewModel.useHint(quiz.exerciseQuizId) {
                        exercise?.exerciseQuizzes?.forEach { it.isHintUsed.set(false) }
                        quiz.isHintUsed.set(true)
                        setHintImageToCooking(quiz.hintImageUrl)
                    }
                }

                if (quiz.userAnswer == null) {
                    // 풀지 않은 문제
                    it.quizSingleAnswer.setOnClickListener(null)
                    it.quizSingleAnswer.isEnabled = true
                    it.quizAnswerBtn.isEnabled = true

                    it.quizSingleAnswer.setOnClickListener { view ->
                        quiz.afterTryAnswered.set(true)
                        onSingleAnswerClick(quiz, view, detailBinding)
                        focusedQuizList.add(quiz)
                    }

                    it.quizAnswerBtn.text = "정답 입력"
                    it.quizAnswerBtn.setOnClickListener(null)
                    it.quizAnswerBtn.setOnClickListener { v ->
                        viewModel.showNumkeyboard.postValue(true)
                        viewModel.selectedShortQuiz.postValue(quiz)
                        viewModel.selectedItemBinding = it
                        binding.numberKeyboard.listener = this@LCCookingFragment
                    }
                } else {
                    // 이미 푼 문제
                    it.quizSingleAnswer.setOnClickListener(null)
                    it.quizAnswerBtn.setOnClickListener(null)

                    it.quizAnswerBtn.text = quiz.userAnswer
                }
            }
        }
        private fun onSingleAnswerClick (quiz: CookingQuiz, sourceView: View, detailBinding: ItemCookingQuizDetailBinding) {
            if (quiz.isAnswerEntered.get()) {
                return
            }

            val selectionImages = quiz.sortedAnswerOptions.map { it.imageUrl }

            val quizSelectionList = selectionImages.mapIndexed { index, s ->
                CookingQuizSelection(s, index + 1, quiz, sourceView, detailBinding)
            }
            viewModel.selectionImageUrlList.postValue(quizSelectionList)
            viewModel.showSelection.postValue(true)

            binding.selectionImageRv.adapter = SelectionListAdapter()

        }

        override fun onItemClick(item: CookingInfoItem) {

        }
    }

    interface CookingItemClickListener {
        fun onItemClick(item: CookingInfoItem)
    }

    inner class SelectionListAdapter(): ListAdapter<CookingQuizSelection, RecyclerView.ViewHolder>(
        DiffCallback<CookingQuizSelection>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return SelectionViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_lc_cooking_selection, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SelectionViewHolder).bind(getItem(position))
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.rightViewBinding?.webView?.onPause()
    }

    inner class SelectionViewHolder(private val binding: ItemLcCookingSelectionBinding): RecyclerView.ViewHolder(binding.root), CookingSelectionItemClickListener {

        fun bind(item: CookingQuizSelection) {
            binding.apply {
                listener = this@SelectionViewHolder
                vm = viewModel
                this.item = item
                lifecycleOwner = viewLifecycleOwner

            }
        }

        override fun onItemClick(selectedContent: CookingQuizSelection) {

            val detailBinding = selectedContent.binding
            val userAnswer = "${selectedContent.seq}"
            val quiz = selectedContent.parentQuiz
            val selectedImageUrl = selectedContent.imageUrl
            viewModel.scoringCookingQuiz(quiz, userAnswer) { isCorrect ->

                detailBinding.apply {
                    quizSingleAnswer.text = ""
                    quizSingleAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_ffffff_stroke_grey_e8e8e8_round_5)
                    quizSingleAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_300))
                    quizSingleAnswer.isEnabled = false

                    quiz.isAnswerEntered.set(true)
                    val isCorrectAnswer = quiz.answer == userAnswer
                    quiz.isCorrectAnswer.set(isCorrectAnswer)
                    quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)

                    focusedQuizList.clear()
                }
            }

            viewModel.showSelection.postValue(false)
        }
    }
    interface CookingSelectionItemClickListener {
        fun onItemClick(content: CookingQuizSelection)
    }
    override fun onResume() {
        super.onResume()
        resetMemoView()
        resumePencilcaseView()
        recoveryQuizSingleAnswer()
        viewModel.rightViewBinding?.webView?.onResume()
    }

    private fun recoveryQuizSingleAnswer() {
        focusedQuizList.forEach {
            it.afterTryAnswered.set(false)
        }
        focusedQuizList.clear()
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
//        binding.rightRv.isBlocked = isBlocked

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

    override fun onEnterBtnClicked(button: Button, answer: String) {
        if (answer.isEmpty()) {
            DaebakToast.show(requireContext(), "값이 입력되지 않았습니다.")
            viewModel.showNumkeyboard.postValue(false)
        } else {
            val quiz = viewModel.selectedShortQuiz.value ?: return

            viewModel.scoringCookingQuiz(quiz, answer) { isCorrect ->
                viewModel.showNumkeyboard.postValue(false)
                viewModel.selectedItemBinding?.apply {

                    quizAnswerBtn.text = answer
                    quizAnswerBtn.isEnabled = false
                    quiz.isAnswerEntered.set(true)
                    val isCorrectAnswer = quiz.answer == answer
                    quiz.isCorrectAnswer.set(isCorrectAnswer)
                    binding.numberKeyboard.releaseKeyboard(null)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}

@BindingAdapter("bind_cooking_list")
fun bindCookingRecyclerView(recyclerView: RecyclerView, item: List<CookingInfoItem>?) {
    println("bind_cooking_list, size=${item?.size}")
    item?.let { itemList ->
        if (recyclerView.adapter == null) return
        val adapter = recyclerView.adapter as LCCookingFragment.CookingAdapter
        adapter.submitList(itemList)
    }
}
@BindingAdapter("bind_cooking_selection_image")
fun bindCookingSelectionImageRecyclerView(recyclerView: RecyclerView, item: List<CookingQuizSelection>?) {
    Log.d("bind_cooking_selection_image", "list=$item")
    item?.let { contentList ->
        if (recyclerView.adapter == null) { return }
        val adapter = recyclerView.adapter as LCCookingFragment.SelectionListAdapter
        adapter.submitList(contentList)
    }
}
