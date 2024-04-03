package com.freewheelin.pulley.revision2021.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2023.ui.fragment.AssessmentFragment
import com.freewheelin.pulley.legacy.activities.solve.*
import com.freewheelin.pulley.legacy.bases.DensityLevel.*
import com.freewheelin.pulley.legacy.bases.densityLevel
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.ActivityAssessmentTestSolveBinding
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.model.ProblemType
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.revision2021.activity.fragments.AssessmentSolveConceptFragment
import com.freewheelin.pulley.revision2021.activity.fragments.AssessmentSolveSolutionFragment
import com.freewheelin.pulley.revision2021.model.response.AssessmentCard
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.model.response.AssessmentWorkbook
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveConceptViewModel
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveSolutionViewModel
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveViewModel
import com.freewheelin.pulley.revision2021.views.AssessmentGalleryViewDelegate
import com.freewheelin.pulley.revision2021.views.AssessmentGalleryView
import com.freewheelin.pulley.legacy.utils.*
import com.freewheelin.pulley.legacy.views.*
import com.freewheelin.pulley.legacy.views.memoView.PathRedoUndoCountChangeListener
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.PencilPanelListener
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class AssessmentSolveActivity : AppCompatActivity(),
    AnswerV2Delegate,
    ProblemGestureListener,
    AssessmentGalleryViewDelegate,
    PencilPanelListener,
    PathRedoUndoCountChangeListener {

    private val binding: ActivityAssessmentTestSolveBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_assessment_test_solve,
            null,
            false
        )
    }
    private val viewModel: AssessmentSolveViewModel by viewModels()
    private val conceptViewModel = AssessmentSolveConceptViewModel.instance
    private val solutionViewModel = AssessmentSolveSolutionViewModel.instance

    val screenWidth by lazy { DisplayUtils.getScreenWidth(this) }
    val screenHeight by lazy { DisplayUtils.getScreenHeight(this) }
    var problemGesture: ProblemGestures? = null
//    var solutionGesture: SolveGestures? = null

    var itemValue = ""
        set(value) {
            field = value
        }

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA) }
    lateinit var finishReceiver: BroadcastReceiver

    companion object {
        val IS_REVIEW = "IS_REVIEW"
        val SELECTED_WORKBOOK = "SELECTED_WORKBOOK"
        val BROADCAST_MSG = "AFF_TEST_SOLVE_ACTIVITY_BROAD"

        fun getIntent(context: Context): Intent {
            val intent = Intent(context, AssessmentSolveActivity::class.java)
            return intent
        }

        fun getIntent(context: Context, card: AssessmentCard): Intent {
            val intent = Intent(context, AssessmentSolveActivity::class.java)
            intent.putExtra(SELECTED_WORKBOOK, card.selectedWorkbook)
            return intent
        }

        fun getIntent(context: Context, workbook: AssessmentWorkbook?): Intent {
            val intent = Intent(context, AssessmentSolveActivity::class.java)
            intent.putExtra(SELECTED_WORKBOOK, workbook)
            return intent
        }

        fun getReviewIntent(context: Context, workbook: AssessmentWorkbook?): Intent {
            val intent = getIntent(context)
            intent.putExtra(IS_REVIEW, true)
            intent.putExtra(SELECTED_WORKBOOK, workbook)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getExtra()
        initUI()
        observeLiveData()
        fetchProblem()
        registerReceiver()
    }

    private fun getExtra() {
        with(viewModel) {
            selectedWorkbook = intent.getSerializableExtra(SELECTED_WORKBOOK) as? AssessmentWorkbook

            isReview.value = intent.getBooleanExtra(IS_REVIEW, false)

            selectedWorkbook?.let {
                workbookId = it.id
                version = it.version
                testStartedAt = it.test_started_at
                testFinishedAt = it.test_finished_at
                isFixedStartTime.value = it.is_fixed_time
                showTimer = it.showTimer
                testPeriodMinutes = it.test_period_minutes
                workbookSeq = it.seq
            }
        }
    }

    fun registerReceiver() {
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                DialogUtils.v2FinishTestDialog(this@AssessmentSolveActivity) {
                    val reConfigureReceiverIntent = Intent(UserManager.RE_CONFIGURE_UI)
                    LocalBroadcastManager.getInstance(this@AssessmentSolveActivity).sendBroadcast(reConfigureReceiverIntent)

                    val intent = Intent(getActivity(), MainActivity::class.java)
                    intent.putExtra(SELECTED_WORKBOOK, viewModel.selectedWorkbook)

                    setResult(AssessmentFragment.SHOW_REPORT_INT, intent)
                    if (!isFinishing) finish()
                }
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(finishReceiver, IntentFilter(BROADCAST_MSG))

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishReceiver)
        super.onDestroy()
    }

    fun observeLiveData() {
        viewModel.apply {
            if (viewModel.isReview.value == true) return
            problemIndex.observe(this@AssessmentSolveActivity) {
                val problemNo = it + 1
                openProblem(problemNo)
            }
        }
    }

    private fun fetchProblem() {
        viewModel.let {
            if (viewModel.isReview.value == true) {
                it.getTestResult { that ->
                    this@AssessmentSolveActivity.runOnUiThread {
                        onProblemSelected(that, false)
                    }
                }
            } else {
                it.getTestProblems(it.workbookId, it.version) { that ->
                    this@AssessmentSolveActivity.runOnUiThread {
                        onProblemSelected(that, false)
//                    setRemainingTimer()
                    }
                }
            }
        }
    }

    var tabFragments: MutableList<Fragment> = mutableListOf(
        AssessmentSolveConceptFragment.newInstance(),
        AssessmentSolveSolutionFragment.newInstance()
    )
