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
            // The ClassCastException happens in AppCompatSpinner before View.onRestoreInstanceState
            // runs, so PFLAG_SAVE_STATE_CALLED is never set and the framework would then throw
            // "Derived class did not call super.onRestoreInstanceState()".
            // Restore a valid, self-typed state instead to satisfy the View contract; the spinner
            // keeps its initial selection, recoverable via data-binding / next user interaction.
            super.onRestoreInstanceState(super.onSaveInstanceState())
        }
    }
}
