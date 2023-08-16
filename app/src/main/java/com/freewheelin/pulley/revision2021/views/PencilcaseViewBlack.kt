package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.cookingmemo.CookingMemoView
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.legacy.utils.DelayDebounce
import com.freewheelin.pulley.legacy.utils.setMarginStart

interface CookingPencilcase {
    val ERASE_THICK: Float
        get() = 28f

    enum class Thickness(val width: Float) {
        line(1.5F),
        thin(5F),
        medium(8f),
        thick(14f)

    }

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
        val alpha: Int
            get() {
                when(this) {
                    black -> return 255
                    else -> return 128
                }
            }
    }

    enum class EditType {
        pencil,
        eraser
    }

    var editType: EditType?
    var penColor: PenColor
    var thickness: Thickness
    var memoViews: ArrayList<CookingMemoView>
}

interface CookingPencilcaseListener {
    fun onEditTypeChanged(type: CookingPencilcase.EditType?)
    fun onEditColorChanged(color: CookingPencilcase.PenColor)
    fun onThicknessSelected(thickness: CookingPencilcase.Thickness)
    fun onModeChanged(isFixedMode: Boolean)
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
                CookingPencilcase.EditType.pencil -> setMode(null)
                else -> setMode(clear)
            }
            configUI()
            listener?.onEditTypeChanged(value)
        }
    private fun setMode(mode: Xfermode?) {
//        println("xjcl2 - setMode nul? :${mode == null}, memoviews size: ${memoViews.size}")
//        memoViews.forEach {
//            println("xjcl2 - setmode  memoId: ${it.memoId}")
//        }
        memoViews.forEach {
            if (mode == null) it.setPencil(mode, penColor.value, penColor.alpha, thickness.width)
            else it.setEraser(ERASE_THICK)
        }
    }
    override var penColor: CookingPencilcase.PenColor = CookingPencilcase.PenColor.black
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                val Xfermode = when (editType) {
                    CookingPencilcase.EditType.pencil -> null
                    else -> clear
                }
                it.setCurrPaint(Xfermode, value.value, value.alpha)
            }
            listener?.onEditColorChanged(value)
        }
    override var thickness: CookingPencilcase.Thickness = CookingPencilcase.Thickness.line
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.setPaintWidthDp(value.width)
            }
            listener?.onThicknessSelected(value)
        }
    override var memoViews: ArrayList<CookingMemoView> = arrayListOf()
    var itemValue = ""
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private val externalBtn: ImageButton by lazy { findViewById(R.id.externalBtn) }
    private val pencilBtn: ImageButton by lazy { findViewById(R.id.pencilBtn) }
    private val eraserBtn: ImageButton  by lazy { findViewById(R.id.eraserBtn) }
    private val lineBtn: ImageButton by lazy { findViewById(R.id.lineBtn) }
    private val thinBtn: ImageButton by lazy { findViewById(R.id.thinBtn) }
    private val mediumBtn: ImageButton by lazy { findViewById(R.id.mediumBtn) }
    private val thickBtn: ImageButton by lazy { findViewById(R.id.thickBtn) }

    private val colorOption0Btn: LinearLayout by lazy { findViewById(R.id.colorOption0Btn) }
    private val colorOption1Btn: LinearLayout by lazy { findViewById(R.id.colorOption1Btn) }
    private val colorOption2Btn: LinearLayout by lazy { findViewById(R.id.colorOption2Btn) }
    private val colorOption3Btn: LinearLayout by lazy { findViewById(R.id.colorOption3Btn) }

    private val blackCheck: ImageView by lazy { findViewById(R.id.blackCheck) }
    private val yellowCheck: ImageView by lazy { findViewById(R.id.yellowCheck) }
    private val redCheck: ImageView by lazy { findViewById(R.id.redCheck) }
    private val greenCheck: ImageView by lazy { findViewById(R.id.greenCheck) }
    private val undoBtn: ImageView by lazy { findViewById(R.id.undoBtn) }
    private val redoBtn: ImageView by lazy { findViewById(R.id.redoBtn) }
    private val bar1: View by lazy { findViewById(R.id.bar1) }

    val writeModeSwitch: Switch by lazy { findViewById(R.id.writeModeSwitch) }

    val clearBtn: TextView by lazy { findViewById(R.id.clearBtn) }
    val pencilOptionLl: LinearLayout by lazy { findViewById(R.id.pencilOptionLl) }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_cooking_pencilcase, this)

        undoBtn.setOnClickListener {
            if (memoViews.size == 1) {
                memoViews.forEach {
                    it.undoLast()
                    it.saveDrawing()
                }
            }
        }
        redoBtn.setOnClickListener {
            if (memoViews.size == 1) {
                memoViews.forEach {
                    it.redoLast()
                    it.saveDrawing()
                }
            }
        }
        externalBtn.setOnClickListener {
            if (editType == CookingPencilcase.EditType.pencil) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                }
            } else if (editType == CookingPencilcase.EditType.eraser) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                }
            } else {
                pencilOptionLl.visibility = View.VISIBLE
                editType = CookingPencilcase.EditType.pencil
            }
        }

        pencilBtn.setOnClickListener {
            if (editType == CookingPencilcase.EditType.pencil) {
                pencilOptionLl.visibility = View.GONE
            }
            editType = CookingPencilcase.EditType.pencil
        }
        eraserBtn.setOnClickListener {
            if (editType == CookingPencilcase.EditType.eraser) {
                pencilOptionLl.visibility = View.GONE
            }
            editType = CookingPencilcase.EditType.eraser
        }

        lineBtn.setOnClickListener {
            thickness = CookingPencilcase.Thickness.line
        }

        thinBtn.setOnClickListener {
            thickness = CookingPencilcase.Thickness.thin
        }

        mediumBtn.setOnClickListener {
            thickness = CookingPencilcase.Thickness.medium
        }

        thickBtn.setOnClickListener {
            thickness = CookingPencilcase.Thickness.thick
        }

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

        clearBtn.setOnClickListener {
            memoViews.forEach {
                it.undoAll()
                it.erase()
                it.clearBitmap()
            }
        }

        writeModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            listener?.onModeChanged(isChecked)
        }
        configUI()
    }
    var hasOneMemo = false
    fun hasOneMemoPerPage(hasOneMemo: Boolean) {
        this.hasOneMemo = hasOneMemo
    }

    private fun resetPenOrEraserBtns(selfView: View) {
        resetBackgroundExceptSelf(
            listOf<View>(pencilBtn, eraserBtn),
            selfView
        )
    }
    private fun resetThicknessBtns(selfView: View) {
        resetBackgroundExceptSelf(
            listOf<View>(lineBtn, thinBtn, mediumBtn, thickBtn),
            selfView
        )
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
            CookingPencilcase.EditType.pencil -> {
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
            CookingPencilcase.EditType.eraser -> {
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


        lineBtn.clearColorFilter()
        thinBtn.clearColorFilter()
        mediumBtn.clearColorFilter()
        thickBtn.clearColorFilter()
        val selectedColor = ContextCompat.getColor(context, R.color.gray_600)
        when(thickness) {
            CookingPencilcase.Thickness.line -> lineBtn
            CookingPencilcase.Thickness.thin -> thinBtn
            CookingPencilcase.Thickness.medium -> mediumBtn
            CookingPencilcase.Thickness.thick -> thickBtn
        }.run {
            setColorFilter(selectedColor)
            resetThicknessBtns(this)
            callThicknessDebounce(this)
        }

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
    }

    fun setCourseUI() {
        when (courseType) {
            CourseType.Pattern -> {

                externalBtn.visibility = View.VISIBLE
                undoBtn.visibility = View.VISIBLE
                redoBtn.visibility = View.VISIBLE
                bar1.visibility = View.VISIBLE
                pencilBtn.setMarginStart(dp = 9)
                eraserBtn.setMarginStart(dp = 8)
            }
            CourseType.Cooking -> {
                externalBtn.visibility = View.VISIBLE
                undoBtn.visibility = View.GONE
                redoBtn.visibility = View.GONE
                bar1.visibility = View.GONE
                pencilBtn.setMarginStart(dp = 0)
                eraserBtn.setMarginStart(dp = 12)
            }
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