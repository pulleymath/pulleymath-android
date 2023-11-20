package com.freewheelin.pulley.legacy.views

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
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.views.memoView.HistoryPath
import com.freewheelin.pulley.legacy.views.memoView.MemoView


interface Pencilcase {

    val ERASE_THICK: Float
        get() = 28f
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
        Figure,
    }
    enum class FigureType {
        Line,
        Circle,
        Arrow
    }

    var editType: EditType?
    var figureType: FigureType?
    var penColor: PenColor
    var penAlpha: PenAlpha
    var thickness: Float
    var memoViews: ArrayList<MemoView>
}


interface PencilcaseListener {
    fun onEditTypeChanged(type: Pencilcase.EditType?)
//    fun onEditColorChanged(color: Pencilcase.PenColor)
    fun onThicknessSelected(thickness: Float)
    fun onFingerDrawModeChanged(value: Boolean)
}
class PencilcaseView: ConstraintLayout, Pencilcase {
    var listener: PencilcaseListener? = null
    private val clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    override var editType: Pencilcase.EditType? = null
        set(value) {
            field = value
            when (value) {
                Pencilcase.EditType.Eraser -> setMode(clear)
                else -> setMode(null)
//                else -> setMode(clear)
            }
            configUI()
            listener?.onEditTypeChanged(value)
        }
    private fun setMode(mode: Xfermode?) {
        memoViews.forEach {
            if (mode == null) it.setPencil(mode, penColor.value, penAlpha.value, thickness)
            else it.setEraser(ERASE_THICK)
        }
    }
    override var figureType: Pencilcase.FigureType? = null
        set(value) {
            field = value
//            configUI()
            // TODO
//            listener?.onFingerDrawModeChanged()
        }
    override var penColor: Pencilcase.PenColor = Pencilcase.PenColor.black
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.paintColor = value.value
//                it.paintAlpha = value.alpha
            }
        }
    override var penAlpha: Pencilcase.PenAlpha = Pencilcase.PenAlpha.Normal
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

    override var memoViews: ArrayList<MemoView> = arrayListOf()
    var itemValue = ""
    var fingerDrawMode = false
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var undoBtn: LinearLayout
    var redoBtn: LinearLayout

    var pencilBtn: ImageButton
    var eraserBtn: ImageButton
    var figureBtn: ImageButton
    var figure2Btn: ImageButton
    var figure3Btn: ImageButton

    var thickSeekBar: SeekBar
    var thicknessIndicator: CardView

    var colorOption0Btn: LinearLayout
    var colorOption1Btn: LinearLayout
    var colorOption2Btn: LinearLayout
    var colorOption3Btn: LinearLayout
    var alphaOptionBtn1: LinearLayout
    var alphaOptionBtn2: LinearLayout

    var blackCheck: ImageView
    var yellowCheck: ImageView
    var redCheck: ImageView
    var greenCheck: ImageView

    var fingerDrawModeSwitch: Switch
    var clearAllBtn: Button
    var pencilOptionLl: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pencilcase, this)

        pencilBtn = findViewById(R.id.pencilBtn)
        eraserBtn = findViewById(R.id.eraserBtn)
        figureBtn = findViewById(R.id.figureBtn)
        figure2Btn = findViewById(R.id.figure2Btn)
        figure3Btn = findViewById(R.id.figure3Btn)
        thickSeekBar = findViewById(R.id.thickSeekBar)
        thicknessIndicator = findViewById(R.id.thicknessIndicator)

        colorOption0Btn = findViewById(R.id.colorOption0Btn)
        colorOption1Btn = findViewById(R.id.colorOption1Btn)
        colorOption2Btn = findViewById(R.id.colorOption2Btn)
        colorOption3Btn = findViewById(R.id.colorOption3Btn)

        undoBtn = findViewById(R.id.undoBtn)
        redoBtn = findViewById(R.id.redoBtn)
        alphaOptionBtn1 = findViewById(R.id.alphaOptionBtn1)
        alphaOptionBtn2 = findViewById(R.id.alphaOptionBtn2)

        blackCheck = findViewById(R.id.blackCheck)
        yellowCheck = findViewById(R.id.yellowCheck)
        redCheck = findViewById(R.id.redCheck)
        greenCheck = findViewById(R.id.greenCheck)

        fingerDrawModeSwitch = findViewById(R.id.fingerDrawModeSwitch)
        clearAllBtn = findViewById(R.id.clearAllBtn)
        pencilOptionLl = findViewById(R.id.pencilOptionLl)

        pencilBtn.setOnClickListener {
            figureType = null
            when (editType) {
                Pencilcase.EditType.Pencil -> {
                    if (pencilOptionLl.visibility == View.VISIBLE) {
                        pencilOptionLl.visibility = View.GONE
                        editType = null
                    } else {
                        pencilOptionLl.visibility = View.VISIBLE
                    }

                }
                else -> {
                    editType = Pencilcase.EditType.Pencil
                    pencilOptionLl.visibility = View.VISIBLE
                }
            }

            clearAllBtn.visibility = View.GONE
        }

        eraserBtn.setOnClickListener {
            figureType = null
            when (editType) {
                Pencilcase.EditType.Eraser -> {
                    if (clearAllBtn.visibility == View.VISIBLE) {
                        clearAllBtn.visibility = View.GONE
                        editType = null
                    } else {
                        clearAllBtn.visibility = View.VISIBLE
                    }

                }
                else -> {
                    editType = Pencilcase.EditType.Eraser
                    clearAllBtn.visibility = View.VISIBLE
                }
            }

            pencilOptionLl.visibility = View.GONE
//            listener?.onEraserBtnClicked(this)
        }
        figureBtn.setOnClickListener {
            figureType = Pencilcase.FigureType.Circle
            when (editType) {
                Pencilcase.EditType.Figure -> {
                    editType = null
                }
                else -> {
                    editType = Pencilcase.EditType.Figure
                }
            }
        }
        figure2Btn.setOnClickListener {
            figureType = Pencilcase.FigureType.Line
            when(editType) {
                Pencilcase.EditType.Figure -> {
                    editType = null
                }
                else -> {
                    editType = Pencilcase.EditType.Figure
                }
            }
        }
        figure3Btn.setOnClickListener {
            figureType = Pencilcase.FigureType.Arrow
            when(editType) {
                Pencilcase.EditType.Figure -> {
                    editType = null
                }
                else -> {
                    editType = Pencilcase.EditType.Figure
                }
            }
        }

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


        undoBtn.setOnClickListener {
            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mPaths) }
            val lastPath = allPaths.sortedBy { it.createdAt }.lastOrNull()
            memoViews.find { it.mPaths.contains(lastPath) }?.undoLast()
        }
        redoBtn.setOnClickListener {
            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mCanceledPaths) }
            val firstPath = allPaths.sortedBy { it.createdAt }.firstOrNull()
            memoViews.find { it.mCanceledPaths.contains(firstPath) }?.redoLast()
        }

        colorOption0Btn.setOnClickListener {
            penColor = Pencilcase.PenColor.black
        }

        colorOption1Btn.setOnClickListener {
            penColor = Pencilcase.PenColor.red
        }

        colorOption2Btn.setOnClickListener {
            penColor = Pencilcase.PenColor.yellow
        }

        colorOption3Btn.setOnClickListener {
            penColor = Pencilcase.PenColor.green
        }
        alphaOptionBtn1.setOnClickListener {
            penAlpha = Pencilcase.PenAlpha.Normal
        }

        alphaOptionBtn2.setOnClickListener {
            penAlpha = Pencilcase.PenAlpha.Highlighter
        }

        clearAllBtn.setOnClickListener {
            memoViews.forEach {
                it.undoAll()
            }
        }

        fingerDrawModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            fingerDrawMode = isChecked
            listener?.onFingerDrawModeChanged(isChecked)
        }
        configUI()
    }

    fun configUI() {
        when (editType) {
            Pencilcase.EditType.Pencil -> {
                pencilBtn.isSelected = true
                eraserBtn.isSelected = false
                figureBtn.isSelected = false
                figure2Btn.isSelected = false
                pencilOptionLl.visibility = View.VISIBLE
                eraserBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                pencilBtn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                figureBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure2Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure3Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
            }
            Pencilcase.EditType.Eraser -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = true
                figureBtn.isSelected = false
                figure2Btn.isSelected = false
                pencilOptionLl.visibility = View.GONE
                eraserBtn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figureBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure2Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure3Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
            }
            Pencilcase.EditType.Figure -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = false
                figureBtn.isSelected = true
                figure2Btn.isSelected = false
                pencilOptionLl.visibility = View.GONE
                clearAllBtn.visibility = View.GONE
                eraserBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                if (figureType == Pencilcase.FigureType.Circle) {
                    figureBtn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                    figure2Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                    figure3Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)

                } else if (figureType == Pencilcase.FigureType.Line) {
                    figureBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                    figure2Btn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                    figure3Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                } else if (figureType == Pencilcase.FigureType.Arrow) {
                    figureBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                    figure2Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                    figure3Btn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                }
            }
            else -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = false
                figureBtn.isSelected = false
                pencilOptionLl.visibility = View.GONE
                clearAllBtn.visibility = View.GONE

                eraserBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figureBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure2Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                figure3Btn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
            }
        }

        blackCheck.visibility = View.GONE
        redCheck.visibility = View.GONE
        yellowCheck.visibility = View.GONE
        greenCheck.visibility = View.GONE

        when(penColor) {
            Pencilcase.PenColor.black -> blackCheck.visibility = View.VISIBLE
            Pencilcase.PenColor.red -> redCheck.visibility = View.VISIBLE
            Pencilcase.PenColor.yellow -> yellowCheck.visibility = View.VISIBLE
            Pencilcase.PenColor.green -> greenCheck.visibility = View.VISIBLE
        }

        val parseColor = Color.parseColor("#${penAlpha.hex}${penColor.hex}")
        thickSeekBar.progressTintList = ColorStateList.valueOf(parseColor)
        thickSeekBar.thumbTintList = ColorStateList.valueOf(parseColor)
        thicknessIndicator.setCardBackgroundColor(ColorStateList.valueOf(parseColor))
    }

    fun setDefaultState() {
        this.editType = null
        pencilOptionLl.visibility = View.GONE
        clearAllBtn.visibility = View.GONE
        configUI()
    }
}