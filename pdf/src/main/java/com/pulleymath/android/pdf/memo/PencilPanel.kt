package com.pulleymath.android.pdf.memo

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pulleymath.android.pdf.R


interface PencilPanelListener {
    fun onFingerDrawModeChanged(value: Boolean)
    fun onDrawTypeChanged(type: DrawType?)

}
enum class PenColorType {
    Black, Red, Yellow, Green, Blue;
    val value: Int
        get() {
            when (this) {
                Black -> return Color.parseColor("#333333")
                Red -> return Color.parseColor("#fe7b67")
                Yellow -> return Color.parseColor("#ffb300")
                Green -> return Color.parseColor("#8DD933")
                Blue -> return Color.parseColor("#30A4FF")
            }
        }
    val hex: String
        get() {
            when (this) {
                Black -> return "333333"
                Red -> return "fe7b67"
                Yellow -> return "ffb300"
                Green -> return "8DD933"
                Blue -> return "30A4FF"
            }
        }
}
enum class PenAlphaType {
    Normal, Highlighter;
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
enum class DrawPathType {
    Curve,
    Line,
    Arrow,
    Circle
}
enum class DrawType {
    Pencil,
    Eraser,
    Figure
}
interface IPencilPanel {

    var drawType: DrawType?
    var pathType: DrawPathType
    var penColorType: PenColorType
    var penAlphaType: PenAlphaType
    var thickness: Float
    var memoViews: ArrayList<MemoView>
}
class PencilPanel(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), IPencilPanel {
    private val clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val ERASE_THICK: Float = 28f
    var listener: PencilPanelListener? = null
    override var memoViews: ArrayList<MemoView> = arrayListOf()

    var fingerDrawMode = false
    override var thickness: Float = 2f
    override var drawType: DrawType? = null
    override var pathType: DrawPathType = DrawPathType.Curve
    override var penColorType: PenColorType = PenColorType.Black
    override var penAlphaType: PenAlphaType = PenAlphaType.Normal


    private val mainPanelLl: LinearLayout by lazy { findViewById(R.id.mainPanelLl) }
    val eraserPanelCl: ConstraintLayout by lazy { findViewById(R.id.eraserPanelCl) }
    val figurePanelCl: ConstraintLayout by lazy { findViewById(R.id.figurePanelCl) }
    val penOptionPanelCl: ConstraintLayout by lazy { findViewById(R.id.penOptionPanelCl) }
    private val undoBtn: ImageButton by lazy { findViewById(R.id.undoBtn) }
    private val redoBtn: ImageButton by lazy { findViewById(R.id.redoBtn) }
    val penBtn: ImageButton by lazy { findViewById(R.id.penBtn) }
    private val eraserBtn: ImageButton by lazy { findViewById(R.id.eraserBtn) }
    private val figureBtn: ImageButton by lazy { findViewById(R.id.figureBtn) }
    private val lineFigureBtn: ImageButton by lazy { findViewById(R.id.lineFigureBtn) }
    private val arrowFigureBtn: ImageButton by lazy { findViewById(R.id.arrowFigureBtn) }
    private val circleFigureBtn: ImageButton by lazy { findViewById(R.id.circleFigureBtn) }
    private val removeAllBtn: TextView by lazy { findViewById(R.id.removeAllBtn) }
    private val penAlphaOptionBtn: TextView by lazy { findViewById(R.id.penAlphaOptionBtn) }
    private val highlighterAlphaOptionBtn: TextView by lazy { findViewById(R.id.highlighterAlphaOptionBtn) }
    private val thicknessIndicator: CardView by lazy { findViewById(R.id.thicknessIndicator) }
    private val thickSeekBar: SeekBar by lazy { findViewById(R.id.thickSeekBar) }
    //    private val thickSlider: Slider by lazy { findViewById(R.id.thickSlider) }
    private val blackColorLl: LinearLayout by lazy { findViewById(R.id.blackColorLl) }
    private val redColorLl: LinearLayout by lazy { findViewById(R.id.redColorLl) }
    private val yellowColorLl: LinearLayout by lazy { findViewById(R.id.yellowColorLl) }
    private val greenColorLl: LinearLayout by lazy { findViewById(R.id.greenColorLl) }
    private val blueColorLl: LinearLayout by lazy { findViewById(R.id.blueColorLl) }

    private val blackColorCl: ConstraintLayout by lazy { findViewById(R.id.blackColorCl) }
    private val blueColorCl: ConstraintLayout by lazy { findViewById(R.id.blueColorCl) }
    private val greenColorCl: ConstraintLayout by lazy { findViewById(R.id.greenColorCl) }
    private val yellowColorCl: ConstraintLayout by lazy { findViewById(R.id.yellowColorCl) }
    private val redColorCl: ConstraintLayout by lazy { findViewById(R.id.redColorCl) }

