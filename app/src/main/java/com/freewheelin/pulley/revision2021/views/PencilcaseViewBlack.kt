package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.legacy.utils.DelayDebounce
import com.freewheelin.pulley.legacy.utils.setMarginStart
import com.freewheelin.pulley.revision2021.cookingmemo.HistoryPath

interface CookingPencilcase {
    val ERASE_THICK: Float
        get() = 28f

//    enum class Thickness(val width: Float) {
//        line(1.5F),
//        thin(5F),
//        medium(8f),
//        thick(14f)
//
//    }

    enum class PenColor {
        black,
        red,
        yellow,
        green;


        val value: Int
            get() {
                when (this) {
                    black -> return Color.parseColor("#818181")
                    red -> return Color.parseColor("#80ff3300")
                    yellow -> return Color.parseColor("#80ffb300")
                    green -> return Color.parseColor("#8000ff6a")
                }
            }
        val hex: String
            get() {
                when (this) {
                    black -> return "333333"
                    red -> return "fe7b67"
                    yellow -> return "e19502"
                    green -> return "8DD933"
                }
            }
    }
    enum class PenAlpha {
        Normal,
        Highlighter;

        val value: Int
            get() {
                return when (this) {
                    Normal -> 255
                    Highlighter -> 128
                }
            }

        val hex: String
            get() {
                return when (this) {
                    Normal -> "ff"
                    Highlighter -> "80"
                }
            }
    }

    enum class EditType {
        Pencil,
        Eraser,
        Figure
    }
    enum class FigureType {
        Line,
        Circle
    }
    var editType: EditType?
    var figureType: FigureType?
    var penColor: PenColor
    var penAlpha: PenAlpha
    var thickness: Float
    var memoViews: ArrayList<CookingMemoView>
}

interface CookingPencilcaseListener {
    fun onEditTypeChanged(type: CookingPencilcase.EditType?)
    fun onEditColorChanged(color: CookingPencilcase.PenColor)
    fun onThicknessSelected(thickness: Float)
    fun onFingerDrawModeChanged(value: Boolean)

}

class CookingPencilcaseView: ConstraintLayout, CookingPencilcase {
    private val clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    var listener: CookingPencilcaseListener? = null

    var undoCount: Int = 0
        set(value) {
            field = value
            undoBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.gray_600
                setColorFilter(ContextCompat.getColor(this@CookingPencilcaseView.context, color))
            }
            clearBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.red_250
                setTextColor(ContextCompat.getColor(this@CookingPencilcaseView.context, color))
                isClickable = value != 0
            }
        }
    var redoCount: Int = 0
        set(value) {
            field = value
            redoBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.gray_600
                setColorFilter(ContextCompat.getColor(this@CookingPencilcaseView.context, color))
            }
        }

    var courseType: CourseType = CourseType.PriorConcept
        set(value) {
            field = value
            setCourseUI()
        }

    override var editType: CookingPencilcase.EditType? = null
        set(value) {
            field = value
            when (value) {
                CookingPencilcase.EditType.Eraser -> setMode(clear)
                else -> setMode(null)
            }
            configUI()
            listener?.onEditTypeChanged(value)
        }
    override var figureType: CookingPencilcase.FigureType? = null
        set(value) {
            field = value
        }

    private fun setMode(mode: Xfermode?) {
//        println("xjcl2 - setMode nul? :${mode == null}, memoviews size: ${memoViews.size}")
//        memoViews.forEach {
//            println("xjcl2 - setmode  memoId: ${it.memoId}")
//        }
        memoViews.forEach {
            if (mode == null) it.setPencil(mode, penColor.value, penAlpha.value, thickness)
            else it.setEraser(ERASE_THICK)
        }
    }
    override var penColor: CookingPencilcase.PenColor = CookingPencilcase.PenColor.black
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                val Xfermode = when (editType) {
                    CookingPencilcase.EditType.Pencil -> null
                    else -> clear
                }
                it.setCurrPaint(Xfermode, value.value, penAlpha.value)
                it.paintColor = value.value
            }
            listener?.onEditColorChanged(value)
        }
    override var penAlpha: CookingPencilcase.PenAlpha = CookingPencilcase.PenAlpha.Normal
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.paintAlpha = value.value
            }
        }

    override var thickness: Float = 5f
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.setPaintWidthDp(value)
            }
            listener?.onThicknessSelected(value)
        }
    override var memoViews: ArrayList<CookingMemoView> = arrayListOf()
    var itemValue = ""
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private val externalBtn: ImageButton by lazy { findViewById(R.id.externalBtn) }
    private val pencilBtn: ImageButton by lazy { findViewById(R.id.pencilBtn) }
    private val eraserBtn: ImageButton  by lazy { findViewById(R.id.eraserBtn) }
