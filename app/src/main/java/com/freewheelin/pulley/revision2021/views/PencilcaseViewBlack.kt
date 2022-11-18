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
        memoViews.forEach {
            if (mode == null) it.setPencil(penColor.value, penColor.alpha, thickness.width)
            else it.setEraser(ERASE_THICK)
        }
    }
    override var penColor: CookingPencilcase.PenColor = CookingPencilcase.PenColor.black
        set(value) {
            field = value
            configUI()
            memoViews.forEach {
                it.paintColor = value.value
                it.paintAlpha = value.alpha
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

    var pencilBtn: ImageButton
    var eraserBtn: ImageButton
    var lineBtn: ImageButton
    var thinBtn: ImageButton
    var mediumBtn: ImageButton
    var thickBtn: ImageButton

    var colorOption0Btn: LinearLayout
    var colorOption1Btn: LinearLayout
    var colorOption2Btn: LinearLayout
    var colorOption3Btn: LinearLayout

    var blackCheck: ImageView
    var yellowCheck: ImageView
    var redCheck: ImageView
    var greenCheck: ImageView

    var writeModeSwitch: Switch
    var clearAllBtn: Button
    var pencilOptionLl: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.view_cooking_pencilcase, this)

        pencilBtn = findViewById(R.id.pencilBtn)
        eraserBtn = findViewById(R.id.eraserBtn)
        lineBtn = findViewById(R.id.lineBtn)
        thinBtn = findViewById(R.id.thinBtn)
        mediumBtn = findViewById(R.id.mediumBtn)
        thickBtn = findViewById(R.id.thickBtn)

        colorOption0Btn = findViewById(R.id.colorOption0Btn)
        colorOption1Btn = findViewById(R.id.colorOption1Btn)
        colorOption2Btn = findViewById(R.id.colorOption2Btn)
        colorOption3Btn = findViewById(R.id.colorOption3Btn)

        blackCheck = findViewById(R.id.blackCheck)
        yellowCheck = findViewById(R.id.yellowCheck)
        redCheck = findViewById(R.id.redCheck)
        greenCheck = findViewById(R.id.greenCheck)

        writeModeSwitch = findViewById(R.id.writeModeSwitch)
        clearAllBtn = findViewById(R.id.clearAllBtn)
        pencilOptionLl = findViewById(R.id.pencilOptionLl)

        pencilBtn.setOnClickListener {

            if (editType == CookingPencilcase.EditType.pencil) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                    editType = CookingPencilcase.EditType.pencil
                }
            } else {
                editType = CookingPencilcase.EditType.pencil
                pencilOptionLl.visibility = View.VISIBLE
            }

            clearAllBtn.visibility = View.GONE

        }

        eraserBtn.setOnClickListener {
            if (editType == CookingPencilcase.EditType.eraser)
                if(clearAllBtn.visibility == View.VISIBLE) {
                    clearAllBtn.visibility = View.GONE
                    editType = null
                } else {
                    clearAllBtn.visibility = View.VISIBLE
                }
            else {
                editType = CookingPencilcase.EditType.eraser
                clearAllBtn.visibility = View.VISIBLE
            }

            pencilOptionLl.visibility = View.GONE
//            listener?.onEraserBtnClicked(this)
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

        clearAllBtn.setOnClickListener {
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

    fun configUI() {
        when (editType) {
            CookingPencilcase.EditType.pencil -> {
                pencilBtn.isSelected = true
                eraserBtn.isSelected = false
                pencilOptionLl.visibility = View.VISIBLE
                pencilBtn.setImageResource(R.drawable.ic_npot_pencil_filled)
                eraserBtn.setImageResource(R.drawable.ic_npot_eraser)
            }
            CookingPencilcase.EditType.eraser -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = true
                pencilOptionLl.visibility = View.GONE
                pencilBtn.setImageResource(R.drawable.ic_npot_pencil)
                eraserBtn.setImageResource(R.drawable.ic_npot_eraser_filled)
            }
            else -> {
                pencilBtn.isSelected = false
                eraserBtn.isSelected = false
                pencilOptionLl.visibility = View.GONE
                pencilBtn.setImageResource(R.drawable.ic_npot_pencil)
                eraserBtn.setImageResource(R.drawable.ic_npot_eraser)
            }
        }

        lineBtn.clearColorFilter()
        thinBtn.clearColorFilter()
        mediumBtn.clearColorFilter()
        thickBtn.clearColorFilter()
        val selectedColor = ContextCompat.getColor(context, R.color.grey_9f9f9f)
        when(thickness) {
            CookingPencilcase.Thickness.line -> lineBtn.setColorFilter(selectedColor)
            CookingPencilcase.Thickness.thin -> thinBtn.setColorFilter(selectedColor)
            CookingPencilcase.Thickness.medium -> mediumBtn.setColorFilter(selectedColor)
            CookingPencilcase.Thickness.thick -> thickBtn.setColorFilter(selectedColor)
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

    fun setDefaultState() {
        this.editType = null
        pencilOptionLl.visibility = View.GONE
        clearAllBtn.visibility = View.GONE
        configUI()
    }
}