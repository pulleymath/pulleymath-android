package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ItemOmrAnswerBinding
import com.freewheelin.pulley.databinding.ItemOmrShortAnswerBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.ProblemType
import java.lang.ref.WeakReference


interface OMRViewListener {
    fun onShortAnswerFocusChanged(editText:EditText, type:OMRView.OMRViewType, hasFocus: Boolean, isLast: Boolean)
    fun onAnswerChanged(answers: ArrayList<Int?>)
}

class OMRView: RecyclerView {
     companion object {
         const val SINGLE_ANSWER: Int = 0
         const val SHORT_ANSWER: Int = 1
         const val SPAN_SIZE: Int = 10
         const val MARGIN_SPAN: Int = 120
         const val SINGLE_ITEM_SPAN = 520
    }

    var singleCount = 0
    var shortCount = 0
    var listener: WeakReference<OMRViewListener>? = null

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        getAttrs(attrs)
        overScrollMode = View.OVER_SCROLL_NEVER
//        adapter = OMRAdapter(context, singleCount, shortCount)

    }

    private fun getAttrs(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.OMRView)
        setTypedArray(array)
    }

    private fun setTypedArray(array: TypedArray) {
        singleCount = array.getInt(R.styleable.OMRView_singleCount, 0)
        shortCount = array.getInt(R.styleable.OMRView_shortCount, 0)
        array.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val clipPath = Path()
        clipPath.addRoundRect(RectF(canvas.clipBounds), 13f, 13f, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }

    fun setOMRViewListener(listener: OMRViewListener) {
        this.listener = WeakReference(listener)
        (this.adapter as? OMRAdapter)?.setOMRViewListener(listener)
    }

    fun getAnswers(): ArrayList<Int?>{
        val answers = (adapter as? OMRAdapter)?.getAnswers() ?: return ArrayList()
        return answers
    }

    fun setGridLayoutManager() {
        this.layoutManager = GridLayoutManager(context, SPAN_SIZE, GridLayoutManager.HORIZONTAL, false).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {

                    if (position == singleCount - 1 && shortCount != 0 && omrViewType == OMRViewType.LEFT) {
                        return OMRView.SPAN_SIZE - (SPAN_SIZE / 10 * (position % 10))
                    }

                    if (position < singleCount || omrViewType == OMRViewType.RIGHT) {
                        return SPAN_SIZE / 10

                    } else {
                        return OMRView.SPAN_SIZE / 10
                    }
                }
            }
        }
    }

    lateinit var omrViewType: OMRViewType
    fun setAnswer(problems: List<Problem>, type: OMRViewType) {

        val shortSize = problems.filter { it.problemType == ProblemType.short }.size
        shortCount = shortSize
        singleCount = problems.size - shortSize
        omrViewType = type
        val omrAnswers = problems.mapIndexed { index, problem -> OMRAnswer(problem.problemNum ?: index+1, problem.userAnswer?.toIntOrNull()) }

        val adapter = OMRAdapter(context, omrAnswers, singleCount, shortCount, type)
        this.adapter = adapter
        setGridLayoutManager()

    }

    fun focusOnNext() {
        val selectedAnswer = (adapter as? OMRAdapter)?.selectedOMRAnswer ?: return
        val position = (adapter as OMRAdapter).omrAnswers.indexOf(selectedAnswer)
        val shortAnswerHolder = findViewHolderForAdapterPosition(position + 1) as? OMRShortAnswer ?: return
        shortAnswerHolder.answerEt.requestFocus()
    }

    fun addTextOnFocusedAnswer(text: String) {
        val selectedAnswer = (adapter as? OMRAdapter)?.selectedOMRAnswer ?: return
        val shortAnswerHolder = getSelectedHolder() ?: return

        shortAnswerHolder.answerEt.append(text)
        selectedAnswer.answer = shortAnswerHolder.answerEt.text.toString().toIntOrNull()
    }

    fun deleteLastTextOnFocusedAnswer() {
        val selectedAnswer = (adapter as? OMRAdapter)?.selectedOMRAnswer ?: return
        val shortAnswerHolder = getSelectedHolder() ?: return

        val length = shortAnswerHolder.answerEt.text.length
        if (length > 0)
            shortAnswerHolder.answerEt.text.delete(length - 1, length)

        if (length == 0 || length == 1)
            selectedAnswer.answer = null
        else {
            try {
                selectedAnswer.answer = shortAnswerHolder.answerEt.text.toString().toInt()
            } catch(e:Exception) {
                selectedAnswer.answer = null
            }
        }
    }

    private fun getSelectedHolder(): OMRShortAnswer? {
        val selectedAnswer = (adapter as? OMRAdapter)?.selectedOMRAnswer ?: return null
        val position = (adapter as OMRAdapter).omrAnswers.indexOf(selectedAnswer)
        return findViewHolderForAdapterPosition(position) as? OMRShortAnswer ?: return null
    }

    enum class OMRViewType {
        LEFT, RIGHT
    }
}

