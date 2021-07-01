package com.freewheelin.pulley.bases

import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R


open class BaseNavActivity: BaseActivity() {
    open val title: String = ""
    open val backTintColor: Int = Color.WHITE

    override fun onSupportNavigateUp(): Boolean {
        this.onBackPressed()
        return true
    }

    fun setToolbar(toolbar: Toolbar) {
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val backDrawable = ContextCompat.getDrawable(this, R.drawable.ic_toolbar_back)

        backDrawable!!.setTint(backTintColor)
        supportActionBar!!.setHomeAsUpIndicator(backDrawable)
    }
}