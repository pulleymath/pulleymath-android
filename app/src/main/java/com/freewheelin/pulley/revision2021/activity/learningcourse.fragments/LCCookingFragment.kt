package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.webkit.WebSettings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.*
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCCookingViewModel
import com.freewheelin.pulley.revision2021.views.*
import com.freewheelin.pulley.revision2023.utils.CookingChromeClient
import com.freewheelin.pulley.revision2023.utils.CookingWebClient
import com.freewheelin.pulley.revision2023.utils.listeners.CookingWebClientClickEventListener
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.legacy.views.memoView.MemoListener
import com.freewheelin.pulley.legacy.views.memoView.MemoView
import com.freewheelin.pulley.legacy.views.memoView.PathAndImageUndoCountListener
import com.freewheelin.pulley.legacy.views.memoView.PathRedoUndoCountChangeListener
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.PencilPanelListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import java.lang.ref.WeakReference

class LCCookingFragment() : Fragment(),
    PencilPanelListener,
    PlusMinusEnterKeypadListener,
//    PathAndImageUndoCountListener,
    PathRedoUndoCountChangeListener {
//    MemoListener
    companion object {
        fun newInstance(courseId: Int) : LCCookingFragment {
            return LCCookingFragment().apply {
                arguments = Bundle().apply {
                    putInt(LearningCourseActivity.COURSE_DETAIL_ID, courseId)
                }
            }
        }
    }

    private val cookingAdapter = CookingAdapter()
    val binding: FragmentLearningCourseCookingBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learning_course_cooking, null, false)
    }

    private val viewModel: LCCookingViewModel by viewModels()
    val screenWidth by lazy { DisplayUtils.getScreenWidth(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.getInt(LearningCourseActivity.COURSE_DETAIL_ID)?.let {
//            viewModel = ViewModelProvider(this).get(LCCookingViewModel::class.java)
            viewModel.initAdapterItem(it)
        }

        return binding.root
    }

    private fun initCustomKeyboardClParams(): FrameLayout.LayoutParams {
        return if (requireContext().isTablet) {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        } else {
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }.apply {
            gravity = Gravity.END or Gravity.CENTER
        }
    }
    private fun initNumberKeyboardParams(): LinearLayout.LayoutParams {
        return if (requireContext().isTablet) {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }.apply {
            marginEnd = 48.toPx()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            rightRv.adapter = cookingAdapter

            customKeyboardCl.layoutParams = initCustomKeyboardClParams()
            numberKeyboard.layoutParams = initNumberKeyboardParams()

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

            viewModel.apply {
                adapter = cookingAdapter
                cookingInfo.observe(viewLifecycleOwner) {
                    this@LCCookingFragment.viewModel.makeChatBotInfo(it)
                    val chapterId = it.chapterId
                    val cookingId = it.conceptCookingId
//                    memoView.memoCase = StudyMemoCase.CONCEPT_COOKING_LEARNING
                    memoView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    memoView.removePathRedoUndoCountChangeListener()
                    memoView.setPathRedoUndoCountChangeListener(this@LCCookingFragment)
//                    memoView.removePathAndImageUndoCountListener()
//                    memoView.setPathAndImageUndoCountListener(this@LCCookingFragment)
                    memoView.setCookingMemoId(chapterId, cookingId)
                    memoView.clearBitmap()
                    memoView.load()
                }
                cookingInfoItems.observe(viewLifecycleOwner) {
                    cookingAdapter.submitList(it)
                }
                cookingImageUrl.observe(viewLifecycleOwner) {
                    viewModel.setCookingImgIv(it, exerciseIv)
                }
                selectedExerciseIndex.observe(viewLifecycleOwner) {index ->
                    val selectedExercise =
                        cookingInfoItems.value?.filter { it.type == CookingInfoItem.ItemType.Exercise }
                            ?.first()
                            ?.exerciseList
                            ?.get(index)
                    currentCookingExercise.postValue(selectedExercise)

                }
            }
        }
    }

    fun setHintImageToCooking(imageUrl: String) {
        viewModel.cookingImageUrl.postValue(imageUrl)
    }

    inner class CookingAdapter(): ListAdapter<CookingInfoItem, RecyclerView.ViewHolder>(DiffCallback<CookingInfoItem>()) {
        private val activeHolders = mutableListOf<WeakReference<CookingItemHolder>>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return CookingItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_cooking_right_view, parent, false))
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CookingItemHolder).bind(getItem(position), position)
        }
        override fun getItemViewType(position: Int): Int {
            return position
        }
        fun releaseAllPlayers() {
            val iterator = activeHolders.iterator()
            while (iterator.hasNext()) {
                val ref = iterator.next()
                val holder = ref.get()
                if (holder != null) {
                    holder.releasePlayer() // 뷰홀더의 플레이어 해제
                } else {
                    iterator.remove() // 이미 메모리에서 사라진 뷰홀더는 리스트에서 제거
                }
            }
            // 리스트 초기화는 선택사항이지만, Fragment 재진입을 고려하면 clear하지 않거나
            // 어댑터가 새로 생성되므로 괜찮습니다. 안전하게 비워줍니다.
            activeHolders.clear()
        }
    }

    var prevExerciseMemoView: MemoView? = null
    inner class CookingItemHolder(private val itemBinding: ItemCookingRightViewBinding): RecyclerView.ViewHolder(itemBinding.root) {
        private var player: ExoPlayer? = null
        private var gestureDetector: GestureDetector? = null
        fun bind(item: CookingInfoItem, position: Int) {
            itemBinding.apply {
                this.item = item
                vm = viewModel
//                listener = this@CookingItemHolder
                lifecycleOwner = viewLifecycleOwner
                setAllContainerViewGone(this)
                rightRvItemRoot.setOnClickListener {
                    (activity as LearningCourseActivity).hidePenPanel()
                }

                when (item.type) {
                    CookingInfoItem.ItemType.Video -> {
                        videoContainerCl.visibility = View.VISIBLE
                        quizTabHeaderWrapperLl.visibility = View.VISIBLE
                        if (item.video !== null) {
                            val isVideoTypeHls = item.video?.url?.contains("m3u8") == true
                            if (isVideoTypeHls) {
                                expPlayerContainer.visibleIf(true)
                                initializePlayer(item)
                            } else { // youtube type
                                videoContainer.visibleIf(true)
                                webView.setOnTouchListener { view, motionEvent ->
                                    (activity as LearningCourseActivity).hidePenPanel()
                                    false
                                }
                                addVideo(item)
                            }
                        } else {
                            expPlayerContainer.visibleIf(false)
                            videoContainer.visibleIf(false)
                        }

                        viewModel.rightViewBinding = itemBinding
                        item.exerciseList?.let {
                            addExerciseBtn(it)
                        }
                        Glide.with(itemView).load(item.video?.thumbnailUrl).into(itemBinding.thumbnailView)

                    }
                    CookingInfoItem.ItemType.Exercise -> {
                        exerciseContainer.visibility = View.VISIBLE

                        viewModel.quizMemoViewList.clear()

                        viewModel.apply {
                            selectedExerciseIndex.observe(viewLifecycleOwner) { selectedIndex ->
                                item.cookingInfo?.let {
                                    val chapterId = it.chapterId
                                    val cookingId = it.conceptCookingId
                                    quizMemoViewList.clear()

//                                    cookingQuizzes.memoView.saveImaged()
                                    cookingQuizzes.memoView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                    cookingQuizzes.memoView.removePathRedoUndoCountChangeListener()
                                    cookingQuizzes.memoView.setPathRedoUndoCountChangeListener(this@LCCookingFragment)
//                                    cookingQuizzes.memoView.removePathAndImageUndoCountListener()
//                                    cookingQuizzes.memoView.setPathAndImageUndoCountListener(this@LCCookingFragment)
                                    cookingQuizzes.memoView.setMemoSavedName(chapterId,cookingId,"cooking_quiz_${selectedIndex}")
                                    cookingQuizzes.memoView.clearBitmap()
                                    cookingQuizzes.memoView.load()
                                    prevExerciseMemoView = cookingQuizzes.memoView
                                    quizMemoViewList.add(cookingQuizzes.memoView)

                                    val lcActivity = (activity as LearningCourseActivity)
                                    quizMemoViewList.forEach { it.set(lcActivity.binding.penPanel) }
                                }

                                cookingInfoItems.value?.filter { it.type == CookingInfoItem.ItemType.Exercise }
                                    ?.first()
                                    ?.exerciseList
                                    ?.get(selectedIndex)
                                    ?.let { selectedExercise ->
                                        val quizBinding = cookingQuizzes
                                        setOnQuizView(quizBinding, selectedExercise)
                                    }
                            }
                            fingerDrawModeMode.observe(viewLifecycleOwner) {
                                cookingQuizzes.memoView.fingerDrawMode = it
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
            itemBinding.quizTabHeader.removeAllViews()
            tabTitles.forEachIndexed { index, title ->
                val exerciseBtn = CookingExerciseHeaderBtn(requireContext()).apply {
                    initUI(index, tabTitles.size)
                }

                exerciseBtn.setOnClickListener {
                    prevExerciseMemoView?.saveImaged()
                    (activity as LearningCourseActivity).hidePenPanel()

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
                        setImageResource(R.drawable.ic_blunt_triangle_5_6_right_purple_200)
                    }
                    itemBinding.quizTabHeader.addView(arrowView)
                }
            }
        }

        private fun setAllContainerViewGone(view: ItemCookingRightViewBinding) {
            view.apply {
                videoContainerCl.visibility = View.GONE
                quizTabHeaderWrapperLl.visibility = View.GONE
                exerciseContainer.visibility = View.GONE
                footerContainer.visibility = View.GONE
            }
        }
        fun addVideo(item: CookingInfoItem) {
            itemBinding.apply {
                loadingContainer.visibleIf(true)
                videoLoadingLottie.playAnimation()

                val marginHorizontal = 64.toPx()
                val vWidth = (((screenWidth * 0.55) - marginHorizontal) / 16 * 9).toInt()

                val lp = webView.layoutParams
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp.height = vWidth
                webView.layoutParams = lp
                webView.setOnDragListener { view, motionEvent ->
                    (activity as? LearningCourseActivity)?.binding?.chatBotBtn?.addDragListenerFromYoutubeWebView(motionEvent)
                    true
                }
                webView.webViewClient = CookingWebClient(
                    urlLoadingCallback = { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        (activity as LearningCourseActivity).hidePenPanel()
                     },
                    pageFinishedCallback = {
                        itemBinding.loadingContainer.hide(300)
                    })
                webView.webChromeClient = CookingChromeClient(requireActivity())
                webView.addJavascriptInterface(CookingWebClientClickEventListener(listener = {
                    val cookingId = viewModel.cookingInfo.value?.conceptCookingId ?: -999
                    LogUtils.logEvent(
                        requireContext(),
                        user,
                        PulleyEvent.BUTTON_CLICK,
                        "풀리개념학습",
                        "유튜브",
                        "conceptCookingId_$cookingId"
                    )
                }), "androidInterface")
                webView.settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    domStorageEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                val videoUrl = makeYoutubeUrl(item)
                val htmlData = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; }
                            iframe { width: 100%; height: 100%; }
                        </style>
                    </head>
                    <body>
                        <iframe  src="${videoUrl}" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(
                    "https://pulleymath.com",
                    htmlData,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
        private fun makeYoutubeUrl(item: CookingInfoItem): String {
            val videoUUID = item.video!!.uuid
            val startTimeQuery = if(item.video!!.startTime == null) "" else "&start=${item.video!!.startTime}"
            val endTimeQuery = if(item.video!!.endTime == null) "" else "&end=${item.video!!.endTime}"
            val hideMoreVideos = "&rel=0"
            return "https://www.youtube.com/embed/${videoUUID}?${startTimeQuery}${endTimeQuery}${hideMoreVideos}"
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
                        quiz.hintImageUrl?.let { hintImage -> setHintImageToCooking(hintImage) }
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
                        viewModel.focusedQuizList.add(quiz)
                    }

                    it.quizAnswerBtn.text = "정답 입력"
                    it.quizAnswerBtn.setOnClickListener(null)
                    it.quizAnswerBtn.setOnClickListener { _ ->
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
            if (quiz.isAnswerEntered.get()) { return }

            val selectionImages = quiz.sortedAnswerOptions.map { it.imageUrl }

            val quizSelectionList = selectionImages.mapIndexed { index, s ->
                CookingQuizSelection(s, index + 1, quiz, sourceView, detailBinding)
            }
            viewModel.selectionImageUrlList.postValue(quizSelectionList)
            viewModel.showSelection.postValue(true)

            binding.selectionImageRv.adapter = SelectionListAdapter()

        }

        private fun initializePlayer(item: CookingInfoItem) {
            // 기존 플레이어가 있다면 해제
            releasePlayer()

            itemBinding.expPlayerContainer.apply {
                val marginHorizontal = 64.toPx() // 웹뷰 로직 참고 (필요시 조정)
                // 만약 fragment의 screenWidth 접근이 어렵다면 resources.displayMetrics.widthPixels 등을 사용
                val vWidth = (((screenWidth * 0.55) - marginHorizontal) / 16 * 9).toInt()

                val lp = layoutParams
                lp.height = vWidth
                layoutParams = lp
            }

            val context = itemBinding.root.context
            itemBinding.loadingContainer2.visibleIf(true)

            // ExoPlayer 생성
            player = ExoPlayer.Builder(context).build()
            itemBinding.playerView.player = player
            itemBinding.playerView.controllerShowTimeoutMs = 5000
            // HLS 소스 설정 (URL이 .m3u8이라고 가정)
            val videoUrl = item.video?.url ?: return // 실제 모델 필드에 맞게 수정 필요

            // 만약 .m3u8이라면 HlsMediaSource 사용, 아니라면 일반 MediaItem
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaItem = MediaItem.fromUri(videoUrl)
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

            player?.setMediaSource(mediaSource)
            player?.prepare()

            // 리스너 설정
            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        itemBinding.loadingContainer2.visibility = View.GONE
                        // 재생이 시작되면 썸네일 숨김
                        if (player?.playWhenReady == true) {
                            itemBinding.thumbnailOverlay.visibility = View.GONE
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        itemBinding.thumbnailOverlay.visibility = View.GONE
                    }
                }
            })

            // 커스텀 UI 이벤트 연결
            setupCustomControls(context)
            setupGestures(context)
        }

        private fun setupCustomControls(context: android.content.Context) {
            val btnSpeed = itemBinding.playerView.findViewById<TextView>(R.id.btn_speed)
            btnSpeed?.setOnClickListener {
                showSpeedSelectionDialog(context, btnSpeed)
            }

            val btnFullscreen = itemBinding.playerView.findViewById<View>(R.id.btn_fullscreen)
            btnFullscreen?.setOnClickListener {
                // 전체화면 로직 (Activity 레벨에서 처리 필요하거나 다이얼로그로 띄우기)
                // 간단하게는 가로/세로 모드 전환
            }

            // [추가] 3. 볼륨 컨트롤 로직
            setupVolumeControl()
        }

        private fun showSpeedSelectionDialog(context: android.content.Context, btnSpeed: TextView) {
            val speeds = arrayOf("0.25x", "0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "2.0x")
            val speedValues = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

            AlertDialog.Builder(context)
                .setTitle("재생 속도")
                .setItems(speeds) { _, which ->
                    val speed = speedValues[which]
                    player?.setPlaybackSpeed(speed)
                    btnSpeed.text = speeds[which]
                }
                .show()
        }

        private fun setupGestures(context: android.content.Context) {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val viewWidth = itemBinding.playerView.width
                    val isLeft = e.x < viewWidth / 3
                    val isRight = e.x > (viewWidth * 2) / 3

                    if (isLeft) {
                        rewind()
                        showDoubleTapFeedback(isLeft = true)
                        return true
                    } else if (isRight) {
                        forward()
                        showDoubleTapFeedback(isLeft = false)
                        return true
                    }
                    return false
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // 탭하면 컨트롤 보이기/숨기기 토글
                    if (itemBinding.playerView.isControllerVisible) {
                        itemBinding.playerView.hideController()
                    } else {
                        itemBinding.playerView.showController()
                    }
                    return true
                }
            })

            itemBinding.playerView.setOnTouchListener { _, event ->
                gestureDetector?.onTouchEvent(event)
                // [추가 2] 반드시 true를 반환하여 이벤트가 PlayerView 내부 로직으로 전파되는 것을 막아야 합니다.
                true
            }
        }

        private fun rewind() {
            val current = player?.currentPosition ?: 0
            player?.seekTo((current - 10000).coerceAtLeast(0))
        }

        private fun forward() {
            val current = player?.currentPosition ?: 0
            val duration = player?.duration ?: 0
            player?.seekTo((current + 10000).coerceAtMost(duration))
        }

        private fun showDoubleTapFeedback(isLeft: Boolean) {
            val overlay = itemBinding.doubleTapOverlay
            val leftView = itemBinding.doubleTapLeft
            val rightView = itemBinding.doubleTapRight

            overlay.visibility = View.VISIBLE
            leftView.visibility = if (isLeft) View.VISIBLE else View.GONE
            rightView.visibility = if (!isLeft) View.VISIBLE else View.GONE

            // 깜빡이는 애니메이션
            val anim = AlphaAnimation(1.0f, 0.0f).apply {
                duration = 800
                fillAfter = true
            }
            if (isLeft) leftView.startAnimation(anim) else rightView.startAnimation(anim)

            // 0.8초 후 숨김
            itemBinding.root.postDelayed({
                overlay.visibility = View.GONE
            }, 800)
        }

        // RecyclerView에서 뷰가 재활용되거나 프래그먼트가 파괴될 때 호출되어야 함
        fun releasePlayer() {
            player?.release()
            player = null
        }
        private var lastVolume: Float = 1.0f


        private fun setupVolumeControl() {
            val btnVolume = itemBinding.playerView.findViewById<ImageButton>(R.id.btn_volume)
            val volumeSeekBar = itemBinding.playerView.findViewById<SeekBar>(R.id.volume_seekbar) ?: return

            // 초기 상태 설정
            val currentVolume = player?.volume ?: 1.0f
            volumeSeekBar.progress = (currentVolume * 100).toInt()
            updateVolumeIcon(btnVolume, currentVolume > 0)
            lastVolume = if (currentVolume == 0f) 1.0f else currentVolume

            // 3-1. SeekBar 리스너 (볼륨 조절)
            volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val newVolume = progress / 100f
                        player?.volume = newVolume
                        updateVolumeIcon(btnVolume, newVolume > 0)

                        if (newVolume > 0) lastVolume = newVolume
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // 3-2. 아이콘 클릭 리스너 (음소거 토글)
            btnVolume?.setOnClickListener {
                val isMuted = (player?.volume ?: 0f) == 0f

                if (isMuted) {
                    // 음소거 해제 (이전 볼륨으로 복구)
                    player?.volume = lastVolume
                    volumeSeekBar.progress = (lastVolume * 100).toInt()
                    updateVolumeIcon(btnVolume, true)
                } else {
                    // 음소거 설정
                    lastVolume = player?.volume ?: 1.0f // 현재 볼륨 저장
                    player?.volume = 0f
                    volumeSeekBar.progress = 0
                    updateVolumeIcon(btnVolume, false)
                }
            }
        }

        private fun updateVolumeIcon(btnVolume: ImageButton?, isSoundOn: Boolean) {
            btnVolume?.setImageResource(
                if (isSoundOn) R.drawable.icon_volume_high // 소리 켜짐 아이콘
                else R.drawable.icon_volume_mute       // 소리 꺼짐(Mute) 아이콘
            )
        }
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
                    quizSingleAnswer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_stroke_gray_300_round_5)
                    quizSingleAnswer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_300))
                    quizSingleAnswer.isEnabled = false

                    quiz.isAnswerEntered.set(true)
                    val isCorrectAnswer = quiz.answer == userAnswer
                    quiz.isCorrectAnswer.set(isCorrectAnswer)
                    quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)

                    viewModel.focusedQuizList.clear()
                }
            }
            viewModel.sendExerciseScoringLog {
                //TODO 챌린지관련
                println("asoaso 챌린지 - 개념학습 예제 풀기")
            }
            viewModel.showSelection.postValue(false)

        }
    }
    interface CookingSelectionItemClickListener {
        fun onItemClick(content: CookingQuizSelection)
    }
    override fun onResume() {
        super.onResume()
//        println("aspasp cookingfragment onresume")
        viewModel.chatbotInfo?.let {
            (activity as LearningCourseActivity).run {
//                println("aspasp chatbot Info inIT from onresume")
                viewModel.chatBotInfo = it
            }
        }
        resetMemoView()
        resumePencilCaseView()
        recoveryQuizSingleAnswer()
        viewModel.rightViewBinding?.webView?.onResume()
        setRedOnAllClearBtnOfPenPanel()
        (activity as LearningCourseActivity).run {
            mainPanelTopMarginByCourseType()
            showMainPanPanelIfPenSelected()
        }
    }
    private fun setRedOnAllClearBtnOfPenPanel() {
        (activity as? LearningCourseActivity)?.setUndoCount(1)
    }

    private fun recoveryQuizSingleAnswer() {
        viewModel.focusedQuizList.forEach {
            it.afterTryAnswered.set(false)
        }
        viewModel.focusedQuizList.clear()
    }
    private fun resetMemoView() {
        val lcActivity = (activity as LearningCourseActivity)
        lcActivity.binding.penPanel.memoViews.clear()
    }

    private fun resumePencilCaseView() {
        val lcActivity = (activity as LearningCourseActivity)
        lcActivity.binding.penPanel.listener = this@LCCookingFragment
        lcActivity.binding.penPanel.isCookingMemo = true
        binding.memoView.set(lcActivity.binding.penPanel)
//        binding.memoView.memoListener = this@LCCookingFragment

        viewModel.quizMemoViewList.forEach { it.set(lcActivity.binding.penPanel) }

        val pencilType = lcActivity.getPencilcaseType()
        val color = lcActivity.getPencilcaseColor()
        val thickn = lcActivity.getPencilcaseThickness()
        val fingerDrawMode = lcActivity.getFingerDrawMode()

        lcActivity.binding.penPanel.apply {

            if (drawType != null) {
                resetMode()
                fingerDrawModeSwitch.isChecked = fingerDrawMode

                val isBlocked = pencilType != null
                binding.leftScrollView.isBlock = isBlocked
                binding.leftScrollView.fingerDrawMode = fingerDrawMode
                binding.memoView.fingerDrawMode = fingerDrawMode
                viewModel.fingerDrawModeMode.postValue(fingerDrawMode)

            }
            lcActivity.hidePenPanel()
        }
    }


    override fun onDrawTypeChanged(type: DrawType?) {
        val isBlocked = type != null
        binding.leftScrollView.isBlock = isBlocked
//        binding.rightRv.isBlocked = isBlocked

//        (parentFragment as LCPatternFragment).setPagerSwipeBlocked(isBlocked)
        (activity as LearningCourseActivity).savePencilcaseType(type)
    }

