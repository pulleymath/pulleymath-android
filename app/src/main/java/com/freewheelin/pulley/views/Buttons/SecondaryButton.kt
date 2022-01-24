package com.freewheelin.pulley.views.Buttons

import android.content.Context
import android.util.AttributeSet

class SecondaryButton: PrimaryButton {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)


    override fun getButtonTheme(rawValue: Int): ButtonTheme {
        return when(rawValue) {
            0 -> ButtonTheme.Secondary_Blue
            else -> ButtonTheme.Secondary_Grey
        }
    }
}