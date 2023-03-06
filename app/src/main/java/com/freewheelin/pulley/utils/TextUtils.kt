package com.freewheelin.pulley.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import com.freewheelin.pulley.model.MyLog
import java.text.DecimalFormat
import java.util.regex.Pattern

class TextUtils {
    companion object {
        private var _percentFormat: DecimalFormat? = null
        val percentFormat: DecimalFormat
        get() {
            if(_percentFormat == null)
                _percentFormat = DecimalFormat("##%")

            return _percentFormat!!
        }


        fun getSelectedTitle(logs: LinkedHashSet<MyLog>): String {
            return if(logs.size > 1)
                "'${logs.first().title}' 외 ${logs.size - 1}"
            else
                "'${logs.first().title}'"

        }
    }
}

fun TextView.underline() {
    this.paintFlags = Paint.UNDERLINE_TEXT_FLAG
}
fun CharSequence.partialUnderline(text: String, onClick: (() -> Unit)? = null): CharSequence {
    return this.partialUnderline(this.indexOf(text), this.indexOf(text) + text.length, onClick)
}

fun CharSequence.partialUnderline(from: Int, to: Int, onClick: (() -> Unit)? = null): CharSequence {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(UnderlineSpan(), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(object : ClickableSpan() {
        override fun onClick(p0: View) {
            if (onClick != null)
                onClick()
        }
    }, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return sb
}

fun CharSequence.partialFont(font: Typeface, text: String): CharSequence {
    return this.partialFont(font, this.indexOf(text), this.indexOf(text) + text.length)
}

fun CharSequence.partialFontAndColored(font: Typeface, color: Int, text: String): CharSequence {
    return this.partialFontAndColored(font, color, this.indexOf(text), this.indexOf(text) + text.length)
}

fun CharSequence.partialFont(font: Typeface, from: Int, to: Int): SpannableStringBuilder {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return sb
}

fun CharSequence.partialFontAndColored(font: Typeface, color: Int, from: Int, to: Int): CharSequence {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(ForegroundColorSpan(color), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return sb
}

fun String.partialFont(font: Typeface, from: Int, to: Int): SpannableStringBuilder {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return sb
}

fun SpannableStringBuilder.partialFont(font: Typeface, from: Int, to: Int): SpannableStringBuilder {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return sb
}

fun String.partialFontAndColoredWithSize(font: Typeface, color: Int, size: Float, from: Int, to: Int): SpannableStringBuilder {
    val sb = SpannableStringBuilder(this)
    sb.setSpan(ForegroundColorSpan(color), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(RelativeSizeSpan(size), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sb.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return sb
}

fun SpannableStringBuilder.partialFontAndColored(font: Typeface, color: Int, from: Int, to: Int): SpannableStringBuilder {
    this.setSpan(ForegroundColorSpan(color), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.setSpan(CustomTypefaceSpan("", font), from, to, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    return this
}

fun String.isValidName(): Boolean {
    return Pattern.compile(
            "^([가-힣]{2,15})|[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*\$"

    ).matcher(this).matches() && length >= 2 && length <= 15 // 길이 2 이상 15 이하
}

fun String.isValidPhoneNum(): Boolean {
    return Pattern.compile(
            "^(010)[0-9]{8}|(01[1-9])[0-9]{7}\$"
    ).matcher(this).matches() // 길이 10 이상 11 이하
}

fun String.isValidEmail(): Boolean {
    return this.isNotEmpty() && EMAIL_ADDRESS.matcher(this).matches()
}

val EMAIL_ADDRESS = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,64}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,256}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
)

fun String.isValidPW(): Boolean {
    val needAlphabet = "(?=.*[A-Za-z])"
    val needSpecial = "(?=.*[^A-Za-z0-9])"
    val needNumber = "(?=.*[0-9])"
    val blockWhiteSpace = "(?=\\S+$)"

    val digit_string = "$needNumber$needAlphabet$blockWhiteSpace.{6,25}"
    val string_special = "$needAlphabet$needSpecial$blockWhiteSpace.{6,25}"
    val special_digit = "$needNumber$needSpecial$blockWhiteSpace.{6,25}"
    val digit_string_special = "$needNumber$needAlphabet$needSpecial$blockWhiteSpace.{6,25}"

    return Pattern.compile(
            "^$digit_string_special|$digit_string|$string_special|$special_digit\$"

    ).matcher(this).matches()
}

fun String.isContainDigit(): Boolean {
    return Pattern.compile(
            "(?=.*[0-9]).+").matcher(this).matches()

}

fun String.isContainAlphabet(): Boolean {
    return Pattern.compile(
            "(?=.*[A-Za-z]).+"
    ).matcher(this).matches()
}

fun String.isContainSpecial(): Boolean {
    return Pattern.compile(
            "(?=.*[^A-Za-z0-9])(?=\\S+$).+"
    ).matcher(this).matches()
}

fun String.toHtml() : Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        return Html.fromHtml(this)
    }
}

