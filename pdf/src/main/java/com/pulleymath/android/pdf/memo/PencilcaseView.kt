package com.pulleymath.android.pdf.memo

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.pulleymath.android.pdf.R

interface Pencilcase {
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
                black -> return Color.parseColor("#000000")
                red -> return Color.parseColor("#ff3300")
                yellow -> return Color.parseColor("#ffb300")
                green -> return Color.parseColor("#00ff6a")
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
    var memoViews: ArrayList<MemoView>
}


//interface PencilcaseListener {
//    fun onEditTypeChanged(type: Pencilcase.EditType?)
//    fun onThicknessSelected(thickness: Pencilcase.Thickness)
//    fun onModeChanged()
//}

class PencilcaseView: ConstraintLayout, Pencilcase {
    private val clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private var screenWidth:Int = 0
    private var screenHeight:Int = 0

//    var listener: PencilcaseListener? = null

    override var editType: Pencilcase.EditType? = null
        set(value) {
            field = value
            when(value) {
                Pencilcase.EditType.pencil -> setMode(null)
                else -> setMode(clear)
            }
            configUI()
//            listener?.onEditTypeChanged(value)
        }

    override var penColor: Pencilcase.PenColor = Pencilcase.PenColor.black
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.paintColor = value.value
                it.paintAlpha = value.alpha
            }
        }
    override var thickness: Pencilcase.Thickness = Pencilcase.Thickness.line
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.setPaintWidthDp(value.width)
            }
//            listener?.onThicknessSelected(value)
        }
    override var memoViews: ArrayList<MemoView> = arrayListOf()
    var itemValue = ""
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var pencilBtn: ImageButton
    var clearAllBtn: Button
    var eraserBtn: ImageButton

    var pencilOptionLl: LinearLayout
    var lineBtn: ImageButton
    var thinBtn: ImageButton
    var mediumBtn: ImageButton
    var thickBtn: ImageButton

    var colorOption0Btn: LinearLayout
    var colorOption1Btn: LinearLayout
    var colorOption2Btn: LinearLayout
    var colorOption3Btn: LinearLayout

    var blackCheck: ImageView
    var redCheck: ImageView
    var yellowCheck: ImageView
    var greenCheck: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pencilcase, this)

        pencilBtn = findViewById(R.id.pencilBtn)
        clearAllBtn = findViewById(R.id.clearAllBtn)
        eraserBtn = findViewById(R.id.eraserBtn)

        pencilOptionLl = findViewById(R.id.pencilOptionLl)
        lineBtn = findViewById(R.id.lineBtn)
        thinBtn = findViewById(R.id.thinBtn)
        mediumBtn = findViewById(R.id.mediumBtn)
        thickBtn = findViewById(R.id.thickBtn)

        colorOption0Btn = findViewById(R.id.colorOption0Btn)
        colorOption1Btn = findViewById(R.id.colorOption1Btn)
        colorOption2Btn = findViewById(R.id.colorOption2Btn)
        colorOption3Btn = findViewById(R.id.colorOption3Btn)

        blackCheck = findViewById(R.id.blackCheck)
        redCheck = findViewById(R.id.redCheck)
        yellowCheck = findViewById(R.id.yellowCheck)
        greenCheck = findViewById(R.id.greenCheck)

        pencilBtn.setOnClickListener {
            if (editType == Pencilcase.EditType.pencil) {

                pencilOptionLl.visibility = View.GONE
                editType = null

//                if (pencilOptionLl.visibility == View.VISIBLE) {
//                    pencilOptionLl.visibility = View.GONE
//                    editType = null
//                } else {
//                    pencilOptionLl.visibility = View.VISIBLE
//                    editType = Pencilcase.EditType.pencil
//                }
            } else {
                editType = Pencilcase.EditType.pencil
                pencilOptionLl.visibility = View.VISIBLE
            }

            clearAllBtn.visibility = View.GONE
        }

        eraserBtn.setOnClickListener {
            if (editType == Pencilcase.EditType.eraser) {

                clearAllBtn.visibility = View.GONE
                editType = null

//                if(clearAllBtn.visibility == View.VISIBLE) {
//                    clearAllBtn.visibility = View.GONE
//                    editType = null
//                } else {
//                    clearAllBtn.visibility = View.VISIBLE
//                }
            } else {
                editType = Pencilcase.EditType.eraser
                clearAllBtn.visibility = View.VISIBLE
            }

            pencilOptionLl.visibility = View.GONE
//            listener?.onEraserBtnClicked(this)
        }

        lineBtn.setOnClickListener {
            thickness = Pencilcase.Thickness.line
        }

        thinBtn.setOnClickListener {
            thickness = Pencilcase.Thickness.thin
        }

        mediumBtn.setOnClickListener {
            thickness = Pencilcase.Thickness.medium
        }

        thickBtn.setOnClickListener {
            thickness = Pencilcase.Thickness.thick
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

        clearAllBtn.setOnClickListener {
            memoViews.forEach {
                if(isVisible(it)) { // 현재 화면에 보일 때만
                    it.undoAll()
                    it.erase()
                    it.clearBitmap()
                }
            }
        }

//        writeModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
//            listener?.onModeChanged()
//        }
        configUI()
        setScreenSize()
        hideWriteMode()
    }

    // pdf 뷰어에서는 안쓴다
    private fun hideWriteMode() {
//        writeModeContainerDivider.visibility = View.GONE
//        writeModeContainer.visibility = View.GONE
    }

    fun setScreenSize() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
