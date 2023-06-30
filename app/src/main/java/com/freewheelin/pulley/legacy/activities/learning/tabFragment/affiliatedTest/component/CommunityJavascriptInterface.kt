package com.freewheelin.pulley.legacy.activities.learning.tabFragment.affiliatedTest.component

import android.app.AlertDialog
import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class CommunityJavascriptInterface(val context: Context) {

    @JavascriptInterface
    fun showToast(toast: String?) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
        // webView.loadUrl("javascript:document.getElementById(\"Button3\").innerHTML = \"bye\";");
    }

    @JavascriptInterface
    fun openAndroidDialog() {
        val myDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        myDialog.setTitle("DANGER!")
        myDialog.setMessage("You can do what you want!")
        myDialog.setPositiveButton("ON", null)
        myDialog.show()
    }
}