//    var tabList: MutableList<TabLayout.Tab> = mutableListOf()
    private var tabTitles = arrayOf("해설", "개념학습")

    fun initUI() {
        binding.apply {
            lifecycleOwner = this@AssessmentSolveActivity
            vm = viewModel

            initGallery(viewModel.selectedWorkbook)
            setGalleryBtn()

            pager.adapter = ViewPagerAdapter(tabFragments, supportFragmentManager, lifecycle)
            pager.isUserInputEnabled = false
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text = tabTitles[position]
//                tabList.add(tab)
            }.attach()

            backBtn.setOnClickListener {
                onBackPressed()
            }
            baseCl.layoutParams.width = screenWidth
//            solveCl.layoutParams.width = screenWidth
//            solutionContainer.visibility = View.GONE
            prevBtn.setOnClickListener { onPrevBtnClicked() }
            nextBtn.setOnClickListener { onNextBtnClicked() }
            externalPenBtn.setOnClickListener {
                penPanel.visibleIf(!penPanel.isVisible)
                if (penPanel.isVisible) {
                    penPanel.openPencilPanel()
                    externalPenBtn.setImageResource(R.drawable.ic_pencil_fliled_purple)
                } else {
                    penPanel.closePencilPanel()
                    externalPenBtn.setImageResource(R.drawable.ic_pencil)
                }
            }
            galleryCloser.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onFoldBtnClicked()
                }
                true
            }

            penPanel.listener = this@AssessmentSolveActivity
            problemMemoView.set(penPanel)
            problemMemoView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            problemMemoView.removePathRedoUndoCountChangeListener()
            problemMemoView.setPathRedoUndoCountChangeListener(this@AssessmentSolveActivity)

//            solutionMemoView.set(pencilcaseView)

            val imageWidth = when (this@AssessmentSolveActivity.densityLevel) {
                Low -> screenWidth / 2
                High -> (screenWidth / 2.7).toInt()
                else -> (500.toPx()).toInt()
            }

            problemIv.maxWidth = imageWidth
//            solutionIv.maxWidth = imageWidth
            problemMemoView.layoutParams.width = screenWidth