//            val insets = windowMetrics.windowInsets
//                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
//            windowMetrics.bounds.width() - insets.left - insets.right
            screenWidth = windowMetrics.bounds.width()
            screenHeight = windowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }
    }

    fun configUI() {
        when (editType) {
            Pencilcase.EditType.pencil -> {
                pencilBtn.isSelected = true
                eraserBtn.isSelected = false
                pencilOptionLl.visibility = View.VISIBLE
                eraserBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                pencilBtn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
            }
            Pencilcase.EditType.eraser -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = true
                pencilOptionLl.visibility = View.GONE
                eraserBtn.setBackgroundResource(R.drawable.bg_purple_100_stroke_purple_300_round)
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
            }
            else -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = false
                pencilOptionLl.visibility = View.GONE

                eraserBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
                pencilBtn.setBackgroundResource(R.drawable.bg_gray_100_stroke_gray_300_round_ripple)
            }
        }

        lineBtn.clearColorFilter()
        thinBtn.clearColorFilter()
        mediumBtn.clearColorFilter()
        thickBtn.clearColorFilter()
        val selectedColor = ContextCompat.getColor(context, R.color.gray_600)
        when(thickness) {
            Pencilcase.Thickness.line -> lineBtn.setColorFilter(selectedColor)
            Pencilcase.Thickness.thin -> thinBtn.setColorFilter(selectedColor)
            Pencilcase.Thickness.medium -> mediumBtn.setColorFilter(selectedColor)
            Pencilcase.Thickness.thick -> thickBtn.setColorFilter(selectedColor)
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
    }

    fun setDefaultState() {
        this.editType = null
        pencilOptionLl.visibility = View.GONE
        clearAllBtn.visibility = View.GONE
        configUI()
    }

    private fun setMode(mode: Xfermode?) {
        if(mode == null) {
            memoViews.forEach { it.setPencil(penColor.value, penColor.alpha, thickness.width) }
        } else {
            memoViews.forEach {
                it.setEraser(ERASE_THICK)
            }
        }
    }


    fun isVisible(view: View?): Boolean {
        if (view == null) {
            return false
        }
        if (!view.isShown) {
            return false
        }
        val actualPosition = Rect()
        view.getGlobalVisibleRect(actualPosition)
        val screen = Rect(0, 0, screenWidth, screenHeight)
        return actualPosition.intersect(screen)
    }
}