    private val blackColorInnerCircleIv: ImageView by lazy { findViewById(R.id.blackColorInnerCircleIv) }
    private val blueColorInnerCircleIv: ImageView by lazy { findViewById(R.id.blueColorInnerCircleIv) }
    private val greenColorInnerCircleIv: ImageView by lazy { findViewById(R.id.greenColorInnerCircleIv) }
    private val yellowColorInnerCircleIv: ImageView by lazy { findViewById(R.id.yellowColorInnerCircleIv) }
    private val redColorInnerCircleIv: ImageView by lazy { findViewById(R.id.redColorInnerCircleIv) }
    val fingerDrawModeSwitch: Switch by lazy { findViewById(R.id.fingerDrawModeSwitch) }

    private val blackColor: Triple<LinearLayout, ImageView, PenColorType> by lazy { Triple(blackColorLl, blackColorInnerCircleIv, PenColorType.Black) }
    private val redColor by lazy {  Triple(redColorLl, redColorInnerCircleIv, PenColorType.Red) }
    private val yellowColor by lazy { Triple(yellowColorLl, yellowColorInnerCircleIv, PenColorType.Yellow) }
    private val greenColor by lazy { Triple(greenColorLl, greenColorInnerCircleIv, PenColorType.Green) }
    private val blueColor by lazy { Triple(blueColorLl, blueColorInnerCircleIv, PenColorType.Blue) }

    private val lineFigure by lazy { Pair(lineFigureBtn, DrawPathType.Line) }
    private val arrowFigure by lazy { Pair(arrowFigureBtn, DrawPathType.Arrow) }
    private val circleFigure by lazy { Pair(circleFigureBtn, DrawPathType.Circle) }

    private var prevHighlighterThickness = 14f
    private var prevHighlighterColor = PenColorType.Blue
    private var prevPenThickness = 2f
    private var prevPenColor = PenColorType.Black
    init {
        LayoutInflater.from(context).inflate(R.layout.view_pencil_panel, this)
        setThicknessIndicatorHeight(thickness.toInt())

        undoBtn.setOnClickListener {
            isMainPanelTransparency = false
            mainPanelLl.animate().cancel()
            mainPanelLl.alpha = 1f

            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mPaths) }
            val lastPath = allPaths.sortedBy { it.createdAt }.lastOrNull()
            memoViews.find { it.mPaths.contains(lastPath) }?.undoLast()
        }
        redoBtn.setOnClickListener {
            isMainPanelTransparency = false
            mainPanelLl.animate().cancel()
            mainPanelLl.alpha = 1f

            val allPaths = mutableListOf<HistoryPath>()
            memoViews.forEach { allPaths.addAll(it.mCanceledPaths) }
            val firstPath = allPaths.sortedBy { it.createdAt }.firstOrNull()
            memoViews.find { it.mCanceledPaths.contains(firstPath) }?.redoLast()
        }
        penBtn.setOnClickListener {
            openPencilPanel()
        }
        eraserBtn.setOnClickListener {
            selectPanelAndShowSubPanel(it as ImageButton)

            isMainPanelTransparency = false
            mainPanelLl.animate().cancel()
            mainPanelLl.alpha = 1f

            penBtn.setImageResource(R.drawable.ic_pencil)
            it.setImageResource(R.drawable.ic_eraser_filled)
            figureBtn.setImageResource(R.drawable.ic_figure)

            it.background = ContextCompat.getDrawable(
                context,
                R.drawable.bg_gray_200_round_5_ripple
            )
            penBtn.background = null
            figureBtn.background = null

            changeDrawType(DrawType.Eraser)
            pathType = DrawPathType.Curve
            setClearMode(clear)
        }
        figureBtn.setOnClickListener {
            selectPanelAndShowSubPanel(it as ImageButton)

            isMainPanelTransparency = false
            mainPanelLl.animate().cancel()
            mainPanelLl.alpha = 1f

            penBtn.setImageResource(R.drawable.ic_pencil)
            eraserBtn.setImageResource(R.drawable.ic_npot_eraser)
            figureBtn.setImageResource(R.drawable.ic_figure_filled)

            figureBtn.background = ContextCompat.getDrawable(
                context,
                R.drawable.bg_gray_200_round_5_ripple
            )
            penBtn.background = null
            eraserBtn.background = null

            pathType = prevFigure.second
            selectFigureBtn(prevFigure.first)
            changeDrawType(DrawType.Figure)
            setFigurePencil()
        }

