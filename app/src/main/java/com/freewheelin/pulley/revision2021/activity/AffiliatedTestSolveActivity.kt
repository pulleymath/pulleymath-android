package com.freewheelin.pulley.revision2021.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.tabFragment.affiliatedTest.AffiliatedTestFragment
import com.freewheelin.pulley.activities.solve.*
import com.freewheelin.pulley.bases.DensityLevel.*
import com.freewheelin.pulley.bases.densityLevel
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.ActivityAffiliatedTestSolveBinding
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestCard
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedTestSolveViewModel
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import kotlinx.android.synthetic.main.activity_solve.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class AffiliatedTestSolveActivity : AppCompatActivity(),
    AnswerV2Delegate,
    ProblemGestureListener,
//    ObservableHashSetListener<AffiliatedTestProblem>,
    PencilcaseListener {

    private val binding: ActivityAffiliatedTestSolveBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_affiliated_test_solve, null, false)
    }
    private val viewModel: AffiliatedTestSolveViewModel by viewModels()

    val screenWidth by lazy { DisplayUtils.getScreenWidth(this) }
    val screenHeight by lazy { DisplayUtils.getScrenHeight(this) }
    var problemGesture: ProblemGestures? = null
    var solutionGesture: SolveGestures? = null

    var itemValue = ""
        set(value) {
            field = value
            binding.pencilcaseView.itemValue = value
        }

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA) }
    lateinit var finishReceiver: BroadcastReceiver

    companion object {
        val IS_REVIEW = "IS_REVIEW"
        val SELECTED_WORKBOOK = "SELECTED_WORKBOOK"
        val BROADCAST_MSG = "AFF_TEST_SOLVE_ACTIVITY_BROAD"

        fun getIntent(context: Context): Intent {
            val intent = Intent(context, AffiliatedTestSolveActivity::class.java)
            return intent
        }

        fun getIntent(context: Context, card: AffiliatedTestCard): Intent {
            val intent = Intent(context, AffiliatedTestSolveActivity::class.java)
            intent.putExtra(SELECTED_WORKBOOK, card.selectedWorkbook)
            return intent
        }

        fun getIntent(context: Context, workbook: AffiliatedTestWorkbook): Intent {
            val intent = Intent(context, AffiliatedTestSolveActivity::class.java)
            intent.putExtra(SELECTED_WORKBOOK, workbook)
            return intent
        }

        fun getReviewIntent(context: Context, workbook: AffiliatedTestWorkbook): Intent {
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
            selectedWorkbook = intent.getSerializableExtra(SELECTED_WORKBOOK) as? AffiliatedTestWorkbook
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
                DialogUtils.v2FinishTestDialog(this@AffiliatedTestSolveActivity) {
                    val intent = Intent(getActivity(), LearningTabActivity::class.java)
                    intent.putExtra(SELECTED_WORKBOOK, viewModel.selectedWorkbook)
                    setResult(AffiliatedTestFragment.SHOW_REPORT_INT, intent)
                    if (!isFinishing) finish()
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(finishReceiver, IntentFilter(BROADCAST_MSG))

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishReceiver)
        super.onDestroy()
    }

    fun observeLiveData() {
        if (viewModel.isReview.value == true) return
        viewModel.apply {
            problemIndex.observe(this@AffiliatedTestSolveActivity, {
                val problemNo = it + 1
                openProblem(problemNo)
            })
        }
    }

    private fun fetchProblem() {
        viewModel.let {
            if (viewModel.isReview.value == true) {
                it.getTestResult { that ->
                    this@AffiliatedTestSolveActivity.runOnUiThread {
                        onProblemSelected(that, false)
                    }
                }
            } else {
                it.getTestProblems(it.workbookId, it.version) { that ->
                    this@AffiliatedTestSolveActivity.runOnUiThread {
                        onProblemSelected(that, false)
//                    setRemainingTimer()
                    }
                }
            }
        }
    }
    fun initUI () {
        binding.apply {
            lifecycleOwner = this@AffiliatedTestSolveActivity
            vm = viewModel
            backBtn.setOnClickListener {
                onBackPressed()
            }
            solveCl.layoutParams.width = screenWidth
//            solutionContainer.visibility = View.GONE
            prevBtn.setOnClickListener { onPrevBtnClicked() }
            nextBtn.setOnClickListener { onNextBtnClicked() }
            pencilcaseView.listener = this@AffiliatedTestSolveActivity
            problemMemoView.set(pencilcaseView)
            solutionMemoView.set(pencilcaseView)

            val imageWidth = when(this@AffiliatedTestSolveActivity.densityLevel) {
                Low -> screenWidth / 2
                High -> (screenWidth / 2.7).toInt()
                else -> (500.toPx()).toInt()
            }

            problemIv.maxWidth = imageWidth
            solutionIv.maxWidth = imageWidth
            problemMemoView.layoutParams.width = screenWidth
            solutionMemoView.layoutParams.width = screenWidth

            answerView.delegate = this@AffiliatedTestSolveActivity

            dimDialogBtn.setOnClickListener {
                this@AffiliatedTestSolveActivity.finish()
            }
            submitBtn.setOnClickListener {
                if (viewModel.isSubmitBtnActive.value == true) {
                    viewModel.onSubmit(this@AffiliatedTestSolveActivity) {
                        saveMemo()
                        viewModel.finishTest {
                            this@AffiliatedTestSolveActivity.runOnUiThread {
                                DialogUtils.v2FinishTestDialog(this@AffiliatedTestSolveActivity) {
                                    val intent = Intent(getActivity(), LearningTabActivity::class.java)
                                    intent.putExtra(SELECTED_WORKBOOK, viewModel.selectedWorkbook)
                                    setResult(AffiliatedTestFragment.SHOW_REPORT_INT, intent)
                                    if (!isFinishing) finish()
                                }
                            }
                        }
                    }
                }
            }
            baseCl.setOnDragListener { view, dragEvent ->
                when(dragEvent.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {

                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        val x = dragEvent.x
                        val y = dragEvent.y
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        var x = dragEvent.x
                        var y = dragEvent.y
                        val answerHeight = answerView.height

                        if (y > screenHeight) {
                            val answerWidth = answerView.width

                            x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
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
                        LogUtils.logEvent(this@AffiliatedTestSolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "플로팅OMR 드래그", itemValue)
                        answerView.visibility = View.VISIBLE

                        val answerHeight = answerView.height
                        val answerWidth = answerView.width

                        var x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
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

            problemGesture = ProblemGestures(this@AffiliatedTestSolveActivity, problemIv, problemMemoView)
            problemGesture?.listener = this@AffiliatedTestSolveActivity
//            answeredSet.listener = this@AffiliatedTestSolveActivity // 필요없음
            solutionGesture = SolveGestures(this@AffiliatedTestSolveActivity, solutionIv, solutionMemoView, problemInfoContainer)
            solutionGesture?.listener = this@AffiliatedTestSolveActivity

            problemContainer.setOnTouchListener(problemGesture)
            solutionContainer.setOnTouchListener(solutionGesture)

            videoView.setOnClickListener {
                val url = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/android-video-test/winter1.mp4"
                val intent = VideoPlayerActivity.getIntent(baseContext, url)
                startActivity(intent)
            }

            videoView2.setOnClickListener {
                val url = "https://pulley-common.s3.ap-northeast-2.amazonaws.com/android-video-test/winter2.mp4"
                val intent = VideoPlayerActivity.getIntent(baseContext, url)
                startActivity(intent)
            }

        }
    }

    fun onProblemSelected(problem: AffiliatedTestProblem?, autoFocus: Boolean) {
        viewModel.currentProblem.postValue(problem)
        saveMemo()
        onSetProblem(problem)

        if(problem != null) {
            if(viewModel.currentProblem.value?.getResultByScoring() == Result.yet && viewModel.currentProblem.value?.getProblemType() == ProblemType.short) {
            // 문제 안풀었고, 단답이고, 정답보기가off 이고, 빠른채점도 off이면 포커스
            } else {
                binding.answerView.clearFocusOnShortAnswer()
            }
        }

        // 리뷰중이고 틀린문제면 해설 표시
        viewModel.apply {
            val isWrongAnswer = problem?.is_correct == false
            val _isReview = isReview.value == true
            showSolutionView.value = _isReview && isWrongAnswer
            isEnableSolutionSwitch.value = isWrongAnswer
        }
    }

    var lastFiveMinTimer: CountDownTimer? = null
    var remainingTimer: CountDownTimer? = null
    var dimScreenTimer: CountDownTimer? = null
    override fun onResume() {
        super.onResume()

        Handler(Looper.getMainLooper()).postDelayed({
            setScreenDimComeInBeforeTestStart()
        }, 800)
        if (viewModel.isReview.value == true) return

        Handler(Looper.getMainLooper()).postDelayed({
            setRemainingTimer()
            set5MinTimer()
        }, 800)
    }

    private fun set5MinTimer() {
        val before5MinItEnds = viewModel.get5MinBeforeFinishedTimeEnds()?: return // todo dummy
//        val before5MinItEnds = "2022-02-07 11:36:00"

        val paredDate = sdf.parse(before5MinItEnds)
        var nowDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val timeDiffMilli = paredDate.time - nowDate.time.time
        lastFiveMinTimer = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {}

            override fun onFinish() {
                binding.remainingTime.setTextColor(ContextCompat.getColor(this@AffiliatedTestSolveActivity, R.color.red_300))
            }
        }.start()
    }

    private fun setScreenDimComeInBeforeTestStart() {
        val startedAt = viewModel.getStartedTime() ?: return // TODO dummy
//        val startedAt = "2022-02-07 10:00:00"

        var nowDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val paredDate = sdf.parse(startedAt)
        val timeDiffMilli = paredDate.time - nowDate.time.time
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
    private fun setRemainingTimer() {
        if (!viewModel.showTimer) return

        val finishedAt = viewModel.getFinishedTime() ?: return // TODO dummy
//        val finishedAt = "2022-02-07 23:20:00"

        val paredDate = sdf.parse(finishedAt)
        var nowDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val timeDiffMilli = paredDate.time - nowDate.time.time

        remainingTimer = object : CountDownTimer(timeDiffMilli, 1000) {
            override fun onTick(diff: Long) {
                val hour = diff / 1000 / 3600
                val min = (diff / 1000 / 60) - (hour * 60)
                val sec = (diff / 1000) - (min * 60) - (hour * 3600)

                val hourStr = if (hour.toString().length < 2) "0$hour" else hour.toString()
                val minStr = if (min.toString().length < 2) "0$min" else min.toString()
                val secStr = if (sec.toString().length < 2) "0$sec" else sec.toString()
                binding.remainingTime.text = "${hourStr}:${minStr}:${secStr}"
            }

            override fun onFinish() {
                viewModel.finishTest {
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(Intent(BROADCAST_MSG))
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

    fun onSetProblem(problem: AffiliatedTestProblem?) {
        if(binding.pencilcaseView.writeModeSwitch.isChecked == false)
            binding.pencilcaseView.setDefaultState()

        if(problem == null) {
            problemGesture?.init()
            solutionGesture?.init()
        } else {

            problemGesture?.init()
            solutionGesture?.init()
            binding.apply {
//                problemIv.setProblemImageURL(problem.img_url)
//                solutionIv.setProblemImageURL(problem.answer_img_url)
//                answerTv.text = "정답 : ${problem.answer}"
//                lvTv.text = "난이도 : ${problem.level}"
//                unitTv.text = "${problem.unit}"
                problemMemoView.load("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_p")
                solutionMemoView.load("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_s")
                answerView.configureUI(problem, false)

            }
        }
    }

    private fun checkShortAnswer() {
        if (viewModel.currentProblem.value?.type == "주관식") {
            val textValue = binding.answerView.getShortAnswerText()
            if(textValue.isNotEmpty() && viewModel.currentProblem.value?.user_answer?.length ?:0 < 1) {
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
        checkShortAnswer()

        binding.problemContainer.setOnTouchListener(null)
        binding.solutionContainer.setOnTouchListener(null)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.solveCl.x = binding.problemContainer.measuredWidth * value
            binding.solveCl.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {
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
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        binding.problemContainer.setOnTouchListener(problemGesture)
                        binding.solutionContainer.setOnTouchListener(solutionGesture)
                    }

                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        val prevIndex = viewModel.problemIndex.value?.minus(1)
                        viewModel.problemIndex.postValue(prevIndex)
                        val prevProblem = prevIndex?.let { viewModel.problemList.value?.get(it) }
                        onProblemSelected(prevProblem, false)
                    }
                })
                anim.start()
            }

            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        anim.start()
    }
    fun nextAnim() {
        checkShortAnswer()

        binding.problemContainer.setOnTouchListener(null)
        binding.solutionContainer.setOnTouchListener(null)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.solveCl.x = binding.problemContainer.measuredWidth * value * -1
            binding.solveCl.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = 100
                anim.addUpdateListener {
                    var value = it.animatedValue as Float
                    value = value.pow(2)
                    binding.solveCl.x = binding.problemContainer.width.toFloat() - binding.problemContainer.measuredWidth.toFloat() * value
                    binding.solveCl.alpha = value
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        binding.problemContainer.setOnTouchListener(problemGesture)
                        binding.solutionContainer.setOnTouchListener(solutionGesture)
                    }
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        val nextIndex = viewModel.problemIndex.value?.plus(1)
                        viewModel.problemIndex.postValue(nextIndex)
                        val nextProblem = nextIndex?.let { viewModel.problemList.value?.get(it) }
                        onProblemSelected(nextProblem, false)
                    }
                })
                anim.start()
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        anim.start()
    }

    override fun onEditTypeChanged(type: Pencilcase.EditType?) {
        if(type == null) {
            binding.problemContainer.isBlock = false
            binding.solutionContainer.isBlock = false
        } else {
            binding.problemContainer.isBlock = true
            binding.solutionContainer.isBlock = true
        }

        if(type == Pencilcase.EditType.pencil)
            Tutor.showToolTipIfNeed(binding.pencilcaseView.pencilBtn, Tutor.TooltipType.takeNoteScroll)
    }

    override fun onThicknessSelected(thickness: Pencilcase.Thickness) {
        val itemName  = when(thickness) {
            Pencilcase.Thickness.line -> "펜굵기-1"
            Pencilcase.Thickness.thin -> "펜굵기-2"
            Pencilcase.Thickness.medium -> "펜굵기-3"
            Pencilcase.Thickness.thick -> "펜굵기-4"

        }
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", itemName, itemValue)
    }

    override fun onModeChanged() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "필기모드토글", itemValue)
    }

//    var answeredSet: ObservableHashSet<AffiliatedTestProblem> = ObservableHashSet()
//    val answeredSet by lazy { viewModel.answeredSet }


    override fun backPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "뒤로가기", itemValue)
        if(viewModel.answeredSet.isNotEmpty()) {
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
        binding.solutionMemoView.save("${user?.studentID}_${problem.id}_${problem.workbook_id ?: 0}_s")
    }
    override fun getActivity(): Activity = this

    override fun onAnswerChanged(view: View, answer: String?, problem: AffiliatedTestProblem?) {
        viewModel.answerChanged(answer, problem)

//        val problem = problem ?: viewModel.currentProblem.value ?: return
//        problem.user_answer = if(answer != null && answer.isNotEmpty()) answer else null
//        if(problem.user_answer == null) viewModel.answeredSet.remove(problem)
//        else viewModel.answeredSet.add(problem)
//        viewModel.isSubmitBtnActive.value = viewModel.answeredSet.isNotEmpty()
//        if (view == binding.answerView) {
//
//        } else {
//            // 스피드answerview일때 여기를 타는데 이 뷰에는 스피드앤서가 없다
//            binding.answerView.configureUI(problem, false)
//        }
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

    override fun onLeftSwipe() {
        if(binding.pencilcaseView.editType == null) {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문제 스와이프-이전", itemValue)
            if(viewModel.currentProblem.value != viewModel.problemList.value?.firstOrNull())
                prevAnim()
            else
                DaebakToast.show(this, "첫번째 문제입니다 :)")
        }
    }

    override fun onRightSwipe() {
        if(binding.pencilcaseView.editType == null) {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문제 스와이프-다음", itemValue)
            if(viewModel.currentProblem.value != viewModel.problemList.value?.lastOrNull())
                nextAnim()
            else
                DaebakToast.show(this, "마지막 문제입니다 :)")
        }
    }

//    override fun onItemChanged(set: ObservableHashSet<AffiliatedTestProblem>) {
//        if(set.isEmpty()) {
//            binding.answerView.disableMarking()
//        } else {
//            binding.answerView.enableMarking(set.size)
//        }
//    }
}


@BindingAdapter("imgRes")
fun loadImage(view: ImageView, imageUrl: String?) {
    imageUrl?.let {
        view.setProblemImageURL(it)
    }
}