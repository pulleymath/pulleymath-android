package com.freewheelin.pulley.dialogs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.utils.*
import kotlinx.android.synthetic.main.dialog_note_detail.*
import kotlinx.coroutines.*


class NoteDetailDialog: Dialog {
    var isClear: Boolean = false
        set(value) {
            field  = value

            if(value) {
                clearHiderCl.visibility = View.VISIBLE
                clearBtn.setBackgroundResource(R.drawable.bg_purple_ecebff_stroke_purple_acacff_round_18)
                clearBtn.setImageResource(R.drawable.ic_check_purple_engrave)
            } else {
                clearHiderCl.visibility = View.GONE
                clearBtn.setBackgroundResource(R.drawable.bg_white_fafafa_stroke_grey_e8e8e8_round_18)
                clearBtn.setImageResource(R.drawable.ic_check_grey_circle)
            }
        }

    var isScrap: Boolean = false
        set(value) {
            field = value

            if(value) {
                scrapBtn.setBackgroundResource(R.drawable.bg_purple_ecebff_stroke_purple_acacff_round_18)
                scrapBtn.setImageResource(R.drawable.ic_tag_purple)
                tagIv.visibility = View.VISIBLE
            } else {
                scrapBtn.setBackgroundResource(R.drawable.bg_white_fafafa_stroke_grey_e8e8e8_round_18)
                scrapBtn.setImageResource(R.drawable.ic_tag_grey)
                tagIv.visibility = View.GONE
            }
        }

    lateinit var user: User
    var startAnimator: ValueAnimator? = null
    var hideAnimator: ValueAnimator? = null

    lateinit var problem: Problem
    var nextProblem: Problem? = null
        set(value) {
            field = value

            if(value != null)
                rightArrowIb.setColorFilter(ContextCompat.getColor(context, R.color.white_ffffff))
            else
                rightArrowIb.setColorFilter(Color.parseColor("#40ffffff"))

            rightArrowIb.isEnabled = value != null
        }
    var prevProblem: Problem? = null
        set(value) {
            field = value

            if(value != null)
                leftArrowIb.setColorFilter(ContextCompat.getColor(context, R.color.white_ffffff))
            else
                leftArrowIb.setColorFilter(Color.parseColor("#40ffffff"))

            leftArrowIb.isEnabled = value != null
        }

    constructor(context: Context, problem: Problem, user: User): super(context) {
        this.user = user
        setContentView(R.layout.dialog_note_detail)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        historyTv.movementMethod = ScrollingMovementMethod()

        xBtn.setOnClickListener { dismiss() }

        clearBtn.setOnClickListener {
            isClear = !isClear
            problem.isClear = isClear
            if(isClear)
                showToast("클리어! 해당 문제를 끝냈습니다.")
            else
                showToast("클리어 해제했습니다.")
        }

        scrapBtn.setOnClickListener {
            isScrap = !isScrap
            problem.isScrap = isScrap
            if(isScrap)
                showToast("즐겨찾기 추가했습니다.")
            else
                showToast("즐겨찾기 해제했습니다.")
        }
        configureUI(problem)
    }

    fun configureUI(problem: Problem) {
        this.problem = problem
        isClear = problem.isClear
        isScrap = problem.isScrap
//        problemSdv.setImageURI(problem.getProblemUrl())
//        Picasso.get().load(problem.getProblemUrl()).into(problemSdv)
        CoroutineScope(Dispatchers.IO).launch {
            val problemImage = GlideApp.with(problemSdv).asBitmap().load(problem.getProblemUrl()).submit().get()
            withContext(Dispatchers.Main) {
                problemSdv.setImageBitmap(problemImage)
            }
        }

        if(problem.getResultByScoring() == Result.correct) {
            resultIv.setImageResource(R.drawable.ic_result_correct)
        } else {
            resultIv.setImageResource(R.drawable.ic_result_incorrect)
        }
        levelTv.text = "난이도 : ${problem.getProblemLevel()}"

        ProblemManager.getDetailInfo(context, user, this.problem) {responseProblem, detail, history ->
            if(problem == responseProblem) {
                if(detail != null)  {
                    intentionTv.visibility = View.VISIBLE
                    val unitInfoText = "과목명 : ${this.problem.getSubject().filterText}" +
                            "\n대단원 : ${detail.chapterBig}" +
                            "\n중단원 : ${detail.chapterMiddle}" +
                            "\n소단원 : ${detail.chapterLittle}" +
                            "\n유형명 : ${detail.unitName}"
                    intentionTv.text = unitInfoText
                } else {
                    intentionTv.visibility = View.GONE
                }

                val historyList = history.filter { it.result == -2 }.map {
                    Pair(it.solveDateTime, DateTimeUtils.yyyy_MM_dd.format(it.solveDateTime) +  " : " + "오답노트에 저장되었습니다. (학습경로 : ${it.subjectTag})")
                }.toMutableList()

                val clearDate = responseProblem.clearDateTime
                if(responseProblem.isClear == true && clearDate != null) {
                    historyList.add(Pair(clearDate, DateTimeUtils.yyyy_MM_dd.format(clearDate) + " : " + "클리어 문항으로 지정되었습니다."))
                }

                val scrapDate = responseProblem.scrapDateTime
                if(responseProblem.isScrap == true && scrapDate != null) {
                    historyList.add(Pair(scrapDate, DateTimeUtils.yyyy_MM_dd.format(scrapDate) + " : " + "즐겨찾기 지정되었습니다."))
                }

                historyList.sortBy { it.first }

                if(historyList.isEmpty()) {
                    historyTv.visibility = View.GONE
//                    LogUtils.assert(false, "NoteDetail no history ${user.studentID}, ${problem.id}")
                    LogUtils.errorEvent(PulleyEvent.ERROR, user, "NoteDetail no history ${user.studentID}")
                } else {
                    historyTv.visibility = View.VISIBLE
                    historyTv.text = historyList.let {
                        var text = ""
                        for(history in it) {
                            text = text + "\n${history.second}"
                        }
                        text
                    }
                }


                categoryTv?.apply {
                    if(history.first().subjectTag.isEmpty()) {
                        this.visibility = View.INVISIBLE
                    } else {
                        this.visibility = View.VISIBLE
                        this.text = history.first().subjectTag
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {

        startAnimator?.cancel()
        hideAnimator?.cancel()
        toastView.visibility = View.VISIBLE
        toastView.text = text
        toastView.measure(0,0)
        val originX = (toastView.measuredWidth + 24f.toPx())  * -1
        startAnimator = ValueAnimator.ofFloat(originX, 0f)
        startAnimator!!.addUpdateListener {
            val value = it.animatedValue as Float
            toastView.translationX = value
        }

        startAnimator!!.duration = 300
        startAnimator!!.interpolator = AccelerateInterpolator(1.5f)
        startAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                hideToast(3000)
            }
        })
        startAnimator!!.start()
    }

    private fun hideToast(delay: Long = 0) {
        val originX = (toastView.measuredWidth + 24f.toPx())  * -1
        hideAnimator = ValueAnimator.ofFloat(0f, originX)
        hideAnimator!!.addUpdateListener {
            val value = it.animatedValue as Float
            toastView.translationX = value
        }

        hideAnimator!!.duration = 300
        hideAnimator!!.interpolator = AccelerateInterpolator(1.5f)
        hideAnimator!!.startDelay = delay
        hideAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                toastView.visibility = View.GONE
            }
        })
        hideAnimator!!.start()
    }

}