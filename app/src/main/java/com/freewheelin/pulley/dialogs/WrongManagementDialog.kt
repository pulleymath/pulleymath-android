package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.analysis.tabFragment.ChapterTreeList
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.model.ChapterAnalysis
import com.freewheelin.pulley.utils.extensionTouchArea
import com.freewheelin.pulley.utils.showBalloon
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.dialog_wrong_management.*

class WrongManagementDialog : Dialog {

    constructor(context: Context, type: Type): super(context) {

        this.pieceProblemType = PieceProblemType.custom
        this.type = type

        if(type == Type.wrongProblem) {
            hideClearSection()
        }
    }

    enum class Level(val text: String) {
        easier("EASIER"),
        normal("NORMAL"),
        harder("HARDER");

        val eventValue: String
        get() {
            return when(this) {
                easier -> "더쉽게"
                normal -> "그대로"
                harder -> "더어렵게"
            }
        }
    }

    enum class PieceProblemType {
        origin,
        custom
    }

    enum class Type(val text: String) {
        wrongProblem("오답"),
        wrongPiece("오답"),
        scrap("즐겨찾기")
    }

    var cnt = 2
    var type: Type
        set(value) {
            field = value
            configureUIByType()
        }
    var wrongCnt = 0
        set(value) {
            field = value
            this.wrongCntTv.text = "${type.text}문제 ${value}개로 학습지를 만듭니다."
        }

    var level: Level? = Level.normal
    var pieceProblemType: PieceProblemType = PieceProblemType.origin
        set(value) {
            field = value

            when (value) {
                PieceProblemType.origin -> disableSecondThirdSection()
                PieceProblemType.custom -> enableSecondThirdSection()
            }
        }

    var repeatUpdateHandler = Handler(Looper.getMainLooper())
    var autoIncrement = false
    var autoDecrement = false
    var isClearInclude = false


    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_wrong_management)
        cntTv.text = cnt.toString()
        plusBtn.setOnClickListener {
            increment()
        }

        minusBtn.setOnClickListener {
            decrement()
        }

        typeRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.originRadioBtn -> pieceProblemType = PieceProblemType.origin
                R.id.customRadioBtn -> pieceProblemType = PieceProblemType.custom
            }
        }

        levelRg.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.easyRb -> level = Level.easier
                R.id.originRb -> level = Level.normal
                R.id.hardRb -> level = Level.harder
            }
        }

        clearRg.setOnCheckedChangeListener { _, id ->
            when(id) {
                R.id.includeRb -> isClearInclude = true
                R.id.excludeRb -> isClearInclude = false
            }
        }

        plusBtn.setOnLongClickListener {
            autoIncrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        plusBtn.setOnTouchListener { view, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoIncrement) {
                autoIncrement = false
            }
            false
        }

        minusBtn.setOnLongClickListener {
            autoDecrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        minusBtn.setOnTouchListener{ _, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoDecrement) {
                autoDecrement = false
            }
            false
        }
        levelQuestionIb.extensionTouchArea(8.toPx())
        levelQuestionIb.setOnClickListener {
            it.showBalloon("배점과 정답률을 고려하여 측정한\n문항의 수준 정보입니다.")
        }
    }

    private fun disableSecondThirdSection() {
        firstHider.visibility = View.VISIBLE
        secondHider.visibility = View.VISIBLE
        clearHider.visibility = View.VISIBLE
    }

    private fun enableSecondThirdSection() {
        firstHider.visibility = View.GONE
        secondHider.visibility = View.GONE
        clearHider.visibility = View.GONE
    }

    private fun hideClearSection() {
        clearSection.visibility = View.GONE
    }

    inner class RptUpdater : Runnable {
        private var delay: Long

        constructor(delay: Long=300): super() {
            this.delay = delay
        }

        override fun run() {
            val postDelay = if (delay < 0)  50 else delay
            if (autoIncrement) {
                increment()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            } else if (autoDecrement) {
                decrement()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            }
        }
    }

    fun increment() {
        if (cnt < 5)
            cnt += 1
        cntTv.text = cnt.toString()
    }

    fun decrement() {
        if (cnt > 1)
            cnt -= 1
        cntTv.text = cnt.toString()
    }

    fun configureUIByType() {
        customRadioBtn.text = "${type.text} 유사문제로 만들기"
        originRadioBtn.text = "${type.text} 그대로 만들기"

        cntGuideTv.text = "${type.text} 하나당"
        makeBtn.text = "${type.text} 학습지 만들기"

        if(type != Type.wrongPiece)
            testTitleTv.text = ""
    }

    fun configureUIByChapter(chapters: ObservableHashSet<ChapterAnalysis>) {
        val problemCount = chapters.sumBy { it.problemTotalNumber }

        if(chapters.size == 1)
            testTitleTv.text = "${chapters.first().name}"
        else
            testTitleTv.text = "'${chapters.first().name}' 외 ${chapters.size - 1}건"

        wrongCntTv.text = "선택한 단원의 문제 ${problemCount}개로 학습지를 만듭니다."

        customRadioBtn.text = "선택문제와 유사한 문제로 만들기"
        originRadioBtn.text = "선택문제 그대로 만들기"
        cntGuideTv.text = "선택문제 하나당"
        makeBtn.text = "단원 학습지 만들기"


    }
}