//    private val lineBtn: ImageButton by lazy { findViewById(R.id.lineBtn) }
//    private val thinBtn: ImageButton by lazy { findViewById(R.id.thinBtn) }
//    private val mediumBtn: ImageButton by lazy { findViewById(R.id.mediumBtn) }
//    private val thickBtn: ImageButton by lazy { findViewById(R.id.thickBtn) }
    private val thickSeekBar: SeekBar by lazy { findViewById(R.id.thickSeekBar) }
    private val thicknessIndicator: CardView by lazy { findViewById(R.id.thicknessIndicator) }

    private val colorOption0Btn: LinearLayout by lazy { findViewById(R.id.colorOption0Btn) }
    private val colorOption1Btn: LinearLayout by lazy { findViewById(R.id.colorOption1Btn) }
    private val colorOption2Btn: LinearLayout by lazy { findViewById(R.id.colorOption2Btn) }
    private val colorOption3Btn: LinearLayout by lazy { findViewById(R.id.colorOption3Btn) }

    private val alphaOptionBtn1: LinearLayout by lazy { findViewById(R.id.alphaOptionBtn1) }
    private val alphaOptionBtn2: LinearLayout by lazy { findViewById(R.id.alphaOptionBtn2) }

    private val figureOptionBtn1: LinearLayout by lazy { findViewById(R.id.figureOptionBtn1) }
    private val figureOptionBtn2: LinearLayout by lazy { findViewById(R.id.figureOptionBtn2) }
    private val figureOptionBtn3: LinearLayout by lazy { findViewById(R.id.figureOptionBtn3) }

    private val blackCheck: ImageView by lazy { findViewById(R.id.blackCheck) }
    private val yellowCheck: ImageView by lazy { findViewById(R.id.yellowCheck) }
    private val redCheck: ImageView by lazy { findViewById(R.id.redCheck) }
    private val greenCheck: ImageView by lazy { findViewById(R.id.greenCheck) }
    private val undoBtn: ImageView by lazy { findViewById(R.id.undoBtn) }
    private val redoBtn: ImageView by lazy { findViewById(R.id.redoBtn) }
    private val bar1: View by lazy { findViewById(R.id.bar1) }

//    val writeModeSwitch: Switch by lazy { findViewById(R.id.writeModeSwitch) }
    val fingerDrawModeSwitch: Switch by lazy { findViewById(R.id.fingerDrawModeSwitch) }

    val clearBtn: TextView by lazy { findViewById(R.id.clearBtn) }
    val pencilOptionLl: LinearLayout by lazy { findViewById(R.id.pencilOptionLl) }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_cooking_pencilcase, this)

        undoBtn.setOnClickListener {

            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mPaths) }
            val lastPath = allPaths.sortedBy { it.createdAt }.lastOrNull()
            memoViews.find { it.mPaths.contains(lastPath) }?.let {
                it.undoLast()
                it.saveDrawing()
            }

        }
        redoBtn.setOnClickListener {
            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mCanceledPaths) }
            val firstPath = allPaths.sortedBy { it.createdAt }.firstOrNull()
            memoViews.find { it.mCanceledPaths.contains(firstPath) }?.let {
                it.redoLast()
                it.saveDrawing()
            }

        }
        externalBtn.setOnClickListener {
            if (editType == CookingPencilcase.EditType.Pencil) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                }
            } else if (editType == CookingPencilcase.EditType.Eraser) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                }
            } else {
                pencilOptionLl.visibility = View.VISIBLE
                editType = CookingPencilcase.EditType.Pencil
            }
        }

        pencilBtn.setOnClickListener {
            figureType = null
            if (editType == CookingPencilcase.EditType.Pencil) {
                pencilOptionLl.visibility = View.GONE
            }
            editType = CookingPencilcase.EditType.Pencil
        }
        eraserBtn.setOnClickListener {
            figureType = null
            if (editType == CookingPencilcase.EditType.Eraser) {
                pencilOptionLl.visibility = View.GONE
            }
            editType = CookingPencilcase.EditType.Eraser
        }
        figureOptionBtn1.setOnClickListener {
            editType = CookingPencilcase.EditType.Pencil
            figureType = null

        }
        figureOptionBtn2.setOnClickListener {
            editType = CookingPencilcase.EditType.Figure
            figureType = CookingPencilcase.FigureType.Circle

        }
        figureOptionBtn3.setOnClickListener {
            editType = CookingPencilcase.EditType.Figure
            figureType = CookingPencilcase.FigureType.Line

        }