//    override fun onEditColorChanged(color: CookingPencilcase.PenColor) {
//        (activity as LearningCourseActivity).savePencilcaseColor(color)
//    }

//    override fun onThicknessSelected(thickness: Float) {
//        (activity as LearningCourseActivity).savePencilcaseThicknesss(thickness)
//    }

//    override fun onModeChanged(isFixedMode: Boolean) {
//        (activity as LearningCourseActivity).savePencilcaseMode(isFixedMode)
//    }

    override fun onFingerDrawModeChanged(value: Boolean) {
        (activity as LearningCourseActivity).saveFingerDrawMode(value)
        binding.leftScrollView.fingerDrawMode = value
        binding.memoView.fingerDrawMode = value
        viewModel.fingerDrawModeMode.postValue(value)
    }

    override fun onEnterBtnClicked(button: Button, answer: String) {
        if (answer.isEmpty()) {
            DaebakToast.show(requireContext(), "값이 입력되지 않았습니다.")
            viewModel.showNumkeyboard.postValue(false)
        } else {
            val quiz = viewModel.selectedShortQuiz.value ?: return

            viewModel.scoringCookingQuiz(quiz, answer) { isCorrect ->
                viewModel.showNumkeyboard.postValue(false)
                viewModel.sendExerciseScoringLog {
                    println("asoaso 챌린지 - 개념학습 쿠킹 예제 풀기 ")
                }
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
        binding.memoView.memoListener = null
        viewModel.run {
            clearCompositeDisposable()
        }

        // [추가됨] Fragment가 멈출 때(닫힐 때 포함) 플레이어 리소스 해제
        println("aspasp lccf onstop")
        cookingAdapter.releaseAllPlayers()
    }

    override fun onUndoCountChanged(undoCount: Int) {
        (activity as? LearningCourseActivity)?.setUndoCount(undoCount)
    }

    override fun onRedoCountChanged(redoCount: Int) {
        (activity as? LearningCourseActivity)?.setRedoCount(redoCount)
    }

//    override fun onImageAndPathUndoCountChanged(undoCount: Int) {
//        (activity as? LearningCourseActivity)?.setMemoImageAndPathUndoCountChanged(undoCount)
//    }

//    override fun onDrawAStroke(memoCase: StudyMemoCase) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRemoveAllMemo() {
//        TODO("Not yet implemented")
//    }
}