//            solutionMemoView.layoutParams.width = screenWidth

            answerView.delegate = this@AssessmentSolveActivity

            dimDialogBtn.setOnClickListener {
                this@AssessmentSolveActivity.finish()
            }
            submitBtn.setOnClickListener {
                if (viewModel.isSubmitBtnActive.value == true) {
                    viewModel.onSubmit(this@AssessmentSolveActivity) {
                        saveMemo()
                        viewModel.finishTest {
                            DialogUtils.v2FinishTestDialog(this@AssessmentSolveActivity) {
                                val reConfigureReceiverIntent = Intent(UserManager.RE_CONFIGURE_UI)
                                LocalBroadcastManager.getInstance(this@AssessmentSolveActivity).sendBroadcast(reConfigureReceiverIntent)

                                val intent = Intent(getActivity(), MainActivity::class.java)
                                intent.putExtra(SELECTED_WORKBOOK, viewModel.selectedWorkbook)

                                setResult(AssessmentFragment.SHOW_REPORT_INT, intent)
                                if (!isFinishing) finish()
                            }
                        }
                    }
                }
            }
            baseCl.setOnDragListener { view, dragEvent ->
                when (dragEvent.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {

                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
//                        val x = dragEvent.x
//                        val y = dragEvent.y
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        var x = dragEvent.x
                        var y = dragEvent.y
                        val answerHeight = answerView.height

                        if (y > screenHeight) {
                            val answerWidth = answerView.width

                            x =
                                dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                            y = dragEvent.y - answerHeight / 2f

                            // 화면 밖으로 나가면 안으로 넣기
                            if (x > screenWidth - answerWidth) {
                                x = (screenWidth - answerWidth).toFloat()
                            } else if (x < (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))) {
                                x = (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                            }

                            if (y > screenHeight - answerHeight) {
                                y = (screenHeight - answerHeight).toFloat()
                            } else if (y < answerHeight) {
                                y = (answerHeight).toFloat()
                            }
                            answerView.setPosition(x, y)
                            answerView.visibility = View.VISIBLE
                        } else if (y != 0f && y < answerHeight) {
                            if (y < answerHeight) {
                                y = 0f
                                answerView.setPosition(x, y)
                                answerView.visibility = View.VISIBLE
                            }

                        }
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        answerView.cancelDragAndDrop()
//                    }
                    }
                    DragEvent.ACTION_DROP -> {
                        LogUtils.logEvent(
                            this@AssessmentSolveActivity,
                            user,
                            PulleyEvent.BUTTON_CLICK,
                            "바로풀기화면",
                            "플로팅OMR 드래그",
                            itemValue
                        )
                        answerView.visibility = View.VISIBLE

                        val answerHeight = answerView.height
                        val answerWidth = answerView.width

                        var x =
                            dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                        var y = dragEvent.y - answerHeight / 2f
                        // 화면 밖으로 나가면 안으로 넣기
                        if (x > screenWidth - answerWidth) {
                            x = (screenWidth - answerWidth).toFloat()
                        } else if (x < (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))) {
                            x = 0f
                        }

                        if (y > screenHeight - answerHeight) {
                            y = (screenHeight - answerHeight * 2).toFloat()
                        } else if (y < answerHeight) {
                            // 아무것도 안해도 괜찮음
//                            y = (answerHeight).toFloat()
                        }

                        answerView.setPosition(x, y)
                    }
                }
                true
            }

            problemGesture =
                ProblemGestures(this@AssessmentSolveActivity, problemIv, problemMemoView)
            problemGesture?.listener = this@AssessmentSolveActivity
            problemContainer.setOnTouchListener(problemGesture)
            initPosition()
        }
    }

    fun initPosition() {
        binding.apply {
            rootView.postDelayed({
                rootView.scrollX = AssessmentGalleryView.getGalleryViewWidth(this@AssessmentSolveActivity)
                answerView.setInitPosition()
            }, 0)
        }
    }
    fun setGalleryBtn() {

        var rotateAnim: ObjectAnimator? = null
        if (Preferences.univGalleryClickCnt.get() > 0) {
            binding.galleryBtn.setOnClickListener {
                onGalleryBtnClicked(rotateAnim)
            }
            return
        } else {
            rotateAnim = ObjectAnimator.ofFloat(binding.galleryBtn, "rotation", 0f, 5f, 0f, -5f, 0f)
            rotateAnim.repeatCount = 20
            rotateAnim.duration = 200
            rotateAnim.start()
            binding.galleryBtn.playAnimation()

            rotateAnim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        rotateAnim.start()
                    }, 500)
                }

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationStart(p0: Animator) {}
            })
            binding.galleryBtn.setOnClickListener {
                onGalleryBtnClicked(rotateAnim)
            }
        }
    }

    fun initGallery(selectedWorkbook: AssessmentWorkbook?) {
        binding.apply {
            galleryView.layoutParams.width = AssessmentGalleryView.getGalleryViewWidth(this@AssessmentSolveActivity)
            selectedWorkbook?.let {
                galleryView.setContent(it)
            }
            galleryView.delegate = this@AssessmentSolveActivity

        }
    }

    fun onGalleryBtnClicked(rotateAnim: ObjectAnimator?) {
        Preferences.univGalleryClickCnt.set(Preferences.galleryClickCnt.get() + 1)
        rotateAnim?.apply {
            cancel()
            repeatCount = 0
            removeAllListeners()
            removeAllUpdateListeners()
            binding.galleryBtn.cancelAnimation()
            binding.galleryBtn.frame = 0
        }

        binding.apply {
            answerView.clearFocusOnShortAnswer()
            val problem = viewModel.currentProblem.value
            if(problem != null) binding.galleryView.scrollTo(problem)

            if (viewModel.showGalleryView.value == true) {
                return@apply
            }
            val galleryParam = (galleryView.layoutParams as LinearLayout.LayoutParams)

            val animator = ValueAnimator.ofInt(galleryParam.width, 0)
            animator.addUpdateListener {
                val value = it.animatedValue as Int
                rootView.scrollTo(value, 0)
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    viewModel.showGalleryView.postValue(true)
                }
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationStart(p0: Animator) {}
            })

            animator.duration = 150
            animator.start()


        }
    }

    override fun onProblemSelected(problem: AssessmentProblem?, autoFocus: Boolean) {

        viewModel.currentProblem.postValue(problem)
        val problemIndex = viewModel.problemList.value?.indexOf(problem) ?: 0
        viewModel.problemIndex.postValue(problemIndex)

//        saveMemo()
        onSetProblem(problem)


        solutionViewModel.fetchMedia(problem?.id) { showEmptyView ->
            binding.apply {
//                tabLayout.getTabAt(1)?.view?.isClickable = isSolutionEnable
                solutionViewModel.showEmptyView.postValue(showEmptyView)
            }
        }

        if (problem != null) {
            if (viewModel.currentProblem.value?.getResultByScoring() == Result.yet && viewModel.currentProblem.value?.getProblemType() == ProblemType.short) {
                // 문제 안풀었고, 단답이고, 정답보기가off 이고, 빠른채점도 off이면 포커스
            } else {
                binding.answerView.clearFocusOnShortAnswer()
            }
        }

        // 리뷰중이고 틀린문제면 해설 표시
        viewModel.apply {
            val isWrongAnswer = problem?.is_correct == false
            isEnableSolutionSwitch.postValue(isWrongAnswer)
        }
    }

    var lastFiveMinTimer: CountDownTimer? = null
    var remainingTimer: CountDownTimer? = null
    var dimScreenTimer: CountDownTimer? = null
    override fun onResume() {
        super.onResume()

        viewModel.getServerTime { serverTimeNow ->
            Handler(Looper.getMainLooper()).postDelayed({
                setScreenDimComeInBeforeTestStart(serverTimeNow)
            }, 800)
            if (viewModel.isReview.value == true) return@getServerTime

            Handler(Looper.getMainLooper()).postDelayed({
                setRemainingTimer(serverTimeNow)
                set5MinTimer(serverTimeNow)
            }, 800)
        }

    }

    private fun set5MinTimer(serverTimeNow: String) {
        val before5MinItEnds = viewModel.get5MinBeforeFinishedTimeEnds() ?: return
        val paredDate = sdf.parse(before5MinItEnds) ?: return
        val parsedCurrentServerDate = sdf.parse(serverTimeNow) ?: return
        val timeDiffMilli = paredDate.time - parsedCurrentServerDate.time

        lastFiveMinTimer = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {}

            override fun onFinish() {
                binding.remainingTime.setTextColor(
                    ContextCompat.getColor(
                        this@AssessmentSolveActivity,
                        R.color.red_300
                    )
                )
            }
        }.start()
    }

    private fun setScreenDimComeInBeforeTestStart(serverTimeNow: String) {
        val startedAt = viewModel.getStartedTime() ?: return
        val paredDate = sdf.parse(startedAt) ?: return
        val parsedCurrentServerDate = sdf.parse(serverTimeNow) ?: return
        val timeDiffMilli = paredDate.time - parsedCurrentServerDate.time

        dimScreenTimer = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {

                val hour = diff / 1000 / 3600
                val min = (diff / 1000 / 60) - (hour * 60)
                val sec = (diff / 1000) - (min * 60) - (hour * 3600)

                val minStr = if (min.toString().length < 2) "0$min" else min.toString()
                val secStr = if (sec.toString().length < 2) "0$sec" else sec.toString()
                viewModel.minInDimDialog.postValue(minStr)
                viewModel.secInDimDialog.postValue(secStr)

                viewModel.showDimView.postValue(true)
                viewModel.showDimBgView.postValue(true)
            }

            override fun onFinish() {
                viewModel.showDimBgView.postValue(false)
                viewModel.showDimView.postValue(false)
            }
        }.start()

    }

    private fun setRemainingTimer(serverTimeNow: String) {
        val finishedAt = viewModel.getFinishedTime() ?: return
//        println("aspasp finishedAt : ${finishedAt}")
        val paredDate = sdf.parse(finishedAt) ?: return

        val parsedCurrentServerDate = sdf.parse(serverTimeNow) ?: return
        val timeDiffMilli = paredDate.time - parsedCurrentServerDate.time
        remainingTimer = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {
                val hour = diff / 1000 / 3600
                val min = (diff / 1000 / 60) - (hour * 60)
                val sec = (diff / 1000) - (min * 60) - (hour * 3600)

                val hourStr = if (hour.toString().length < 2) "0$hour" else hour.toString()
                val minStr = if (min.toString().length < 2) "0$min" else min.toString()
                val secStr = if (sec.toString().length < 2) "0$sec" else sec.toString()
                println("aspasp remain ${hourStr}:${minStr}:${secStr}")
                binding.remainingTime.text = "${hourStr}:${minStr}:${secStr}"
            }

            override fun onFinish() {
                viewModel.finishTest {
                    LocalBroadcastManager.getInstance(baseContext)
                        .sendBroadcast(Intent(BROADCAST_MSG))
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        remainingTimer?.cancel()
        dimScreenTimer?.cancel()
        lastFiveMinTimer?.cancel()
    }

    fun onSetProblem(problem: AssessmentProblem?) {
//        if (binding.pencilcaseView.writeModeSwitch.isChecked == false)
//            binding.pencilcaseView.setDefaultState()

        binding.penPanel.apply {
            if (drawType != null) {
                val fingerDrawMode = binding.penPanel.fingerDrawModeSwitch.isChecked
                binding.problemMemoView.fingerDrawMode = fingerDrawMode
                binding.problemContainer.fingerDrawMode = fingerDrawMode
            }

            binding.penPanel.figurePanelCl.visibleIf(false)
            binding.penPanel.penOptionPanelCl.visibleIf(false)
            binding.penPanel.eraserPanelCl.visibleIf(false)
        }

        if (problem == null) {
            problemGesture?.init()
//            solutionGesture?.init()
            binding.apply {
                val conceptFragment =
                    supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
                conceptFragment?.gestureInit()
            }
        } else {

            problemGesture?.init()
//            solutionGesture?.init()
            binding.apply {
                val conceptFragment =
                    supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
                conceptFragment?.gestureInit()

                problemMemoView.load("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_p")
                penPanel.resetMode()
//                solutionMemoView.load("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_s")
                answerView.configureUI(problem, false)

            }
        }
    }

    private fun checkShortAnswer() {
        if (viewModel.currentProblem.value?.type == "주관식") {
            val textValue = binding.answerView.getShortAnswerText()
            if (textValue.isNotEmpty() && viewModel.currentProblem.value?.user_answer?.length ?: 0 < 1) {
                viewModel.currentProblem.value?.user_answer = textValue
            }
        }
    }

    fun onPrevBtnClicked() {
        if (viewModel.currentProblem.value != viewModel.problemList.value?.firstOrNull()) {
            prevAnim()
        } else {
            DaebakToast.show(this, "첫번째 문제입니다 :)")
        }
    }

    fun onNextBtnClicked() {
        if (viewModel.currentProblem.value != viewModel.problemList.value?.lastOrNull()) {
            nextAnim()
        } else {
            DaebakToast.show(this, "마지막 문제입니다 :)")
        }
    }

    fun prevAnim() {
        saveMemo()
        checkShortAnswer()

        binding.apply {
            problemContainer.setOnTouchListener(null)
            val conceptFragment =
                supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
            conceptFragment?.setTouchListener(isRelease = true)
        }


//        binding.solutionContainer.setOnTouchListener(null)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.solveCl.x = binding.problemContainer.measuredWidth * value
            binding.solveCl.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = 100
                anim.addUpdateListener {
                    var value = it.animatedValue as Float
                    value = value.pow(2)
                    binding.solveCl.x =
                        -binding.problemContainer.width.toFloat() + binding.problemContainer.measuredWidth.toFloat() * value
                    binding.solveCl.alpha = value
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        binding.apply {
                            problemContainer.setOnTouchListener(problemGesture)
                            val conceptFragment = supportFragmentManager.findFragmentByTag(
                                "f" + pager.adapter?.getItemId(pager.currentItem)
                            ) as? AssessmentSolveConceptFragment
                            conceptFragment?.setTouchListener(isRelease = false)
                        }
                    }

                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationStart(p0: Animator) {
                        val prevIndex = viewModel.problemIndex.value?.minus(1)
                        viewModel.problemIndex.postValue(prevIndex)
                        val prevProblem = prevIndex?.let { viewModel.problemList.value?.get(it) }
                        onProblemSelected(prevProblem, false)
                    }
                })
                anim.start()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationStart(p0: Animator) {}
        })
        anim.start()
    }

    fun nextAnim() {
        saveMemo()
        checkShortAnswer()

        binding.apply {
            problemContainer.setOnTouchListener(null)
            val conceptFragment =
                supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
            conceptFragment?.setTouchListener(isRelease = true)
        }

        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.solveCl.x = binding.problemContainer.measuredWidth * value * -1
            binding.solveCl.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = 100
                anim.addUpdateListener {
                    var value = it.animatedValue as Float
                    value = value.pow(2)
                    binding.solveCl.x =
                        binding.problemContainer.width.toFloat() - binding.problemContainer.measuredWidth.toFloat() * value
                    binding.solveCl.alpha = value
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        binding.apply {
                            problemContainer.setOnTouchListener(problemGesture)
                            val conceptFragment = supportFragmentManager.findFragmentByTag(
                                "f" + pager.adapter?.getItemId(pager.currentItem)
                            ) as? AssessmentSolveConceptFragment
                            conceptFragment?.setTouchListener(isRelease = false)
                        }

//                        binding.solutionContainer.setOnTouchListener(solutionGesture)

                    }

                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationStart(p0: Animator) {
                        val nextIndex = viewModel.problemIndex.value?.plus(1)
                        viewModel.problemIndex.postValue(nextIndex)
                        val nextProblem = nextIndex?.let { viewModel.problemList.value?.get(it) }
                        onProblemSelected(nextProblem, false)
                    }
                })
                anim.start()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationStart(p0: Animator) {}
        })
        anim.start()
    }

    override fun onDrawTypeChanged(type: DrawType?) {
        if (type == null) {
            binding.apply {
                problemContainer.isBlock = false
                val conceptFragment =
                    supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
                conceptFragment?.setConceptContainerBlock(false)
            }
        } else {
            binding.apply {
                problemContainer.isBlock = true
                val conceptFragment =
                    supportFragmentManager.findFragmentByTag("f" + pager.adapter?.getItemId(pager.currentItem)) as? AssessmentSolveConceptFragment
                conceptFragment?.setConceptContainerBlock(true)
            }
        }

        if (type == DrawType.Pencil)
            Tutor.showToolTipIfNeed(
                binding.penPanel.penBtn,
                Tutor.TooltipType.takeNoteScroll
            )
    }

