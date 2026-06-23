package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

open class SafeSpinner : AppCompatSpinner {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, mode: Int) : super(context, mode)

    override fun onRestoreInstanceState(state: Parcelable?) {
        try {
            super.onRestoreInstanceState(state)
        } catch (_: ClassCastException) {
            // Saved state belongs to a different view type (id collision across layouts).
            // Skip restoration; the spinner remains in its initial state, which is recoverable
            // by the next user interaction or by the data-binding layer rebinding the selection.
        }
    }
}
