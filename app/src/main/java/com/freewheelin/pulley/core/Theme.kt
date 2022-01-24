package com.freewheelin.pulley.core

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.freewheelin.pulley.R

enum class ScreenTheme {
    Bright,
    Dark
}

class Theme {
    companion object {

        var _light: Typeface? = null
        fun light(context: Context): Typeface {
            if(_light == null)
                _light = ResourcesCompat.getFont(context, R.font.nanum_square_light)

            return _light!!
        }

        var _regular: Typeface? = null
        fun regular(context: Context): Typeface {
            if(_regular == null)
                _regular = ResourcesCompat.getFont(context, R.font.nanum_square_regular)

            return _regular!!
        }

        var _bold: Typeface? = null
        fun bold(context: Context): Typeface {
            if(_bold == null)
                _bold = Typeface.create(regular(context), Typeface.BOLD)

            return _bold!!
        }
        var _extraBold: Typeface? = null
        fun extraBold(context: Context): Typeface {
            if(_extraBold == null)
                _extraBold = ResourcesCompat.getFont(context, R.font.nanum_square_extra_bold)

            return _extraBold!!
        }


    }
}