//        lineBtn.setOnClickListener {
//            thickness = CookingPencilcase.Thickness.line
//        }
//
//        thinBtn.setOnClickListener {
//            thickness = CookingPencilcase.Thickness.thin
//        }
//
//        mediumBtn.setOnClickListener {
//            thickness = CookingPencilcase.Thickness.medium
//        }
//
//        thickBtn.setOnClickListener {
//            thickness = CookingPencilcase.Thickness.thick
//        }
        thickSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val thickness = (p1.toFloat() + 15) / 10
                (thicknessIndicator.layoutParams as? LayoutParams)?.apply {
                    width = thickness.toInt() * 2
                }
                thicknessIndicator.requestLayout()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val value = p0?.progress?.toFloat() ?: 0.toFloat()
                val result = (value + 15) / 10
                thickness = result
            }

        })

        colorOption0Btn.setOnClickListener {
            penColor = CookingPencilcase.PenColor.black
        }

        colorOption1Btn.setOnClickListener {
            penColor = CookingPencilcase.PenColor.red
        }

        colorOption2Btn.setOnClickListener {
            penColor = CookingPencilcase.PenColor.yellow
        }

        colorOption3Btn.setOnClickListener {
            penColor = CookingPencilcase.PenColor.green
        }

        alphaOptionBtn1.setOnClickListener {
            penAlpha = CookingPencilcase.PenAlpha.Normal
        }

        alphaOptionBtn2.setOnClickListener {
            penAlpha = CookingPencilcase.PenAlpha.Highlighter
        }

        clearBtn.setOnClickListener {
            memoViews.forEach {
                it.undoAll()
                it.erase()
                it.clearBitmap()
            }
        }

//        writeModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
//            listener?.onModeChanged(isChecked)
//        }
        fingerDrawModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            listener?.onFingerDrawModeChanged(isChecked)
        }
        configUI()
    }
//    var hasOneMemo = false
//    fun hasOneMemoPerPage(hasOneMemo: Boolean) {
//        this.hasOneMemo = hasOneMemo
//    }

    private fun resetPenOrEraserBtns(selfView: View) {
        resetBackgroundExceptSelf(
            listOf<View>(pencilBtn, eraserBtn),
            selfView
        )
    }
    private fun resetThicknessBtns(selfView: View) {
//        resetBackgroundExceptSelf(
//            listOf<View>(lineBtn, thinBtn, mediumBtn, thickBtn),
//            selfView
//        )
    }
    private fun resetColorOptionBtns(selfView: View) {
        resetBackgroundExceptSelf(
            listOf<View>(colorOption0Btn, colorOption1Btn, colorOption2Btn, colorOption3Btn),
            selfView
        )
    }
    private fun resetBackgroundExceptSelf(list: List<View>, selfView: View) {
        list.filter { it != selfView }
            .forEach {
                it.setBackgroundResource(R.drawable.bg_gray_100_round_5_ripple_gray200)
            }
    }

    private fun setSelectedBackground(view: View) {
        view.setBackgroundResource(R.drawable.bg_gray_200_round_5_ripple)
    }

    private var pencilDebounce: DelayDebounce<View>? = DelayDebounce()
    private var thicknessDebounce: DelayDebounce<View>? = DelayDebounce()
    private var colorOptionDebounce: DelayDebounce<View>? = DelayDebounce()

    fun callPencilDebounce(view: View) {
        pencilDebounce?.invoke(view, 200) {
            setSelectedBackground(view)
        }
    }
    fun callThicknessDebounce(view: View) {
        thicknessDebounce?.invoke(view, 200) {
            setSelectedBackground(view)
        }
    }
    fun callColorOptionDebounce(view: View) {
        colorOptionDebounce?.invoke(view, 200) {
            setSelectedBackground(view)
        }
    }

    fun configUI() {
        when (editType) {
            CookingPencilcase.EditType.Pencil -> {
                pencilBtn.isSelected = true
                pencilBtn.run {
                    isSelected = true
                    setImageResource(R.drawable.ic_npot_pencil_filled)
                    setColorFilter(ContextCompat.getColor(this@CookingPencilcaseView.context, R.color.gray_600))
                    callPencilDebounce(this)
//                    setBackgroundResource(R.drawable.bg_gray_200_round_5_ripple_gray200)
                }

                eraserBtn.run {
                    isSelected = false
                    setImageResource(R.drawable.ic_npot_eraser)
                    setBackgroundResource(R.drawable.bg_gray_100_round_5_ripple_gray200)
                }
                pencilOptionLl.visibility = View.VISIBLE
                externalBtn.setImageResource(R.drawable.ic_npot_pencil_filled)

            }
            CookingPencilcase.EditType.Eraser -> {
                pencilBtn.isSelected = false
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_round_5_ripple_gray200)
                eraserBtn.run {
                    isSelected = true
                    setImageResource(R.drawable.ic_npot_eraser_filled)
                    setColorFilter(ContextCompat.getColor(this@CookingPencilcaseView.context, R.color.gray_600))
                    callPencilDebounce(this)
                }
//                eraserBtn.setBackgroundResource(R.drawable.bg_gray_200_round_5_ripple_gray200)

                externalBtn.setImageResource(R.drawable.ic_npot_eraser_filled)
                pencilBtn.setImageResource(R.drawable.ic_npot_pencil)
            }
            else -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = false
                pencilOptionLl.visibility = View.GONE
                externalBtn.setImageResource(R.drawable.ic_npot_pencil)
                pencilBtn.setImageResource(R.drawable.ic_npot_pencil)
                eraserBtn.setImageResource(R.drawable.ic_npot_eraser)
            }
        }