        penAlphaOptionBtn.setOnClickListener {
            if (penAlphaType == PenAlphaType.Normal) return@setOnClickListener
            prevHighlighterThickness = thickness
            prevHighlighterColor = penColorType

            penAlphaType = PenAlphaType.Normal
            penColorType = prevPenColor
            thickness = prevPenThickness

            setClearMode(null)

            setThicknessIndicatorHeight(thickness.toInt())
            thickSeekBar.progress = thickness.toInt() * 10 - 15

            // prevSelectedColor
            listOf(blackColor, redColor, yellowColor, greenColor, blueColor)
                .find { it.third === penColorType }
                ?.let {
                    changeColorCheckIcon(it.second)
                    prevSelectedColor = it
                }
            prevSelectedAlpha = PenAlphaType.Normal
            selectAlphaOptionTextColor(prevSelectedAlpha)
            changeColorOnAlphaOption(prevSelectedAlpha)

            setAlphaTypeBackground(it)
        }
        highlighterAlphaOptionBtn.setOnClickListener {
            if (penAlphaType == PenAlphaType.Highlighter) return@setOnClickListener
            prevPenThickness = thickness
            prevPenColor = penColorType

            penAlphaType = PenAlphaType.Highlighter
            penColorType = prevHighlighterColor
            thickness = prevHighlighterThickness
            setClearMode(null)

            setThicknessIndicatorHeight(thickness.toInt())
            thickSeekBar.progress = thickness.toInt() * 10 - 15

            // prevSelectedColor
            listOf(blackColor, redColor, yellowColor, greenColor, blueColor)
                .find { it.third === penColorType }
                ?.let {
                    changeColorCheckIcon(it.second)
                    prevSelectedColor = it
                }
            prevSelectedAlpha = PenAlphaType.Highlighter
            selectAlphaOptionTextColor(prevSelectedAlpha)
            changeColorOnAlphaOption(prevSelectedAlpha)
//            changeMemoAlpha()
            setAlphaTypeBackground(it)
        }
        thickSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val thickness = (p1.toFloat() + 15) / 10
                setThicknessIndicatorHeight(thickness.toInt())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val value = p0?.progress?.toFloat() ?: 0.toFloat()
                val result = (value + 15) / 10
                thickness = result
                memoViews.forEach {
                    it.setPaintWidthDp(result)
                }
            }

        })

        listOf(blackColor, redColor, yellowColor, greenColor, blueColor).forEach { item ->
            item.first.setOnClickListener {
                changeColorCheckIcon(item.second)
                penColorType = item.third
                penAlphaType = prevSelectedAlpha
                changeMemoColor()
                changeMemoAlpha()
                prevSelectedColor = item
            }
        }


        fingerDrawModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            fingerDrawMode = isChecked
            listener?.onFingerDrawModeChanged(isChecked)
        }

        removeAllBtn.setOnClickListener {
            memoViews.forEach { it.undoAll() }
        }

        lineFigureBtn.setOnClickListener {
            pathType = DrawPathType.Line
            prevFigure = lineFigure
            selectFigureBtn(it)
        }
        arrowFigureBtn.setOnClickListener {
            pathType = DrawPathType.Arrow
            prevFigure = arrowFigure
            selectFigureBtn(it)
        }
        circleFigureBtn.setOnClickListener {
            pathType = DrawPathType.Circle
            prevFigure = circleFigure
            selectFigureBtn(it)
        }
    }
    var prevSelectedColor = blackColor
    var prevSelectedAlpha = PenAlphaType.Normal
    var prevFigure = lineFigure

    private fun selectAlphaOptionTextColor(penAlphaType: PenAlphaType) {
        val penBtnColor = if (penAlphaType == PenAlphaType.Normal) ContextCompat.getColor(context, R.color.gray_800) else ContextCompat.getColor(context, R.color.gray_600)
        val highlighterBtnColor = if (penAlphaType == PenAlphaType.Highlighter) ContextCompat.getColor(context, R.color.gray_800) else ContextCompat.getColor(context, R.color.gray_600)
        penAlphaOptionBtn.setTextColor(penBtnColor)
        highlighterAlphaOptionBtn.setTextColor(highlighterBtnColor)
    }
    private fun changeColorOnAlphaOption(penAlphaType: PenAlphaType) {
        when (penAlphaType) {
            PenAlphaType.Normal -> {
                blackColorCl.setBackgroundResource(R.drawable.bg_black_200_circle)
                blueColorCl.setBackgroundResource(R.drawable.bg_blue_400_circle)
                greenColorCl.setBackgroundResource(R.drawable.bg_green_300_circle)
                yellowColorCl.setBackgroundResource(R.drawable.bg_yellow_300_circle)
                redColorCl.setBackgroundResource(R.drawable.bg_red_300_circle)
            }
            PenAlphaType.Highlighter -> {
                blackColorCl.setBackgroundResource(R.drawable.bg_gray_700_circle)
                blueColorCl.setBackgroundResource(R.drawable.bg_blue_300_circle)
                greenColorCl.setBackgroundResource(R.drawable.bg_green_200_circle)
                yellowColorCl.setBackgroundResource(R.drawable.bg_yellow_200_circle)
                redColorCl.setBackgroundResource(R.drawable.bg_red_200_circle)
            }
        }
    }
    private fun selectFigureBtn(selectedBtn: View) {
        listOf(lineFigureBtn, arrowFigureBtn, circleFigureBtn).forEach {
            if (it == selectedBtn) {
                it.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_gray_200_round_5_ripple
                )
            } else {
                it.setBackgroundResource(0)
            }
        }
    }
    var undoCount: Int = 0
        set(value) {
            field = value
            undoBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.gray_600
                setColorFilter(ContextCompat.getColor(context, color))
            }
            removeAllBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.red_250
                setTextColor(ContextCompat.getColor(context, color))
                isClickable = value != 0
            }
        }
    var redoCount: Int = 0
        set(value) {
            field = value
            redoBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.gray_600
                setColorFilter(ContextCompat.getColor(context, color))
            }
            removeAllBtn.run {
                val color = if (value == 0) R.color.gray_400 else R.color.red_250
                setTextColor(ContextCompat.getColor(context, color))
                isClickable = value != 0
            }
        }
    private fun setThicknessIndicatorHeight(value: Int) {
        (thicknessIndicator.layoutParams as? LayoutParams)?.apply {
            height = value * 3
        }
        thicknessIndicator.requestLayout()
    }
    fun openPencilPanel() {
        selectPanelAndShowSubPanel(penBtn)
        isMainPanelTransparency = false
        mainPanelLl.animate().cancel()
        mainPanelLl.alpha = 1f

        penBtn.setImageResource(R.drawable.ic_pencil_filled)
        eraserBtn.setImageResource(R.drawable.ic_npot_eraser)
        figureBtn.setImageResource(R.drawable.ic_figure)

        penBtn.background = ContextCompat.getDrawable(
            context,
            R.drawable.bg_gray_200_round_5_ripple
        )
        eraserBtn.background = null
        figureBtn.background = null

        changeDrawType(DrawType.Pencil)
        pathType = DrawPathType.Curve
        changeColorCheckIcon(prevSelectedColor.second)
        penColorType = prevSelectedColor.third
        penAlphaType = prevSelectedAlpha
        selectAlphaOptionTextColor(prevSelectedAlpha)
        changeColorOnAlphaOption(prevSelectedAlpha)
        setClearMode(null)
        changeMemoColor()
        changeMemoAlpha()
    }
    fun closePencilPanel() {
        changeDrawType(null)
    }

    var isMainPanelTransparency = false
    fun transparencyMainPanel() {
        if (!isMainPanelTransparency) {
            isMainPanelTransparency = true
            mainPanelLl.animate().alpha(0.4f).setDuration(3000)
                .setInterpolator(AccelerateDecelerateInterpolator()).start()
        }
    }
    private fun changeMemoColor() {
        memoViews.forEach {
            it.paintColor = penColorType.value
        }
    }
    private fun changeMemoAlpha() {
        memoViews.forEach {
            it.paintAlpha = penAlphaType.value
        }
    }
    private fun setAlphaTypeBackground(view: View) {
        listOf(penAlphaOptionBtn, highlighterAlphaOptionBtn)
            .forEach {
                if (it == view) {
                    it.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_white_round_2
                    )
                } else {
                    it.setBackgroundResource(0)
                }
            }
    }

    private fun changeDrawType (type: DrawType?) {
        drawType = type
        listener?.onDrawTypeChanged(drawType)
    }
    private fun selectPanelAndShowSubPanel(btn: ImageButton) {
        listOf(
            Pair(penBtn, penOptionPanelCl),
            Pair(eraserBtn, eraserPanelCl),
            Pair(figureBtn, figurePanelCl),
        ).forEach { item ->
            item.second.visibility = if (item.first == btn) View.VISIBLE else View.GONE
        }
    }

    private fun changeColorCheckIcon(selected: ImageView) {
        listOf(
            blackColorInnerCircleIv,
            redColorInnerCircleIv,
            yellowColorInnerCircleIv,
            greenColorInnerCircleIv,
            blueColorInnerCircleIv
        ).forEach {
            it.visibility = if (it == selected) View.VISIBLE else View.GONE
        }
    }
    private fun setClearMode(mode: Xfermode?) {
        memoViews.forEach {
            if (mode == null) it.setPencil(mode, penColorType.value, penAlphaType.value, thickness)
            else it.setEraser(ERASE_THICK)
        }
    }
    private fun setFigurePencil() {
        memoViews.forEach {
            it.setPencil(null, PenColorType.Black.value, PenAlphaType.Normal.value, 2f)
        }
    }
    fun setFingerDrawModeWithPencilcase(value: Boolean) {
        memoViews.forEach {
            it.fingerDrawMode = value
        }
    }
}