class OMRAdapter(val context: Context,
                 val omrAnswers: List<OMRAnswer>,
                 val singleCount: Int,
                 val shortCount: Int,
                 val type: OMRView.OMRViewType): RecyclerView.Adapter<OMRAnswerView>(), OMRAnswerListener {

    var height: Int? = null
    var listener: WeakReference<OMRViewListener>? = null
    var selectedOMRAnswer: OMRAnswer? = null

    override fun onBindViewHolder(holder: OMRAnswerView, _position: Int) {
        val position = holder.bindingAdapterPosition
        val isLast = (position == singleCount - 1 && type == OMRView.OMRViewType.LEFT) || (position == singleCount + shortCount - 1)

        (holder as? OMRShortAnswer)?.apply {
            holder.setOnFocusChangeListener(View.OnFocusChangeListener { view, hasFocus ->
                this@OMRAdapter.selectedOMRAnswer = holder.answer
                val isLast = (selectedOMRAnswer == omrAnswers?.last())
                this@OMRAdapter.listener?.get()?.onShortAnswerFocusChanged(this.answerEt, type, hasFocus, isLast)
            })
        }

        if(position < singleCount) {
            val answer = omrAnswers[position]
            answer.listener = WeakReference(this)
            holder.set(answer, isLast)
            if (position == singleCount - 1) {
                height?.let { holder.itemView.layoutParams.height = it / 10 }

            }
        } else if (position >= singleCount) {
            val answer = omrAnswers[position]
            answer.listener = WeakReference(this)
            holder.set(answer, isLast)
            val visibility = if (type == OMRView.OMRViewType.RIGHT && singleCount != 0 && position == singleCount) View.VISIBLE else View.GONE
            holder.setTopHorizontalBorderVisibility(visibility)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OMRAnswerView {
        height = parent.measuredHeight

        return when (viewType) {
            OMRView.SINGLE_ANSWER -> {
                val itemBinding: ItemOmrAnswerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_omr_answer, parent, false)
                val holder = OMRSingleAnswerView(itemBinding)
                holder.itemView.layoutParams.width = if (type == OMRView.OMRViewType.RIGHT) parent.measuredWidth else parent.measuredWidth / 3
                return holder
            }
            OMRView.SHORT_ANSWER -> {
                val itemBinding: ItemOmrShortAnswerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_omr_short_answer, parent, false)
                val holder = OMRShortAnswer(itemBinding)
                holder.itemView.layoutParams.width = if (type == OMRView.OMRViewType.RIGHT) parent.measuredWidth else parent.measuredWidth / 3
                return holder
            }
            else -> {
                val itemBinding: ItemOmrShortAnswerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_omr_short_answer, parent, false)
                val holder = OMRShortAnswer(itemBinding)
                holder.itemView.layoutParams.width = 0
                return holder
            }
        }
    }

    override fun getItemCount(): Int {
        return omrAnswers.size
    }

    override fun getItemViewType(position: Int): Int {

        return when {
            position < singleCount -> OMRView.SINGLE_ANSWER
            position >= singleCount -> OMRView.SHORT_ANSWER
            else -> -1
        }
    }

    override fun onAnswerChanged(answer: Int?) {
        listener?.get()?.onAnswerChanged(getAnswers())
    }

    fun getAnswers(): ArrayList<Int?> {
        var answers = ArrayList<Int?>()

        for (answer in omrAnswers?: ArrayList()) {
            answers.add(answer.answer)
        }

        return answers
    }

    fun setOMRViewListener(listener: OMRViewListener) {
        this.listener = WeakReference(listener)
    }
}

