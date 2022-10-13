package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.pxToSp

class LabelFlowView: androidx.appcompat.widget.AppCompatTextView {
    lateinit var title:String
    lateinit var color:String

    constructor(context: Context, title:String, color:String) : this(context, null) {
        this.title = "#$title"
        this.color = color
        if(!color.startsWith("#")){
            this.color = "#$color"
        }
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        prepare(context)
    }

    private fun prepare(context: Context){
        this.setBackgroundResource(R.drawable.bg_white_stroke_gray_400_round_40)
        val dim1dp = context.resources.getDimensionPixelSize(R.dimen.dp1)
        val dim8dp = context.resources.getDimensionPixelSize(R.dimen.dp10)
        this.setPadding(dim8dp, dim1dp, dim8dp, dim1dp)
        this.gravity = Gravity.CENTER_VERTICAL
        this.setTextColor(ContextCompat.getColor(context, R.color.gray_600))
    }

    fun load() : View {
//        background.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP)
        background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_gray_400_round_40)
        typeface = ResourcesCompat.getFont(context, R.font.pretendard_semibold)
        textSize = resources.getDimension(R.dimen.sp14).pxToSp()

        this.text = title
        this.setSingleLine(true)
        this.maxLines = 1
        this.ellipsize = TextUtils.TruncateAt.END
        return this
    }

}