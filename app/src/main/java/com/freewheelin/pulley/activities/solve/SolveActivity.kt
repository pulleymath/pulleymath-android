package com.freewheelin.pulley.activities.solve

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.bases.*
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.manage.*
import com.freewheelin.pulley.core.manage.MockExamManager.ARG_MOCK_IS_RESTART
import com.freewheelin.pulley.core.manage.MockExamManager.ARG_START_PROBLEM
import com.freewheelin.pulley.core.manage.PieceManager.ARG_REVIEW_SYNC
import com.freewheelin.pulley.core.tutorial.Tutor
import com.freewheelin.pulley.databinding.ActivitySolveBinding
import com.freewheelin.pulley.dialogs.*
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.lib.ObservableHashSetListener
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemErrorStatus
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.*
import com.freewheelin.pulley.revision2021.activity.MockReportActivity
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.pow

class SolveActivity : BaseActivity(),
        AnswerDelegate,
        GalleryViewDelegate,
        SpeedAnswerDelegate,
        PencilcaseListener,
        ProblemGestureListener,
        ObservableHashSetListener<Problem>,
        LifecycleObserver,
        AppUsageMonitorListener {

    private val binding: ActivitySolveBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_solve, null,false)
    }

    val screenWidth by lazy { DisplayUtils.getScreenWidth(this) }
    val screenHeight by lazy { DisplayUtils.getScreenHeight(this) }

    var problemGesture: ProblemGestures? = null
    var solutionGesture: SolveGestures? = null
    var content: Content? = null
    var isReview:Boolean = false

    companion object {

        val IS_REVIEW = "IS_REVIEW"

        var arg_piece_problems:List<Problem>? = null

        fun getIntent(context: Context): Intent {
            val intent = Intent(context, SolveActivity::class.java)
            return intent
        }

        fun getIntent(context: Context, content: Content): Intent {
            val intent = Intent(context, SolveActivity::class.java)
            intent.putExtra(ContentManager.ARG_CONTENT, content)
            return intent
        }

        fun getIntent(context: Context, content: Content, isRestart: Boolean): Intent {
            val intent = getIntent(context, content)
            intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, isRestart)
            return intent
        }

        fun getReviewIntent(context: Context, content: Content, startProblemID: Int): Intent {
            val intent = getIntent(context, content)
            intent.putExtra(IS_REVIEW, true)
            intent.putExtra(ARG_START_PROBLEM, startProblemID)

            return intent
        }

        fun getReviewIntent(context: Context, content: Content, isNeedSync: Boolean = true): Intent {
            val intent = Intent(context, SolveActivity::class.java)
            intent.putExtra(IS_REVIEW, true)
            intent.putExtra(ContentManager.ARG_CONTENT, content)
            intent.putExtra(ARG_REVIEW_SYNC, isNeedSync)

            return intent
        }
        fun getReviewIntent(context: Context, subject: String, problems: List<Problem>): Intent {
            val intent = Intent(context, SolveActivity::class.java)
            intent.putExtra(IS_REVIEW, true)
//            intent.putExtra(PieceManager.ARG_PIECE_PROBLEMS, ArrayList(problems))
            arg_piece_problems = problems
            intent.putExtra(PieceManager.ARG_PIECE_SUBJECT, subject)

            return intent
        }
    }

    var selectedProblem: Problem? = null
    override val isShowAnswer: Boolean
        get() = binding.speedyScoreSwitch.isChecked

    var answeredSet: ObservableHashSet<Problem> = ObservableHashSet()
    var itemValue = ""
        set(value) {
            field = value
            binding.pencilcaseView.itemValue = value
            binding.galleryView.itemValue = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        initUI()

        val content = intent.getSerializableExtra(ContentManager.ARG_CONTENT) as? Content
        isReview = intent.getBooleanExtra(IS_REVIEW, false)

        Log.d("문제풀기", "content=$content")
        Log.d("문제풀기", "isReview=$isReview")

        if(isReview)
            initReviewContent(content)
        else
            initContent(content)

        Log.d("문제풀기", "onCreate()")

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setInitPosition()

//        setSpen()
    }

    override fun onResume() {
        Log.d("문제풀기", "onResume()")
        super.onResume()
    }

    override fun onStop() {
        saveMemo()
        Log.d("문제풀기", "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        arg_piece_problems = null
        AppUsageMonitor.finishStudy(this)
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        binding.timerView?.deinitTimer()
    }

    override fun onBackPressed() {
        Log.d("문제풀기", "onBackPressed()")
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "뒤로가기", itemValue)
        if(answeredSet.isNotEmpty()) {
            val canceListener = if(content is Test) DialogInterface.OnCancelListener {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "뒤로가기취소", "문제풀이직후")
            } else null

            DialogUtils.showProblemSolveExitDialog(this, answeredSet.size, onExitClicked = {
                saveMemo()
                super.onBackPressed()
            }, cancelListener = canceListener)
        } else {
            saveMemo()
            super.onBackPressed()
        }
    }

    // spen 처리
