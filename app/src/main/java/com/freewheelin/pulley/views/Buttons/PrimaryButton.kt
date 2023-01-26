package com.freewheelin.pulley.views.buttons

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.ScreenTheme
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.dialogs.PulleyPlusPriceDialog
import com.freewheelin.pulley.utils.DialogUtils

enum class ButtonTheme {
    Primary_Blue,
    Primary_Orange,
    Primary_Black,

    Secondary_Blue,
    Secondary_Grey;

    fun getTextColor(context: Context): Int {
        return when(this) {
            Primary_Black, Primary_Orange, Primary_Blue -> ContextCompat.getColor(context, R.color.white_ffffff)
            Secondary_Grey -> ContextCompat.getColor(context, R.color.black_4c4c4c)
            Secondary_Blue -> ContextCompat.getColor(context, R.color.purple_6D6DFF)
        }
    }

    fun getBgDrawable(context: Context): Drawable {
        return when(this) {
            Primary_Blue -> ContextCompat.getDrawable(context, R.drawable.bg_purple_6d6dff_round)!!
            Primary_Black -> ContextCompat.getDrawable(context, R.drawable.bg_black_4c4c4c_round)!!
            Primary_Orange -> ContextCompat.getDrawable(context, R.drawable.bg_yellow_ffb300_round)!!
            Secondary_Blue -> ContextCompat.getDrawable(context, R.drawable.rp_bg_purple_ecebff_round)!!
            Secondary_Grey -> ContextCompat.getDrawable(context, R.drawable.bg_grey_f2f2f2_round)!!
        }
    }

    fun getTextTypeface(context: Context): Typeface {
        return when(this) {
            Primary_Orange, Primary_Blue, Primary_Black, Secondary_Blue -> Theme.extraBold(context)
            Secondary_Grey -> Theme.bold(context)
        }
    }

    fun getLoadingAnim(): String {
        return when(this) {
            Primary_Blue -> "btn_loading_white.json"
            else -> "btn_loading_purple.json"
        }
    }
}

enum class ButtonLockImage {
    small16, mid20, mid24, big48
}

enum class ButtonMode {
    normal, pulley_plus
}

enum class ButtonLockColor {
    white, purple
}

open class PrimaryButton: ConstraintLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var buttonText: TextView
    var imageLock: ImageView
    var button: Button
    var lottie: LottieAnimationView

    init {
        LayoutInflater.from(context).inflate(R.layout.button_primarybutton, this)
        theme = ButtonTheme.Primary_Blue

        imageLock = findViewById(R.id.imageLock)
        button = findViewById(R.id.button)
        buttonText = findViewById(R.id.buttonText)
        lottie = findViewById(R.id.lottie)
    }

    private var mode = ButtonMode.normal
    private var hasPulleyPlus = false

    var screenTheme: ScreenTheme = ScreenTheme.Bright
        set(value) {
            if(isEnableUI == false)
                toDisableUI()
            field = value
        }

    var theme: ButtonTheme
        set(value) {
            field = value
            if(isEnableUI)
                toEnableUI()
        }

    var text: CharSequence = ""
        set(value) {
            field = value
            buttonText.text = field
        }

    private var isEnableUI: Boolean = true