//    override fun onThicknessSelected(thickness: Float) {
//        val itemName = when (thickness) {
//            Pencilcase.Thickness.line -> "펜굵기-1"
//            Pencilcase.Thickness.thin -> "펜굵기-2"
//            Pencilcase.Thickness.medium -> "펜굵기-3"
//            Pencilcase.Thickness.thick -> "펜굵기-4"
//
//        }
//        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "펜 굵기", "$thickness")
//    }

    override fun onFingerDrawModeChanged(value: Boolean) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "스타일러스온리모드", itemValue)
        binding.problemContainer.fingerDrawMode = value
        binding.problemMemoView.fingerDrawMode = value
    }

//    val answeredSet by lazy { viewModel.answeredSet }


    override fun backPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "뒤로가기", itemValue)
        if (viewModel.answeredSet.isNotEmpty()) {
            DialogUtils.v2GetOutSolveViewDialog(this) {
                saveMemo()
                super.onBackPressed()
            }
        } else {
            saveMemo()
            super.onBackPressed()
        }
    }

    private fun saveMemo() {
        val problem = viewModel.currentProblem.value ?: return
        binding.problemMemoView.save("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_p")
//        binding.solutionMemoView.save("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_s")
    }

    override fun getActivity(): Activity = this

    override fun onAnswerChanged(view: View, answer: String?, problem: AssessmentProblem?) {
        viewModel.answerChanged(answer, problem)
    }

    override fun onEnter() {
        next()
    }

    fun onMarkingBtnClicked() {

    }

    override fun next() {
        onNextBtnClicked()
    }

    override fun prev() {
        onPrevBtnClicked()
    }
    override fun onGestureTouch() {
        binding.penPanel.run {
//            pencilOptionLl.isSelected = false
//            pencilOptionLl.visibility = View.GONE
//            clearAllBtn.isSelected = false
//            clearAllBtn.visibility = View.GONE
        }
    }
    override fun onLeftSwipe() {
        if (binding.penPanel.drawType == null || !binding.penPanel.fingerDrawMode) {
            LogUtils.logEvent(
                this,
                user,
                PulleyEvent.BUTTON_CLICK,
                "바로풀기화면",
                "문제 스와이프-이전",
                itemValue
            )
            if (viewModel.currentProblem.value != viewModel.problemList.value?.firstOrNull())
                prevAnim()
            else
                DaebakToast.show(this, "첫번째 문제입니다 :)")
        }
    }

    override fun onRightSwipe() {
        if (binding.penPanel.drawType == null || !binding.penPanel.fingerDrawMode) {
            LogUtils.logEvent(
                this,
                user,
                PulleyEvent.BUTTON_CLICK,
                "바로풀기화면",
                "문제 스와이프-다음",
                itemValue
            )
            if (viewModel.currentProblem.value != viewModel.problemList.value?.lastOrNull())
                nextAnim()
            else
                DaebakToast.show(this, "마지막 문제입니다 :)")
        }
    }
    override fun onStop() {
        super.onStop()
        saveMemo()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onFoldBtnClicked() {
        viewModel.showGalleryView.postValue(false)
        val galleyParam = (binding.galleryView.layoutParams as LinearLayout.LayoutParams)
        val animator = ValueAnimator.ofInt(0, galleyParam.width)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            binding.rootView.scrollTo(value, 0)
        }
        animator.duration = 150
        animator.start()
        viewModel.currentProblem.value?.let { binding.answerView.configureUI(it, true) }
    }

    override fun onUndoCountChanged(undoCount: Int) {
        if (binding.penPanel.memoViews.size == 0) return
        val undoCount = binding.penPanel.memoViews.map {
            it.undoCount
        }.reduce { acc, next -> acc + next }

        binding.penPanel.undoCount = undoCount
    }

    override fun onRedoCountChanged(redoCount: Int) {
        if (binding.penPanel.memoViews.size == 0) return
        val redoCount = binding.penPanel.memoViews.map {
            it.redoCount
        }.reduce { acc, next -> acc + next }

        binding.penPanel.redoCount = redoCount
    }
}

class ViewPagerAdapter(val fragments: List<Fragment>, fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return AssessmentSolveSolutionFragment.tabSize
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> fragments[position]
            1 -> {
                val fragment = fragments[position]
                fragment
            }
            else -> fragments[position]
        }
    }
}

@BindingAdapter("imgRes")
fun loadImage(view: ImageView, imageUrl: String?) {
    imageUrl?.let {
        view.setProblemImageURL(it)
    }
}