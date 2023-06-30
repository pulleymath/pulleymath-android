package com.freewheelin.pulley.revision2021.views

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewFloatingAnswerSheetBinding
import com.freewheelin.pulley.revision2021.activity.base.CustomBaseView
import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.pattern.AnswerShadowBuilder
import com.freewheelin.pulley.revision2021.model.LCPatternQuiz
import com.freewheelin.pulley.revision2021.model.QuizFormat
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.legacy.views.AnswerSelectionListener
import com.freewheelin.pulley.legacy.views.AnswerSelectionView

interface FloatingAnswerDelegate {
    fun onAnswerChanged(view: View, answer: String?)
    fun onShortAnswerChanged(answer: String?)
}

class FloatingAnswerSheet: CustomBaseView, AnswerSelectionListener {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    companion object {
        var x: Float? = null
        var y: Float? = null
    }
    var delegate: FloatingAnswerDelegate? = null

    lateinit var binding: ViewFloatingAnswerSheetBinding


    override fun onViewCreated() {
        binding = ViewFloatingAnswerSheetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun doOnAttached() {
        binding.apply {
            selectionAnswerView.listener = this@FloatingAnswerSheet

        }

        background = ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_32)
        setToucnEvent()
    }

    fun configureUI(quiz: LCPatternQuiz) {
        binding.apply {
            when (quiz.quizFormat) {
                QuizFormat.Single, QuizFormat.Multi -> {
                    selectionAnswerView.setQuizFormat(quiz.quizFormat)
                }
                QuizFormat.Short -> {
                    shortAnswerView.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(var1: CharSequence?, var2: Int,var3: Int, var4: Int) {}
                        override fun afterTextChanged(var1: Editable?) {}

                        override fun onTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int) {
                            delegate?.onShortAnswerChanged(var1.toString())
                        }
                    })
                }
            }
        }
    }
    fun coufigureUIOnNote(note: LCWrongNoteMapCard) {
        binding.apply {
            when (note.quizFormat) {
                QuizFormat.Single, QuizFormat.Multi -> {
                    selectionAnswerView.setQuizFormat(note.quizFormat)
                }
                QuizFormat.Short -> {
                    shortAnswerView.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(var1: CharSequence?, var2: Int,var3: Int, var4: Int) {}
                        override fun afterTextChanged(var1: Editable?) {}

                        override fun onTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int) {
                            delegate?.onShortAnswerChanged(var1.toString())
                        }
                    })
                }
            }
        }
    }


    override fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?) {
        delegate?.onAnswerChanged(this, answerStr)
    }
    override fun doOnDetached() {}

    fun setInitPosition() {
        val locationStr = Preferences.floatingAnswerSheetLastLocation.get()
        locationStr.split("&&").let {
            if (it.size > 1) {
                val x = it[0].toFloat()
                val y = it[1].toFloat()
                this.x = x
                this.y = y
                this.visibility = View.VISIBLE
            }
        }

    }
    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        FloatingAnswerSheet.x = x
        FloatingAnswerSheet.y = y
    }
    fun setToucnEvent() {
        binding.dragIv.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("emform, dragIv Down")

                    val data = ClipData.newPlainText("", "")
                    val shadowBuilder = AnswerShadowBuilder(this)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        this.startDragAndDrop(data, shadowBuilder, view, 0)
                    } else {
                        this.startDrag(data, shadowBuilder, view, 0)
                    }
                    this.visibility = View.INVISIBLE
                    true
                }
                MotionEvent.ACTION_UP -> {
                    println("emform, dragIv up")
                    this.visibility = View.VISIBLE
                    false
                }
                else -> {
                    false
                }
            }
        }
    }
}