//    private var mSpenRemote: SpenRemote? = null
//    private var mSpenUnitManager: SpenUnitManager? = null
//
//    private fun setSpen() {
//        mSpenRemote = SpenRemote.getInstance()
//        mSpenRemote?.isFeatureEnabled(FEATURE_TYPE_BUTTON)
//
//        Log.d("펜체크", "setSpen() = $mSpenRemote, ${mSpenRemote?.isConnected}")
//
//        if(mSpenRemote?.isConnected == false) {
//            mSpenRemote?.connect(this,
//                object : SpenRemote.ConnectionResultCallback {
//
//                    override fun onSuccess(manager : SpenUnitManager) {
//                        mSpenUnitManager = manager
//                        spenListener(manager)
//                    }
//
//                    override fun onFailure(error: Int) {
//                        Log.e("펜체크", "could not connect!")
//                    }
//                })
//        }
//    }
//
//    private fun spenListener(mSpenUnitManager: SpenUnitManager) {
//        val button = mSpenUnitManager.getUnit(SpenUnit.TYPE_BUTTON)
//        Log.d("펜체크", "spenListener() = $mSpenUnitManager")
//        mSpenUnitManager.registerSpenEventListener({ event ->
//            val buttonEvent = ButtonEvent(event)
//
//            Log.d("펜체크", "SpenEvent=$event")
//
//            when (buttonEvent.action) {
//                ButtonEvent.ACTION_DOWN -> {
//                    Log.d("펜체크", "Spen Button Pressed")
//                }
//                ButtonEvent.ACTION_UP -> {
//                    Log.d("펜체크", "Spen Button Released")
//                }
//            }
//        }, button)
//    }

    fun initReviewContent(content: Content?) {
        Log.d("문제풀기", "initReview content======>$content")
        with(binding) {
            onItemChanged(answeredSet)
            timerView.visibility = View.INVISIBLE
            mainFormatTool.visibility = View.VISIBLE

            when(content) {
                is Book -> {
                    itemValue = "유형학습-리뷰"
                    if (content.pieceCategoryTag == BookType.BOOK) {
                        BookManager.reviewBookV2(this@SolveActivity, content, user!!) {
                            it.arrangeChapter()
                            this@SolveActivity.content = it
                            galleryView.set(it)
                            speedAnswerView.set(it)
                            answerView.showMarkingBtn()
                            speedAnswerView.showMarkingBtn()
                        }
                    } else {
                        BookManager.reviewCustomBookV2(this@SolveActivity, content, user!!) {
                            this@SolveActivity.content = it
                            galleryView.set(it)
                            speedAnswerView.set(it)
                            answerView.showMarkingBtn()
                            speedAnswerView.showMarkingBtn()
                        }
                    }

                }
                is Test -> {
                    itemValue = "테스트-리뷰"
                    TestManager.getTestReview(this@SolveActivity, user!!, content) {
                        this@SolveActivity.content = it
                        galleryView.set(it)
                        speedAnswerView.set(it)
                        answerView.showMarkingBtn()
                        speedAnswerView.showMarkingBtn()
                    }
                }

                is MockExam -> {
                    itemValue = "모의고사-리뷰"
                    MockExamManager.getExamReviewProblems(this@SolveActivity, content, user!!) { it ->
                        this@SolveActivity.content = it
                        galleryView.set(it)
                        speedAnswerView.set(it)
                        answerView.showMarkingBtn()
                        speedAnswerView.showMarkingBtn()

                        val startProblemID = intent?.extras?.getInt(ARG_START_PROBLEM, -1)
                        val startProblem = this@SolveActivity.content?.problems?.filter { it.id == startProblemID }?.firstOrNull()
                        if (startProblem != null)
                            galleryView.select(startProblem)
                    }
                }

                is Piece -> {
                    itemValue = "2차학습-리뷰"
                    val isNeedSync = intent.getBooleanExtra(ARG_REVIEW_SYNC, false)

                    if (isNeedSync) {
                        PieceManager.getPieceReviewProblems(this@SolveActivity, content, user!!) {
                            this@SolveActivity.content = it
                            galleryView.set(it)
                            speedAnswerView.set(it)
                            answerView.showMarkingBtn()
                            speedAnswerView.showMarkingBtn()
                        }
                    } else {
                        this@SolveActivity.content = content
                        galleryView.set(content)
                        speedAnswerView.set(content)
                        answerView.showMarkingBtn()
                        speedAnswerView.showMarkingBtn()
                    }
                }
                else -> {
                    itemValue = "2차학습-리뷰"
//                val problems = intent.getSerializableExtra(PieceManager.ARG_PIECE_PROBLEMS) as? List<Problem>
                    arg_piece_problems?.let {
                        val subject = intent.getStringExtra(PieceManager.ARG_PIECE_SUBJECT)?:""
                        PieceManager.getReviewInfo(this@SolveActivity, subject, it, user!!) {
                            this@SolveActivity.content = it
                            galleryView.set(it)
                            speedAnswerView.set(it)
                            answerView.showMarkingBtn()
                            speedAnswerView.showMarkingBtn()
                        }
                    }
                }
            }
        }
    }

    fun initContent(content: Content?) {

        Log.d("문제풀기", "init content======>$content")
        with(binding) {
            when(content) {
                is Book -> {
                    itemValue = "유형학습"
                    timerView.visibility = View.INVISIBLE
                    mainFormatTool.visibility = View.VISIBLE
                    BookManager.getBook(this@SolveActivity, content, user!!) {
                        Log.d("유형학습", "init getBook======>$it")
                        this@SolveActivity.content = it
                        galleryView.set(it)
                        speedAnswerView.set(it)
                        answerView.showMarkingBtn()
                        speedAnswerView.showMarkingBtn()
                    }
                }
                is Test -> {
                    itemValue = "테스트"
                    solutionSwitch.visibility = View.GONE
                    timerView.visibility = View.INVISIBLE
                    mainFormatTool.visibility = View.VISIBLE

                    when (content.getTestType()) {
                        Test.TestType.daily -> {
                            TestManager.getDailyTest(this@SolveActivity, user!!, content) {
                                it.scoringTestPieceCount = content.scoringTestPieceCount
                                this@SolveActivity.content = it
                                galleryView.set(it)
                                galleryView.hideFilter()
                                speedAnswerView.set(it)
                                answerView.showSubmitBtn()
                                speedAnswerView.showSubmitBtn()
                            }
                        }
                        else -> {
                            TestManager.getTest(this@SolveActivity, user!!, content) {
                                it.scoringTestPieceCount = content.scoringTestPieceCount
                                this@SolveActivity.content = it
                                galleryView.set(it)
                                galleryView.hideFilter()
                                speedAnswerView.set(it)
                                answerView.showSubmitBtn()
                                speedAnswerView.showSubmitBtn()
                            }
                        }
                    }
                }
                is MockExam -> {
                    itemValue = "모의고사"
                    solutionSwitch.visibility = View.GONE
                    val isRestart = intent.getBooleanExtra(ARG_MOCK_IS_RESTART, false)
                    timerView.visibility = View.VISIBLE
                    timerView.setTimerViewListener(object : SolveTimerViewListener {
                        override fun onSubmitTypeChanged(submitType: SolveTimerView.SubmitType) {
                            val itemName = if (submitType == SolveTimerView.SubmitType.lenient) "시간제한없음" else "100분자동제출"
                            LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", itemName, itemValue)
                        }

                        override fun onTimerSwitchChecked() {
                            LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "타이머표시", itemValue)
                        }

                        override fun onTimerStopClicked() {
                            LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "일시정지", itemValue)
                        }

                        override fun onTimerExpired(solveTimerView: SolveTimerView, type: SolveTimerView.SubmitType) {
                            if (type == SolveTimerView.SubmitType.lenient) return

                            solveTimerView.stop()
                            val notSolvedProblem = content.problems.filter { !it.isUserAnswerInput() }

                            DialogUtils.showExamExpiredDialog(this@SolveActivity, notSolvedProblem.size,
                                onSolveClicked = {
//                                    if (timerView.isTimerShown())
//                                        timerView.showOverTimerView()
                                    solveTimerView.hideTypeRadio()
                                    solveTimerView.runTimer()
                                },
                                onSubmitClicked = {
                                    val time = solveTimerView.elapsedTime
                                    ContentManager.score(this@SolveActivity, user!!, content, content.problems.toSet(), time) {
                                        val intent = MockReportActivity.getIntent(this@SolveActivity, content, it)
                                        startActivity(intent)
                                        setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                                        finish()
                                    }
                                }
                            )
                        }
                    })

                    mainFormatTool.visibility = View.GONE

                    MockExamManager.getMockProblems(this@SolveActivity, content, user!!) {
                        Log.d("문제풀기", "모의고사 it=${it.assignID}")
                        content.assignID = it.assignID
                        content.problems = it.problems
                        content.time = it.time
//                    content.problems.forEach { if(it.getResultByScoring() != Result.yet) { it.rawResult == Result.yet.rawValue } }
                        this@SolveActivity.content = content

                        if (content.time != null && !isRestart) {
                            val time = content.time!!
                            if (time >= 6000) {
                                timerView.submitType = SolveTimerView.SubmitType.lenient
                                timerView.setLenientOvetimeUI()
                            }
                            timerView.elapsedTime = time
                        }

                        AnimationUtils.showTimer(this@SolveActivity, 3, "시험 시작!", object : AnimationListener {
                            override fun onAnimationEnd() {
                                timerView.runTimer()
                            }

                            override fun onAnimationCancel() {
                                timerView.runTimer()
                            }
                        })

                        galleryView.set(content)
                        galleryView.hideFilter()
                        speedAnswerView.set(content)
                        answerView.showSubmitBtn()
                        speedAnswerView.showSubmitBtn()

                        resetProblemResult(content)
                    }
                }
                is Piece -> {
                    itemValue = "2차학습"
                    timerView.visibility = View.INVISIBLE
                    mainFormatTool.visibility = View.VISIBLE
                    PieceManager.getProblems(this@SolveActivity, content, user!!) {
                        content.problems = it
                        this@SolveActivity.content = content
                        galleryView.set(content)
                        speedAnswerView.set(content)
                        answerView.showMarkingBtn()
                        speedAnswerView.showMarkingBtn()
                    }
                }
            }

            onItemChanged(answeredSet)
        }
    }

    // 모의고사 다시 풀기시 문제 전부 채점 전 상태로 변경
    private fun resetProblemResult(content:Content) {
        content.problems.forEach {
            it.rawResult = Result.yet.rawValue
        }
    }

    private fun initUI() {


        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
        }

        with(binding) {
            if(isSPYMode) {
                spyBtn.visibility = View.VISIBLE
                spyBtn.setOnClickListener { onSpyBtnClikcked() }
            } else {
                spyBtn.visibility = View.GONE
            }

            galleryView.layoutParams.width = GalleryView.getGalleryViewWidth(this@SolveActivity)
            solveCl.layoutParams.width = screenWidth
            galleryView.delegate = this@SolveActivity

//        var isSetScrollPosition: Boolean = false
//        rootView.viewTreeObserver.addOnGlobalLayoutListener {
//            if(isSetScrollPosition == false) {
//                rootView.scrollX = GalleryView.getGalleryViewWidth(this)
//                answerView.setInitPosition()
//                isSetScrollPosition = true
//            }
//        }

            answeredSet.listener = this@SolveActivity
            speedAnswerView.answerDelegate = this@SolveActivity
            solutionContainer.visibility = View.GONE
            speedAnswerView.visibility = View.GONE

            backBtn.setOnClickListener { onBackPressed() }
            galleryBtn.setOnClickListener { onGalleryBtnClicked() }
            galleryBtn.extensionTouchArea(8.toPx())
            galleryCloser.setOnTouchListener { view, motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-갤러리화면닫기", itemValue)
                    onFoldBtnClicked()
                }
                true
            }
            prevBtn.setOnClickListener { onPrevBtnClicked() }
            nextBtn.setOnClickListener { onNextBtnClicked() }
            clearBtn.setOnClickListener { onClearBtnClicked() }
            scrapBtn.setOnClickListener { onScrapBtnClicked() }
            reportBtn.setOnClickListener { onSirenBtnClicked() }
            // 풀리플러스 처리
            lockIv.visibility = if(user!!.hasPulleyPlus) View.GONE else View.VISIBLE
            plusIv.visibility = if(user!!.hasPulleyPlus) View.VISIBLE else View.GONE
            addSimilarProblemCl.setOnClickListener {
                if(user!!.hasPulleyPlus) {
                    onAddSimilarBtnClicked()
                } else {
                    DialogUtils.confirmHasPulleyPlus(this@SolveActivity) {
                        PulleyPlusPriceDialog(this@SolveActivity).show()
                    }
                }
            }
            changeSimilarProblemCl.setOnClickListener { onChangeSimilarBtnClicked() }
            answerView.markingBtn.setOnClickListener {
                LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "바로-채점하기", itemValue)
                onMarkingBtnClicked()
            }
            answerView.submitBtn.setOnClickListener {
                LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "바로-채점하기", itemValue)
                onSubmitBtnClicked()
            }
            speedAnswerView.markingBtn.setOnClickListener {
                LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "빠른-채점하기", itemValue)
                onMarkingBtnClicked()
            }
            speedAnswerView.submitBtn.setOnClickListener {
                LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "빠른-채점하기", itemValue)
                onSubmitBtnClicked()
            }
            pencilcaseView.listener = this@SolveActivity
            problemMemoView.set(pencilcaseView)
            solutionMemoView.set(pencilcaseView)

            speedyScoreSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { button, isChecked ->
                onSpeedyScoringCheckChanged(isChecked)
            })
            solutionSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { button, isChecked ->
                onShowSolutionCheckChanged(isChecked)
            })

            val imageWidth = when(densityLevel) {
                DensityLevel.Low -> screenWidth / 2
                DensityLevel.High -> (screenWidth / 2.7).toInt()
                else -> (500.toPx()).toInt()
            }

            problemIv.maxWidth = imageWidth
            solutionIv.maxWidth = imageWidth

            problemMemoView.layoutParams.width = screenWidth
            solutionMemoView.layoutParams.width = screenWidth
            answerView.delegate = this@SolveActivity

            solveCl.setOnDragListener { view, dragEvent ->
                when(dragEvent.action) {
                    DragEvent.ACTION_DRAG_LOCATION -> {

                    }
                    DragEvent.ACTION_DRAG_STARTED -> {
                        val x = dragEvent.x
                        val y = dragEvent.y
                        Log.d("드래그", "Started x=$x, y=$y")
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        var x = dragEvent.x
                        var y = dragEvent.y

                        if (y > screenHeight) {
                            val answerHeight = answerView.height
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
                        }
                        Log.d("드래그", "Ended x=$x, y=$y, sw=$screenWidth, sh=$screenHeight")
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        answerView.cancelDragAndDrop()
//                    }
                    }
                    DragEvent.ACTION_DROP -> {
                        LogUtils.logEvent(this@SolveActivity, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "플로팅OMR 드래그", itemValue)
                        answerView.visibility = View.VISIBLE

                        val answerHeight = answerView.height
                        val answerWidth = answerView.width

                        var x = dragEvent.x - (40.toPx() / 2f + view.resources.getDimension(R.dimen.dp16))
                        var y = dragEvent.y - answerHeight / 2f

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

                        Log.d("드래그", "x=$x, y=$y, w=$answerWidth")

                        answerView.setPosition(x, y)
                    }
                }
                true
            }

            problemGesture = ProblemGestures(this@SolveActivity, problemIv, problemMemoView)
            problemGesture?.listener = this@SolveActivity
            problemContainer.setOnTouchListener(problemGesture)

            solutionGesture = SolveGestures(this@SolveActivity, solutionIv, solutionMemoView, problemInfoContainer)
            solutionGesture?.listener = this@SolveActivity
            solutionContainer.setOnTouchListener(solutionGesture)
            highlightGalleryBtnIfNeed()
        }
    }

    private fun setInitPosition() {
        with(binding) {
            rootView.postDelayed({
                rootView.scrollX = GalleryView.getGalleryViewWidth(this@SolveActivity)
                answerView.setInitPosition()
            }, 0)

            rootView.viewTreeObserver.addOnGlobalFocusChangeListener { oldFocus, newFocus ->
                Log.d("키보드", "currentFocus new=$newFocus id=${newFocus?.id} old=$oldFocus")
                if(newFocus != null){
                    when(newFocus) {
                        is AppCompatImageButton, is Switch, is SwitchCompat, is DaebakSwitch, is AppCompatTextView, is AppCompatImageView -> {
                            newFocus.clearFocus()
                        }
                    }
                }
            }
        }
    }

    fun onMarkingBtnClicked() {
        if (content == null || answeredSet.isEmpty()) return
        if(content is Test) {
            ContentManager.score(this, user!!, content!!, answeredSet) {
                val scoredCnt = answeredSet.size
                answeredSet.forEach { it.mark() }
                answeredSet.clear()
                binding.galleryView.updateAll()
                binding.speedAnswerView.updateAll()
                onProblemSelected(selectedProblem)

                AddOptionUnitToast.showCompleteDialogIfNeed(this, it)
//                if(it?.isNeedToShowCompletedToast() == true) {
//                    SuccessToast.showCompleteDialogIfNeed(this, it)
//                }
//                if(it?.getAskAddSubjects()?.isNotEmpty() == true && user!!.isShowAddOptionalSubjectStatus()) {
//                    AddOptionUnitToast.showCompleteDialogIfNeed(this, it)
//                }
            }
        } else {
            // 추천 학습지 채점 로그 분리
            if(content is Book) {
                if((content as Book).clientBookType == ClientBookType.RECOMMEND)
                    LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면-추천", "채점", content?.assignID?.toString()?:"")
            }

            ContentManager.score(this, user!!, content!!, answeredSet) {
                val scoredCnt = answeredSet.size

                answeredSet.forEach { it.mark() }
                answeredSet.clear()
                binding.galleryView.updateAll()
                binding.speedAnswerView.updateAll()
                onProblemSelected(selectedProblem)

                if(it?.isNeedToShowCompletedToast() == true) {
                    SuccessToast.showCompleteDialogIfNeed(this, it)
                }
                if(it?.getAskAddSubjects()?.isNotEmpty() == true && user!!.isShowAddOptionalSubjectStatus()) {
                    AddOptionUnitToast.showCompleteDialogIfNeed(this, it)
                }
            }
        }
    }

    fun onSubmitBtnClicked() {
        saveMemo()
        when(content) {
            is Test -> {
                val test = content as Test
                val notSolvedProblems = test.problems.filter { it.userAnswer == null }
                val dialogTitle = "검토까지 끝났나요?"
                val dialogContents = if (notSolvedProblems.isEmpty()) {
                    "제출하시면 테스트가 종료됩니다."
                } else {
                    "풀지 않은 문제: ${notSolvedProblems.size}개\n" +
                            "제출하시면 테스트가 종료됩니다."
                }

                val dialog = DialogUtils.makeDialog(
                        this,
                        dialogTitle,
                        dialogContents,
                        "취소", "제출하기")
                dialog.binding.leftBtn.setOnClickListener {
                    dialog.cancel()
                }
                dialog.setOnCancelListener {
                    LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "제출취소", "문제풀이직후")
                }

                dialog.binding.rightBtn.setOnClickListener {
                    if (content == null) {
                        DaebakToast.show(this, "처리할 내용이 없습니다.")
                    } else {
                        answeredSet.addAll(content!!.problems)
                        ContentManager.score(this, user!!, test, answeredSet) {
                            val scoredCnt = answeredSet.size
                            answeredSet.forEach { it.mark() }
                            answeredSet.clear()
                            binding.galleryView.updateAll()
                            binding.speedAnswerView.updateAll()
                            onProblemSelected(selectedProblem)
                            binding.answerView.showMarkingBtn()
                            binding.speedAnswerView.showMarkingBtn()
                            binding.galleryView.showFilter()
                            dialog.dismiss()
                            binding.solutionSwitch.visibility = View.VISIBLE

                            Log.d("테스", "askAddSubjectCode=${it?.getAskAddSubjects()}, show=${user!!.isShowAddOptionalSubjectStatus()}")

//                            if(it?.getAskAddSubjects()?.isNotEmpty() == true && user!!.isShowAddOptionalSubjectStatus()) {
//                                AddOptionUnitToast.showCompleteDialogIfNeed(this, it)
//                            } else {
                            if (test.getTestType() == Test.TestType.daily || test.getTestType() == Test.TestType.weekly)
                                SubmitCompleteLottieDialog(this, test).show {
                                    SuccessToast.showCompleteDialogIfNeed(this, it)
                                }
                            else {
                                SuccessToast.showCompleteDialogIfNeed(this, it)
                            }
//                            }
                        }
                    }
                }
                dialog.show()

            }

            is MockExam -> {
                val exam = content as MockExam
                val notSolvedCnt = exam.problems.filter { !it.isUserAnswerInput() }.size
                val time = binding.timerView!!.elapsedTime

                Log.d("선택모의고사", "exam=${exam.assignID}")

                if (notSolvedCnt > 0) {
                    DialogUtils.showExamSubmitDialog(this, notSolvedCnt) {
                        ContentManager.score(this, user!!, exam, answeredSet, time) {
                            setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                            finish()
                        }
                    }
                } else {
                    val dialog = CompleteDialog(this, "수고하셨습니다!", "분석 보고서로 이동합니다.")
                    dialog.setCancelable(false)
                    dialog.showFor {
                        ContentManager.score(this, user!!, exam, exam.problems.toSet(), time) {
                            getMockWithOptionalSubjects(exam) { mock ->
                                val intent = MockReportActivity.getIntent(this@SolveActivity, mock)
                                startActivity(intent)
                                setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSpeedNumberClicked(problem: Problem) {
        binding.galleryView.select(problem)
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean { // 화면 끌림 방지
//        if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//            return false
//        }
//        return super.onKeyDown(keyCode, event)
//    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("키보드", "솔브액티비티 event=$event")

        if( event?.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || event?.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || (keyCode >= KeyEvent.KEYCODE_1  && keyCode <= KeyEvent.KEYCODE_5)
                || event?.keyCode == KeyEvent.KEYCODE_ENTER
                || event?.keyCode == KeyEvent.KEYCODE_DEL
                || event?.keyCode == KeyEvent.KEYCODE_TAB
                || event?.keyCode == KeyEvent.KEYCODE_DPAD_UP
                || event?.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            when (event?.keyCode) {

                KeyEvent.KEYCODE_1 -> inputNumber(1)
                KeyEvent.KEYCODE_2 -> inputNumber(2)
                KeyEvent.KEYCODE_3 -> inputNumber(3)
                KeyEvent.KEYCODE_4 -> inputNumber(4)
                KeyEvent.KEYCODE_5 -> inputNumber(5)

                KeyEvent.KEYCODE_DPAD_LEFT -> if (binding.speedAnswerView.visibility != View.VISIBLE) prev()
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (binding.speedAnswerView.visibility != View.VISIBLE) next()
                KeyEvent.KEYCODE_ENTER -> if (binding.speedAnswerView.visibility != View.VISIBLE) onEnter()
                KeyEvent.KEYCODE_DEL -> if (binding.speedAnswerView.visibility != View.VISIBLE) inputBack()
                KeyEvent.KEYCODE_TAB -> next()
//                KeyEvent.KEYCODE_DPAD_DOWN -> next()
//                KeyEvent.KEYCODE_DPAD_UP -> prev()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun speedAnswerViewNext() {
        if (binding.speedAnswerView.visibility == View.VISIBLE) {
            val problems = content?.problems ?: return
            val index = problems.indexOf(selectedProblem)
            binding.speedAnswerView.changedFocus(focusIndex = index, prevIndex = index-1)
        }
    }

    private fun speedAnswerViewPrev() {
        if (binding.speedAnswerView.visibility == View.VISIBLE) {
            val problems = content?.problems ?: return
            val index = problems.indexOf(selectedProblem)
            binding.speedAnswerView.changedFocus(focusIndex = index, prevIndex = index+1)
        }
    }

    override fun backPressed() {
        onBackPressed()
    }

    override fun getActivity() : Activity = this

    override fun onAnswerChanged(view: View, answer: String?, problem: Problem?) {
        val problem = problem ?: selectedProblem ?: return

        problem.userAnswer = if(answer != null && answer.isNotEmpty()) answer else null
        if(problem.userAnswer == null)
            answeredSet.remove(problem)
        else
            answeredSet.add(problem)

        binding.galleryView.update(problem)

        if(selectedProblem != problem)
            binding.galleryView.select(problem)

        binding.galleryView.clearFocus()
        if(binding.speedAnswerView.visibility != View.VISIBLE) {
            binding.speedAnswerView.clearFocus()
        }

        if(view == binding.answerView) {
            binding.speedAnswerView.update(problem)
        } else {
            binding.answerView.configureUI(problem, binding.speedAnswerView.visibility == View.GONE && binding.galleryCloser.visibility == View.GONE)
        }
    }

    private fun inputNumber(num: Int) {
        Log.d("키보드", "inputnum => scoring=${selectedProblem?.getResultByScoring()}, type=${selectedProblem?.problemType}, num=$num")
        if(selectedProblem?.getResultByScoring() == Result.yet) { // 아직 채점하지 않았고,
            when (selectedProblem?.problemType){
                ProblemType.single, ProblemType.multi ->
                    binding.answerView.enterNumberBtnClickedFromSolve("$num", selectedProblem!!.problemType)
                else -> {}
            }
        }
    }

    private fun inputBack() {
        Log.d("키보드", "inputnum => scoring=${selectedProblem?.getResultByScoring()}, type=${selectedProblem?.problemType}")
        if(selectedProblem?.getResultByScoring() == Result.yet) { // 아직 채점하지 않았고,
            when (selectedProblem?.problemType){
                ProblemType.short -> binding.answerView.deleteBtnClicked()
                else -> {}
            }
        } else {
            onBackPressed()
        }
    }

    override fun onEnter() {
//        Log.d("키보드", "enter scoring=${selectedProblem?.getResultByScoring()}, type=${selectedProblem?.problemType}")
//        Log.d("키보드", "submit=${answerView.isShowSubmit}")

        if(selectedProblem?.getResultByScoring() == Result.yet) { // 아직 채점하지 않았을 때만
            binding.answerView.releasePad()
            if(binding.answerView.isShowSubmit) {
                onSubmitBtnClicked()
            } else {
                onMarkingBtnClicked()
            }
            binding.answerView.requestFocus()
        } else {
            next()
        }
    }

    override fun next() {
        if(binding.galleryCloser.visibility != View.VISIBLE) {
            onNextBtnClicked()
        } // 갤러리가 닫혀 있을 때만
    }

    override fun prev() {
        if(binding.galleryCloser.visibility != View.VISIBLE) {
            onPrevBtnClicked()
        } // 갤러리가 닫혀 있을 때만
    }

    override fun onFoldBtnClicked() {
        binding.galleryCloser.visibility = View.GONE

        val galleyParam = (binding.galleryView.layoutParams as LinearLayout.LayoutParams)
        val animator = ValueAnimator.ofInt(0, galleyParam.width)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            binding.rootView.scrollTo(value, 0)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.galleryBtn.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }

        })
        animator.duration = 150
        animator.start()

        selectedProblem?.let { selected ->
            binding.answerView?.configureUI(selected, true)

            if (binding.speedyScoreSwitch.isChecked) {
                binding.speedAnswerView.scrollTo(selected, "onFoldBtnClicked()")
            }
        }
    }

    override fun onFilterCheckChanged(isChecked: Boolean) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤-틀린문제토글", itemValue)
        with(binding) {
            emptyGuide.visibility = galleryView.emptyFilterContainer.visibility
            speedAnswerView.isFilter = isChecked
            if(galleryCloser.visibility != View.VISIBLE) { // 갤러리가 열려있을 때는 안보여야 됨
                speedAnswerView.emptyFilterContainer.visibility = galleryView.emptyFilterContainer.visibility
                answerView.visibility = getAnswerViewVisibility(selectedProblem)
            }
        }
    }

    fun onPrevBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문항 양옆 이전 버튼", itemValue)
        if(selectedProblem != content?.problems?.firstOrNull())
            prevAnim()
        else
            DaebakToast.show(this, "첫번째 문제입니다 :)")
    }

    fun onNextBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문항 양옆 다음 버튼", itemValue)
        if(selectedProblem != content?.problems?.lastOrNull())
            nextAnim()
        else
            DaebakToast.show(this, "마지막 문제입니다 :)")
    }

    override fun onLeftSwipe() {
        if(binding.pencilcaseView.editType == null) {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문제 스와이프-이전", itemValue)
            if(selectedProblem != content?.problems?.firstOrNull())
                prevAnim()
            else
                DaebakToast.show(this, "첫번째 문제입니다 :)")
        }
    }

    override fun onRightSwipe() {
        if(binding.pencilcaseView.editType == null) {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문제 스와이프-다음", itemValue)
            if(selectedProblem != content?.problems?.lastOrNull())
                nextAnim()
            else
                DaebakToast.show(this, "마지막 문제입니다 :)")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        AppUsageMonitor.startStudy(this)
//        VersionManager.requestVersionInfo(this) {
//            handleUser()
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        AppUsageMonitor.finishStudy(this)
    }
    // 키보드로 입력시 주관식 정답 저장 안되는 현상
    private fun checkShortAnswer() {
        if(selectedProblem?.problemType == ProblemType.short) {
            val textValue = binding.answerView.getShortAnswerText()
            if(textValue?.length!! > 0 && selectedProblem?.userAnswer?.length ?:0 < 1) {
                selectedProblem?.userAnswer = textValue
            }
        }
    }

    private fun prevAnim() {
        checkShortAnswer()

        binding.problemContainer.setOnTouchListener(null)
        binding.solutionContainer.setOnTouchListener(null)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.container.x = binding.problemContainer.measuredWidth * value
            binding.container.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = 100
                anim.addUpdateListener {
                    var value = it.animatedValue as Float
                    value = value.pow(2)
                    binding.container.x =
                        -binding.problemContainer.width.toFloat() + binding.problemContainer.measuredWidth.toFloat() * value
                    binding.container.alpha = value
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator) {
                    }

                    override fun onAnimationEnd(p0: Animator) {
                        binding.problemContainer.setOnTouchListener(problemGesture)
                        binding.solutionContainer.setOnTouchListener(solutionGesture)
                    }

                    override fun onAnimationCancel(p0: Animator) {
                    }

                    override fun onAnimationStart(p0: Animator) {
                    }
                })
                if (binding.speedAnswerView.visibility == View.VISIBLE) {
                    val problems = content?.problems ?: return
                    val index = problems.indexOf(selectedProblem)

                    if (index > 0) {
                        binding.speedAnswerView.changedFocus(focusIndex = index, prevIndex = index+1)
                    }
                }
                binding.galleryView.prev()
                anim.start()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })
        anim.start()
    }

    private fun nextAnim() {
        // shortAnswer 체크 후 editField에 값이 있는데, answer 에 값이 없을 경우 입력
        checkShortAnswer()

        binding.problemContainer.setOnTouchListener(null)
        binding.solutionContainer.setOnTouchListener(null)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 100
        anim.addUpdateListener {
            var value = it.animatedValue as Float
            value = value.pow(2)
            binding.container.x = binding.problemContainer.measuredWidth * value * -1
            binding.container.alpha = 1 - value
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = 100
                anim.addUpdateListener {
                    var value = it.animatedValue as Float
                    value = value.pow(2)
                    binding.container.x = binding.problemContainer.width.toFloat() - binding.problemContainer.measuredWidth.toFloat() * value
                    binding.container.alpha = value
                }
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        binding.problemContainer.setOnTouchListener(problemGesture)
                        binding.solutionContainer.setOnTouchListener(solutionGesture)
                    }
                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationStart(p0: Animator) {}
                })
                anim.start()
                binding.galleryView.next()
                speedAnswerViewNext()
            }
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationStart(p0: Animator) {}

        })
        anim.start()
    }

    fun onGalleryBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "갤러리 아이콘", itemValue)
        binding.galleryBtn.visibility = View.GONE
        binding.answerView.clearFocusOnShortAnswer()

        val galleyParam = (binding.galleryView.layoutParams as LinearLayout.LayoutParams)
        val animator = ValueAnimator.ofInt(galleyParam.width, 0)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            binding.rootView.scrollTo(value, 0)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                binding.galleryCloser.visibility = View.VISIBLE
            }
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationStart(p0: Animator) {}
        })
        speedAnswerViewPrev()
        animator.duration = 150
        animator.start()

        val problem = selectedProblem
        if(problem != null) binding.galleryView.scrollTo(problem)
    }

    fun onClearBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "클리어 버튼", itemValue)
        val problem = selectedProblem ?: return
        setBtnSelected(binding.clearBtn, !binding.clearBtn.isSelected)
        ProblemManager.clear(this, user!!, problem, binding.clearBtn.isSelected, content is Test) {
            problem.isClear = binding.clearBtn.isSelected
            onProblemSelected(selectedProblem)
            binding.galleryView.update(problem)
            showClearToast(problem.isClear)
        }
    }

    fun onScrapBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "즐겨찾기 버튼", itemValue)
        val problem = selectedProblem ?: return
        setBtnSelected(binding.scrapBtn, !binding.scrapBtn.isSelected)
        ProblemManager.scrap(this, user!!, problem, binding.scrapBtn.isSelected) {
            selectedProblem?.isScrap = binding.scrapBtn.isSelected
            onProblemSelected(selectedProblem)
            binding.galleryView.update(problem)
            showScrapToast(problem.isScrap)
        }
    }

    fun onSirenBtnClicked() {
        if(selectedProblem != null) {
            val dialog = ProblemReportDialog(this, selectedProblem!!)
            dialog.listener = object : ProblemReportDialogListener {
                override fun onReportCompleted(problem: Problem) {
                    problem.problemErrorStatus = ProblemErrorStatus.REPORT
                    onProblemSelected(selectedProblem)
                    binding.galleryView.update(problem)
                    binding.speedAnswerView.updateAll()
                }
            }

            dialog.show()
        }
    }

    fun onShowSolutionCheckChanged(isChecked: Boolean) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "정답/해설표시토글", itemValue)
        if(isChecked) {
            binding.solutionContainer.visibility = View.VISIBLE
        } else {
            binding.solutionContainer.visibility = View.GONE
        }
    }

    fun onSpeedyScoringCheckChanged(isChecked: Boolean) {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "빠른채점토글", itemValue)
        if(binding.speedAnswerView.delegate == null)
            binding.speedAnswerView.delegate = this

        if(isChecked) {
            binding.speedAnswerView.visibility = View.VISIBLE
            binding.answerView.visibility = View.INVISIBLE
            binding.speedAnswerView.recyclerView.adapter?.notifyDataSetChanged()
            CoroutineScope(Dispatchers.Default).launch {
                delay(100)
                withContext(Dispatchers.Main) {
                    binding.speedAnswerView.scrollTo(selectedProblem,"onSpeedyScoringCheckChanged")
                }
            }

        } else {
            binding.speedAnswerView.visibility = View.GONE
            if(selectedProblem?.problemErrorStatus == ProblemErrorStatus.ERROR
                    || selectedProblem?.problemErrorStatus == ProblemErrorStatus.REPORT
                    || binding.emptyGuide.visibility == View.VISIBLE)
                binding.answerView.visibility = View.INVISIBLE
            else
                binding.answerView.visibility = View.VISIBLE
        }
    }

    private fun setBtnSelected(btn: ImageButton, isSelected: Boolean) {
        btn.isSelected = isSelected
        val selectedImage = if(btn === binding.scrapBtn)
            ContextCompat.getDrawable(this, R.drawable.ic_tag_14_selected)
        else if(btn == binding.clearBtn)
            ContextCompat.getDrawable(this, R.drawable.ic_check_purple_20)
        else
            null

        val unselectedImage = if(btn === binding.scrapBtn)
            ContextCompat.getDrawable(this, R.drawable.ic_tag_14_unselected)
        else if(btn == binding.clearBtn)
            ContextCompat.getDrawable(this, R.drawable.ic_check_grey_20)
        else
            null

        if(btn.isSelected) {
            btn.background = ContextCompat.getDrawable(this, R.drawable.bg_purple_ecebff_round)
            btn.setImageDrawable(selectedImage)
        } else {
            btn.background = ContextCompat.getDrawable(this, R.drawable.bg_grey_3d3d3d_stroke_black_4c4c4c_round)
            btn.setImageDrawable(unselectedImage)
        }
    }

    fun onAddSimilarBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "유사문제버튼", itemValue)
        val problem = selectedProblem ?: return

        if(content == null) return // 컨텐츠 안내려오는 경우 있음