//    var enableFlag: Boolean = true
//    set(value) {
//        field = value
//        if (value) toEnableUI() else toDisableUI()
//    }

    fun setLock(hasPulleyPlus:Boolean,
                lockImage:ButtonLockImage = ButtonLockImage.small16,
                mode: ButtonMode = ButtonMode.normal,
                color: ButtonLockColor = ButtonLockColor.white) {
        this.hasPulleyPlus = hasPulleyPlus
        this.mode = mode
        when(mode) {
            ButtonMode.pulley_plus -> {
                if (!hasPulleyPlus) {
                    imageLock.visibility = View.VISIBLE
                    when (lockImage) {
                        ButtonLockImage.mid20 -> imageLock.setImageResource(R.drawable.ic_lock_20_white)
                        ButtonLockImage.mid24 -> imageLock.setImageResource(R.drawable.ic_lock_24_white)
                        ButtonLockImage.big48 -> imageLock.setImageResource(R.drawable.ic_lock_48_white)
                        else -> imageLock.setImageResource(R.drawable.ic_lock_16_white)
                    }
//                    imageLock.setColorFilter(button.currentTextColor)
                    when (color) {
                        ButtonLockColor.white -> {
                            imageLock.setColorFilter(button.currentTextColor)
                        }
                        ButtonLockColor.purple -> {
                            imageLock.setColorFilter(ContextCompat.getColor(context, R.color.purple_300))
                        }
                    }

                } else {
                    imageLock.visibility = View.GONE
                }
            }
            else -> {
                setUnlock()
            }
        }
    }

    fun setUnlock() {
        this.mode = ButtonMode.normal
        imageLock.visibility = View.GONE
    }

    fun toDisableUI() {
        isEnableUI = false
//        button.typeface = Theme.bold(context)

        when(screenTheme) {
            ScreenTheme.Bright -> {
                buttonText.setTextColor(ContextCompat.getColor(context, R.color.white_ffffff))
                button.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_6d6dff_round_non_ripple)
            }

            ScreenTheme.BrightOutside -> {
                buttonText.setTextColor(ContextCompat.getColor(context, R.color.white_4cffffff))
                button.background = ContextCompat.getDrawable(context, R.drawable.bg_purple_6d6dff_round_non_ripple)
            }

            ScreenTheme.Dark -> {
//                button.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                buttonText.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                button.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_3d3d3d_round)
            }
        }
    }

    fun toProcessingUI() {
        isEnableUI = true
//        button.typeface = Theme.bold(context)
        when(screenTheme) {
            ScreenTheme.Bright, ScreenTheme.BrightOutside -> {
//                button.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                buttonText.setTextColor(ContextCompat.getColor(context, R.color.purple_6D6DFF))
                button.background = ContextCompat.getDrawable(context, R.drawable.rp_bg_purple_ecebff_round)!!
            }

            ScreenTheme.Dark -> {
//                button.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                buttonText.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
                button.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_3d3d3d_round)
            }
        }
    }

    fun toEnableUI() {
        isEnableUI = true
//        button.typeface = theme.getTextTypeface(context)
//        button.setTextColor(theme.getTextColor(context))
        buttonText.setTextColor(theme.getTextColor(context))
        button.background = theme.getBgDrawable(context)
    }

    fun isEnableUI(): Boolean {
        return isEnableUI
    }

    open fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PrimaryButton)
        val rawButtonTheme = array.getInt(R.styleable.PrimaryButton_buttonTheme, 0)
        this.theme = getButtonTheme(rawButtonTheme)
        val rawScreenTheme = array.getInt(R.styleable.PrimaryButton_screenTheme, 0)
        this.screenTheme = getScreenTheme(rawScreenTheme)

        val buttonTextSize: Float = array.getDimension(R.styleable.PrimaryButton_ButtonTextSize,
                resources.getDimension(R.dimen.sp16))

        buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize)

        val set = intArrayOf(
                android.R.attr.background, // idx 0
                android.R.attr.text// idx 1
        )

        val androidAttrs = context.obtainStyledAttributes(attrs, set)
        text = androidAttrs.getText(set.indexOf(android.R.attr.text))
        androidAttrs.recycle()
        array.recycle()
    }

    open fun getButtonTheme(rawValue: Int): ButtonTheme {
        return when(rawValue) {
            0 -> ButtonTheme.Primary_Blue
            1 -> ButtonTheme.Primary_Orange
            else -> ButtonTheme.Primary_Black
        }
    }

    fun getScreenTheme(rawValue: Int): ScreenTheme {
        return when(rawValue) {
            0 -> ScreenTheme.Bright
            1 -> ScreenTheme.Dark
            else -> ScreenTheme.BrightOutside
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        when(mode) {
            ButtonMode.pulley_plus -> {
                if(hasPulleyPlus)
                    button.setOnClickListener(listener)
                else
                    button.setOnClickListener {
                        DialogUtils.confirmHasPulleyPlus(context) {
                            PulleyPlusPriceDialog(context).show()
                        }
                    }
            }
            else -> {
                button.setOnClickListener(listener)
            }
        }
    }

    fun startLoding() {
        button.isEnabled = false
        buttonText.text = ""
        lottie.setAnimation(theme.getLoadingAnim())
        lottie.visibility = View.VISIBLE
        lottie.playAnimation()
    }

    fun completeLoading() {
        button.isEnabled = true
        buttonText.text = text
        lottie.visibility = View.INVISIBLE
        lottie.pauseAnimation()
    }
}