abstract class OMRAnswerView (view: View): RecyclerView.ViewHolder(view) {
    abstract var answer: OMRAnswer?
    abstract fun set(omrAnswer: OMRAnswer, isLast: Boolean)
    abstract fun setTopHorizontalBorderVisibility(view: Int)
}

class OMRSingleAnswerView (itemBinding: ItemOmrAnswerBinding) : OMRAnswerView(itemBinding.root), AnswerSelectionListener {
    override var answer: OMRAnswer? = null

    var numberTv = itemBinding.numberTv
    var horizontalBorder = itemBinding.horizontalBorder
    var answerSelectionView = itemBinding.answerSelectionView

    override fun set(omrAnswer: OMRAnswer, isLast: Boolean) {
        this.answer = omrAnswer
        numberTv.text = (omrAnswer.problemNum).toString()
        answerSelectionView.listener = this

        var hashSet: HashSet<String> = HashSet()

        when(omrAnswer.answer) {
            1 -> hashSet.add("1")
            2 -> hashSet.add("2")
            3 -> hashSet.add("3")
            4 -> hashSet.add("4")
            5 -> hashSet.add("5")
        }
        answerSelectionView.answer = hashSet

        horizontalBorder.visibility = if ((bindingAdapterPosition + 1) % 10 == 5 || isLast) View.VISIBLE else View.INVISIBLE
    }

    override fun onAnswerChanged(view: AnswerSelectionView, answerStr: String?) {
        val answer = view.answer
        if(answer.isEmpty()) {
            this.answer?.answer = null
        } else {
            this.answer?.answer = answer.first().toInt()
        }
    }

    override fun setTopHorizontalBorderVisibility(view: Int) {
    }
}

class OMRShortAnswer(val itemBinding: ItemOmrShortAnswerBinding) : OMRAnswerView(itemBinding.root) {
    override var answer: OMRAnswer? = null
    var answerEt = itemBinding.answerEt
    var numberTv = itemBinding.numberTv
    var horizontalBorder = itemBinding.horizontalBorder
    var topHorizontalBorder = itemBinding.topHorizontalBorder

    var listener: View.OnFocusChangeListener? = null

    override fun set(omrAnswer: OMRAnswer, isLast: Boolean) {
        this.answer = omrAnswer
        numberTv.text = (omrAnswer.problemNum).toString()

        horizontalBorder.visibility = if ((bindingAdapterPosition + 1) % 10 == 5 || isLast) View.VISIBLE else View.INVISIBLE

        answerEt.run {
            setText(omrAnswer.answer?.toString())
            setTextIsSelectable(true)
            showSoftInputOnFocus = false
        }
        answerEt.setOnFocusChangeListener{ _, hasFocus ->
            listener?.onFocusChange(answerEt, hasFocus)
        }

        answerEt.setOnTouchListener{ v, event ->
            v.onTouchEvent(event)
            true
        }

        answerEt.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        }

    }

    fun setOnFocusChangeListener(listener: View.OnFocusChangeListener) {
        this.listener = listener
    }
    override fun setTopHorizontalBorderVisibility(view: Int) {
        topHorizontalBorder.visibility = view
    }
}

interface OMRAnswerListener {
    fun onAnswerChanged(answer: Int?)
}
class OMRAnswer {
    var problemNum: Int = 0
    var answer: Int? = null
        set(value) {
            field = value
            listener?.get()?.onAnswerChanged(answer)
        }

    var listener: WeakReference<OMRAnswerListener>? = null

    constructor(problemNum: Int, answer: Int?) {
        this.problemNum = problemNum
        this.answer = answer
    }

}