//        Log.d("유사문제", "similar check===>${content!!.tempSimilarProblems}")


        problem.getSimilarProblem(this, user!!, content!!) {
            if(it == null) {
                showNotExistSimilarToast()
            } else {
                it.problemNum = problem.problemNum
                it.page = problem.page

                if(problem.rootProblem == null) {
                    it.rootProblem = problem
                    problem.similarProblems.add(it)
                } else {
                    it.rootProblem = problem.rootProblem
                    problem.rootProblem!!.similarProblems.add(it)
                }

                content!!.addSimilarProblem(it)
                binding.galleryView.add(it)
                binding.speedAnswerView.updateAll()
                showSimilarProblemAddedToast(problem)
            }
        }
    }

    fun onChangeSimilarBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "문제교체버튼", itemValue)
        val problem = selectedProblem ?: return

        if(content == null) return // 컨텐츠 안내려오는 경우 있음

        problem.getSimilarProblem(this, user!!, content!!) {
            if(it == null) {
                showNotExistSimilarToast()
            } else {
                it.problemNum = problem.problemNum
                it.page = problem.page

                val similarProblems: ArrayList<Problem>
                if(problem.rootProblem == null) {
                    it.rootProblem = problem
                    similarProblems = problem.similarProblems
                } else {
                    it.rootProblem = problem.rootProblem
                    similarProblems = problem.rootProblem!!.similarProblems
                }

                val index = similarProblems.indexOf(problem)
                if(index >= 0) { // 문제교체 버튼 빨리 클릭하면 죽는 케이스
                    similarProblems.set(index, it)
                    (problem.rootProblem ?: problem).similarProblems = similarProblems
                    answeredSet.remove(problem)

                    content!!.changeSimilarProblem(problem, it)
                    binding.galleryView.change(problem, it)
                    binding.speedAnswerView.updateAll()
                    showSimilarProblemAddedToast(problem)
                }
            }
        }
    }

    override fun onProblemSelected(problem: Problem?, autoFocus: Boolean) {
        saveMemo()
        selectedProblem = problem
        onSetProblem()
        if(problem != null) {
            binding.speedAnswerView.scrollTo(problem, "onProblemSelected")
            binding.galleryView.scrollTo(problem)
            if(selectedProblem?.getResultByScoring() == Result.yet && selectedProblem?.problemType == ProblemType.short && !binding.speedyScoreSwitch.isChecked && !binding.solutionSwitch.isChecked) { // 문제 안풀었고, 단답이고, 정답보기가off 이고, 빠른채점도 off이면 포커스
                Log.d("포커스", "autoFocus=$autoFocus, keyPad=${binding.answerView.keyPad}, isShow=${binding.answerView.keyPad?.isShowing}")
                Log.d("포커스", "galleryCloser.visibility=${binding.galleryCloser.visibility}")
//                answerView.keyPad?.dismiss()

                // 210513 채점버튼 관련 QA 수정사항으로 '문제 진입시 focus 해제'에 해당함
//                if(galleryCloser.visibility != View.VISIBLE) {
//                    answerView.postDelayed({ answerView.requestFocusOnShortAnswer() }, 100)
//                }
            } else {
                binding.answerView.clearFocusOnShortAnswer()
            }
        }
    }

    fun configureSimilarUI() {

        Log.d("유사문제", "=========================================")
        Log.d("유사문제", "isSimilarProblem=${selectedProblem?.isSimilarProblem()}")
        Log.d("유사문제", "parentProblemID=${selectedProblem?.parentProblemID}")
        Log.d("유사문제", "similarProblems=${selectedProblem?.similarProblems?.count()}")
        Log.d("유사문제", "userAnswer=${selectedProblem?.userAnswer}")
        Log.d("유사문제", "isAllAnswered=${selectedProblem?.isAllAnswered()}")
        Log.d("유사문제", "getResultByScoring=${selectedProblem?.getResultByScoring()}")

        if(selectedProblem?.isSimilarProblem() == true) { // 유사문제 이면
             if(selectedProblem?.userAnswer == null)  { // 답이 없으면 유사문제 가림
                 binding.changeSimilarProblemCl.visibility = View.VISIBLE
                 binding.addSimilarProblemCl.visibility = View.GONE
                 Tutor.showToolTipIfNeed(binding.changeSimilarProblemCl, Tutor.TooltipType.changeSimilar)
            } else {
                if(selectedProblem?.rootProblem?.isAllAnswered() == false) { // 답이 있으면 부모 문제가 가지고 있는 자식 문제가 모두 답을 했는지 체크
                    binding.changeSimilarProblemCl.visibility = View.GONE
                    binding.addSimilarProblemCl.visibility = View.GONE
                } else {
                    animAddSimilarShowing()
                }
            }
        } else {
            if(selectedProblem?.getResultByScoring() == Result.yet) {
                binding.addSimilarProblemCl.visibility = View.GONE
                binding.changeSimilarProblemCl.visibility = View.GONE
            } else {
                if(selectedProblem?.isAllAnswered() == false) {
                    binding.addSimilarProblemCl.visibility = View.GONE
                    binding.changeSimilarProblemCl.visibility = View.GONE
                } else {
                    animAddSimilarShowing()
                }
            }
        }
    }

    fun animAddSimilarShowing() {
        if(binding.addSimilarProblemCl.visibility == View.VISIBLE)
            return

        binding.addSimilarProblemCl.visibility = View.VISIBLE
        binding.changeSimilarProblemCl.visibility = View.GONE


        val anim = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.duration = 250
        binding.addSimilarProblemCl.startAnimation(anim)

        if(Tutor.TooltipType.addSimilar.isNeedToShow()) {
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Tutor.showToolTipIfNeed(binding.addSimilarProblemCl, Tutor.TooltipType.addSimilar)
                    }, 500)
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
        }
    }

    fun onSetProblem() {
        if(!binding.pencilcaseView.writeModeSwitch.isChecked)
            binding.pencilcaseView.setDefaultState()

        val problem = selectedProblem
        binding.titleTv.text = getTitleText()


        Log.d("테스트", "SolveActivity problem=${problem}")


        if(problem == null) {
            problemGesture?.init()
            solutionGesture?.init()
            binding.numTv.text = "-"
            binding.numExtTv.text = ""
            binding.totalCntTv.text = "/-"
        } else {
            problemGesture?.init()
            solutionGesture?.init()
            binding.problemIv.setProblemImageURL(problem.getProblemUrl())
            binding.solutionIv.setProblemImageURL(problem.getSolutionUrl())
            binding.answerTv.text = "정답 : ${problem.answerData}"

            if(problem.correctRate == null)
                binding.correctRateTv.text = "정답률 : -"
            else
                binding.correctRateTv.text = "정답률 : ${TextUtils.percentFormat.format(problem.correctRate)}"

            binding.lvTv.text = "난이도 : ${problem.getProblemLevel()}"
            binding.intentionTv.text = "${problem.unit}"
            binding.clearContainer.visibility = if(problem.isClear) View.VISIBLE else View.INVISIBLE
            setBtnSelected(binding.clearBtn, problem.isClear)
            binding.tag.visibility = if(problem.isScrap) View.VISIBLE else View.INVISIBLE
            setBtnSelected(binding.scrapBtn, problem.isScrap)

            binding.numTv.text = problem.getCurNumberText()
            binding.numExtTv.text = "${problem.getExtNumberText()}"

            if(content?.similarCount?:0 > 0)
                binding.totalCntTv.text = "/ ${content?.originCount} (+${content?.similarCount})"
            else
                binding.totalCntTv.text = "/ ${content?.originCount}"

            binding.problemMemoView.load("${problem.id}_${content?.assignID ?: 0}_p")
            binding.solutionMemoView.load("${problem.id}_${content?.assignID ?: 0}_s")

            if(binding.galleryCloser.visibility != View.VISIBLE) {
                var requestFocus = if(binding.speedAnswerView.visibility == View.GONE) binding.galleryCloser.visibility != View.VISIBLE else false
                binding.answerView.configureUI(problem, requestFocus)
                binding.answerView.visibility = getAnswerViewVisibility(problem)
            }

            when(problem.problemErrorStatus) {
                ProblemErrorStatus.NONE -> {
                    binding.statusContainer.visibility = View.GONE
                }
                ProblemErrorStatus.REPORT -> {
                    binding.statusContainer.visibility = View.VISIBLE
                    binding.statusTv.text = "신고 처리 중입니다.\n" +
                            "빠른 시일 내에 처리하겠습니다 :)"
                    binding.statusIcon.setImageResource(R.drawable.ic_siren_w28)
                    binding.statusContainer.setBackgroundColor(Color.parseColor("#80818181"))
                }
                ProblemErrorStatus.ERROR -> {
                    binding.statusContainer.visibility = View.VISIBLE
                    binding.statusTv.text = "오류로 삭제된 문제입니다.\n" +
                            "이용에 불편을 드려 죄송합니다."
                    binding.statusIcon.setImageResource(R.drawable.ic_error_white_w21)
                    binding.statusContainer.setBackgroundColor(Color.parseColor("#818181"))
                }
            }
            if(problem.getResultByScoring() == Result.yet) {
                binding.clearBtn.visibility = View.GONE
            } else {
                binding.clearBtn.visibility = View.VISIBLE
            }

            configureSimilarUI()
        }
    }

    private fun getTitleText(): String {
        return when(content) {
            is Book -> {
                val book = (content as Book)
                var title = "${book.bookName} / ${book.subject}"
                if (book.chapter?.isNotEmpty() == true) {
                    title += " / ${book.chapter}"
                }
                title
            }
            is Test, is Piece -> {
                "${content!!.subject}"
            }
            is MockExam -> {
                val mock = (content as MockExam)
                mock.getMockTitle()
            }
            else -> {
                ""
            }
        }
    }

    override fun onEditTypeChanged(type: Pencilcase.EditType?) {
        with(binding) {
            if(type == null) {
                problemContainer.isBlock = false
                solutionContainer.isBlock = false
            } else {
                problemContainer.isBlock = true
                solutionContainer.isBlock = true
            }

            if(type == Pencilcase.EditType.pencil)
                Tutor.showToolTipIfNeed(pencilcaseView.pencilBtn, Tutor.TooltipType.takeNoteScroll)
        }

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

    override fun onItemChanged(set: ObservableHashSet<Problem>) {
        with(binding) {
            if(set.isEmpty()) {
                answerView.disableMarking()
                speedAnswerView.disableMarking()
            } else {
                answerView.enableMarking(set.size)
                speedAnswerView.enableMarking(set.size)
            }
        }
    }

    private fun saveMemo() {
        with(binding) {
            val problem = selectedProblem ?: return
            problemMemoView.save("${problem.id}_${content?.assignID ?: 0}_p")
            solutionMemoView.save("${problem.id}_${content?.assignID ?: 0}_s")
        }
    }

    fun showNotExistSimilarToast() {
        val text = "해당 유형의 문제를 다 풀었습니다."
        DaebakToast.show(this, text)
    }

    private fun showSimilarProblemAddedToast(problem: Problem) {
        val problemNum = problem.problemNum.toString() + "번"

        DaebakToast.show(this, "'${problemNum}'문제의 유사문제가 추가되었습니다.")
    }

    fun showClearToast(isClear: Boolean) {
        val text = if(isClear) "클리어! 해당 문제를 끝냈습니다." else "클리어 해제했습니다."
        DaebakToast.show(this, text)
    }

    fun showScrapToast(isScrap: Boolean) {
        val text = if(isScrap) "즐겨찾기 추가했습니다." else "즐겨찾기 해제했습니다."
        DaebakToast.show(this, text)
    }
    private fun getAnswerViewVisibility(problem: Problem?): Int {
        if (problem == null) return View.INVISIBLE

        return when (problem.problemErrorStatus) {
            ProblemErrorStatus.ERROR, ProblemErrorStatus.REPORT -> {
                View.INVISIBLE
            }
            else -> {
                if(binding.speedAnswerView.visibility == View.VISIBLE || binding.emptyGuide.visibility == View.VISIBLE)
                    View.INVISIBLE
                else
                    View.VISIBLE
            }
        }
    }

    fun onSpyBtnClikcked() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("SPY")
        val items = mutableListOf(
                "컨닝",
                "3번으로 찍기",
                "문제상태: 정상",
                "문제상태: 신고",
                "문제상태: 숨겨짐"
        )

        if(content is MockExam) {
            items.add("타이머 세팅: 5분전")
            items.add("타이머 세팅: 제출직전")
        }
        with(binding) {
            builder.setItems(items.toTypedArray()) { dialog, position ->
                when(position) {
                    0 -> {
                        content?.let { content ->
                            answeredSet.addAll(content.problems)
                            content.problems.forEach { it.userAnswer = it.answerData }
                            binding.galleryView.updateAll()
                            speedAnswerView.updateAll()
                            onProblemSelected(selectedProblem)
                        }
                    }
                    1 -> {
                        content?.let { content ->
                            answeredSet.addAll(content.problems)
                            content.problems.forEach { it.userAnswer = "3" }
                            galleryView.updateAll()
                            speedAnswerView.updateAll()
                            onProblemSelected(selectedProblem)
                        }
                    }
                    2 -> {
                        selectedProblem?.problemErrorStatus = ProblemErrorStatus.NONE
                        galleryView.updateAll()
                        speedAnswerView.updateAll()
                        onProblemSelected(selectedProblem)
                    }
                    3 -> {
                        selectedProblem?.problemErrorStatus = ProblemErrorStatus.REPORT
                        galleryView.updateAll()
                        speedAnswerView.updateAll()
                        onProblemSelected(selectedProblem)
                    }
                    4 -> {
                        selectedProblem?.problemErrorStatus = ProblemErrorStatus.ERROR
                        galleryView.updateAll()
                        speedAnswerView.updateAll()
                        onProblemSelected(selectedProblem)
                    }
                    5 -> {
                        timerView?.elapsedTime = 5695
                    }
                    6 -> {
                        timerView?.elapsedTime = 5995
                    }
                }
            }

            builder.show()
        }
    }

    private fun highlightGalleryBtnIfNeed() {
        if(Preferences.galleryClickCnt.get() > 0)
            return
        else {
            val rotateAnim = ObjectAnimator.ofFloat(binding.galleryBtn, "rotation", 0f, 5f, 0f, -5f, 0f)
            rotateAnim.setRepeatCount(20)
            rotateAnim.setDuration(200)
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
                Preferences.galleryClickCnt.set(Preferences.galleryClickCnt.get() + 1)
                rotateAnim.cancel()
                rotateAnim.repeatCount = 0

                rotateAnim.removeAllListeners()
                rotateAnim.removeAllUpdateListeners()
                binding.galleryBtn.cancelAnimation()
                binding.galleryBtn.frame = 0
                onGalleryBtnClicked()
            }
        }
    }

    override fun monitoringTick() {
        var sec = AppUsageMonitor.accumulatedStudyTime
        val min = sec / 60
        sec = sec % 60

        runOnUiThread {
            binding.spyBtn.text =  String.format("%02d", min) + ":" + String.format("%02d", sec)
        }


//        Log.d("MONITOR", "[SOLVE] TICK - ${AppUsageMonitor.accumulatedStudyTime}")
    }
    private fun getMockWithOptionalSubjects(content: Content, cb: (summary: MockExam) -> Unit) {
        val mock = MockExam(content)
        MockExamManager.getMockSummary(this, content.mockID, user!!) { mockExamSummery ->
            val optionResult = mutableListOf<CommercialSubject>()
            mockExamSummery?.let {
                val optionalSubjects = mockExamSummery.optionalSubjectSummary

                for(subject in optionalSubjects?: arrayOf()) {
                    if (subject.isSelected) {
                        optionResult.add(CommercialSubject.valueOf(subject.subjectCodeType))
                    }
                }
                mock.selectOptional = optionResult
                mock.examType = mockExamSummery.examType.let {
                    MockExam.ExamType.valueOnString(it)
                }
                mock.grade = mockExamSummery.grade
            }

            cb(mock)
        }
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