//        lineBtn.clearColorFilter()
//        thinBtn.clearColorFilter()
//        mediumBtn.clearColorFilter()
//        thickBtn.clearColorFilter()
//        val selectedColor = ContextCompat.getColor(context, R.color.gray_600)
//        when(thickness) {
//            CookingPencilcase.Thickness.line -> lineBtn
//            CookingPencilcase.Thickness.thin -> thinBtn
//            CookingPencilcase.Thickness.medium -> mediumBtn
//            CookingPencilcase.Thickness.thick -> thickBtn
//        }.run {
//            setColorFilter(selectedColor)
//            resetThicknessBtns(this)
//            callThicknessDebounce(this)
//        }

        when (penColor) {
            CookingPencilcase.PenColor.black -> colorOption0Btn
            CookingPencilcase.PenColor.red -> colorOption1Btn
            CookingPencilcase.PenColor.yellow -> colorOption2Btn
            CookingPencilcase.PenColor.green -> colorOption3Btn
        }.run {
            resetColorOptionBtns(this)
            callColorOptionDebounce(this)
        }

        blackCheck.visibility = View.GONE
        redCheck.visibility = View.GONE
        yellowCheck.visibility = View.GONE
        greenCheck.visibility = View.GONE

        when(penColor) {
            CookingPencilcase.PenColor.black -> blackCheck.visibility = View.VISIBLE
            CookingPencilcase.PenColor.red -> redCheck.visibility = View.VISIBLE
            CookingPencilcase.PenColor.yellow -> yellowCheck.visibility = View.VISIBLE
            CookingPencilcase.PenColor.green -> greenCheck.visibility = View.VISIBLE
        }

        val parseColor = Color.parseColor("#${penAlpha.hex}${penColor.hex}")
        thickSeekBar.progressTintList = ColorStateList.valueOf(parseColor)
        thickSeekBar.thumbTintList = ColorStateList.valueOf(parseColor)
        thicknessIndicator.setCardBackgroundColor(ColorStateList.valueOf(parseColor))
    }

    fun setCourseUI() {
        when (courseType) {
            CourseType.Pattern, CourseType.Cooking -> {

                externalBtn.visibility = View.VISIBLE
                undoBtn.visibility = View.VISIBLE
                redoBtn.visibility = View.VISIBLE
                bar1.visibility = View.VISIBLE
                pencilBtn.setMarginStart(dp = 9)
                eraserBtn.setMarginStart(dp = 8)
            }
//            CourseType.Cooking -> {
//                externalBtn.visibility = View.VISIBLE
//                undoBtn.visibility = View.GONE
//                redoBtn.visibility = View.GONE
//                bar1.visibility = View.GONE
//                pencilBtn.setMarginStart(dp = 0)
//                eraserBtn.setMarginStart(dp = 12)
//            }
            else -> {
                externalBtn.visibility = View.GONE
            }
        }
    }

    fun setDefaultState() {
        this.editType = null
        pencilOptionLl.visibility = View.GONE
//        clearAllBtn.visibility = View.GONE
        configUI()
    }
}