package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.visibleIf

open class CommonButton: ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setTypedArray(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setTypedArray(attrs)
    }

    fun setTypedArray(attributes: AttributeSet?) {
        attributes.let { attrs ->
            context.obtainStyledAttributes(attrs, R.styleable.CommonButton).let { array ->

                val showStartIcon = array.getBoolean(R.styleable.CommonButton_showStartIcon, false)
                startIcon.visibleIf(showStartIcon)
                val startIconRawValue = array.getInt(R.styleable.CommonButton_startIcon, 0)
                startIcon.setImageResource(getStartIconSource(startIconRawValue))

                val showEndIcon = array.getBoolean(R.styleable.CommonButton_showEndIcon, false)
                endIcon.visibleIf(showEndIcon)
                val endIconRawValue = array.getInt(R.styleable.CommonButton_endIcon, 0)
                endIcon.setImageResource(getEndIconSource(endIconRawValue))

                val buttonType = array.getInt(R.styleable.CommonButton_buttonType, 0)

                setComponentColorOnType(buttonType)
                setTextColorFromTypedArray(array) // type의 글씨색을 override 한다

                val text = array.getString(R.styleable.CommonButton_android_text)
                title.text = text

                val sp18 = resources.getDimension(R.dimen.sp18)
                val textSize = array.getDimensionPixelSize(R.styleable.CommonButton_android_textSize, sp18.toInt())
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())

                array.recycle()
            }

            val propertyArray = intArrayOf(android.R.attr.background, android.R.attr.fontFamily)

            context.obtainStyledAttributes(attrs, propertyArray).let { androidAttrs ->
                val fontAttr = androidAttrs.getResourceId(propertyArray.indexOf(android.R.attr.fontFamily), R.font.font_pretendard_bold)
                title.typeface = ResourcesCompat.getFont(context, fontAttr)
                androidAttrs.recycle()
            }
        }
    }
    private fun setComponentColorOnType(buttonType: Int) {
        this.type = buttonType

        rootCl.setBackgroundResource(getEnabledBgByType())
        val contentsColor = getContentsColorByType()
        setIconColor(contentsColor)
        setTextColor(contentsColor)
    }

    var rootCl: ConstraintLayout
    var contentsCl: ConstraintLayout
    var startIcon: ImageView
    var endIcon: ImageView
    var title: TextView
    var lottie: LottieAnimationView

    var type: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_common_button, this)
        rootCl = findViewById(R.id.rootCl)
        contentsCl = findViewById(R.id.contentsCl)
        startIcon = findViewById(R.id.startIcon)
        endIcon = findViewById(R.id.endIcon)
        title = findViewById(R.id.titleTv)
        lottie = findViewById(R.id.lottie)
//        isEnabled = true
        setDefaultIcon()
    }

    var text: CharSequence = ""
        set(value) {
            field = value
            title.text = field
        }
    fun showStartIcon(value: Boolean) {
        startIcon.visibleIf(value)
    }

    fun showEndIcon(value: Boolean) {
        endIcon.visibleIf(value)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            rootCl.setBackgroundResource(getEnabledBgByType())
            val contentsColor = getContentsColorByType()
            setIconColor(contentsColor)
            setTextColor(contentsColor)
        } else {
            rootCl.setBackgroundResource(getDisabledBgByType())
            val contentsColor = getContentsDisabledColorByType()
            setIconColor(contentsColor)
            setTextColor(contentsColor)
        }
    }
    fun setLoading(value: Boolean) {
        lottie.visibleIf(value)
        contentsCl.visibleIf(!value)
        if (value) {
            lottie.playAnimation()
        } else {
            lottie.cancelAnimation()
        }
    }

    private fun setDefaultIcon() {
        startIcon.setImageResource(R.drawable.ic_lock_20_white)
        endIcon.setImageResource(R.drawable.ic_tailless_arrow_right_24)
        setIconColor()
    }
    private fun getStartIconSource(rawValue: Int): Int {
        return when(rawValue) {
            1 -> R.drawable.ic_crown
            // 새로 필요한 source는 attrs에 enum으로 추가후 정의해주세요
            else -> R.drawable.ic_lock_20_white
        }
    }
    private fun getEndIconSource(rawValue: Int): Int {
        return when(rawValue) {
            // 새로 필요한 source는 attrs에 enum으로 추가후 정의해주세요
            1 -> R.drawable.ic_lock_20_white
            else -> R.drawable.ic_tailless_arrow_right_24
        }
    }

    open fun getDisabledBgByType(): Int {
        return when (type) {
//            1 -> R.drawable.bg_purple_100_round_disabled
//            2 -> R.drawable.bg_gray_300_round_disabled
            1 -> R.drawable.bg_yellow_300_round_disabled
            else -> R.drawable.bg_purple_300_round_non_ripple
        }
    }
    open fun getEnabledBgByType(): Int {
        return when (type) {
//            1 -> R.drawable.bg_purple_100_round_ripple
//            2 -> R.drawable.bg_gray_300_round_ripple
            1 -> R.drawable.bg_yellow_300_round_ripple
            else -> R.drawable.bg_purple_300_round_ripple
        }
    }
    open fun getContentsColorByType(): Int {
        return when (type) {
//            1 -> R.color.purple_300
//            2 -> R.color.gray_800
            1 -> R.color.white
            else -> R.color.white
        }
    }
    open fun getContentsDisabledColorByType(): Int {
        return when (type) {
//            1 -> R.color.purple_300_disabled
//            2 -> R.color.gray_800_disabled
            1 -> R.color.white
            else -> R.color.white
        }
    }
    fun setIconColor(color: Int = R.color.white) {
        startIcon.setColorFilter(ContextCompat.getColor(context, color))
        endIcon.setColorFilter(ContextCompat.getColor(context, color))
    }
    fun setTextColor(color: Int = R.color.white) {
        val enabledTextColor = ContextCompat.getColor(context, color)
        title.setTextColor(enabledTextColor)
    }

    open fun setTextColorFromTypedArray(array: TypedArray) {
        val textColor = array.getColor(R.styleable.CommonButton_android_textColor, Color.WHITE)
        title.setTextColor(textColor)
    }

}

@BindingAdapter("commonButton_binding_text")
fun setCommonButtonText(view: CommonButton, value: String?) {
    println("setCommonButtonText : $value")
    view.title.text = value
}
@BindingAdapter("commonButton_binding_showStartIcon")
fun showCommonButtonStartIcon(view: CommonButton, value: Boolean) {
    println("showCommonButtonStartIcon : $value")
    view.startIcon.visibleIf(value)
}
@BindingAdapter("commonButton_binding_showEndIcon")
fun showCommonButtonEndIcon(view: CommonButton, value: Boolean) {
    println("showCommonButtonEndIcon : $value")
    view.endIcon.visibleIf(value)
}
@BindingAdapter("commonButton_binding_isLoading")
fun setCommonButtonIsLoading(view: CommonButton, value: Boolean) {
    println("setCommonButtonIsLoading : $value")
    // TODO
    view.lottie.visibleIf(value)
    view.contentsCl.visibleIf(!value)
    if (value) {
        view.lottie.playAnimation()
    } else {
        view.lottie.cancelAnimation()
    }
}
@BindingAdapter("commonButton_binding_enabled")
fun setCommonButtonEnabled(view: CommonButton, value: Boolean) {
    println("setCommonButtonEnabled : $value")
    view.rootCl.setBackgroundResource(if (value) R.drawable.bg_purple_300_round_ripple else R.drawable.bg_purple_300_round_non_ripple)
    view.rootCl.isEnabled = value
}