package com.freewheelin.pulley.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.views.memoView.MemoView

interface Pencilcase {
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
    var memoViews: ArrayList<MemoView>
}


interface PencilcaseListener {
    fun onEditTypeChanged(type: Pencilcase.EditType?)
    fun onThicknessSelected(thickness: Pencilcase.Thickness)
    fun onModeChanged()
}
class PencilcaseView: ConstraintLayout, Pencilcase {
    var listener: PencilcaseListener? = null

    override var editType: Pencilcase.EditType? = null
        set(value) {
            field = value
            configUI()
            listener?.onEditTypeChanged(value)
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
            listener?.onThicknessSelected(value)
        }
    override var memoViews: ArrayList<MemoView> = arrayListOf()
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
        LayoutInflater.from(context).inflate(R.layout.view_pencilcase, this)

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

            if (editType == Pencilcase.EditType.pencil) {
                if (pencilOptionLl.visibility == View.VISIBLE) {
                    pencilOptionLl.visibility = View.GONE
                    editType = null
                } else {
                    pencilOptionLl.visibility = View.VISIBLE
                    editType = Pencilcase.EditType.pencil
                }
            } else {
                editType = Pencilcase.EditType.pencil
                pencilOptionLl.visibility = View.VISIBLE
            }

            clearAllBtn.visibility = View.GONE
            LogUtils.logEvent(context, user, PulleyEvent.BUTTON_CLICK, "바로풀기화면", "연필 아이콘", itemValue)
        }

        eraserBtn.setOnClickListener {
            if (editType == Pencilcase.EditType.eraser)
                if(clearAllBtn.visibility == View.VISIBLE) {
                    clearAllBtn.visibility = View.GONE
                    editType = null
                } else {
                    clearAllBtn.visibility = View.VISIBLE
                }
            else {
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
                it.undoAll()
            }
        }

        writeModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            listener?.onModeChanged()
        }
        configUI()
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
}