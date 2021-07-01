package com.freewheelin.pulley.bases

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.freewheelin.pulley.model.User
import io.reactivex.disposables.CompositeDisposable


val Activity.user: User?
    get() = MyApplication.user

open class BaseActivity : AppCompatActivity() {

    protected val disposables by lazy { CompositeDisposable() }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    fun startActivity(class1: Class<out BaseActivity>) {
        val intent = Intent(applicationContext, class1)
        startActivity(intent)
    }

    companion object {
        private const val FONT_SCALE = 1.0 //0.85 small size, 1 normal size
    }

    // 터치 이벤트가 클릭인지 스크롤인지 구분하기 위한 프로퍼티
    private val CLICK_THRESHOLD = 100
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        val duration = event.eventTime - event.downTime
        if(event.action == MotionEvent.ACTION_UP && duration < CLICK_THRESHOLD) { // Down 과 Up의 시간차가 100 미만이면 클릭
            val focusView: View? = currentFocus
            if (focusView != null) {
                val rect = Rect()
                focusView.getGlobalVisibleRect(rect)
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (!rect.contains(x, y)) {
                    val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                    focusView.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}